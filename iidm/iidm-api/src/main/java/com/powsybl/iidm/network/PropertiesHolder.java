/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network;

import java.util.Set;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface PropertiesHolder {

    /**
     * Check that this object has some properties.
     */
    boolean hasProperty();

    /**
     * Check that this object has property with specified name.
     */
    boolean hasProperty(String key);

    /**
     * Get property associated to specified key.
     */
    String getProperty(String key);

    /**
     * Get property associated to specified key, with default value.
     */
    String getProperty(String key, String defaultValue);

    /**
     * Set property value associated to specified key.
     */
    String setProperty(String key, String value);

    /**
     * Remove property with specified key.
     *
     * @param key the property key
     * @return {@code true} if property exists and has been removed, {@code false} otherwise
     */
    boolean removeProperty(String key);

    /**
     * Get properties key values.
     */
    Set<String> getPropertyNames();

    Network getNetwork();
}
