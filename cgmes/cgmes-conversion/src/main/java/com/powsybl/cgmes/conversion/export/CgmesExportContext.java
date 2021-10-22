/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.ConversionException;
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

    private int cimVersion = 16;
    private CgmesTopologyKind topologyKind = CgmesTopologyKind.BUS_BRANCH;
    private DateTime scenarioTime = DateTime.now();

    private ModelDescription eqModelDescription = new ModelDescription("EQ Model", CgmesNamespace.EQ_PROFILE);
    private ModelDescription svModelDescription = new ModelDescription("SV Model", CgmesNamespace.SV_PROFILE);
    private ModelDescription sshModelDescription = new ModelDescription("SSH Model", CgmesNamespace.SSH_PROFILE);

    private boolean exportBoundaryPowerFlows = true;
    private boolean exportFlowsForSwitches = false;

    private final Map<String, Set<String>> topologicalNodeByBusViewBusMapping = new HashMap<>();
    private final Set<String> unmappedTopologicalNodes = new HashSet<>();

    private final Map<Double, String> baseVoltageByNominalVoltageMapping = new HashMap<>();
    private final Set<String> unmappedBaseVoltages = new HashSet<>();

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
        // TODO CgmesSvMetadata and CgmesSshMetadata could be in fact same class
        // Add multiple instances of CgmesMetadata to Network, one for each profile
        CgmesSvMetadata svMetadata = network.getExtension(CgmesSvMetadata.class);
        if (svMetadata != null) {
            svModelDescription.setDescription(svMetadata.getDescription());
            svModelDescription.setVersion(svMetadata.getSvVersion() + 1);
            svModelDescription.addDependencies(svMetadata.getDependencies());
            svModelDescription.setModelingAuthoritySet(svMetadata.getModelingAuthoritySet());
        }
        CgmesSshMetadata sshMetadata = network.getExtension(CgmesSshMetadata.class);
        if (sshMetadata != null) {
            sshModelDescription.setDescription(sshMetadata.getDescription());
            sshModelDescription.setVersion(sshMetadata.getSshVersion() + 1);
            sshModelDescription.addDependencies(sshMetadata.getDependencies());
            sshModelDescription.setModelingAuthoritySet(sshMetadata.getModelingAuthoritySet());
        }
        addIidmMappings(network);
    }

    /**
     * @deprecated Not used anymore. To add the topological nodes mappings, use
     * {@link CgmesExportContext#addIidmMappings(Network)} instead.
     */
    @Deprecated
    public void addTopologicalNodeMappings(Network network) {
        throw new ConversionException("Deprecated. Not used anymore");
    }

    public void addIidmMappings(Network network) {
        // For a merging view we plan to call CgmesExportContext() and then addIidmMappings(network) for every network
        addIidmMappingsTopologicalNodes(network);
        addIidmMappingsBaseVoltages(network);
        addIidmMappingsTerminals(network);
        addIidmMappingsGenerators(network);
        addIidmMappingsShuntCompensators(network);
        addIidmMappingsTapChangers(network);
        addIidmMappingsEquivalentInjection(network);
    }

    private void addIidmMappingsTopologicalNodes(Network network) {
        CgmesIidmMapping cgmesIidmMapping = network.getExtension(CgmesIidmMapping.class);
        if (cgmesIidmMapping != null) {
            Map<String, Set<String>> tnsByBus = cgmesIidmMapping.topologicalNodesByBusViewBusMap();
            topologicalNodeByBusViewBusMapping.putAll(tnsByBus);
            unmappedTopologicalNodes.addAll(cgmesIidmMapping.getUnmappedTopologicalNodes());

            // And remove from unmapped the currently mapped
            // When we have multiple networks, mappings from a new Network may add mapped TNs to the list
            unmappedTopologicalNodes.removeAll(tnsByBus.values().stream().flatMap(Set::stream).collect(Collectors.toSet()));
        } else {
            // If we do not have an explicit mapping
            // For bus/branch models there is a 1:1 mapping between busBreakerView bus and TN
            // We can not obtain the configured buses inside a BusView bus looking only at connected terminals
            // If we consider only connected terminals we would miss configured buses that only have connections through switches
            // Switches do not add as terminals
            // We have to rely on the busView to obtain the calculated bus for every configured bus (getMergedBus)s
            for (VoltageLevel vl : network.getVoltageLevels()) {
                if (vl.getTopologyKind() == TopologyKind.BUS_BREAKER) {
                    addTopologicalNodeBusBreakerMappings(vl);
                } else {
                    addTopologicalNodeNodeBreakerMappings(vl);
                }
            }
        }
    }

    private void addTopologicalNodeBusBreakerMappings(VoltageLevel vl) {
        Map<String, Set<String>> tnsFromBusBreaker = new HashMap<>();
        Set<String> mappedTns = new HashSet<>();
        for (Bus configuredBus : vl.getBusBreakerView().getBuses()) {
            Bus busViewBus;
            String topologicalNode;
            // Bus/breaker IIDM networks have been created from bus/branch CGMES data
            // CGMES Topological Nodes have been used as configured bus identifiers
            topologicalNode = configuredBus.getId();
            busViewBus = vl.getBusView().getMergedBus(configuredBus.getId());
            if (busViewBus != null && topologicalNode != null) {
                tnsFromBusBreaker.computeIfAbsent(busViewBus.getId(), b -> new HashSet<>()).add(topologicalNode);
                mappedTns.add(topologicalNode);
            }
        }
        topologicalNodeByBusViewBusMapping.putAll(tnsFromBusBreaker);
        unmappedTopologicalNodes.removeAll(mappedTns);
    }

    private void addTopologicalNodeNodeBreakerMappings(VoltageLevel vl) {
        Map<String, Set<String>> tnsFromBusBreaker = new HashMap<>();
        Set<String> mappedTns = new HashSet<>();
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
                        tnsFromBusBreaker.computeIfAbsent(busViewBus.getId(), b -> new HashSet<>()).add(topologicalNode);
                        mappedTns.add(topologicalNode);
                    }
                }
            }
        }
        topologicalNodeByBusViewBusMapping.putAll(tnsFromBusBreaker);
        unmappedTopologicalNodes.removeAll(mappedTns);
    }

    private void addIidmMappingsBaseVoltages(Network network) {
        CgmesIidmMapping cgmesIidmMapping = network.getExtension(CgmesIidmMapping.class);
        if (cgmesIidmMapping != null) {
            Map<Double, String> bvByNominalVoltage = cgmesIidmMapping.baseVoltagesByNominalVoltageMap();
            baseVoltageByNominalVoltageMapping.putAll(bvByNominalVoltage);
            unmappedBaseVoltages.addAll(cgmesIidmMapping.getUnmappedBaseVoltages());

            // And remove from unmapped the currently mapped
            // When we have multiple networks, mappings from a new Network may add mapped TNs to the list
            unmappedBaseVoltages.removeAll(bvByNominalVoltage.values().stream().collect(Collectors.toSet()));
        } else {
            Map<Double, String> bvsFromBusBreaker = new HashMap<>();
            Set<String> mappedBvs = new HashSet<>();
            for (VoltageLevel vl : network.getVoltageLevels()) {
                double nominalV = vl.getNominalV();
                String baseVoltageId = CgmesExportUtil.getUniqueId();
                bvsFromBusBreaker.computeIfAbsent(nominalV, v -> baseVoltageId);
                mappedBvs.add(baseVoltageId);
            }

            baseVoltageByNominalVoltageMapping.putAll(bvsFromBusBreaker);
            unmappedBaseVoltages.removeAll(mappedBvs);
        }
    }

    private void addIidmMappingsTerminals(Network network) {
        for (Connectable<?> c : network.getConnectables()) {
            for (Terminal t : c.getTerminals()) {
                addIidmMappingsTerminal(t, c);
            }
        }

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

        for (HvdcLine line : network.getHvdcLines()) {
            String dcNode1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode1").orElse(null);
            if (dcNode1 == null) {
                dcNode1 = CgmesExportUtil.getUniqueId();
                line.addAlias(dcNode1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode1");
            }
            String dcNode2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode2").orElse(null);
            if (dcNode2 == null) {
                dcNode2 = CgmesExportUtil.getUniqueId();
                line.addAlias(dcNode2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCNode2");
            }
            String dcTerminal1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal1").orElse(null);
            if (dcTerminal1 == null) {
                dcTerminal1 = CgmesExportUtil.getUniqueId();
                line.addAlias(dcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal1");
            }
            String dcTerminal2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal2").orElse(null);
            if (dcTerminal2 == null) {
                dcTerminal2 = CgmesExportUtil.getUniqueId();
                line.addAlias(dcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCTerminal2");
            }
            String acdcConverterDcTerminal1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal1").orElse(null);
            if (acdcConverterDcTerminal1 == null) {
                acdcConverterDcTerminal1 = CgmesExportUtil.getUniqueId();
                line.addAlias(acdcConverterDcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal1");
            }
            String acdcConverterDcTerminal2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal2").orElse(null);
            if (acdcConverterDcTerminal2 == null) {
                acdcConverterDcTerminal2 = CgmesExportUtil.getUniqueId();
                line.addAlias(acdcConverterDcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "ACDCConverterDCTerminal2");
            }
        }
    }

    private void addIidmMappingsTerminal(Terminal t, Connectable<?> c) {
        if (c instanceof DanglingLine) {
            String terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Network").orElse(null);
            if (terminalId == null) {
                terminalId = CgmesExportUtil.getUniqueId();
                c.addAlias(terminalId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Network");
            }
            String boundaryId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary").orElse(null);
            if (boundaryId == null) {
                boundaryId = CgmesExportUtil.getUniqueId();
                c.addAlias(boundaryId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Boundary");
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

    private void addIidmMappingsGenerators(Network network) {
        for (Generator generator : network.getGenerators()) {
            String generatingUnit = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit");
            if (generatingUnit == null) {
                generatingUnit = CgmesExportUtil.getUniqueId();
                generator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "GeneratingUnit", generatingUnit);
            }
            String regulatingControlId = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl");
            if (regulatingControlId == null && (generator.isVoltageRegulatorOn() || !Objects.equals(generator, generator.getRegulatingTerminal().getConnectable()))) {
                regulatingControlId = CgmesExportUtil.getUniqueId();
                generator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", regulatingControlId);
            }
        }
    }

    private void addIidmMappingsShuntCompensators(Network network) {
        for (ShuntCompensator shuntCompensator : network.getShuntCompensators()) {
            String regulatingControlId = shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl");
            if (regulatingControlId == null) {
                regulatingControlId = CgmesExportUtil.getUniqueId();
                shuntCompensator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", regulatingControlId);
            }
        }
    }

    private void addIidmMappingsTapChangers(Network network) {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            addIidmPhaseTapChanger(twt, twt.getPhaseTapChanger());
            addIidmRatioTapChanger(twt, twt.getRatioTapChanger());
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            addIidmPhaseTapChanger(twt, twt.getLeg1().getPhaseTapChanger(), 1);
            addIidmRatioTapChanger(twt, twt.getLeg1().getRatioTapChanger(), 1);
            addIidmPhaseTapChanger(twt, twt.getLeg2().getPhaseTapChanger(), 2);
            addIidmRatioTapChanger(twt, twt.getLeg2().getRatioTapChanger(), 2);
            addIidmPhaseTapChanger(twt, twt.getLeg3().getPhaseTapChanger(), 3);
            addIidmRatioTapChanger(twt, twt.getLeg3().getRatioTapChanger(), 3);
        }
    }

    private void addIidmPhaseTapChanger(Identifiable<?> eq, PhaseTapChanger ptc) {
        if (ptc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1)
                    .orElseGet(() -> eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 2).orElse(null));
            if (tapChangerId == null) {
                tapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(tapChangerId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1);
            }
        }
    }

    private void addIidmRatioTapChanger(Identifiable<?> eq, RatioTapChanger rtc) {
        if (rtc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1)
                    .orElseGet(() -> eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 2).orElse(null));
            if (tapChangerId == null) {
                tapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(tapChangerId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 1);
            }
        }
    }

    private void addIidmPhaseTapChanger(Identifiable<?> eq, PhaseTapChanger ptc, int sequence) {
        if (ptc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + sequence).orElse(null);
            if (tapChangerId == null) {
                tapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(tapChangerId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + sequence);
            }
        }
    }

    private void addIidmRatioTapChanger(Identifiable<?> eq, RatioTapChanger rtc, int sequence) {
        if (rtc != null) {
            String tapChangerId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + sequence).orElse(null);
            if (tapChangerId == null) {
                tapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(tapChangerId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + sequence);
            }
        }
    }

    private void addIidmMappingsEquivalentInjection(Network network) {
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

    public Set<String> getTopologicalNodesByBusViewBus(String busId) {
        return topologicalNodeByBusViewBusMapping.get(busId);
    }

    public Set<String> getUnmappedTopologicalNodes() {
        return Collections.unmodifiableSet(unmappedTopologicalNodes);
    }

    public void isTopologicalNodeMapped(String mappedTopologicalNode) {
        this.unmappedTopologicalNodes.remove(mappedTopologicalNode);
    }

    public String getBaseVoltageByNominalVoltage(double nominalV) {
        return baseVoltageByNominalVoltageMapping.get(nominalV);
    }

    public Set<String> getUnmappedBaseVoltages() {
        return Collections.unmodifiableSet(unmappedBaseVoltages);
    }
}
