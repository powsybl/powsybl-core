/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;

/*
 @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public enum ThreeSides {
    ONE(1),
    TWO(2),
    THREE(3);

    private final int num;

    ThreeSides(int num) {
        this.num = num;
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

    public Branch.Side toBranchSide() {
        return switch (this) {
            case ONE -> Branch.Side.ONE;
            case TWO -> Branch.Side.TWO;
            case THREE -> throw new PowsyblException("A Branch has only two sides, ONE and TWO, "
                                                      + "here it is called with " + this.name());
        };
    }

    public ThreeWindingsTransformer.Side toThreeWindingsTransformerSide() {
        return switch (this) {
            case ONE -> ThreeWindingsTransformer.Side.ONE;
            case TWO -> ThreeWindingsTransformer.Side.TWO;
            case THREE -> ThreeWindingsTransformer.Side.THREE;
        };
    }

}
