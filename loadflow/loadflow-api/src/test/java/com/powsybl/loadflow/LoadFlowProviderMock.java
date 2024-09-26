/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest.DummyExtension;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest.DummySerializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(LoadFlowProvider.class)
public class LoadFlowProviderMock implements LoadFlowProvider {

    public static final String DOUBLE_PARAMETER_NAME = "parameterDouble";
    public static final String BOOLEAN_PARAMETER_NAME = "parameterBoolean";
    public static final String STRING_PARAMETER_NAME = "parameterString";

    public static final List<Parameter> PARAMETERS = List.of(new Parameter(DOUBLE_PARAMETER_NAME, ParameterType.DOUBLE, "a double parameter", 6.4),
                                                             new Parameter(BOOLEAN_PARAMETER_NAME, ParameterType.BOOLEAN, "a boolean parameter", false),
                                                             new Parameter(STRING_PARAMETER_NAME, ParameterType.STRING, "a string parameter", "yes", List.of("yes", "no")));

    @Override
    public CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingStateId, LoadFlowParameters parameters, ReportNode reportNode) {
        return CompletableFuture.completedFuture(new LoadFlowResultImpl(true, Collections.emptyMap(), ""));
    }

    @Override
    public String getName() {
        return "LoadFlowMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Optional<Class<? extends Extension<LoadFlowParameters>>> getSpecificParametersClass() {
        return Optional.of(DummyExtension.class);
    }

    @Override
    public Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.of(new DummySerializer());
    }

    @Override
    public Optional<Extension<LoadFlowParameters>> loadSpecificParameters(PlatformConfig config) {
        DummyExtension extension = new DummyExtension();
        config.getOptionalModuleConfig("dummy-extension").ifPresent(moduleConfig -> {
            extension.setParameterDouble(moduleConfig.getDoubleProperty(DOUBLE_PARAMETER_NAME, DummyExtension.PARAMETER_DOUBLE_DEFAULT_VALUE));
            extension.setParameterBoolean(moduleConfig.getBooleanProperty(BOOLEAN_PARAMETER_NAME, DummyExtension.PARAMETER_BOOLEAN_DEFAULT_VALUE));
            extension.setParameterString(moduleConfig.getStringProperty(STRING_PARAMETER_NAME, DummyExtension.PARAMETER_STRING_DEFAULT_VALUE));
        });
        return Optional.of(extension);
    }

    @Override
    public Optional<Extension<LoadFlowParameters>> loadSpecificParameters(Map<String, String> properties) {
        DummyExtension extension = new DummyExtension();
        updateSpecificParameters(extension, properties);
        return Optional.of(extension);
    }

    @Override
    public Map<String, String> createMapFromSpecificParameters(Extension<LoadFlowParameters> extension) {
        return Map.of(DOUBLE_PARAMETER_NAME, Double.toString(((DummyExtension) extension).getParameterDouble()),
                      BOOLEAN_PARAMETER_NAME, Boolean.toString(((DummyExtension) extension).isParameterBoolean()),
                      STRING_PARAMETER_NAME, ((DummyExtension) extension).getParameterString());
    }

    @Override
    public void updateSpecificParameters(Extension<LoadFlowParameters> extension, Map<String, String> properties) {
        Optional.ofNullable(properties.get(DOUBLE_PARAMETER_NAME))
                .ifPresent(prop -> ((DummyExtension) extension).setParameterDouble(Double.parseDouble(prop)));
        Optional.ofNullable(properties.get(BOOLEAN_PARAMETER_NAME))
                .ifPresent(prop -> ((DummyExtension) extension).setParameterBoolean(Boolean.parseBoolean(prop)));
        Optional.ofNullable(properties.get(STRING_PARAMETER_NAME))
                .ifPresent(prop -> ((DummyExtension) extension).setParameterString(prop));
    }

    @Override
    public List<Parameter> getSpecificParameters() {
        return PARAMETERS;
    }
}
