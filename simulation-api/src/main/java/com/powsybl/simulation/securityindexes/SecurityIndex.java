/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import java.util.Map;

/**
 * Based class for all security indexes.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface SecurityIndex {

    /**
     * Get security index id.
     * @return security index type
     */
    SecurityIndexId getId();

    /**
     * Get the security index synthetic value.
     * @return the security index synthetic value
     */
    boolean isOk();

    Map<String, String > toMap();

    String toXml();

}
