/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteAngleRegulationTest {

    @Test
    void test() {
        UcteAngleRegulation angleRegulation = new UcteAngleRegulation(1.0, 2.0, 3, 4, 5.0, UcteAngleRegulationType.ASYM);

        assertEquals(1.0, angleRegulation.getDu(), 0.0);
        angleRegulation.setDu(1.1);
        assertEquals(1.1, angleRegulation.getDu(), 0.0);

        assertEquals(2.0, angleRegulation.getTheta(), 0.0);
        angleRegulation.setTheta(2.1);
        assertEquals(2.1, angleRegulation.getTheta(), 0.0);

        assertEquals(Integer.valueOf(3), angleRegulation.getN());
        angleRegulation.setN(-3);
        assertEquals(Integer.valueOf(-3), angleRegulation.getN());
        angleRegulation.setN(null);
        assertNull(angleRegulation.getN());

        assertEquals(Integer.valueOf(4), angleRegulation.getNp());
        angleRegulation.setNp(-4);
        assertEquals(Integer.valueOf(-4), angleRegulation.getNp());
        angleRegulation.setNp(null);
        assertNull(angleRegulation.getNp());

        assertEquals(5.0, angleRegulation.getP(), 0.0);
        angleRegulation.setP(5.1);
        assertEquals(5.1, angleRegulation.getP(), 0.0);

        assertEquals(UcteAngleRegulationType.ASYM, angleRegulation.getType());
        angleRegulation.setType(UcteAngleRegulationType.SYMM);
        assertEquals(UcteAngleRegulationType.SYMM, angleRegulation.getType());
        angleRegulation.setType(null);
        assertNull(angleRegulation.getType());
    }
}
