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

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteVoltageLevelCodeTest {

    @Test
    void test() {
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

        assertEquals('0', UcteVoltageLevelCode.voltageLevelCodeFromVoltage(1000));
        assertEquals('1', UcteVoltageLevelCode.voltageLevelCodeFromVoltage(386));
        assertEquals('2', UcteVoltageLevelCode.voltageLevelCodeFromVoltage(220));
        assertEquals('2', UcteVoltageLevelCode.voltageLevelCodeFromVoltage(195));
        assertEquals('3', UcteVoltageLevelCode.voltageLevelCodeFromVoltage(150));
        assertEquals('4', UcteVoltageLevelCode.voltageLevelCodeFromVoltage(120));
        assertEquals('4', UcteVoltageLevelCode.voltageLevelCodeFromVoltage(125));
        assertEquals('7', UcteVoltageLevelCode.voltageLevelCodeFromVoltage(22));
    }
}
