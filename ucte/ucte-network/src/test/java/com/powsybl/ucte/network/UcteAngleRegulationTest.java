/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteAngleRegulationTest {

    @Test
    public void test() {
        UcteAngleRegulation angleRegulation = new UcteAngleRegulation(1.0f, 2.0f, 3, 4, 5.0f, UcteAngleRegulationType.ASYM);

        assertEquals(1.0f, angleRegulation.getDu(), 0.0f);
        angleRegulation.setDu(1.1f);
        assertEquals(1.1f, angleRegulation.getDu(), 0.0f);

        assertEquals(2.0f, angleRegulation.getTheta(), 0.0f);
        angleRegulation.setTheta(2.1f);
        assertEquals(2.1f, angleRegulation.getTheta(), 0.0f);

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

        assertEquals(5.0f, angleRegulation.getP(), 0.0f);
        angleRegulation.setP(5.1f);
        assertEquals(5.1f, angleRegulation.getP(), 0.0f);

        assertEquals(UcteAngleRegulationType.ASYM, angleRegulation.getType());
        angleRegulation.setType(UcteAngleRegulationType.SYMM);
        assertEquals(UcteAngleRegulationType.SYMM, angleRegulation.getType());
        angleRegulation.setType(null);
        assertNull(angleRegulation.getType());
    }
}
