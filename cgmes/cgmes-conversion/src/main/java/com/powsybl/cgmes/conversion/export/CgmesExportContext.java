/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.fasterxml.uuid.Generators;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.naming.CgmesObjectReference;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.conversion.naming.NamingStrategyFactory;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Identifiable;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part.*;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refGeneratingUnit;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CgmesExportContext {

    private static final String REGULATING_CONTROL = "RegulatingControl";
    private static final String GENERATING_UNIT = "GeneratingUnit";

    private static final String DCNODE = "DCNode";
    private static final String DCTERMINAL = "DCTerminal";
    private static final String ACDCCONVERTERDCTERMINAL = "ACDCConverterDCTerminal";

    private static final String TERMINAL_BOUNDARY = "Terminal_Boundary";
    private static final String REGION_ID = "regionId";
    private static final String REGION_NAME = "regionName";
    private static final String DEFAULT_REGION = "default region";
    public static final String SUB_REGION_ID = "subRegionId";

    private CgmesNamespace.Cim cim = CgmesNamespace.CIM_16;
    private CgmesTopologyKind topologyKind = CgmesTopologyKind.BUS_BRANCH;
    private ZonedDateTime scenarioTime = ZonedDateTime.now();
    private ReportNode reportNode = ReportNode.NO_OP;
    private String boundaryEqId; // may be null
    private String boundaryTpId; // may be null
    private String businessProcess = DEFAULT_BUSINESS_PROCESS;

    private final ModelDescription eqModelDescription = new ModelDescription("EQ Model", cim.getProfileUri("EQ"));
    private final ModelDescription tpModelDescription = new ModelDescription("TP Model", cim.getProfileUri("TP"));
    private final ModelDescription svModelDescription = new ModelDescription("SV Model", cim.getProfileUri("SV"));
    private final ModelDescription sshModelDescription = new ModelDescription("SSH Model", cim.getProfileUri("SSH"));

    private NamingStrategy namingStrategy = new NamingStrategy.Identity();

    public static final boolean EXPORT_BOUNDARY_POWER_FLOWS_DEFAULT_VALUE = true;
    public static final boolean EXPORT_POWER_FLOWS_FOR_SWITCHES_DEFAULT_VALUE = true;
    public static final boolean EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1_DEFAULT_VALUE = false;
    public static final boolean ENCODE_IDS_DEFAULT_VALUE = true;
    public static final boolean EXPORT_LOAD_FLOW_STATUS_DEFAULT_VALUE = true;
    // From QoCDC 3.3.1 rules IGMConvergence, KirchhoffsFirstLaw, ... that refer to SV_INJECTION_LIMIT=0.1
    public static final double MAX_P_MISMATCH_CONVERGED_DEFAULT_VALUE = 0.1;
    public static final double MAX_Q_MISMATCH_CONVERGED_DEFAULT_VALUE = 0.1;
    public static final boolean EXPORT_SV_INJECTIONS_FOR_SLACKS_DEFAULT_VALUE = true;
    public static final String DEFAULT_MODELING_AUTHORITY_SET_VALUE = "powsybl.org";
    public static final UUID DEFAULT_UUID_NAMESPACE = Generators.nameBasedGenerator().generate(DEFAULT_MODELING_AUTHORITY_SET_VALUE);
    public static final String DEFAULT_BUSINESS_PROCESS = "1D";

    private boolean exportBoundaryPowerFlows = EXPORT_BOUNDARY_POWER_FLOWS_DEFAULT_VALUE;
    private boolean exportFlowsForSwitches = EXPORT_POWER_FLOWS_FOR_SWITCHES_DEFAULT_VALUE;
    private boolean exportTransformersWithHighestVoltageAtEnd1 = EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1_DEFAULT_VALUE;
    private boolean exportLoadFlowStatus = EXPORT_LOAD_FLOW_STATUS_DEFAULT_VALUE;
    private double maxPMismatchConverged = MAX_P_MISMATCH_CONVERGED_DEFAULT_VALUE;
    private double maxQMismatchConverged = MAX_Q_MISMATCH_CONVERGED_DEFAULT_VALUE;
    private boolean isExportSvInjectionsForSlacks = EXPORT_SV_INJECTIONS_FOR_SLACKS_DEFAULT_VALUE;
    private boolean exportEquipment = false;
    private boolean encodeIds = ENCODE_IDS_DEFAULT_VALUE;

    private final Map<Double, BaseVoltageMapping.BaseVoltageSource> baseVoltageByNominalVoltageMapping = new HashMap<>();

    private final BiMap<String, String> regionsIdsByRegionName = HashBiMap.create();
    private final BiMap<String, String> subRegionsIdsBySubRegionName = HashBiMap.create();
    private final Map<String, String> fictitiousContainers = new HashMap<>();
    private final Map<String, Bus> topologicalNodes = new HashMap<>();
    private final ReferenceDataProvider referenceDataProvider;

    // Update dependencies in a way that:
    // [EQ.dependentOn EQ_BD]
    // SV.dependentOn TP, SSH[, TP_BD]
    // TP.dependentOn EQ
    // SSH.dependentOn EQ
    public void updateDependencies() {
        Set<String> eqModelIds = getEqModelDescription().getIds();
        if (!eqModelIds.isEmpty()) {
            getTpModelDescription()
                    .clearDependencies()
                    .addDependencies(eqModelIds);
            getSshModelDescription()
                    .clearDependencies()
                    .addDependencies(eqModelIds);
            getSvModelDescription().clearDependencies();
            Set<String> tpModelIds = getTpModelDescription().getIds();
            if (!tpModelIds.isEmpty()) {
                getSvModelDescription().addDependencies(tpModelIds);
            }
            Set<String> sshModelIds = getSshModelDescription().getIds();
            if (!sshModelIds.isEmpty()) {
                getSvModelDescription().addDependencies(sshModelIds);
            }
            if (boundaryEqId != null) {
                getEqModelDescription().addDependency(boundaryEqId);
            }
            if (boundaryTpId != null) {
                getSvModelDescription().addDependency(boundaryTpId);
            }
        }
    }

    public String getFictitiousContainerFor(Identifiable<?> id) {
        return fictitiousContainers.get(id.getId());
    }

    public void setFictitiousContainerFor(Identifiable<?> id, String containerId) {
        fictitiousContainers.put(id.getId(), containerId);
    }

    public static final class ModelDescription {

        private String description;
        private int version = 1;
        private String supersedes;
        private final List<String> dependencies = new ArrayList<>();
        private String modelingAuthoritySet = DEFAULT_MODELING_AUTHORITY_SET_VALUE;
        private final Set<String> ids = new HashSet<>();

        // TODO Each model may have a list of profiles, not only one
        private String profile;

        private ModelDescription(String description, String profile) {
            this.description = description;
            this.profile = profile;
        }

        public String getDescription() {
            return description;
        }

        public String getSupersedes() {
            return supersedes;
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

        public ModelDescription addDependencies(Set<String> dependencies) {
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

        public ModelDescription setProfile(String profile) {
            this.profile = profile;
            return this;
        }

        public void setIds(List<String> ids) {
            this.ids.clear();
            this.ids.addAll(Objects.requireNonNull(ids));
        }

        public void addId(String id) {
            this.ids.add(Objects.requireNonNull(id));
        }

        public void setIds(String... ids) {
            setIds(Arrays.asList(Objects.requireNonNull(ids)));
        }

        public Set<String> getIds() {
            return Collections.unmodifiableSet(ids);
        }

        public void setSupersedes(String id) {
            this.supersedes = id;
        }

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
        this.referenceDataProvider = referenceDataProvider;
        this.namingStrategy = namingStrategy;
        CimCharacteristics cimCharacteristics = network.getExtension(CimCharacteristics.class);
        if (cimCharacteristics != null) {
            setCimVersion(cimCharacteristics.getCimVersion());
            topologyKind = cimCharacteristics.getTopologyKind();
        } else {
            topologyKind = networkTopologyKind(network);
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
            sshModelDescription.setSupersedes(sshMetadata.getId());
            sshModelDescription.addDependencies(sshMetadata.getDependencies());
            sshModelDescription.setModelingAuthoritySet(sshMetadata.getModelingAuthoritySet());
        }
        addIidmMappings(network);
    }

    private CgmesTopologyKind networkTopologyKind(Network network) {
        for (VoltageLevel vl : network.getVoltageLevels()) {
            if (vl.getTopologyKind().equals(TopologyKind.NODE_BREAKER)) {
                return CgmesTopologyKind.NODE_BREAKER;
            }
        }
        return CgmesTopologyKind.BUS_BRANCH;
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
        addIidmMappingsControlArea(network);
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
        // We ignore fictitious loads used to model CGMES SvInjection objects that represent calculation mismatches
        // We also ignore fictitious switches used to model CGMES disconnected Terminals
        boolean ignored = c.isFictitious() &&
                (c instanceof Load
                        || c instanceof Switch && "true".equals(c.getProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL)));
        return !ignored;
    }

    public CgmesExportContext setBoundaryEqId(String boundaryEqId) {
        this.boundaryEqId = boundaryEqId;
        return this;
    }

    public CgmesExportContext setBoundaryTpId(String boundaryTpId) {
        this.boundaryTpId = boundaryTpId;
        return this;
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
            String dcNode1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCNODE + "1").orElse(null);
            if (dcNode1 == null) {
                dcNode1 = namingStrategy.getCgmesId(refTyped(line), Part.DCNODE, ref(1));
                line.addAlias(dcNode1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCNODE + "1");
            }
            String dcNode2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCNODE + "2").orElse(null);
            if (dcNode2 == null) {
                dcNode2 = namingStrategy.getCgmesId(refTyped(line), Part.DCNODE, ref(2));
                line.addAlias(dcNode2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCNODE + "2");
            }
            String dcTerminal1 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCTERMINAL + "1").orElse(null);
            if (dcTerminal1 == null) {
                dcTerminal1 = namingStrategy.getCgmesId(refTyped(line), TERMINAL, ref(1));
                line.addAlias(dcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCTERMINAL + "1");
            }
            String dcTerminal2 = line.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCTERMINAL + "2").orElse(null);
            if (dcTerminal2 == null) {
                dcTerminal2 = namingStrategy.getCgmesId(refTyped(line), TERMINAL, ref(2));
                line.addAlias(dcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + DCTERMINAL + "2");
            }
            String acdcConverterDcTerminal1 = line.getConverterStation1().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL).orElse(null);
            if (acdcConverterDcTerminal1 == null) {
                acdcConverterDcTerminal1 = namingStrategy.getCgmesId(refTyped(line), ACDC_CONVERTER_DC_TERMINAL, ref(1));
                line.getConverterStation1().addAlias(acdcConverterDcTerminal1, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL);
            }
            String acdcConverterDcTerminal2 = line.getConverterStation2().getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL).orElse(null);
            if (acdcConverterDcTerminal2 == null) {
                acdcConverterDcTerminal2 = namingStrategy.getCgmesId(refTyped(line), ACDC_CONVERTER_DC_TERMINAL, ref(2));
                line.getConverterStation2().addAlias(acdcConverterDcTerminal2, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + ACDCCONVERTERDCTERMINAL);
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
        // TODO(Luma) This has to be revisited with the pull request for preserving detailed info for generators (#2726)
        return generator.getMinP() == 0 && generator.getMaxP() == 0;
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
            String regulatingControlId = generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL);
            if (regulatingControlId == null && hasVoltageControlCapability(generator)) {
                regulatingControlId = namingStrategy.getCgmesId(ref(generator), Part.REGULATING_CONTROL);
                generator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL, regulatingControlId);
            }
        }
    }

    private static boolean hasVoltageControlCapability(Generator generator) {
        if (Double.isNaN(generator.getTargetV()) || generator.getReactiveLimits() == null) {
            return false;
        }

        ReactiveLimits reactiveLimits = generator.getReactiveLimits();
        if (reactiveLimits.getKind() == ReactiveLimitsKind.CURVE) {
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
            String regulatingControlId = shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL);
            if (regulatingControlId == null && (shuntCompensator.isVoltageRegulatorOn() || !Objects.equals(shuntCompensator, shuntCompensator.getRegulatingTerminal().getConnectable()))) {
                regulatingControlId = namingStrategy.getCgmesId(ref(shuntCompensator), Part.REGULATING_CONTROL);
                shuntCompensator.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL, regulatingControlId);
            }
        }
    }

    private void addIidmMappingsStaticVarCompensators(Network network) {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            String regulatingControlId = svc.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL);
            if (regulatingControlId == null && (StaticVarCompensator.RegulationMode.VOLTAGE.equals(svc.getRegulationMode()) || !Objects.equals(svc, svc.getRegulatingTerminal().getConnectable()))) {
                regulatingControlId = namingStrategy.getCgmesId(ref(svc), Part.REGULATING_CONTROL);
                svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL, regulatingControlId);
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

    private void addIidmMappingsControlArea(Network network) {
        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        if (cgmesControlAreas == null) {
            network.newExtension(CgmesControlAreasAdder.class).add();
            cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
            String cgmesControlAreaId = namingStrategy.getCgmesId(refTyped(network), CONTROL_AREA);
            cgmesControlAreas.newCgmesControlArea()
                    .setId(cgmesControlAreaId)
                    .setName("Network")
                    .setEnergyIdentificationCodeEic("Network--1")
                    .add();
            CgmesControlArea cgmesControlArea = cgmesControlAreas.getCgmesControlArea(cgmesControlAreaId);
            for (DanglingLine danglingLine : CgmesExportUtil.getBoundaryDanglingLines(network)) {
                cgmesControlArea.add(danglingLine.getTerminal());
            }
        }
    }

    public int getCimVersion() {
        return cim.getVersion();
    }

    public CgmesExportContext setCimVersion(int cimVersion) {
        cim = CgmesNamespace.getCim(cimVersion);
        if (cim.hasProfiles()) {
            eqModelDescription.setProfile(cim.getProfileUri("EQ"));
            tpModelDescription.setProfile(cim.getProfileUri("TP"));
            svModelDescription.setProfile(cim.getProfileUri("SV"));
            sshModelDescription.setProfile(cim.getProfileUri("SSH"));
        }
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

    public boolean writeConnectivityNodes() {
        boolean writeConnectivityNodes = cim.writeConnectivityNodes();
        if (!writeConnectivityNodes) {
            return topologyKind == CgmesTopologyKind.NODE_BREAKER;
        }
        return true;
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
}

