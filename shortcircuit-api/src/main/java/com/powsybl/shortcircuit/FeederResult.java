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

import javax.annotation.Nullable;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface FeederResult {

    /**
     * The ID of the connectable contributing to the three-phase short-circuit current.
     */
    String getConnectableId();

    /**
     * The side of the equipment where the result applies.
     * Will be {@code null} for equipments other than branches and three windings transformers.
     */
    @Nullable
    ThreeSides getSide();

    /**
     * The side of the equipment with two sides (like branch) where the result applies.
     */
    TwoSides getSideAsTwoSides();

}
