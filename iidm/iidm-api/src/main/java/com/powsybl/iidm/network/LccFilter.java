/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * Harmonic filter.
 * q = b * v^2
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface LccFilter {

    /**
     * Get filter susceptance (&#937;).
     * @return
     */
    double getB();

    /**
     * Set filter susceptance (&#937;).
     * @param b filter susceptance;
     * @return the filter itself to allow method chaining
     */
    LccFilter setB(double b);

    /**
     * Check the filter is connected.
     * @return true if the filter is connected, false otherwise.
     */
    boolean isConnected();

    /**
     * Set the connection status of the filter.
     * @param connected the new connection status of the filter
     * @return the filter itself to allow method chaining
     */
    LccFilter setConnected(boolean connected);
}

