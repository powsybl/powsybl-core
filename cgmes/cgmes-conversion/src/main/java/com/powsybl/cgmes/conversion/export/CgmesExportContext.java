/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesExportContext {

    private static final String REGULATING_CONTROL = "RegulatingControl";
    private static final String GENERATING_UNIT = "GeneratingUnit";

    private static final String DCNODE = "DCNode";
    private static final String DCTERMINAL = "DCTerminal";
    private static final String ACDCCONVERTERDCTERMINAL = "ACDCConverterDCTerminal";

    private static final String TERMINAL_NETWORK = "Terminal_Network";
    private static final String TERMINAL_BOUNDARY = "Terminal_Boundary";

    private int cimVersion = 16;
    private CgmesTopologyKind topologyKind = CgmesTopologyKind.BUS_BRANCH;
    private DateTime scenarioTime = DateTime.now();

    private ModelDescription eqModelDescription = new ModelDescription("EQ Model", CgmesNamespace.EQ_PROFILE);
    private ModelDescription tpModelDescription = new ModelDescription("TP Model", CgmesNamespace.TP_PROFILE);
    private ModelDescription svModelDescription = new ModelDescription("SV Model", CgmesNamespace.SV_PROFILE);
    private ModelDescription sshModelDescription = new ModelDescription("SSH Model", CgmesNamespace.SSH_PROFILE);

    private boolean exportBoundaryPowerFlows = true;
    private boolean exportFlowsForSwitches = false;

    private final Map<String, Set<CgmesIidmMapping.CgmesTopologicalNode>> topologicalNodeByBusViewBusMapping = new HashMap<>();
    private final Set<CgmesIidmMapping.CgmesTopologicalNode> unmappedTopologicalNodes = new HashSet<>();

    private final Map<Double, CgmesIidmMapping.BaseVoltageSource> baseVoltageByNominalVoltageMapping = new HashMap<>();

    public static final class ModelDescription {

        private String description;
        private int version = 1;
        private final List<String> dependencies = new ArrayList<>();
        private String modelingAuthoritySet = "powsybl.org";
        // TODO Each model may have a list of profiles, not only one
        private String profile;

        private ModelDescription(String description, String profile) {
            this.description = description;
            this.profile = profile;
        }

        public String getDescription() {
            return description;
        }

        public ModelDescription setDescription(String description) {
            this.description = description;
            return this;
        }

        public int getVersion() {
            return version;
        }

        public ModelDescription setVersion(int version) {
            this.version = version;
            return this;
        }

        public List<String> getDependencies() {
            return Collections.unmodifiableList(dependencies);
        }

        public ModelDescription addDependency(String dependency) {
            dependencies.add(Objects.requireNonNull(dependency));
            return this;
        }

        public ModelDescription addDependencies(List<String> dependencies) {
            this.dependencies.addAll(Objects.requireNonNull(dependencies));
            return this;
        }

        public ModelDescription removeDependency(String dependency) {
            this.dependencies.remove(dependency);
            return this;
        }

        public ModelDescription clearDependencies() {
            this.dependencies.clear();
            return this;
        }

        public String getModelingAuthoritySet() {
            return modelingAuthoritySet;
        }

        public ModelDescription setModelingAuthoritySet(String modelingAuthoritySet) {
            this.modelingAuthoritySet = Objects.requireNonNull(modelingAuthoritySet);
            return this;
        }

        public String getProfile() {
            return profile;
        }
    }

    public CgmesExportContext(Network network) {
        CimCharacteristics cimCharacteristics = network.getExtension(CimCharacteristics.class);
        if (cimCharacteristics != null) {
            cimVersion = cimCharacteristics.getCimVersion();
            topologyKind = cimCharacteristics.getTopologyKind();
        }
        scenarioTime = network.getCaseDate();
        CgmesModelDescriptions metadata = network.getExtension(CgmesModelDescriptions.class);
        if (metadata != null) {
            metadata.getModel(CgmesNamespace.EQ_PROFILE).ifPresent(eq -> setModel(eqModelDescription, eq));
            metadata.getModel(CgmesNamespace.TP_PROFILE).ifPresent(tp -> setModel(tpModelDescription, tp));
            metadata.getModel(CgmesNamespace.SSH_PROFILE).ifPresent(ssh -> setModel(sshModelDescription, ssh));
            metadata.getModel(CgmesNamespace.SV_PROFILE).ifPresent(sv -> setModel(svModelDescription, sv));
        }
        addIidmMappings(network);
    }

    private static void setModel(ModelDescription contextModel, CgmesModelDescriptions.Model extModel) {
        contextModel.setDescription(extModel.getDescription());
        contextModel.setVersion(extModel.getVersion() + 1);
        contextModel.addDependencies(extModel.getDependencies());
        contextModel.setModelingAuthoritySet(extModel.getModelingAuthoritySet());
    }

    public void addIidmMappings(Network network) {
        // For a merging view we plan to call CgmesExportContext() and then addIidmMappings(network) for every network
        addIidmMappingsSubstations(network);
        CgmesIidmMapping mapping = network.getExtension(CgmesIidmMapping.class);
        if (mapping == null) {
            network.newExtension(CgmesIidmMappingAdder.class).add();
            mapping = network.getExtension(CgmesIidmMapping.class);
            mapping.addTopologyListener();
        }
        addIidmMappingsTopologicalNodes(mapping, network);
        addIidmMappingsBaseVoltages(mapping, network);
        addIidmMappingsTerminals(network);
        addIidmMappingsGenerators(network);
        addIidmMappingsShuntCompensators(network);
        addIidmMappingsEndsAndTapChangers(network);
        addIidmMappingsEquivalentInjection(network);
        addIidmMappingsControlArea(network);
    }

    private void addIidmMappingsSubstations(Network network) {
        for (Substation substation : network.getSubstations()) {
            if (!substation.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionId")) {
                String regionId = CgmesExportUtil.getUniqueId();
                substation.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionId", regionId);
            }
            if (!substation.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "subRegionId")) {
                String subRegionId = CgmesExportUtil.getUniqueId();
                substation.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "subRegionId", subRegionId);
            }
        }
    }

    private void addIidmMappingsTopologicalNodes(CgmesIidmMapping mapping, Network network) {
        updateTopologicalNodesMapping(mapping, network);
        Map<String, Set<CgmesIidmMapping.CgmesTopologicalNode>> tnsByBus = mapping.topologicalNodesByBusViewBusMap();
        topologicalNodeByBusViewBusMapping.putAll(tnsByBus);
        unmappedTopologicalNodes.addAll(mapping.getUnmappedTopologicalNodes());

        // And remove from unmapped the currently mapped
        // When we have multiple networks, mappings from a new Network may add mapped TNs to the list
        unmappedTopologicalNodes.removeAll(tnsByBus.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));
    }

    public static void updateTopologicalNodesMapping(Network network) {
        CgmesIidmMapping mapping = network.getExtension(CgmesIidmMapping.class);
        if (mapping != null) {
            updateTopologicalNodesMapping(mapping, network);
        }
    }

    private static void updateTopologicalNodesMapping(CgmesIidmMapping mapping, Network network) {
        if (mapping.isTopologicalNodeEmpty()) {
            // If we do not have an explicit mapping
            // For bus/branch models there is a 1:1 mapping between busBreakerView bus and TN
            // We can not obtain the configured buses inside a BusView bus looking only at connected terminals
            // If we consider only connected terminals we would miss configured buses that only have connections through switches
            // Switches do not add as terminals
            // We have to rely on the busView to obtain the calculated bus for every configured bus (getMergedBus)s
            for (VoltageLevel vl : network.getVoltageLevels()) {
                if (vl.getTopologyKind() == TopologyKind.BUS_BREAKER) {
                    updateBusBreakerTopologicalNodesMapping(mapping, vl);
                } else {
                    updateNodeBreakerTopologicalNodesMapping(mapping, vl);
                }
            }
        }
    }

    private static void updateBusBreakerTopologicalNodesMapping(CgmesIidmMapping mapping, VoltageLevel vl) {
        for (Bus configuredBus : vl.getBusBreakerView().getBuses()) {
            Bus busViewBus;
            String topologicalNode;
            // Bus/breaker IIDM networks have been created from bus/branch CGMES data
            // CGMES Topological Nodes have been used as configured bus identifiers
            topologicalNode = configuredBus.getId();

            busViewBus = vl.getBusView().getMergedBus(configuredBus.getId());
            if (busViewBus != null && topologicalNode != null) {
                String topologicalNodeName = configuredBus.getNameOrId();
                mapping.putTopologicalNode(busViewBus.getId(), configuredBus.getId(), topologicalNodeName, CgmesIidmMapping.Source.IGM);
            }
        }
    }

    private static void updateNodeBreakerTopologicalNodesMapping(CgmesIidmMapping mapping, VoltageLevel vl) {
        for (int node : vl.getNodeBreakerView().getNodes()) {
            Bus busViewBus;
            String topologicalNode;
            // Node/breaker IIDM networks have been created from node/breaker CGMES data
            // CGMES topological nodes have not been used in model import
            Terminal terminal = vl.getNodeBreakerView().getTerminal(node);
            if (terminal != null) {
                Bus bus = terminal.getBusBreakerView().getBus();
                if (bus != null) {
                    topologicalNode = bus.getId();
                    busViewBus = terminal.getBusView().getBus();
                    if (topologicalNode != null && busViewBus != null) {
                        String topologicalNodeName = bus.getNameOrId();
                        mapping.putTopologicalNode(busViewBus.getId(), bus.getId(), topologicalNodeName, CgmesIidmMapping.Source.IGM);
                    }
                }
            }
        }
    }

    private void addIidmMappingsBaseVoltages(CgmesIidmMapping mapping, Network network) {
        if (mapping.isBaseVoltageEmpty()) {
            for (VoltageLevel vl : network.getVoltageLevels()) {
                double nominalV = vl.getNominalV();
                String baseVoltageId = CgmesExportUtil.getUniqueId();
                mapping.addBaseVoltage(nominalV, baseVoltageId, CgmesIidmMapping.Source.IGM);
            }
        }
        Map<Double, CgmesIidmMapping.BaseVoltageSource> bvByNominalVoltage = mapping.baseVoltagesByNominalVoltageMap();
        baseVoltageByNominalVoltageMapping.putAll(bvByNominalVoltage);
    }

    private static void addIidmMappingsTerminals(Network network) {
        for (Connectable<?> c : network.getConnectables()) {
            for (Terminal t : c.getTerminals()) {
                addIidmMappingsTerminal(t, c);
            }
        }
        addIidmMappingsSwitchTerminals(network);
        addIidmMappingsHvdcTerminals(network);
    }

    private static void addIidmMappingsSwitchTerminals(Network network) {
        for (Switch sw : network.getSwitches()) {
            String terminal1Id = sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "1").orElse(null);
            if (terminal1Id == null) {
                terminal1Id = CgmesExportUtil.getUniqueId();
                sw.addAlias(terminal1Id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "1");
            }
            String terminal2Id = sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "2").orElse(null);
            if (terminal2Id == null) {
                terminal2Id = CgmesExportUtil.getUniqueId();
                sw.addAlias(terminal2Id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "2");
            }
        }
    }

    private static void addIidmMappingsHvdcTerminals(Network network) {
        for (HvdcLine line : network.getHvdcLines()) {
            String dcNode1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCNODE + "1").orElse(null);
            if (dcNode1 == null) {
                dcNode1 = CgmesExportUtil.getUniqueId();
                line.addAlias(dcNode1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCNODE + "1");
            }
            String dcNode2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCNODE + "2").orElse(null);
            if (dcNode2 == null) {
                dcNode2 = CgmesExportUtil.getUniqueId();
                line.addAlias(dcNode2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCNODE + "2");
            }
            String dcTerminal1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCTERMINAL + "1").orElse(null);
            if (dcTerminal1 == null) {
                dcTerminal1 = CgmesExportUtil.getUniqueId();
                line.addAlias(dcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCTERMINAL + "1");
            }
            String dcTerminal2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCTERMINAL + "2").orElse(null);
            if (dcTerminal2 == null) {
                dcTerminal2 = CgmesExportUtil.getUniqueId();
                line.addAlias(dcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCTERMINAL + "2");
            }
            String acdcConverterDcTerminal1 = line.getConverterStation1().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL).orElse(null);
            if (acdcConverterDcTerminal1 == null) {
                acdcConverterDcTerminal1 = CgmesExportUtil.getUniqueId();
                line.getConverterStation1().addAlias(acdcConverterDcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL);
            }
            String acdcConverterDcTerminal2 = line.getConverterStation2().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL).orElse(null);
            if (acdcConverterDcTerminal2 == null) {
                acdcConverterDcTerminal2 = CgmesExportUtil.getUniqueId();
                line.getConverterStation2().addAlias(acdcConverterDcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL);
            }
        }
    }

    private static void addIidmMappingsTerminal(Terminal t, Connectable<?> c) {
        if (c instanceof DanglingLine) {
            String terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_NETWORK).orElse(null);
            if (terminalId == null) {
                terminalId = CgmesExportUtil.getUniqueId();
                c.addAlias(terminalId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_NETWORK);
            }
            String boundaryId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_BOUNDARY).orElse(null);
            if (boundaryId == null) {
                boundaryId = CgmesExportUtil.getUniqueId();
                c.addAlias(boundaryId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_BOUNDARY);
            }
        } else if (c instanceof Load && ((Load) c).isFictitious()) {
            // An fictitious load do not need an alias
        } else {
            int sequenceNumber = CgmesExportUtil.getTerminalSide(t, c);
            String terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + sequenceNumber).orElse(null);
            if (terminalId == null) {
                terminalId = CgmesExportUtil.getUniqueId();
                c.addAlias(terminalId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + sequenceNumber);
            }
        }
    }

    private static void addIidmMappingsGenerators(Network network) {
        for (Generator generator : network.getGenerators()) {
            String generatingUnit = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + GENERATING_UNIT);
            if (generatingUnit == null) {
                generatingUnit = CgmesExportUtil.getUniqueId();
                generator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + GENERATING_UNIT, generatingUnit);
            }
            String regulatingControlId = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL);
            if (regulatingControlId == null && (generator.isVoltageRegulatorOn() || !Objects.equals(generator, generator.getRegulatingTerminal().getConnectable()))) {
                regulatingControlId = CgmesExportUtil.getUniqueId();
                generator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL, regulatingControlId);
            }
        }
    }

    private static void addIidmMappingsShuntCompensators(Network network) {
        for (ShuntCompensator shuntCompensator : network.getShuntCompensators()) {
            String regulatingControlId = shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL);
            if (regulatingControlId == null) {
                regulatingControlId = CgmesExportUtil.getUniqueId();
                shuntCompensator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL, regulatingControlId);
            }
        }
    }

    private static void addIidmMappingsEndsAndTapChangers(Network network) {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            addIidmTransformerEnd(twt, 1);
            addIidmTransformerEnd(twt, 2);
            addIidmPhaseTapChanger(twt, twt.getPhaseTapChanger());
            addIidmRatioTapChanger(twt, twt.getRatioTapChanger());
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            addIidmTransformerEnd(twt, 1);
            addIidmTransformerEnd(twt, 2);
            addIidmTransformerEnd(twt, 3);
            addIidmPhaseTapChanger(twt, twt.getLeg1().getPhaseTapChanger(), 1);
            addIidmRatioTapChanger(twt, twt.getLeg1().getRatioTapChanger(), 1);
            addIidmPhaseTapChanger(twt, twt.getLeg2().getPhaseTapChanger(), 2);
            addIidmRatioTapChanger(twt, twt.getLeg2().getRatioTapChanger(), 2);
            addIidmPhaseTapChanger(twt, twt.getLeg3().getPhaseTapChanger(), 3);
            addIidmRatioTapChanger(twt, twt.getLeg3().getRatioTapChanger(), 3);
        }
    }

    private static void addIidmTransformerEnd(Identifiable<?> eq, int end) {
        String endId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + end).orElse(null);
        if (endId == null) {
            endId = CgmesExportUtil.getUniqueId();
            eq.addAlias(endId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + end);
        }
    }

    private static void addIidmPhaseTapChanger(Identifiable<?> eq, PhaseTapChanger ptc) {
        if (ptc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1)
                    .orElseGet(() -> eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 2).orElse(null));
            if (tapChangerId == null) {
                tapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(tapChangerId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1);
            }
        }
    }

    private static void addIidmRatioTapChanger(Identifiable<?> eq, RatioTapChanger rtc) {
        if (rtc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1)
                    .orElseGet(() -> eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 2).orElse(null));
            if (tapChangerId == null) {
                tapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(tapChangerId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1);
            }
        }
    }

    private static void addIidmPhaseTapChanger(Identifiable<?> eq, PhaseTapChanger ptc, int sequence) {
        if (ptc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + sequence).orElse(null);
            if (tapChangerId == null) {
                tapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(tapChangerId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + sequence);
            }
        }
    }

    private static void addIidmRatioTapChanger(Identifiable<?> eq, RatioTapChanger rtc, int sequence) {
        if (rtc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + sequence).orElse(null);
            if (tapChangerId == null) {
                tapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(tapChangerId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + sequence);
            }
        }
    }

    private static void addIidmMappingsEquivalentInjection(Network network) {
        for (DanglingLine danglingLine : network.getDanglingLines()) {
            String equivalentInjectionId = danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection").orElse(null);
            if (equivalentInjectionId == null) {
                equivalentInjectionId = CgmesExportUtil.getUniqueId();
                danglingLine.addAlias(equivalentInjectionId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection");
            }
            String topologicalNode = danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE).orElse(null);
            if (topologicalNode == null) {
                topologicalNode = CgmesExportUtil.getUniqueId();
                danglingLine.addAlias(topologicalNode, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            }
        }
    }

    private void addIidmMappingsControlArea(Network network) {
        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        if (cgmesControlAreas == null) {
            network.newExtension(CgmesControlAreasAdder.class).add();
            cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
            String cgmesControlAreaId = CgmesExportUtil.getUniqueId();
            cgmesControlAreas.newCgmesControlArea()
                    .setId(cgmesControlAreaId)
                    .setName("Network")
                    .setEnergyIdentificationCodeEic("Network--1")
                    .add();
            CgmesControlArea cgmesControlArea = cgmesControlAreas.getCgmesControlArea(cgmesControlAreaId);
            for (DanglingLine danglingLine : network.getDanglingLines()) {
                cgmesControlArea.add(danglingLine.getTerminal());
            }
        }
    }

    public CgmesExportContext() {
    }

    public int getCimVersion() {
        return cimVersion;
    }

    public CgmesExportContext setCimVersion(int cimVersion) {
        this.cimVersion = cimVersion;
        return this;
    }

    public CgmesTopologyKind getTopologyKind() {
        return topologyKind;
    }

    public CgmesExportContext setTopologyKind(CgmesTopologyKind topologyKind) {
        this.topologyKind = Objects.requireNonNull(topologyKind);
        return this;
    }

    public DateTime getScenarioTime() {
        return scenarioTime;
    }

    public CgmesExportContext setScenarioTime(DateTime scenarioTime) {
        this.scenarioTime = Objects.requireNonNull(scenarioTime);
        return this;
    }

    public ModelDescription getEqModelDescription() {
        return eqModelDescription;
    }

    public ModelDescription getTpModelDescription() {
        return tpModelDescription;
    }

    public ModelDescription getSvModelDescription() {
        return svModelDescription;
    }

    public ModelDescription getSshModelDescription() {
        return sshModelDescription;
    }

    public boolean exportBoundaryPowerFlows() {
        return exportBoundaryPowerFlows;
    }

    public CgmesExportContext setExportBoundaryPowerFlows(boolean exportBoundaryPowerFlows) {
        this.exportBoundaryPowerFlows = exportBoundaryPowerFlows;
        return this;
    }

    public boolean exportFlowsForSwitches() {
        return exportFlowsForSwitches;
    }

    public CgmesExportContext setExportFlowsForSwitches(boolean exportFlowsForSwitches) {
        this.exportFlowsForSwitches = exportFlowsForSwitches;
        return this;
    }

    public String getCimNamespace() {
        return CgmesNamespace.getCimNamespace(cimVersion);
    }

    public Set<CgmesIidmMapping.CgmesTopologicalNode> getTopologicalNodesByBusViewBus(String busId) {
        return topologicalNodeByBusViewBusMapping.get(busId);
    }

    public Set<CgmesIidmMapping.CgmesTopologicalNode> getUnmappedTopologicalNodes() {
        return Collections.unmodifiableSet(unmappedTopologicalNodes);
    }

    public CgmesIidmMapping.CgmesTopologicalNode getUnmappedTopologicalNode(String topologicalNodeId) {
        return unmappedTopologicalNodes.stream().filter(cgmesTopologicalNode -> cgmesTopologicalNode.getCgmesId().equals(topologicalNodeId)).findAny().orElse(null);
    }

    public CgmesIidmMapping.BaseVoltageSource getBaseVoltageByNominalVoltage(double nominalV) {
        return baseVoltageByNominalVoltageMapping.get(nominalV);
    }
}
