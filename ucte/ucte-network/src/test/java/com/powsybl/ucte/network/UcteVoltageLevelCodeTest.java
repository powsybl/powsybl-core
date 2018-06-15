/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteVoltageLevelCodeTest {

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
}
