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
class UctePhaseRegulationTest {

    @Test
    void test() {
        UctePhaseRegulation phaseRegulation = new UctePhaseRegulation(1.0, 2, 3, 4.0);

        assertEquals(1.0, phaseRegulation.getDu(), 0.0);
        phaseRegulation.setDu(1.1);
        assertEquals(1.1, phaseRegulation.getDu(), 0.0);

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

        assertEquals(4.0, phaseRegulation.getU(), 0.0);
        phaseRegulation.setU(4.1);
        assertEquals(4.1, phaseRegulation.getU(), 0.0);
    }
}
