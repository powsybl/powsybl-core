/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteRegulationTest {

    private UcteElementId createElementId() {
        UcteNodeCode node1 = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, 'A');
        UcteNodeCode node2 = new UcteNodeCode(UcteCountryCode.BE, "BBBBB", UcteVoltageLevelCode.VL_220, 'B');
        return new UcteElementId(node1, node2, '1');
    }

    @Test
    public void test() {
        UcteElementId id = createElementId();

        UctePhaseRegulation phaseRegulation = new UctePhaseRegulation(1.0f, 2, 3, 4.0f);
        UcteAngleRegulation angleRegulation = new UcteAngleRegulation(1.0f, 2.0f, 3, 4, 5.0f, UcteAngleRegulationType.ASYM);

        UcteRegulation regulation = new UcteRegulation(id, phaseRegulation, angleRegulation);
        assertEquals(id, regulation.getTransfoId());

        assertEquals(phaseRegulation, regulation.getPhaseRegulation());
        regulation.setPhaseRegulation(null);
        assertNull(regulation.getPhaseRegulation());

        assertEquals(angleRegulation, regulation.getAngleRegulation());
        regulation.setAngleRegulation(null);
        assertNull(regulation.getAngleRegulation());
    }

    @Test
    public void testFix() {
        UcteElementId id = createElementId();
        UcteRegulation regulation = new UcteRegulation(id, null, null);

        UctePhaseRegulation invalidPhaseRegulation1 = new UctePhaseRegulation(0.0f, 1, 1, -10);
        UctePhaseRegulation invalidPhaseRegulation2 = new UctePhaseRegulation(Float.NaN, null, null, Float.NaN);
        UctePhaseRegulation invalidPhaseRegulation3 = new UctePhaseRegulation(Float.NaN, 0, null, Float.NaN);
        UctePhaseRegulation invalidPhaseRegulation4 = new UctePhaseRegulation(Float.NaN, 1, null, Float.NaN);
        UctePhaseRegulation invalidPhaseRegulation5 = new UctePhaseRegulation(Float.NaN, 1, 0, Float.NaN);

        regulation.setPhaseRegulation(invalidPhaseRegulation1);
        regulation.fix();
        assertNotNull(regulation.getPhaseRegulation());
        assertTrue(Float.isNaN(invalidPhaseRegulation1.getU()));

        testFix(regulation, invalidPhaseRegulation2);
        testFix(regulation, invalidPhaseRegulation3);
        testFix(regulation, invalidPhaseRegulation4);
        testFix(regulation, invalidPhaseRegulation5);

        UcteAngleRegulation invalidAngleRegulation1 = new UcteAngleRegulation(0.0f, 0.0f, 1, 1, 0.0f, null);
        UcteAngleRegulation invalidAngleRegulation2 = new UcteAngleRegulation(Float.NaN, Float.NaN, null, null, Float.NaN, null);
        UcteAngleRegulation invalidAngleRegulation3 = new UcteAngleRegulation(Float.NaN, Float.NaN, 0, null, Float.NaN, null);
        UcteAngleRegulation invalidAngleRegulation4 = new UcteAngleRegulation(Float.NaN, Float.NaN, 1, null, Float.NaN, null);
        UcteAngleRegulation invalidAngleRegulation5 = new UcteAngleRegulation(Float.NaN, Float.NaN, 1, 0, Float.NaN, null);
        UcteAngleRegulation invalidAngleRegulation6 = new UcteAngleRegulation(Float.NaN, Float.NaN, 1, 0, Float.NaN, null);
        UcteAngleRegulation invalidAngleRegulation7 = new UcteAngleRegulation(0.0f, Float.NaN, 1, 0, Float.NaN, null);

        regulation.setAngleRegulation(invalidAngleRegulation1);
        regulation.fix();
        assertNotNull(regulation.getAngleRegulation());
        assertEquals(UcteAngleRegulationType.ASYM, invalidAngleRegulation1.getType());

        testFix(regulation, invalidAngleRegulation2);
        testFix(regulation, invalidAngleRegulation3);
        testFix(regulation, invalidAngleRegulation4);
        testFix(regulation, invalidAngleRegulation5);
        testFix(regulation, invalidAngleRegulation6);
        testFix(regulation, invalidAngleRegulation7);
    }

    private void testFix(UcteRegulation regulation, UctePhaseRegulation phaseRegulation) {
        regulation.setPhaseRegulation(phaseRegulation);
        regulation.fix();
        assertNull(regulation.getPhaseRegulation());
    }

    private void testFix(UcteRegulation regulation, UcteAngleRegulation angleRegulation) {
        regulation.setAngleRegulation(angleRegulation);
        regulation.fix();
        assertNull(regulation.getAngleRegulation());
    }
}
