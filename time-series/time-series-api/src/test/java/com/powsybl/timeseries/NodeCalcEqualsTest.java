/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.google.common.testing.EqualsTester;
import com.powsybl.timeseries.ast.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NodeCalcEqualsTest {

    @Test
    void integerTest() {
        new EqualsTester()
                .addEqualityGroup(new IntegerNodeCalc(1), new IntegerNodeCalc(1))
                .addEqualityGroup(new IntegerNodeCalc(2), new IntegerNodeCalc(2))
                .testEquals();
    }

    @Test
    void floatTest() {
        new EqualsTester()
                .addEqualityGroup(new FloatNodeCalc(1.3f), new FloatNodeCalc(1.3f))
                .addEqualityGroup(new FloatNodeCalc(2.4f), new FloatNodeCalc(2.4f))
                .testEquals();
    }

    @Test
    void doubleTest() {
        new EqualsTester()
                .addEqualityGroup(new DoubleNodeCalc(1.3), new DoubleNodeCalc(1.3))
                .addEqualityGroup(new DoubleNodeCalc(2.4), new DoubleNodeCalc(2.4))
                .testEquals();
    }

    @Test
    void bigDecimalTest() {
        new EqualsTester()
                .addEqualityGroup(new BigDecimalNodeCalc(BigDecimal.valueOf(1.3)), new BigDecimalNodeCalc(BigDecimal.valueOf(1.3)))
                .addEqualityGroup(new BigDecimalNodeCalc(BigDecimal.valueOf(2.4)), new BigDecimalNodeCalc(BigDecimal.valueOf(2.4)))
                .testEquals();
    }

    @Test
    void timeSeriesNameTest() {
        new EqualsTester()
                .addEqualityGroup(new TimeSeriesNameNodeCalc("ts1"), new TimeSeriesNameNodeCalc("ts1"))
                .addEqualityGroup(new TimeSeriesNameNodeCalc("ts2"), new TimeSeriesNameNodeCalc("ts2"))
                .testEquals();
    }

    @Test
    void timeSeriesNumTest() {
        new EqualsTester()
                .addEqualityGroup(new TimeSeriesNumNodeCalc(1), new TimeSeriesNumNodeCalc(1))
                .addEqualityGroup(new TimeSeriesNumNodeCalc(2), new TimeSeriesNumNodeCalc(2))
                .testEquals();
    }

    @Test
    void timeTest() {
        new EqualsTester()
                .addEqualityGroup(new TimeNodeCalc(new IntegerNodeCalc(1)), new TimeNodeCalc(new IntegerNodeCalc(1)))
                .addEqualityGroup(new TimeNodeCalc(new IntegerNodeCalc(2)), new TimeNodeCalc(new IntegerNodeCalc(2)))
                .testEquals();
    }

    @Test
    void binaryOperationTest() {
        new EqualsTester()
                .addEqualityGroup(BinaryOperation.plus(new IntegerNodeCalc(1), new IntegerNodeCalc(2)),
                                  BinaryOperation.plus(new IntegerNodeCalc(1), new IntegerNodeCalc(2)))
                .addEqualityGroup(BinaryOperation.plus(new IntegerNodeCalc(3), new IntegerNodeCalc(4)),
                                  BinaryOperation.plus(new IntegerNodeCalc(3), new IntegerNodeCalc(4)))
                .testEquals();
    }

    @Test
    void unaryOperationTest() {
        new EqualsTester()
                .addEqualityGroup(UnaryOperation.negative(new IntegerNodeCalc(1)),
                                  UnaryOperation.negative(new IntegerNodeCalc(1)))
                .addEqualityGroup(UnaryOperation.negative(new IntegerNodeCalc(3)),
                                  UnaryOperation.negative(new IntegerNodeCalc(3)))
                .testEquals();
    }

    @Test
    void minOperationTest() {
        new EqualsTester()
                .addEqualityGroup(new MinNodeCalc(new IntegerNodeCalc(1), 3), new MinNodeCalc(new IntegerNodeCalc(1), 3))
                .addEqualityGroup(new MinNodeCalc(new IntegerNodeCalc(2), 5), new MinNodeCalc(new IntegerNodeCalc(2), 5))
                .testEquals();
    }

    @Test
    void maxOperationTest() {
        new EqualsTester()
                .addEqualityGroup(new MaxNodeCalc(new IntegerNodeCalc(1), 3), new MaxNodeCalc(new IntegerNodeCalc(1), 3))
                .addEqualityGroup(new MaxNodeCalc(new IntegerNodeCalc(2), 5), new MaxNodeCalc(new IntegerNodeCalc(2), 5))
                .testEquals();
    }

    @Test
    void binaryMinOperationTest() {
        new EqualsTester()
                .addEqualityGroup(new BinaryMinCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3)), new BinaryMinCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3)))
                .addEqualityGroup(new BinaryMinCalc(new IntegerNodeCalc(2), new IntegerNodeCalc(5)), new BinaryMinCalc(new IntegerNodeCalc(2), new IntegerNodeCalc(5)))
                .testEquals();

        // Different BinaryMinCalc
        assertNotEquals(new BinaryMinCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3)), new BinaryMinCalc(new IntegerNodeCalc(2), new IntegerNodeCalc(5)));
        assertNotEquals(new BinaryMinCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3)), new BinaryMinCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(5)));

        // BinaryMinCalc and other NodeCal
        NodeCalc node1 = new BinaryMaxCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3));
        NodeCalc node2 = new BinaryMinCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3));
        assertNotEquals(node2, node1);
    }

    @Test
    void binaryMaxOperationTest() {
        new EqualsTester()
                .addEqualityGroup(new BinaryMaxCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3)), new BinaryMaxCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3)))
                .addEqualityGroup(new BinaryMaxCalc(new IntegerNodeCalc(2), new IntegerNodeCalc(5)), new BinaryMaxCalc(new IntegerNodeCalc(2), new IntegerNodeCalc(5)))
                .testEquals();

        // Different BinaryMaxCalc
        assertNotEquals(new BinaryMaxCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3)), new BinaryMaxCalc(new IntegerNodeCalc(2), new IntegerNodeCalc(5)));
        assertNotEquals(new BinaryMaxCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3)), new BinaryMaxCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(5)));

        // BinaryMaxCalc and other NodeCal
        NodeCalc node1 = new BinaryMaxCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3));
        NodeCalc node2 = new BinaryMinCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3));
        assertNotEquals(node1, node2);
    }

    @Test
    void cachedTest() {
        new EqualsTester()
            .addEqualityGroup(new CachedNodeCalc(new IntegerNodeCalc(1)), new CachedNodeCalc(new IntegerNodeCalc(1)))
            .addEqualityGroup(new CachedNodeCalc(new IntegerNodeCalc(2)), new CachedNodeCalc(new IntegerNodeCalc(2)))
            .testEquals();

        // Different BinaryMaxCalc
        assertNotEquals(
            new CachedNodeCalc(new IntegerNodeCalc(1)),
            new CachedNodeCalc(new IntegerNodeCalc(2)));

        // BinaryMaxCalc and other NodeCal
        NodeCalc node1 = new CachedNodeCalc(new IntegerNodeCalc(1));
        NodeCalc node2 = new BinaryMinCalc(new IntegerNodeCalc(1), new IntegerNodeCalc(3));
        assertNotEquals(node1, node2);
    }
}
