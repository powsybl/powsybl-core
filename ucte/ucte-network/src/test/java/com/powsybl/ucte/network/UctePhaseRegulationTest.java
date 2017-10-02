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
public class UctePhaseRegulationTest {

    @Test
    public void test() {
        UctePhaseRegulation phaseRegulation = new UctePhaseRegulation(1.0f, 2, 3, 4.0f);

        assertEquals(1.0f, phaseRegulation.getDu(), 0.0f);
        phaseRegulation.setDu(1.1f);
        assertEquals(1.1f, phaseRegulation.getDu(), 0.0f);

        assertEquals(Integer.valueOf(2), phaseRegulation.getN());
        phaseRegulation.setN(-2);
        assertEquals(Integer.valueOf(-2), phaseRegulation.getN());
        phaseRegulation.setN(null);
        assertNull(phaseRegulation.getN());

        assertEquals(Integer.valueOf(3), phaseRegulation.getNp());
        phaseRegulation.setNp(-3);
        assertEquals(Integer.valueOf(-3), phaseRegulation.getNp());
        phaseRegulation.setNp(null);
        assertNull(phaseRegulation.getNp());

        assertEquals(4.0f, phaseRegulation.getU(), 0.0f);
        phaseRegulation.setU(4.1f);
        assertEquals(4.1f, phaseRegulation.getU(), 0.0f);
    }
}
