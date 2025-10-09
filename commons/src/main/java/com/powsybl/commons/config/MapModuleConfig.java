/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@link ModuleConfig} backed by a simple key/value {@link Map}.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class MapModuleConfig extends AbstractMapModuleConfig {

    private final Map<Object, Object> properties;

    public MapModuleConfig(FileSystem fs) {
        this(new HashMap<>(), fs);
    }

    public MapModuleConfig(Map<Object, Object> properties) {
        this(properties, FileSystems.getDefault());
    }

    public MapModuleConfig(Map<Object, Object> properties, FileSystem fs) {
        super(fs);
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    protected Object getValue(String propertyName) {
        return properties.get(propertyName);
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

    public void setPathProperty(String name, Path path) {
        properties.put(name, path.toAbsolutePath().toString());
    }

    public <T> void setClassProperty(String name, Class<T> subClass) {
        Objects.requireNonNull(subClass);
        setStringProperty(name, subClass.getName());
    }

    public void setStringProperty(String name, String value) {
        Objects.requireNonNull(name);
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

    public void setStringListProperty(String name, List<String> value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        properties.put(name, value);
    }
}
