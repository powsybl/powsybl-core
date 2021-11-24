/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.elements.*;
import com.powsybl.cgmes.conversion.elements.hvdc.CgmesDcConversion;
import com.powsybl.cgmes.conversion.elements.transformers.ThreeWindingsTransformerConversion;
import com.powsybl.cgmes.conversion.elements.transformers.TwoWindingsTransformerConversion;
import com.powsybl.cgmes.conversion.update.CgmesUpdate;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.Conversion.Config.StateProfile.SSH;

/**
 * TwoWindingsTransformer Interpretation
 * <p>
 * Ratio and Phase Interpretation (Xfmr2RatioPhaseInterpretationAlternative) <br>
 * END1. All tapChangers (ratioTapChanger and phaseTapChanger) are considered at end1 (before transmission impedance) <br>
 * END2. All tapChangers (ratioTapChanger and phaseTapChanger) are considered at end2 (after transmission impedance) <br>
 * END1_END2. TapChangers (ratioTapChanger and phaseTapChanger) are considered at the end where they are defined in Cgmes <br>
 * X. If x1 == 0 all tapChangers (ratioTapChanger and phaseTapChanger) are considered at the end1 otherwise they are considered at end2
 * <p>
 * Shunt Admittance Interpretation (Xfmr2ShuntInterpretationAlternative) <br>
 * END1. All shunt admittances to ground (g, b) at end1 (before transmission impedance) <br>
 * END2. All shunt admittances to ground (g, b) at end2 (after transmission impedance) <br>
 * END1_END2. Shunt admittances to ground (g, b) at the end where they are defined in Cgmes model <br>
 * SPLIT. Split shunt admittances to ground (g, b) between end1 and end2. <br>
 * <p>
 * Structural Ratio (Xfmr2StructuralRatioInterpretationAlternative) <br>
 * END1. Structural ratio always at end1 (before transmission impedance) <br>
 * END2. Structural ratio always at end2 (after transmission impedance) <br>
 * X. If x1 == 0 structural ratio at end1, otherwise at end2
 * <p>
 * ThreeWindingsTransformer Interpretation.
 * <p>
 * Ratio and Phase Interpretation.  (Xfmr3RatioPhaseInterpretationAlternative) <br>
 * NETWORK_SIDE. All tapChangers (ratioTapChanger and phaseTapChanger) at the network side. <br>
 * STAR_BUS_SIDE. All tapChangers (ratioTapChanger and phaseTapChanger) at the star bus side.
 * <p>
 * Shunt Admittance Interpretation (Xfmr3ShuntInterpretationAlternative) <br>
 * NETWORK_SIDE. Shunt admittances to ground at the network side (end1 of the leg) <br>
 * STAR_BUS_SIDE. Shunt admittances to ground at the start bus side (end2 of the leg) <br>
 * SPLIT. Split shunt admittances to ground between two ends of the leg
 * <p>
 * Structural Ratio Interpretation (Xfmr3StructuralRatioInterpretationAlternative) <br>
 * STAR_BUS_SIDE. Structural ratio at the star bus side of all legs and RatedU0 = RatedU1 <br>
 * NETWORK_SIDE. Structural ratio at the network side of all legs. RatedU0 = 1 kv <br>
 * END1. Structural ratio at the network side of legs 2 and 3. RatedU0 = RatedU1 <br>
 * END2. Structural ratio at the network side of legs 1 and 3. RatedU0 = RatedU2 <br>
 * END3. Structural ratio at the network side of legs 1 and 2. RatedU0 = RatedU2 <br>
 * <p>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 *
 */
public class Conversion {

    public enum Xfmr2RatioPhaseInterpretationAlternative {
        END1, END2, END1_END2, X
    }

    public enum Xfmr2ShuntInterpretationAlternative {
        END1, END2, END1_END2, SPLIT
    }

    public enum Xfmr2StructuralRatioInterpretationAlternative {
        END1, END2, X
    }

    public enum Xfmr3RatioPhaseInterpretationAlternative {
        NETWORK_SIDE, STAR_BUS_SIDE
    }

    public enum Xfmr3ShuntInterpretationAlternative {
        NETWORK_SIDE, STAR_BUS_SIDE, SPLIT
    }

    public enum Xfmr3StructuralRatioInterpretationAlternative {
        NETWORK_SIDE, STAR_BUS_SIDE, END1, END2, END3
    }

