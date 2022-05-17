/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.parameters;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * To override programmatic default value.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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

    public Object getValue(String format, Parameter parameter) {
        Objects.requireNonNull(parameter);
        switch (parameter.getType()) {
            case BOOLEAN:
                return getBooleanValue(format, parameter);
            case STRING:
                return getStringValue(format, parameter);
            case STRING_LIST:
                return getStringListValue(format, parameter);
            case DOUBLE:
                return getDoubleValue(format, parameter);
            default:
                throw new AssertionError();
        }
    }

    public boolean getBooleanValue(String format, Parameter parameter) {
        return getValue(format, (Boolean) parameter.getDefaultValue(), parameter, ModuleConfig::getOptionalBooleanProperty);
    }

    public String getStringValue(String format, Parameter parameter) {
        return getValue(format, (String) parameter.getDefaultValue(), parameter, ModuleConfig::getOptionalStringProperty);
    }

    public List<String> getStringListValue(String format, Parameter parameter) {
        return getValue(format, (List<String>) parameter.getDefaultValue(), parameter, ModuleConfig::getOptionalStringListProperty);
    }

    public double getDoubleValue(String format, Parameter parameter) {
        return getValue(format, (Double) parameter.getDefaultValue(), parameter,
            (moduleConfig, name) -> moduleConfig.getOptionalDoubleProperty(name).stream().boxed().findFirst());
    }

    private <T> T getValue(String format, T defaultValue, Parameter parameter, BiFunction<ModuleConfig, String, Optional<T>> supplier) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(parameter);
        ModuleConfig moduleConfig = getModuleConfig();

        if (moduleConfig != null) {
            for (String name : parameter.getNames()) {
                T value = supplier.apply(moduleConfig, name)
                        .orElseGet(() -> supplier.apply(moduleConfig, format + "_" + name).orElse(null));
                if (value != null) {
                    return value;
                }
            }
        }
        return defaultValue;
    }
}
