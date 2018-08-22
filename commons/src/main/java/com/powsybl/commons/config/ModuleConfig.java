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

    String getStringProperty(String name);

    String getStringProperty(String name, String defaultValue);

    List<String> getStringListProperty(String name);

    List<String> getStringListProperty(String name, List<String> defaultValue);

    <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz);

    <E extends Enum<E>> E getEnumProperty(String name, Class<E> clazz, E defaultValue);

    <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz);

    <E extends Enum<E>> Set<E> getEnumSetProperty(String name, Class<E> clazz, Set<E> defaultValue);

    int getIntProperty(String name);

    Optional<Integer> getOptionalIntegerProperty(String name);

    int getIntProperty(String name, int defaultValue);

    OptionalLong getOptionalLongProperty(String name);

    long getLongProperty(String name);

    long getLongProperty(String name, long defaultValue);

    Optional<Float> getOptionalFloatProperty(String name);

    float getFloatProperty(String name);

    float getFloatProperty(String name, float defaultValue);

    OptionalDouble getOptionalDoubleProperty(String name);

    double getDoubleProperty(String name);

    double getDoubleProperty(String name, double defaultValue);

    boolean getBooleanProperty(String name);

    boolean getBooleanProperty(String name, boolean defaultValue);

    Optional<Boolean> getOptionalBooleanProperty(String name);

    Path getPathProperty(String name);

    Path getPathProperty(String name, Path defaultValue);

    Optional<Path> getOptionalPathProperty(String name);

    List<Path> getPathListProperty(String name);

    Optional<List<Path>> getOptionalPathListProperty(String name);

    <T> Class<? extends T> getClassProperty(String name, Class<T> subClass);

    <T> Class<? extends T> getClassProperty(String name, Class<T> subClass, Class<? extends T> defaultValue);

    Optional<DateTime> getOptionalDateTimeProperty(String name);

    DateTime getDateTimeProperty(String name);

    Interval getIntervalProperty(String name);

}
