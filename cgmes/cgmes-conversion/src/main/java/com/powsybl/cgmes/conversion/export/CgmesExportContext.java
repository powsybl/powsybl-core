/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.fasterxml.uuid.Generators;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.cgmes.conversion.CgmesExport.ExportParameters;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.naming.CgmesObjectReference;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.conversion.naming.NamingStrategyFactory;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.*;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.triplestore.api.PropertyBag;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.export.CgmesExportUtil.obtainSynchronousMachineKind;
import static com.powsybl.cgmes.conversion.export.EquipmentExport.hasDifferentTNsAtBothEnds;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refGeneratingUnit;
import static com.powsybl.cgmes.model.CgmesNames.DC_TERMINAL1;
import static com.powsybl.cgmes.model.CgmesNames.DC_TERMINAL2;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CgmesExportContext {

    private static final String GENERATING_UNIT = "GeneratingUnit";

    private static final String TERMINAL_BOUNDARY = "Terminal_Boundary";
    private static final String REGION_ID = "regionId";
    private static final String REGION_NAME = "regionName";
    private static final String DEFAULT_REGION = "default region";
    public static final String SUB_REGION_ID = "subRegionId";
    private static final String BOUNDARY_EQ_ID_PROPERTY = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EQ_BD_ID";
    private static final String BOUNDARY_TP_ID_PROPERTY = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "TP_BD_ID";

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

    private final Map<Double, BaseVoltageMapping.BaseVoltageSource> baseVoltageByNominalVoltageMapping = new HashMap<>();

    private final BiMap<String, String> regionsIdsByRegionName = HashBiMap.create();
    private final BiMap<String, String> subRegionsIdsBySubRegionName = HashBiMap.create();
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
        addIidmMappings(network);
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
        } else if (network.hasProperty(BOUNDARY_EQ_ID_PROPERTY)) {
            setBoundaryEqId(network.getProperty(BOUNDARY_EQ_ID_PROPERTY));
        }

        // Boundary TP id
        if (exportParameters != null && exportParameters.boundaryTpId() != null) {
            setBoundaryTpId(exportParameters.boundaryTpId());
        } else if (referenceDataProvider != null && referenceDataProvider.getTopologyBoundaryId() != null) {
            setBoundaryTpId(referenceDataProvider.getTopologyBoundaryId());
        } else if (network.hasProperty(BOUNDARY_TP_ID_PROPERTY)) {
            setBoundaryTpId(network.getProperty(BOUNDARY_TP_ID_PROPERTY));
        }
    }

    public ReferenceDataProvider getReferenceDataProvider() {
        return referenceDataProvider;
    }

    public void addIidmMappings(Network network) {
        // For a merging view we plan to call CgmesExportContext() and then addIidmMappings(network) for every network
        // TODO add option to skip this part (if from CGMES)
        addIidmMappingsSubstations(network);
        BaseVoltageMapping bvMapping = network.getExtension(BaseVoltageMapping.class);
        if (bvMapping == null) {
            network.newExtension(BaseVoltageMappingAdder.class).add();
            bvMapping = network.getExtension(BaseVoltageMapping.class);
        }
        addIidmMappingsBaseVoltages(bvMapping, network);
        addIidmMappingsTerminals(network);
        addIidmMappingsGenerators(network);
        addIidmMappingsBatteries(network);
        addIidmMappingsShuntCompensators(network);
        addIidmMappingsStaticVarCompensators(network);
        addIidmMappingsEndsAndTapChangers(network);
        addIidmMappingsEquivalentInjection(network);
    }

    private void addIidmMappingsSubstations(Network network) {
        for (Substation substation : network.getSubstations()) {
            String regionName;
            if (!substation.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGION_ID)) {
                Pair<String, String> region = getCreateRegion(substation);
                String regionId = region.getLeft();
                regionName = region.getRight();
                substation.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGION_ID, regionId);
                substation.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGION_NAME, regionName);
            } else {
                // Only add with this name if the id is not already mapped
                // We can not have the same id mapped to two different names
                String regionId = namingStrategy.getCgmesIdFromProperty(substation, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGION_ID);
                regionName = substation.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGION_NAME);
                if (!regionsIdsByRegionName.containsValue(regionId)) {
                    regionsIdsByRegionName.computeIfAbsent(regionName, k -> regionId);
                }
            }
            String geoTag;
            if (substation.getGeographicalTags().size() == 1) {
                geoTag = substation.getGeographicalTags().iterator().next();
            } else {
                geoTag = regionName;
            }
            if (!substation.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + SUB_REGION_ID)) {
                String id = subRegionsIdsBySubRegionName.computeIfAbsent(geoTag, k -> namingStrategy.getCgmesId(ref(k), SUB_GEOGRAPHICAL_REGION));
                substation.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + SUB_REGION_ID, id);
            } else {
                subRegionsIdsBySubRegionName.computeIfAbsent(geoTag, k -> namingStrategy.getCgmesIdFromProperty(substation, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + SUB_REGION_ID));
            }
        }
    }

    private Pair<String, String> getCreateRegion(Substation substation) {
        // The current substation does not have explicit information for geographical region,
        // Try to obtain it from the reference data based on the current sourcing actor we are using for export
        Pair<String, String> region = null;
        if (referenceDataProvider != null) {
            region = referenceDataProvider.getSourcingActorRegion();
        }
        if (region == null) {
            // If no information is available from reference data,
            // Just create a new geographical region using country name as name
            String regionName = substation.getCountry().map(Country::name).orElse(DEFAULT_REGION);
            String regionId = regionsIdsByRegionName.computeIfAbsent(regionName, k -> namingStrategy.getCgmesId(ref(k), GEOGRAPHICAL_REGION));
            region = Pair.of(regionId, regionName);
        }
        return region;
    }

    private void addIidmMappingsBaseVoltages(BaseVoltageMapping mapping, Network network) {
        DecimalFormat noTrailingZerosFormat = new DecimalFormat("0.##");
        if (mapping.isBaseVoltageEmpty()) {
            // Here we do not have previous information about base voltages
            // (The mapping is filled when the Network has been imported from CGMES)
            // Now that we want to export, we may find some base voltages are defined in the reference data
            for (VoltageLevel vl : network.getVoltageLevels()) {
                double nominalV = vl.getNominalV();
                // Only create a new unique id if no reference data exists
                String baseVoltageId = null;
                if (referenceDataProvider != null) {
                    baseVoltageId = referenceDataProvider.getBaseVoltage(nominalV);
                    if (baseVoltageId != null) {
                        mapping.addBaseVoltage(nominalV, baseVoltageId, Source.BOUNDARY);
                    }
                }
                if (baseVoltageId == null && mapping.getBaseVoltage(nominalV) == null) {
                    CgmesObjectReference vref = ref(noTrailingZerosFormat.format(nominalV));
                    baseVoltageId = namingStrategy.getCgmesId(vref, BASE_VOLTAGE);
                    mapping.addBaseVoltage(nominalV, baseVoltageId, Source.IGM);
                }
            }
        }
        Map<Double, BaseVoltageMapping.BaseVoltageSource> bvByNominalVoltage = mapping.baseVoltagesByNominalVoltageMap();
        baseVoltageByNominalVoltageMapping.putAll(bvByNominalVoltage);
    }

    private void addIidmMappingsTerminals(Network network) {
        for (Connectable<?> c : network.getConnectables()) {
            if (isExportedEquipment(c)) {
                for (Terminal t : c.getTerminals()) {
                    addIidmMappingsTerminal(t, c);
                }
            }
        }
        addIidmMappingsSwitchTerminals(network);
        addIidmMappingsHvdcTerminals(network);
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
            ignored = sw.isFictitious() && "true".equals(sw.getProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL))
                    || isCim16BusBranchExport() && sw.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, "").equals("GroundDisconnector")
                    || isBusBranchExport() && !sw.isRetained()
                    || isBusBranchExport() && !hasDifferentTNsAtBothEnds(sw);
        }
        return !ignored;
    }

    private void addIidmMappingsSwitchTerminals(Network network) {
        for (Switch sw : network.getSwitches()) {
            String terminal1Id = sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "1").orElse(null);
            if (terminal1Id == null) {
                terminal1Id = namingStrategy.getCgmesId(refTyped(sw), TERMINAL, ref(1));
                sw.addAlias(terminal1Id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "1");
            }
            String terminal2Id = sw.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "2").orElse(null);
            if (terminal2Id == null) {
                terminal2Id = namingStrategy.getCgmesId(refTyped(sw), TERMINAL, ref(2));
                sw.addAlias(terminal2Id, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "2");
            }
        }
    }

    private void addIidmMappingsHvdcTerminals(Network network) {
        for (HvdcLine line : network.getHvdcLines()) {
            String dcTerminal1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL1).orElse(null);
            if (dcTerminal1 == null) {
                dcTerminal1 = namingStrategy.getCgmesId(refTyped(line), TERMINAL, ref(1));
                line.addAlias(dcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL1);
            }
            String dcTerminal2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2).orElse(null);
            if (dcTerminal2 == null) {
                dcTerminal2 = namingStrategy.getCgmesId(refTyped(line), TERMINAL, ref(2));
                line.addAlias(dcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2);
            }
            String acdcConverter1DcTerminal1 = line.getConverterStation1().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL1).orElse(null);
            if (acdcConverter1DcTerminal1 == null) {
                acdcConverter1DcTerminal1 = namingStrategy.getCgmesId(refTyped(line.getConverterStation1()), ACDC_CONVERTER_DC_TERMINAL, ref(1));
                line.getConverterStation1().addAlias(acdcConverter1DcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL1);
            }
            String acdcConverter1DcTerminal2 = line.getConverterStation1().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2).orElse(null);
            if (acdcConverter1DcTerminal2 == null) {
                acdcConverter1DcTerminal2 = namingStrategy.getCgmesId(refTyped(line.getConverterStation1()), ACDC_CONVERTER_DC_TERMINAL, ref(2));
                line.getConverterStation1().addAlias(acdcConverter1DcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2);
            }
            String acdcConverter2DcTerminal1 = line.getConverterStation2().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL1).orElse(null);
            if (acdcConverter2DcTerminal1 == null) {
                acdcConverter2DcTerminal1 = namingStrategy.getCgmesId(refTyped(line.getConverterStation2()), ACDC_CONVERTER_DC_TERMINAL, ref(1));
                line.getConverterStation2().addAlias(acdcConverter2DcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL1);
            }
            String acdcConverter2DcTerminal2 = line.getConverterStation2().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2).orElse(null);
            if (acdcConverter2DcTerminal2 == null) {
                acdcConverter2DcTerminal2 = namingStrategy.getCgmesId(refTyped(line.getConverterStation2()), ACDC_CONVERTER_DC_TERMINAL, ref(2));
                line.getConverterStation2().addAlias(acdcConverter2DcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DC_TERMINAL2);
            }
        }
    }

    private void addIidmMappingsTerminal(Terminal t, Connectable<?> c) {
        if (c instanceof DanglingLine) {
            String terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElse(null);
            if (terminalId == null) {
                // Legacy: in previous versions, dangling line terminals were recorded in a different alias
                // read it, remove it and store in the standard alias for equipment terminal (Terminal1)
                terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL).orElse(null);
                if (terminalId != null) {
                    c.removeAlias(terminalId);
                } else {
                    terminalId = namingStrategy.getCgmesId(refTyped(c), TERMINAL);
                }
                c.addAlias(terminalId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1);
            }
            String boundaryId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_BOUNDARY).orElse(null);
            if (boundaryId == null) {
                boundaryId = namingStrategy.getCgmesId(refTyped(c), BOUNDARY_TERMINAL);
                c.addAlias(boundaryId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_BOUNDARY);
            }
        } else {
            int sequenceNumber = CgmesExportUtil.getTerminalSequenceNumber(t);
            String terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + sequenceNumber).orElse(null);
            if (terminalId == null) {
                terminalId = namingStrategy.getCgmesId(refTyped(c), TERMINAL, ref(sequenceNumber));
                c.addAlias(terminalId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + sequenceNumber);
            }
        }
    }

    private static boolean isCondenser(Generator generator) {
        return obtainSynchronousMachineKind(generator, generator.getMinP(), generator.getMaxP(), CgmesExportUtil.obtainCurve(generator)).contains("condenser");
    }

    private void addIidmMappingsGenerators(Network network) {
        for (Generator generator : network.getGenerators()) {
            // Condensers should not have generating units
            if (!isCondenser(generator)) {
                String generatingUnit = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + GENERATING_UNIT);
                if (generatingUnit == null) {
                    generatingUnit = namingStrategy.getCgmesId(ref(generator), refGeneratingUnit(generator));
                    generator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + GENERATING_UNIT, generatingUnit);
                }
            }
            String regulatingControlId = generator.getProperty(Conversion.PROPERTY_REGULATING_CONTROL);
            if (regulatingControlId == null && hasRegulatingControlCapability(generator)) {
                regulatingControlId = namingStrategy.getCgmesId(ref(generator), Part.REGULATING_CONTROL);
                generator.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, regulatingControlId);
            }
        }
    }

    private static boolean hasRegulatingControlCapability(Generator generator) {
        return generator.getExtension(RemoteReactivePowerControl.class) != null
                || !Double.isNaN(generator.getTargetV()) && hasReactiveCapability(generator);
    }

    private static boolean hasReactiveCapability(Generator generator) {
        ReactiveLimits reactiveLimits = generator.getReactiveLimits();
        if (reactiveLimits == null) {
            return false;
        } else if (reactiveLimits.getKind() == ReactiveLimitsKind.CURVE) {
            return hasReactiveCapability((ReactiveCapabilityCurve) reactiveLimits);
        } else if (reactiveLimits.getKind() == ReactiveLimitsKind.MIN_MAX) {
            return hasReactiveCapability((MinMaxReactiveLimits) reactiveLimits);
        }
        return false;
    }

    private static boolean hasReactiveCapability(ReactiveCapabilityCurve rcc) {
        for (ReactiveCapabilityCurve.Point point : rcc.getPoints()) {
            if (point.getMaxQ() != point.getMinQ()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasReactiveCapability(MinMaxReactiveLimits mmrl) {
        return mmrl.getMaxQ() != mmrl.getMinQ();
    }

    private void addIidmMappingsBatteries(Network network) {
        for (Battery battery : network.getBatteries()) {
            String generatingUnit = battery.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + GENERATING_UNIT);
            if (generatingUnit == null) {
                generatingUnit = namingStrategy.getCgmesId(refTyped(battery), Part.GENERATING_UNIT);
                battery.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + GENERATING_UNIT, generatingUnit);
            }
            // TODO regulation
        }
    }

    private void addIidmMappingsShuntCompensators(Network network) {
        for (ShuntCompensator shuntCompensator : network.getShuntCompensators()) {
            if ("true".equals(shuntCompensator.getProperty(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT))) {
                continue;
            }
            String regulatingControlId = shuntCompensator.getProperty(Conversion.PROPERTY_REGULATING_CONTROL);
            if (regulatingControlId == null && (CgmesExportUtil.isValidVoltageSetpoint(shuntCompensator.getTargetV())
                                            || !Objects.equals(shuntCompensator, shuntCompensator.getRegulatingTerminal().getConnectable()))) {
                regulatingControlId = namingStrategy.getCgmesId(ref(shuntCompensator), Part.REGULATING_CONTROL);
                shuntCompensator.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, regulatingControlId);
            }
        }
    }

    private void addIidmMappingsStaticVarCompensators(Network network) {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            String regulatingControlId = svc.getProperty(Conversion.PROPERTY_REGULATING_CONTROL);
            boolean validVoltageSetpoint = CgmesExportUtil.isValidVoltageSetpoint(svc.getVoltageSetpoint());
            boolean validReactiveSetpoint = CgmesExportUtil.isValidReactivePowerSetpoint(svc.getReactivePowerSetpoint());
            if (regulatingControlId == null && (validReactiveSetpoint
                                                || validVoltageSetpoint
                                                || !Objects.equals(svc, svc.getRegulatingTerminal().getConnectable()))) {
                regulatingControlId = namingStrategy.getCgmesId(ref(svc), Part.REGULATING_CONTROL);
                svc.setProperty(Conversion.PROPERTY_REGULATING_CONTROL, regulatingControlId);
            }
        }
    }

    private void addIidmMappingsEndsAndTapChangers(Network network) {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            addIidmTransformerEnd(twt, 1);
            addIidmTransformerEnd(twt, 2);
            //  For two winding transformers we can not check-and-add based on endNumber
            //  The resulting IIDM tap changer is always at end1
            //  But the original position of tap changer could be 1 or 2
            addIidmTapChanger2wt(twt, twt.getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER);
            addIidmTapChanger2wt(twt, twt.getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER);
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            addIidmTransformerEnd(twt, 1);
            addIidmTransformerEnd(twt, 2);
            addIidmTransformerEnd(twt, 3);
            addIidmTapChanger(twt, twt.getLeg1().getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER, 1);
            addIidmTapChanger(twt, twt.getLeg1().getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER, 1);
            addIidmTapChanger(twt, twt.getLeg2().getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER, 2);
            addIidmTapChanger(twt, twt.getLeg2().getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER, 2);
            addIidmTapChanger(twt, twt.getLeg3().getPhaseTapChanger(), CgmesNames.PHASE_TAP_CHANGER, 3);
            addIidmTapChanger(twt, twt.getLeg3().getRatioTapChanger(), CgmesNames.RATIO_TAP_CHANGER, 3);
        }
    }

    private void addIidmTransformerEnd(Identifiable<?> eq, int end) {
        String endId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + end).orElse(null);
        if (endId == null) {
            endId = namingStrategy.getCgmesId(ref(eq), combo(TRANSFORMER_END, ref(end)));
            eq.addAlias(endId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + end);
        }
    }

    private void addIidmTapChanger(Identifiable<?> eq, TapChanger<?, ?, ?, ?> tc, String typeChangerTypeName, int endNumber) {
        if (tc != null) {
            String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + typeChangerTypeName + endNumber;
            if (eq.getAliasFromType(aliasType).isEmpty()) {
                Part ratioPhasePart = Objects.equals(typeChangerTypeName, CgmesNames.PHASE_TAP_CHANGER) ? PHASE_TAP_CHANGER : RATIO_TAP_CHANGER;
                String newTapChangerId = namingStrategy.getCgmesId(refTyped(eq), ratioPhasePart, ref(endNumber));
                eq.addAlias(newTapChangerId, aliasType);
            }
        }
    }

    private void addIidmTapChanger2wt(Identifiable<?> eq, TapChanger<?, ?, ?, ?> tc, String typeChangerTypeName) {
        if (tc != null) {
            String aliasType1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + typeChangerTypeName + 1;
            String aliasType2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + typeChangerTypeName + 2;
            // Only create a new identifier, always at end 1,
            // If no previous identifiers were found
            // Neither at end 1 nor at end 2
            if (eq.getAliasFromType(aliasType1).isEmpty() && eq.getAliasFromType(aliasType2).isEmpty()) {
                Part ratioPhasePart = Objects.equals(typeChangerTypeName, CgmesNames.PHASE_TAP_CHANGER) ? PHASE_TAP_CHANGER : RATIO_TAP_CHANGER;
                String newTapChangerId = namingStrategy.getCgmesId(refTyped(eq), ratioPhasePart, ref(1));
                eq.addAlias(newTapChangerId, aliasType1);
            }
        }
    }

    private void addIidmMappingsEquivalentInjection(Network network) {
        for (DanglingLine danglingLine : network.getDanglingLines(DanglingLineFilter.ALL)) {
            String alias;
            alias = danglingLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION);
            if (alias == null) {
                String equivalentInjectionId = namingStrategy.getCgmesId(refTyped(danglingLine), EQUIVALENT_INJECTION);
                danglingLine.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.EQUIVALENT_INJECTION, equivalentInjectionId);
            }
            alias = danglingLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
            if (alias == null) {
                String equivalentInjectionTerminalId = namingStrategy.getCgmesId(refTyped(danglingLine), EQUIVALENT_INJECTION, TERMINAL);
                danglingLine.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal", equivalentInjectionTerminalId);
            }
        }
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

    public BaseVoltageMapping.BaseVoltageSource getBaseVoltageByNominalVoltage(double nominalV) {
        return baseVoltageByNominalVoltageMapping.get(nominalV);
    }

    public Collection<String> getRegionsIds() {
        return Collections.unmodifiableSet(regionsIdsByRegionName.values());
    }

    public String getRegionName(String regionId) {
        return regionsIdsByRegionName.inverse().get(regionId);
    }

    public String getSubRegionName(String subRegionId) {
        return subRegionsIdsBySubRegionName.inverse().get(subRegionId);
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

