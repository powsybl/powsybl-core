/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.powsybl.ucte.network.UcteVoltageLevelCode.isVoltageLevel;
import static com.powsybl.ucte.network.UcteVoltageLevelCode.voltageLevelCodeFromChar;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteVoltageLevelCodeTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void test() {
        assertEquals(10, UcteVoltageLevelCode.values().length);
        assertEquals(750, UcteVoltageLevelCode.VL_750.getVoltageLevel());
        assertEquals(380, UcteVoltageLevelCode.VL_380.getVoltageLevel());
        assertEquals(220, UcteVoltageLevelCode.VL_220.getVoltageLevel());
        assertEquals(150, UcteVoltageLevelCode.VL_150.getVoltageLevel());
        assertEquals(120, UcteVoltageLevelCode.VL_120.getVoltageLevel());
        assertEquals(110, UcteVoltageLevelCode.VL_110.getVoltageLevel());
        assertEquals(70, UcteVoltageLevelCode.VL_70.getVoltageLevel());
        assertEquals(27, UcteVoltageLevelCode.VL_27.getVoltageLevel());
        assertEquals(330, UcteVoltageLevelCode.VL_330.getVoltageLevel());
        assertEquals(500, UcteVoltageLevelCode.VL_500.getVoltageLevel());
    }

    @Test
    public void isVoltageLevelTest() {
        assertTrue(isVoltageLevel('0'));
        assertTrue(isVoltageLevel('9'));
        assertFalse(isVoltageLevel('_'));
        assertFalse(isVoltageLevel('&'));
    }

    @Test
    public void voltageLevelCodeFromIidmVoltageTest() {
        assertSame(UcteVoltageLevelCode.VL_750, voltageLevelCodeFromChar('0'));
        assertSame(UcteVoltageLevelCode.VL_500, voltageLevelCodeFromChar('9'));
        assertSame(UcteVoltageLevelCode.VL_380, voltageLevelCodeFromChar('1'));
        assertSame(UcteVoltageLevelCode.VL_330, voltageLevelCodeFromChar('8'));
        assertSame(UcteVoltageLevelCode.VL_220, voltageLevelCodeFromChar('2'));
        assertSame(UcteVoltageLevelCode.VL_150, voltageLevelCodeFromChar('3'));
        assertSame(UcteVoltageLevelCode.VL_120, voltageLevelCodeFromChar('4'));
        assertSame(UcteVoltageLevelCode.VL_110, voltageLevelCodeFromChar('5'));
        assertSame(UcteVoltageLevelCode.VL_70, voltageLevelCodeFromChar('6'));
        assertSame(UcteVoltageLevelCode.VL_27, voltageLevelCodeFromChar('7'));
        exception.expect(IllegalArgumentException.class);
        assertSame(new IllegalArgumentException(), voltageLevelCodeFromChar('&'));
    }
}
