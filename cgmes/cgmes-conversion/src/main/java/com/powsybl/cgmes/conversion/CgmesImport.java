/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.naming.NamingStrategyFactory;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CgmesOnDataSource;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterScope;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.iidm.network.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.api.TripleStoreOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
@AutoService(Importer.class)
public class CgmesImport implements Importer {

    enum FictitiousSwitchesCreationMode {
        ALWAYS,
        ALWAYS_EXCEPT_SWITCHES,
        NEVER;
    }

    public CgmesImport(PlatformConfig platformConfig, List<CgmesImportPreProcessor> preProcessors, List<CgmesImportPostProcessor> postProcessors) {
        this.defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
        this.preProcessors = Objects.requireNonNull(preProcessors).stream()
                .collect(Collectors.toMap(CgmesImportPreProcessor::getName, e -> e));
        this.postProcessors = Objects.requireNonNull(postProcessors).stream()
                .collect(Collectors.toMap(CgmesImportPostProcessor::getName, e -> e));
        String boundaryPath = platformConfig.getConfigDir()
                .map(dir -> dir.resolve(FORMAT).resolve("boundary"))
                .map(Path::toString)
                .orElse(null);
        // Boundary location parameter can not be static
        // because we want its default value
        // to depend on the received platformConfig
        boundaryLocationParameter = new Parameter(
                BOUNDARY_LOCATION,
                ParameterType.STRING,
                "The location of boundary files",
                boundaryPath,
                null,
                ParameterScope.TECHNICAL);
        preProcessorsParameter = new Parameter(
                PRE_PROCESSORS,
                ParameterType.STRING_LIST,
                "Pre processors",
                Collections.emptyList(),
                preProcessors.stream().map(CgmesImportPreProcessor::getName).collect(Collectors.toList()));
        postProcessorsParameter = new Parameter(
                POST_PROCESSORS,
                ParameterType.STRING_LIST,
                "Post processors",
                Collections.emptyList(),
                postProcessors.stream().map(CgmesImportPostProcessor::getName).collect(Collectors.toList()));
    }

    public CgmesImport(PlatformConfig platformConfig) {
        this(platformConfig,
                new ServiceLoaderCache<>(CgmesImportPreProcessor.class).getServices(),
                new ServiceLoaderCache<>(CgmesImportPostProcessor.class).getServices());
    }

    public CgmesImport(List<CgmesImportPreProcessor> preProcessors, List<CgmesImportPostProcessor> postProcessors) {
        this(PlatformConfig.defaultConfig(), preProcessors, postProcessors);
    }

    public CgmesImport() {
        this(PlatformConfig.defaultConfig());
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> allParams = new ArrayList<>(STATIC_PARAMETERS);
        allParams.add(boundaryLocationParameter);
        allParams.add(postProcessorsParameter);
        return Collections.unmodifiableList(allParams);
    }

    @Override
    public boolean exists(ReadOnlyDataSource ds) {
        CgmesOnDataSource cds = new CgmesOnDataSource(ds);
        if (cds.exists()) {
            return true;
        }
        // If we are configured to support CIM14,
        // check if there is this CIM14 data
        return importCim14 && cds.existsCim14();
    }

