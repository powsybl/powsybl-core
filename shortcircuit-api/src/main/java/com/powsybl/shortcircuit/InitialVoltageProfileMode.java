/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * The initial voltage profile to consider for the computation.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public enum InitialVoltageProfileMode {
    /**
     * The nominal values of the voltage are used.
     */
    NOMINAL,

    /**
     * The user gives the voltage profile.
     */
    CONFIGURED,

    /**
     * The voltage profile used is the one calculated by the load flow calculation.
     */
    PREVIOUS_VALUE

}
