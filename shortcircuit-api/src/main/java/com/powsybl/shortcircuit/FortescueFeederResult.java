/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.iidm.network.ThreeSides;

/**
 * Result detailed on the three phases for a feeder contributing to the short-circuit current.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class FortescueFeederResult extends AbstractFeederResult {

    private final FortescueValue current;

    public FortescueFeederResult(String connectableId, FortescueValue current) {
        this(connectableId, current, null);
    }

    public FortescueFeederResult(String connectableId, FortescueValue current, ThreeSides side) {
        super(connectableId, side);
        this.current = current;
    }

    /**
     * The current on the three phases [in A].
     */
    public FortescueValue getCurrent() {
        return current;
    }
}
