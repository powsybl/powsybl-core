/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
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
    void threeSidesToBranchSideTest() {
        assertSame(Branch.Side.ONE, ThreeSides.ONE.toBranchSide());
        assertSame(Branch.Side.TWO, ThreeSides.TWO.toBranchSide());
        assertThrows(PowsyblException.class, ThreeSides.THREE::toBranchSide);
    }

    @Test
    void threeSidesToThreeWindingsTransformerSideTest() {
        assertSame(ThreeWindingsTransformer.Side.ONE, ThreeSides.ONE.toThreeWindingsTransformerSide());
        assertSame(ThreeWindingsTransformer.Side.TWO, ThreeSides.TWO.toThreeWindingsTransformerSide());
        assertSame(ThreeWindingsTransformer.Side.THREE, ThreeSides.THREE.toThreeWindingsTransformerSide());
    }

    @Test
    void branchSideToThreeSidesTest() {
        assertSame(ThreeSides.ONE, Branch.Side.ONE.toThreeSides());
        assertSame(ThreeSides.TWO, Branch.Side.TWO.toThreeSides());
    }

    @Test
    void threeWindingsTransformerSideToThreeSidesTest() {
        assertSame(ThreeSides.ONE, ThreeWindingsTransformer.Side.ONE.toThreeSides());
        assertSame(ThreeSides.TWO, ThreeWindingsTransformer.Side.TWO.toThreeSides());
        assertSame(ThreeSides.THREE, ThreeWindingsTransformer.Side.THREE.toThreeSides());
    }

}
