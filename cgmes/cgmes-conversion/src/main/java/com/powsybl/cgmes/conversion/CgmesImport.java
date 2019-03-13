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
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
@AutoService(Importer.class)
public class CgmesImport implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CgmesImport.class);

    public static final String CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE
            = "iidm.import.cgmes.change-sign-for-shunt-reactive-power-flow-initial-state";
    public static final String CONVERT_BOUNDARY = "iidm.import.cgmes.convert-boundary";
    public static final String CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE
            = "iidm.import.cgmes.create-busbar-section-for-every-connectivity-node";
    public static final String POWSYBL_TRIPLESTORE = "iidm.import.cgmes.powsybl-triplestore";
    public static final String STORE_CGMES_MODEL_AS_NETWORK_EXTENSION = "iidm.import.cgmes.store-cgmes-model-as-network-extension";
    public static final String POST_PROCESSORS = "iidm.import.cgmes.post-processors";

    public static final Parameter CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE_PARAMETER
            = new Parameter(CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE, ParameterType.BOOLEAN, "Change the sign of the reactive power flow for shunt in initial state", Boolean.FALSE)
                .addAdditionalNames("changeSignForShuntReactivePowerFlowInitialState");
    public static final Parameter CONVERT_BOUNDARY_PARAMETER = new Parameter(CONVERT_BOUNDARY, ParameterType.BOOLEAN, "Convert boundary during import", Boolean.FALSE)
            .addAdditionalNames("convertBoundary");
    public static final Parameter CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER
            = new Parameter(CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE, ParameterType.BOOLEAN, "Create busbar section for every connectivity node", Boolean.FALSE)
            .addAdditionalNames("createBusbarSectionForEveryConnectivityNode");
    public static final Parameter POWSYBL_TRIPLESTORE_PARAMETER = new Parameter(POWSYBL_TRIPLESTORE, ParameterType.STRING, "The triplestore used during the import", TripleStoreFactory.defaultImplementation())
            .addAdditionalNames("powsyblTripleStore");
    public static final Parameter STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER = new Parameter(STORE_CGMES_MODEL_AS_NETWORK_EXTENSION, ParameterType.BOOLEAN, "Store the initial CGMES model as a network extension", Boolean.TRUE)
            .addAdditionalNames("storeCgmesModelAsNetworkExtension");
    public static final Parameter POST_PROCESSORS_PARAMETER = new Parameter(POST_PROCESSORS, ParameterType.STRING_LIST, "Post processors", Collections.emptyList());

    private static final List<Parameter> PARAMETERS = ImmutableList.of(CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE_PARAMETER,
                                                                       CONVERT_BOUNDARY_PARAMETER,
                                                                       CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER,
                                                                       POWSYBL_TRIPLESTORE_PARAMETER,
                                                                       STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER,
                                                                       POST_PROCESSORS_PARAMETER);

    private final Map<String, CgmesImportPostProcessor> postProcessors;

    private final ParameterDefaultValueConfig defaultValueConfig;

    public CgmesImport(PlatformConfig platformConfig, List<CgmesImportPostProcessor> postProcessors) {
        this.defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
        this.postProcessors = Objects.requireNonNull(postProcessors).stream().collect(Collectors.toMap(CgmesImportPostProcessor::getName, e -> e));
    }

    public CgmesImport(PlatformConfig platformConfig) {
        this(platformConfig, new ServiceLoaderCache<>(CgmesImportPostProcessor.class).getServices());
    }

    public CgmesImport() {
        this(PlatformConfig.defaultConfig());
    }

    @Override
    public List<Parameter> getParameters() {
        return PARAMETERS;
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
    public Network importData(ReadOnlyDataSource ds, Properties p) {
        String tripleStoreImpl = ConversionParameters.readStringParameter(getFormat(), p, POWSYBL_TRIPLESTORE_PARAMETER, defaultValueConfig);
        CgmesModel cgmes = CgmesModelFactory.create(ds, tripleStoreImpl);

        Conversion.Config config = new Conversion.Config();
        config.setChangeSignForShuntReactivePowerFlowInitialState(ConversionParameters.readBooleanParameter(getFormat(), p, CHANGE_SIGN_FOR_SHUNT_REACTIVE_POWER_FLOW_INITIAL_STATE_PARAMETER, defaultValueConfig));
        config.setConvertBoundary(ConversionParameters.readBooleanParameter(getFormat(), p, CONVERT_BOUNDARY_PARAMETER, defaultValueConfig));
        config.setCreateBusbarSectionForEveryConnectivityNode(ConversionParameters.readBooleanParameter(getFormat(), p, CREATE_BUSBAR_SECTION_FOR_EVERY_CONNECTIVITY_NODE_PARAMETER, defaultValueConfig));

        List<CgmesImportPostProcessor> activatedPostProcessors = ConversionParameters.readStringListParameter(getFormat(), p, POST_PROCESSORS_PARAMETER, defaultValueConfig)
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

        Network network = new Conversion(cgmes, config, activatedPostProcessors).convert();

        boolean storeCgmesModelAsNetworkExtension = ConversionParameters.readBooleanParameter(getFormat(), p, STORE_CGMES_MODEL_AS_NETWORK_EXTENSION_PARAMETER, defaultValueConfig);
        if (storeCgmesModelAsNetworkExtension) {
            // Store a reference to the original CGMES model inside the IIDM network
            // We could also add listeners to be aware of changes in IIDM data
            network.addExtension(CgmesModelExtension.class, new CgmesModelExtension(cgmes));
        }

        return network;
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

    private void copyStream(ReadOnlyDataSource from, DataSource to, String fromName, String toName) throws IOException {
        if (from.exists(fromName)) {
            try (InputStream is = from.newInputStream(fromName);
                    OutputStream os = to.newOutputStream(toName, false)) {
                ByteStreams.copy(is, os);
            }
        }
    }

    private static final String FORMAT = "CGMES";

    // TODO Allow this property to be configurable
    // Parameters of importers are only passed to importData method,
    // but to decide if we are importers also for CIM 14 files
    // we must implement the exists method, that has not access to parameters
    private boolean importCim14 = false;
}
