/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
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
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractIdentifiableWithPropertiesAdder<T extends AbstractIdentifiableWithPropertiesAdder<T>> extends AbstractIdentifiableAdder<T> implements PropertiesHolder {

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
