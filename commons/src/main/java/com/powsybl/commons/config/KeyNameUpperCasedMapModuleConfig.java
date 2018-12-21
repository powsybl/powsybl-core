/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import org.joda.time.DateTime;

import java.nio.file.FileSystem;
import java.util.*;

import static com.powsybl.commons.config.KeyNameUpperCasedMapModuleConfigRepository.SEPARATOR;
import static com.powsybl.commons.config.KeyNameUpperCasedMapModuleConfigRepository.UPPER_UNDERSCORE_FORMATTER;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class KeyNameUpperCasedMapModuleConfig extends MapModuleConfig {

    private final String prefix;

    public KeyNameUpperCasedMapModuleConfig(Map<Object, Object> properties, FileSystem fs, String moduleName) {
        super(properties, fs);
        this.prefix = UPPER_UNDERSCORE_FORMATTER.apply(moduleName) + SEPARATOR;
    }

    private String toUpper(String name) {
        return prefix + UPPER_UNDERSCORE_FORMATTER.apply(name);
    }

    @Override
    public boolean hasProperty(String name) {
        return super.hasProperty(toUpper(name));
    }

    @Override
    public Optional<String> getOptionalStringProperty(String name) {
        return super.getOptionalStringProperty(toUpper(name));
    }

    @Override
    public Optional<List<String>> getOptionalStringListProperty(String name) {
        return super.getOptionalStringListProperty(toUpper(name));
    }

    @Override
    public OptionalInt getOptionalIntProperty(String name) {
        return super.getOptionalIntProperty(toUpper(name));
    }

    @Override
    public OptionalLong getOptionalLongProperty(String name) {
        return super.getOptionalLongProperty(toUpper(name));
    }

    @Override
    public Optional<Float> getOptionalFloatProperty(String name) {
        return super.getOptionalFloatProperty(toUpper(name));
    }

    @Override
    public OptionalDouble getOptionalDoubleProperty(String name) {
        return super.getOptionalDoubleProperty(toUpper(name));
    }

    @Override
    public Optional<Boolean> getOptionalBooleanProperty(String name) {
        return super.getOptionalBooleanProperty(toUpper(name));
    }

    @Override
    public Optional<DateTime> getOptionalDateTimeProperty(String name) {
        return super.getOptionalDateTimeProperty(toUpper(name));
    }

    @Override
    public Set<String> getPropertyNames() {
        throw new UnsupportedOperationException();
    }
}
