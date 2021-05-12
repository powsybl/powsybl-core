/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
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

    private boolean exportBoundaryPowerFlows = false;

    private final Map<String, Set<String>> topologicalNodeByBusViewBusMapping = new HashMap<>();
    private final Set<String> unmappedTopologicalNodes = new HashSet<>();

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
        addTopologicalNodeMappings(network);
    }

    public void addTopologicalNodeMappings(Network network) {
        // For a merging view we plan to call CgmesExportContext() and then addTopologicalNodesMapping(network) for every network
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
            Map<String, Set<String>> tnsFromBusBreaker = new HashMap<>();
            Set<String> mappedTns = new HashSet<>();
            for (VoltageLevel vl : network.getVoltageLevels()) {
                for (Bus configuredBus : vl.getBusBreakerView().getBuses()) {
                    Bus busViewBus;
                    String topologicalNode;
                    if (vl.getTopologyKind() == TopologyKind.BUS_BREAKER) {
                        // Bus/breaker IIDM networks have been created from bus/branch CGMES data
                        // CGMES Topological Nodes have been used as configured bus identifiers
                        topologicalNode = configuredBus.getId();
                        busViewBus = vl.getBusView().getMergedBus(configuredBus.getId());
                    } else {
                        // We throw an error if we do not have a Bus-TN mapping for node/breaker
                        // TODO (Luma) When TP file is exported,
                        // the identifiers for TN and the mapping Bus-TN should be established here.
                        // TP export should use TN identifiers defined in the mapping
                        // topologicalNode = ..
                        // TODO (Luma) remove this exception when TN identifiers are assigned and TP file is exported
                        String problem = "Node/breaker model without explicit mapping between IIDM buses and CGMES Topological Nodes";
                        String solution = String.format("To be able to export you must import the CGMES data with the parameter %s set to true",
                            CgmesImport.CREATE_CGMES_EXPORT_MAPPING);
                        String msg = String.format("%s. %s", problem, solution);
                        throw new PowsyblException(msg);
                    }
                    if (busViewBus != null && topologicalNode != null) {
                        tnsFromBusBreaker.computeIfAbsent(busViewBus.getId(), b -> new HashSet<>()).add(topologicalNode);
                        mappedTns.add(topologicalNode);
                    }
                }
            }
            topologicalNodeByBusViewBusMapping.putAll(tnsFromBusBreaker);
            unmappedTopologicalNodes.removeAll(mappedTns);
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

    public String getCimNamespace() {
        return CgmesNamespace.getCimNamespace(cimVersion);
    }

    public Set<String> getTopologicalNodesByBusViewBus(String busId) {
        return topologicalNodeByBusViewBusMapping.get(busId);
    }

    public Set<String> getUnmappedTopologicalNodes() {
        return Collections.unmodifiableSet(unmappedTopologicalNodes);
    }

    public void isMapped(String mappedTopologicalNode) {
        this.unmappedTopologicalNodes.remove(mappedTopologicalNode);
    }
}
