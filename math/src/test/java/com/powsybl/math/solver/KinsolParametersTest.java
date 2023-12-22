/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class KinsolParametersTest {

    @Test
    void test() {
        KinsolParameters parameters = new KinsolParameters();
        assertEquals(200, parameters.getMaxIters());
        assertEquals(0, parameters.getMsbset());
        assertEquals(0, parameters.getMsbsetsub());
        assertEquals(0d, parameters.getFnormtol(), 0d);
        assertEquals(0d, parameters.getScsteptol(), 0d);
        parameters.setMaxIters(100)
                .setMsbset(6)
                .setMsbsetsub(3)
                .setFnormtol(0.00001d)
                .setScsteptol(0.0003d);
        assertEquals(100, parameters.getMaxIters());
        assertEquals(6, parameters.getMsbset());
        assertEquals(3, parameters.getMsbsetsub());
        assertEquals(0.00001d, parameters.getFnormtol(), 0d);
        assertEquals(0.0003d, parameters.getScsteptol(), 0d);
    }
}
