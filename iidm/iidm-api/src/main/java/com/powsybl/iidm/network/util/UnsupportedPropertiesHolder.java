/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.PropertiesHolder;

import java.util.Set;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public class UnsupportedPropertiesHolder implements PropertiesHolder {
    @Override
    public boolean hasProperty() {
        return false;
    }

    @Override
    public boolean hasProperty(String key) {
        return false;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return null;
    }

    @Override
    public String setProperty(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeProperty(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getPropertyNames() {
        return Set.of();
    }
}
