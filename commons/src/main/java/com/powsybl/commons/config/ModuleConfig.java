/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.nio.file.Path;
import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ModuleConfig {

    boolean hasProperty(String name);

    Set<String> getPropertyNames();

    // String

    Optional<String> getOptionalStringProperty(String name);

    String getStringProperty(String name);

    String getStringProperty(String name, String defaultValue);

    // List<String>

    Optional<List<String>> getOptionalStringListProperty(String name);

    List<String> getStringListProperty(String name);

    List<String> getStringListProperty(String name, List<String> defaultValue);

    // Enum

    <E extends Enum<E>> Optional<E> getOptionalEnumProperty(String name, Class<E> clazz);

    <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz);

    <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz, E defaultValue);

    // EnumSet

    <E extends Enum<E>> Optional<Set<E>> getOptionalEnumSetProperty(String name, Class<E> clazz);

    <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz);

    <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz, Set<E> defaultValue);

    // int

    OptionalInt getOptionalIntProperty(String name);

    /**
     * @deprecated Use {@link #getOptionalIntProperty(String)}
     */
    @Deprecated
    Optional<Integer> getOptionalIntegerProperty(String name);

    int getIntProperty(String name);

    int getIntProperty(String name, int defaultValue);

    // long

    OptionalLong getOptionalLongProperty(String name);

    long getLongProperty(String name);

    long getLongProperty(String name, long defaultValue);

    // float

    Optional<Float> getOptionalFloatProperty(String name);

    float getFloatProperty(String name);

    float getFloatProperty(String name, float defaultValue);

    // double

    OptionalDouble getOptionalDoubleProperty(String name);

    double getDoubleProperty(String name);

    double getDoubleProperty(String name, double defaultValue);

    // boolean

    Optional<Boolean> getOptionalBooleanProperty(String name);

    boolean getBooleanProperty(String name);

    boolean getBooleanProperty(String name, boolean defaultValue);

    // path

    Optional<Path> getOptionalPathProperty(String name);

    Path getPathProperty(String name);

    Path getPathProperty(String name, Path defaultValue);

    // List<Path>

    Optional<List<Path>> getOptionalPathListProperty(String name);

    List<Path> getPathListProperty(String name);

    // Class

    <T> Optional<Class<? extends T>> getOptionalClassProperty(String name, Class<T> subClass);

    <T> Class<? extends T> getClassProperty(String name, Class<T> subClass);

    <T> Class<? extends T> getClassProperty(String name, Class<T> subClass, Class<? extends T> defaultValue);

    // DateTime

    Optional<DateTime> getOptionalDateTimeProperty(String name);

    DateTime getDateTimeProperty(String name);

    // Interval

    Optional<Interval> getOptionalIntervalProperty(String name);

    Interval getIntervalProperty(String name);
}
