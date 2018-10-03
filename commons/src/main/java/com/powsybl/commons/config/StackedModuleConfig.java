/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import org.joda.time.DateTime;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StackedModuleConfig extends AbstractMapModule {

    private final ModuleConfig config1;

    private final ModuleConfig config2;

    public StackedModuleConfig(ModuleConfig config1, ModuleConfig config2) {
        this.config1 = Objects.requireNonNull(config1);
        this.config2 = Objects.requireNonNull(config2);
    }

    @Override
    public boolean hasProperty(String name) {
        return config1.hasProperty(name) || config2.hasProperty(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        Set<String> names = new HashSet<>(config2.getPropertyNames());
        names.addAll(config1.getPropertyNames());
        return names;
    }

    private static <T> Optional<T> or(Optional<T> o1, Supplier<Optional<T>> o2) {
        return o1.isPresent() ? o1 : o2.get();
    }

    @Override
    public Optional<String> getOptionalStringProperty(String name) {
        return or(config1.getOptionalStringProperty(name), () -> config2.getOptionalStringProperty(name));
    }

    @Override
    public Optional<List<String>> getOptionalStringListProperty(String name) {
        return or(config1.getOptionalStringListProperty(name), () -> config2.getOptionalStringListProperty(name));
    }

    @Override
    public OptionalInt getOptionalIntProperty(String name) {
        OptionalInt o = config1.getOptionalIntProperty(name);
        return o.isPresent() ? o : config2.getOptionalIntProperty(name);
    }

    @Override
    public OptionalLong getOptionalLongProperty(String name) {
        OptionalLong o = config1.getOptionalLongProperty(name);
        return o.isPresent() ? o : config2.getOptionalLongProperty(name);
    }

    @Override
    public Optional<Float> getOptionalFloatProperty(String name) {
        return or(config1.getOptionalFloatProperty(name), () -> config2.getOptionalFloatProperty(name));
    }

    @Override
    public OptionalDouble getOptionalDoubleProperty(String name) {
        OptionalDouble o = config1.getOptionalDoubleProperty(name);
        return o.isPresent() ? o : config2.getOptionalDoubleProperty(name);
    }

    @Override
    public Optional<Boolean> getOptionalBooleanProperty(String name) {
        return or(config1.getOptionalBooleanProperty(name), () -> config2.getOptionalBooleanProperty(name));
    }

    @Override
    public Optional<Path> getOptionalPathProperty(String name) {
        return or(config1.getOptionalPathProperty(name), () -> config2.getOptionalPathProperty(name));
    }

    @Override
    public Optional<List<Path>> getOptionalPathListProperty(String name) {
        return or(config1.getOptionalPathListProperty(name), () -> config2.getOptionalPathListProperty(name));
    }

    @Override
    public Optional<DateTime> getOptionalDateTimeProperty(String name) {
        return or(config1.getOptionalDateTimeProperty(name), () -> config2.getOptionalDateTimeProperty(name));
    }
}
