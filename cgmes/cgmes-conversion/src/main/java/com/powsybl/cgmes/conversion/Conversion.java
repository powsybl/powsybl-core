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
import java.util.stream.Stream;

import static com.powsybl.cgmes.conversion.Conversion.Config.StateProfile.SSH;
import static com.powsybl.cgmes.conversion.Update.updateLoads;
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
        // Check presence of report node for functional logs and EQ profile in the data source
        Objects.requireNonNull(reportNode);
        if (!cgmes.hasEquipmentCore()) {
            throw new CgmesModelException("Data source does not contain EquipmentCore data");
        }

        // Apply preprocessors, which mainly create missing containers
        ReportNode preProcessorsNode = CgmesReports.applyingPreprocessorsReport(reportNode);
        for (CgmesImportPreProcessor preProcessor : preProcessors) {
            CgmesReports.applyingProcessorReport(preProcessorsNode, preProcessor.getName());
            preProcessor.process(cgmes);
        }
        if (LOG.isTraceEnabled() && cgmes.baseVoltages() != null) {
            LOG.trace("{}{}{}", "BaseVoltages", System.lineSeparator(), cgmes.baseVoltages().tabulate());
        }

        // Create base network with metadata information
        Network network = createNetwork();
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);

        Context context = createContext(network, reportNode);

        assignNetworkProperties(context);
        addMetadataModels(network, context);
        addCimCharacteristics(network);

        // Build mappings
        context.pushReportNode(CgmesReports.buildingMappingsReport(reportNode));
        context.nodeContainerMapping().build();
        BaseVoltageMappingAdder bvAdder = network.newExtension(BaseVoltageMappingAdder.class);
        cgmes.baseVoltages().forEach(bv -> bvAdder.addBaseVoltage(bv.getId("BaseVoltage"), bv.asDouble("nominalVoltage"), isBoundaryBaseVoltage(bv.getLocal("graph"))));
        bvAdder.add();
        cgmes.computedTerminals().forEach(t -> context.terminalMapping().buildTopologicalNodeCgmesTerminalsMapping(t));
        cgmes.regulatingControls().forEach(p -> context.regulatingControlMapping().cacheRegulatingControls(p));
        context.popReportNode();

        // Convert containers
        convert(cgmes.substations(), CgmesNames.SUBSTATION, context);
        convert(cgmes.voltageLevels(), CgmesNames.VOLTAGE_LEVEL, context);
        createFictitiousVoltageLevelsForLineContainers(context);

        // Convert topology
        PropertyBags nodes = context.nodeBreaker() ? cgmes.connectivityNodes() : cgmes.topologicalNodes();
        if (context.nodeBreaker()) {
            convert(nodes, CgmesNames.CONNECTIVITY_NODE, context);
        } else {
            convert(nodes, CgmesNames.TOPOLOGICAL_NODE, context);
        }
        if (!context.config().createBusbarSectionForEveryConnectivityNode()) {
            convert(cgmes.busBarSections(), CgmesNames.BUSBAR_SECTION, context);
        }

        // Convert single terminal equipments
        convert(cgmes.grounds(), CgmesNames.GROUND, context);
        convert(cgmes.energyConsumers(), CgmesNames.ENERGY_CONSUMER, context);
        convert(cgmes.energySources(), CgmesNames.ENERGY_SOURCE, context);
        convert(cgmes.equivalentInjections(), CgmesNames.EQUIVALENT_INJECTION, context);
        convert(cgmes.externalNetworkInjections(), CgmesNames.EXTERNAL_NETWORK_INJECTION, context);
        convert(cgmes.shuntCompensators(), CgmesNames.SHUNT_COMPENSATOR, context);
        convert(cgmes.equivalentShunts(), CgmesNames.EQUIVALENT_SHUNT, context);
        convert(cgmes.staticVarCompensators(), CgmesNames.STATIC_VAR_COMPENSATOR, context);
        convert(cgmes.asynchronousMachines(), CgmesNames.ASYNCHRONOUS_MACHINE, context);
        convert(cgmes.synchronousMachinesAll(), CgmesNames.SYNCHRONOUS_MACHINE, context);

        // Convert multiple terminals equipments
        // We will delay the conversion of some lines/switches that have an end at boundary
        // They have to be processed after all lines/switches have been reviewed
        // FIXME(Luma) store delayedBoundaryNodes in context
        Set<String> delayedBoundaryNodes = new HashSet<>();
        convertSwitches(context, delayedBoundaryNodes);
        convertACLineSegmentsToLines(context, delayedBoundaryNodes);
        convertEquivalentBranchesToLines(context, delayedBoundaryNodes);
        convert(cgmes.seriesCompensators(), CgmesNames.SERIES_COMPENSATOR, context);
        convertTransformers(context, delayedBoundaryNodes);
        context.pushReportNode(CgmesReports.convertingElementTypeReport(reportNode, "equipments at boundaries"));
        delayedBoundaryNodes.forEach(node -> convertEquipmentAtBoundaryNode(context, node));
        context.popReportNode();

        // Convert DC equipments, limits, SV injections, control areas, regulating controls
        context.pushReportNode(CgmesReports.convertingElementTypeReport(reportNode, "DC network"));
        CgmesDcConversion cgmesDcConversion = new CgmesDcConversion(cgmes, context);
        cgmesDcConversion.convert();
        clearUnattachedHvdcConverterStations(network, context);
        context.popReportNode();

        convert(cgmes.operationalLimits(), CgmesNames.OPERATIONAL_LIMIT, context);
        context.loadingLimitsMapping().addAll();
        setSelectedOperationalLimitsGroup(context);

        if (config.convertSvInjections()) {
            convert(cgmes.svInjections(), CgmesNames.SV_INJECTION, context);
        }

        if (config.importControlAreas()) {
            convert(cgmes.controlAreas(), CgmesNames.CONTROL_AREA, context);
            convert(cgmes.tieFlows(), CgmesNames.TIE_FLOW, context);
        }

        context.pushReportNode(CgmesReports.convertingElementTypeReport(reportNode, CgmesNames.REGULATING_CONTROL));
        context.regulatingControlMapping().setAllRegulatingControls(network);
        context.popReportNode();

        // Fix dangling lines issues
        context.pushReportNode(CgmesReports.fixingDanglingLinesIssuesReport(reportNode));
        handleDangingLineDisconnectedAtBoundary(network, context);
        adjustMultipleUnpairedDanglingLinesAtSameBoundaryNode(network, context);
        context.popReportNode();

        // Set voltages and angles
        context.pushReportNode(CgmesReports.settingVoltagesAndAnglesReport(reportNode));
        voltageAngles(nodes, context);
        completeVoltagesAndAngles(network);
        context.popReportNode();

        // Save/store data for debug or external validation
        if (config.debugTopology()) {
            debugTopology(context);
        }
        if (config.storeCgmesModelAsNetworkExtension()) {
            network.newExtension(CgmesModelExtensionAdder.class).withModel(cgmes).add();
        }
        if (config.storeCgmesConversionContextAsNetworkExtension()) {
            network.newExtension(CgmesConversionContextExtensionAdder.class).withContext(context).add();
        }

        ReportNode postProcessorsNode = CgmesReports.applyingPostprocessorsReport(reportNode);
        for (CgmesImportPostProcessor postProcessor : postProcessors) {
            // FIXME generic cgmes models may not have an underlying triplestore
            // TODO maybe pass the properties to the post processors
            CgmesReports.applyingProcessorReport(postProcessorsNode, postProcessor.getName());
            postProcessor.process(network, cgmes.tripleStore());
        }

        CgmesReports.importedCgmesNetworkReport(reportNode, network.getId());

        updateWithAllInputs(network, reportNode);

        return network;
    }

    private void updateWithAllInputs(Network network, ReportNode reportNode) {
        if (!sshOrSvIsIncludedInCgmesModel(this.cgmes)) {
            return;
        }
        this.cgmes.invalidateCaches();
        this.cgmes.setQueryCatalog("-update");
        Context updateContext = createUpdateContext(network, reportNode);

        // add processes to create new equipment using update data (ssh and sv data)

        update(network, updateContext, reportNode);
    }

    // TODO Remove CIM14 support after PR #3375 (Drop support for CIM14) has been merged into the main branch
    private static boolean sshOrSvIsIncludedInCgmesModel(CgmesModel cgmes) {
        return cgmes.version().contains("CIM14")
                || cgmes.fullModels().stream()
                .map(fullModel -> fullModel.getId("profileList"))
                .anyMatch(profileList -> profileList.contains("SteadyStateHypothesis") || profileList.contains("StateVariables"));
    }

    public void update(Network network, ReportNode reportNode) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(reportNode);
        Context updateContext = createUpdateContext(network, reportNode);
        update(network, updateContext, reportNode);
    }

    private void update(Network network, Context updateContext, ReportNode reportNode) {
        // Inspect the contents of the loaded data
        if (LOG.isDebugEnabled()) {
            PropertyBags nts = cgmes.numObjectsByType();
            LOG.debug("CGMES objects read for the update:");
            nts.forEach(nt -> LOG.debug(String.format("  %5d %s", nt.asInt("numObjects"), nt.getLocal("Type"))));
            nts.forEach(nt -> LOG.debug(cgmes.allObjectsOfType(nt.getLocal("Type")).tabulateLocals()));
        }

        updateLoads(network, cgmes, updateContext);
        network.runValidationChecks(false, reportNode);
        network.setMinimumAcceptableValidationLevel(ValidationLevel.STEADY_STATE_HYPOTHESIS);
    }

    /**
     * Retrieve the Collection of OperationalLimitGroups for identifiable that have flow limits
     * (branch, dangling line, 3w-transformer).
     * If the collection has only one element, it gets to be the identifiable's selectedGroup.
     * If there is more than one element in the collection, don't set any as selected.
     * @param context The conversion's Context.
     */
    private void setSelectedOperationalLimitsGroup(Context context) {
        // Set selected limits group for branches
        context.network().getBranchStream().map(b -> (Branch<?>) b).forEach(branch -> {
            // Side 1
            Collection<OperationalLimitsGroup> limitsHolder1 = branch.getOperationalLimitsGroups1();
            if (limitsHolder1.size() == 1) {
                branch.setSelectedOperationalLimitsGroup1(limitsHolder1.iterator().next().getId());
            }
            // Side 2
            Collection<OperationalLimitsGroup> limitsHolder2 = branch.getOperationalLimitsGroups2();
            if (limitsHolder2.size() == 1) {
                branch.setSelectedOperationalLimitsGroup2(limitsHolder2.iterator().next().getId());
            }
        });

        // Set selected limits group for Dangling lines
        context.network().getDanglingLineStream().forEach(dl -> {
            Collection<OperationalLimitsGroup> limitsHolder = dl.getOperationalLimitsGroups();
            if (limitsHolder.size() == 1) {
                dl.setSelectedOperationalLimitsGroup(limitsHolder.iterator().next().getId());
            }
        });

        // Set selected limits group for 3w transformers legs
        context.network().getThreeWindingsTransformerStream().flatMap(ThreeWindingsTransformer::getLegStream).forEach(leg -> {
            Collection<OperationalLimitsGroup> limitsHolder = leg.getOperationalLimitsGroups();
            if (limitsHolder.size() == 1) {
                leg.setSelectedOperationalLimitsGroup(limitsHolder.iterator().next().getId());
            }
        });
    }

    private void handleDangingLineDisconnectedAtBoundary(Network network, Context context) {
        if (config.disconnectNetworkSideOfDanglingLinesIfBoundaryIsDisconnected()) {
            for (DanglingLine dl : network.getDanglingLines()) {
                String terminalBoundaryId = dl.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary").orElse(null);
                if (terminalBoundaryId == null) {
                    LOG.warn("Dangling line {}: alias for terminal at boundary is missing", dl.getId());
                } else {
                    disconnectDanglingLineAtBounddary(dl, terminalBoundaryId, context);
                }
            }
        }
    }

    private void disconnectDanglingLineAtBounddary(DanglingLine dl, String terminalBoundaryId, Context context) {
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

    private void convert(PropertyBags elements, String elementType, Context context) {
        context.pushReportNode(CgmesReports.convertingElementTypeReport(context.getReportNode(), elementType));
        for (PropertyBag element : elements) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(element.tabulateLocals(elementType));
            }
            AbstractObjectConversion c = switch (elementType) {
                case CgmesNames.SUBSTATION -> new SubstationConversion(element, context);
                case CgmesNames.VOLTAGE_LEVEL -> new VoltageLevelConversion(element, context);
                case CgmesNames.CONNECTIVITY_NODE, CgmesNames.TOPOLOGICAL_NODE -> new NodeConversion(elementType, element, context);
                case CgmesNames.BUSBAR_SECTION -> new BusbarSectionConversion(element, context);
                case CgmesNames.GROUND -> new GroundConversion(element, context);
                case CgmesNames.ENERGY_CONSUMER -> new EnergyConsumerConversion(element, context);
                case CgmesNames.ENERGY_SOURCE -> new EnergySourceConversion(element, context);
                case CgmesNames.EQUIVALENT_INJECTION -> new EquivalentInjectionConversion(element, context);
                case CgmesNames.EXTERNAL_NETWORK_INJECTION -> new ExternalNetworkInjectionConversion(element, context);
                case CgmesNames.SHUNT_COMPENSATOR -> new ShuntConversion(element, context);
                case CgmesNames.EQUIVALENT_SHUNT -> new EquivalentShuntConversion(element, context);
                case CgmesNames.STATIC_VAR_COMPENSATOR -> new StaticVarCompensatorConversion(element, context);
                case CgmesNames.ASYNCHRONOUS_MACHINE -> new AsynchronousMachineConversion(element, context);
                case CgmesNames.SYNCHRONOUS_MACHINE -> new SynchronousMachineConversion(element, context);
                case CgmesNames.SERIES_COMPENSATOR -> new SeriesCompensatorConversion(element, context);
                case CgmesNames.OPERATIONAL_LIMIT -> new OperationalLimitConversion(element, context);
                case CgmesNames.SV_INJECTION -> new SvInjectionConversion(element, context);
                case CgmesNames.CONTROL_AREA -> new ControlAreaConversion(element, context);
                case CgmesNames.TIE_FLOW -> new TieFlowConversion(element, context);
                default -> throw new IllegalArgumentException("Invalid elementType.");
            };
            if (c.insideBoundary()) {
                c.convertInsideBoundary();
            } else if (c.valid()) {
                c.convert();
            }
        }
        context.popReportNode();
    }

    private Network createNetwork() {
        String networkId = cgmes.modelId();
        String sourceFormat = "CGMES";
        return networkFactory.createNetwork(networkId, sourceFormat);
    }

    private Context createContext(Network network, ReportNode reportNode) {
        Context context = new Context(cgmes, config, network, reportNode);
        context.dc().initialize();
        return context;
    }

    private Context createUpdateContext(Network network, ReportNode reportNode) {
        Context context = new Context(cgmes, config, network, reportNode);
        context.buildUpdateCache();
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

    /**
     * Read the model header (the FullModel node) that holds metadata information.
     * The metadata will be stored in the {@link CgmesMetadataModels} extension.
     * @param network The network described by the model header and that will hold the extension.
     * @param context The conversion context.
     */
    private void addMetadataModels(Network network, Context context) {
        PropertyBags ps = cgmes.fullModels();
        if (ps.isEmpty()) {
            return;
        }
        CgmesMetadataModelsAdder modelsAdder = network.newExtension(CgmesMetadataModelsAdder.class);
        for (PropertyBag p : ps) {
            CgmesMetadataModelsAdder.ModelAdder modelAdder = modelsAdder.newModel()
                .setId(p.getId("FullModel"))
                .setSubset(subsetFromGraph(p.getLocal("graph")))
                .setDescription(p.getId("description"))
                .setVersion(readVersion(p, context))
                .setModelingAuthoritySet(p.getId("modelingAuthoritySet"));
            addMetadataModelReferences(p, "profileList", modelAdder::addProfile);
            addMetadataModelReferences(p, "dependentOnList", modelAdder::addDependentOn);
            addMetadataModelReferences(p, "supersedesList", modelAdder::addSupersedes);
            modelAdder.add();
        }
        modelsAdder.add();
    }

    /**
     * Add references (profiles, dependencies, supersedes) to the {@link CgmesMetadataModel} being created by the adder.
     * @param p The property bag holding the references.
     * @param refsProperty The property name to look for in the property bag.
     *                     The property value must be split to retrieve the complete list of references.
     * @param adder The method in the adder that will add the references to the model.
     */
    private void addMetadataModelReferences(PropertyBag p, String refsProperty, Function<String, CgmesMetadataModelsAdder.ModelAdder> adder) {
        String refs = p.get(refsProperty);
        if (refs != null && !refs.isEmpty()) {
            for (String ref : refs.split(" ")) {
                adder.apply(ref);
            }
        }
    }

    /**
     * Retrieve the subset from the graph.
     * @param graph The file name. It shall contain the subset identifier.
     * @return The {@link CgmesSubset} corresponding to the graph.
     */
    private CgmesSubset subsetFromGraph(String graph) {
        return Stream.of(CgmesSubset.values())
                .filter(subset -> subset.isValidName(graph))
                .findFirst()
                .orElse(CgmesSubset.UNKNOWN);
    }

    /**
     * Retrieve the version number from the property bag.
     * @param propertyBag The bag where to look for a version property.
     * @param context The conversion context.
     * @return The version number if found and is a proper integer, else the default value: 1.
     */
    private int readVersion(PropertyBag propertyBag, Context context) {
        try {
            return propertyBag.asInt("version");
        } catch (NumberFormatException e) {
            context.fixed("Version", "The version is expected to be an integer: " + propertyBag.get("version") + ". Fixed to 1");
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

    private void convertACLineSegmentsToLines(Context context, Set<String> delayedBoundaryNodes) {
        context.pushReportNode(CgmesReports.convertingElementTypeReport(context.getReportNode(), CgmesNames.AC_LINE_SEGMENT));
        for (PropertyBag line : cgmes.acLineSegments()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(line.tabulateLocals(CgmesNames.AC_LINE_SEGMENT));
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
        context.popReportNode();
    }

    // Fictitious voltageLevels for Line and Substation(when it includes nodes) containers
    private void createFictitiousVoltageLevelsForLineContainers(Context context) {

        context.nodeContainerMapping().getFictitiousVoltageLevelsForLineContainersToBeCreated().forEach(fictitiousVoltageLevelId -> {
            String containerId = context.nodeContainerMapping().getContainerId(fictitiousVoltageLevelId).orElseThrow();
            String containerName = context.nodeContainerMapping().getContainerName(fictitiousVoltageLevelId).orElseThrow();
            String referenceVoltageLevelId = context.nodeContainerMapping().getReferenceVoltageLevelId(fictitiousVoltageLevelId).orElseThrow();

            if (context.network().getVoltageLevel(fictitiousVoltageLevelId) == null) {
                VoltageLevel referenceVoltageLevel = context.network().getVoltageLevel(referenceVoltageLevelId);
                if (referenceVoltageLevel == null) {
                    throw new ConversionException("VoltageLevel not found for voltageLevelId: " + referenceVoltageLevelId);
                }
                createFictitiousVoltageLevelsForLineContainer(context, fictitiousVoltageLevelId, containerId, containerName, referenceVoltageLevel);
            }
        });
    }

    private void createFictitiousVoltageLevelsForLineContainer(Context context, String fictitiousVoltageLevelId, String containerId, String containerName, VoltageLevel vlref) {
        LOG.warn("Fictitious Voltage Level {} created for Line container {} name {}", fictitiousVoltageLevelId, containerId, containerName);
        // Nominal voltage and low/high limits are copied from the reference voltage level, if it is given
        VoltageLevel vl = context.network().newVoltageLevel()
                .setNominalV(vlref.getNominalV())
                .setTopologyKind(
                        context.nodeBreaker()
                                ? TopologyKind.NODE_BREAKER
                                : TopologyKind.BUS_BREAKER)
                .setLowVoltageLimit(vlref.getLowVoltageLimit())
                .setHighVoltageLimit(vlref.getHighVoltageLimit())
                .setId(fictitiousVoltageLevelId)
                .setFictitious(true)
                .setName(containerName)
                .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                .add();
        vl.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "LineContainerId", containerId);
    }

    private void convertSwitches(Context context, Set<String> delayedBoundaryNodes) {
        context.pushReportNode(CgmesReports.convertingElementTypeReport(context.getReportNode(), CgmesNames.SWITCH));
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
        context.popReportNode();
    }

    private void convertEquivalentBranchesToLines(Context context, Set<String> delayedBoundaryNodes) {
        context.pushReportNode(CgmesReports.convertingElementTypeReport(context.getReportNode(), CgmesNames.EQUIVALENT_BRANCH));
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
        context.popReportNode();
    }

    private void convertTransformers(Context context, Set<String> delayedBoundaryNodes) {
        context.pushReportNode(CgmesReports.convertingElementTypeReport(context.getReportNode(), CgmesNames.POWER_TRANSFORMER));
        cgmes.transformers().stream()
                .map(t -> context.transformerEnds(t.getId("PowerTransformer")))
                .forEach(ends -> {
                    String transformerId = ends.get(0).getId("PowerTransformer");
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Transformer {}, {}-winding", transformerId, ends.size());
                        ends.forEach(e -> LOG.trace(e.tabulateLocals("TransformerEnd")));
                    }
                    if (ends.size() == 2) {
                        convertTwoWindingsTransformers(context, ends, delayedBoundaryNodes);
                    } else if (ends.size() == 3) {
                        convertThreeWindingsTransformers(context, ends);
                    } else {
                        String what = "PowerTransformer " + transformerId;
                        Supplier<String> reason = () -> String.format("Has %d ends. Only 2 or 3 ends are supported", ends.size());
                        context.invalid(what, reason);
                    }
                });
        context.popReportNode();
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
                NodeConversion nc = new NodeConversion(CgmesNames.CONNECTIVITY_NODE, n, context);
                if (!nc.insideBoundary() || nc.insideBoundary() && context.config().convertBoundary()) {
                    nc.setVoltageAngleNodeBreaker();
                }
            }
        }
    }

    private void clearUnattachedHvdcConverterStations(Network network, Context context) {
        // In case of faulty CGMES files, remove HVDC Converter Stations without HVDC lines
        network.getHvdcConverterStationStream()
                .filter(converter -> converter.getHvdcLine() == null)
                .forEach(converter -> {
                    CgmesReports.removingUnattachedHvdcConverterStationReport(context.getReportNode(), converter.getId());
                    context.ignored("HVDC Converter Station " + converter.getId(), "No correct linked HVDC line found.");
                    converter.remove();
                });
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

        /**
         * Specifies the default behavior to apply when updating equipment attributes
         * and no value is provided.
         * <br/>
         * The available options are:
         * <ul>
         *   <li><b>EQ</b>: Uses the default value received from the EQ file.</li>
         *   <li><b>DEFAULT</b>: Assigns a predefined default value.</li>
         *   <li><b>EMPTY</b>: Leaves the attribute empty (e.g., {@code Double.NaN}) if allowed.</li>
         *   <li><b>PREVIOUS</b>: Reuses the value from the previous update.</li>
         * </ul>
         */
        public enum DefaultValue {
            EQ,
            DEFAULT,
            EMPTY,
            PREVIOUS
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
            String forInitialValuesShuntSectionsTapPositions = Objects.requireNonNull(profileForInitialValuesShuntSectionsTapPositions);
            if (forInitialValuesShuntSectionsTapPositions.equals("SSH") || forInitialValuesShuntSectionsTapPositions.equals("SV")) {
                this.profileForInitialValuesShuntSectionsTapPositions = StateProfile.valueOf(profileForInitialValuesShuntSectionsTapPositions);
            } else {
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

        public boolean updateTerminalConnectionInNodeBreakerVoltageLevel() {
            return UPDATE_TERMINAL_CONNECTION_IN_NODE_BREAKER_VOLTAGE_LEVEL;
        }

        public List<DefaultValue> updateDefaultValuesPriority() {
            return updateDefaultValuesPriority;
        }

        public boolean getCreateFictitiousVoltageLevelsForEveryNode() {
            return createFictitiousVoltageLevelsForEveryNode;
        }

        public Config setCreateFictitiousVoltageLevelsForEveryNode(boolean b) {
            createFictitiousVoltageLevelsForEveryNode = b;
            return this;
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
        private boolean createFictitiousVoltageLevelsForEveryNode = true;
        private static final boolean UPDATE_TERMINAL_CONNECTION_IN_NODE_BREAKER_VOLTAGE_LEVEL = false;
        private final List<DefaultValue> updateDefaultValuesPriority = List.of(DefaultValue.EQ, DefaultValue.DEFAULT, DefaultValue.EMPTY);
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
    public static final String PROPERTY_WIND_GEN_UNIT_TYPE = CGMES_PREFIX_ALIAS_PROPERTIES + "windGenUnitType";
    public static final String PROPERTY_CGMES_ORIGINAL_CLASS = CGMES_PREFIX_ALIAS_PROPERTIES + "originalClass";
    public static final String PROPERTY_BUSBAR_SECTION_TERMINALS = CGMES_PREFIX_ALIAS_PROPERTIES + "busbarSectionTerminals";
    public static final String PROPERTY_CGMES_GOVERNOR_SCD = CGMES_PREFIX_ALIAS_PROPERTIES + "governorSCD";
    public static final String PROPERTY_CGMES_SYNCHRONOUS_MACHINE_TYPE = CGMES_PREFIX_ALIAS_PROPERTIES + "synchronousMachineType";
    public static final String PROPERTY_CGMES_SYNCHRONOUS_MACHINE_OPERATING_MODE = CGMES_PREFIX_ALIAS_PROPERTIES + "synchronousMachineOperatingMode";
    public static final String PROPERTY_OPERATIONAL_LIMIT_SET_IDENTIFIERS = CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.OPERATIONAL_LIMIT_SET + "_identifiers";
    public static final String PROPERTY_REGULATING_CONTROL = CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.REGULATING_CONTROL;
}
