/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import org.joda.time.DateTime;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapModuleConfig extends AbstractMapModule {

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

    private static PowsyblException createUnexpectedPropertyTypeException(String name, Class<?> type, Class<?>[] expectedTypes) {
        return new PowsyblException("Unexpected value type " + type.getName()
                + " for property " + name + ", " + Arrays.toString(expectedTypes) + " is expected ");
    }

    private static PowsyblException createPropertyIsNotException(String name, String what, Exception e) {
        throw new PowsyblException("Property " + name + " is not " + what, e);
    }

    @Override
    public Optional<String> getOptionalStringProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof String)) {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {String.class});
        }
        return Optional.of((String) value).map(PlatformEnv::substitute);
    }

    public void setStringProperty(String name, String value) {
        properties.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
    }

    @Override
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
                    return Optional.of(Arrays.asList((PlatformEnv.substitute(trimmedString)).split("[:,]")));
                }
            } else if (value instanceof List) {
                return Optional.of(((List<String>) value).stream().map(PlatformEnv::substitute).collect(Collectors.toList()));
            } else {
                throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {String.class, List.class});
            }
        }
    }

    public void setStringListProperty(String name, List<String> value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        properties.put(name, value);
    }

    @Override
    public OptionalInt getOptionalIntProperty(String name) {
        Objects.requireNonNull(name);
        Object value = properties.get(name);
        if (value == null) {
            return OptionalInt.empty();
        }
        if (value instanceof Integer) {
            return OptionalInt.of((int) value);
        } else if (value instanceof String) {
            try {
                return OptionalInt.of(Integer.parseInt((String) value));
            } catch (NumberFormatException e) {
                throw createPropertyIsNotException(name, "an integer", e);
            }
        } else {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {String.class, Integer.class});
        }
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
    public Optional<Path> getOptionalPathProperty(String name) {
        return getOptionalStringProperty(name).map(fs::getPath);
    }

    public void setPathProperty(String name, Path path) {
        properties.put(name, path.toAbsolutePath().toString());
    }

    @Override
    public Optional<List<Path>> getOptionalPathListProperty(String name) {
        return getOptionalStringListProperty(name).flatMap(strings -> Optional.of(strings.stream()
                                                                                         .map(fs::getPath)
                                                                                         .collect(Collectors.toList())));
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
}
