/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * Base class for <code>Generator</code> reactive limits.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ReactiveLimits {

    ReactiveLimitsKind getKind();

    /**
     * Get the reactive power minimum value at a given active power value.
     *
     * @param p the active power
     */
    double getMinQ(double p);

    /**
     * Get the reactive power maximum value at a given active power value.
     *
     * @param p the active power
     */
    double getMaxQ(double p);

}
