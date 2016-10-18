/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.parameters;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;

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

    private final ModuleConfig config ;

    public ParameterDefaultValueConfig() {
        config = PlatformConfig.defaultConfig().moduleExists(MODULE_NAME) ? PlatformConfig.defaultConfig().getModuleConfig(MODULE_NAME) : null;
    }

    public Object getValue(String format, Parameter parameter) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(parameter);
        String name = format + "_" + parameter.getName();
        switch (parameter.getType()) {
            case BOOLEAN: {
                boolean defaultValue = (Boolean) parameter.getDefaultValue();
                return config != null ? config.getBooleanProperty(name, defaultValue) : defaultValue;
            }
            case STRING: {
                String defaultValue = (String) parameter.getDefaultValue();
                return config != null ? config.getStringProperty(name, defaultValue) : defaultValue;
            }
            case STRING_LIST: {
                List<String> defaultValue = (List<String>) parameter.getDefaultValue();
                return config != null ? config.getStringListProperty(name, defaultValue) : defaultValue;
            }
            default:
                throw new AssertionError();
        }
    }

}
