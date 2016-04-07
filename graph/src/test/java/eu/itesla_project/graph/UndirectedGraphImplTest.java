/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.graph;

import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UndirectedGraphImplTest {

    private static class Vertex {

        private final String name;

        private Vertex(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    private UndirectedGraph<Vertex, Object> graph;

    public UndirectedGraphImplTest() {
    }

    @Before
    public void setUp() {
        graph = new UndirectedGraphImpl<>();
    }

    @After
    public void tearDown() {
        graph = null;
    }

    @Test
    public void testConstructor() {
        assertTrue(graph.getVertexCount() == 0);
        assertTrue(graph.getEdgeCount() == 0);
    }

    @Test
    public void testAddVertex() {
        graph.addVertex();
        assertTrue(graph.getVertexCount() == 1);
    }

    @Test
    public void testAddEdge() {
        graph.addVertex();
        graph.addVertex();
        int e = graph.addEdge(0, 1, null);
        assertTrue(graph.getVertexCount() == 2);
        assertTrue(e == 0);
        assertTrue(graph.getEdgeCount() == 1);
    }

    @Test
    public void testRemoveEdge() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        int e = graph.addEdge(0, 1, null);
        graph.removeEdge(e);
        assertTrue(graph.getEdgeCount() == 0);
        e = graph.addEdge(0, 1, null);
        assertTrue(e == 0);
        int e2 = graph.addEdge(1, 2, null);
        graph.removeEdge(e);
        assertTrue(graph.getEdgeCount() == 1);
        e = graph.addEdge(0, 1, null);
        assertTrue(e == 0);
        graph.removeEdge(e);
        graph.removeEdge(e2);
        assertTrue(graph.getEdgeCount() == 0);
    }

}
