/**
 * Copyright (c) 2024, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.ModuleConfig;

import java.util.*;

/**
 * @author Antoine Bouhours {@literal <antoine.bouhours at rte-france.com>}
 */
public class ConfiguredParameter extends Parameter {

    private final Object baseDefaultValue;

    public ConfiguredParameter(Parameter parameter, Object configuredValue) {
        super(parameter.getNames(),
                parameter.getType(),
                parameter.getDescription(),
                configuredValue,
                parameter.getPossibleValues(),
                parameter.getScope(),
                parameter.getCategoryKey());
        this.baseDefaultValue = parameter.getDefaultValue();
    }

    public Object getBaseDefaultValue() {
        return baseDefaultValue;
    }

    /**
     * Processes a list of parameters, assigning default values from the provided configuration,
     * {@link ParameterDefaultValueConfig}, if applicable.
     * If {@code defaultValueConfig} is null, the method returns the original list of parameters.
     * Otherwise, each parameter is checked against the configuration. If a matching default
     * value is found in the configuration, it is applied to the parameter. If no matching
     * value is found, the parameter retains its base default value.
     *
     * @param parameters         The collection of parameters to be processed.
     * @param prefix             A string prefix used to fetch values from {@code defaultValueConfig}.
     * @param defaultValueConfig  The configuration providing default values for parameters, which may override base defaults.
     * @return A list of parameters with default values assigned, either from the configuration or the parameter's own base value.
     *
     * @throws PowsyblException If the parameter from the provided configuration is not correct.
     */
    public static List<Parameter> load(Collection<Parameter> parameters, String prefix, ParameterDefaultValueConfig defaultValueConfig) {
        if (defaultValueConfig == null) {
            return ImmutableList.copyOf(parameters);
        }

        return parameters.stream()
                .map(param -> processParameters(defaultValueConfig.getValue(prefix, param), param))
                .toList();
    }

    private static Parameter processParameters(Object configuredValue, Parameter param) {
        if (!Objects.equals(configuredValue, param.getDefaultValue())) {
            return createConfiguredParameter(configuredValue, param);
        }
        return param;
    }

    private static ConfiguredParameter createConfiguredParameter(Object configuredValue, Parameter param) {
        try {
            return new ConfiguredParameter(param, configuredValue);
        } catch (PowsyblException | IllegalArgumentException e) {
            throw new PowsyblException("Default value check failed for parameter: " + param.getName() + ", with value: " + configuredValue.toString(), e);
        }
    }

    /**
     * Processes a list of parameters, assigning default values from the provided configuration,
     * {@link ModuleConfig}, if applicable.
     * If {@code moduleConfig} is null, the method returns the original list of parameters.
     * Otherwise, each parameter is checked against the configuration. If a matching default
     * value is found in the configuration, it is applied to the parameter. If no matching
     * value is found, the parameter retains its base default value.
     *
     * @param parameters   The collection of parameters to be processed.
     * @param moduleConfig The configuration providing default values for parameters, which may override base defaults.
     * @return A list of parameters with default values assigned, either from the configuration or the parameter's own base value.
     *
     * @throws PowsyblException If the parameter from the provided configuration is not correct.
     */
    public static List<Parameter> load(Collection<Parameter> parameters, ModuleConfig moduleConfig) {
        if (moduleConfig == null) {
            return ImmutableList.copyOf(parameters);
        }

        return parameters.stream()
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
