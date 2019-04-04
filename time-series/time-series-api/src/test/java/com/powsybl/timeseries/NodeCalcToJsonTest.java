/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.powsybl.timeseries.ast.*;
import org.junit.Test;

import java.math.BigDecimal;

import static com.powsybl.timeseries.ast.BinaryOperation.*;
import static com.powsybl.timeseries.ast.LiteralNodeCalc.*;
import static com.powsybl.timeseries.ast.UnaryOperation.abs;
import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeCalcToJsonTest {

    @Test
    public void test() {
        NodeCalc node = abs(div(plus(new TimeNodeCalc(createInteger(11)),
                                     createFloat(3.14f)),
                                multiply(createDouble(2.4566),
                                         greaterThan(createBigDecimal(BigDecimal.TEN),
                                                     new MinNodeCalc(new MaxNodeCalc(new TimeSeriesNameNodeCalc("ts"),
                                                                                     10),
                                                                     20)))));

        String json = NodeCalc.toJson(node);
        NodeCalc node2 = NodeCalc.parseJson(json);
        assertEquals(node, node2);
    }
}
