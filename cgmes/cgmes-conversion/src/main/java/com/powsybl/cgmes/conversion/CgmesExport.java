/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.conversion.export.*;
import com.powsybl.cgmes.conversion.naming.NamingStrategy;
import com.powsybl.cgmes.conversion.naming.NamingStrategyFactory;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesMetadataModelImpl;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.CgmesReports.inconsistentProfilesTPRequiredReport;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {

    private final ParameterDefaultValueConfig defaultValueConfig;
    private final CgmesImport importer;

    public CgmesExport(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
        // We may need to import the boundaries to be able to export proper references
        importer = new CgmesImport(platformConfig);
    }

    public CgmesExport() {
        this(PlatformConfig.defaultConfig());
    }

    @Override
    public List<Parameter> getParameters() {
        return STATIC_PARAMETERS;
    }

    /**
     * Export the requested network to the CGMES format.
     * @param network The network to export.
     * @param parameters Optional parameters that influence the export.
     * @param dataSource The dataSource used by the export.
     * @param reportNode The reportNode used for functional logs.
     */
    @Override
    public void export(Network network, Properties parameters, DataSource dataSource, ReportNode reportNode) {
        Objects.requireNonNull(network);

        // Determine reference data (boundaries, base voltages and other sourcing references) for the export
        String sourcingActorName = Parameter.readString(getFormat(), parameters, SOURCING_ACTOR_PARAMETER, defaultValueConfig);
        String countryName = getCountry(network);
        ReferenceDataProvider referenceDataProvider = new ReferenceDataProvider(sourcingActorName, countryName, importer, parameters);

        // Create the context (the object that stores relevant data for the export)
        String namingStrategyImpl = Parameter.readString(getFormat(), parameters, NAMING_STRATEGY_PARAMETER, defaultValueConfig);
        UUID uuidNamespace = UUID.fromString(Parameter.readString(getFormat(), parameters, UUID_NAMESPACE_PARAMETER, defaultValueConfig));
        NamingStrategy namingStrategy = NamingStrategyFactory.create(namingStrategyImpl, uuidNamespace);
        CgmesExportContext context = new CgmesExportContext(network, referenceDataProvider, namingStrategy);
        addParametersToContext(context, parameters, reportNode, referenceDataProvider);

        // Export the network
        if (Parameter.readBoolean(getFormat(), parameters, CGM_EXPORT_PARAMETER, defaultValueConfig)) {
            exportCGM(network, dataSource, context);
        } else {
            exportIGM(network, dataSource, context);
        }
    }

    /**
     * Common Grid Model export.
     * This consists in providing an updated SSH for the IGMs (subnetworks) and an updated SV for the CGM (network).
     * @param network The network to export. This is the parent network that contains the subnetworks.
     * @param dataSource The dataSource used by the export.
     * @param context The context that stores relevant data for the export.
     */
    private void exportCGM(Network network, DataSource dataSource, CgmesExportContext context) {
        checkCgmConsistency(network, context);

        // Initialize models for export. The original IGM EQ, SSH, TP and TP_BD don't get exported,
        // but we need to init their models to retrieve their IDs when building the dependencies.
        Map<Network, IgmModelsForCgm> igmModels = new HashMap<>();
        for (Network subnetwork : network.getSubnetworks()) {
            IgmModelsForCgm igmModelsForCgm = new IgmModelsForCgm(
                    initializeModelForExport(subnetwork, CgmesSubset.STEADY_STATE_HYPOTHESIS, context, false, true),
                    initializeModelForExport(subnetwork, CgmesSubset.EQUIPMENT, context, false, false),
                    initializeModelForExport(subnetwork, CgmesSubset.STEADY_STATE_HYPOTHESIS, context, false, false),
                    initializeModelForExport(subnetwork, CgmesSubset.TOPOLOGY, context, false, false),
                    initializeModelForExport(subnetwork, CgmesSubset.TOPOLOGY_BOUNDARY, context, false, false)
            );
            igmModels.put(subnetwork, igmModelsForCgm);
        }
        CgmesMetadataModel updatedCgmSvModel = initializeModelForExport(network, CgmesSubset.STATE_VARIABLES, context, true, true);

        // Update dependencies
        if (context.updateDependencies()) {
            updateDependenciesCGM(igmModels.values(), updatedCgmSvModel, context.getBoundaryTpId());
        }

        // Export the SSH for the IGMs and the SV for the CGM
        String baseName = getBaseName(context, dataSource, network);
        for (Network subnetwork : network.getSubnetworks()) {
            context.addIidmMappings(subnetwork);

            String country = getCountry(subnetwork);
            String igmName = country != null ? country : subnetwork.getNameOrId();
            String igmSshFileName = baseName + "_" + igmName + "_" + CgmesSubset.STEADY_STATE_HYPOTHESIS.getIdentifier() + ".xml";
            subsetExport(subnetwork, CgmesSubset.STEADY_STATE_HYPOTHESIS, igmSshFileName, dataSource, context, igmModels.get(subnetwork).updatedSsh);
        }
        String cgmSvFileName = baseName + "_" + CgmesSubset.STATE_VARIABLES.getIdentifier() + ".xml";
        subsetExport(network, CgmesSubset.STATE_VARIABLES, cgmSvFileName, dataSource, context, updatedCgmSvModel);
    }

    /**
     * Individual Grid Model export.
     * This consists in providing the requested subsets among EQ, TP, SSH, SV.
     * @param network The network to export.
     * @param dataSource The dataSource used by the export.
     * @param context The context that stores relevant data for the export.
     */
    private void exportIGM(Network network, DataSource dataSource, CgmesExportContext context) {
        List<CgmesSubset> requestedSubsets = Arrays.stream(CgmesSubset.values()).filter(s -> context.getProfiles().contains(s.getIdentifier())).toList();
        checkIgmConsistency(requestedSubsets, network, context);

        // Init all exportable subsets (even the ones that don't get exported)
        // in order to retrieve their IDs when building the dependencies
        Map<CgmesSubset, CgmesMetadataModel> subsetModels = new EnumMap<>(CgmesSubset.class);
        for (CgmesSubset exportableSubset : List.of(
                CgmesSubset.EQUIPMENT, CgmesSubset.TOPOLOGY, CgmesSubset.STEADY_STATE_HYPOTHESIS, CgmesSubset.STATE_VARIABLES)) {
            CgmesMetadataModel subsetModel = initializeModelForExport(network, exportableSubset, context, true, false);
            subsetModels.put(exportableSubset, subsetModel);
        }

        // Update dependencies
        if (context.updateDependencies()) {
            updateDependenciesIGM(subsetModels, context.getBoundaryEqId(), context.getBoundaryTpId());
        }

        // Export requested subsets
        context.setExportEquipment(requestedSubsets.contains(CgmesSubset.EQUIPMENT));
        String baseName = getBaseName(context, dataSource, network);
        for (CgmesSubset subset : requestedSubsets) {
            String fileName = baseName + "_" + subset.getIdentifier() + ".xml";
            subsetExport(network, subset, fileName, dataSource, context, subsetModels.get(subset));
        }

        context.getNamingStrategy().debug(baseName, dataSource);
    }

    /**
     * Initialize the model (= the metadata information) that is used by the export.
     * If existing, the network model extension is used for the initialization.
     * If existing, optional parameters are also used for the initialization.
     * If both are present, the optional parameters prevail the values in the network extension.
     * @param network The network in which to look for an existing model extension as basis for initialization.
     * @param subset The subset of the model to initialize.
     * @param context The context used by the export.
     * @param mainNetwork A boolean indicating whether the exported network is the main network (not a subnetwork).
     * @param modelUpdate A boolean indicating whether the model version and ID should be updated.
     * @return A model with all necessary metadata information for the export.
     */
    public static CgmesMetadataModel initializeModelForExport(
            Network network, CgmesSubset subset, CgmesExportContext context, boolean mainNetwork, boolean modelUpdate) {
        // Initialize a new model for the export
        CgmesMetadataModel modelForExport = new CgmesMetadataModelImpl(subset, CgmesExportContext.DEFAULT_MODELING_AUTHORITY_SET_VALUE);
        modelForExport.setProfile(context.getCim().getProfileUri(subset.getIdentifier()));

        // If a model extension exists, use it as basis for the export
        CgmesMetadataModels networkModels = network.getExtension(CgmesMetadataModels.class);
        Optional<CgmesMetadataModel> networkSubsetModel = networkModels != null ?
                networkModels.getModelForSubset(subset) :
                Optional.empty();
        networkSubsetModel.ifPresent(m -> modelForExport.setId(m.getId()));
        networkSubsetModel.ifPresent(m -> modelForExport.setDescription(m.getDescription()));
        networkSubsetModel.ifPresent(m -> modelForExport.setVersion(m.getVersion()));
        networkSubsetModel.ifPresent(m -> modelForExport.addDependentOn(m.getDependentOn()));
        networkSubsetModel.ifPresent(m -> modelForExport.addSupersedes(m.getSupersedes()));
        networkSubsetModel.ifPresent(m -> modelForExport.addProfiles(m.getProfiles()));
        networkSubsetModel.ifPresent(m -> modelForExport.setModelingAuthoritySet(m.getModelingAuthoritySet()));

        // Use parameters if they have been defined.
        // It doesn't apply to subnetworks, except for the version of updated SSH of a CGM export
        if ((mainNetwork || modelUpdate) && context.getModelDescription() != null) {
            modelForExport.setDescription(context.getModelDescription());
        }
        if ((mainNetwork || modelUpdate) && context.getModelVersion() != null) {
            modelForExport.setVersion(Integer.parseInt(context.getModelVersion()));
        }
        if (mainNetwork && context.getModelingAuthoritySet() != null) {
            modelForExport.setModelingAuthoritySet(context.getModelingAuthoritySet());
        }

        // Initialize the model id in the case of an updated SSH/SV of a CGM export, or if it isn't present
        if (modelUpdate || modelForExport.getId() == null) {
            CgmesExportUtil.initializeModelId(network, modelForExport, context);
        }

        return modelForExport;
    }

    /**
     * Update cross dependencies between the subset models (including boundaries) through the dependentOn relationship.
     * The IGMs updated SSH supersede the original ones and depend on the original EQ. Other dependencies are kept.
     * The CGM updated SV depends on the IGMs updated SSH and on the IGMs original TP and TP_BD.
     * @param igmModels For each IGM: the updated SSH model and the original SSH, TP and TP_BD models.
     * @param updatedCgmSvModel The SV model for the CGM.
     * @param boundaryTpId The model id for the TP_BD subset.
     */
    private void updateDependenciesCGM(Collection<IgmModelsForCgm> igmModels, CgmesMetadataModel updatedCgmSvModel, String boundaryTpId) {
        // Each updated SSH model depends on the original EQ model
        igmModels.forEach(m -> m.updatedSsh.addDependentOn(m.originalEq.getId()));

        // Each updated SSH model supersedes the original one
        igmModels.forEach(m -> m.updatedSsh.clearSupersedes());
        igmModels.forEach(m -> m.updatedSsh.addSupersedes(m.originalSsh.getId()));

        // Updated SV model depends on updated SSH models and original TP and TP_BD models
        updatedCgmSvModel.addDependentOn(igmModels.stream().map(m -> m.updatedSsh.getId()).collect(Collectors.toSet()));
        updatedCgmSvModel.addDependentOn(igmModels.stream().map(m -> m.originalTp.getId()).collect(Collectors.toSet()));
        if (boundaryTpId != null) {
            updatedCgmSvModel.addDependentOn(boundaryTpId);
        } else {
            updatedCgmSvModel.addDependentOn(igmModels.stream().map(m -> m.originalTpBd.getId()).collect(Collectors.toSet()));
        }
    }

    /**
     * Update cross dependencies between the subset models (including boundaries) through the dependentOn relationship.
     * @param subsetModels The models for the following subsets: EQ, TP, SSH, SV.
     * @param boundaryEqId The model id for the EQ_BD subset.
     * @param boundaryTpId The model id for the TP_BD subset.
     */
    private void updateDependenciesIGM(Map<CgmesSubset, CgmesMetadataModel> subsetModels, String boundaryEqId, String boundaryTpId) {
        // Retrieve EQ model ID
        String eqModelId = subsetModels.get(CgmesSubset.EQUIPMENT).getId();
        if (eqModelId == null || eqModelId.isEmpty()) {
            return;
        }

        // TP and SSH depend on EQ
        subsetModels.get(CgmesSubset.TOPOLOGY).addDependentOn(eqModelId);
        subsetModels.get(CgmesSubset.STEADY_STATE_HYPOTHESIS).addDependentOn(eqModelId);

        // SV depends on TP and SSH
        subsetModels.get(CgmesSubset.STATE_VARIABLES)
                .addDependentOn(subsetModels.get(CgmesSubset.TOPOLOGY).getId())
                .addDependentOn(subsetModels.get(CgmesSubset.STEADY_STATE_HYPOTHESIS).getId());

        // EQ depends on EQ_BD (if present)
        if (boundaryEqId != null) {
            subsetModels.get(CgmesSubset.EQUIPMENT).addDependentOn(boundaryEqId);
        }

        // SV depends on TP_BD (if present)
        if (boundaryTpId != null) {
            subsetModels.get(CgmesSubset.STATE_VARIABLES).addDependentOn(boundaryTpId);
        }
    }

    /**
     * Check that the given network is consistent with a CGM export.
     * @param network The network to export as a CGM.
     * @param context The context used by the export.
     */
    private void checkCgmConsistency(Network network, CgmesExportContext context) {
        // Check that the network has subnetworks
        if (network.getSubnetworks().size() < 2) {
            LOG.error("Network {} must have at least 2 subnetworks for a CGM export.", network.getId());
        }

        // QoCDC, rule CgmSvSshVersionMismatch: SSHs and SV must have same scenario time
        ZonedDateTime scenarioTime = network.getCaseDate();
        for (Network subnetwork : network.getSubnetworks()) {
            if (!subnetwork.getCaseDate().equals(scenarioTime)) {
                LOG.error("Parent network doesn't have the same scenarioTime as subnetwork: {}.", subnetwork.getId());
            }
        }

        // QoCDC, rule CgmSvSshVersionMismatch: SSHs and SV must have same version
        // If version has been provided as a parameter, use it, otherwise take the max version of models and increment
        if (context.getModelVersion() == null || context.getModelVersion().isEmpty()) {
            // If there is no version number found in the input files getVersionNumber will return -1
            int currentVersion = getVersionNumber(network, CgmesSubset.STATE_VARIABLES);
            for (Network subnetwork : network.getSubnetworks()) {
                currentVersion = Math.max(getVersionNumber(subnetwork, CgmesSubset.STEADY_STATE_HYPOTHESIS), currentVersion);
            }
            // If no version number has been read, it is assumed everything was at version "1", so next version is "2"
            // Although from QoCDC it is not clear if first version of IGMs should be "0" or "1"
            int nextVersion = currentVersion >= 0 ? currentVersion + 1 : 2;
            context.setModelVersion(String.valueOf(nextVersion));
        }
    }

    /**
     * Check that the given network is consistent with an IGM export for the requested subsets.
     * @param requestedSubsets The subsets to export for that network.
     * @param network The network to export as an IGM.
     * @param context The context used by the export.
     */
    private void checkIgmConsistency(List<CgmesSubset> requestedSubsets, Network network, CgmesExportContext context) {
        boolean networkIsNodeBreaker = network.getVoltageLevelStream()
                .map(VoltageLevel::getTopologyKind)
                .anyMatch(tk -> tk == TopologyKind.NODE_BREAKER);
        if (networkIsNodeBreaker
                && (requestedSubsets.contains(CgmesSubset.STEADY_STATE_HYPOTHESIS) || requestedSubsets.contains(CgmesSubset.STATE_VARIABLES))
                && !requestedSubsets.contains(CgmesSubset.TOPOLOGY)) {
            inconsistentProfilesTPRequiredReport(context.getReportNode(), network.getId());
            LOG.error("Network {} contains node/breaker information. References to Topological Nodes in SSH/SV files will not be valid if TP is not exported.", network.getId());
        }
    }

    /**
     * Export a CGMES subset of a network.
     * @param network The network whose subset is to be exported.
     * @param subset The CGMES subset to export (accepted values are: EQ, TP, SSH, SV).
     * @param fileName The name of the exported file.
     * @param dataSource The data source used by the export.
     * @param context The context used by the export.
     * @param model The model (= metadata information) to use.
     */
    private void subsetExport(Network network, CgmesSubset subset, String fileName, DataSource dataSource, CgmesExportContext context, CgmesMetadataModel model) {
        try (OutputStream out = new BufferedOutputStream(dataSource.newOutputStream(fileName, false))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", out);
            switch (subset) {
                case EQUIPMENT:
                    EquipmentExport.write(network, writer, context, model);
                    break;
                case TOPOLOGY:
                    TopologyExport.write(network, writer, context, model);
                    break;
                case STEADY_STATE_HYPOTHESIS:
                    SteadyStateHypothesisExport.write(network, writer, context, model);
                    break;
                case STATE_VARIABLES:
                    StateVariablesExport.write(network, writer, context, model);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid subset, one of the following value is expected: EQ/TP/SSH/SV.");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    /**
     * Get the version number of a network subset model
     * @param network The network for which the subset model is looked for.
     * @param subset The subset of the network to look for.
     * @return The version number of a network subset model, -1 if no version is found
     */
    private int getVersionNumber(Network network, CgmesSubset subset) {
        // Retrieve model version
        // In the case of a CGM export, the SSH subsets are updated and their version number is incremented
        CgmesMetadataModels networkModels = network.getExtension(CgmesMetadataModels.class);
        Optional<CgmesMetadataModel> networkSubsetModel = networkModels != null ?
                networkModels.getModelForSubset(subset) :
                Optional.empty();
        return networkSubsetModel.map(CgmesMetadataModel::getVersion).orElse(-1);
    }

    /**
     * Retrieve the country of a network if it's unique.
     * @param network The network for which the country is being looked for.
     * @return The network country if it's unique inside the network, else null.
     */
    private static String getCountry(Network network) {
        Set<String> countries = network.getSubstationStream()
                .map(Substation::getCountry)
                .flatMap(Optional::stream)
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());

        if (countries.size() == 1) {
            return countries.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Read the parameters and store them as properties in the context that is used by the export.
     * @param context The context that is used by the export.
     * @param params The optional parameters to read and store.
     * @param reportNode The reportNode used for functional logs.
     * @param referenceDataProvider The reference data such as boundaries or base voltage.
     */
    private void addParametersToContext(CgmesExportContext context, Properties params, ReportNode reportNode, ReferenceDataProvider referenceDataProvider) {
        context.setExportBoundaryPowerFlows(Parameter.readBoolean(getFormat(), params, EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER, defaultValueConfig))
                .setExportFlowsForSwitches(Parameter.readBoolean(getFormat(), params, EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER, defaultValueConfig))
                .setExportTransformersWithHighestVoltageAtEnd1(Parameter.readBoolean(getFormat(), params, EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1_PARAMETER, defaultValueConfig))
                .setExportLoadFlowStatus(Parameter.readBoolean(getFormat(), params, EXPORT_LOAD_FLOW_STATUS_PARAMETER, defaultValueConfig))
                .setExportAllLimitsGroup(Parameter.readBoolean(getFormat(), params, EXPORT_ALL_LIMITS_GROUP_PARAMETER, defaultValueConfig))
                .setMaxPMismatchConverged(Parameter.readDouble(getFormat(), params, MAX_P_MISMATCH_CONVERGED_PARAMETER, defaultValueConfig))
                .setMaxQMismatchConverged(Parameter.readDouble(getFormat(), params, MAX_Q_MISMATCH_CONVERGED_PARAMETER, defaultValueConfig))
                .setExportSvInjectionsForSlacks(Parameter.readBoolean(getFormat(), params, EXPORT_SV_INJECTIONS_FOR_SLACKS_PARAMETER, defaultValueConfig))
                .setEncodeIds(Parameter.readBoolean(getFormat(), params, ENCODE_IDS_PARAMETERS, defaultValueConfig))
                .setBusinessProcess(Parameter.readString(getFormat(), params, BUSINESS_PROCESS_PARAMETER, defaultValueConfig))
                .setModelDescription(Parameter.readString(getFormat(), params, MODEL_DESCRIPTION_PARAMETER, defaultValueConfig))
                .setModelVersion(Parameter.readString(getFormat(), params, MODEL_VERSION_PARAMETER, defaultValueConfig))
                .setModelingAuthoritySet(Parameter.readString(getFormat(), params, MODELING_AUTHORITY_SET_PARAMETER, defaultValueConfig))
                .setProfiles(Parameter.readStringList(getFormat(), params, PROFILES_PARAMETER, defaultValueConfig))
                .setBaseName(Parameter.readString(getFormat(), params, BASE_NAME_PARAMETER))
                .setReportNode(reportNode)
                .setUpdateDependencies(Parameter.readBoolean(getFormat(), params, UPDATE_DEPENDENCIES_PARAMETER, defaultValueConfig));

        // If sourcing actor data has been found and the modeling authority set has not been specified explicitly, set it
        PropertyBag sourcingActor = referenceDataProvider.getSourcingActor();
        if (sourcingActor.containsKey("masUri") && context.getModelingAuthoritySet() == null) {
            context.setModelingAuthoritySet(sourcingActor.get("masUri"));
        }

        // Set CIM version
        String cimVersion = Parameter.readString(getFormat(), params, CIM_VERSION_PARAMETER, defaultValueConfig);
        if (cimVersion != null) {
            context.setCimVersion(Integer.parseInt(cimVersion));
        }

        // Set boundaries
        String boundaryEqId = getBoundaryId(CgmesSubset.EQUIPMENT_BOUNDARY, params, BOUNDARY_EQ_ID_PARAMETER, referenceDataProvider);
        if (boundaryEqId != null && context.getBoundaryEqId() == null) {
            context.setBoundaryEqId(boundaryEqId);
        }
        String boundaryTpId = getBoundaryId(CgmesSubset.TOPOLOGY_BOUNDARY, params, BOUNDARY_TP_ID_PARAMETER, referenceDataProvider);
        if (boundaryTpId != null && context.getBoundaryTpId() == null) {
            context.setBoundaryTpId(boundaryTpId);
        }
    }

    /**
     * Get the boundary id for the given subset,
     * preferentially from the corresponding optional parameter, otherwise from the reference data.
     * @param subset The boundary subset (EQ_BD or TP_BD) for which the id is being looked for.
     * @param params The optional parameters set for that export.
     * @param parameter The boundary ID parameter.
     * @param referenceDataProvider The reference data such as boundaries or base voltage.
     * @return If found, the id of the boundary subset, else null.
     */
    private String getBoundaryId(CgmesSubset subset, Properties params, Parameter parameter, ReferenceDataProvider referenceDataProvider) {
        String id = Parameter.readString(getFormat(), params, parameter, defaultValueConfig);
        // If not specified through a parameter, try to load it from reference data
        if (id == null && referenceDataProvider != null) {
            if (CgmesSubset.EQUIPMENT_BOUNDARY.equals(subset)) {
                id = referenceDataProvider.getEquipmentBoundaryId();
            } else if (CgmesSubset.TOPOLOGY_BOUNDARY.equals(subset)) {
                id = referenceDataProvider.getTopologyBoundaryId();
            }
        }
        return id;
    }

    /**
     * Get the base name for the exported file.
     * @param context The context used by the export. It may store an optional parameter for base name.
     * @param dataSource The dataSoure used by the export. It may also contain the base name.
     * @param network The exported network. If no base name has been provided, the network name or id is used.
     * @return A base name for the exported file.
     */
    private String getBaseName(CgmesExportContext context, DataSource dataSource, Network network) {
        if (context.getBaseName() != null) {
            return context.getBaseName();
        } else if (dataSource.getBaseName() != null && !dataSource.getBaseName().isEmpty()) {
            return dataSource.getBaseName();
        } else {
            return network.getNameOrId();
        }
    }

    /**
     * A small class to manipulate models of an IGM
     * when setting the relationships (dependOn, supersedes) between them in a CGM export.
     */
    private static class IgmModelsForCgm {
        CgmesMetadataModel updatedSsh;
        CgmesMetadataModel originalEq;
        CgmesMetadataModel originalSsh;
        CgmesMetadataModel originalTp;
        CgmesMetadataModel originalTpBd;

        public IgmModelsForCgm(CgmesMetadataModel updatedSsh, CgmesMetadataModel originalEq, CgmesMetadataModel originalSsh,
                               CgmesMetadataModel originalTp, CgmesMetadataModel originalTpBd) {
            this.updatedSsh = updatedSsh;
            this.originalEq = originalEq;
            this.originalSsh = originalSsh;
            this.originalTp = originalTp;
            this.originalTpBd = originalTpBd;
        }
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return "CGMES";
    }

    public static final String BASE_NAME = "iidm.export.cgmes.base-name";
    public static final String BOUNDARY_EQ_ID = "iidm.export.cgmes.boundary-EQ-identifier";
    public static final String BOUNDARY_TP_ID = "iidm.export.cgmes.boundary-TP-identifier";
    public static final String CIM_VERSION = "iidm.export.cgmes.cim-version";
    private static final String ENCODE_IDS = "iidm.export.cgmes.encode-ids";
    public static final String EXPORT_BOUNDARY_POWER_FLOWS = "iidm.export.cgmes.export-boundary-power-flows";
    public static final String EXPORT_POWER_FLOWS_FOR_SWITCHES = "iidm.export.cgmes.export-power-flows-for-switches";
    public static final String NAMING_STRATEGY = "iidm.export.cgmes.naming-strategy";
    public static final String PROFILES = "iidm.export.cgmes.profiles";
    public static final String CGM_EXPORT = "iidm.export.cgmes.cgm_export";
    public static final String MODELING_AUTHORITY_SET = "iidm.export.cgmes.modeling-authority-set";
    public static final String MODEL_DESCRIPTION = "iidm.export.cgmes.model-description";
    public static final String EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1 = "iidm.export.cgmes.export-transformers-with-highest-voltage-at-end1";
    public static final String EXPORT_LOAD_FLOW_STATUS = "iidm.export.cgmes.export-load-flow-status";
    public static final String EXPORT_ALL_LIMITS_GROUP = "iidm.export.cgmes.export-all-limits-group";
    public static final String MAX_P_MISMATCH_CONVERGED = "iidm.export.cgmes.max-p-mismatch-converged";
    public static final String MAX_Q_MISMATCH_CONVERGED = "iidm.export.cgmes.max-q-mismatch-converged";
    public static final String EXPORT_SV_INJECTIONS_FOR_SLACKS = "iidm.export.cgmes.export-sv-injections-for-slacks";
    public static final String SOURCING_ACTOR = "iidm.export.cgmes.sourcing-actor";
    public static final String UUID_NAMESPACE = "iidm.export.cgmes.uuid-namespace";
    public static final String MODEL_VERSION = "iidm.export.cgmes.model-version";
    public static final String BUSINESS_PROCESS = "iidm.export.cgmes.business-process";
    public static final String UPDATE_DEPENDENCIES = "iidm.export.cgmes.update-dependencies";

    private static final Parameter BASE_NAME_PARAMETER = new Parameter(
            BASE_NAME,
            ParameterType.STRING,
            "Basename for output files",
            null);
    private static final Parameter CIM_VERSION_PARAMETER = new Parameter(
            CIM_VERSION,
            ParameterType.STRING,
            "CIM version to export",
            null,
            CgmesNamespace.CIM_LIST.stream().map(cim -> Integer.toString(cim.getVersion())).collect(Collectors.toList()));
    private static final Parameter ENCODE_IDS_PARAMETERS = new Parameter(
            ENCODE_IDS,
            ParameterType.BOOLEAN,
            "Encode IDs as valid URI",
            CgmesExportContext.ENCODE_IDS_DEFAULT_VALUE);
    private static final Parameter EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER = new Parameter(
            EXPORT_BOUNDARY_POWER_FLOWS,
            ParameterType.BOOLEAN,
            "Export boundaries' power flows",
            CgmesExportContext.EXPORT_BOUNDARY_POWER_FLOWS_DEFAULT_VALUE);
    private static final Parameter EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER = new Parameter(
            EXPORT_POWER_FLOWS_FOR_SWITCHES,
            ParameterType.BOOLEAN,
            "Export power flows for switches",
            CgmesExportContext.EXPORT_POWER_FLOWS_FOR_SWITCHES_DEFAULT_VALUE);
    private static final Parameter NAMING_STRATEGY_PARAMETER = new Parameter(
            NAMING_STRATEGY,
            ParameterType.STRING,
            "Configure what type of naming strategy you want",
            NamingStrategyFactory.IDENTITY,
            new ArrayList<>(NamingStrategyFactory.LIST));
    private static final Parameter PROFILES_PARAMETER = new Parameter(
            PROFILES,
            ParameterType.STRING_LIST,
            "Profiles to export",
            List.of("EQ", "TP", "SSH", "SV"),
            List.of("EQ", "TP", "SSH", "SV"));
    private static final Parameter CGM_EXPORT_PARAMETER = new Parameter(
            CGM_EXPORT,
            ParameterType.BOOLEAN,
            "True for a CGM export, False for an IGM export",
            CgmesExportContext.CGM_EXPORT_VALUE);
    private static final Parameter BOUNDARY_EQ_ID_PARAMETER = new Parameter(
            BOUNDARY_EQ_ID,
            ParameterType.STRING,
            "Boundary EQ model identifier",
            null);
    private static final Parameter BOUNDARY_TP_ID_PARAMETER = new Parameter(
            BOUNDARY_TP_ID,
            ParameterType.STRING,
            "Boundary TP model identifier",
            null);
    private static final Parameter MODELING_AUTHORITY_SET_PARAMETER = new Parameter(
            MODELING_AUTHORITY_SET,
            ParameterType.STRING,
            "Modeling authority set",
            null);
    private static final Parameter MODEL_DESCRIPTION_PARAMETER = new Parameter(
            MODEL_DESCRIPTION,
            ParameterType.STRING,
            "Model description",
            null);
    private static final Parameter SOURCING_ACTOR_PARAMETER = new Parameter(
            SOURCING_ACTOR,
            ParameterType.STRING,
            "Sourcing actor name (for CGM business processes)",
            null);

    private static final Parameter EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1_PARAMETER = new Parameter(
            EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1,
            ParameterType.BOOLEAN,
            "Export transformers with highest voltage at end1",
            CgmesExportContext.EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1_DEFAULT_VALUE);

    private static final Parameter EXPORT_LOAD_FLOW_STATUS_PARAMETER = new Parameter(
            EXPORT_LOAD_FLOW_STATUS,
            ParameterType.BOOLEAN,
            "Export load flow status of topological islands",
            CgmesExportContext.EXPORT_LOAD_FLOW_STATUS_DEFAULT_VALUE);
    private static final Parameter EXPORT_ALL_LIMITS_GROUP_PARAMETER = new Parameter(
            EXPORT_ALL_LIMITS_GROUP,
            ParameterType.BOOLEAN,
            "True to export all OperationalLimitsGroup, False to export only the selected group",
            CgmesExportContext.EXPORT_LOAD_FLOW_STATUS_DEFAULT_VALUE);
    private static final Parameter MAX_P_MISMATCH_CONVERGED_PARAMETER = new Parameter(
            MAX_P_MISMATCH_CONVERGED,
            ParameterType.DOUBLE,
            "Max mismatch in active power to consider a bus converged when exporting load flow status of topological islands",
            CgmesExportContext.MAX_P_MISMATCH_CONVERGED_DEFAULT_VALUE);
    private static final Parameter MAX_Q_MISMATCH_CONVERGED_PARAMETER = new Parameter(
            MAX_Q_MISMATCH_CONVERGED,
            ParameterType.DOUBLE,
            "Max mismatch in reactive power to consider a bus converged when exporting load flow status of topological islands",
            CgmesExportContext.MAX_Q_MISMATCH_CONVERGED_DEFAULT_VALUE);
    private static final Parameter EXPORT_SV_INJECTIONS_FOR_SLACKS_PARAMETER = new Parameter(
            EXPORT_SV_INJECTIONS_FOR_SLACKS,
            ParameterType.BOOLEAN,
            "Export SvInjections with the mismatch of slack buses",
            CgmesExportContext.EXPORT_SV_INJECTIONS_FOR_SLACKS_DEFAULT_VALUE);

    private static final Parameter UUID_NAMESPACE_PARAMETER = new Parameter(
            UUID_NAMESPACE,
            ParameterType.STRING,
            "Namespace to use for name-based UUID generation. It must be a valid UUID itself",
            CgmesExportContext.DEFAULT_UUID_NAMESPACE.toString());

    private static final Parameter MODEL_VERSION_PARAMETER = new Parameter(
            MODEL_VERSION,
            ParameterType.STRING,
            "Model version",
            null);

    private static final Parameter BUSINESS_PROCESS_PARAMETER = new Parameter(
            BUSINESS_PROCESS,
            ParameterType.STRING,
            "Business process",
            CgmesExportContext.DEFAULT_BUSINESS_PROCESS);

    private static final Parameter UPDATE_DEPENDENCIES_PARAMETER = new Parameter(
            UPDATE_DEPENDENCIES,
            ParameterType.BOOLEAN,
            "True if dependencies should be updated automatically. False if the user has already put them in the extension for metadata models",
            CgmesExportContext.UPDATE_DEPENDENCIES_DEFAULT_VALUE);

    private static final List<Parameter> STATIC_PARAMETERS = List.of(
            BASE_NAME_PARAMETER,
            CIM_VERSION_PARAMETER,
            EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER,
            EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER,
            NAMING_STRATEGY_PARAMETER,
            PROFILES_PARAMETER,
            CGM_EXPORT_PARAMETER,
            BOUNDARY_EQ_ID_PARAMETER,
            BOUNDARY_TP_ID_PARAMETER,
            MODELING_AUTHORITY_SET_PARAMETER,
            MODEL_DESCRIPTION_PARAMETER,
            EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1_PARAMETER,
            SOURCING_ACTOR_PARAMETER,
            EXPORT_LOAD_FLOW_STATUS_PARAMETER,
            MAX_P_MISMATCH_CONVERGED_PARAMETER,
            MAX_Q_MISMATCH_CONVERGED_PARAMETER,
            EXPORT_SV_INJECTIONS_FOR_SLACKS_PARAMETER,
            UUID_NAMESPACE_PARAMETER,
            MODEL_VERSION_PARAMETER,
            BUSINESS_PROCESS_PARAMETER,
            UPDATE_DEPENDENCIES_PARAMETER);

    private static final Logger LOG = LoggerFactory.getLogger(CgmesExport.class);
}
