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
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.CgmesReports.inconsistentProfilesTPRequiredReport;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@AutoService(Exporter.class)
public class CgmesExport implements Exporter {

    private static final String INDENT = "    ";

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

    @Override
    public void export(Network network, Properties params, DataSource ds, ReportNode reportNode) {
        Objects.requireNonNull(network);
        String baseName = baseName(params, ds, network);

        // Reference data (if required) will come from imported boundaries
        // We may have received a sourcing actor as a parameter
        String sourcingActorName = Parameter.readString(getFormat(), params, SOURCING_ACTOR_PARAMETER, defaultValueConfig);
        String countryName = null;
        if (sourcingActorName == null || sourcingActorName.isEmpty()) {
            // If not given explicitly,
            // the reference data provider can try to obtain it from the country of the network
            // If we have multiple countries we do not pass this info to the reference data provider
            Set<String> countries = getCountries(network);
            if (countries.size() == 1) {
                countryName = countries.iterator().next();
            }
        }
        ReferenceDataProvider referenceDataProvider = new ReferenceDataProvider(sourcingActorName, countryName, importer, params);

        // The UUID namespace parameter must be a valid UUID itself
        UUID uuidNamespace = UUID.fromString(Parameter.readString(getFormat(), params, UUID_NAMESPACE_PARAMETER, defaultValueConfig));
        NamingStrategy namingStrategy = NamingStrategyFactory.create(
                Parameter.readString(getFormat(), params, NAMING_STRATEGY_PARAMETER, defaultValueConfig),
                uuidNamespace);
        CgmesExportContext context = new CgmesExportContext(network, referenceDataProvider, namingStrategy)
                .setExportBoundaryPowerFlows(Parameter.readBoolean(getFormat(), params, EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER, defaultValueConfig))
                .setExportFlowsForSwitches(Parameter.readBoolean(getFormat(), params, EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER, defaultValueConfig))
                .setExportTransformersWithHighestVoltageAtEnd1(Parameter.readBoolean(getFormat(), params, EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1_PARAMETER, defaultValueConfig))
                .setExportLoadFlowStatus(Parameter.readBoolean(getFormat(), params, EXPORT_LOAD_FLOW_STATUS_PARAMETER, defaultValueConfig))
                .setMaxPMismatchConverged(Parameter.readDouble(getFormat(), params, MAX_P_MISMATCH_CONVERGED_PARAMETER, defaultValueConfig))
                .setMaxQMismatchConverged(Parameter.readDouble(getFormat(), params, MAX_Q_MISMATCH_CONVERGED_PARAMETER, defaultValueConfig))
                .setExportSvInjectionsForSlacks(Parameter.readBoolean(getFormat(), params, EXPORT_SV_INJECTIONS_FOR_SLACKS_PARAMETER, defaultValueConfig))
                .setEncodeIds(Parameter.readBoolean(getFormat(), params, ENCODE_IDS_PARAMETERS, defaultValueConfig))
                .setReportNode(reportNode)
                .setBusinessProcess(Parameter.readString(getFormat(), params, BUSINESS_PROCESS_PARAMETER, defaultValueConfig));

        // If sourcing actor data has been found and the modeling authority set has not been specified explicitly, set it
        String masUri = Parameter.readString(getFormat(), params, MODELING_AUTHORITY_SET_PARAMETER, defaultValueConfig);
        PropertyBag sourcingActor = referenceDataProvider.getSourcingActor();
        if (sourcingActor.containsKey("masUri") && masUri == null) {
            masUri = sourcingActor.get("masUri");
        }
        String modelDescription = Parameter.readString(getFormat(), params, MODEL_DESCRIPTION_PARAMETER, defaultValueConfig);
        String modelVersion = Parameter.readString(getFormat(), params, MODEL_VERSION_PARAMETER, defaultValueConfig);
        String cimVersionParam = Parameter.readString(getFormat(), params, CIM_VERSION_PARAMETER, defaultValueConfig);
        if (cimVersionParam != null) {
            context.setCimVersion(Integer.parseInt(cimVersionParam));
        }

        if (Parameter.readBoolean(getFormat(), params, CGM_EXPORT_PARAMETER, defaultValueConfig)) {
            /* CGM export
            This export consists in providing an updated SSH for the IGMs and an updated SV for the whole CGM
            The new updated IGMs SSH shall supersede the original ones
            The new updated CGM SV is dependend on the new updated IGMs SHH and on the original IGMs TP
            */

            // checkCgmConsistency();

            Set<String> updatedIgmSshIds = new HashSet<>();
            Set<String> originalIgmTpIds = new HashSet<>();
            for (Network subnetwork : network.getSubnetworks()) {
                // Retrieve the IGM original SSH and TP model
                CgmesMetadataModels originalIgmModels = subnetwork.getExtension(CgmesMetadataModels.class);
                Optional<CgmesMetadataModel> originalIgmTpModel = originalIgmModels != null ?
                        originalIgmModels.getModelForSubset(CgmesSubset.TOPOLOGY) :
                        Optional.empty();
                originalIgmTpModel.ifPresent(m -> originalIgmTpIds.add(m.getId()));

                // Create a new IGM SSH model based on the original one
                CgmesMetadataModel updatedIgmSshModel = initializeModelForExport(
                        subnetwork, CgmesSubset.STEADY_STATE_HYPOTHESIS, null, null, null, context, Boolean.TRUE);

                // Export the IGM SSH using the updated model
                Set<String> countries = getCountries(subnetwork);
                String igmName = countries.size() == 1 ? countries.iterator().next() : subnetwork.getId();
                String igmFileNameSsh = baseName + "_" + igmName + "_SSH.xml";
                context.addIidmMappings(subnetwork);
                subsetExport(subnetwork, CgmesSubset.STEADY_STATE_HYPOTHESIS, igmFileNameSsh, ds, context, updatedIgmSshModel);
                updatedIgmSshIds.add(updatedIgmSshModel.getId());
            }

            // Create a new CGM SV model based on the original one
            CgmesMetadataModel updatedCgmSvModel = initializeModelForExport(
                    network, CgmesSubset.STATE_VARIABLES, masUri, modelDescription, modelVersion, context, Boolean.TRUE);
            updatedCgmSvModel.addDependentOn(updatedIgmSshIds);
            updatedCgmSvModel.addDependentOn(originalIgmTpIds);

            // Export the CGM SV using the new model
            subsetExport(network, CgmesSubset.STATE_VARIABLES, baseName + "_SV.xml", ds, context, updatedCgmSvModel);
        } else {
            // Initialize models for export
            List<CgmesSubset> exportableSubsets = List.of(
                    CgmesSubset.EQUIPMENT,
                    CgmesSubset.TOPOLOGY,
                    CgmesSubset.STEADY_STATE_HYPOTHESIS,
                    CgmesSubset.STATE_VARIABLES);
            Map<CgmesSubset, CgmesMetadataModel> subsetModels = new EnumMap<>(CgmesSubset.class);
            Boolean modelUpdate = Parameter.readBoolean(getFormat(), params, MODEL_UPDATE_PARAMETER, defaultValueConfig);
            for (CgmesSubset subset : exportableSubsets) {
                CgmesMetadataModel subsetModel = initializeModelForExport(network, subset, masUri, modelDescription, modelVersion, context, modelUpdate);
                subsetModels.put(subset, subsetModel);
            }

            // Update dependencies
            String boundaryEqId = getBoundaryId(CgmesSubset.EQUIPMENT.getIdentifier(), network, params, BOUNDARY_EQ_ID_PARAMETER, referenceDataProvider);
            String boundaryTpId = getBoundaryId(CgmesSubset.TOPOLOGY.getIdentifier(), network, params, BOUNDARY_TP_ID_PARAMETER, referenceDataProvider);
            updateDependenciesIGM(subsetModels, boundaryEqId, boundaryTpId);

            // Export requested subsets
            List<String> requestedSubsets = Parameter.readStringList(getFormat(), params, PROFILES_PARAMETER, defaultValueConfig);
            checkIgmConsistency(requestedSubsets, network, context);
            context.setExportEquipment(requestedSubsets.contains(CgmesSubset.EQUIPMENT.getIdentifier()));
            for (CgmesSubset subset : exportableSubsets) {
                if (requestedSubsets.contains(subset.getIdentifier())) {
                    String fileName = baseName + "_" + subset.getIdentifier() + ".xml";
                    subsetExport(network, subset, fileName, ds, context, subsetModels.get(subset));
                }
            }

            context.getNamingStrategy().debug(baseName, ds);
        }
    }

