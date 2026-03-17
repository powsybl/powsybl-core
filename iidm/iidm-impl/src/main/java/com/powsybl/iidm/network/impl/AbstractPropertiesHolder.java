/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.PropertiesHolder;

import java.util.Properties;
import java.util.Set;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractPropertiesHolder implements PropertiesHolder {

    protected final PropertiesContainer properties = new PropertiesContainer();

    public Properties getProperties() {
        return properties.getProperties();
    }

    @Override
    public boolean hasProperty() {
        return properties.hasProperty();
    }

    @Override
    public boolean hasProperty(String key) {
        return properties.hasProperty(key);
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    @Override
    public String setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }

    @Override
    public boolean removeProperty(String key) {
        return properties.removeProperty(key) != null;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.getPropertyNames();
    }
}
