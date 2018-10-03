/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedClassNotFoundException;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractMapModule implements ModuleConfig {

    private static PowsyblException createPropertyNotSetException(String name) {
        return new PowsyblException("Property " + name + " is not set");
    }

    @Override
    public String getStringProperty(String name) {
        return getOptionalStringProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public String getStringProperty(String name, String defaultValue) {
        return getOptionalStringProperty(name).orElse(defaultValue);
    }

    @Override
    public List<String> getStringListProperty(String name) {
        return getOptionalStringListProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public List<String> getStringListProperty(String name, List<String> defaultValue) {
        return getOptionalStringListProperty(name).orElse(defaultValue);
    }

    @Override
    public <E extends Enum<E>> Optional<E> getOptionalEnumProperty(String name, Class<E> clazz) {
        return getOptionalStringProperty(name).map(s -> Enum.valueOf(clazz, s));
    }

    @Override
    public <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz) {
        return getOptionalEnumProperty(name, clazz).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz, E defaultValue) {
        return getOptionalEnumProperty(name, clazz).orElse(defaultValue);
    }

    @Override
    public <E extends Enum<E>> Optional<Set<E>> getOptionalEnumSetProperty(String name, Class<E> clazz) {
        return getOptionalStringListProperty(name).flatMap(strings -> Optional.of(strings.stream()
                                                                              .map(s -> Enum.valueOf(clazz, s))
                                                                              .collect(Collectors.toSet())));
    }

    @Override
    public <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz) {
        return getOptionalEnumSetProperty(name, clazz).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz, Set<E> defaultValue) {
        return getOptionalEnumSetProperty(name, clazz).orElse(defaultValue);
    }

    @Override
    public Optional<Integer> getOptionalIntegerProperty(String name) {
        OptionalInt oi = getOptionalIntProperty(name);
        return oi.isPresent() ? Optional.of(oi.getAsInt()) : Optional.empty();
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
    public long getLongProperty(String name) {
        return getOptionalLongProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public long getLongProperty(String name, long defaultValue) {
        return getOptionalLongProperty(name).orElse(defaultValue);
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
    public double getDoubleProperty(String name) {
        return getOptionalDoubleProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public double getDoubleProperty(String name, double defaultValue) {
        return getOptionalDoubleProperty(name).orElse(defaultValue);
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
        return getOptionalPathProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public Path getPathProperty(String name, Path defaultValue) {
        return getOptionalPathProperty(name).orElse(defaultValue);
    }

    @Override
    public List<Path> getPathListProperty(String name) {
        return getOptionalPathListProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public <T> Optional<Class<? extends T>> getOptionalClassProperty(String name, Class<T> subClass) {
        return getOptionalStringProperty(name).map(s -> {
            try {
                return Class.forName(s).asSubclass(subClass);
            } catch (ClassNotFoundException e) {
                throw new UncheckedClassNotFoundException(e);
            }
        });
    }

    @Override
    public <T> Class<? extends T> getClassProperty(String name, Class<T> subClass) {
        return getOptionalClassProperty(name, subClass).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public <T> Class<? extends T> getClassProperty(String name, Class<T> subClass, Class<? extends T> defaultValue) {
        return getOptionalClassProperty(name, subClass).orElse(defaultValue);
    }

    @Override
    public DateTime getDateTimeProperty(String name) {
        return getOptionalDateTimeProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }

    @Override
    public Optional<Interval> getOptionalIntervalProperty(String name) {
        return getOptionalStringProperty(name).map(Interval::parse);
    }

    @Override
    public Interval getIntervalProperty(String name) {
        return getOptionalIntervalProperty(name).orElseThrow(() -> createPropertyNotSetException(name));
    }
}
