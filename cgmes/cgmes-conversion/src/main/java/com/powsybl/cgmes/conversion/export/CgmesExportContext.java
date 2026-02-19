/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.fasterxml.uuid.Generators;
import com.powsybl.cgmes.conversion.CgmesExport.ExportParameters;
import com.powsybl.cgmes.conversion.naming.CgmesObjectReference;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.conversion.naming.NamingStrategyFactory;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.*;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.triplestore.api.PropertyBag;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.Conversion.*;
import static com.powsybl.cgmes.conversion.export.EquipmentExport.hasDifferentTNsAtBothEnds;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CgmesExportContext {

    private static final String DEFAULT_REGION = "default region";

    private CgmesNamespace.Cim cim = CgmesNamespace.CIM_16;
    private CgmesTopologyKind topologyKind = CgmesTopologyKind.NODE_BREAKER;
    private ZonedDateTime scenarioTime = ZonedDateTime.now();
    private ReportNode reportNode = ReportNode.NO_OP;
    private String businessProcess = DEFAULT_BUSINESS_PROCESS;
    private NamingStrategy namingStrategy = new NamingStrategy.Identity();
    private String modelingAuthoritySet = null;
    private String modelDescription = null;
    private String modelVersion = null;
    private String boundaryEqId = null;
    private String boundaryTpId = null;
    private List<String> profiles = null;
    private String baseName = null;
    public static final boolean CGM_EXPORT_VALUE = false;
    public static final boolean EXPORT_BOUNDARY_POWER_FLOWS_DEFAULT_VALUE = true;
    public static final boolean EXPORT_POWER_FLOWS_FOR_SWITCHES_DEFAULT_VALUE = true;
    public static final boolean EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1_DEFAULT_VALUE = false;
    public static final boolean ENCODE_IDS_DEFAULT_VALUE = true;
    public static final boolean EXPORT_LOAD_FLOW_STATUS_DEFAULT_VALUE = true;
    public static final boolean EXPORT_ALL_LIMITS_GROUP_DEFAULT_VALUE = true;
    public static final boolean EXPORT_GENERATORS_IN_LOCAL_REGULATION_MODE_DEFAULT_VALUE = false;
    // From QoCDC 3.3.1 rules IGMConvergence, KirchhoffsFirstLaw, ... that refer to SV_INJECTION_LIMIT=0.1
    public static final double MAX_P_MISMATCH_CONVERGED_DEFAULT_VALUE = 0.1;
    public static final double MAX_Q_MISMATCH_CONVERGED_DEFAULT_VALUE = 0.1;
    public static final boolean EXPORT_SV_INJECTIONS_FOR_SLACKS_DEFAULT_VALUE = true;
    public static final String DEFAULT_MODELING_AUTHORITY_SET_VALUE = "powsybl.org";
    public static final UUID DEFAULT_UUID_NAMESPACE = Generators.nameBasedGenerator().generate(DEFAULT_MODELING_AUTHORITY_SET_VALUE);
    public static final String DEFAULT_BUSINESS_PROCESS = "1D";
    public static final boolean UPDATE_DEPENDENCIES_DEFAULT_VALUE = true;

    private boolean exportBoundaryPowerFlows = EXPORT_BOUNDARY_POWER_FLOWS_DEFAULT_VALUE;
    private boolean exportFlowsForSwitches = EXPORT_POWER_FLOWS_FOR_SWITCHES_DEFAULT_VALUE;
    private boolean exportTransformersWithHighestVoltageAtEnd1 = EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1_DEFAULT_VALUE;
    private boolean exportLoadFlowStatus = EXPORT_LOAD_FLOW_STATUS_DEFAULT_VALUE;
    private boolean exportAllLimitsGroup = EXPORT_ALL_LIMITS_GROUP_DEFAULT_VALUE;
    private boolean exportGeneratorsInLocalRegulationMode = EXPORT_GENERATORS_IN_LOCAL_REGULATION_MODE_DEFAULT_VALUE;
    private double maxPMismatchConverged = MAX_P_MISMATCH_CONVERGED_DEFAULT_VALUE;
    private double maxQMismatchConverged = MAX_Q_MISMATCH_CONVERGED_DEFAULT_VALUE;
    private boolean isExportSvInjectionsForSlacks = EXPORT_SV_INJECTIONS_FOR_SLACKS_DEFAULT_VALUE;
    private boolean updateDependencies = UPDATE_DEPENDENCIES_DEFAULT_VALUE;
    private boolean exportEquipment = false;
    private boolean encodeIds = ENCODE_IDS_DEFAULT_VALUE;

    record BaseVoltageSource(Double nominalV, String id, Source source) { }
    private final Map<Double, BaseVoltageSource> baseVoltageMapping = new HashMap<>();

    record Region(String id, String name) { }
    protected record SubRegion(String id, String name, String regionId) { }
    private final Map<String, String> regionsNameById = new HashMap<>();
    private final Map<String, SubRegion> subRegionsById = new HashMap<>();
    private final Map<String, String> substationsSubRegion = new HashMap<>();
    private final Map<String, String> fictitiousContainers = new HashMap<>();
    private final Map<String, Bus> topologicalNodes = new HashMap<>();
    private final ReferenceDataProvider referenceDataProvider;

    public String getFictitiousContainerFor(Identifiable<?> id) {
        return fictitiousContainers.get(id.getId());
    }

    public void setFictitiousContainerFor(Identifiable<?> id, String containerId) {
        fictitiousContainers.put(id.getId(), containerId);
    }

    public CgmesExportContext() {
        referenceDataProvider = null;
    }

    public CgmesExportContext(Network network) {
        this(network, null, NamingStrategyFactory.create(NamingStrategyFactory.IDENTITY, DEFAULT_UUID_NAMESPACE));
    }

    public CgmesExportContext(Network network, ReferenceDataProvider referenceDataProvider) {
        this(network, referenceDataProvider, NamingStrategyFactory.create(NamingStrategyFactory.IDENTITY, DEFAULT_UUID_NAMESPACE));
    }

    public CgmesExportContext(Network network, UUID uuidNamespace) {
        this(network, null, NamingStrategyFactory.create(NamingStrategyFactory.IDENTITY, uuidNamespace));
    }

    public CgmesExportContext(Network network, ReferenceDataProvider referenceDataProvider, UUID uuidNamespace) {
        this(network, referenceDataProvider, NamingStrategyFactory.create(NamingStrategyFactory.IDENTITY, uuidNamespace));
    }

    public CgmesExportContext(Network network, ReferenceDataProvider referenceDataProvider, NamingStrategy namingStrategy) {
        this(network, referenceDataProvider, namingStrategy, ReportNode.NO_OP, null);
    }

    public CgmesExportContext(Network network, ReferenceDataProvider referenceDataProvider,
                              NamingStrategy namingStrategy, ReportNode reportNode, ExportParameters exportParameters) {
        setReportNode(reportNode);
        this.referenceDataProvider = referenceDataProvider;
        this.namingStrategy = namingStrategy;
        scenarioTime = network.getCaseDate();
        addParameters(exportParameters);
        computeCimVersion(exportParameters, network);
        computeTopologyKind(exportParameters, network);
        computeModelingAuthoritySet(exportParameters, referenceDataProvider);
        computeBoundaryIds(exportParameters, network);
        computeSubstationMapping(network);
        computeBaseVoltageMapping(network);
    }

    private void addParameters(ExportParameters exportParameters) {
        if (exportParameters != null) {
            setExportBoundaryPowerFlows(exportParameters.exportBoundaryPowerFlows());
            setExportFlowsForSwitches(exportParameters.exportFlowsForSwitches());
            setExportTransformersWithHighestVoltageAtEnd1(exportParameters.exportTransformersWithHighestVoltageAtEnd1());
            setExportLoadFlowStatus(exportParameters.exportLoadFlowStatus());
            setExportAllLimitsGroup(exportParameters.exportAllLimitsGroup());
            setExportGeneratorsInLocalRegulationMode(exportParameters.exportGeneratorsInLocalRegulationMode());
            setMaxPMismatchConverged(exportParameters.maxPMismatchConverged());
            setMaxQMismatchConverged(exportParameters.maxQMismatchConverged());
            setExportSvInjectionsForSlacks(exportParameters.exportSvInjectionsForSlacks());
            setEncodeIds(exportParameters.encodeIds());
            setBusinessProcess(exportParameters.businessProcess());
            setModelDescription(exportParameters.modelDescription());
            setModelVersion(exportParameters.modelVersion());
            setProfiles(exportParameters.profiles());
            setBaseName(exportParameters.baseName());
            setUpdateDependencies(exportParameters.updateDependencies());
        }
    }

    private void computeCimVersion(ExportParameters exportParameters, Network network) {
        if (exportParameters != null && exportParameters.cimVersion() != null) {
            setCimVersion(Integer.parseInt(exportParameters.cimVersion()));
        } else if (network.getExtension(CimCharacteristics.class) != null) {
            setCimVersion(network.getExtension(CimCharacteristics.class).getCimVersion());
        }
    }

    private void computeTopologyKind(ExportParameters exportParameters, Network network) {
        if (exportParameters != null && exportParameters.topologyKind() != null) {
            setTopologyKind(Enum.valueOf(CgmesTopologyKind.class, exportParameters.topologyKind()));
        } else if (network.getExtension(CimCharacteristics.class) != null) {
            setTopologyKind(network.getExtension(CimCharacteristics.class).getTopologyKind());
        } else {
            CgmesTopologyKind topologyKindForExport = detectNetworkTopologyKind(network);
            if (topologyKindForExport == CgmesTopologyKind.MIXED_TOPOLOGY) {
                if (getCimVersion() < 100) {
                    topologyKindForExport = CgmesTopologyKind.BUS_BRANCH;
                } else {
                    topologyKindForExport = CgmesTopologyKind.NODE_BREAKER;
                }
            }
            setTopologyKind(topologyKindForExport);
        }
    }

    private CgmesTopologyKind detectNetworkTopologyKind(Network network) {
        long nodeBreakerVoltageLevelsCount = network.getVoltageLevelStream()
                .filter(vl -> vl.getTopologyKind() == TopologyKind.NODE_BREAKER)
                .count();
        long busBreakerVoltageLevelsCount = network.getVoltageLevelStream()
                .filter(vl -> vl.getTopologyKind() == TopologyKind.BUS_BREAKER)
                .count();

        if (nodeBreakerVoltageLevelsCount > 0 && busBreakerVoltageLevelsCount == 0) {
            return CgmesTopologyKind.NODE_BREAKER;
        } else if (nodeBreakerVoltageLevelsCount == 0 && busBreakerVoltageLevelsCount > 0) {
            return CgmesTopologyKind.BUS_BRANCH;
        } else {
            return CgmesTopologyKind.MIXED_TOPOLOGY;
        }
    }

    private void computeModelingAuthoritySet(ExportParameters exportParameters, ReferenceDataProvider referenceDataProvider) {
        if (exportParameters != null && exportParameters.modelingAuthoritySet() != null) {
            setModelingAuthoritySet(exportParameters.modelingAuthoritySet());
        } else if (referenceDataProvider != null) {
            PropertyBag sourcingActor = referenceDataProvider.getSourcingActor();
            if (sourcingActor.containsKey("masUri")) {
                setModelingAuthoritySet(sourcingActor.get("masUri"));
            }
        }
    }

    private void computeBoundaryIds(ExportParameters exportParameters, Network network) {
        // Boundary EQ id
        if (exportParameters != null && exportParameters.boundaryEqId() != null) {
            setBoundaryEqId(exportParameters.boundaryEqId());
        } else if (referenceDataProvider != null && referenceDataProvider.getEquipmentBoundaryId() != null) {
            setBoundaryEqId(referenceDataProvider.getEquipmentBoundaryId());
        } else if (network.hasProperty(PROPERTY_EQ_BD_ID)) {
            setBoundaryEqId(network.getProperty(PROPERTY_EQ_BD_ID));
        }

        // Boundary TP id
        if (exportParameters != null && exportParameters.boundaryTpId() != null) {
            setBoundaryTpId(exportParameters.boundaryTpId());
        } else if (referenceDataProvider != null && referenceDataProvider.getTopologyBoundaryId() != null) {
            setBoundaryTpId(referenceDataProvider.getTopologyBoundaryId());
        } else if (network.hasProperty(PROPERTY_TP_BD_ID)) {
            setBoundaryTpId(network.getProperty(PROPERTY_TP_BD_ID));
        }
    }

    public ReferenceDataProvider getReferenceDataProvider() {
        return referenceDataProvider;
    }

    private void computeSubstationMapping(Network network) {
        for (Substation substation : network.getSubstations()) {
            Region region = getOrCreateRegion(substation);
            String subRegionId = getOrCreateSubRegion(substation, region);
            substationsSubRegion.put(substation.getId(), subRegionId);
        }
    }

    private Region getOrCreateRegion(Substation substation) {
        String regionId;
        String regionName;
        String defaultRegionName = substation.hasProperty(PROPERTY_REGION_NAME) ?
                substation.getProperty(PROPERTY_REGION_NAME) :
                substation.getCountry().map(Country::name).orElse(DEFAULT_REGION);
        if (substation.hasProperty(PROPERTY_REGION_ID)) {
            // Add this geographical region id from property if it is not already mapped.
            regionId = namingStrategy.getCgmesIdFromProperty(substation, PROPERTY_REGION_ID);
            regionName = regionsNameById.computeIfAbsent(regionId, k -> defaultRegionName);
        } else {
            if (referenceDataProvider != null && referenceDataProvider.getSourcingActorRegion() != null) {
                // If not defined by a property, try to retrieve geographical region id from the reference data.
                Pair<String, String> regionRef = referenceDataProvider.getSourcingActorRegion();
                regionId = regionRef.getLeft();
                regionName = regionsNameById.computeIfAbsent(regionId, k -> regionRef.getRight());
            } else {
                // If not in the reference data, create a new unique id.
                regionId = namingStrategy.getCgmesId(ref(defaultRegionName), GEOGRAPHICAL_REGION);
                regionName = regionsNameById.computeIfAbsent(regionId, k -> defaultRegionName);
            }
        }
        return new Region(regionId, regionName);
    }

    private String getOrCreateSubRegion(Substation substation, Region region) {
        Set<String> geoTags = substation.getGeographicalTags();
        String subRegionName = geoTags.size() == 1 ? geoTags.iterator().next() : region.name;
        String subRegionId = substation.hasProperty(PROPERTY_SUB_REGION_ID) ?
                namingStrategy.getCgmesIdFromProperty(substation, PROPERTY_SUB_REGION_ID) :
                namingStrategy.getCgmesId(ref(subRegionName), SUB_GEOGRAPHICAL_REGION);
        subRegionsById.computeIfAbsent(subRegionId, k -> new SubRegion(subRegionId, subRegionName, region.id));

        return subRegionId;
    }

    private void computeBaseVoltageMapping(Network network) {
        DecimalFormat noTrailingZerosFormat = new DecimalFormat("0.##");

        // Retrieve reference BaseVoltage mapping stored in the dedicated extension.
        // This may not match exactly the base voltages used in the network.
        BaseVoltageMapping bvMappingExtension = network.getExtension(BaseVoltageMapping.class);
        Map<Double, BaseVoltageMapping.BaseVoltageSource> referenceBvMapping = bvMappingExtension == null ?
            new HashMap<>() :
            bvMappingExtension.getBaseVoltages();

        // Create the mapping (nominalV/id/source) for this network's base voltages.
        network.getVoltageLevelStream()
            .map(VoltageLevel::getNominalV)
            .distinct()
            .forEach(nominalV -> {
                BaseVoltageSource bvSource;
                // If it exists in the extension, keep it.
                if (referenceBvMapping.containsKey(nominalV)) {
                    BaseVoltageMapping.BaseVoltageSource referenceBvSource = referenceBvMapping.get(nominalV);
                    bvSource = new BaseVoltageSource(nominalV, referenceBvSource.getId(), referenceBvSource.getSource());
                } else {
                    // Try to retrieve the BaseVoltage id from reference data.
                    String baseVoltageId = null;
                    if (referenceDataProvider != null) {
                        baseVoltageId = referenceDataProvider.getBaseVoltage(nominalV);
                    }
                    if (baseVoltageId == null) {
                        // If not in the reference data, create a new unique id.
                        CgmesObjectReference vref = ref(noTrailingZerosFormat.format(nominalV));
                        baseVoltageId = namingStrategy.getCgmesId(vref, BASE_VOLTAGE);
                    }
                    bvSource = new BaseVoltageSource(nominalV, baseVoltageId, Source.IGM);
                }
                baseVoltageMapping.put(nominalV, bvSource);
            });
    }

    public boolean isExportEquipment() {
        return exportEquipment;
    }

    public CgmesExportContext setExportEquipment(boolean exportEquipment) {
        this.exportEquipment = exportEquipment;
        return this;
    }

    public boolean isExportedEquipment(Identifiable<?> c) {
        boolean ignored = false;
        if (c instanceof Load load) {
            ignored = load.isFictitious()
                    || isCim16BusBranchExport() && CgmesNames.STATION_SUPPLY.equals(CgmesExportUtil.loadClassName(load));
        } else if (c instanceof Switch sw) {
            ignored = sw.isFictitious() && "true".equals(sw.getProperty(PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL))
                    || isCim16BusBranchExport() && sw.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS, "").equals("GroundDisconnector")
                    || isBusBranchExport() && !sw.isRetained()
                    || isBusBranchExport() && !hasDifferentTNsAtBothEnds(sw);
        }
        return !ignored;
    }

    public int getCimVersion() {
        return cim.getVersion();
    }

    public CgmesExportContext setCimVersion(int cimVersion) {
        cim = CgmesNamespace.getCim(cimVersion);
        return this;
    }

    public CgmesTopologyKind getTopologyKind() {
        return topologyKind;
    }

    public CgmesExportContext setTopologyKind(CgmesTopologyKind topologyKind) {
        this.topologyKind = Objects.requireNonNull(topologyKind);
        return this;
    }

    public ZonedDateTime getScenarioTime() {
        return scenarioTime;
    }

    public CgmesExportContext setScenarioTime(ZonedDateTime scenarioTime) {
        this.scenarioTime = Objects.requireNonNull(scenarioTime);
        return this;
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

    public boolean exportTransformersWithHighestVoltageAtEnd1() {
        return exportTransformersWithHighestVoltageAtEnd1;
    }

    public CgmesExportContext setExportTransformersWithHighestVoltageAtEnd1(boolean exportTransformersWithHighestVoltageAtEnd1) {
        this.exportTransformersWithHighestVoltageAtEnd1 = exportTransformersWithHighestVoltageAtEnd1;
        return this;
    }

    public boolean isExportLoadFlowStatus() {
        return exportLoadFlowStatus;
    }

    public CgmesExportContext setExportLoadFlowStatus(boolean exportLoadFlowStatus) {
        this.exportLoadFlowStatus = exportLoadFlowStatus;
        return this;
    }

    public boolean isExportAllLimitsGroup() {
        return exportAllLimitsGroup;
    }

    public CgmesExportContext setExportAllLimitsGroup(boolean exportAllLimitsGroup) {
        this.exportAllLimitsGroup = exportAllLimitsGroup;
        return this;
    }

    public boolean isExportGeneratorsInLocalRegulationMode() {
        return exportGeneratorsInLocalRegulationMode;
    }

    public CgmesExportContext setExportGeneratorsInLocalRegulationMode(boolean exportGeneratorsInLocalRegulationMode) {
        this.exportGeneratorsInLocalRegulationMode = exportGeneratorsInLocalRegulationMode;
        return this;
    }

    public double getMaxPMismatchConverged() {
        return maxPMismatchConverged;
    }

    public CgmesExportContext setMaxPMismatchConverged(double maxPMismatchConverged) {
        this.maxPMismatchConverged = maxPMismatchConverged;
        return this;
    }

    public double getMaxQMismatchConverged() {
        return maxQMismatchConverged;
    }

    public CgmesExportContext setMaxQMismatchConverged(double maxQMismatchConverged) {
        this.maxQMismatchConverged = maxQMismatchConverged;
        return this;
    }

    public boolean isExportSvInjectionsForSlacks() {
        return isExportSvInjectionsForSlacks;
    }

    public CgmesExportContext setExportSvInjectionsForSlacks(boolean exportSvInjectionsForSlacks) {
        this.isExportSvInjectionsForSlacks = exportSvInjectionsForSlacks;
        return this;
    }

    public String encode(String id) {
        if (encodeIds) {
            return URLEncoder.encode(id, StandardCharsets.UTF_8);
        }
        return id;
    }

    public CgmesExportContext setEncodeIds(boolean encodeIds) {
        this.encodeIds = encodeIds;
        return this;
    }

    public CgmesNamespace.Cim getCim() {
        return cim;
    }

    public NamingStrategy getNamingStrategy() {
        return namingStrategy;
    }

    public CgmesExportContext setNamingStrategy(NamingStrategy namingStrategy) {
        this.namingStrategy = Objects.requireNonNull(namingStrategy);
        return this;
    }

    public Set<BaseVoltageSource> getBaseVoltageSources() {
        return new HashSet<>(baseVoltageMapping.values());
    }

    public String getBaseVoltageIdFromNominalV(double nominalV) {
        return baseVoltageMapping.get(nominalV).id();
    }

    protected Map<String, String> getRegions() {
        return new HashMap<>(regionsNameById);
    }

    protected Set<SubRegion> getSubRegions() {
        return new HashSet<>(subRegionsById.values());
    }

    protected String getSubstationSubRegion(String substationId) {
        return substationsSubRegion.get(substationId);
    }

    public CgmesExportContext setReportNode(ReportNode reportNode) {
        this.reportNode = reportNode;
        return this;
    }

    public ReportNode getReportNode() {
        return this.reportNode;
    }

    public void putTopologicalNode(String tn, Bus bus) {
        topologicalNodes.put(tn, bus);
    }

    public boolean containsTopologicalNode(String tn) {
        return topologicalNodes.containsKey(tn);
    }

    public Map<String, Bus> getTopologicalNodes(Network network) {
        if (topologicalNodes.isEmpty()) {
            return network.getBusBreakerView().getBusStream().collect(Collectors.toMap(b -> namingStrategy.getCgmesId(b), b -> b));
        }
        return Collections.unmodifiableMap(topologicalNodes);
    }

    /**
     * The business process related to the export, used to get a unique ID for EQ, TP, SSH and SV FullModel.
     */
    public String getBusinessProcess() {
        return businessProcess;
    }

    public CgmesExportContext setBusinessProcess(String businessProcess) {
        this.businessProcess = businessProcess;
        return this;
    }

    public String getModelingAuthoritySet() {
        return modelingAuthoritySet;
    }

    public CgmesExportContext setModelingAuthoritySet(String modelingAuthoritySet) {
        this.modelingAuthoritySet = modelingAuthoritySet;
        return this;
    }

    public String getModelDescription() {
        return modelDescription;
    }

    public CgmesExportContext setModelDescription(String modelDescription) {
        this.modelDescription = modelDescription;
        return this;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public CgmesExportContext setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    public String getBoundaryEqId() {
        return boundaryEqId;
    }

    public CgmesExportContext setBoundaryEqId(String boundaryEqId) {
        this.boundaryEqId = boundaryEqId;
        return this;
    }

    public String getBoundaryTpId() {
        return boundaryTpId;
    }

    public CgmesExportContext setBoundaryTpId(String boundaryTpId) {
        this.boundaryTpId = boundaryTpId;
        return this;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public CgmesExportContext setProfiles(List<String> profiles) {
        this.profiles = profiles;
        return this;
    }

    public boolean isCim16BusBranchExport() {
        return getCimVersion() == 16 && isBusBranchExport();
    }

    public boolean isBusBranchExport() {
        return getTopologyKind() == CgmesTopologyKind.BUS_BRANCH;
    }

    public String getBaseName() {
        return baseName;
    }

    public CgmesExportContext setBaseName(String baseName) {
        this.baseName = baseName;
        return this;
    }

    public CgmesExportContext setUpdateDependencies(boolean updateDependencies) {
        this.updateDependencies = updateDependencies;
        return this;
    }

    public boolean updateDependencies() {
        return updateDependencies;
    }
}

