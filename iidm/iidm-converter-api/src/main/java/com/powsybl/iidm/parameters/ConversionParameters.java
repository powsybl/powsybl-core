/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.parameters;

import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.ModuleConfigUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class ConversionParameters {

    public static Object readParameter(String format, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        Objects.requireNonNull(configuredParameter);
        switch (configuredParameter.getType()) {
            case BOOLEAN:
                return readBooleanParameter(format, parameters, configuredParameter, defaultValueConfig);
            case STRING:
                return readStringParameter(format, parameters, configuredParameter, defaultValueConfig);
            case STRING_LIST:
                return readStringListParameter(format, parameters, configuredParameter, defaultValueConfig);
            case DOUBLE:
                return readDoubleParameter(format, parameters, configuredParameter, defaultValueConfig);
            default:
                throw new IllegalStateException("Unknown parameter type: " + configuredParameter.getType());
        }
    }

    public static Object readParameter(String format, Properties paramaters, Parameter configuredParameter) {
        return readParameter(format, paramaters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    public static boolean readBooleanParameter(String format, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return readParameter(format, parameters, configuredParameter, defaultValueConfig.getBooleanValue(format, configuredParameter), ModuleConfigUtil::getOptionalBooleanProperty);
    }

    public static boolean readBooleanParameter(String format, Properties parameters, Parameter configuredParameter) {
        return readBooleanParameter(format, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    public static String readStringParameter(String format, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return readParameter(format, parameters, configuredParameter, defaultValueConfig.getStringValue(format, configuredParameter), ModuleConfigUtil::getOptionalStringProperty);
    }

    public static String readStringParameter(String format, Properties parameters, Parameter configuredParameter) {
        return readStringParameter(format, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    public static List<String> readStringListParameter(String format, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return readParameter(format, parameters, configuredParameter, defaultValueConfig.getStringListValue(format, configuredParameter), ModuleConfigUtil::getOptionalStringListProperty);
    }

    public static List<String> readStringListParameter(String format, Properties parameters, Parameter configuredParameter) {
        return readStringListParameter(format, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    public static double readDoubleParameter(String format, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return readParameter(format, parameters, configuredParameter, defaultValueConfig.getDoubleValue(format, configuredParameter),
            (moduleConfig, names) -> ModuleConfigUtil.getOptionalDoubleProperty(moduleConfig, names).orElse(Double.NaN), value -> !Double.isNaN(value));
    }

    public static double readDoubleParameter(String format, Properties parameters, Parameter configuredParameter) {
        return readDoubleParameter(format, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    private static <T> T readParameter(String format, Properties parameters, Parameter configuredParameter, T defaultValue,
                                       BiFunction<ModuleConfig, List<String>, T> supplier, Predicate<T> isPresent) {
        Objects.requireNonNull(format);
        Objects.requireNonNull(configuredParameter);
        T value = null;
        // priority on passed parameters
        if (parameters != null) {
            MapModuleConfig moduleConfig = new MapModuleConfig(parameters);
            value = supplier.apply(moduleConfig, configuredParameter.getNames());

            // check that if possible values are configured, value is contained in possible values
            if (value != null
                    && configuredParameter.getPossibleValues() != null) {
                Parameter.checkPossibleValuesContainsValue(configuredParameter.getPossibleValues(), value,
                    v -> new IllegalArgumentException("Value " + v + " of parameter " + configuredParameter.getName() +
                                                      " is not contained in possible values " + configuredParameter.getPossibleValues()));
            }
        }
        // if none, use configured parameters
        if (isPresent.test(value)) {
            return value;
        }
        return defaultValue;
    }

    private static <T> T readParameter(String format, Properties parameters, Parameter configuredParameter, T defaultValue, BiFunction<ModuleConfig, List<String>, Optional<T>> supplier) {
        return readParameter(format, parameters, configuredParameter, defaultValue, (moduleConfig, strings) -> supplier.apply(moduleConfig, strings).orElse(null), Objects::nonNull);
    }

    private ConversionParameters() {
    }
}
