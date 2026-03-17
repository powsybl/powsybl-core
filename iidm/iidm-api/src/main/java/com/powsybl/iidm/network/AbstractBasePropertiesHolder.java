/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public abstract class AbstractBasePropertiesHolder implements BasePropertiesHolder {

    protected final HashMap<String, String> properties = new LinkedHashMap<>();

    @Override
    public boolean hasProperty() {
        return !properties.isEmpty();
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    @Override
    public String setProperty(String key, String value) {
        return properties.put(key, value);
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

}
