/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.converter;

import com.google.auto.service.AutoService;
import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.powerfactory.model.*;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Importer.class)
public class PowerFactoryImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerFactoryImporter.class);

    private static final String FORMAT = "POWER-FACTORY";

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public String getComment() {
        return "PowerFactory to IIDM converter";
    }

    private Optional<ProjectLoader> findProjectLoader(ReadOnlyDataSource dataSource) {
        for (ProjectLoader projectLoader : ServiceLoader.load(ProjectLoader.class)) {
            try {
                if (dataSource.exists(null, projectLoader.getExtension())) {
                    return Optional.of(projectLoader);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        return findProjectLoader(dataSource).filter(projectLoader -> {
            try (InputStream is = dataSource.newInputStream(null, projectLoader.getExtension())) {
                return projectLoader.test(is);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).isPresent();
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        findProjectLoader(fromDataSource).ifPresent(projectLoader -> {
            try (InputStream is = fromDataSource.newInputStream(null, projectLoader.getExtension());
                 OutputStream os = toDataSource.newOutputStream(null, projectLoader.getExtension(), false)) {
                ByteStreams.copy(is, os);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    static class ImportContext {

        final ContainersMapping containerMapping;

        // object id to node (IIDM node/breaker node) mapping
        final Map<Long, List<NodeRef>> objIdToNode = new HashMap<>();

        final Map<String, MutableInt> nodeCountByVoltageLevelId = new HashMap<>();

        List<DataObject> cubiclesObjectNotFound = new ArrayList<>();

        ImportContext(ContainersMapping containerMapping) {
            this.containerMapping = containerMapping;
        }
    }

    static class NodeRef {

        final String voltageLevelId;
        final int node;

        NodeRef(String voltageLevelId, int node) {
            this.voltageLevelId = voltageLevelId;
            this.node = node;
        }

        @Override
        public String toString() {
            return "NodeRef(voltageLevelId='" + voltageLevelId + '\'' +
                    ", node=" + node +
                    ')';
        }
    }

    private static List<NodeRef> checkNodes(DataObject obj, Map<Long, List<NodeRef>> objIdToNode, int connections) {
        List<NodeRef> nodeRefs = objIdToNode.get(obj.getId());
        if (nodeRefs == null || nodeRefs.size() != connections) {
            throw new PowsyblException("Inconsistent number (" + (nodeRefs != null ? nodeRefs.size() : 0)
                    + ") of connection for '" + obj + "'");
        }
        return nodeRefs;
    }

    private Network createNetwork(Project project, NetworkFactory networkFactory) {
        List<StudyCase> studyCases = project.getStudyCases();
        LOGGER.info("Project has {} study case(s): {}", studyCases.size(), studyCases.stream().map(StudyCase::getName).collect(Collectors.toList()));
        StudyCase studyCase = project.findActiveStudyCase().orElseThrow(() -> new PowsyblException("Active study case not found"));
        LOGGER.info("Active study case is '{}'", studyCase.getName());
        List<DataObject> elmNets = studyCase.getElmNets();
        if (elmNets.isEmpty()) {
            throw new PowsyblException("No ElmNet object found");
        }
        LOGGER.info("Active study case has {} network(s): {}", elmNets.size(), elmNets.stream().map(DataObject::getName).collect(Collectors.toList()));
        List<NetworkVariation> variations = studyCase.getNetworkVariations();
        LOGGER.info("Active study case has {} network variation(s): {}", variations.size(),
                variations.stream().map(NetworkVariation::getName).collect(Collectors.toList()));
        for (NetworkVariation variation : variations) {
            List<NetworkExpansionStage> expansionStages = variation.getActiveExpansionStages();
            LOGGER.info("Network variation '{}' has {} active expansion stage(s): {}", variation.getName(), expansionStages.size(),
                    expansionStages.stream().map(NetworkExpansionStage::getName).collect(Collectors.toList()));
        }

        // get all network objects resulting from base network and active expansion stages
        List<DataObject> objs = studyCase.applyNetworkExpansionStages();

        Network network = networkFactory.createNetwork(project.getRootObject().toString(), FORMAT);

        // case date
        DateTime caseDate = new Instant(studyCase.getTime().toEpochMilli()).toDateTime();
        network.setCaseDate(caseDate);

        List<DataObject> elmTerms = objs.stream()
                .filter(obj -> obj.getDataClassName().equals("ElmTerm"))
                .collect(Collectors.toList());

        LOGGER.info("Creating containers...");

        ContainersMapping containerMapping = ContainersMappingHelper.create(project, elmTerms);
        ImportContext importContext = new ImportContext(containerMapping);

        LOGGER.info("Creating topology graphs...");

        // process terminals
        for (DataObject elmTerm : elmTerms) {
            createNode(network, importContext, elmTerm);
        }

        if (!importContext.cubiclesObjectNotFound.isEmpty()) {
            LOGGER.warn("{} cubicles have a missing connected object", importContext.cubiclesObjectNotFound.size());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Cubicles with missing connected object: {}", importContext.cubiclesObjectNotFound);
            }
        }

        LOGGER.info("Creating equipments...");

        for (DataObject obj : objs) {
            switch (obj.getDataClassName()) {
                case "ElmCoup":
                    createSwitch(network, importContext, obj);
                    break;

                case "ElmSym":
                case "ElmAsm":
                case "ElmGenstat":
                    createGenerator(network, importContext, obj);
                    break;

                case "ElmLod":
                    createLoad(network, importContext, obj);
                    break;

                case "ElmShnt":
                    createShunt(network, importContext, obj);
                    break;

                case "ElmLne":
                    createLine(network, importContext, obj);
                    break;

                case "ElmTr2":
                    create2wTransformer(network, importContext, obj);
                    break;

                case "ElmTr3":
                    create3wTransformer(network, importContext, obj);
                    break;

                case "ElmZpu":
                    // TODO
                    break;

                case "ElmNet":
                case "StaCubic":
                case "ElmTerm":
                case "ElmSubstat":
                case "ElmTrfstat":
                case "StaSwitch":
                    // already processed
                    break;

                case "ElmDsl":
                case "ElmComp":
                case "ElmStactrl":
                case "ElmPhi__pll":
                case "ElmSecctrl":
                case "StaPqmea":
                case "StaVmea":
                case "ElmFile":
                case "ElmZone":
                    // not interesting
                    break;

                default:
                    LOGGER.warn("Unexpected data class '{}' ('{}')", obj.getDataClassName(), obj.toString());
            }
        }

        LOGGER.info("{} substations, {} voltage levels, {} lines, {} 2w-transformers, {} 3w-transformers, {} generators, {} loads, {} shunts have been created",
                network.getSubstationCount(), network.getVoltageLevelCount(), network.getLineCount(), network.getTwoWindingsTransformerCount(),
                network.getThreeWindingsTransformerCount(), network.getGeneratorCount(), network.getLoadCount(), network.getShuntCompensatorCount());

        return network;
    }

    private void createLoad(Network network, ImportContext importContext, DataObject elmLod) {
        NodeRef nodeRef = checkNodes(elmLod, importContext.objIdToNode, 1).iterator().next();
        VoltageLevel vl = network.getVoltageLevel(nodeRef.voltageLevelId);
        float p0 = elmLod.getFloatAttributeValue("plini");
        float q0 = elmLod.getFloatAttributeValue("qlini");
        vl.newLoad()
                .setId(elmLod.getName())
                .setEnsureIdUnicity(true)
                .setNode(nodeRef.node)
                .setP0(p0)
                .setQ0(q0)
                .add();
    }

    private void createShunt(Network network, ImportContext importContext, DataObject elmShnt) {
        NodeRef nodeRef = checkNodes(elmShnt, importContext.objIdToNode, 1).iterator().next();
        VoltageLevel vl = network.getVoltageLevel(nodeRef.voltageLevelId);
        int shtype = elmShnt.getIntAttributeValue("shtype");
        double gPerSection;
        double bPerSection;
        if (shtype == 1) { // RL
            float rrea = elmShnt.getFloatAttributeValue("rrea");
            float xrea = elmShnt.getFloatAttributeValue("xrea");
            if (rrea == 0) {
                gPerSection = 0;
                bPerSection = -1 / xrea;
            } else {
                throw new PowsyblException("Cannot convert RL shunt");
            }
        } else if (shtype == 2) { // C
            float gparac = elmShnt.getFloatAttributeValue("gparac");
            float bcap = elmShnt.getFloatAttributeValue("bcap");
            gPerSection = gparac * Math.pow(10, -6);
            bPerSection = bcap * Math.pow(10, -6);
        } else {
            throw new PowsyblException("Shunt type not supported: " + shtype);
        }
        int ncapa = elmShnt.getIntAttributeValue("ncapa");
        int ncapx = elmShnt.getIntAttributeValue("ncapx");
        vl.newShuntCompensator()
                .setId(elmShnt.getName())
                .setEnsureIdUnicity(true)
                .setNode(nodeRef.node)
                .setSectionCount(ncapa)
                .newLinearModel()
                    .setGPerSection(gPerSection)
                    .setBPerSection(bPerSection)
                    .setMaximumSectionCount(ncapx)
                .add()
                .add();
    }

    private void createGenerator(Network network, ImportContext importContext, DataObject elmSym) {
        NodeRef nodeRef = checkNodes(elmSym, importContext.objIdToNode, 1).iterator().next();
        VoltageLevel vl = network.getVoltageLevel(nodeRef.voltageLevelId);
        int ivMode = elmSym.getIntAttributeValue("iv_mode");
        float pgini = elmSym.getFloatAttributeValue("pgini");
        float qgini = elmSym.getFloatAttributeValue("qgini");
        double usetp = elmSym.getFloatAttributeValue("usetp") * vl.getNominalV();
        double pMinUc = elmSym.getFloatAttributeValue("Pmin_uc");
        double pMaxUc = elmSym.getFloatAttributeValue("Pmax_uc");
        Generator g = vl.newGenerator()
                .setId(elmSym.getName())
                .setEnsureIdUnicity(true)
                .setNode(nodeRef.node)
                .setTargetP(pgini)
                .setTargetQ(qgini)
                .setTargetV(usetp)
                .setVoltageRegulatorOn(ivMode == 1)
                .setMinP(pMinUc)
                .setMaxP(pMaxUc)
                .add();
        elmSym.findObjectAttributeValue("typ_id").ifPresent(typSym -> createReactiveLimits(elmSym, typSym, g));
    }

    private void createReactiveLimits(DataObject elmSym, DataObject typSym, Generator g) {
        if (typSym.getDataClassName().equals("TypSym")) {
            DataObject pQlimType = elmSym.findObjectAttributeValue("pQlimType").orElse(null);
            if (pQlimType != null) {
                throw new PowsyblException("Reactive capability curve not supported: '" + elmSym + "'");
            } else {
                int iqtype = elmSym.getIntAttributeValue("iqtype");
                float qMinPu;
                float qMaxPu;
                if (iqtype == 0) { // use limits specified in element
                    qMinPu = elmSym.getFloatAttributeValue("q_min");
                    qMaxPu = elmSym.getFloatAttributeValue("q_max");
                } else { // use limits specified in type
                    qMinPu = typSym.getFloatAttributeValue("q_min");
                    qMaxPu = typSym.getFloatAttributeValue("q_max");
                }
                float qMinTyp = typSym.getFloatAttributeValue("Q_min");
                float qMaxTyp = typSym.getFloatAttributeValue("Q_max");
                double minQ = -1 * qMinPu * qMinTyp;
                double maxQ = qMaxPu * qMaxTyp;
                g.newMinMaxReactiveLimits()
                        .setMinQ(minQ)
                        .setMaxQ(maxQ)
                        .add();
            }
        } else {
            // TODO
        }
    }

    private void createLine(Network network, ImportContext importContext, DataObject elmLne) {
        Collection<NodeRef> nodeRefs = checkNodes(elmLne, importContext.objIdToNode, 2);
        Iterator<NodeRef> it = nodeRefs.iterator();
        NodeRef nodeRef1 = it.next();
        NodeRef nodeRef2 = it.next();
        float dline = elmLne.getFloatAttributeValue("dline");
        DataObject typLne = elmLne.getObjectAttributeValue("typ_id");
        float rline = typLne.getFloatAttributeValue("rline");
        float xline = typLne.getFloatAttributeValue("xline");
        float bline = typLne.getFloatAttributeValue("bline");
        float tline = typLne.getFloatAttributeValue("tline");
        double r = rline * dline;
        double x = xline * dline;
        double g = tline * dline * 10e-6;
        double b = bline * dline * 10e-6;
        double g1 = g / 2;
        double g2 = g / 2;
        double b1 = b / 2;
        double b2 = b / 2;
        network.newLine()
                .setId(elmLne.getName())
                .setEnsureIdUnicity(true)
                .setVoltageLevel1(nodeRef1.voltageLevelId)
                .setVoltageLevel2(nodeRef2.voltageLevelId)
                .setNode1(nodeRef1.node)
                .setNode2(nodeRef2.node)
                .setR(r)
                .setX(x)
                .setG1(g1)
                .setG2(g2)
                .setB1(b1)
                .setB2(b2)
                .add();
    }

    private void create2wTransformer(Network network, ImportContext importContext, DataObject elmTr2) {
        Collection<NodeRef> nodeRefs = checkNodes(elmTr2, importContext.objIdToNode, 2);
        Iterator<NodeRef> it = nodeRefs.iterator();
        NodeRef nodeRef1 = it.next();
        NodeRef nodeRef2 = it.next();
        VoltageLevel vl1 = network.getVoltageLevel(nodeRef1.voltageLevelId);
        VoltageLevel vl2 = network.getVoltageLevel(nodeRef2.voltageLevelId);
        Substation s = vl1.getSubstation();
        DataObject typTr2 = elmTr2.getObjectAttributeValue("typ_id");
        float strn = typTr2.getFloatAttributeValue("strn");
        float utrnL = typTr2.getFloatAttributeValue("utrn_l");
        float utrnH = typTr2.getFloatAttributeValue("utrn_h");
        float uktr = typTr2.getFloatAttributeValue("uktr");
        float pcutr = typTr2.getFloatAttributeValue("pcutr");
        float curmg = typTr2.getFloatAttributeValue("curmg");
        float pfe = typTr2.getFloatAttributeValue("pfe");
        double ratedU1;
        double ratedU2;
        double primaryNominalV;
        if (vl1.getNominalV() > vl2.getNominalV()) {
            ratedU1 = utrnH;
            ratedU2 = utrnL;
            primaryNominalV = vl1.getNominalV();
        } else {
            ratedU1 = utrnL;
            ratedU2 = utrnH;
            primaryNominalV = vl2.getNominalV();
        }

        TransformerModel transformerModel = TransformerModel.fromMeasures(uktr, pcutr, curmg, pfe, strn, primaryNominalV);
        double r = transformerModel.getR();
        double x = transformerModel.getX();
        if (vl1.getNominalV() > vl2.getNominalV()) {
            double rho = ratedU2 / ratedU1;
            r *= rho * rho;
            x *= rho * rho;
        }

        // TODO
        s.newTwoWindingsTransformer()
                .setId(elmTr2.getName())
                .setEnsureIdUnicity(true)
                .setVoltageLevel1(nodeRef1.voltageLevelId)
                .setVoltageLevel2(nodeRef2.voltageLevelId)
                .setNode1(nodeRef1.node)
                .setNode2(nodeRef2.node)
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2)
                .setRatedS(strn)
                .setR(r)
                .setX(x)
                .setG(0)
                .setB(0)
                .add();
    }

    private void create3wTransformer(Network network, ImportContext importContext, DataObject elmTr3) {
        Collection<NodeRef> nodeRefs = checkNodes(elmTr3, importContext.objIdToNode, 3);
        Iterator<NodeRef> it = nodeRefs.iterator();
        NodeRef nodeRef1 = it.next();
        NodeRef nodeRef2 = it.next();
        NodeRef nodeRef3 = it.next();
        VoltageLevel vl1 = network.getVoltageLevel(nodeRef1.voltageLevelId);
        VoltageLevel vl2 = network.getVoltageLevel(nodeRef2.voltageLevelId);
        VoltageLevel vl3 = network.getVoltageLevel(nodeRef3.voltageLevelId);
        DataObject typTr3 = elmTr3.getObjectAttributeValue("typ_id");
        float utrn3L = typTr3.getFloatAttributeValue("utrn3_l");
        float utrn3H = typTr3.getFloatAttributeValue("utrn3_h");
        float utrn3M = typTr3.getFloatAttributeValue("utrn3_m");
        float[] utrn3 = {utrn3H, utrn3M, utrn3L};
        List<VoltageLevel> vls = Arrays.asList(vl1, vl2, vl3).stream()
                .sorted(Comparator.comparingDouble(VoltageLevel::getNominalV))
                .collect(Collectors.toList());
        double ratedU1 = utrn3[vls.indexOf(vl1)];
        double ratedU2 = utrn3[vls.indexOf(vl2)];
        double ratedU3 = utrn3[vls.indexOf(vl3)];
        // TODO
        Substation s = vl1.getSubstation();
        s.newThreeWindingsTransformer()
                .setId(elmTr3.getName())
                .setEnsureIdUnicity(true)
                .setRatedU0(utrn3H)
                .newLeg1()
                    .setNode(nodeRef1.node)
                    .setVoltageLevel(vl1.getId())
                    .setRatedU(ratedU1)
                    .setR(0.1)
                    .setX(1)
                    .setG(0)
                    .setB(0)
                .add()
                .newLeg2()
                    .setNode(nodeRef2.node)
                    .setVoltageLevel(vl2.getId())
                    .setRatedU(ratedU2)
                    .setR(0.1)
                    .setX(1)
                    .setG(0)
                    .setB(0)
                .add()
                .newLeg3()
                    .setNode(nodeRef3.node)
                    .setVoltageLevel(vl3.getId())
                    .setRatedU(ratedU3)
                    .setR(0.1)
                    .setX(1)
                    .setG(0)
                    .setB(0)
                .add()
                .add();
    }

    private VoltageLevel createVoltageLevel(Network network, ImportContext importContext, DataObject elmTerm) {
        String voltageLevelId = importContext.containerMapping.getVoltageLevelId(Ints.checkedCast(elmTerm.getId()));
        String substationId = importContext.containerMapping.getSubstationId(voltageLevelId);
        Substation s = network.getSubstation(substationId);
        if (s == null) {
            s = network.newSubstation()
                    .setId(substationId)
                    .add();
        }
        VoltageLevel vl = network.getVoltageLevel(voltageLevelId);
        if (vl == null) {
            float uknom = elmTerm.getFloatAttributeValue("uknom");
            vl = s.newVoltageLevel()
                    .setId(voltageLevelId)
                    .setNominalV(uknom)
                    .setTopologyKind(TopologyKind.NODE_BREAKER)
                    .add();
        }
        return vl;
    }

    private void createNode(Network network, ImportContext importContext, DataObject elmTerm) {
        VoltageLevel vl = createVoltageLevel(network, importContext, elmTerm);
        int iUsage = elmTerm.getIntAttributeValue("iUsage");
        MutableInt nodeCount = importContext.nodeCountByVoltageLevelId.computeIfAbsent(vl.getId(), k -> new MutableInt());
        int bbNode = nodeCount.intValue();
        nodeCount.increment();
        if (iUsage == 0) { // busbar
            vl.getNodeBreakerView().newBusbarSection()
                    .setId(vl.getId() + "_" + elmTerm.getName())
                    .setEnsureIdUnicity(true)
                    .setNode(bbNode)
                    .add();
        }
        for (DataObject staCubic : elmTerm.getChildren("StaCubic")) {
            DataObject connectedObj = staCubic.findObjectAttributeValue("obj_id").orElse(null);
            if (connectedObj == null) {
                importContext.cubiclesObjectNotFound.add(staCubic);
            } else {
                List<DataObject> staSwitches = staCubic.getChildren("StaSwitch");
                if (staSwitches.size() > 1) {
                    throw new PowsyblException("Multiple staSwitch not supported");
                }
                DataObject staSwitch = staSwitches.isEmpty() ? null : staSwitches.get(0);

                int node;
                if (staSwitch != null) {
                    node = nodeCount.intValue();
                    nodeCount.increment();
                    String switchId = vl.getId() + "_" + staSwitch.getName();
                    boolean open = staSwitch.findIntAttributeValue("on_off").orElse(0) == 0;
                    vl.getNodeBreakerView().newSwitch()
                            .setId(switchId)
                            .setEnsureIdUnicity(true)
                            .setKind(SwitchKind.BREAKER)
                            .setNode1(bbNode)
                            .setNode2(node)
                            .setOpen(open)
                            .add();
                } else {
                    if (connectedObj.getDataClassName().equals("ElmCoup")) {
                        // no need to create an intermediate internal node
                        node = bbNode;
                    } else {
                        node = nodeCount.intValue();
                        nodeCount.increment();
                        vl.getNodeBreakerView().newInternalConnection()
                                .setNode1(bbNode)
                                .setNode2(node)
                                .add();
                    }
                }
                importContext.objIdToNode.computeIfAbsent(connectedObj.getId(), k -> new ArrayList<>())
                        .add(new NodeRef(vl.getId(), node));
            }
        }
    }

    private void createSwitch(Network network, ImportContext importContext, DataObject elmCoup) {
        Collection<NodeRef> nodeRefs = checkNodes(elmCoup, importContext.objIdToNode, 2);
        Iterator<NodeRef> it = nodeRefs.iterator();
        NodeRef nodeRef1 = it.next();
        NodeRef nodeRef2 = it.next();
        if (!nodeRef1.voltageLevelId.equals(nodeRef2.voltageLevelId)) {
            throw new PowsyblException("ElmCoup not connected to same ElmSubstat at both sides: " + elmCoup);
        }
        String switchId = nodeRef1.voltageLevelId + "_" + elmCoup.getName();
        boolean open = elmCoup.findIntAttributeValue("on_off").orElse(0) == 0;
        String aUsage = elmCoup.getStringAttributeValue("aUsage");
        SwitchKind switchKind;
        switch (aUsage) {
            case "cbk":
            case "swt":
                switchKind = SwitchKind.BREAKER;
                break;
            case "dct":
                switchKind = SwitchKind.DISCONNECTOR;
                break;
            default:
                throw new PowsyblException("Unknown switch type: " + aUsage);
        }
        VoltageLevel vl1 = network.getVoltageLevel(nodeRef1.voltageLevelId);
        vl1.getNodeBreakerView().newSwitch()
                .setId(switchId)
                .setEnsureIdUnicity(true)
                .setKind(switchKind)
                .setNode1(nodeRef1.node)
                .setNode2(nodeRef2.node)
                .setOpen(open)
                .add();
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        return findProjectLoader(dataSource).map(projectLoader -> {
            LOGGER.info("Starting PowerFactory import...");
            Stopwatch stopwatch = Stopwatch.createStarted();
            try (InputStream is = dataSource.newInputStream(null, projectLoader.getExtension())) {
                Project project = projectLoader.doLoad(dataSource.getBaseName(), is);
                return createNetwork(project, networkFactory);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                stopwatch.stop();
                LOGGER.info("PowerFactory import done in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }).orElseThrow(() -> new PowsyblException("This is not a supported PowerFactory file"));
    }
}
