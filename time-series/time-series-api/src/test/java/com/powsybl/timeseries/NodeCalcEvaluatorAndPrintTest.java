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

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeCalcEvaluatorAndPrintTest {

    @Test
    public void testFloat() {
        NodeCalc node = new FloatNodeCalc(3f);
        assertEquals(3f, NodeCalcEvaluator.eval(node, null), 0f);
        assertEquals("3.0", NodeCalcPrinter.print(node));
    }

    @Test
    public void testDouble() {
        NodeCalc node = new DoubleNodeCalc(3.45d);
        assertEquals(3.45d, NodeCalcEvaluator.eval(node, null), 0d);
        assertEquals("3.45", NodeCalcPrinter.print(node));
    }

    @Test
    public void testInteger() {
        NodeCalc node = new IntegerNodeCalc(3);
        assertEquals(3, NodeCalcEvaluator.eval(node, null), 0);
        assertEquals("3", NodeCalcPrinter.print(node));
    }

    @Test
    public void testBigDecimal() {
        NodeCalc node = new BigDecimalNodeCalc(BigDecimal.valueOf(3.4945949));
        assertEquals(3.4945949d, NodeCalcEvaluator.eval(node, null), 0f);
        assertEquals("3.4945949", NodeCalcPrinter.print(node));
    }

    @Test
    public void testPlus() {
        NodeCalc node = BinaryOperation.plus(new FloatNodeCalc(1f), new FloatNodeCalc(2f));
        assertEquals(3f, NodeCalcEvaluator.eval(node, null), 0f);
        assertEquals("(1.0 + 2.0)", NodeCalcPrinter.print(node));
    }

    @Test
    public void testMinus() {
        NodeCalc node = BinaryOperation.minus(new FloatNodeCalc(2f), new FloatNodeCalc(1f));
        assertEquals(1f, NodeCalcEvaluator.eval(node, null), 0f);
        assertEquals("(2.0 - 1.0)", NodeCalcPrinter.print(node));
    }

    @Test
    public void testMultiply() {
        NodeCalc node = BinaryOperation.multiply(new FloatNodeCalc(2f), new FloatNodeCalc(3f));
        assertEquals(6f, NodeCalcEvaluator.eval(node, null), 0f);
        assertEquals("(2.0 * 3.0)", NodeCalcPrinter.print(node));
    }

    @Test
    public void testDivide() {
        NodeCalc node = BinaryOperation.div(new FloatNodeCalc(6f), new FloatNodeCalc(3f));
        assertEquals(2f, NodeCalcEvaluator.eval(node, null), 0f);
        assertEquals("(6.0 / 3.0)", NodeCalcPrinter.print(node));
    }

    @Test
    public void testAbs() {
        NodeCalc node = UnaryOperation.abs(new FloatNodeCalc(-1f));
        assertEquals(1f, NodeCalcEvaluator.eval(node, null), 0f);
        assertEquals("(-1.0).abs()", NodeCalcPrinter.print(node));
    }

    @Test
    public void testNegative() {
        NodeCalc node = UnaryOperation.negative(new FloatNodeCalc(-1f));
        assertEquals(1f, NodeCalcEvaluator.eval(node, null), 0f);
        assertEquals("(-1.0).negative()", NodeCalcPrinter.print(node));
    }

    @Test
    public void testPositive() {
        NodeCalc node = UnaryOperation.positive(new FloatNodeCalc(-1f));
        assertEquals(-1f, NodeCalcEvaluator.eval(node, null), 0f);
        assertEquals("(-1.0).positive()", NodeCalcPrinter.print(node));
    }

    @Test
    public void testTimeSeriesName() {
        NodeCalc node1 = new TimeSeriesNameNodeCalc("foo");
        assertEquals("timeSeries['foo']", NodeCalcPrinter.print(node1));
        NodeCalc node2 = BinaryOperation.plus(new FloatNodeCalc(1f), node1);
        assertEquals("(1.0 + timeSeries['foo'])", NodeCalcPrinter.print(node2));
    }

    @Test
    public void testTimeSeriesNum() {
        NodeCalc node1 = new TimeSeriesNumNodeCalc(4);
        assertEquals("timeSeries[4]", NodeCalcPrinter.print(node1));
    }

    @Test
    public void testTime() {
        NodeCalc node1 = new TimeNodeCalc(new TimeSeriesNameNodeCalc("foo"));
        assertEquals("(timeSeries['foo']).time()", NodeCalcPrinter.print(node1));
    }

    @Test
    public void testMin() {
        NodeCalc node = new MinNodeCalc(new TimeSeriesNameNodeCalc("foo"), 2);
        assertEquals("timeSeries['foo'].min(2.0)", NodeCalcPrinter.print(node));
    }

    @Test
    public void testMax() {
        NodeCalc node = new MaxNodeCalc(new TimeSeriesNameNodeCalc("foo"), 2);
        assertEquals("timeSeries['foo'].max(2.0)", NodeCalcPrinter.print(node));
    }
}
