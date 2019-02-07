/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm;

import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.ModuleConfigUtil;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class Converters {

    public static Object readParameter(String format, Properties parameters, Parameter configuredParameter) {
        return readParameter(format, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    public static Object readParameter(String format, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(configuredParameter);
        Objects.requireNonNull(defaultValueConfig);
        Object value = null;
        // priority on import parameter
        if (parameters != null) {
            MapModuleConfig moduleConfig = new MapModuleConfig(parameters);
            switch (configuredParameter.getType()) {
                case BOOLEAN:
                    value = ModuleConfigUtil.getOptionalBooleanProperty(moduleConfig, configuredParameter.getNames()).orElse(null);
                    break;
                case STRING:
                    value = ModuleConfigUtil.getOptionalStringProperty(moduleConfig, configuredParameter.getNames()).orElse(null);
                    break;
                case STRING_LIST:
                    value = ModuleConfigUtil.getOptionalStringListProperty(moduleConfig, configuredParameter.getNames()).orElse(null);
                    break;
                default:
                    throw new AssertionError("Unexpected ParameterType value: " + configuredParameter.getType());
            }
        }
        // if none, use configured parameters
        if (value == null) {
            value = defaultValueConfig.getValue(format, configuredParameter);
        }
        return value;
    }

    private Converters() {
    }
}
