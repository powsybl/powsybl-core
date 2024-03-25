/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.elements.*;
import com.powsybl.cgmes.conversion.elements.hvdc.CgmesDcConversion;
import com.powsybl.cgmes.conversion.elements.transformers.ThreeWindingsTransformerConversion;
import com.powsybl.cgmes.conversion.elements.transformers.TwoWindingsTransformerConversion;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.*;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.Identifiables;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.powsybl.cgmes.conversion.CgmesReports.importedCgmesNetworkReport;
import static com.powsybl.cgmes.conversion.Conversion.Config.StateProfile.SSH;
import static java.util.stream.Collectors.groupingBy;

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
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
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
        this(cgmes, config, Collections.emptyList(), Collections.emptyList());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config, List<CgmesImportPostProcessor> postProcessors) {
        this(cgmes, config, Collections.emptyList(), postProcessors, NetworkFactory.findDefault());
    }

    public Conversion(CgmesModel cgmes, Conversion.Config config, List<CgmesImportPreProcessor> preProcessors, List<CgmesImportPostProcessor> postProcessors) {
        this(cgmes, config, preProcessors, postProcessors, NetworkFactory.findDefault());
    }

    public Conversion(CgmesModel cgmes, Config config, List<CgmesImportPreProcessor> activatedPreProcessors, List<CgmesImportPostProcessor> activatedPostProcessors, NetworkFactory networkFactory) {
        this.cgmes = Objects.requireNonNull(cgmes);
        this.config = Objects.requireNonNull(config);
        this.preProcessors = Objects.requireNonNull(activatedPreProcessors);
        this.postProcessors = Objects.requireNonNull(activatedPostProcessors);
        this.networkFactory = Objects.requireNonNull(networkFactory);
    }

    public void report(Consumer<String> out) {
        new ReportTapChangers(cgmes, out).report();
    }

    public Network convert() {
        return convert(ReportNode.NO_OP);
    }

    public Network convert(ReportNode reportNode) {
        Objects.requireNonNull(reportNode);

        // apply pre-processors before starting the conversion
        for (CgmesImportPreProcessor preProcessor : preProcessors) {
            preProcessor.process(cgmes);
        }

        if (LOG.isTraceEnabled() && cgmes.baseVoltages() != null) {
            LOG.trace("{}{}{}", "BaseVoltages", System.lineSeparator(), cgmes.baseVoltages().tabulate());
        }
        // Check that at least we have an EquipmentCore profile
        if (!cgmes.hasEquipmentCore()) {
            throw new CgmesModelException("Data source does not contain EquipmentCore data");
        }
        Network network = createNetwork();
        Context context = createContext(network, reportNode);
        assignNetworkProperties(context);
        addCgmesSvMetadata(network, context);
        addCgmesSshMetadata(network, context);
        addCimCharacteristics(network);
        BaseVoltageMappingAdder bvAdder = network.newExtension(BaseVoltageMappingAdder.class);
        cgmes.baseVoltages().forEach(bv -> bvAdder.addBaseVoltage(bv.getId("BaseVoltage"), bv.asDouble("nominalVoltage"), isBoundaryBaseVoltage(bv.getLocal("graph"))));
        bvAdder.add();

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

        convert(cgmes.grounds(), g -> new GroundConversion(g, context));
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
        convert(cgmes.synchronousMachinesGenerators(), sm -> new SynchronousMachineConversion(sm, context));
        convert(cgmes.synchronousMachinesCondensers(), sm -> new SynchronousMachineConversion(sm, context));

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

        if (config.convertSvInjections()) {
            convert(cgmes.svInjections(), si -> new SvInjectionConversion(si, context));
        }

        clearUnattachedHvdcConverterStations(network, context); // in case of faulty CGMES files, remove HVDC Converter Stations without HVDC lines
        voltageAngles(nodes, context);

        if (config.importControlAreas()) {
            network.newExtension(CgmesControlAreasAdder.class).add();
            CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
            cgmes.controlAreas().forEach(ca -> createControlArea(cgmesControlAreas, ca));
            cgmes.tieFlows().forEach(tf -> addTieFlow(context, cgmesControlAreas, tf));
            cgmesControlAreas.cleanIfEmpty();
        }

        // set all regulating controls
        context.regulatingControlMapping().setAllRegulatingControls(network);
        if (context.config().debugTopology()) {
            debugTopology(context);
        }

        if (config.storeCgmesModelAsNetworkExtension()) {
            // Store a reference to the original CGMES model inside the IIDM network
            network.newExtension(CgmesModelExtensionAdder.class).withModel(cgmes).add();
        }

        // apply post-processors
        handleDangingLineDisconnectedAtBoundary(network, context);
        adjustMultipleUnpairedDanglingLinesAtSameBoundaryNode(network, context);
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

        importedCgmesNetworkReport(context.getReportNode(), network.getId());
        return network;
    }

    private void handleDangingLineDisconnectedAtBoundary(Network network, Context context) {
        if (config.disconnectNetworkSideOfDanglingLinesIfBoundaryIsDisconnected()) {
            for (DanglingLine dl : network.getDanglingLines()) {
                String terminalBoundaryId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary").orElse(null);
                if (terminalBoundaryId == null) {
                    LOG.warn("Dangling line {}: alias for terminal at boundary is missing", dl.getId());
                } else {
                    CgmesTerminal terminalBoundary = cgmes.terminal(terminalBoundaryId);
                    if (terminalBoundary == null) {
                        LOG.warn("Dangling line {}: terminal at boundary with id {} is not found in CGMES model", dl.getId(), terminalBoundaryId);
                    } else {
                        if (!terminalBoundary.connected() && dl.getTerminal().isConnected()) {
                            LOG.warn("DanglingLine {} was connected at network side and disconnected at boundary side. It has been disconnected also at network side.", dl.getId());
                            CgmesReports.danglingLineDisconnectedAtBoundaryHasBeenDisconnectedReport(context.getReportNode(), dl.getId());
                            dl.getTerminal().disconnect();
                        }
                    }
                }
            }
        }
    }

    private void adjustMultipleUnpairedDanglingLinesAtSameBoundaryNode(Network network, Context context) {
        network.getDanglingLineStream(DanglingLineFilter.UNPAIRED)
                .filter(dl -> dl.getTerminal().isConnected())
                .collect(groupingBy(Conversion::getDanglingLineBoundaryNode))
                .values().stream()
                // Only perform adjustment for the groups with more than one connected dangling line
                .filter(dls -> dls.size() > 1)
                .forEach(dls -> adjustMultipleUnpairedDanglingLinesAtSameBoundaryNode(dls, context));
    }

    private void adjustMultipleUnpairedDanglingLinesAtSameBoundaryNode(List<DanglingLine> dls, Context context) {
        // All dangling lines will have same value for p0, q0. Take it from the first one
        double p0 = dls.get(0).getP0();
        double q0 = dls.get(0).getQ0();
        // Divide this value between all connected dangling lines
        // This method is called only if there is more than 1 connected dangling line
        long count = dls.size();
        final double p0Adjusted = p0 / count;
        final double q0Adjusted = q0 / count;
        dls.forEach(dl -> {
            LOG.warn("Multiple unpaired DanglingLines were connected at the same boundary side. Adjusted original injection from ({}, {}) to ({}, {}) for dangling line {}.", p0, q0, p0Adjusted, q0Adjusted, dl.getId());
            CgmesReports.multipleUnpairedDanglingLinesAtSameBoundaryReport(context.getReportNode(), dl.getId(), p0, q0, p0Adjusted, q0Adjusted);
            dl.setP0(p0Adjusted);
            dl.setQ0(q0Adjusted);
        });
    }

    public static String getDanglingLineBoundaryNode(DanglingLine dl) {
        String node;
        node = dl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.CONNECTIVITY_NODE_BOUNDARY);
        if (node == null) {
            node = dl.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE_BOUNDARY);
        }
        if (node == null) {
            LOG.warn("Dangling line {} does not have a boundary node identifier.", dl.getId());
            node = "unknown";
        }
        return node;
    }

    private Source isBoundaryBaseVoltage(String graph) {
        //There are unit tests where the boundary file contains the sequence "EQBD" and others "EQ_BD"
        return graph.contains("EQ") && graph.contains("BD") ? Source.BOUNDARY : Source.IGM;
    }

    private static void completeVoltagesAndAngles(Network network) {

        // Voltage and angle in starBus as properties
        network.getThreeWindingsTransformers()
            .forEach(ThreeWindingsTransformerConversion::calculateVoltageAndAngleInStarBus);

        // Voltage and angle in boundary buses
        network.getDanglingLineStream(DanglingLineFilter.UNPAIRED)
            .forEach(AbstractConductingEquipmentConversion::calculateVoltageAndAngleInBoundaryBus);
        network.getTieLines().forEach(tieLine -> AbstractConductingEquipmentConversion.calculateVoltageAndAngleInBoundaryBus(tieLine.getDanglingLine1(), tieLine.getDanglingLine2()));
    }

    private static void createControlArea(CgmesControlAreas cgmesControlAreas, PropertyBag ca) {
        String controlAreaId = ca.getId("ControlArea");
        cgmesControlAreas.newCgmesControlArea()
                .setId(controlAreaId)
                .setName(ca.getLocal("name"))
                .setEnergyIdentificationCodeEic(ca.getLocal("energyIdentCodeEic"))
                .setNetInterchange(ca.asDouble("netInterchange", Double.NaN))
                .setPTolerance(ca.asDouble("pTolerance", Double.NaN))
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
        Boundary boundary = context.terminalMapping().findBoundary(terminalId, context.cgmes());
        if (boundary != null) {
            cgmesControlArea.add(boundary);
            return;
        }
        RegulatingTerminalMapper.mapForTieFlow(terminalId, context).ifPresent(cgmesControlArea::add);
    }

    private void convert(
            PropertyBags elements,
            Function<PropertyBag, AbstractObjectConversion> f) {
        String logTitle = null;
        for (PropertyBag element : elements) {
            AbstractObjectConversion c = f.apply(element);
            if (LOG.isTraceEnabled()) {
                if (logTitle == null) {
                    logTitle = c.getClass().getSimpleName();
                    logTitle = logTitle.replace("Conversion", "");
                }
                LOG.trace(element.tabulateLocals(logTitle));
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

    private Context createContext(Network network, ReportNode reportNode) {
        Context context = new Context(cgmes, config, network, reportNode);
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
        PropertyBags modelProfiles = context.cgmes().modelProfiles();
        String fullModel = "FullModel";
        modelProfiles.sort(Comparator.comparing(p -> p.getId(fullModel)));
        for (PropertyBag modelProfile : modelProfiles) { // Import of profiles ID as properties TODO import them in a dedicated extension
            if (modelProfile.getId(fullModel).equals(context.network().getId())) {
                continue;
            }
            String profile = CgmesNamespace.getProfile(modelProfile.getId("profile"));
            if (profile != null && !"EQ_OP".equals(profile) && !"SV".equals(profile)) { // don't import EQ_OP and SV profiles as they are not used for CGMES export
                context.network()
                        .setProperty(Identifiables.getUniqueId(CGMES_PREFIX_ALIAS_PROPERTIES + profile + "_ID", property -> context.network().hasProperty(property)),
                                modelProfile.getId(fullModel));
            }
        }
        ZonedDateTime modelScenarioTime = cgmes.scenarioTime();
        ZonedDateTime modelCreated = cgmes.created();
        long forecastDistance = Duration.between(modelCreated, modelScenarioTime).toMinutes();
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
                    .setDescription(svDescription.get(0).get("description"))
                    .setSvVersion(readVersion(svDescription, context))
                    .setModelingAuthoritySet(svDescription.get(0).get("modelingAuthoritySet"));
            svDescription.pluckLocals("DependentOn").forEach(adder::addDependency);
            adder.add();
        }
    }

    private void addCgmesSshMetadata(Network network, Context context) {
        PropertyBags sshDescription = cgmes.fullModel(CgmesSubset.STEADY_STATE_HYPOTHESIS.getProfile());
        if (sshDescription != null && !sshDescription.isEmpty()) {
            CgmesSshMetadataAdder adder = network.newExtension(CgmesSshMetadataAdder.class)
                    .setId(sshDescription.get(0).get("FullModel"))
                    .setDescription(sshDescription.get(0).get("description"))
                    .setSshVersion(readVersion(sshDescription, context))
                    .setModelingAuthoritySet(sshDescription.get(0).get("modelingAuthoritySet"));
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
        if (cgmes instanceof CgmesModelTripleStore cgmesModelTripleStore) {
            network.newExtension(CimCharacteristicsAdder.class)
                    .setTopologyKind(cgmes.isNodeBreaker() ? CgmesTopologyKind.NODE_BREAKER : CgmesTopologyKind.BUS_BRANCH)
                    .setCimVersion(cgmesModelTripleStore.getCimVersion())
                    .add();
        }
    }

    private void putVoltageLevelRefByLineContainerIdIfPresent(String lineContainerId, Supplier<String> terminalId1,
                                                              Supplier<String> terminalId2,
                                                              Map<String, VoltageLevel> nominalVoltageByLineContainerId,
                                                              Context context) {
        String vlId = Optional.ofNullable(context.namingStrategy().getIidmId("VoltageLevel",
                        context.cgmes().voltageLevel(cgmes.terminal(terminalId1.get()), context.nodeBreaker())))
                .orElseGet(() -> context.namingStrategy().getIidmId("VoltageLevel",
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
            if (LOG.isTraceEnabled()) {
                LOG.trace(line.tabulateLocals("ACLineSegment"));
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
        String vlId = context.namingStrategy().getIidmId("VoltageLevel", context.cgmes().voltageLevel(t, context.nodeBreaker()));
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
        if (!context.nodeBreaker()) {
            vl.getBusBreakerView().newBus().setId(vldata.nodeId).add();
        }
    }

    private void convertSwitches(Context context, Set<String> delayedBoundaryNodes) {
        for (PropertyBag sw : cgmes.switches()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(sw.tabulateLocals("Switch"));
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
        for (PropertyBag equivalentBranch : cgmes.equivalentBranches()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(equivalentBranch.tabulateLocals("EquivalentBranch"));
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
            if (LOG.isTraceEnabled()) {
                LOG.trace("Transformer {}, {}-winding", t, ends.size());
                ends.forEach(e -> LOG.trace(e.tabulateLocals("TransformerEnd")));
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
            // In some TYNDP there are three acLineSegments at the boundary node,
            // one of them disconnected. The two connected acLineSegments are imported.
            List<BoundaryEquipment> connectedBeqs = beqs.stream()
                .filter(beq -> !beq.isAcLineSegmentDisconnected(context)).toList();
            if (connectedBeqs.size() == 2) {
                convertTwoEquipmentsAtBoundaryNode(context, node, connectedBeqs.get(0), connectedBeqs.get(1));
                // There can be multiple disconnected ACLineSegment to the same X-node (for example, for planning purposes)
                beqs.stream().filter(beq -> !connectedBeqs.contains(beq)).toList()
                    .forEach(beq -> {
                        context.fixed("convertEquipmentAtBoundaryNode",
                                String.format("Multiple AcLineSegments at boundary %s. Disconnected AcLineSegment %s is imported as a dangling line.", node, beq.getAcLineSegmentId()));
                        beq.createConversion(context).convertAtBoundary();
                    });
            } else {
                // This case should not happen and will not result in an equivalent network at the end of the conversion
                context.fixed(node, "More than two connected AcLineSegments at boundary: only dangling lines are created." +
                        " Please note that the converted IIDM network will probably not be equivalent to the CGMES network.");
                beqs.forEach(beq -> beq.createConversion(context).convertAtBoundary());
            }
        }
    }

    private static void convertTwoEquipmentsAtBoundaryNode(Context context, String node, BoundaryEquipment beq1, BoundaryEquipment beq2) {
        EquipmentAtBoundaryConversion conversion1 = beq1.createConversion(context);
        EquipmentAtBoundaryConversion conversion2 = beq2.createConversion(context);

        conversion1.convertAtBoundary();
        Optional<DanglingLine> dl1 = conversion1.getDanglingLine();
        conversion2.convertAtBoundary();
        Optional<DanglingLine> dl2 = conversion2.getDanglingLine();

        if (dl1.isPresent() && dl2.isPresent()) {
            // there can be several dangling lines linked to same x-node in one IGM for planning purposes
            // in this case, we don't merge them
            // please note that only one of them should be connected
            String regionName1 = obtainRegionName(dl1.get().getTerminal().getVoltageLevel());
            String regionName2 = obtainRegionName(dl2.get().getTerminal().getVoltageLevel());

            String pairingKey1 = dl1.get().getPairingKey();
            String pairingKey2 = dl2.get().getPairingKey();

            if (!(pairingKey1 != null && pairingKey1.equals(pairingKey2))) {
                context.ignored(node, "Both dangling lines do not have the same pairingKey: we do not consider them as a merged line");
            } else if (regionName1 != null && regionName1.equals(regionName2)) {
                context.ignored(node, "Both dangling lines are in the same voltage level: we do not consider them as a merged line");
            } else if (dl2.get().getId().compareTo(dl1.get().getId()) >= 0) {
                ACLineSegmentConversion.convertToTieLine(context, dl1.get(), dl2.get());
            } else {
                ACLineSegmentConversion.convertToTieLine(context, dl2.get(), dl1.get());
            }
        }
    }

    private static String obtainRegionName(VoltageLevel voltageLevel) {
        return voltageLevel.getSubstation().map(s -> s.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionName")).orElse(null);
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
                .toList()
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

        public boolean importNodeBreakerAsBusBreaker() {
            return importNodeBreakerAsBusBreaker;
        }

        public Config setImportNodeBreakerAsBusBreaker(boolean importNodeBreakerAsBusBreaker) {
            this.importNodeBreakerAsBusBreaker = importNodeBreakerAsBusBreaker;
            return this;
        }

        public boolean convertBoundary() {
            return convertBoundary;
        }

        public Config setConvertBoundary(boolean convertBoundary) {
            this.convertBoundary = convertBoundary;
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

        public boolean convertSvInjections() {
            return convertSvInjections;
        }

        public Config setConvertSvInjections(boolean convertSvInjections) {
            this.convertSvInjections = convertSvInjections;
            return this;
        }

        public StateProfile getProfileForInitialValuesShuntSectionsTapPositions() {
            return profileForInitialValuesShuntSectionsTapPositions;
        }

        public Config setProfileForInitialValuesShuntSectionsTapPositions(String profileForInitialValuesShuntSectionsTapPositions) {
            switch (Objects.requireNonNull(profileForInitialValuesShuntSectionsTapPositions)) {
                case "SSH":
                case "SV":
                    this.profileForInitialValuesShuntSectionsTapPositions = StateProfile.valueOf(profileForInitialValuesShuntSectionsTapPositions);
                    break;
                default:
                    throw new CgmesModelException("Unexpected profile used for shunt sections / tap positions state hypothesis: " + profileForInitialValuesShuntSectionsTapPositions);
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

        public boolean createActivePowerControlExtension() {
            return createActivePowerControlExtension;
        }

        public Config setCreateActivePowerControlExtension(boolean createActivePowerControlExtension) {
            this.createActivePowerControlExtension = createActivePowerControlExtension;
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

        public NamingStrategy getNamingStrategy() {
            return namingStrategy;
        }

        public Config setNamingStrategy(NamingStrategy namingStrategy) {
            this.namingStrategy = Objects.requireNonNull(namingStrategy);
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

        public double getMissingPermanentLimitPercentage() {
            return missingPermanentLimitPercentage;
        }

        public Config setMissingPermanentLimitPercentage(double missingPermanentLimitPercentage) {
            if (missingPermanentLimitPercentage < 0 || missingPermanentLimitPercentage > 100) {
                throw new IllegalArgumentException("Missing permanent limit percentage must be between 0 and 100.");
            }
            this.missingPermanentLimitPercentage = missingPermanentLimitPercentage;
            return this;
        }

        public CgmesImport.FictitiousSwitchesCreationMode getCreateFictitiousSwitchesForDisconnectedTerminalsMode() {
            return createFictitiousSwitchesForDisconnectedTerminalsMode;
        }

        public Config createFictitiousSwitchesForDisconnectedTerminalsMode(CgmesImport.FictitiousSwitchesCreationMode createFictitiousSwitchesForDisconnectedTerminalsMode) {
            this.createFictitiousSwitchesForDisconnectedTerminalsMode = createFictitiousSwitchesForDisconnectedTerminalsMode;
            return this;
        }

        public Config setDisconnectNetworkSideOfDanglingLinesIfBoundaryIsDisconnected(boolean b) {
            disconnectNetworkSideOfDanglingLinesIfBoundaryIsDisconnected = b;
            return this;
        }

        public boolean disconnectNetworkSideOfDanglingLinesIfBoundaryIsDisconnected() {
            return disconnectNetworkSideOfDanglingLinesIfBoundaryIsDisconnected;
        }

        private boolean convertBoundary = false;

        private boolean createBusbarSectionForEveryConnectivityNode = false;
        private boolean convertSvInjections = true;
        private StateProfile profileForInitialValuesShuntSectionsTapPositions = SSH;
        private boolean storeCgmesModelAsNetworkExtension = true;
        private boolean storeCgmesConversionContextAsNetworkExtension = false;
        private boolean createActivePowerControlExtension = false;

        private CgmesImport.FictitiousSwitchesCreationMode createFictitiousSwitchesForDisconnectedTerminalsMode = CgmesImport.FictitiousSwitchesCreationMode.ALWAYS;

        private boolean ensureIdAliasUnicity = false;
        private boolean importControlAreas = true;
        private boolean importNodeBreakerAsBusBreaker = false;
        private boolean disconnectNetworkSideOfDanglingLinesIfBoundaryIsDisconnected = true;

        private NamingStrategy namingStrategy = new NamingStrategy.Identity();

        // Default interpretation.
        private Xfmr2RatioPhaseInterpretationAlternative xfmr2RatioPhase = Xfmr2RatioPhaseInterpretationAlternative.END1_END2;
        private Xfmr2ShuntInterpretationAlternative xfmr2Shunt = Xfmr2ShuntInterpretationAlternative.END1_END2;
        private Xfmr2StructuralRatioInterpretationAlternative xfmr2StructuralRatio = Xfmr2StructuralRatioInterpretationAlternative.X;

        private Xfmr3RatioPhaseInterpretationAlternative xfmr3RatioPhase = Xfmr3RatioPhaseInterpretationAlternative.NETWORK_SIDE;
        private Xfmr3ShuntInterpretationAlternative xfmr3Shunt = Xfmr3ShuntInterpretationAlternative.NETWORK_SIDE;
        private Xfmr3StructuralRatioInterpretationAlternative xfmr3StructuralRatio = Xfmr3StructuralRatioInterpretationAlternative.STAR_BUS_SIDE;

        private double missingPermanentLimitPercentage = 100;
    }

    private final CgmesModel cgmes;
    private final Config config;
    private final List<CgmesImportPostProcessor> postProcessors;
    private final List<CgmesImportPreProcessor> preProcessors;
    private final NetworkFactory networkFactory;

    private static final Logger LOG = LoggerFactory.getLogger(Conversion.class);

    public static final String NETWORK_PS_CGMES_MODEL_DETAIL = "CGMESModelDetail";
    public static final String NETWORK_PS_CGMES_MODEL_DETAIL_BUS_BRANCH = "bus-branch";
    public static final String NETWORK_PS_CGMES_MODEL_DETAIL_NODE_BREAKER = "node-breaker";

    public static final String CGMES_PREFIX_ALIAS_PROPERTIES = "CGMES.";
    public static final String PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL = CGMES_PREFIX_ALIAS_PROPERTIES + "isCreatedForDisconnectedTerminal";
    public static final String PROPERTY_IS_EQUIVALENT_SHUNT = CGMES_PREFIX_ALIAS_PROPERTIES + "isEquivalentShunt";
    public static final String PROPERTY_HYDRO_PLANT_STORAGE_TYPE = CGMES_PREFIX_ALIAS_PROPERTIES + "hydroPlantStorageKind";
    public static final String PROPERTY_FOSSIL_FUEL_TYPE = CGMES_PREFIX_ALIAS_PROPERTIES + "fuelType";
    public static final String PROPERTY_CGMES_ORIGINAL_CLASS = CGMES_PREFIX_ALIAS_PROPERTIES + "originalClass";
    public static final String PROPERTY_BUSBAR_SECTION_TERMINALS = CGMES_PREFIX_ALIAS_PROPERTIES + "busbarSectionTerminals";
}
