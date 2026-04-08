/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.timeseries.ast.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class NodeCalcClonerTest {

    NodeCalcCloner<Object> cloner = new NodeCalcCloner<>();

    @Test
    void testCached() {
        CachedNodeCalc node = new CachedNodeCalc(new DoubleNodeCalc(5.0));
        assertSame(node, cloner.visit(node, null, null));
    }

    @Test
    void testTimeSeriesName() {
        TimeSeriesNameNodeCalc node = new TimeSeriesNameNodeCalc("test");
        assertEquals(node, cloner.visit(node, null));
    }

    @Test
    void testTimeSeriesNum() {
        TimeSeriesNumNodeCalc node = new TimeSeriesNumNodeCalc(5);
        assertEquals(node, cloner.visit(node, null));
    }
}
