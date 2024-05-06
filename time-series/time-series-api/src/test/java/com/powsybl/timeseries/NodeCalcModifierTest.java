/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.timeseries.ast.CachedNodeCalc;
import com.powsybl.timeseries.ast.DoubleNodeCalc;
import com.powsybl.timeseries.ast.NodeCalcModifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class NodeCalcModifierTest {

    NodeCalcModifier<Object> modifier = new NodeCalcModifier<>();

    @Test
    void testCached() {
        CachedNodeCalc node = new CachedNodeCalc(new DoubleNodeCalc(5.0));
        DoubleNodeCalc child = new DoubleNodeCalc(4.0);

        // Case new child is null
        assertNull(modifier.visit(node, null, null));
        assertEquals(new DoubleNodeCalc(5.0), node.getChild());

        // Case new child is not null
        assertNull(modifier.visit(node, null, child));
        assertEquals(child, node.getChild());
    }
}
