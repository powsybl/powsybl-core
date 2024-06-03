/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.MapModuleConfig;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.ModuleConfigUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class Parameter {

    private final List<String> names = new ArrayList<>();

    private final ParameterType type;

    private final String description;

    private final Object defaultValue;

    private final List<Object> possibleValues;

    private final ParameterScope scope;

    private final String categoryKey;

    public Parameter(String name, ParameterType type, String description, Object defaultValue,
                     List<Object> possibleValues, ParameterScope scope, String categoryKey) {
        names.add(Objects.requireNonNull(name));
        this.type = Objects.requireNonNull(type);
        this.description = Objects.requireNonNull(description);
        this.defaultValue = checkDefaultValue(type, defaultValue);
        this.possibleValues = checkPossibleValues(type, possibleValues, defaultValue);
        this.scope = scope;
        this.categoryKey = categoryKey;
    }

    public Parameter(String name, ParameterType type, String description, Object defaultValue,
                     List<Object> possibleValues, ParameterScope scope) {
        this(name, type, description, defaultValue, possibleValues, scope, null);
    }

    public Parameter(String name, ParameterType type, String description, Object defaultValue, ParameterScope scope,
                     String categoryKey) {
        this(name, type, description, defaultValue, null, scope, categoryKey);
    }

    public Parameter(String name, ParameterType type, String description, Object defaultValue,
                     List<Object> possibleValues) {
        this(name, type, description, defaultValue, possibleValues, ParameterScope.FUNCTIONAL);
    }

    public Parameter(String name, ParameterType type, String description, Object defaultValue) {
        this(name, type, description, defaultValue, null);
    }

    private static void checkValue(Class<?> typeClass, Object value) {
        if (value != null && !typeClass.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Bad default value type " + value.getClass() + ", " + typeClass + " was expected");
        }
    }

    private static void checkPossibleValuesContainsValue(List<Object> possibleValues, Object value,
                                                         Function<Object, IllegalArgumentException> exceptionCreator) {
        Objects.requireNonNull(possibleValues);
        Objects.requireNonNull(exceptionCreator);
        if (value != null) {
            if (value instanceof List) {
                for (Object valueElement : (List<?>) value) {
                    if (!possibleValues.contains(valueElement)) {
                        throw exceptionCreator.apply(valueElement);
                    }
                }
            } else {
                if (!possibleValues.contains(value)) {
                    throw exceptionCreator.apply(value);
                }
            }
        }
    }

    private static List<Object> checkPossibleValues(ParameterType type, List<Object> possibleValues, Object defaultValue) {
        if (possibleValues != null) {
            possibleValues.forEach(value -> checkValue(type.getElementClass(), value));
            checkPossibleValuesContainsValue(possibleValues, defaultValue, v -> {
                throw new IllegalArgumentException("Parameter possible values " + possibleValues + " should contain default value " + v);
            });
        }
        return possibleValues;
    }

    private static Object checkDefaultValue(ParameterType type, Object defaultValue) {
        checkValue(type.getTypeClass(), defaultValue);
        if (type == ParameterType.BOOLEAN && defaultValue == null) {
            throw new PowsyblException("With Boolean parameter you are not allowed to pass a null default value");
        }
        if (type == ParameterType.DOUBLE && defaultValue == null) {
            throw new PowsyblException("With Double parameter you are not allowed to pass a null default value");
        }
        if (type == ParameterType.INTEGER && defaultValue == null) {
            throw new PowsyblException("With Integer parameter you are not allowed to pass a null default value");
        }
        return defaultValue;
    }

    public static Object read(String prefix, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        Objects.requireNonNull(configuredParameter);
        switch (configuredParameter.getType()) {
            case BOOLEAN:
                return readBoolean(prefix, parameters, configuredParameter, defaultValueConfig);
            case STRING:
                return readString(prefix, parameters, configuredParameter, defaultValueConfig);
            case STRING_LIST:
                return readStringList(prefix, parameters, configuredParameter, defaultValueConfig);
            case DOUBLE:
                return readDouble(prefix, parameters, configuredParameter, defaultValueConfig);
            case INTEGER:
                return readInteger(prefix, parameters, configuredParameter, defaultValueConfig);
            default:
                throw new IllegalStateException("Unknown parameter type: " + configuredParameter.getType());
        }
    }

    public static Object read(String prefix, Properties paramaters, Parameter configuredParameter) {
        return read(prefix, paramaters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    public static boolean readBoolean(String prefix, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return read(parameters, configuredParameter, defaultValueConfig.getBooleanValue(prefix, configuredParameter), ModuleConfigUtil::getOptionalBooleanProperty);
    }

    public static boolean readBoolean(String prefix, Properties parameters, Parameter configuredParameter) {
        return readBoolean(prefix, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    public static String readString(String prefix, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return read(parameters, configuredParameter, defaultValueConfig.getStringValue(prefix, configuredParameter), ModuleConfigUtil::getOptionalStringProperty);
    }

    public static String readString(String prefix, Properties parameters, Parameter configuredParameter) {
        return readString(prefix, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    public static List<String> readStringList(String prefix, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return read(parameters, configuredParameter, defaultValueConfig.getStringListValue(prefix, configuredParameter), ModuleConfigUtil::getOptionalStringListProperty);
    }

    public static List<String> readStringList(String prefix, Properties parameters, Parameter configuredParameter) {
        return readStringList(prefix, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    public static double readDouble(String prefix, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return read(parameters, configuredParameter, defaultValueConfig.getDoubleValue(prefix, configuredParameter),
            (moduleConfig, names) -> ModuleConfigUtil.getOptionalDoubleProperty(moduleConfig, names).orElse(Double.NaN), value -> value != null && !Double.isNaN(value));
    }

    public static int readInteger(String prefix, Properties parameters, Parameter configuredParameter, ParameterDefaultValueConfig defaultValueConfig) {
        return read(parameters, configuredParameter, defaultValueConfig.getIntegerValue(prefix, configuredParameter),
            (moduleConfig, names) -> ModuleConfigUtil.getOptionalIntProperty(moduleConfig, names).stream().boxed().findFirst().orElse(null), Objects::nonNull);
    }

    public static double readDouble(String prefix, Properties parameters, Parameter configuredParameter) {
        return readDouble(prefix, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
    }

    private static <T> T read(Properties parameters, Parameter configuredParameter, T defaultValue,
                              BiFunction<ModuleConfig, List<String>, T> supplier, Predicate<T> isPresent) {
        Objects.requireNonNull(configuredParameter);
        T value = null;
        // priority on passed parameters
        if (parameters != null) {
            MapModuleConfig moduleConfig = new MapModuleConfig(parameters);
            value = supplier.apply(moduleConfig, configuredParameter.getNames());

            // check that if possible values are configured, value is contained in possible values
            if (value != null
                    && configuredParameter.getPossibleValues() != null) {
                checkPossibleValuesContainsValue(configuredParameter.getPossibleValues(), value,
                    v -> new IllegalArgumentException("Value " + v + " of parameter " + configuredParameter.getName() +
                                                      " is not contained in possible values " + configuredParameter.getPossibleValues()));
            }
        }
        // if none, use configured parameters
        if (value != null && isPresent.test(value)) {
            return value;
        }
        return defaultValue;
    }

    private static <T> T read(Properties parameters, Parameter configuredParameter, T defaultValue, BiFunction<ModuleConfig, List<String>, Optional<T>> supplier) {
        return read(parameters, configuredParameter, defaultValue, (moduleConfig, strings) -> supplier.apply(moduleConfig, strings).orElse(null), Objects::nonNull);
    }

    public Parameter addAdditionalNames(String... names) {
        Objects.requireNonNull(names);
        this.names.addAll(Arrays.asList(names));
        return this;
    }

    public String getName() {
        return names.get(0);
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    public ParameterType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public List<Object> getPossibleValues() {
        return possibleValues;
    }

    public ParameterScope getScope() {
        return scope;
    }

    public String getCategoryKey() {
        return categoryKey;
    }
}
