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
public class DynamicValueParameter extends Parameter {

    private final Object staticDefaultValue;

    public DynamicValueParameter(Parameter parameter, Object dynamicDefaultValue) {
        super(parameter.getNames(),
                parameter.getType(),
                parameter.getDescription(),
                dynamicDefaultValue,
                parameter.getPossibleValues(),
                parameter.getScope(),
                parameter.getCategoryKey());
        this.staticDefaultValue = parameter.getDefaultValue();
    }

    @Override
    public DefaultValueSource getDefaultValueSource() {
        return DefaultValueSource.CONFIGURATION;
    }

    public Object getStaticDefaultValue() {
        return staticDefaultValue;
    }

    public static List<Parameter> load(Collection<Parameter> parameters, String prefix, ParameterDefaultValueConfig defaultValueConfig) {
        return defaultValueConfig == null
                ? ImmutableList.copyOf(parameters)
                : parameters.stream()
                .map(param -> processParameters(defaultValueConfig.getValue(prefix, param), param))
                .toList();
    }

    private static Parameter processParameters(Object defaultValue, Parameter param) {
        if (defaultValue != param.getDefaultValue()) {
            return new DynamicValueParameter(param, defaultValue);
        }
        return param;
    }

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