    /**
     * Update dependencies in a way that:
     *   SV depends on TP and SSH
     *   TP depends on EQ
     *   SSH depends on EQ
     * If the boundaries subset have been defined:
     *   EQ depends on EQ_BD
     *   SV depends on TP_BD
     * @param subsetModels The model for the following subsets: EQ, TP, SSH, SV.
     * @param boundaryEqId The model id for the EQ_BD subset.
     * @param boundaryTpId The model id for the TP_BD subset.
     */
    public void updateDependenciesIGM(Map<CgmesSubset, CgmesMetadataModel> subsetModels, String boundaryEqId, String boundaryTpId) {
        String eqModelId = subsetModels.get(CgmesSubset.EQUIPMENT).getId();
        if (eqModelId == null || eqModelId.isEmpty()) {
            return;
        }

        subsetModels.get(CgmesSubset.TOPOLOGY)
                .clearDependencies()
                .addDependentOn(eqModelId);

        subsetModels.get(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .clearDependencies()
                .addDependentOn(eqModelId);

        subsetModels.get(CgmesSubset.STATE_VARIABLES)
                .clearDependencies()
                .addDependentOn(subsetModels.get(CgmesSubset.TOPOLOGY).getId())
                .addDependentOn(subsetModels.get(CgmesSubset.STEADY_STATE_HYPOTHESIS).getId());

        if (boundaryEqId != null) {
            subsetModels.get(CgmesSubset.EQUIPMENT).addDependentOn(boundaryEqId);
        }
        if (boundaryTpId != null) {
            subsetModels.get(CgmesSubset.STATE_VARIABLES).addDependentOn(boundaryTpId);
        }
    }

    /**
     * Initialize the model (= the metadata information) that is used by the export.
     * If existing, the network model extension is used for the initialization.
     * If existing, optional properties are also used for the initialization.
     * If both are present, the optional parameters prevail the values in the network extension.
     * @param network The network in which to look for an existing model extension as basis for initialization.
     * @param subset The subset of the model to initialize.
     * @param modelingAuthoritySet The modeling authority set of the model to initialize.
     * @param modelDescription An optional parameter to give the description of the model to initialize.
     * @param modelVersion An optional parameter to give the version of the model to initialize.
     * @param context The context used by the export.
     * @param modelUpdate A boolean indicating whether the model has been updated.
     * @return A model with all necessary metadata information that will be used by the export.
     */
    public static CgmesMetadataModel initializeModelForExport(
            Network network,
            CgmesSubset subset,
            String modelingAuthoritySet,
            String modelDescription,
            String modelVersion,
            CgmesExportContext context,
            Boolean modelUpdate) {
        // Initialize a new model that will be used by the export
        CgmesMetadataModel modelForExport = new CgmesMetadataModel(subset, CgmesExportContext.DEFAULT_MODELING_AUTHORITY_SET_VALUE);
        modelForExport.setProfile(context.getCim().getProfileUri(subset.getIdentifier()));

        // If a model extension has been created, use it as basis for the export
        CgmesMetadataModels networkModels = network.getExtension(CgmesMetadataModels.class);
        Optional<CgmesMetadataModel> networkSubsetModel = networkModels != null ?
                networkModels.getModelForSubset(subset) :
                Optional.empty();
        networkSubsetModel.ifPresent(m -> modelForExport.setDescription(m.getDescription()));
        networkSubsetModel.ifPresent(m -> modelForExport.setVersion(m.getVersion()));
        networkSubsetModel.ifPresent(m -> modelForExport.addSupersedes(m.getId()));
        networkSubsetModel.ifPresent(m -> modelForExport.addDependentOn(m.getDependentOn()));
        networkSubsetModel.ifPresent(m -> modelForExport.setModelingAuthoritySet(m.getModelingAuthoritySet()));

        // In case the model has been updated, it should supersede the base one and its version number should be incremented
        if (modelUpdate.equals(Boolean.TRUE) && networkSubsetModel.isPresent()) {
            modelForExport.addSupersedes(networkSubsetModel.get().getId());
            modelForExport.setVersion(networkSubsetModel.get().getVersion() + 1);
        }

        // If optional parameters have been specified, use them
        if (modelDescription != null) {
            modelForExport.setDescription(modelDescription);
        }
        if (modelVersion != null) {
            modelForExport.setVersion(Integer.parseInt(modelVersion));
        }
        if (modelingAuthoritySet != null) {
            modelForExport.setModelingAuthoritySet(modelingAuthoritySet);
        }

        // Now that all information have been set, initialize the model id
        CgmesExportUtil.initializeModelId(network, modelForExport, context);

        return modelForExport;
    }

    /**
     * Retrieve all the countries present in a network.
     * @param network the network for which the countries are being looked for
     * @return a Set of countries present in the network
     */
    private static Set<String> getCountries(Network network) {
        return network.getSubstationStream()
                .map(Substation::getCountry)
                .flatMap(Optional::stream)
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Export a CGMES subset of a network.
     * @param network the network whose subset is to be exported
     * @param subset the CGMES subset to export (accepted values are: EQ/TP/SSH/SV)
     * @param fileName the name of the exported file
     * @param dataSource the data source used by the export
     * @param context the context used by the export
     * @param model if provided, the model information to use
     */
    private void subsetExport(Network network, CgmesSubset subset, String fileName, DataSource dataSource, CgmesExportContext context, CgmesMetadataModel model) {
        try (OutputStream out = new BufferedOutputStream(dataSource.newOutputStream(fileName, false))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, out);
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

    private static void checkCgmConsistency() {
        /* TODO
        Verify that each of the subnetwork has a CgmesMetadataModel of part SSH and TP
        This is necessary in order to correctly build the references (dependentOn and supersedes)
        to the IGM SSH and TP export files
        */
    }

    private String getBoundaryId(String profile, Network network, Properties params, Parameter parameter, ReferenceDataProvider referenceDataProvider) {
        if (network.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + profile + "_BD_ID")) {
            return network.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + profile + "_BD_ID");
        }
        String id = Parameter.readString(getFormat(), params, parameter, defaultValueConfig);
        // If not specified through a parameter, try to load it from reference data
        if (id == null && referenceDataProvider != null) {
            if ("EQ".equals(profile)) {
                id = referenceDataProvider.getEquipmentBoundaryId();
            } else if ("TP".equals(profile)) {
                id = referenceDataProvider.getTopologyBoundaryId();
            }
        }
        return id;
    }

    private static void addSubsetIdentifiers(Network network, String profile, CgmesMetadataModel description) {
        description.addDependentOn(network.getPropertyNames().stream()
                .filter(p -> p.startsWith(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + profile + "_ID"))
                .map(network::getProperty)
                .toList());
    }

    private static void checkIgmConsistency(List<String> profiles, Network network, CgmesExportContext context) {
        boolean networkIsNodeBreaker = network.getVoltageLevelStream()
                .map(VoltageLevel::getTopologyKind)
                .anyMatch(tk -> tk == TopologyKind.NODE_BREAKER);
        if (networkIsNodeBreaker
                && (profiles.contains("SSH") || profiles.contains("SV"))
                && !profiles.contains("TP")) {
            inconsistentProfilesTPRequiredReport(context.getReportNode(), network.getId());
            LOG.error("Network {} contains node/breaker information. References to Topological Nodes in SSH/SV files will not be valid if TP is not exported.", network.getId());
        }
    }

    private String baseName(Properties params, DataSource ds, Network network) {
        String baseName = Parameter.readString(getFormat(), params, BASE_NAME_PARAMETER);
        if (baseName != null) {
            return baseName;
        } else if (ds.getBaseName() != null && !ds.getBaseName().isEmpty()) {
            return ds.getBaseName();
        }
        return network.getNameOrId();
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
    public static final String MODEL_UPDATE = "iidm.export.cgmes.model_update";
    public static final String MODELING_AUTHORITY_SET = "iidm.export.cgmes.modeling-authority-set";
    public static final String MODEL_DESCRIPTION = "iidm.export.cgmes.model-description";
    public static final String EXPORT_TRANSFORMERS_WITH_HIGHEST_VOLTAGE_AT_END1 = "iidm.export.cgmes.export-transformers-with-highest-voltage-at-end1";
    public static final String EXPORT_LOAD_FLOW_STATUS = "iidm.export.cgmes.export-load-flow-status";
    public static final String MAX_P_MISMATCH_CONVERGED = "iidm.export.cgmes.max-p-mismatch-converged";
    public static final String MAX_Q_MISMATCH_CONVERGED = "iidm.export.cgmes.max-q-mismatch-converged";
    public static final String EXPORT_SV_INJECTIONS_FOR_SLACKS = "iidm.export.cgmes.export-sv-injections-for-slacks";
    public static final String SOURCING_ACTOR = "iidm.export.cgmes.sourcing-actor";
    public static final String UUID_NAMESPACE = "iidm.export.cgmes.uuid-namespace";
    public static final String MODEL_VERSION = "iidm.export.cgmes.model-version";
    public static final String BUSINESS_PROCESS = "iidm.export.cgmes.business-process";

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
    private static final Parameter MODEL_UPDATE_PARAMETER = new Parameter(
            MODEL_UPDATE,
            ParameterType.BOOLEAN,
            "True if the model has been updated, False otherwise",
            CgmesExportContext.MODEL_UPDATE_VALUE);
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

    private static final List<Parameter> STATIC_PARAMETERS = List.of(
            BASE_NAME_PARAMETER,
            CIM_VERSION_PARAMETER,
            EXPORT_BOUNDARY_POWER_FLOWS_PARAMETER,
            EXPORT_POWER_FLOWS_FOR_SWITCHES_PARAMETER,
            NAMING_STRATEGY_PARAMETER,
            PROFILES_PARAMETER,
            CGM_EXPORT_PARAMETER,
            MODEL_UPDATE_PARAMETER,
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
            BUSINESS_PROCESS_PARAMETER);

    private static final Logger LOG = LoggerFactory.getLogger(CgmesExport.class);
}
