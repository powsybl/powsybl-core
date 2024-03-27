/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.timeseries.ast.*;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

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
        runAllVisitors(node);
    }

    @Test
    void testRight() {
        NodeCalc node = new IntegerNodeCalc(0);
        for (int i = 0; i < 10000; i++) {
            node = BinaryOperation.plus(new IntegerNodeCalc(0), node);
        }
        runAllVisitors(node);
    }
}
