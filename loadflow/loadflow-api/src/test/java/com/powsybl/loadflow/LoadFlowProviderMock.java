/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest.DummyExtension;
import com.powsybl.loadflow.json.JsonLoadFlowParametersTest.DummySerializer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(LoadFlowProvider.class)
public class LoadFlowProviderMock implements LoadFlowProvider {

    public static final String DOUBLE_PARAMETER_NAME = "parameterDouble";
    public static final String INTEGER_PARAMETER_NAME = "parameterInteger";
    public static final String BOOLEAN_PARAMETER_NAME = "parameterBoolean";
    public static final String STRING_PARAMETER_NAME = "parameterString";
    public static final String NULLABLE_STRING_PARAMETER_NAME = "parameterNullableString";
    public static final String STRING_LIST_PARAMETER_NAME = "parameterStringList";

    public static final List<Parameter> PARAMETERS = List.of(new Parameter(DOUBLE_PARAMETER_NAME, ParameterType.DOUBLE, "a double parameter", DummyExtension.PARAMETER_DOUBLE_DEFAULT_VALUE),
                                                             new Parameter(INTEGER_PARAMETER_NAME, ParameterType.INTEGER, "an integer parameter", DummyExtension.PARAMETER_INTEGER_DEFAULT_VALUE),
                                                             new Parameter(BOOLEAN_PARAMETER_NAME, ParameterType.BOOLEAN, "a boolean parameter", DummyExtension.PARAMETER_BOOLEAN_DEFAULT_VALUE),
                                                             new Parameter(STRING_PARAMETER_NAME, ParameterType.STRING, "a string parameter", DummyExtension.PARAMETER_STRING_DEFAULT_VALUE, List.of("yes", "no")),
                                                             new Parameter(NULLABLE_STRING_PARAMETER_NAME, ParameterType.STRING, "a nullable string parameter", DummyExtension.PARAMETER_NULLABLE_STRING_DEFAULT_VALUE),
                                                             new Parameter(STRING_LIST_PARAMETER_NAME, ParameterType.STRING_LIST, " a string list paramter", DummyExtension.PARAMETER_STRING_LIST_DEFAULT_VALUE));

    @Override
    public CompletableFuture<LoadFlowResult> run(Network network, String workingStateId, LoadFlowRunParameters runParameters) {
        Executor executor = runParameters.getComputationManager().getExecutor();
        if (executor != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    // Simulate some processing
                }
            });
        }
        ReportNode reportNode = runParameters.getReportNode();
        if (reportNode != null) {
            reportNode.newReportNode()
                .withMessageTemplate("testLoadflow")
                .withUntypedValue("variantId", workingStateId)
                .add();
        }
        String logs = "";
        LoadFlowParameters parameters = runParameters.getLoadFlowParameters();
        if (parameters != null) {
            logs = "Loadflow parameters: " + parameters;
        }
        return CompletableFuture.completedFuture(new LoadFlowResultImpl(true, Collections.emptyMap(), logs));
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
        updateSpecificParameters(extension, config);
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
        HashMap result = new HashMap();
        DummyExtension ext = (DummyExtension) extension;

        result.put(DOUBLE_PARAMETER_NAME, Objects.toString(ext.getParameterDouble()));
        result.put(INTEGER_PARAMETER_NAME, Objects.toString(ext.getParameterInteger()));
        result.put(BOOLEAN_PARAMETER_NAME, Objects.toString(ext.isParameterBoolean()));
        if (ext.getParameterString() != null) {
            result.put(STRING_PARAMETER_NAME, ext.getParameterString());
        }
        if (ext.getParameterNullableString() != null) {
            result.put(NULLABLE_STRING_PARAMETER_NAME, ext.getParameterNullableString());
        }
        if (ext.getParameterStringList() != null) {
            result.put(STRING_LIST_PARAMETER_NAME, String.join(",", (ext.getParameterStringList()).stream().map(Object::toString).toList()));
        }

        return result;
    }

    @Override
    public void updateSpecificParameters(Extension<LoadFlowParameters> extension, Map<String, String> properties) {
        Optional.ofNullable(properties.get(DOUBLE_PARAMETER_NAME))
                .ifPresent(prop -> ((DummyExtension) extension).setParameterDouble(Double.parseDouble(prop)));
        Optional.ofNullable(properties.get(INTEGER_PARAMETER_NAME))
                .ifPresent(prop -> ((DummyExtension) extension).setParameterInteger(Integer.parseInt(prop)));
        Optional.ofNullable(properties.get(BOOLEAN_PARAMETER_NAME))
                .ifPresent(prop -> ((DummyExtension) extension).setParameterBoolean(Boolean.parseBoolean(prop)));
        Optional.ofNullable(properties.get(STRING_PARAMETER_NAME))
                .ifPresent(prop -> ((DummyExtension) extension).setParameterString(prop));
        Optional.ofNullable(properties.get(NULLABLE_STRING_PARAMETER_NAME))
                .ifPresent(prop -> ((DummyExtension) extension).setParameterNullableString(prop));
        Optional.ofNullable(properties.get(STRING_LIST_PARAMETER_NAME))
                .ifPresent(prop -> ((DummyExtension) extension).setParameterStringList(Arrays.asList(prop.split("[:,]"))));
    }

    @Override
    public void updateSpecificParameters(Extension<LoadFlowParameters> extension, PlatformConfig config) {
        config.getOptionalModuleConfig("dummy-extension").ifPresent(moduleConfig -> {
            moduleConfig.getOptionalDoubleProperty(DOUBLE_PARAMETER_NAME)
                    .ifPresent(((DummyExtension) extension)::setParameterDouble);
            moduleConfig.getOptionalIntProperty(INTEGER_PARAMETER_NAME)
                    .ifPresent(((DummyExtension) extension)::setParameterInteger);
            moduleConfig.getOptionalBooleanProperty(BOOLEAN_PARAMETER_NAME)
                    .ifPresent(((DummyExtension) extension)::setParameterBoolean);
            moduleConfig.getOptionalStringProperty(STRING_PARAMETER_NAME)
                    .ifPresent(((DummyExtension) extension)::setParameterString);
            moduleConfig.getOptionalStringProperty(NULLABLE_STRING_PARAMETER_NAME)
                    .ifPresent(((DummyExtension) extension)::setParameterNullableString);
            moduleConfig.getOptionalStringListProperty(STRING_LIST_PARAMETER_NAME)
                    .ifPresent(((DummyExtension) extension)::setParameterStringList);
        });
    }

    @Override
    public List<Parameter> getSpecificParameters() {
        return PARAMETERS;
    }

    @Override
    public Optional<ModuleConfig> getModuleConfig(PlatformConfig platformConfig) {
        return platformConfig.getOptionalModuleConfig("dummy-extension");
    }
}
