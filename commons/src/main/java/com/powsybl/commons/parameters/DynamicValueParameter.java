/**
 * Copyright (c) 2024, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

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
                checkDefaultValue(parameter.getType(), dynamicDefaultValue),
                parameter.getPossibleValues(),
                parameter.getScope(),
                parameter.getCategoryKey());
        this.staticDefaultValue = parameter.getDefaultValue();
    }

    public Object getStaticDefaultValue() {
        return staticDefaultValue;
    }

    public static List<Parameter> load(Collection<Parameter> parameters, String prefix, ParameterDefaultValueConfig defaultValueConfig) {
        return defaultValueConfig == null
                ? new ArrayList<>(parameters)
                : parameters.stream().map(param -> (Parameter) new DynamicValueParameter(param, defaultValueConfig.getValue(prefix, param))).toList();
    }

    public static List<Parameter> load(Collection<Parameter> parameters, ModuleConfig moduleConfig) {
        return moduleConfig == null
                ? new ArrayList<>(parameters)
                : parameters.stream().map(param -> (Parameter) new DynamicValueParameter(param, getPropertyFromModuleConfig(param, moduleConfig))).toList();
    }

    private static Object getPropertyFromModuleConfig(Parameter param, ModuleConfig moduleConfig) {
        return switch (param.getType()) {
            case STRING -> moduleConfig.getStringProperty(param.getName());
            case BOOLEAN -> moduleConfig.getBooleanProperty(param.getName());
            case INTEGER -> moduleConfig.getIntProperty(param.getName());
            case STRING_LIST -> moduleConfig.getStringListProperty(param.getName());
            case DOUBLE -> moduleConfig.getDoubleProperty(param.getName());
        };
    }
}
