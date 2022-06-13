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

    private Optional<PowerFactoryDataLoader<StudyCase>> findProjectLoader(ReadOnlyDataSource dataSource) {
        for (PowerFactoryDataLoader<StudyCase> studyCaseLoader : PowerFactoryDataLoader.find(StudyCase.class)) {
            try {
                if (dataSource.exists(null, studyCaseLoader.getExtension())) {
                    return Optional.of(studyCaseLoader);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        return findProjectLoader(dataSource).filter(studyCaseLoader -> {
            try (InputStream is = dataSource.newInputStream(null, studyCaseLoader.getExtension())) {
                return studyCaseLoader.test(is);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).isPresent();
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        findProjectLoader(fromDataSource).ifPresent(studyCaseLoader -> {
            try (InputStream is = fromDataSource.newInputStream(null, studyCaseLoader.getExtension());
                 OutputStream os = toDataSource.newOutputStream(null, studyCaseLoader.getExtension(), false)) {
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

        // elmTerm object id to busbarSection id mapping
        final Map<Long, NodeRef> elmTermIdToNode = new HashMap<>();

        List<DataObject> cubiclesObjectNotFound = new ArrayList<>();

        ImportContext(ContainersMapping containerMapping) {
            this.containerMapping = containerMapping;
        }
    }

    // TODO move to AbstractConverter at the end
    static class NodeRef {

        final String voltageLevelId;
        final int node;
        final int busIndexIn;

        NodeRef(String voltageLevelId, int node, int busIndexIn) {
            this.voltageLevelId = voltageLevelId;
            this.node = node;
            this.busIndexIn = busIndexIn;
        }

        @Override
        public String toString() {
            return "NodeRef(voltageLevelId='" + voltageLevelId + '\'' +
                    ", node=" + node +
                    ')';
        }
    }

    // TODO delete at the end
    private static List<NodeRef> checkNodes(DataObject obj, Map<Long, List<NodeRef>> objIdToNode, int connections) {
        List<NodeRef> nodeRefs = objIdToNode.get(obj.getId());
        if (nodeRefs == null || nodeRefs.size() != connections) {
            throw new PowsyblException("Inconsistent number (" + (nodeRefs != null ? nodeRefs.size() : 0)
                    + ") of connection for '" + obj + "'");
        }
        return nodeRefs.stream().sorted(Comparator.comparing(nodoref -> nodoref.busIndexIn)).collect(Collectors.toList());
    }

    private static PowerFactoryException createNotYetSupportedException() {
        return new PowerFactoryException("Not yet supported");
    }

    private Network createNetwork(StudyCase studyCase, NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork(studyCase.getName(), FORMAT);

        List<DataObject> elmNets = studyCase.getElmNets();
        if (elmNets.isEmpty()) {
            throw new PowsyblException("No ElmNet object found");
        }
        LOGGER.info("Study case has {} network(s): {}", elmNets.size(), elmNets.stream().map(DataObject::getLocName).collect(Collectors.toList()));

        // case date
        DateTime caseDate = new Instant(studyCase.getTime().toEpochMilli()).toDateTime();
        network.setCaseDate(caseDate);

        List<DataObject> elmTerms = studyCase.getElmNets().stream()
                .flatMap(elmNet -> elmNet.search(".*.ElmTerm").stream())
                .collect(Collectors.toList());

        LOGGER.info("Creating containers...");

        ContainersMapping containerMapping = ContainersMappingHelper.create(studyCase.getIndex(), elmTerms);
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

        var objs = studyCase.getElmNets().stream()
                .flatMap(elmNet -> elmNet.search(".*").stream())
                .collect(Collectors.toList());
        for (DataObject obj : objs) {
            switch (obj.getDataClassName()) {
                case "ElmCoup":
                    createSwitch(network, importContext, obj);
                    break;

                case "ElmSym":
                case "ElmAsm":
                case "ElmGenstat":
                    new GeneratorConverter(importContext, network).create(obj);
                    break;

                case "ElmLod":
                    new LoadConverter(importContext, network).create(obj);
                    break;

                case "ElmShnt":
                    createShunt(network, importContext, obj);
                    break;

                case "ElmLne":
                    new LineConverter(importContext, network).create(obj);
                    break;

                case "ElmTr2":
                    new TransformerConverter(importContext, network).createTwoWindings(obj);
                    break;

                case "ElmTr3":
                    new TransformerConverter(importContext, network).createThreeWindings(obj);
                    break;
                case "ElmZpu":
                    throw createNotYetSupportedException();

                case "ElmNet":
                case "StaCubic":
                case "ElmTerm":
                case "ElmSubstat":
                case "ElmTrfstat":
                case "StaSwitch":
                    // already processed
                    break;

                case "TypLne":
                case "TypSym":
                case "TypLod":
                case "TypTr2":
                case "TypTr3":
                    // Referenced by other objects
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
                case "ElmRelay":
                    // not interesting
                    break;

                default:
                    LOGGER.warn("Unexpected data class '{}' ('{}')", obj.getDataClassName(), obj);
            }
        }

        LOGGER.info("{} substations, {} voltage levels, {} lines, {} 2w-transformers, {} 3w-transformers, {} generators, {} loads, {} shunts have been created",
                network.getSubstationCount(), network.getVoltageLevelCount(), network.getLineCount(), network.getTwoWindingsTransformerCount(),
                network.getThreeWindingsTransformerCount(), network.getGeneratorCount(), network.getLoadCount(), network.getShuntCompensatorCount());

        setVoltagesAndAngles(network, importContext, elmTerms);

        return network;
    }

    private static void setVoltagesAndAngles(Network network, ImportContext importContext, List<DataObject> elmTerms) {
        for (DataObject elmTerm : elmTerms) {
            setVoltageAndAngle(network, importContext, elmTerm);
        }
    }

    private static void setVoltageAndAngle(Network network, ImportContext importContext, DataObject elmTerm) {
        if (!importContext.elmTermIdToNode.containsKey(elmTerm.getId())) {
            return;
        }
        Optional<Float> uknom = elmTerm.findFloatAttributeValue("uknom");
        Optional<Float> u = elmTerm.findFloatAttributeValue("m:u");
        Optional<Float> phiu = elmTerm.findFloatAttributeValue("m:phiu");

        if (uknom.isPresent() && u.isPresent() && phiu.isPresent()) {
            NodeRef nodeRef = importContext.elmTermIdToNode.get(elmTerm.getId());
            Terminal terminal = network.getVoltageLevel(nodeRef.voltageLevelId).getNodeBreakerView().getTerminal(nodeRef.node);
            Bus bus = terminal.getBusView().getBus();
            if (bus != null) {
                bus.setV(u.get() * uknom.get());
                bus.setAngle(phiu.get());
            }
        }
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
                .setId(elmShnt.getLocName())
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

    private void createSwitch(ImportContext importContext, VoltageLevel vl, MutableInt nodeCount, int bbNode,
                              DataObject staCubic, DataObject connectedObj) {
        List<DataObject> staSwitches = staCubic.getChildrenByClass("StaSwitch");
        if (staSwitches.size() > 1) {
            throw new PowsyblException("Multiple staSwitch not supported");
        }
        DataObject staSwitch = staSwitches.isEmpty() ? null : staSwitches.get(0);
        int busIndexIn = staCubic.getIntAttributeValue("obj_bus");

        int node;
        if (staSwitch != null) {
            node = nodeCount.intValue();
            nodeCount.increment();
            String switchId = vl.getId() + "_" + staSwitch.getLocName();
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
                .add(new NodeRef(vl.getId(), node, busIndexIn));
    }

    private void createNode(Network network, ImportContext importContext, DataObject elmTerm) {
        VoltageLevel vl = createVoltageLevel(network, importContext, elmTerm);
        int iUsage = elmTerm.getIntAttributeValue("iUsage");
        MutableInt nodeCount = importContext.nodeCountByVoltageLevelId.computeIfAbsent(vl.getId(), k -> new MutableInt());
        int bbNode = nodeCount.intValue();
        nodeCount.increment();

        importContext.elmTermIdToNode.putIfAbsent(elmTerm.getId(), new NodeRef(vl.getId(), bbNode, 0));

        if (iUsage == 0) { // busbar
            vl.getNodeBreakerView().newBusbarSection()
                    .setId(vl.getId() + "_" + elmTerm.getLocName())
                    .setEnsureIdUnicity(true)
                    .setNode(bbNode)
                    .add();
        }
        for (DataObject staCubic : elmTerm.getChildrenByClass("StaCubic")) {
            DataObject connectedObj = staCubic.findObjectAttributeValue("obj_id")
                    .flatMap(DataObjectRef::resolve)
                    .orElse(null);
            if (connectedObj == null) {
                importContext.cubiclesObjectNotFound.add(staCubic);
            } else {
                createSwitch(importContext, vl, nodeCount, bbNode, staCubic, connectedObj);
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
        String switchId = nodeRef1.voltageLevelId + "_" + elmCoup.getLocName();
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
        return findProjectLoader(dataSource).map(studyCaseLoader -> {
            LOGGER.info("Starting PowerFactory import...");
            Stopwatch stopwatch = Stopwatch.createStarted();
            try (InputStream is = dataSource.newInputStream(null, studyCaseLoader.getExtension())) {
                StudyCase studyCase = studyCaseLoader.doLoad(dataSource.getBaseName(), is);
                return createNetwork(studyCase, networkFactory);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                stopwatch.stop();
                LOGGER.info("PowerFactory import done in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }).orElseThrow(() -> new PowsyblException("This is not a supported PowerFactory file"));
    }
}
