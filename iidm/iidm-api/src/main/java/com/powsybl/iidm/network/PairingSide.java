/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * Side of a {@link BoundaryLine} used to pair two boundary lines sharing the same pairing key
 * into a {@link TieLine}. Two boundary lines can be paired only if they are on different pairing sides.
 *
 * @author Leclerc Clement {@literal <clement.leclerc at rte-france.com>}
 */
public enum PairingSide {
    SIDE_1(1),
    SIDE_2(2);

    private final int side;

    PairingSide(int side) {
        this.side = side;
    }

    public int getSide() {
        return side;
    }

    public PairingSide getOppositeSide() {
        return switch (this) {
            case SIDE_1 -> SIDE_2;
            case SIDE_2 -> SIDE_1;
        };
    }
}
