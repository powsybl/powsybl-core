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
            configCache = platformConfig.moduleExists(MODULE_NAME) ? platformConfig.getModuleConfig(MODULE_NAME) : null;
            init = true;
        }
        return configCache;
    }

    public Object getValue(String format, Parameter parameter) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(parameter);
        ModuleConfig moduleConfig = getModuleConfig();
        switch (parameter.getType()) {
            case BOOLEAN:
                return getBooleanProperty((boolean) parameter.getDefaultValue(), format, parameter.getNames(), moduleConfig);
            case STRING:
                return getStringProperty((String) parameter.getDefaultValue(), format, parameter.getNames(), moduleConfig);
            case STRING_LIST:
                return getStringListProperty((List<String>) parameter.getDefaultValue(), format, parameter.getNames(), moduleConfig);
            default:
                throw new AssertionError();
        }
    }

    private boolean getBooleanProperty(boolean defaultValue, String format, List<String> names, ModuleConfig moduleConfig) {
        if (moduleConfig != null) {
            for (String name : names) {
                Boolean value = moduleConfig.getOptionalBooleanProperty(name)
                        .orElseGet(() -> moduleConfig.getOptionalBooleanProperty(format + "_" + name).orElse(null));
                if (value != null) {
                    return value;
                }
            }
        }
        return defaultValue;
    }

    private String getStringProperty(String defaultValue, String format, List<String> names, ModuleConfig moduleConfig) {
        if (moduleConfig != null) {
            for (String name : names) {
                String value = moduleConfig.getOptionalStringProperty(name)
                        .orElseGet(() -> moduleConfig.getOptionalStringProperty(format + "_" + name).orElse(null));
                if (value != null) {
                    return value;
                }
            }
        }
        return defaultValue;
    }

    private List<String> getStringListProperty(List<String> defaultValue, String format, List<String> names, ModuleConfig moduleConfig) {
        if (moduleConfig != null) {
            for (String name : names) {
                List<String> value = moduleConfig.getOptionalStringListProperty(name)
                        .orElseGet(() -> moduleConfig.getOptionalStringListProperty(format + "_" + name).orElse(null));
                if (value != null) {
                    return value;
                }
            }
        }
        return defaultValue;
    }
}
