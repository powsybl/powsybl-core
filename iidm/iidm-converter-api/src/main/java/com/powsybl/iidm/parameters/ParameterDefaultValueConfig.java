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
        String name = format + "_" + parameter.getName();
        ModuleConfig moduleConfig = getModuleConfig();
        switch (parameter.getType()) {
            case BOOLEAN: {
                boolean defaultValue = (Boolean) parameter.getDefaultValue();
                return moduleConfig != null ? moduleConfig.getBooleanProperty(name, defaultValue) : defaultValue;
            }
            case STRING: {
                String defaultValue = (String) parameter.getDefaultValue();
                return moduleConfig != null ? moduleConfig.getStringProperty(name, defaultValue) : defaultValue;
            }
            case STRING_LIST: {
                List<String> defaultValue = (List<String>) parameter.getDefaultValue();
                return moduleConfig != null ? moduleConfig.getStringListProperty(name, defaultValue) : defaultValue;
            }
            default:
                throw new AssertionError();
        }
    }

}
