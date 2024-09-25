/**
 * Copyright (c) 2024, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.config.ModuleConfig;

import java.util.*;

/**
 * @author Antoine Bouhours {@literal <antoine.bouhours at rte-france.com>}
 */
public class ConfiguredParameter extends Parameter {

    private final Object configuredValue;

    public ConfiguredParameter(Parameter parameter, Object configuredValue) {
        super(parameter.getNames(),
                parameter.getType(),
                parameter.getDescription(),
                parameter.getDefaultValue(),
                parameter.getPossibleValues(),
                parameter.getScope(),
                parameter.getCategoryKey());
        this.configuredValue = checkDefaultValue(parameter.getType(), configuredValue);
    }

    @Override
    public ParameterSourceType getParameterSourceType() {
        return ParameterSourceType.CONFIGURED_PARAMETER;
    }

    public Object getConfiguredValue() {
        return configuredValue;
    }

    /**
     * Loads a list of parameters and dynamically assigns default values from the provided configuration {@link ParameterDefaultValueConfig}.
     * If the defaultValueConfig is null, the method returns the original list of parameters.
     * Otherwise, each parameter is processed with a potential dynamic default value derived from the configuration.
     *
     * @param parameters        The collection of parameters to be processed.
     * @param prefix            The prefix used to fetch values from the defaultValueConfig.
     * @param defaultValueConfig The configuration containing default values for parameters.
     * @return A list of parameters with dynamically assigned default values.
     */
    public static List<Parameter> load(Collection<Parameter> parameters, String prefix, ParameterDefaultValueConfig defaultValueConfig) {
        return defaultValueConfig == null
                ? ImmutableList.copyOf(parameters)
                : parameters.stream()
                .map(param -> processParameters(defaultValueConfig.getValue(prefix, param), param))
                .toList();
    }

    private static Parameter processParameters(Object configuredValue, Parameter param) {
        if (configuredValue != param.getDefaultValue()) {
            return new ConfiguredParameter(param, configuredValue);
        }
        return param;
    }

    /**
     * Loads a list of parameters and dynamically assigns default values from the provided configuration {@link ModuleConfig}.
     * If the moduleConfig is null, the method returns the original list of parameters.
     * Otherwise, each parameter is processed with a potential dynamic default value derived from the configuration.
     *
     * @param parameters  The collection of parameters to be processed.
     * @param moduleConfig The module configuration containing default values for parameters.
     * @return A list of parameters with dynamically assigned default values.
     */
    public static List<Parameter> load(Collection<Parameter> parameters, ModuleConfig moduleConfig) {
        return moduleConfig == null
                ? ImmutableList.copyOf(parameters)
                : parameters.stream()
                .map(param -> processParameters(getValueFromModuleConfig(param, moduleConfig), param))
                .toList();
    }

    private static Object getValueFromModuleConfig(Parameter param, ModuleConfig moduleConfig) {
        Object moduleConfigDefaultValue = switch (param.getType()) {
            case STRING -> moduleConfig.getOptionalStringProperty(param.getName()).orElse(null);
            case BOOLEAN -> moduleConfig.getOptionalBooleanProperty(param.getName()).orElse(null);
            case INTEGER -> moduleConfig.getOptionalIntProperty(param.getName()).stream().boxed().findFirst().orElse(null);
            case STRING_LIST -> moduleConfig.getOptionalStringListProperty(param.getName()).orElse(null);
            case DOUBLE -> moduleConfig.getOptionalDoubleProperty(param.getName()).stream().boxed().findFirst().orElse(null);
        };
        return moduleConfigDefaultValue != null ? moduleConfigDefaultValue : param.getDefaultValue();
    }
}
