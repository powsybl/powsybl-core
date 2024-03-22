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
        String filenameEq = baseName + "_EQ.xml";
        String filenameTp = baseName + "_TP.xml";
        String filenameSsh = baseName + "_SSH.xml";
        String filenameSv = baseName + "_SV.xml";

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
                .setBoundaryEqId(getBoundaryId("EQ", network, params, BOUNDARY_EQ_ID_PARAMETER, referenceDataProvider))
                .setBoundaryTpId(getBoundaryId("TP", network, params, BOUNDARY_TP_ID_PARAMETER, referenceDataProvider))
                .setReportNode(reportNode)
                .setBusinessProcess(Parameter.readString(getFormat(), params, BUSINESS_PROCESS_PARAMETER, defaultValueConfig));

        // If sourcing actor data has been found and the modeling authority set has not been specified explicitly, set it
        String masUri = Parameter.readString(getFormat(), params, MODELING_AUTHORITY_SET_PARAMETER, defaultValueConfig);
        PropertyBag sourcingActor = referenceDataProvider.getSourcingActor();
        if (sourcingActor.containsKey("masUri") && masUri.equals(CgmesExportContext.DEFAULT_MODELING_AUTHORITY_SET_VALUE)) {
            masUri = sourcingActor.get("masUri");
        }

        context.getExportedEQModel().setModelingAuthoritySet(masUri);
        context.getExportedTPModel().setModelingAuthoritySet(masUri);
        context.getExportedSSHModel().setModelingAuthoritySet(masUri);
        context.getExportedSVModel().setModelingAuthoritySet(masUri);
        String modelDescription = Parameter.readString(getFormat(), params, MODEL_DESCRIPTION_PARAMETER, defaultValueConfig);
        if (modelDescription != null) {
            context.getExportedEQModel().setDescription(modelDescription);
            context.getExportedTPModel().setDescription(modelDescription);
            context.getExportedSSHModel().setDescription(modelDescription);
            context.getExportedSVModel().setDescription(modelDescription);
        }
        String cimVersionParam = Parameter.readString(getFormat(), params, CIM_VERSION_PARAMETER, defaultValueConfig);
        if (cimVersionParam != null) {
            context.setCimVersion(Integer.parseInt(cimVersionParam));
        }

        String modelVersion = Parameter.readString(getFormat(), params, MODEL_VERSION_PARAMETER, defaultValueConfig);
        if (modelVersion != null) {
            context.getExportedEQModel().setVersion(Integer.parseInt(modelVersion));
            context.getExportedTPModel().setVersion(Integer.parseInt(modelVersion));
            context.getExportedSSHModel().setVersion(Integer.parseInt(modelVersion));
            context.getExportedSVModel().setVersion(Integer.parseInt(modelVersion));
        }

        if (Parameter.readBoolean(getFormat(), params, EXPORT_AS_CGM_PARAMETER, defaultValueConfig)) {
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
                Optional<CgmesMetadataModel> originalIgmSshModel = originalIgmModels != null ?
                        originalIgmModels.getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS) :
                        Optional.empty();
                Optional<CgmesMetadataModel> originalIgmTpModel = originalIgmModels != null ?
                        originalIgmModels.getModelForSubset(CgmesSubset.TOPOLOGY) :
                        Optional.empty();
                originalIgmTpModel.ifPresent(m -> originalIgmTpIds.add(m.getId()));

                // Create a new IGM SSH model based on the original one
                String igmMAS = originalIgmSshModel
                        .map(CgmesMetadataModel::getModelingAuthoritySet)
                        .orElseGet(() -> CgmesExportContext.DEFAULT_MODELING_AUTHORITY_SET_VALUE);
                CgmesMetadataModel updatedIgmSshModel = new CgmesMetadataModel(CgmesSubset.STEADY_STATE_HYPOTHESIS, igmMAS);
                context.getExportedSSHModel().getProfiles().forEach(updatedIgmSshModel::setProfile);
                originalIgmSshModel.ifPresent(m -> updatedIgmSshModel.setDescription(m.getDescription()));
                originalIgmSshModel.ifPresent(m -> updatedIgmSshModel.setVersion(m.getVersion() + 1));
                originalIgmSshModel.ifPresent(m -> updatedIgmSshModel.addSupersedes(m.getId()));

                // Export the IGM SSH using the updated model
                Set<String> countries = getCountries(subnetwork);
                String igmName = countries.size() == 1 ? countries.iterator().next() : subnetwork.getId();
                String igmFileNameSsh = baseName + "_" + igmName + "_SSH.xml";
                context.addIidmMappings(subnetwork);
                subsetExport(subnetwork, CgmesSubset.STEADY_STATE_HYPOTHESIS, igmFileNameSsh, ds, context, Optional.of(updatedIgmSshModel));
                updatedIgmSshIds.add(updatedIgmSshModel.getId());
            }

            // Check for an existing CGM SV model
            CgmesMetadataModels originalCgmModels = network.getExtension(CgmesMetadataModels.class);
            Optional<CgmesMetadataModel> originalCgmSvModel = originalCgmModels != null ?
                    originalCgmModels.getModelForSubset(CgmesSubset.STATE_VARIABLES) :
                    Optional.empty();

            // Create a new CGM SV model based on the original one
            CgmesMetadataModel updatedCgmSvModel = new CgmesMetadataModel(CgmesSubset.STATE_VARIABLES, masUri);
            context.getExportedSVModel().getProfiles().forEach(updatedCgmSvModel::setProfile);
            updatedCgmSvModel.addDependentOn(updatedIgmSshIds);
            updatedCgmSvModel.addDependentOn(originalIgmTpIds);
            originalCgmSvModel.ifPresent(m -> updatedCgmSvModel.setDescription(m.getDescription()));
            originalCgmSvModel.ifPresent(m -> updatedCgmSvModel.setVersion(m.getVersion() + 1));

            // Export the CGM SV using the new model
            subsetExport(network, CgmesSubset.STATE_VARIABLES, filenameSv, ds, context, Optional.of(updatedCgmSvModel));
        } else {
            // IGM export
            context.updateDependenciesIGM();

            List<String> profiles = Parameter.readStringList(getFormat(), params, PROFILES_PARAMETER, defaultValueConfig);
            checkIgmConsistency(profiles, network, context);
            if (profiles.contains("EQ")) {
                subsetExport(network, CgmesSubset.EQUIPMENT, filenameEq, ds, context, Optional.empty());
            } else {
                addSubsetIdentifiers(network, "EQ", context.getExportedEQModel());
                context.getExportedEQModel().setId(context.getNamingStrategy().getCgmesId(network));
            }
            if (profiles.contains("TP")) {
                subsetExport(network, CgmesSubset.TOPOLOGY, filenameTp, ds, context, Optional.empty());
            } else {
                addSubsetIdentifiers(network, "TP", context.getExportedTPModel());
            }
            if (profiles.contains("SSH")) {
                subsetExport(network, CgmesSubset.STEADY_STATE_HYPOTHESIS, filenameSsh, ds, context, Optional.empty());
            } else {
                addSubsetIdentifiers(network, "SSH", context.getExportedSSHModel());
            }
            if (profiles.contains("SV")) {
                subsetExport(network, CgmesSubset.STATE_VARIABLES, filenameSv, ds, context, Optional.empty());
            }
            context.getNamingStrategy().debug(baseName, ds);
        }
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
     * @param dataSource the data source used for the export
     * @param context the context used for the export
     * @param model if provided, the model information to use
     */
    private void subsetExport(Network network, CgmesSubset subset, String fileName, DataSource dataSource, CgmesExportContext context, Optional<CgmesMetadataModel> model) {
        try (OutputStream out = new BufferedOutputStream(dataSource.newOutputStream(fileName, false))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, INDENT, out);
            switch (subset) {
                case EQUIPMENT:
                    EquipmentExport.write(network, writer, context);
                    break;
                case TOPOLOGY:
                    TopologyExport.write(network, writer, context);
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
    public static final String EXPORT_AS_CGM = "iidm.export.cgmes.export_as_cgm";
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
    private static final Parameter EXPORT_AS_CGM_PARAMETER = new Parameter(
            EXPORT_AS_CGM,
            ParameterType.BOOLEAN,
            "True for a CGM export, False for an IGM export",
            CgmesExportContext.EXPORT_AS_CGM_VALUE);
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
            CgmesExportContext.DEFAULT_MODELING_AUTHORITY_SET_VALUE);
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
            EXPORT_AS_CGM_PARAMETER,
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
