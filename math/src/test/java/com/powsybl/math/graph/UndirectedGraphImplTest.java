/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;
import org.junit.After;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UndirectedGraphImplTest {

    private static final class Vertex {

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

    /**
     *           0
     *           |
     *         ---------
     *         |   |   |
     *         1   2   3
     *         |   |   |
     *         -----   |
     *           |     |
     *           4     |
     *           |     |
     *           -------
     *              |
     *              5
     *
     *  edges:
     *  0 <-> 1 : 0
     *  0 <-> 2 : 1
     *  0 <-> 3 : 2
     *  1 <-> 4 : 3
     *  2 <-> 4 : 4
     *  4 <-> 5 : 5
     *  3 <-> 5 : 6
     *
     *  all paths (edge numbers) between vertex 0 and 5:
     *  0, 3, 5
     *  1, 4, 5
     *  2, 6
     */
    @Test
    public void testFindAllPaths() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.setVertexObject(5, new Vertex("end"));
        graph.addEdge(0, 1, null); // 0
        graph.addEdge(0, 2, null); // 1
        graph.addEdge(0, 3, null); // 2
        graph.addEdge(1, 4, null); // 3
        graph.addEdge(2, 4, null); // 4
        graph.addEdge(4, 5, null); // 5
        graph.addEdge(3, 5, null); // 6
        List<TIntArrayList> paths = graph.findAllPaths(0, vertex -> vertex != null && "end".equals(vertex.name), null);
        assertTrue(paths.size() == 3);
        assertArrayEquals(paths.get(0).toArray(), new int[] {2, 6});
        assertArrayEquals(paths.get(1).toArray(), new int[] {0, 3, 5});
        assertArrayEquals(paths.get(2).toArray(), new int[] {1, 4, 5});
    }
}
