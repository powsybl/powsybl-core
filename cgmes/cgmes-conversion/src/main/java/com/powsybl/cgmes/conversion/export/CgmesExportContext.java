/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.NamingStrategy;
import com.powsybl.cgmes.conversion.NamingStrategyFactory;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
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
    private DateTime scenarioTime = DateTime.now();
    private Reporter reporter = Reporter.NO_OP;
    private String boundaryEqId; // may be null
    private String boundaryTpId; // may be null

    private final ModelDescription eqModelDescription = new ModelDescription("EQ Model", cim.getProfileUri("EQ"));
    private final ModelDescription tpModelDescription = new ModelDescription("TP Model", cim.getProfileUri("TP"));
    private final ModelDescription svModelDescription = new ModelDescription("SV Model", cim.getProfileUri("SV"));
    private final ModelDescription sshModelDescription = new ModelDescription("SSH Model", cim.getProfileUri("SSH"));

    private NamingStrategy namingStrategy = new NamingStrategy.Identity();

    private boolean exportBoundaryPowerFlows = true;
    private boolean exportFlowsForSwitches = false;
    private boolean exportEquipment = false;
    private boolean encodeIds = true;

    private final Map<Double, BaseVoltageMapping.BaseVoltageSource> baseVoltageByNominalVoltageMapping = new HashMap<>();

    private final BiMap<String, String> regionsIdsByRegionName = HashBiMap.create();
    private final BiMap<String, String> subRegionsIdsBySubRegionName = HashBiMap.create();
    private final Map<String, String> fictitiousContainers = new HashMap<>();

    // Update dependencies in a way that:
    // [EQ.dependentOn EQ_BD]
    // SV.dependentOn TP, SSH
    // TP.dependentOn EQ[, EQ_BD][, TP_BD]
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
                getSvModelDescription().addDependencies(sshModelIds);
            }
            if (boundaryEqId != null) {
                getEqModelDescription().addDependency(boundaryEqId);
                getTpModelDescription().addDependency(boundaryEqId);
            }
            if (boundaryTpId != null) {
                getTpModelDescription().addDependency(boundaryTpId);
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
        private final List<String> dependencies = new ArrayList<>();
        private String modelingAuthoritySet = "powsybl.org";
        private Set<String> ids = new HashSet<>();

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
    }

    public CgmesExportContext() {
    }

    public CgmesExportContext(Network network) {
        this(network, NamingStrategyFactory.create(NamingStrategyFactory.IDENTITY));
    }

    public CgmesExportContext(Network network, NamingStrategy namingStrategy) {
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
                regionName = substation.getCountry().map(Country::name).orElse(DEFAULT_REGION);
                String regionId = regionsIdsByRegionName.computeIfAbsent(regionName, k -> CgmesExportUtil.getUniqueId());
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
                String id = subRegionsIdsBySubRegionName.computeIfAbsent(geoTag, k -> CgmesExportUtil.getUniqueId());
                substation.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + SUB_REGION_ID, id);
            } else {
                subRegionsIdsBySubRegionName.computeIfAbsent(geoTag, k -> namingStrategy.getCgmesIdFromProperty(substation, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + SUB_REGION_ID));
            }
        }
    }

    private void addIidmMappingsBaseVoltages(BaseVoltageMapping mapping, Network network) {
        if (mapping.isBaseVoltageEmpty()) {
            for (VoltageLevel vl : network.getVoltageLevels()) {
                double nominalV = vl.getNominalV();
                String baseVoltageId = CgmesExportUtil.getUniqueId();
                mapping.addBaseVoltage(nominalV, baseVoltageId, Source.IGM);
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
            String terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL).orElse(null);
            if (terminalId == null) {
                terminalId = CgmesExportUtil.getUniqueId();
                c.addAlias(terminalId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL);
            }
            String boundaryId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_BOUNDARY).orElse(null);
            if (boundaryId == null) {
                boundaryId = CgmesExportUtil.getUniqueId();
                c.addAlias(boundaryId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + TERMINAL_BOUNDARY);
            }
        } else {
            int sequenceNumber = CgmesExportUtil.getTerminalSequenceNumber(t);
            String terminalId = c.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + sequenceNumber).orElse(null);
            if (terminalId == null) {
                if (c instanceof TieLine && c.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_" + sequenceNumber)) { // TODO fix when merging is handled properly
                    terminalId = c.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + "_" + sequenceNumber);
                    c.removeAlias(terminalId);
                } else {
                    terminalId = CgmesExportUtil.getUniqueId();
                }
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

    private static void addIidmMappingsStaticVarCompensators(Network network) {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            String regulatingControlId = svc.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL);
            if (regulatingControlId == null && (StaticVarCompensator.RegulationMode.VOLTAGE.equals(svc.getRegulationMode()) || !Objects.equals(svc, svc.getRegulatingTerminal().getConnectable()))) {
                regulatingControlId = CgmesExportUtil.getUniqueId();
                svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL, regulatingControlId);
            }
        }
    }

    private static void addIidmMappingsEndsAndTapChangers(Network network) {
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

    private static void addIidmTransformerEnd(Identifiable<?> eq, int end) {
        String endId = eq.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + end).orElse(null);
        if (endId == null) {
            endId = CgmesExportUtil.getUniqueId();
            eq.addAlias(endId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TRANSFORMER_END + end);
        }
    }

    private static void addIidmTapChanger(Identifiable<?> eq, TapChanger<?, ?> tc, String typeChangerTypeName, int endNumber) {
        if (tc != null) {
            String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + typeChangerTypeName + endNumber;
            if (eq.getAliasFromType(aliasType).isEmpty()) {
                String newTapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(newTapChangerId, aliasType);
            }
        }
    }

    private static void addIidmTapChanger2wt(Identifiable<?> eq, TapChanger<?, ?> tc, String typeChangerTypeName) {
        if (tc != null) {
            String aliasType1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + typeChangerTypeName + 1;
            String aliasType2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + typeChangerTypeName + 2;
            // Only create a new identifier, always at end 1,
            // If no previous identifiers were found
            // Neither at end 1 nor at end 2
            if (eq.getAliasFromType(aliasType1).isEmpty() && eq.getAliasFromType(aliasType2).isEmpty()) {
                String newTapChangerId = CgmesExportUtil.getUniqueId();
                eq.addAlias(newTapChangerId, aliasType1);
            }
        }
    }

    private static void addIidmMappingsEquivalentInjection(Network network) {
        for (DanglingLine danglingLine : network.getDanglingLines()) {
            String alias;
            alias = danglingLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection");
            if (alias == null) {
                String equivalentInjectionId = CgmesExportUtil.getUniqueId();
                danglingLine.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection", equivalentInjectionId);
            }
            alias = danglingLine.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
            if (alias == null) {
                String equivalentInjectionTerminalId = CgmesExportUtil.getUniqueId();
                danglingLine.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal", equivalentInjectionTerminalId);
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

    public CgmesExportContext setReporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }

    public Reporter getReporter() {
        return this.reporter;
    }
}

