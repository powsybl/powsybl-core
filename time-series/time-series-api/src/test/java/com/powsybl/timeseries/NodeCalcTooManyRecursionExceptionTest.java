/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.timeseries.ast.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NodeCalcTooManyRecursionExceptionTest {

    private void runAllVisitors(NodeCalc root) {
        //Should not throw
        NodeCalcEvaluator.eval(root, null);
        NodeCalcPrinter.print(root);
        TimeSeriesNames.list(root);
        NodeCalcResolver.resolve(root, new HashMap<>());
        NodeCalcSimplifier.simplify(root);
    }

    @Test
    void testLeft() {
        NodeCalc node = new IntegerNodeCalc(0);
        for (int i = 0; i < 10000; i++) {
            node = BinaryOperation.plus(node, new IntegerNodeCalc(0));
        }
        try {
            runAllVisitors(node);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testRight() {
        NodeCalc node = new IntegerNodeCalc(0);
        for (int i = 0; i < 10000; i++) {
            node = BinaryOperation.plus(new IntegerNodeCalc(0), node);
        }
        try {
            runAllVisitors(node);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void shouldPrintDeepTreeInLinearTime() {
        long t1 = timeToPrint(100_000);
        long t2 = timeToPrint(200_000);
        double ratio = (double) t2 / t1;
        assertTrue(ratio < 3);
    }

    private static long timeToPrint(int n) {
        NodeCalc node = new IntegerNodeCalc(0);
        for (int i = 0; i < n; i++) {
            node = BinaryOperation.plus(new IntegerNodeCalc(0), node);
        }
        NodeCalcPrinter.print(node);
        long start = System.nanoTime();
        NodeCalcPrinter.print(node);
        return System.nanoTime() - start;
    }

}
