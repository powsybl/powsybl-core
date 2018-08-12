/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedClassNotFoundException;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapModuleConfig implements ModuleConfig {

    private final Map<Object, Object> properties;

    private final FileSystem fs;

    public MapModuleConfig(FileSystem fs) {
        this(new HashMap<>(), fs);
    }

    public MapModuleConfig(Map<Object, Object> properties) {
        this(properties, FileSystems.getDefault());
    }

    public MapModuleConfig(Map<Object, Object> properties, FileSystem fs) {
        this.properties = properties;
        this.fs = fs;
    }

    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.entrySet().stream()
                .map(Map.Entry::getKey)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    /*
     * Environment variables substitution
     */
    private static String substitureEnvVar(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("$HOME", System.getProperty("user.home"));
    }

    private static PowsyblException createPropertyNotSetException(String name) {
        return new PowsyblException("Property " + name + " is not set");
    }

    private static PowsyblException createUnexpectedPropertyTypeException(String name, Class<?> type, Class<?>[] expectedTypes) {
        return new PowsyblException("Unexpected value type " + type.getName()
                + " for property " + name + ", " + Arrays.toString(expectedTypes) + " is expected ");
    }

    private static PowsyblException createPropertyIsNotException(String name, String what, Exception e) {
        throw new PowsyblException("Property " + name + " is not " + what, e);
    }

    public Optional<String> getOptionalStringProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof String)) {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {String.class});
        }
        return Optional.of((String) value).map(MapModuleConfig::substitureEnvVar);
    }

    @Override
    public String getStringProperty(String name) {
        return getOptionalStringProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public String getStringProperty(String name, String defaultValue) {
        return getOptionalStringProperty(name).orElse(defaultValue);
    }

    public void setStringProperty(String name, String value) {
        properties.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
    }

    public Optional<List<String>> getOptionalStringListProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return Optional.empty();
        } else {
            if (value instanceof String) {
                String trimmedString = ((String) value).trim();
                if (trimmedString.isEmpty()) {
                    return Optional.of(Collections.emptyList());
                } else {
                    return Optional.of(Arrays.asList((substitureEnvVar(trimmedString)).split("[:,]")));
                }
            } else if (value instanceof List) {
                return Optional.of(((List<String>) value).stream().map(MapModuleConfig::substitureEnvVar).collect(Collectors.toList()));
            } else {
                throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {String.class, List.class});
            }
        }
    }

    @Override
    public List<String> getStringListProperty(String name) {
        return getOptionalStringListProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public List<String> getStringListProperty(String name, List<String> defaultValue) {
        return getOptionalStringListProperty(name).orElse(defaultValue);
    }

    public void setStringListProperty(String name, List<String> value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        properties.put(name, value);
    }

    @Override
    public <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz) {
        return Enum.valueOf(clazz, getStringProperty(name));
    }

    @Override
    public <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz, E defaultValue) {
        return Enum.valueOf(clazz, getStringProperty(name, Objects.requireNonNull(defaultValue).name()));
    }

    @Override
    public <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz) {
        List<String> strings = getStringListProperty(name);
        return strings.stream().map(s -> Enum.valueOf(clazz, s)).collect(Collectors.toSet());
    }

    @Override
    public <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz, Set<E> defaultValue) {
        List<String> strings = getStringListProperty(name, null);
        if (strings == null) {
            return defaultValue;
        }
        return strings.stream().map(s -> Enum.valueOf(clazz, s)).collect(Collectors.toSet());
    }

    @Override
    public Optional<Integer> getOptionalIntegerProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Integer) {
            return Optional.of((Integer) value);
        } else if (value instanceof String) {
            try {
                return Optional.of(Integer.parseInt((String) value));
            } catch (NumberFormatException e) {
                throw createPropertyIsNotException(name, "an integer", e);
            }
        } else {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {String.class, Integer.class});
        }
    }

    @Override
    public int getIntProperty(String name) {
        return getOptionalIntegerProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public int getIntProperty(String name, int defaultValue) {
        return getOptionalIntegerProperty(name).orElse(defaultValue);
    }

    @Override
    public OptionalLong getOptionalLongProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return OptionalLong.empty();
        }
        if (value instanceof Long) {
            return OptionalLong.of((Long) value);
        } else if (value instanceof Integer) {
            return OptionalLong.of((Integer) value);
        } else if (value instanceof String) {
            try {
                return OptionalLong.of(Long.parseLong((String) value));
            } catch (NumberFormatException e) {
                throw createPropertyIsNotException(name, "a long", e);
            }
        } else {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {String.class, Long.class, Integer.class});
        }
    }

    @Override
    public long getLongProperty(String name) {
        return getOptionalLongProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public long getLongProperty(String name, long defaultValue) {
        return getOptionalLongProperty(name).orElse(defaultValue);
    }

    @Override
    public Optional<Float> getOptionalFloatProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Number) {
            return Optional.of(((Number) value).floatValue());
        } else if (value instanceof String) {
            try {
                return Optional.of(Float.parseFloat((String) value));
            } catch (NumberFormatException e) {
                throw createPropertyIsNotException(name, "a float", e);
            }
        } else {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {Number.class, String.class});
        }
    }

    @Override
    public float getFloatProperty(String name) {
        return getOptionalFloatProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public float getFloatProperty(String name, float defaultValue) {
        return getOptionalFloatProperty(name).orElse(defaultValue);
    }

    @Override
    public OptionalDouble getOptionalDoubleProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return OptionalDouble.empty();
        }
        if (value instanceof Number) {
            return OptionalDouble.of(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return OptionalDouble.of(Double.parseDouble((String) value));
            } catch (NumberFormatException e) {
                throw createPropertyIsNotException(name, "a double", e);
            }
        } else {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {Number.class, String.class});
        }
    }

    @Override
    public double getDoubleProperty(String name) {
        return getOptionalDoubleProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public double getDoubleProperty(String name, double defaultValue) {
        return getOptionalDoubleProperty(name).orElse(defaultValue);
    }

    @Override
    public Optional<Boolean> getOptionalBooleanProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Boolean) {
            return Optional.of((Boolean) value);
        } else if (value instanceof String) {
            return Optional.of(Boolean.parseBoolean((String) value));
        } else {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {Boolean.class, String.class});
        }
    }

    @Override
    public boolean getBooleanProperty(String name) {
        return getOptionalBooleanProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public boolean getBooleanProperty(String name, boolean defaultValue) {
        return getOptionalBooleanProperty(name).orElse(defaultValue);
    }

    @Override
    public Path getPathProperty(String name) {
        return fs.getPath(getStringProperty(name));
    }

    @Override
    public Path getPathProperty(String name, Path defaultValue) {
        String value = getStringProperty(name, defaultValue != null ? defaultValue.toString() : null);
        return value != null ? fs.getPath(value) : null;
    }

    @Override
    public Optional<Path> getOptionalPathProperty(String name) {
        return Optional.ofNullable(getPathProperty(name, null));
    }

    public void setPathProperty(String name, Path path) {
        properties.put(name, path.toAbsolutePath().toString());
    }

    private List<Path> toPath(List<String> strings) {
        return strings.stream().map(fs::getPath).collect(Collectors.toList());
    }

    @Override
    public List<Path> getPathListProperty(String name) {
        return toPath(getStringListProperty(name));
    }

    @Override
    public Optional<List<Path>> getOptionalPathListProperty(String name) {
        return getOptionalStringListProperty(name).map(this::toPath);
    }

    @Override
    public <T> Class<? extends T> getClassProperty(String name, Class<T> subClass) {
        try {
            return Class.forName(getStringProperty(name)).asSubclass(subClass);
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e);
        }
    }

    @Override
    public <T> Class<? extends T> getClassProperty(String name, Class<T> subClass, Class<? extends T> defaultValue) {
        try {
            Object value = properties.get(name);
            if (value == null) {
                return defaultValue;
            } else {
                return Class.forName((String) value).asSubclass(subClass);
            }
        } catch (ClassNotFoundException e) {
            throw new UncheckedClassNotFoundException(e);
        }
    }

    public <T> void setClassProperty(String name, Class<T> subClass) {
        Objects.requireNonNull(subClass);
        setStringProperty(name, subClass.getName());
    }

    @Override
    public Optional<DateTime> getOptionalDateTimeProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Date) {
            return Optional.of(new DateTime(value));
        } else if (value instanceof String) {
            try {
                return Optional.of(DateTime.parse((String) value));
            } catch (IllegalArgumentException e) {
                throw createPropertyIsNotException(name, "an ISO date time", e);
            }
        } else {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {Date.class, String.class});
        }
    }

    @Override
    public DateTime getDateTimeProperty(String name) {
        return getOptionalDateTimeProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public Interval getIntervalProperty(String name) {
        return Interval.parse(getStringProperty(name));
    }

}
