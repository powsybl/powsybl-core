/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.graph;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class GraphUtilTest {

    @Test
    public void removeIsolatedVertices() throws Exception {
        UndirectedGraph<Object, Object> graph = new UndirectedGraphImpl<>();

        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(1, 2, null);
        graph.setVertexObject(0, new Object());

        assertEquals(3, graph.getVertexCount());
        // Vertex 0 is isolated but has an associated object: it must not be removed.
        GraphUtil.removeIsolatedVertices(graph);
        assertEquals(3, graph.getVertexCount());
        graph.setVertexObject(0, null);
        // Now vertex 0 must be removed.
        GraphUtil.removeIsolatedVertices(graph);
        assertEquals(2, graph.getVertexCount());
    }

}
