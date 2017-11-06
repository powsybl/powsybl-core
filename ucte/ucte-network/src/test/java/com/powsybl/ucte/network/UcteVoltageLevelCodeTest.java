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
        assertEquals(750, UcteVoltageLevelCode._750.getVoltageLevel());
        assertEquals(380, UcteVoltageLevelCode._380.getVoltageLevel());
        assertEquals(220, UcteVoltageLevelCode._220.getVoltageLevel());
        assertEquals(150, UcteVoltageLevelCode._150.getVoltageLevel());
        assertEquals(120, UcteVoltageLevelCode._120.getVoltageLevel());
        assertEquals(110, UcteVoltageLevelCode._110.getVoltageLevel());
        assertEquals(70, UcteVoltageLevelCode._70.getVoltageLevel());
        assertEquals(27, UcteVoltageLevelCode._27.getVoltageLevel());
        assertEquals(330, UcteVoltageLevelCode._330.getVoltageLevel());
        assertEquals(500, UcteVoltageLevelCode._500.getVoltageLevel());
    }
}
