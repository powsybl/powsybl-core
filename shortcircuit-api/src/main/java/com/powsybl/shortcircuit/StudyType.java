/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * The type of short-circuit calculation, transient, sub-transient or steady-state.
 */
public enum StudyType {

    /**
     * The first stage of the short circuit, right when the fault occurs.
     */
    SUB_TRANSIENT,

    /**
     * The second stage of the short circuit, before the system stabilizes.
     */
    TRANSIENT,

    /**
     * The final stage of the short circuit, when all transient effects have disappeared.
     */
    STEADY_STATE
}
