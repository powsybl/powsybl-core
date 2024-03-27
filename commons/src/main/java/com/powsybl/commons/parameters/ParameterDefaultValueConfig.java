/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * To override programmatic default value.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ParameterDefaultValueConfig {

    private static final String MODULE_NAME = "import-export-parameters-default-value";

    public static final ParameterDefaultValueConfig INSTANCE = new ParameterDefaultValueConfig();

    private PlatformConfig platformConfig;

    private boolean init = false;

    private ModuleConfig configCache;

    public ParameterDefaultValueConfig() {
        this(null);
    }

    public ParameterDefaultValueConfig(PlatformConfig platformConfig) {
        this.platformConfig = platformConfig;
    }

    private synchronized ModuleConfig getModuleConfig() {
        if (!init) {
            if (platformConfig == null) {
                platformConfig = PlatformConfig.defaultConfig();
            }
            configCache = platformConfig.getOptionalModuleConfig(MODULE_NAME).orElse(null);
            init = true;
        }
        return configCache;
    }

    public Object getValue(String prefix, Parameter parameter) {
        Objects.requireNonNull(parameter);
        switch (parameter.getType()) {
            case BOOLEAN:
                return getBooleanValue(prefix, parameter);
            case STRING:
                return getStringValue(prefix, parameter);
            case STRING_LIST:
                return getStringListValue(prefix, parameter);
            case DOUBLE:
                return getDoubleValue(prefix, parameter);
            case INTEGER:
                return getIntegerValue(prefix, parameter);
            default:
                throw new IllegalStateException("Unsupported parameter type: " + parameter.getType());
        }
    }

    public boolean getBooleanValue(String prefix, Parameter parameter) {
        return getValue(prefix, (Boolean) parameter.getDefaultValue(), parameter, ModuleConfig::getOptionalBooleanProperty);
    }

    public String getStringValue(String prefix, Parameter parameter) {
        return getValue(prefix, (String) parameter.getDefaultValue(), parameter, ModuleConfig::getOptionalStringProperty);
    }

    public List<String> getStringListValue(String prefix, Parameter parameter) {
        return getValue(prefix, (List<String>) parameter.getDefaultValue(), parameter, ModuleConfig::getOptionalStringListProperty);
    }

    public double getDoubleValue(String prefix, Parameter parameter) {
        return getValue(prefix, (Double) parameter.getDefaultValue(), parameter,
            (moduleConfig, name) -> moduleConfig.getOptionalDoubleProperty(name).stream().boxed().findFirst());
    }

    public int getIntegerValue(String prefix, Parameter parameter) {
        return getValue(prefix, (Integer) parameter.getDefaultValue(), parameter,
            (moduleConfig, name) -> moduleConfig.getOptionalIntProperty(name).stream().boxed().findFirst());
    }

    private <T> T getValue(String prefix, T defaultValue, Parameter parameter, BiFunction<ModuleConfig, String, Optional<T>> valueSupplier) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(parameter);
        ModuleConfig moduleConfig = getModuleConfig();

        if (moduleConfig != null) {
            for (String name : parameter.getNames()) {
                T value = valueSupplier.apply(moduleConfig, name)
                        .orElseGet(() -> valueSupplier.apply(moduleConfig, prefix + "_" + name).orElse(null));
                if (value != null) {
                    return value;
                }
            }
        }
        return defaultValue;
    }
}
