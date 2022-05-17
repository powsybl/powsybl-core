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
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

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

    private static final String TERMINAL_NETWORK = "Terminal_Network";
    private static final String TERMINAL_BOUNDARY = "Terminal_Boundary";

    private CgmesNamespace.Cim cim = CgmesNamespace.CIM_16;
    private CgmesTopologyKind topologyKind = CgmesTopologyKind.BUS_BRANCH;
    private DateTime scenarioTime = DateTime.now();

    private final ModelDescription eqModelDescription = new ModelDescription("EQ Model", cim.getProfile("EQ"));
    private final ModelDescription tpModelDescription = new ModelDescription("TP Model", cim.getProfile("TP"));
    private final ModelDescription svModelDescription = new ModelDescription("SV Model", cim.getProfile("SV"));
    private final ModelDescription sshModelDescription = new ModelDescription("SSH Model", cim.getProfile("SSH"));

    private boolean exportBoundaryPowerFlows = true;
    private boolean exportFlowsForSwitches = false;

    private final Map<Double, BaseVoltageMapping.BaseVoltageSource> baseVoltageByNominalVoltageMapping = new HashMap<>();

    private final BiMap<String, String> regionsIdsByRegionName = HashBiMap.create();
    private final BiMap<String, String> subRegionsIdsBySubRegionName = HashBiMap.create();

    // Update dependencies in a way that:
    // SV.dependentOn TP
    // SV.dependentOn SSH
    // TP.dependentOn EQ
    // SSH.dependentOn EQ
    public void updateDependencies() {
        String eqModelId = getEqModelDescription().getId();
        if (eqModelId != null) {
            getTpModelDescription()
                    .clearDependencies()
                    .addDependency(eqModelId);
            getSshModelDescription()
                    .clearDependencies()
                    .addDependency(eqModelId);
            getSvModelDescription().clearDependencies();
            String tpModelId = getTpModelDescription().getId();
            if (tpModelId != null) {
                getSvModelDescription().addDependency(tpModelId);
            }
            String sshModelId = getSshModelDescription().getId();
            if (sshModelId != null) {
                getSvModelDescription().addDependency(sshModelId);
                getSvModelDescription().addDependency(sshModelId);
            }
        }
    }

    public static final class ModelDescription {

        private String description;
        private int version = 1;
        private final List<String> dependencies = new ArrayList<>();
        private String modelingAuthoritySet = "powsybl.org";
        private String id = null;

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

        public ModelDescription setProfile(String profile) {
            this.profile = profile;
            return this;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public CgmesExportContext() {
    }

    public CgmesExportContext(Network network) {
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
            String country = substation.getCountry().map(Country::name).orElseGet(network::getNameOrId);
            if (!substation.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionId")) {
                String id = regionsIdsByRegionName.computeIfAbsent(country, k -> CgmesExportUtil.getUniqueId());
                substation.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionId", id);
            } else {
                regionsIdsByRegionName.computeIfAbsent(country, k -> substation.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "regionId"));
            }
            String geoTag;
            if (substation.getGeographicalTags().size() == 1) {
                geoTag = substation.getGeographicalTags().iterator().next();
            } else {
                geoTag = country;
            }
            if (!substation.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "subRegionId")) {
                String id = subRegionsIdsBySubRegionName.computeIfAbsent(geoTag, k -> CgmesExportUtil.getUniqueId());
                substation.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "subRegionId", id);
            } else {
                subRegionsIdsBySubRegionName.computeIfAbsent(geoTag, k -> substation.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "subRegionId"));
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
        } else if (c instanceof Load && c.isFictitious()) {
            // A fictitious load do not need an alias
        } else {
            int sequenceNumber = CgmesExportUtil.getTerminalSequenceNumber(t);
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

    private static void addIidmMappingsStaticVarCompensators(Network network) {
        for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
            String regulatingControlId = svc.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + REGULATING_CONTROL);
            if (regulatingControlId == null && (StaticVarCompensator.RegulationMode.VOLTAGE.equals(svc.getRegulationMode()) || !Objects.equals(svc, svc.getRegulatingTerminal().getConnectable()))) {
                regulatingControlId = CgmesExportUtil.getUniqueId();
                svc.setProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl", regulatingControlId);
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
            Optional<String> alias;
            alias = danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection");
            if (!alias.isPresent()) {
                String equivalentInjectionId = CgmesExportUtil.getUniqueId();
                danglingLine.addAlias(equivalentInjectionId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection");
            }
            alias = danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
            if (!alias.isPresent()) {
                String equivalentInjectionTerminalId = CgmesExportUtil.getUniqueId();
                danglingLine.addAlias(equivalentInjectionTerminalId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
            }
            alias = danglingLine.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TOPOLOGICAL_NODE);
            if (!alias.isPresent()) {
                String topologicalNode = CgmesExportUtil.getUniqueId();
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

    public int getCimVersion() {
        return cim.getVersion();
    }

    public CgmesExportContext setCimVersion(int cimVersion) {
        cim = CgmesNamespace.getCim(cimVersion);
        if (cim.hasProfiles()) {
            eqModelDescription.setProfile(cim.getProfile("EQ"));
            tpModelDescription.setProfile(cim.getProfile("TP"));
            svModelDescription.setProfile(cim.getProfile("SV"));
            sshModelDescription.setProfile(cim.getProfile("SSH"));
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

    public CgmesNamespace.Cim getCim() {
        return cim;
    }

    public BaseVoltageMapping.BaseVoltageSource getBaseVoltageByNominalVoltage(double nominalV) {
        return baseVoltageByNominalVoltageMapping.get(nominalV);
    }

    public boolean isWriteConnectivityNodes() {
        boolean isWriteConnectivityNodes = cim.isWriteConnectivityNodes();
        if (!isWriteConnectivityNodes) {
            isWriteConnectivityNodes = topologyKind.equals(CgmesTopologyKind.NODE_BREAKER);
        }
        return isWriteConnectivityNodes;
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

}
