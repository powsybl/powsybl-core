/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.google.common.testing.EqualsTester;
import com.powsybl.timeseries.ast.*;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeCalcEqualsTest {

    @Test
    public void integerTest() {
        new EqualsTester()
                .addEqualityGroup(new IntegerNodeCalc(1), new IntegerNodeCalc(1))
                .addEqualityGroup(new IntegerNodeCalc(2), new IntegerNodeCalc(2))
                .testEquals();
    }

    @Test
    public void floatTest() {
        new EqualsTester()
                .addEqualityGroup(new FloatNodeCalc(1.3f), new FloatNodeCalc(1.3f))
                .addEqualityGroup(new FloatNodeCalc(2.4f), new FloatNodeCalc(2.4f))
                .testEquals();
    }

    @Test
    public void doubleTest() {
        new EqualsTester()
                .addEqualityGroup(new DoubleNodeCalc(1.3), new DoubleNodeCalc(1.3))
                .addEqualityGroup(new DoubleNodeCalc(2.4), new DoubleNodeCalc(2.4))
                .testEquals();
    }

    @Test
    public void bigDecimalTest() {
        new EqualsTester()
                .addEqualityGroup(new BigDecimalNodeCalc(BigDecimal.valueOf(1.3)), new BigDecimalNodeCalc(BigDecimal.valueOf(1.3)))
                .addEqualityGroup(new BigDecimalNodeCalc(BigDecimal.valueOf(2.4)), new BigDecimalNodeCalc(BigDecimal.valueOf(2.4)))
                .testEquals();
    }

    @Test
    public void timeSeriesNameTest() {
        new EqualsTester()
                .addEqualityGroup(new TimeSeriesNameNodeCalc("ts1"), new TimeSeriesNameNodeCalc("ts1"))
                .addEqualityGroup(new TimeSeriesNameNodeCalc("ts2"), new TimeSeriesNameNodeCalc("ts2"))
                .testEquals();
    }

    @Test
    public void timeSeriesNumTest() {
        new EqualsTester()
                .addEqualityGroup(new TimeSeriesNumNodeCalc(1), new TimeSeriesNumNodeCalc(1))
                .addEqualityGroup(new TimeSeriesNumNodeCalc(2), new TimeSeriesNumNodeCalc(2))
                .testEquals();
    }

    @Test
    public void timeTest() {
        new EqualsTester()
                .addEqualityGroup(new TimeNodeCalc(new IntegerNodeCalc(1)), new TimeNodeCalc(new IntegerNodeCalc(1)))
                .addEqualityGroup(new TimeNodeCalc(new IntegerNodeCalc(2)), new TimeNodeCalc(new IntegerNodeCalc(2)))
                .testEquals();
    }

    @Test
    public void binaryOperationTest() {
        new EqualsTester()
                .addEqualityGroup(BinaryOperation.plus(new IntegerNodeCalc(1), new IntegerNodeCalc(2)),
                                  BinaryOperation.plus(new IntegerNodeCalc(1), new IntegerNodeCalc(2)))
                .addEqualityGroup(BinaryOperation.plus(new IntegerNodeCalc(3), new IntegerNodeCalc(4)),
                                  BinaryOperation.plus(new IntegerNodeCalc(3), new IntegerNodeCalc(4)))
                .testEquals();
    }

    @Test
    public void unaryOperationTest() {
        new EqualsTester()
                .addEqualityGroup(UnaryOperation.negative(new IntegerNodeCalc(1)),
                                  UnaryOperation.negative(new IntegerNodeCalc(1)))
                .addEqualityGroup(UnaryOperation.negative(new IntegerNodeCalc(3)),
                                  UnaryOperation.negative(new IntegerNodeCalc(3)))
                .testEquals();
    }

    @Test
    public void minOperationTest() {
        new EqualsTester()
                .addEqualityGroup(new MinNodeCalc(new IntegerNodeCalc(1), 3), new MinNodeCalc(new IntegerNodeCalc(1), 3))
                .addEqualityGroup(new MinNodeCalc(new IntegerNodeCalc(2), 5), new MinNodeCalc(new IntegerNodeCalc(2), 5))
                .testEquals();
    }

    @Test
    public void maxOperationTest() {
        new EqualsTester()
                .addEqualityGroup(new MaxNodeCalc(new IntegerNodeCalc(1), 3), new MaxNodeCalc(new IntegerNodeCalc(1), 3))
                .addEqualityGroup(new MaxNodeCalc(new IntegerNodeCalc(2), 5), new MaxNodeCalc(new IntegerNodeCalc(2), 5))
                .testEquals();
    }
}
