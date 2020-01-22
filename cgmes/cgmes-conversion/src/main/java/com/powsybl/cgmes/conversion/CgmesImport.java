/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.CgmesOnDataSource;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Importer.class)
public class CgmesImport implements Importer {

    public CgmesImport(PlatformConfig platformConfig, List<CgmesImportPostProcessor> postProcessors) {
        this.defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
        this.postProcessors = Objects.requireNonNull(postProcessors).stream()
                .collect(Collectors.toMap(CgmesImportPostProcessor::getName, e -> e));
        // Boundary location parameter can not be static
        // because we want its default value
        // to depend on the received platformConfig
        this.boundaryLocationParameter = new Parameter(
                BOUNDARY_LOCATION,
                ParameterType.STRING,
                "The location of boundary files",
                platformConfig.getConfigDir().resolve(FORMAT).resolve("boundary").toString());
    }

    public CgmesImport(PlatformConfig platformConfig) {
        this(platformConfig, new ServiceLoaderCache<>(CgmesImportPostProcessor.class).getServices());
    }

    public CgmesImport(List<CgmesImportPostProcessor> postProcessors) {
        this(PlatformConfig.defaultConfig(), postProcessors);
    }

    public CgmesImport() {
        this(PlatformConfig.defaultConfig());
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> allParams = new ArrayList<>(STATIC_PARAMETERS);
        allParams.add(boundaryLocationParameter);
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
    public Network importData(ReadOnlyDataSource ds, NetworkFactory networkFactory, Properties p) {
        CgmesModel cgmes = CgmesModelFactory.create(ds, boundary(p), tripleStore(p));
        return new Conversion(cgmes, config(p), activatedPostProcessors(p), networkFactory).convert();
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
        Path loc = Paths.get(
                ConversionParameters.readStringParameter(
                        getFormat(),
                        p,
                        boundaryLocationParameter,
                        defaultValueConfig));
        // Check that the Data Source has valid CGMES names
        ReadOnlyDataSource ds = new GenericReadOnlyDataSource(loc, DataSourceUtil.getBaseName(loc));
        if ((new CgmesOnDataSource(ds)).names().isEmpty()) {
            return null;
        }
        return ds;
    }

    private String tripleStore(Properties p) {
        return ConversionParameters.readStringParameter(
                getFormat(),
                p,
                POWSYBL_TRIPLESTORE_PARAMETER,
                defaultValueConfig);
    }

    private Conversion.Config config(Properties p) {
        return new Conversion.Config()
                .setAllowUnsupportedTapChangers(
                        ConversionParameters.readBooleanParameter(
                                getFormat(),
                                p,
                                ALLOW_UNSUPPORTED_TAP_CHANGERS_PARAMETER,
                                defaultValueConfig))
                .setChangeSignForShuntReactivePowerFlowInitialState(
                        ConversionParameters.readBooleanParameter(
                                getFormat(),
                                p,
                                CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE_PARAMETER,
                                defaultValueConfig))
                .setConvertBoundary(
                        ConversionParameters.readBooleanParameter(
                                getFormat(),
                                p,
                                CONVERT_BOUNDARY_PARAMETER,
                                defaultValueConfig))
                .setConvertSvInjections(
                        ConversionParameters.readBooleanParameter(
                                getFormat(),
                                p,
                                CONVERT_SV_INJECTIONS_PARAMETER,
                                defaultValueConfig))
                .setCreateBusbarSectionForEveryConnectivityNode(
                        ConversionParameters.readBooleanParameter(
                                getFormat(),
                                p,
                                CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER,
                                defaultValueConfig))
                .setProfileUsedForInitialStateValues(
                        ConversionParameters.readStringParameter(
                                getFormat(),
                                p,
                                PROFILE_USED_FOR_INITIAL_STATE_VALUES_PARAMETER,
                                defaultValueConfig))
                .setStoreCgmesModelAsNetworkExtension(
                        ConversionParameters.readBooleanParameter(
                                getFormat(),
                                p,
                                STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER,
                                defaultValueConfig))
                .setStoreCgmesConversionContextAsNetworkExtension(
                        ConversionParameters.readBooleanParameter(
                                getFormat(),
                                p,
                                STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION_PARAMETER,
                                defaultValueConfig))
                .setUseNewTransformerConversion(
                        ConversionParameters.readBooleanParameter(
                                getFormat(),
                                p,
                                TEMP_USE_NEW_TRANSFORMER_CONVERSION_PARAMETER,
                                defaultValueConfig));
    }

    private List<CgmesImportPostProcessor> activatedPostProcessors(Properties p) {
        return ConversionParameters
                .readStringListParameter(getFormat(), p, POST_PROCESSORS_PARAMETER, defaultValueConfig)
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
    public static final String CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE = "iidm.import.cgmes.create-busbar-section-for-every-connectivity-node";
    public static final String POST_PROCESSORS = "iidm.import.cgmes.post-processors";
    public static final String POWSYBL_TRIPLESTORE = "iidm.import.cgmes.powsybl-triplestore";
    public static final String PROFILE_USED_FOR_INITIAL_STATE_VALUES = "iidm.import.cgmes.profile-used-for-initial-state-values";
    public static final String STORE_CGMES_MODEL_AS_NETWORK_EXTENSION = "iidm.import.cgmes.store-cgmes-model-as-network-extension";
    public static final String STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION = "iidm.import.cgmes.store-cgmes-conversion-context-as-network-extension";

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
    private static final Parameter POST_PROCESSORS_PARAMETER = new Parameter(
            POST_PROCESSORS,
            ParameterType.STRING_LIST,
            "Post processors",
            Collections.emptyList());
    private static final Parameter POWSYBL_TRIPLESTORE_PARAMETER = new Parameter(
            POWSYBL_TRIPLESTORE,
            ParameterType.STRING,
            "The triplestore used during the import",
            TripleStoreFactory.defaultImplementation())
            .addAdditionalNames("powsyblTripleStore");
    private static final Parameter PROFILE_USED_FOR_INITIAL_STATE_VALUES_PARAMETER = new Parameter(
            PROFILE_USED_FOR_INITIAL_STATE_VALUES,
            ParameterType.STRING,
            "Profile used for initial state values",
            "SSH");
    private static final Parameter STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION_PARAMETER = new Parameter(
            STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION,
            ParameterType.BOOLEAN,
            "Store the CGMES-IIDM terminal mapping as a network extension",
            Boolean.FALSE);
    private static final Parameter STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER = new Parameter(
            STORE_CGMES_MODEL_AS_NETWORK_EXTENSION,
            ParameterType.BOOLEAN,
            "Store the initial CGMES model as a network extension",
            Boolean.TRUE)
            .addAdditionalNames("storeCgmesModelAsNetworkExtension");
    private static final Parameter TEMP_USE_NEW_TRANSFORMER_CONVERSION_PARAMETER = new Parameter(
            "tempUseNewTransformerConversion",
            ParameterType.BOOLEAN,
            "Temporal parameter to configure which version of transformer conversion to use",
            Boolean.TRUE);

    private static final List<Parameter> STATIC_PARAMETERS = ImmutableList.of(
            ALLOW_UNSUPPORTED_TAP_CHANGERS_PARAMETER,
            CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE_PARAMETER,
            CONVERT_BOUNDARY_PARAMETER,
            CONVERT_SV_INJECTIONS_PARAMETER,
            CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER,
            POST_PROCESSORS_PARAMETER,
            POWSYBL_TRIPLESTORE_PARAMETER,
            STORE_CGMES_CONVERSION_CONTEXT_AS_NETWORK_EXTENSION_PARAMETER,
            STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER,
            TEMP_USE_NEW_TRANSFORMER_CONVERSION_PARAMETER);

    private final Parameter boundaryLocationParameter;
    private final Map<String, CgmesImportPostProcessor> postProcessors;
    private final ParameterDefaultValueConfig defaultValueConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(CgmesImport.class);

    // TODO Allow this property to be configurable
    // Parameters of importers are only passed to importData method,
    // but to decide if we are importers also for CIM 14 files
    // we must implement the exists method, that has not access to parameters
    private boolean importCim14 = false;
}
