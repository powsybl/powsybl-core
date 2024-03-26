/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.impl.results;

import com.google.common.testing.EqualsTester;
import com.powsybl.security.results.ThreeWindingsTransformerResult;
import org.junit.jupiter.api.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class ThreeWindingsTransformerResultTest {

    @Test
    void test() {

        ThreeWindingsTransformerResult threeWindingsTransformerResult = new ThreeWindingsTransformerResult("id1", 500, 200, 300, -500, 300, -300, 200, 300, 800);
        assertEquals("id1", threeWindingsTransformerResult.getThreeWindingsTransformerId());
        assertEquals(300.0, threeWindingsTransformerResult.getI1());
        assertEquals(500.0, threeWindingsTransformerResult.getP1());
        assertEquals(200.0, threeWindingsTransformerResult.getQ1());
        assertEquals(-300.0, threeWindingsTransformerResult.getI2());
        assertEquals(-500.0, threeWindingsTransformerResult.getP2());
        assertEquals(300.0, threeWindingsTransformerResult.getQ2());
        assertEquals(800.0, threeWindingsTransformerResult.getI3());
        assertEquals(200.0, threeWindingsTransformerResult.getP3());
        assertEquals(300.0, threeWindingsTransformerResult.getQ3());

        new EqualsTester()
            .addEqualityGroup(new ThreeWindingsTransformerResult("id2", 400, 200, 300, -500, 300, -300, 200, 300, 500),
                new ThreeWindingsTransformerResult("id2", 400, 200, 300, -500, 300, -300, 200, 300, 500))
            .addEqualityGroup(threeWindingsTransformerResult, threeWindingsTransformerResult)
            .testEquals();
    }
}
