/*
 *  Copyright (c) 2017, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network;

public enum TwoSides {
    ONE(1),
    TWO(2);

    private final int num;

    TwoSides(int num) {
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    public ThreeSides toThreeSides() {
        return switch (this) {
            case ONE -> ThreeSides.ONE;
            case TWO -> ThreeSides.TWO;
        };
    }
}
