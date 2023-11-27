/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public enum ThreeSides {
    ONE(1, TwoSides.ONE),
    TWO(2, TwoSides.TWO),
    THREE(3, null);

    private final int num;

    private final TwoSides sideAsTwoSides;

    ThreeSides(int num, TwoSides sideAsTwoSides) {
        this.num = num;
        this.sideAsTwoSides = sideAsTwoSides;
    }

    public int getNum() {
        return num;
    }

    public static ThreeSides valueOf(int num) {
        return switch (num) {
            case 1 -> ONE;
            case 2 -> TWO;
            case 3 -> THREE;
            default -> throw new PowsyblException("Cannot convert integer value " + num + " to ThreeSides.");
        };
    }

    public TwoSides toTwoSides() {
        if (sideAsTwoSides == null) {
            throw new PowsyblException("Cannot convert ThreeSides value " + this.name()
                    + " as a TwoSides (" + TwoSides.ONE.name() + ", " + TwoSides.TWO.name() + ")");
        }
        return sideAsTwoSides;
    }
}
