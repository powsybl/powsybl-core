/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface PropertiesHolder extends BasePropertiesHolder {

    /**
     * Get property associated to specified key, with default value.
     */
    String getProperty(String key, String defaultValue);

    /**
     * Check that this object has property with specified name.
     */
    boolean hasProperty(String key);

    /**
     * Remove property with specified key.
     *
     * @param key the property key
     * @return {@code true} if property exists and has been removed, {@code false} otherwise
     */
    boolean removeProperty(String key);

}
