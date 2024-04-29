/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoSides;

import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
abstract class AbstractFeederResult implements FeederResult {

    private final String connectableId;
    private final ThreeSides side;

    protected AbstractFeederResult(String connectableId) {
        this(connectableId, null);
    }

    protected AbstractFeederResult(String connectableId, ThreeSides side) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.side = side;
    }

    public String getConnectableId() {
        return connectableId;
    }

    public ThreeSides getSide() {
        return side;
    }

    public TwoSides getSideAsTwoSides() {
        return Objects.requireNonNull(side).toTwoSides();
    }

}
