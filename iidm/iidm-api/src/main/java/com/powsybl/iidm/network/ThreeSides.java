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
}
