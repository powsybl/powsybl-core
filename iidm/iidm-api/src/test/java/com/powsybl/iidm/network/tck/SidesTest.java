/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoSides;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SidesTest {

    @Test
    void threeSidesValueOfTest() {
        assertEquals(ThreeSides.ONE, ThreeSides.valueOf(1));
        assertEquals(ThreeSides.TWO, ThreeSides.valueOf(2));
        assertEquals(ThreeSides.THREE, ThreeSides.valueOf(3));
        assertThrows(PowsyblException.class, () -> ThreeSides.valueOf(4));
    }

    @Test
    void threeSidesGetNumTest() {
        assertEquals(1, ThreeSides.ONE.getNum());
        assertEquals(2, ThreeSides.TWO.getNum());
        assertEquals(3, ThreeSides.THREE.getNum());
    }

    @Test
    void threeSidesToTwoSidesTest() {
        assertSame(TwoSides.ONE, ThreeSides.ONE.toTwoSides());
        assertSame(TwoSides.TWO, ThreeSides.TWO.toTwoSides());
        assertThrows(PowsyblException.class, ThreeSides.THREE::toTwoSides);
    }

    @Test
    void twoSidesToThreeSidesTest() {
        assertSame(ThreeSides.ONE, TwoSides.ONE.toThreeSides());
        assertSame(ThreeSides.TWO, TwoSides.TWO.toThreeSides());
    }
}
