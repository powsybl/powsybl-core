/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.extensions.*;
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
        addIidmMappingsTopologicalNode(network);
        addIidmMappingsBaseVoltage(network);
    }

    private void addIidmMappingsTopologicalNode(Network network) {
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

    private void addIidmMappingsBaseVoltage(Network network) {
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
                if (!bvsFromBusBreaker.containsKey(nominalV)) {
                    String baseVoltageId = CgmesExportUtil.getUniqueId();
                    bvsFromBusBreaker.put(nominalV, baseVoltageId);
                    mappedBvs.add(baseVoltageId);
                }
            }

            baseVoltageByNominalVoltageMapping.putAll(bvsFromBusBreaker);
            unmappedBaseVoltages.removeAll(mappedBvs);
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
                topologicalNode = bus.getId();
                busViewBus = terminal.getBusView().getBus();
                if (topologicalNode != null && busViewBus != null) {
                    tnsFromBusBreaker.computeIfAbsent(busViewBus.getId(), b -> new HashSet<>()).add(topologicalNode);
                    mappedTns.add(topologicalNode);
                }
            }
        }
        topologicalNodeByBusViewBusMapping.putAll(tnsFromBusBreaker);
        unmappedTopologicalNodes.removeAll(mappedTns);
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
