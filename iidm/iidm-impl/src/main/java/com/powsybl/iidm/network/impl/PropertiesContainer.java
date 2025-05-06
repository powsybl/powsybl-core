/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.PropertiesBearer;

import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class PropertiesContainer {
    protected final PropertiesBearer bearer;
    protected final Properties properties = new Properties();

    public PropertiesContainer(PropertiesBearer bearer) {
        this.bearer = bearer;
    }

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
        String oldValue = (String) properties.put(key, value);
        if (bearer instanceof Identifiable<?> identifiable) {
            if (Objects.isNull(oldValue)) {
                getNetwork().getListeners().notifyPropertyAdded(identifiable, () -> getPropertyStringForNotification(key), value);
            } else {
                getNetwork().getListeners().notifyPropertyReplaced(identifiable, () -> getPropertyStringForNotification(key), oldValue, value);
            }
        }
        return oldValue;
    }

    public boolean removeProperty(String key) {
        Object oldValue = properties.remove(key);
        if (oldValue != null) {
            if (bearer instanceof Identifiable<?> identifiable) {
                getNetwork().getListeners().notifyPropertyRemoved(identifiable, () -> getPropertyStringForNotification(key), oldValue);
            }
            return true;
        }
        return false;
    }

    public Set<String> getPropertyNames() {
        return properties.keySet().stream().map(Object::toString).collect(Collectors.toSet());
    }

    private static String getPropertyStringForNotification(String key) {
        return "properties[" + key + "]";
    }

    private NetworkImpl getNetwork() {
        return (NetworkImpl) bearer.getNetwork();
    }
}
