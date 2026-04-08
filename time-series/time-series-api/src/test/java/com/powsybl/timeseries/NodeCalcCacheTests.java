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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class NodeCalcCacheTests {

    @Test
    void testCacheCreatorOnCachedNodeCalc() {
        BinaryOperation binaryNode = BinaryOperation.greaterThan(new DoubleNodeCalc(5.0), new DoubleNodeCalc(10.5));
        BinaryOperation node = BinaryOperation.plus(binaryNode, new TimeNodeCalc(binaryNode));

        NodeCalcCacheCreator.cacheDuplicated(node);
        assertInstanceOf(BinaryOperation.class, node);
        assertInstanceOf(CachedNodeCalc.class, node.getLeft());
        assertInstanceOf(CachedNodeCalc.class, ((TimeNodeCalc) node.getRight()).getChild());
    }
}
