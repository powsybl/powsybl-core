/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.timeseries;

import com.powsybl.timeseries.ast.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
//TODO
class FastNodeCalcPrinterTest {

    @Test
    void testBinaryOperations() {
        assertEquals("(1.0 + 2.0)", FastNodeCalcPrinter.print(BinaryOperation.plus(new FloatNodeCalc(1f), new FloatNodeCalc(2f))));
        assertEquals("(2.0 - 1.0)", FastNodeCalcPrinter.print(BinaryOperation.minus(new FloatNodeCalc(2f), new FloatNodeCalc(1f))));
        assertEquals("(1.0 * 2.0)", FastNodeCalcPrinter.print(BinaryOperation.multiply(new FloatNodeCalc(1f), new FloatNodeCalc(2f))));
        assertEquals("(2.0 / 1.0)", FastNodeCalcPrinter.print(BinaryOperation.div(new FloatNodeCalc(2f), new FloatNodeCalc(1f))));
    }

    @Test
    void testBigDecimalOperations() {
        assertEquals("1.5", FastNodeCalcPrinter.print(new BigDecimalNodeCalc(BigDecimal.valueOf(1.5))));
    }
}
