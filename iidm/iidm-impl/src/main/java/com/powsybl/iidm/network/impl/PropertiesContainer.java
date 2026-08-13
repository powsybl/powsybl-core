/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class PropertiesContainer {
    private Properties properties = null;

    public Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public boolean hasProperty() {
        return properties != null && !properties.isEmpty();
    }

    public boolean hasProperty(String key) {
        return properties != null && properties.containsKey(key);
    }

    public String getProperty(String key) {
        if (properties == null) {
            return null;
        }
        Object val = properties.get(key);
        return val != null ? val.toString() : null;
    }

    public String getProperty(String key, String defaultValue) {
        if (properties == null) {
            return defaultValue;
        }

        Object val = properties.getOrDefault(key, defaultValue);
        return val != null ? val.toString() : null;
    }

    public String setProperty(String key, String value) {
        if (properties == null) {
            properties = new Properties();
        }
        return (String) properties.put(key, value);
    }

    public String removeProperty(String key) {
        if (properties == null) {
            return null;
        }
        return (String) properties.remove(key);
    }

    public Set<String> getPropertyNames() {
        if (properties == null) {
            return Collections.emptySet();
        }
        return properties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
    }
}
