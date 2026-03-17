/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network;

import java.util.Set;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public interface BasePropertiesHolder {

    /**
     * Check that this object has some properties.
     */
    boolean hasProperty();

    /**
     * Get property associated to specified key.
     */
    String getProperty(String key);

    /**
     * Set property value associated to specified key.
     */
    String setProperty(String key, String value);

    /**
     * Get properties key values.
     */
    Set<String> getPropertyNames();

    /**
     * Copy the properties to another properties holder
     * @param propertiesHolder the destination properties holder
     * */
    default void copyPropertiesTo(BasePropertiesHolder propertiesHolder) {
        getPropertyNames().forEach(name -> propertiesHolder.setProperty(name, getProperty(name)));
    }

}