    @Override
    public String getComment() {
        return "ENTSO-E CGMES version 2.4.15";
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public Network importData(ReadOnlyDataSource ds, NetworkFactory networkFactory, Properties p, Reporter reporter) {
        Objects.requireNonNull(ds);
        Objects.requireNonNull(networkFactory);
        Objects.requireNonNull(reporter);
        CgmesModel cgmes = readCgmes(ds, p, reporter);
        Reporter conversionReporter = reporter.createSubReporter("CGMESConversion", "Importing CGMES file(s)");
        return new Conversion(cgmes, config(p), activatedPreProcessors(p), activatedPostProcessors(p), networkFactory).convert(conversionReporter);
    }

    public CgmesModel readCgmes(ReadOnlyDataSource ds, Properties p, Reporter reporter) {
        TripleStoreOptions options = new TripleStoreOptions();
        String sourceForIidmIds = Parameter.readString(getFormat(), p, SOURCE_FOR_IIDM_ID_PARAMETER, defaultValueConfig);
        if (sourceForIidmIds.equalsIgnoreCase(SOURCE_FOR_IIDM_ID_MRID)) {
            options.setRemoveInitialUnderscoreForIdentifiers(true);
        } else if (sourceForIidmIds.equalsIgnoreCase(SOURCE_FOR_IIDM_ID_RDFID)) {
            options.setRemoveInitialUnderscoreForIdentifiers(false);
        }
        options.decodeEscapedIdentifiers(Parameter.readBoolean(getFormat(), p, DECODE_ESCAPED_IDENTIFIERS_PARAMETER, defaultValueConfig));
        Reporter tripleStoreReporter = reporter.createSubReporter("CGMESTriplestore", "Reading CGMES Triplestore");
        return CgmesModelFactory.create(ds, boundary(p), tripleStore(p), tripleStoreReporter, options);
    }

    @Override
    public void copy(ReadOnlyDataSource from, DataSource to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        try {
            CgmesOnDataSource fromCgmes = new CgmesOnDataSource(from);
            // TODO map "from names" to "to names" using base names of data sources
            for (String fromName : fromCgmes.names()) {
                String toName = fromName;
                copyStream(from, to, fromName, toName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ReadOnlyDataSource boundary(Properties p) {
        String loc = Parameter.readString(
                getFormat(),
                p,
                boundaryLocationParameter,
                defaultValueConfig);
        if (loc == null) {
            return null;
        }
        Path ploc = Path.of(loc);
        if (!Files.exists(ploc)) {
            LOGGER.warn("Location of boundaries does not exist {}. No attempt to load boundaries will be made", loc);
            return null;
        }
        // Check that the Data Source has valid CGMES names
        ReadOnlyDataSource ds = new GenericReadOnlyDataSource(ploc);
        if ((new CgmesOnDataSource(ds)).names().isEmpty()) {
            return null;
        }
        return ds;
    }

    private String tripleStore(Properties p) {
        return Parameter.readString(
                getFormat(),
                p,
                POWSYBL_TRIPLESTORE_PARAMETER,
                defaultValueConfig);
    }

    private Conversion.Config config(Properties p) {
        Conversion.Config config = new Conversion.Config()
                .setAllowUnsupportedTapChangers(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                ALLOW_UNSUPPORTED_TAP_CHANGERS_PARAMETER,
                                defaultValueConfig))
                .setChangeSignForShuntReactivePowerFlowInitialState(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE_PARAMETER,
                                defaultValueConfig))
                .setConvertBoundary(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CONVERT_BOUNDARY_PARAMETER,
                                defaultValueConfig))
                .setConvertSvInjections(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CONVERT_SV_INJECTIONS_PARAMETER,
                                defaultValueConfig))
                .setCreateBusbarSectionForEveryConnectivityNode(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER,
                                defaultValueConfig))
                .setEnsureIdAliasUnicity(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                ENSURE_ID_ALIAS_UNICITY_PARAMETER,
                                defaultValueConfig))
                .setImportControlAreas(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                IMPORT_CONTROL_AREAS_PARAMETER,
                                defaultValueConfig))
                .setProfileForInitialValuesShuntSectionsTapPositions(
                        Parameter.readString(
                                getFormat(),
                                p,
                                PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS_PARAMETER,
                                defaultValueConfig))
                .setStoreCgmesModelAsNetworkExtension(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER,
                                defaultValueConfig))
                .setStoreCgmesConversionContextAsNetworkExtension(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION_PARAMETER,
                                defaultValueConfig))
                .setCreateActivePowerControlExtension(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                CREATE_ACTIVE_POWER_CONTROL_EXTENSION_PARAMETER,
                                defaultValueConfig))
                .createFictitiousSwitchesForDisconnectedTerminalsMode(FictitiousSwitchesCreationMode.valueOf(
                        Parameter.readString(
                                getFormat(),
                                p,
                                CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE_PARAMETER,
                                defaultValueConfig)))
                .setImportNodeBreakerAsBusBreaker(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                IMPORT_NODE_BREAKER_AS_BUS_BREAKER_PARAMETER,
                                defaultValueConfig))
                .setDisconnectNetworkSideOfDanglingLinesIfBoundaryIsDisconnected(
                        Parameter.readBoolean(
                                getFormat(),
                                p,
                                DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED_PARAMETER,
                                defaultValueConfig));
        String namingStrategy = Parameter.readString(getFormat(), p, NAMING_STRATEGY_PARAMETER, defaultValueConfig);

        // FIXME(Luma) When using a naming strategy for CGMES import we should not need an uuid namespace,
        //   because we won't be creating new UUIDs
        UUID uuidNamespace = CgmesExportContext.DEFAULT_UUID_NAMESPACE;
        config.setNamingStrategy(NamingStrategyFactory.create(namingStrategy, uuidNamespace));
        return config;
    }

    private List<CgmesImportPreProcessor> activatedPreProcessors(Properties p) {
        return Parameter
                .readStringList(getFormat(), p, preProcessorsParameter, defaultValueConfig)
                .stream()
                .filter(name -> {
                    boolean found = preProcessors.containsKey(name);
                    if (!found) {
                        LOGGER.warn("CGMES pre processor {} not found", name);
                    }
                    return found;
                })
                .map(preProcessors::get)
                .collect(Collectors.toList());
    }

    private List<CgmesImportPostProcessor> activatedPostProcessors(Properties p) {
        return Parameter
                .readStringList(getFormat(), p, postProcessorsParameter, defaultValueConfig)
                .stream()
                .filter(name -> {
                    boolean found = postProcessors.containsKey(name);
                    if (!found) {
                        LOGGER.warn("CGMES post processor {} not found", name);
                    }
                    return found;
                })
                .map(postProcessors::get)
                .collect(Collectors.toList());
    }

    private void copyStream(ReadOnlyDataSource from, DataSource to, String fromName, String toName) throws IOException {
        if (from.exists(fromName)) {
            try (InputStream is = from.newInputStream(fromName);
                 OutputStream os = to.newOutputStream(toName, false)) {
                ByteStreams.copy(is, os);
            }
        }
    }

    private static final String FORMAT = "CGMES";

    public static final String ALLOW_UNSUPPORTED_TAP_CHANGERS = "iidm.import.cgmes.allow-unsupported-tap-changers";
    public static final String BOUNDARY_LOCATION = "iidm.import.cgmes.boundary-location";
    public static final String CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE = "iidm.import.cgmes.change-sign-for-shunt-reactive-power-flow-initial-state";
    public static final String CONVERT_BOUNDARY = "iidm.import.cgmes.convert-boundary";
    public static final String CONVERT_SV_INJECTIONS = "iidm.import.cgmes.convert-sv-injections";
    public static final String CREATE_ACTIVE_POWER_CONTROL_EXTENSION = "iidm.import.cgmes.create-active-power-control-extension";
    public static final String CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE = "iidm.import.cgmes.create-busbar-section-for-every-connectivity-node";
    public static final String CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE = "iidm.import.cgmes.create-fictitious-switches-for-disconnected-terminals-mode";
    public static final String DECODE_ESCAPED_IDENTIFIERS = "iidm.import.cgmes.decode-escaped-identifiers";
    public static final String ENSURE_ID_ALIAS_UNICITY = "iidm.import.cgmes.ensure-id-alias-unicity";
    public static final String IMPORT_CONTROL_AREAS = "iidm.import.cgmes.import-control-areas";
    public static final String NAMING_STRATEGY = "iidm.import.cgmes.naming-strategy";
    public static final String PRE_PROCESSORS = "iidm.import.cgmes.pre-processors";
    public static final String POST_PROCESSORS = "iidm.import.cgmes.post-processors";
    public static final String POWSYBL_TRIPLESTORE = "iidm.import.cgmes.powsybl-triplestore";
    public static final String PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS = "iidm.import.cgmes.profile-for-initial-values-shunt-sections-tap-positions";
    public static final String SOURCE_FOR_IIDM_ID = "iidm.import.cgmes.source-for-iidm-id";
    public static final String STORE_CGMES_MODEL_AS_NETWORK_EXTENSION = "iidm.import.cgmes.store-cgmes-model-as-network-extension";
    public static final String STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION = "iidm.import.cgmes.store-cgmes-conversion-context-as-network-extension";
    public static final String IMPORT_NODE_BREAKER_AS_BUS_BREAKER = "iidm.import.cgmes.import-node-breaker-as-bus-breaker";
    public static final String DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED = "iidm.import.cgmes.disconnect-dangling-line-if-boundary-side-is-disconnected";

    public static final String SOURCE_FOR_IIDM_ID_MRID = "mRID";
    public static final String SOURCE_FOR_IIDM_ID_RDFID = "rdfID";

    private static final Parameter ALLOW_UNSUPPORTED_TAP_CHANGERS_PARAMETER = new Parameter(
            ALLOW_UNSUPPORTED_TAP_CHANGERS,
            ParameterType.BOOLEAN,
            "Allow import of potentially unsupported tap changers",
            Boolean.TRUE);
    private static final Parameter CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE_PARAMETER = new Parameter(
            CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE,
            ParameterType.BOOLEAN,
            "Change the sign of the reactive power flow for shunt in initial state",
            Boolean.FALSE)
            .addAdditionalNames("changeSignForShuntReactivePowerFlowInitialState");
    private static final Parameter CONVERT_BOUNDARY_PARAMETER = new Parameter(
            CONVERT_BOUNDARY,
            ParameterType.BOOLEAN,
            "Convert boundary during import",
            Boolean.FALSE)
            .addAdditionalNames("convertBoundary");
    private static final Parameter CONVERT_SV_INJECTIONS_PARAMETER = new Parameter(
            CONVERT_SV_INJECTIONS,
            ParameterType.BOOLEAN,
            "Convert SV injections during import",
            Boolean.TRUE);
    private static final Parameter CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER = new Parameter(
            CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE,
            ParameterType.BOOLEAN,
            "Create busbar section for every connectivity node",
            Boolean.FALSE)
            .addAdditionalNames("createBusbarSectionForEveryConnectivityNode");
    private static final Parameter ENSURE_ID_ALIAS_UNICITY_PARAMETER = new Parameter(
            ENSURE_ID_ALIAS_UNICITY,
            ParameterType.BOOLEAN,
            "Ensure IDs and aliases are unique",
            Boolean.FALSE);
    private static final Parameter NAMING_STRATEGY_PARAMETER = new Parameter(
            NAMING_STRATEGY,
            ParameterType.STRING,
            "Configure what type of naming strategy you want to use",
            NamingStrategyFactory.IDENTITY,
            new ArrayList<>(NamingStrategyFactory.LIST));
    private static final Parameter IMPORT_CONTROL_AREAS_PARAMETER = new Parameter(
            IMPORT_CONTROL_AREAS,
            ParameterType.BOOLEAN,
            "Import control areas",
            Boolean.TRUE);
    private static final Parameter POWSYBL_TRIPLESTORE_PARAMETER = new Parameter(
            POWSYBL_TRIPLESTORE,
            ParameterType.STRING,
            "The triplestore used during the import",
            TripleStoreFactory.defaultImplementation(),
            null,
            ParameterScope.TECHNICAL)
            .addAdditionalNames("powsyblTripleStore");
    private static final Parameter PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS_PARAMETER = new Parameter(
            PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS,
            ParameterType.STRING,
            "Profile used for initial state values",
            "SSH",
            List.of("SSH", "SV"))
            .addAdditionalNames("iidm.import.cgmes.profile-used-for-initial-state-values");
    private static final Parameter STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION_PARAMETER = new Parameter(
            STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION,
            ParameterType.BOOLEAN,
            "Store the CGMES-IIDM terminal mapping as a network extension",
            Boolean.FALSE);
    private static final Parameter CREATE_ACTIVE_POWER_CONTROL_EXTENSION_PARAMETER = new Parameter(
            CREATE_ACTIVE_POWER_CONTROL_EXTENSION,
            ParameterType.BOOLEAN,
            "Create active power control extension during import",
            Boolean.FALSE);
    private static final Parameter CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE_PARAMETER = new Parameter(
            CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE,
            ParameterType.STRING,
            "Defines in which case fictitious switches for disconnected terminals are created (relevant for node-breaker models only): always, always except for switches or never",
            FictitiousSwitchesCreationMode.ALWAYS.name(),
            Arrays.stream(FictitiousSwitchesCreationMode.values()).map(Enum::name).collect(Collectors.toList()));
    private static final Parameter STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER = new Parameter(
            STORE_CGMES_MODEL_AS_NETWORK_EXTENSION,
            ParameterType.BOOLEAN,
            "Store the initial CGMES model as a network extension",
            Boolean.TRUE)
            .addAdditionalNames("storeCgmesModelAsNetworkExtension");
    private static final Parameter SOURCE_FOR_IIDM_ID_PARAMETER = new Parameter(
            SOURCE_FOR_IIDM_ID,
            ParameterType.STRING,
            "Source for IIDM identifiers",
            SOURCE_FOR_IIDM_ID_MRID,
            List.of(SOURCE_FOR_IIDM_ID_MRID, SOURCE_FOR_IIDM_ID_RDFID));
    private static final Parameter DECODE_ESCAPED_IDENTIFIERS_PARAMETER = new Parameter(
            DECODE_ESCAPED_IDENTIFIERS,
            ParameterType.BOOLEAN,
            "Decode escaped special characters in IDs",
            Boolean.TRUE);
    public static final Parameter IMPORT_NODE_BREAKER_AS_BUS_BREAKER_PARAMETER = new Parameter(
            IMPORT_NODE_BREAKER_AS_BUS_BREAKER,
            ParameterType.BOOLEAN,
            "Force import of CGMES node/breaker models as bus/breaker",
            Boolean.FALSE);
    public static final Parameter DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED_PARAMETER = new Parameter(
            DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED,
            ParameterType.BOOLEAN,
            "Force disconnection of dangling line network side if boundary side is disconnected",
            Boolean.TRUE);

    private static final List<Parameter> STATIC_PARAMETERS = List.of(
            ALLOW_UNSUPPORTED_TAP_CHANGERS_PARAMETER,
            CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE_PARAMETER,
            CONVERT_BOUNDARY_PARAMETER,
            CONVERT_SV_INJECTIONS_PARAMETER,
            CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER,
            ENSURE_ID_ALIAS_UNICITY_PARAMETER,
            NAMING_STRATEGY_PARAMETER,
            IMPORT_CONTROL_AREAS_PARAMETER,
            POWSYBL_TRIPLESTORE_PARAMETER,
            PROFILE_FOR_INITIAL_VALUES_SHUNT_SECTIONS_TAP_POSITIONS_PARAMETER,
            SOURCE_FOR_IIDM_ID_PARAMETER,
            STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION_PARAMETER,
            STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER,
            CREATE_ACTIVE_POWER_CONTROL_EXTENSION_PARAMETER,
            DECODE_ESCAPED_IDENTIFIERS_PARAMETER,
            CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE_PARAMETER,
            IMPORT_NODE_BREAKER_AS_BUS_BREAKER_PARAMETER,
            DISCONNECT_DANGLING_LINE_IF_BOUNDARY_SIDE_IS_DISCONNECTED_PARAMETER);

    private final Parameter boundaryLocationParameter;
    private final Parameter preProcessorsParameter;
    private final Parameter postProcessorsParameter;
    private final Map<String, CgmesImportPostProcessor> postProcessors;
    private final Map<String, CgmesImportPreProcessor> preProcessors;
    private final ParameterDefaultValueConfig defaultValueConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(CgmesImport.class);

    // TODO Allow this property to be configurable
    // Parameters of importers are only passed to importData method,
    // but to decide if we are importers also for CIM 14 files
    // we must implement the exists method, that has not access to parameters
    private boolean importCim14 = false;
}