    public Conversion(CgmesModel cgmes) {
        this(cgmes, new Config());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config) {
        this(cgmes, config, Collections.emptyList());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors) {
        this(cgmes, config, postProcessors, NetworkFactory.findDefault());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors,
                      NetworkFactory networkFactory) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.config = Objects.requireNonNull(config);
        this.postProcessors = Objects.requireNonNull(postProcessors);
        this.networkFactory = Objects.requireNonNull(networkFactory);
    }

    public void report(Consumer<String> out) {
        new ReportTapChangers(cgmes, out).report();
    }

    public Network convert() {

        if (LOG.isDebugEnabled() && cgmes.baseVoltages() != null) {
            LOG.debug(cgmes.baseVoltages().tabulate());
        }
        // Check that at least we have an EquipmentCore profile
        if (!cgmes.hasEquipmentCore()) {
            throw new CgmesModelException("Data source does not contain EquipmentCore data");
        }
        Network network = createNetwork();
        Context context = createContext(network);
        assignNetworkProperties(context);
        addCgmesSvMetadata(network, context);
        addCgmesSshMetadata(network, context);
        addCimCharacteristics(network);
        if (context.nodeBreaker() && context.config().createCgmesExportMapping) {
            CgmesIidmMappingAdder mappingAdder = network.newExtension(CgmesIidmMappingAdder.class);
            cgmes.topologicalNodes().forEach(tn -> mappingAdder.addTopologicalNode(tn.getId("TopologicalNode")));
            mappingAdder.add();
        }

        Function<PropertyBag, AbstractObjectConversion> convf;

        cgmes.computedTerminals().forEach(t -> context.terminalMapping().buildTopologicalNodeCgmesTerminalsMapping(t));
        cgmes.regulatingControls().forEach(p -> context.regulatingControlMapping().cacheRegulatingControls(p));

        convert(cgmes.substations(), s -> new SubstationConversion(s, context));
        convert(cgmes.voltageLevels(), vl -> new VoltageLevelConversion(vl, context));
        PropertyBags nodes = context.nodeBreaker()
                ? cgmes.connectivityNodes()
                : cgmes.topologicalNodes();
        String nodeTypeName = context.nodeBreaker()
                ? "ConnectivityNode"
                : "TopologicalNode";
        convert(nodes, n -> new NodeConversion(nodeTypeName, n, context));
        if (!context.config().createBusbarSectionForEveryConnectivityNode()) {
            convert(cgmes.busBarSections(), bbs -> new BusbarSectionConversion(bbs, context));
        }

        convert(cgmes.energyConsumers(), ec -> new EnergyConsumerConversion(ec, context));
        convert(cgmes.energySources(), es -> new EnergySourceConversion(es, context));
        convf = eqi -> new EquivalentInjectionConversion(eqi, context);
        convert(cgmes.equivalentInjections(), convf);
        convf = eni -> new ExternalNetworkInjectionConversion(eni, context);
        convert(cgmes.externalNetworkInjections(), convf);
        convert(cgmes.shuntCompensators(), sh -> new ShuntConversion(sh, context));
        convert(cgmes.equivalentShunts(), es -> new EquivalentShuntConversion(es, context));
        convf = svc -> new StaticVarCompensatorConversion(svc, context);
        convert(cgmes.staticVarCompensators(), convf);
        convf = asm -> new AsynchronousMachineConversion(asm, context);
        convert(cgmes.asynchronousMachines(), convf);
        convert(cgmes.synchronousMachines(), sm -> new SynchronousMachineConversion(sm, context));

        // We will delay the conversion of some lines/switches that have an end at boundary
        // They have to be processed after all lines/switches have been reviewed
        // FIXME(Luma) store delayedBoundaryNodes in context
        Set<String> delayedBoundaryNodes = new HashSet<>();
        convertSwitches(context, delayedBoundaryNodes);
        convertACLineSegmentsToLines(context, delayedBoundaryNodes);

        convertEquivalentBranchesToLines(context, delayedBoundaryNodes);
        convert(cgmes.seriesCompensators(), sc -> new SeriesCompensatorConversion(sc, context));

        convertTransformers(context, delayedBoundaryNodes);
        delayedBoundaryNodes.forEach(node -> convertEquipmentAtBoundaryNode(context, node));

        CgmesDcConversion cgmesDcConversion = new CgmesDcConversion(cgmes, context);
        cgmesDcConversion.convert();

        convert(cgmes.operationalLimits(), l -> new OperationalLimitConversion(l, context));
        context.loadingLimitsMapping().addAll();

        if (config.importControlAreas()) {
            network.newExtension(CgmesControlAreasAdder.class).add();
            CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
            cgmes.controlAreas().forEach(ca -> createControlArea(cgmesControlAreas, ca));
            cgmes.tieFlows().forEach(tf -> addTieFlow(context, cgmesControlAreas, tf));
            cgmesControlAreas.cleanIfEmpty();
        }

        if (config.convertSvInjections()) {
            convert(cgmes.svInjections(), si -> new SvInjectionConversion(si, context));
        }

        clearUnattachedHvdcConverterStations(network, context); // in case of faulty CGMES files, remove HVDC Converter Stations without HVDC lines
        voltageAngles(nodes, context);

        // set all regulating controls
        context.regulatingControlMapping().setAllRegulatingControls(network);
        if (context.config().debugTopology()) {
            debugTopology(context);
        }

        if (config.storeCgmesModelAsNetworkExtension()) {
            // Store a reference to the original CGMES model inside the IIDM network
            // CgmesUpdate will add a listener to Network changes
            CgmesUpdate cgmesUpdater = new CgmesUpdate(network);
            network.newExtension(CgmesModelExtensionAdder.class).withModel(cgmes).withUpdate(cgmesUpdater).add();
        }

        // apply post-processors
        for (CgmesImportPostProcessor postProcessor : postProcessors) {
            // FIXME generic cgmes models may not have an underlying triplestore
            // TODO maybe pass the properties to the post processors
            postProcessor.process(network, cgmes.tripleStore());
        }

        // Complete Voltages and angles in starBus as properties
        // Complete Voltages and angles in boundary buses
        completeVoltagesAndAngles(network);

        if (config.storeCgmesConversionContextAsNetworkExtension()) {
            // Store the terminal mapping in an extension for external validation
            network.newExtension(CgmesConversionContextExtensionAdder.class).withContext(context).add();
        }

        return network;
    }

    private static void completeVoltagesAndAngles(Network network) {

        // Voltage and angle in starBus as properties
        network.getThreeWindingsTransformers()
            .forEach(ThreeWindingsTransformerConversion::calculateVoltageAndAngleInStarBus);

        // Voltage and angle in boundary buses
        network.getDanglingLines()
            .forEach(AbstractConductingEquipmentConversion::calculateVoltageAndAngleInBoundaryBus);
    }

    private static void createControlArea(CgmesControlAreas cgmesControlAreas, PropertyBag ca) {
        String controlAreaId = ca.getId("ControlArea");
        cgmesControlAreas.newCgmesControlArea()
                .setId(controlAreaId)
                .setName(ca.getLocal("name"))
                .setEnergyIdentificationCodeEic(ca.getLocal("energyIdentCodeEic"))
                .setNetInterchange(ca.asDouble("netInterchange", Double.NaN))
                .add();
    }

    private static void addTieFlow(Context context, CgmesControlAreas cgmesControlAreas, PropertyBag tf) {
        String controlAreaId = tf.getId("ControlArea");
        CgmesControlArea cgmesControlArea = cgmesControlAreas.getCgmesControlArea(controlAreaId);
        if (cgmesControlArea == null) {
            context.ignored("Tie Flow", String.format("Tie Flow %s refers to a non-existing control area", tf.getId("TieFlow")));
            return;
        }
        String terminalId = tf.getId("terminal");
        if (context.terminalMapping().find(terminalId) != null) {
            cgmesControlArea.add(context.terminalMapping().find(terminalId));
        } else if (context.terminalMapping().findBoundary(terminalId) != null) {
            cgmesControlArea.add(context.terminalMapping().findBoundary(terminalId));
        }
    }

    private void convert(
            PropertyBags elements,
            Function<PropertyBag, AbstractObjectConversion> f) {
        String conversion = null;

        for (PropertyBag element : elements) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(element.tabulateLocals());
            }
            AbstractObjectConversion c = f.apply(element);
            if (conversion == null) {
                conversion = c.getClass().getName();
                conversion = conversion.substring(conversion.lastIndexOf('.') + 1);
                conversion = conversion.replace("Conversion", "");
            }
            if (c.insideBoundary()) {
                c.convertInsideBoundary();
            } else if (c.valid()) {
                c.convert();
            }
        }
    }

    private Network createNetwork() {
        String networkId = cgmes.modelId();
        String sourceFormat = "CGMES";
        return networkFactory.createNetwork(networkId, sourceFormat);
    }

    private Context createContext(Network network) {
        Context context = new Context(cgmes, config, network);
        context.substationIdMapping().build();
        context.dc().initialize();
        context.loadRatioTapChangers();
        context.loadPhaseTapChangers();
        context.loadRatioTapChangerTables();
        context.loadPhaseTapChangerTables();
        context.loadReactiveCapabilityCurveData();
        return context;
    }

    private void assignNetworkProperties(Context context) {
        context.network().setProperty(NETWORK_PS_CGMES_MODEL_DETAIL,
                context.nodeBreaker()
                        ? NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER
                        : NETWORK_PS_CGMES_MODEL_DETAIL_BUS_BRANCH);
        DateTime modelScenarioTime = cgmes.scenarioTime();
        DateTime modelCreated = cgmes.created();
        long forecastDistance = new Duration(modelCreated, modelScenarioTime).getStandardMinutes();
        context.network().setForecastDistance(forecastDistance >= 0 ? (int) forecastDistance : 0);
        context.network().setCaseDate(modelScenarioTime);
        LOG.info("cgmes scenarioTime       : {}", modelScenarioTime);
        LOG.info("cgmes modelCreated       : {}", modelCreated);
        LOG.info("network caseDate         : {}", context.network().getCaseDate());
        LOG.info("network forecastDistance : {}", context.network().getForecastDistance());
    }

    private void addCgmesSvMetadata(Network network, Context context) {
        PropertyBags svDescription = cgmes.fullModel(CgmesSubset.STATE_VARIABLES.getProfile());
        if (svDescription != null && !svDescription.isEmpty()) {
            CgmesSvMetadataAdder adder = network.newExtension(CgmesSvMetadataAdder.class)
                    .setDescription(svDescription.get(0).getId("description"))
                    .setSvVersion(readVersion(svDescription, context))
                    .setModelingAuthoritySet(svDescription.get(0).getId("modelingAuthoritySet"));
            svDescription.pluckLocals("DependentOn").forEach(adder::addDependency);
            adder.add();
        }
    }

    private void addCgmesSshMetadata(Network network, Context context) {
        PropertyBags sshDescription = cgmes.fullModel(CgmesSubset.STEADY_STATE_HYPOTHESIS.getProfile());
        if (sshDescription != null && !sshDescription.isEmpty()) {
            CgmesSshMetadataAdder adder = network.newExtension(CgmesSshMetadataAdder.class)
                    .setDescription(sshDescription.get(0).getId("description"))
                    .setSshVersion(readVersion(sshDescription, context))
                    .setModelingAuthoritySet(sshDescription.get(0).getId("modelingAuthoritySet"));
            sshDescription.pluckLocals("DependentOn").forEach(adder::addDependency);
            adder.add();
        }
    }

    private int readVersion(PropertyBags propertyBags, Context context) {
        try {
            return propertyBags.get(0).asInt("version");
        } catch (NumberFormatException e) {
            context.fixed("Version", "The version is expected to be an integer: " + propertyBags.get(0).get("version") + ". Fixed to 1");
            return 1;
        }
    }

    private void addCimCharacteristics(Network network) {
        if (cgmes instanceof CgmesModelTripleStore) {
            network.newExtension(CimCharacteristicsAdder.class)
                    .setTopologyKind(cgmes.isNodeBreaker() ? CgmesTopologyKind.NODE_BREAKER : CgmesTopologyKind.BUS_BRANCH)
                    .setCimVersion(((CgmesModelTripleStore) cgmes).getCimVersion())
                    .add();
        }
    }

    private void putVoltageLevelRefByLineContainerIdIfPresent(String lineContainerId, Supplier<String> terminalId1,
                                                              Supplier<String> terminalId2,
                                                              Map<String, VoltageLevel> nominalVoltageByLineContainerId,
                                                              Context context) {
        String vlId = Optional.ofNullable(context.namingStrategy().getId("VoltageLevel",
                        context.cgmes().voltageLevel(cgmes.terminal(terminalId1.get()), context.nodeBreaker())))
                .orElseGet(() -> context.namingStrategy().getId("VoltageLevel",
                        context.cgmes().voltageLevel(cgmes.terminal(terminalId2.get()), context.nodeBreaker())));
        if (vlId != null) {
            VoltageLevel vl = context.network().getVoltageLevel(vlId);
            if (vl != null) {
                nominalVoltageByLineContainerId.put(lineContainerId, vl);
            }
        }
    }

    private void convertACLineSegmentsToLines(Context context, Set<String> delayedBoundaryNodes) {
        Map<String, VoltageLevel> voltageLevelRefByLineContainerId = new HashMap<>();
        PropertyBags acLineSegments = cgmes.acLineSegments();
        for (PropertyBag line : acLineSegments) { // Retrieve a voltage level reference for every line container of AC Line Segments outside boundaries
            String lineContainerId = line.getId("Line");
            if (lineContainerId != null && !voltageLevelRefByLineContainerId.containsKey(lineContainerId)) {
                putVoltageLevelRefByLineContainerIdIfPresent(lineContainerId, () -> line.getId("Terminal1"), () -> line.getId("Terminal2"),
                        voltageLevelRefByLineContainerId, context);
            }
        }
        for (PropertyBag line : acLineSegments) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(line.tabulateLocals("ACLineSegment"));
            }
            String lineContainerId = line.getId("Line");
            if (lineContainerId != null) { // Create fictitious voltage levels for AC line segments inside line containers outside boundaries
                VoltageLevel vlRef = voltageLevelRefByLineContainerId.get(lineContainerId);
                createLineContainerFictitiousVoltageLevels(context, lineContainerId, vlRef, line);
            }
            ACLineSegmentConversion c = new ACLineSegmentConversion(line, context);
            if (c.valid()) {
                String node = c.boundaryNode();
                if (node != null && !context.config().convertBoundary()) {
                    context.boundary().addAcLineSegmentAtNode(line, node);
                    delayedBoundaryNodes.add(node);
                } else {
                    c.convert();
                }
            }
        }
    }

    static class LineContainerFictitiousVoltageLevelData {
        String lineId;

        String lineName;
        String nodeId;
        VoltageLevel vl;

        String idForFictitiousVoltageLevel() {
            return nodeId + "_VL";
        }
    }

    private LineContainerFictitiousVoltageLevelData voltageLevelDataForACLSinLineContainer(Context context, String lineId, PropertyBag lineSegment, String terminalRef) {
        LineContainerFictitiousVoltageLevelData vldata = new LineContainerFictitiousVoltageLevelData();
        vldata.lineId = lineId;
        vldata.lineName = lineSegment.get("lineName");
        CgmesTerminal t = cgmes.terminal(lineSegment.getId(terminalRef));
        vldata.nodeId = context.nodeBreaker() ? t.connectivityNode() : t.topologicalNode();
        String vlId = context.namingStrategy().getId("VoltageLevel", context.cgmes().voltageLevel(t, context.nodeBreaker()));
        if (vlId != null) {
            vldata.vl = context.network().getVoltageLevel(vlId);
        } else {
            vldata.vl = context.network().getVoltageLevel(vldata.idForFictitiousVoltageLevel());
        }
        return vldata;
    }

    private void createLineContainerFictitiousVoltageLevels(Context context, String lineId, VoltageLevel vlRef, PropertyBag lineSegment) {
        // Try to obtain data for a potential fictitious voltage level from Terminal1 of AC Line Segment
        LineContainerFictitiousVoltageLevelData vldata1 = voltageLevelDataForACLSinLineContainer(context, lineId, lineSegment, "Terminal1");
        // The same, from Terminal2 of AC Line Segment
        LineContainerFictitiousVoltageLevelData vldata2 = voltageLevelDataForACLSinLineContainer(context, lineId, lineSegment, "Terminal2");
        // Only create a fictitious voltage levels replacing cim:Line Container if we are NOT at boundaries
        if (vldata1.vl == null && !context.boundary().containsNode(vldata1.nodeId)) {
            createLineContainerFictitiousVoltageLevel(context, vldata1, vlRef);
        }
        if (vldata2.vl == null && !context.boundary().containsNode(vldata2.nodeId)) {
            createLineContainerFictitiousVoltageLevel(context, vldata2, vlRef);
        }
    }

    private void createLineContainerFictitiousVoltageLevel(Context context, LineContainerFictitiousVoltageLevelData vldata, VoltageLevel vlref) {
        String id = vldata.idForFictitiousVoltageLevel();
        LOG.warn("Fictitious Voltage Level {} created for Line container {} node {}", id, vldata.lineId, vldata.lineName);
        // Nominal voltage and low/high limits are copied from the reference voltage level, if it is given
        VoltageLevel vl = context.network().newVoltageLevel()
                .setNominalV(vlref.getNominalV())
                .setTopologyKind(
                        context.nodeBreaker()
                                ? TopologyKind.NODE_BREAKER
                                : TopologyKind.BUS_BREAKER)
                .setLowVoltageLimit(vlref.getLowVoltageLimit())
                .setHighVoltageLimit(vlref.getHighVoltageLimit())
                .setId(id)
                .setName(vldata.lineName)
                .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                .add();
        vl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "LineContainerId", vldata.lineId);
    }

    private void convertSwitches(Context context, Set<String> delayedBoundaryNodes) {
        Iterator<PropertyBag> k = cgmes.switches().iterator();
        while (k.hasNext()) {
            PropertyBag sw = k.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(sw.tabulateLocals("Switch"));
            }
            SwitchConversion c = new SwitchConversion(sw, context);
            if (c.valid()) {
                String node = c.boundaryNode();
                if (node != null && !context.config().convertBoundary()) {
                    context.boundary().addSwitchAtNode(sw, node);
                    delayedBoundaryNodes.add(node);
                } else {
                    c.convert();
                }
            }
        }
    }

    private void convertEquivalentBranchesToLines(Context context, Set<String> delayedBoundaryNodes) {
        Iterator<PropertyBag> k = cgmes.equivalentBranches().iterator();
        while (k.hasNext()) {
            PropertyBag equivalentBranch = k.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug(equivalentBranch.tabulateLocals("EquivalentBranch"));
            }
            EquivalentBranchConversion c = new EquivalentBranchConversion(equivalentBranch, context);
            if (c.valid()) {
                String node = c.boundaryNode();
                if (node != null && !context.config().convertBoundary()) {
                    context.boundary().addEquivalentBranchAtNode(equivalentBranch, node);
                    delayedBoundaryNodes.add(node);
                } else {
                    c.convert();
                }
            }
        }
    }

    private void convertTransformers(Context context, Set<String> delayedBoundaryNodes) {
        cgmes.groupedTransformerEnds().forEach((t, ends) -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Transformer {}, {}-winding", t, ends.size());
                ends.forEach(e -> LOG.debug(e.tabulateLocals("TransformerEnd")));
            }
            if (ends.size() == 2) {
                convertTwoWindingsTransformers(context, ends, delayedBoundaryNodes);
            } else if (ends.size() == 3) {
                convertThreeWindingsTransformers(context, ends);
            } else {
                String what = "PowerTransformer " + t;
                Supplier<String> reason = () -> String.format("Has %d ends. Only 2 or 3 ends are supported", ends.size());
                context.invalid(what, reason);
            }
        });
    }

    private static void convertTwoWindingsTransformers(Context context, PropertyBags ends, Set<String> delayedBoundaryNodes) {
        AbstractConductingEquipmentConversion c = new TwoWindingsTransformerConversion(ends, context);
        if (c.valid()) {
            String node = c.boundaryNode();
            if (node != null && !context.config().convertBoundary()) {
                context.boundary().addTransformerAtNode(ends, node);
                delayedBoundaryNodes.add(node);
            } else {
                c.convert();
            }
        }
    }

    private static void convertThreeWindingsTransformers(Context context, PropertyBags ends) {
        AbstractConductingEquipmentConversion c = new ThreeWindingsTransformerConversion(ends, context);
        if (c.valid()) {
            c.convert();
        }
    }

    // Supported conversions:
    // Only one Line (--> create dangling line)
    // Only one Switch (--> create dangling line with z0)
    // Only one Transformer (--> create dangling line)
    // Only one EquivalentBranch (--> create dangling line)
    // Any combination of Line, Switch, Transformer and EquivalentBranch

    private void convertEquipmentAtBoundaryNode(Context context, String node) {
        // At least each delayed boundary node should have one equipment attached to it
        // Currently supported equipment at boundary are lines and switches
        List<BoundaryEquipment> beqs = context.boundary().boundaryEquipmentAtNode(node);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Delayed boundary node {} with {} equipment at it", node, beqs.size());
            beqs.forEach(BoundaryEquipment::log);
        }
        int numEquipmentsAtNode = beqs.size();
        if (numEquipmentsAtNode == 1) {
            beqs.get(0).createConversion(context).convertAtBoundary();
        } else if (numEquipmentsAtNode == 2) {
            convertTwoEquipmentsAtBoundaryNode(context, node, beqs.get(0), beqs.get(1));
        } else if (numEquipmentsAtNode > 2) {
            context.invalid(node, "Too many equipment at boundary node");
        }
    }

    private static void convertTwoEquipmentsAtBoundaryNode(Context context, String node, BoundaryEquipment beq1, BoundaryEquipment beq2) {
        BoundaryLine boundaryLine1 = beq1.createConversion(context).asBoundaryLine(node);
        BoundaryLine boundaryLine2 = beq2.createConversion(context).asBoundaryLine(node);
        if (boundaryLine1 != null && boundaryLine2 != null) {
            if (boundaryLine2.getId().compareTo(boundaryLine1.getId()) >= 0) {
                ACLineSegmentConversion.convertBoundaryLines(context, node, boundaryLine1, boundaryLine2);
            } else {
                ACLineSegmentConversion.convertBoundaryLines(context, node, boundaryLine2, boundaryLine1);
            }
        } else {
            context.invalid(node, "Unexpected boundaryLine");
        }
    }

    private void voltageAngles(PropertyBags nodes, Context context) {
        if (context.nodeBreaker()) {
            // TODO(Luma): we create again one conversion object for every node
            // In node-breaker conversion,
            // set (voltage, angle) values after all nodes have been created and connected
            for (PropertyBag n : nodes) {
                NodeConversion nc = new NodeConversion("ConnectivityNode", n, context);
                if (!nc.insideBoundary() || nc.insideBoundary() && context.config().convertBoundary()) {
                    nc.setVoltageAngleNodeBreaker();
                }
            }
        }
    }

    private void clearUnattachedHvdcConverterStations(Network network, Context context) {
        network.getHvdcConverterStationStream()
                .filter(converter -> converter.getHvdcLine() == null)
                .peek(converter -> context.ignored("HVDC Converter Station " + converter.getId(), "No correct linked HVDC line found."))
                .collect(Collectors.toList())
                .forEach(Connectable::remove);
    }

    private void debugTopology(Context context) {
        context.network().getVoltageLevels().forEach(vl -> {
            String name = vl.getSubstation().map(s -> s.getNameOrId() + "-").orElse("") + vl.getNameOrId();
            name = name.replace('/', '-');
            Path file = Paths.get(System.getProperty("java.io.tmpdir"), "temp-cgmes-" + name + ".dot");
            try {
                vl.exportTopology(file);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public static class Config {

        public enum StateProfile {
            SSH,
            SV
        }

        public List<String> substationIdsExcludedFromMapping() {
            return Collections.emptyList();
        }

        public boolean debugTopology() {
            return false;
        }

        public boolean allowUnsupportedTapChangers() {
            return allowUnsupportedTapChangers;
        }

        public Config setAllowUnsupportedTapChangers(boolean allowUnsupportedTapChangers) {
            this.allowUnsupportedTapChangers = allowUnsupportedTapChangers;
            return this;
        }

        public boolean useNodeBreaker() {
            return true;
        }

        public double lowImpedanceLineR() {
            return lowImpedanceLineR;
        }

        public double lowImpedanceLineX() {
            return lowImpedanceLineX;
        }

        public boolean convertBoundary() {
            return convertBoundary;
        }

        public Config setConvertBoundary(boolean convertBoundary) {
            this.convertBoundary = convertBoundary;
            return this;
        }

        public boolean changeSignForShuntReactivePowerFlowInitialState() {
            return changeSignForShuntReactivePowerFlowInitialState;
        }

        public Config setChangeSignForShuntReactivePowerFlowInitialState(boolean b) {
            changeSignForShuntReactivePowerFlowInitialState = b;
            return this;
        }

        public boolean computeFlowsAtBoundaryDanglingLines() {
            return true;
        }

        public boolean createBusbarSectionForEveryConnectivityNode() {
            return createBusbarSectionForEveryConnectivityNode;
        }

        public Config setCreateBusbarSectionForEveryConnectivityNode(boolean b) {
            createBusbarSectionForEveryConnectivityNode = b;
            return this;
        }

        public boolean createCgmesExportMapping() {
            return createCgmesExportMapping;
        }

        public Config setCreateCgmesExportMapping(boolean createCgmesExportMapping) {
            this.createCgmesExportMapping = createCgmesExportMapping;
            return this;
        }

        public boolean convertSvInjections() {
            return convertSvInjections;
        }

        public Config setConvertSvInjections(boolean convertSvInjections) {
            this.convertSvInjections = convertSvInjections;
            return this;
        }

        public StateProfile getProfileUsedForInitialStateValues() {
            return profileUsedForInitialStateValues;
        }

        public Config setProfileUsedForInitialStateValues(String profileUsedForInitialFlowsValues) {
            switch (Objects.requireNonNull(profileUsedForInitialFlowsValues)) {
                case "SSH":
                case "SV":
                    this.profileUsedForInitialStateValues = StateProfile.valueOf(profileUsedForInitialFlowsValues);
                    break;
                default:
                    throw new CgmesModelException("Unexpected profile used for state hypothesis: " + profileUsedForInitialFlowsValues);
            }
            return this;
        }

        public boolean storeCgmesModelAsNetworkExtension() {
            return storeCgmesModelAsNetworkExtension;
        }

        public Config setStoreCgmesModelAsNetworkExtension(boolean storeCgmesModelAsNetworkExtension) {
            this.storeCgmesModelAsNetworkExtension = storeCgmesModelAsNetworkExtension;
            return this;
        }

        public boolean storeCgmesConversionContextAsNetworkExtension() {
            return storeCgmesConversionContextAsNetworkExtension;
        }

        public Config setStoreCgmesConversionContextAsNetworkExtension(boolean storeCgmesTerminalMappingAsNetworkExtension) {
            this.storeCgmesConversionContextAsNetworkExtension = storeCgmesTerminalMappingAsNetworkExtension;
            return this;
        }

        public boolean isEnsureIdAliasUnicity() {
            return ensureIdAliasUnicity;
        }

        public Config setEnsureIdAliasUnicity(boolean ensureIdAliasUnicity) {
            this.ensureIdAliasUnicity = ensureIdAliasUnicity;
            return this;
        }

        public boolean importControlAreas() {
            return importControlAreas;
        }

        public Config setImportControlAreas(boolean importControlAreas) {
            this.importControlAreas = importControlAreas;
            return this;
        }

        public Xfmr2RatioPhaseInterpretationAlternative getXfmr2RatioPhase() {
            return xfmr2RatioPhase;
        }

        public void setXfmr2RatioPhase(Xfmr2RatioPhaseInterpretationAlternative alternative) {
            xfmr2RatioPhase = alternative;
        }

        public Xfmr2ShuntInterpretationAlternative getXfmr2Shunt() {
            return xfmr2Shunt;
        }

        public void setXfmr2Shunt(Xfmr2ShuntInterpretationAlternative alternative) {
            xfmr2Shunt = alternative;
        }

        public Xfmr2StructuralRatioInterpretationAlternative getXfmr2StructuralRatio() {
            return xfmr2StructuralRatio;
        }

        public void setXfmr2StructuralRatio(Xfmr2StructuralRatioInterpretationAlternative alternative) {
            xfmr2StructuralRatio = alternative;
        }

        public Xfmr3RatioPhaseInterpretationAlternative getXfmr3RatioPhase() {
            return xfmr3RatioPhase;
        }

        public void setXfmr3RatioPhase(Xfmr3RatioPhaseInterpretationAlternative alternative) {
            this.xfmr3RatioPhase = alternative;
        }

        public Xfmr3ShuntInterpretationAlternative getXfmr3Shunt() {
            return xfmr3Shunt;
        }

        public void setXfmr3Shunt(Xfmr3ShuntInterpretationAlternative alternative) {
            xfmr3Shunt = alternative;
        }

        public Xfmr3StructuralRatioInterpretationAlternative getXfmr3StructuralRatio() {
            return xfmr3StructuralRatio;
        }

        public void setXfmr3StructuralRatio(Xfmr3StructuralRatioInterpretationAlternative alternative) {
            xfmr3StructuralRatio = alternative;
        }

        private boolean allowUnsupportedTapChangers = true;
        private boolean convertBoundary = false;
        private boolean changeSignForShuntReactivePowerFlowInitialState = false;
        private double lowImpedanceLineR = 7.0E-5;
        private double lowImpedanceLineX = 7.0E-5;

        private boolean createBusbarSectionForEveryConnectivityNode = false;
        private boolean convertSvInjections = true;
        private StateProfile profileUsedForInitialStateValues = SSH;
        private boolean storeCgmesModelAsNetworkExtension = true;
        private boolean storeCgmesConversionContextAsNetworkExtension = false;

        private boolean ensureIdAliasUnicity = false;
        private boolean importControlAreas = true;

        private boolean createCgmesExportMapping = false;

        // Default interpretation.
        private Xfmr2RatioPhaseInterpretationAlternative xfmr2RatioPhase = Xfmr2RatioPhaseInterpretationAlternative.END1_END2;
        private Xfmr2ShuntInterpretationAlternative xfmr2Shunt = Xfmr2ShuntInterpretationAlternative.END1_END2;
        private Xfmr2StructuralRatioInterpretationAlternative xfmr2StructuralRatio = Xfmr2StructuralRatioInterpretationAlternative.X;

        private Xfmr3RatioPhaseInterpretationAlternative xfmr3RatioPhase = Xfmr3RatioPhaseInterpretationAlternative.NETWORK_SIDE;
        private Xfmr3ShuntInterpretationAlternative xfmr3Shunt = Xfmr3ShuntInterpretationAlternative.NETWORK_SIDE;
        private Xfmr3StructuralRatioInterpretationAlternative xfmr3StructuralRatio = Xfmr3StructuralRatioInterpretationAlternative.STAR_BUS_SIDE;

    }

    private final CgmesModel cgmes;
    private final Config config;
    private final List<CgmesImportPostProcessor> postProcessors;
    private final NetworkFactory networkFactory;

    private static final Logger LOG = LoggerFactory.getLogger(Conversion.class);

    public static final String NETWORK_PS_CGMES_MODEL_DETAIL = "CGMESModelDetail";
    public static final String NETWORK_PS_CGMES_MODEL_DETAIL_BUS_BRANCH = "bus-branch";
    public static final String NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER = "node-breaker";

    public static final String CGMES_PREFIX_ALIAS_PROPERTIES = "CGMES.";
}
