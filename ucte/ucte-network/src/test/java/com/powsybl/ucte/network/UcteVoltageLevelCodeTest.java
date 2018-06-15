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
        assertEquals(750, UcteVoltageLevelCode.LV_750.getVoltageLevel());
        assertEquals(380, UcteVoltageLevelCode.LV_380.getVoltageLevel());
        assertEquals(220, UcteVoltageLevelCode.LV_220.getVoltageLevel());
        assertEquals(150, UcteVoltageLevelCode.LV_150.getVoltageLevel());
        assertEquals(120, UcteVoltageLevelCode.LV_120.getVoltageLevel());
        assertEquals(110, UcteVoltageLevelCode.LV_110.getVoltageLevel());
        assertEquals(70, UcteVoltageLevelCode.LV_70.getVoltageLevel());
        assertEquals(27, UcteVoltageLevelCode.LV_27.getVoltageLevel());
        assertEquals(330, UcteVoltageLevelCode.LV_330.getVoltageLevel());
        assertEquals(500, UcteVoltageLevelCode.LV_500.getVoltageLevel());
    }
}
