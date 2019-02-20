/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.powsybl.timeseries.ast.*;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeCalcTooManyRecursionExceptionTest {

    @Test
    public void  test() {
        NodeCalc one = new IntegerNodeCalc(1);
        TimeSeriesNameNodeCalc nameNode = new TimeSeriesNameNodeCalc("a");
        NodeCalc node = BinaryOperation.plus(nameNode, one);
        for (int i = 0; i < 100000; i++) {
            node = BinaryOperation.plus(node, one);
        }
        try {
            NodeCalcSimplifier.simplify(node);
            fail();
        } catch (NodeCalcTooManyRecursionException e) {
            assertSame(node, e.getNodeCalc());
        }
    }
}
