/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.casting;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Double2FloatTest {

    @Test
    public void testCasting() {
        double nan = Double.NaN;
        assertTrue(Float.isNaN(Double2Float.safeCasting(nan)));

        double maxQ = Double.MAX_VALUE;
        assertTrue(Float.MAX_VALUE == Double2Float.safeCasting(maxQ));

        double minQ = -Double.MAX_VALUE;
        assertTrue(-Float.MAX_VALUE == Double2Float.safeCasting(minQ));

        Double n = null;
        assertNull(Double2Float.safeCasting(n));
    }
}
