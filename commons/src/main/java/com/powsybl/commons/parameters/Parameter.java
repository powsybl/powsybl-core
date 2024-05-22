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

    private final ParameterUsageRestrictions usageRestrictions;

    private final String categoryKey;

    private final String unitSymbol;

    private Parameter(String name, ParameterType type, String description, Object defaultValue,
                      List<Object> possibleValues, ParameterScope scope, ParameterUsageRestrictions usageRestrictions,
                      String categoryKey, String unitSymbol) {
        names.add(Objects.requireNonNull(name));
        this.type = Objects.requireNonNull(type);
        this.description = Objects.requireNonNull(description);
        this.defaultValue = checkDefaultValue(type, defaultValue);
        this.possibleValues = checkPossibleValues(type, possibleValues, defaultValue);
        this.scope = Objects.requireNonNull(scope);
        this.usageRestrictions = usageRestrictions;
        this.categoryKey = categoryKey;
        this.unitSymbol = unitSymbol;
    }

    public Parameter(String name, ParameterType type, String description, Object defaultValue,
                     List<Object> possibleValues, ParameterScope scope) {
        this(name, type, description, defaultValue, possibleValues, scope, null, null, null);
    }

    public Parameter(String name, ParameterType type, String description, Object defaultValue,
                     List<Object> possibleValues) {
        this(name, type, description, defaultValue, possibleValues, ParameterScope.FUNCTIONAL);
    }

    public Parameter(String name, ParameterType type, String description, Object defaultValue) {
        this(name, type, description, defaultValue, null);
    }

    public Builder builder(String name, ParameterType type, String description, Object defaultValue) {
        return new Builder(name, type, description, defaultValue);
    }

    public static final class Builder {
        private final String name;
        private final ParameterType type;
        private final String description;
        private final Object defaultValue;
        private List<Object> possibleValues = null;
        private ParameterScope scope = ParameterScope.FUNCTIONAL;
        private ParameterUsageRestrictions usageRestrictions = null;
        private String categoryKey = null;
        private String unitSymbol = null;

        private Builder(String name, ParameterType type, String description, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.defaultValue = defaultValue;
        }

        public Builder withPossibleValues(List<Object> possibleValues) {
            this.possibleValues = possibleValues;
            return this;
        }

        public Builder withScope(ParameterScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder withUsageRestrictions(ParameterUsageRestrictions usageRestrictions) {
            this.usageRestrictions = usageRestrictions;
            return this;
        }

        public Builder withCategoryKey(String categoryKey) {
            this.categoryKey = categoryKey;
            return this;
        }

        public Builder withUnitSymbol(String unitSymbol) {
            this.unitSymbol = unitSymbol;
            return this;
        }

        public Parameter build() {
            return new Parameter(name, type, description, defaultValue, possibleValues, scope, usageRestrictions,
                    categoryKey, unitSymbol);
        }
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
        return switch (configuredParameter.getType()) {
            case BOOLEAN -> readBoolean(prefix, parameters, configuredParameter, defaultValueConfig);
            case STRING -> readString(prefix, parameters, configuredParameter, defaultValueConfig);
            case STRING_LIST -> readStringList(prefix, parameters, configuredParameter, defaultValueConfig);
            case DOUBLE -> readDouble(prefix, parameters, configuredParameter, defaultValueConfig);
            case INTEGER -> readInteger(prefix, parameters, configuredParameter, defaultValueConfig);
        };
    }

    public static Object read(String prefix, Properties parameters, Parameter configuredParameter) {
        return read(prefix, parameters, configuredParameter, ParameterDefaultValueConfig.INSTANCE);
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
            Optional<List<Object>> possibleValues = configuredParameter.getPossibleValues();
            if (value != null && possibleValues.isPresent()) {
                checkPossibleValuesContainsValue(possibleValues.get(), value,
                    v -> new IllegalArgumentException("Value " + v + " of parameter " + configuredParameter.getName() +
                                                      " is not contained in possible values " + possibleValues.get()));
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

    public Optional<List<Object>> getPossibleValues() {
        return Optional.ofNullable(possibleValues);
    }

    public ParameterScope getScope() {
        return scope;
    }

    public Optional<ParameterUsageRestrictions> getUsageRestrictions() {
        return Optional.ofNullable(usageRestrictions);
    }

    public Optional<String> getCategoryKey() {
        return Optional.ofNullable(categoryKey);
    }

    public Optional<String> getUnitSymbol() {
        return Optional.ofNullable(unitSymbol);
    }
}
