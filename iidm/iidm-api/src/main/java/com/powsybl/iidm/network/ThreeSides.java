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
    ONE(1, ThreeWindingsTransformer.Side.ONE, Branch.Side.ONE),
    TWO(2, ThreeWindingsTransformer.Side.TWO, Branch.Side.TWO),
    THREE(3, ThreeWindingsTransformer.Side.THREE, (Branch.Side) null);

    private final int num;
    private final ThreeWindingsTransformer.Side threeWindingsTransformerSide;
    private final Branch.Side branchSide;

    ThreeSides(int num, ThreeWindingsTransformer.Side transformerSide, Branch.Side branchSide) {
        this.num = num;
        this.threeWindingsTransformerSide = transformerSide;
        this.branchSide = branchSide;
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
        if (branchSide == null) {
            throw new PowsyblException("A Branch has only two sides, ONE and TWO, " +
                    "here it is called with " + this.name());
        }
        return branchSide;
    }

    public ThreeWindingsTransformer.Side toThreeWindingsTransformerSide() {
        return threeWindingsTransformerSide;
    }

}
