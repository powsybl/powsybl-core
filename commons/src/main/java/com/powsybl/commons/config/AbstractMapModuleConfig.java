/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides implementation for most methods of {@link ModuleConfig},
 * based on a single "mapping" method to be implemented {@link this#getValue(String)}.
 * A {@link FileSystem} also needs to be provided to build {@link Path} properties.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractMapModuleConfig extends AbstractModuleConfig {

    private final FileSystem fs;

    private static PowsyblException createUnexpectedPropertyTypeException(String name, Class<?> type, Class<?>[] expectedTypes) {
        return new PowsyblException("Unexpected value type " + type.getName()
                + " for property " + name + ", " + Arrays.toString(expectedTypes) + " is expected ");
    }

    private static PowsyblException createPropertyIsNotException(String name, String what, Exception e) {
        return new PowsyblException("Property " + name + " is not " + what, e);
    }

    /**
     * @param fs File system to provide {@link Path} objects.
     */
    protected AbstractMapModuleConfig(FileSystem fs) {
        this.fs = Objects.requireNonNull(fs);
    }

    /**
     * Returns the value of the property with the specified name, or {@code null} if it does not exist.
     * The returned object may be a {@link String} or directly a more specialized type
     * ({@link Date} or {@link Float} for instance).
     *
     * @param propertyName The name of the property to be looked up
     * @return             The value of the specified property it it exists, {@code null} otherwise.
     */
    protected abstract Object getValue(String propertyName);

    @Override
    public Optional<String> getOptionalStringProperty(String name) {
        Objects.requireNonNull(name);
        Object value = getValue(name);
        if (value == null) {
            return Optional.empty();
        }
        if (!(value instanceof String)) {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {String.class});
        }
        return Optional.of((String) value).map(PlatformEnv::substitute);
    }

    @Override
    public Optional<List<String>> getOptionalStringListProperty(String name) {
        Objects.requireNonNull(name);
        Object value = getValue(name);
        if (value == null) {
            return Optional.empty();
        } else {
            if (value instanceof String val) {
                String trimmedString = val.trim();
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

    @Override
    public OptionalInt getOptionalIntProperty(String name) {
        Objects.requireNonNull(name);
        Object value = getValue(name);
        if (value == null) {
            return OptionalInt.empty();
        }
        if (value instanceof Integer) {
            return OptionalInt.of((int) value);
        } else if (value instanceof String val) {
            try {
                return OptionalInt.of(Integer.parseInt(val));
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
        Object value = getValue(name);
        if (value == null) {
            return OptionalLong.empty();
        }
        if (value instanceof Long val) {
            return OptionalLong.of(val);
        } else if (value instanceof Integer val) {
            return OptionalLong.of(val);
        } else if (value instanceof String val) {
            try {
                return OptionalLong.of(Long.parseLong(val));
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
        Object value = getValue(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Number val) {
            return Optional.of(val.floatValue());
        } else if (value instanceof String val) {
            try {
                return Optional.of(Float.parseFloat(val));
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
        Object value = getValue(name);
        if (value == null) {
            return OptionalDouble.empty();
        }
        if (value instanceof Number val) {
            return OptionalDouble.of(val.doubleValue());
        } else if (value instanceof String val) {
            try {
                return OptionalDouble.of(Double.parseDouble(val));
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
        Object value = getValue(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Boolean val) {
            return Optional.of(val);
        } else if (value instanceof String val) {
            return Optional.of(Boolean.parseBoolean(val));
        } else {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {Boolean.class, String.class});
        }
    }

    @Override
    public Optional<ZonedDateTime> getOptionalDateTimeProperty(String name) {
        Objects.requireNonNull(name);
        Object value = getValue(name);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Date date) {
            return Optional.of(ZonedDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC));
        } else if (value instanceof String val) {
            try {
                return Optional.of(ZonedDateTime.parse(val));
            } catch (IllegalArgumentException e) {
                throw createPropertyIsNotException(name, "an ISO date time", e);
            }
        } else {
            throw createUnexpectedPropertyTypeException(name, value.getClass(), new Class[] {Date.class, String.class});
        }
    }

    @Override
    public Optional<Path> getOptionalPathProperty(String name) {
        return getOptionalStringProperty(name).map(fs::getPath);
    }

    @Override
    public Optional<List<Path>> getOptionalPathListProperty(String name) {
        return getOptionalStringListProperty(name).flatMap(strings -> Optional.of(strings.stream()
                .map(fs::getPath)
                .collect(Collectors.toList())));
    }
}
