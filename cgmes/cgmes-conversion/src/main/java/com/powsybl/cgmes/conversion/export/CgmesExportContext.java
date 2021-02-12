/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.cgmes.extensions.CgmesSshMetadata;
import com.powsybl.cgmes.extensions.CgmesSvMetadata;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.extensions.CimCharacteristics;
import com.powsybl.cgmes.model.CgmesNamespace;

import com.powsybl.iidm.network.Network;
import org.joda.time.DateTime;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesExportContext {

    public enum TopologicalMappingUse {
        MAPPING_ONLY,
        PARTIAL_MAPPING,
        NO_MAPPING
    }

    private int cimVersion = 16;
    private CgmesTopologyKind topologyKind = CgmesTopologyKind.BUS_BRANCH;
    private DateTime scenarioTime = DateTime.now();

    private ModelDescription svModelDescription = new ModelDescription("SV Model", CgmesNamespace.SV_PROFILE);
    private ModelDescription sshModelDescription = new ModelDescription("SSH Model", CgmesNamespace.SSH_PROFILE);

    private boolean exportBoundaryPowerFlows = false;

    private final Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping = new HashMap<>();

    private TopologicalMappingUse topologicalMappingUse = TopologicalMappingUse.NO_MAPPING;
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
            this.description = Objects.requireNonNull(description);
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
        CgmesIidmMapping cgmesIidmMapping = network.getExtension(CgmesIidmMapping.class);
        if (cgmesIidmMapping != null) {
            topologicalMappingUse = TopologicalMappingUse.MAPPING_ONLY;
            topologicalNodeByBusBreakerBusMapping.putAll(cgmesIidmMapping.toMap());
            unmappedTopologicalNodes.addAll(cgmesIidmMapping.getUnmappedTopologicalNodes());
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

    public CgmesExportContext setTopologicalMappingUse(TopologicalMappingUse topologicalMappingUse) {
        this.topologicalMappingUse = Objects.requireNonNull(topologicalMappingUse);
        return this;
    }

    public Set<String> getTopologicalNodesByBusBreakerBus(String busId) {
        if (topologicalMappingUse == TopologicalMappingUse.MAPPING_ONLY) {
            return topologicalNodeByBusBreakerBusMapping.get(busId);
        } else if (topologicalMappingUse == TopologicalMappingUse.NO_MAPPING) {
            return Collections.singleton(busId);
        } else if (topologicalMappingUse == TopologicalMappingUse.PARTIAL_MAPPING) {
            return Optional.ofNullable(topologicalNodeByBusBreakerBusMapping.get(busId)).orElseGet(() -> Collections.singleton(busId));
        }
        throw new AssertionError("Unexpected mapping use: " + topologicalMappingUse);
    }

    public CgmesExportContext setTopologicalNodeByBusBreakerBusMapping(Map<String, Set<String>> topologicalNodeByBusBreakerBusMapping) {
        this.topologicalNodeByBusBreakerBusMapping.clear();
        this.topologicalNodeByBusBreakerBusMapping.putAll(topologicalNodeByBusBreakerBusMapping);
        return this;
    }

    public Set<String> getUnmappedTopologicalNodes() {
        return Collections.unmodifiableSet(unmappedTopologicalNodes);
    }

    public void isMapped(String mappedTopologicalNode) {
        this.unmappedTopologicalNodes.remove(mappedTopologicalNode);
    }

    public CgmesExportContext setUnmappedTopologicalNodes(Set<String> unmappedTopologicalNodes) {
        this.unmappedTopologicalNodes.clear();
        this.unmappedTopologicalNodes.addAll(unmappedTopologicalNodes);
        return this;
    }
}
