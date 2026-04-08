/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class GraphUtilTest {

    @Test
    void testComputeConnectedComponents() {
        TIntArrayList[] adjacencyList = new TIntArrayList[100000];
        for (int i = 0; i < adjacencyList.length; i++) {
            adjacencyList[i] = new TIntArrayList();
        }
        for (int i = 2; i < adjacencyList.length - 1; i++) {
            adjacencyList[i].add(i + 1);
        }

        GraphUtil.ConnectedComponentsComputationResult result = GraphUtil.computeConnectedComponents(adjacencyList);
        assertEquals(3, result.getComponentSize().length);
        assertEquals(adjacencyList.length - 2, result.getComponentSize()[0]);
        assertEquals(1, result.getComponentSize()[1]);
        assertEquals(1, result.getComponentSize()[2]);
        assertEquals(0, result.getComponentNumber()[40000]);
        assertEquals(1, result.getComponentNumber()[0]);
        assertEquals(2, result.getComponentNumber()[1]);
    }

}
