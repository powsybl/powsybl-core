/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class PropertiesContainer {
    protected final Properties properties = new Properties();

    public Properties getProperties() {
        return properties;
    }

    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public String getProperty(String key) {
        Object val = properties.get(key);
        return val != null ? val.toString() : null;
    }

    public String getProperty(String key, String defaultValue) {
        Object val = properties.getOrDefault(key, defaultValue);
        return val != null ? val.toString() : null;
    }

    public String setProperty(String key, String value) {
        return (String) properties.put(key, value);
    }

    public String removeProperty(String key) {
        return (String) properties.remove(key);
    }

    public Set<String> getPropertyNames() {
        return properties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
    }
}
