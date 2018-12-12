/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TwtDataTest {

    @Test
    public void test() {
        TwtData twtData = new TwtData(new TwtTestData().get3WTransformer(), 0, false);

        assertEquals(TwtTestData.P1, twtData.getComputedP(Side.ONE), .3);
        assertEquals(TwtTestData.Q1, twtData.getComputedQ(Side.ONE), .3);
        assertEquals(TwtTestData.P2, twtData.getComputedP(Side.TWO), .3);
        assertEquals(TwtTestData.Q2, twtData.getComputedQ(Side.TWO), .3);
        assertEquals(TwtTestData.P3, twtData.getComputedP(Side.THREE), .3);
        assertEquals(TwtTestData.Q3, twtData.getComputedQ(Side.THREE), .3);

        assertEquals(TwtTestData.STAR_U, twtData.getStarU(), .0001);
        assertEquals(TwtTestData.STAR_ANGLE, Math.toDegrees(twtData.getStarTheta()), .0001);
    }

}
