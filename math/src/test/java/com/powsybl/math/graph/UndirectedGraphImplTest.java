/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.graph;

import com.powsybl.commons.PowsyblException;
import gnu.trove.list.array.TIntArrayList;
import org.junit.After;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UndirectedGraphImplTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

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
    public void testGetVertices() {
        graph.addVertex();
        graph.addVertex();
        assertArrayEquals(new int[]{0, 1}, graph.getVertices());
    }

    @Test
    public void testGetVertexObject() {
        graph.addVertex();
        graph.setVertexObject(0, new Vertex("Test"));
        assertEquals("Test", graph.getVertexObject(0).toString());
    }

    @Test
    public void testGetMaxVertex() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.removeVertex(1);
        assertEquals(3, graph.getMaxVertex());
    }

    @Test
    public void testRemoveVertexException() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        graph.removeVertex(2);
        exception.expect(PowsyblException.class);
        graph.removeVertex(0);
    }

    @Test
    public void testRemoveAllVertices() {
        graph.addVertex();
        graph.addVertex();
        assertEquals(2, graph.getVertexCount());
        graph.removeAllVertices();
        assertEquals(0, graph.getVertexCount());
    }

    @Test
    public void testRemoveAllVerticesException() {
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        exception.expect(PowsyblException.class);
        graph.removeAllVertices();
    }

    @Test
    public void testAddEdge() {
        graph.addVertex();
        graph.addVertex();
        int e = graph.addEdge(0, 1, null);
        assertTrue(graph.getVertexCount() == 2);
        assertTrue(e == 0);
        assertTrue(graph.getEdgeCount() == 1);
        assertEquals(0, graph.getEdgeVertex1(e));
        assertEquals(1, graph.getEdgeVertex2(e));
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

    @Test
    public void testRemoveVertex() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        int e1 = graph.addEdge(0, 1, null);
        int e2 = graph.addEdge(1, 2, null);
        graph.removeEdge(e1);
        graph.removeVertex(0);
    }

    @Test
    public void testRemoveAllEdges() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        graph.addEdge(1, 0, null);
        assertEquals(2, graph.getEdgeCount());
        graph.removeAllEdges();
        assertEquals(0, graph.getEdgeCount());
    }

    @Test
    public void testGetEdges() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        graph.addEdge(1, 2, null);
        assertArrayEquals(new int[]{0, 1}, graph.getEdges());
    }

    @Test
    public void testGetEdgeObject() {
        graph.addVertex();
        graph.addVertex();
        int e = graph.addEdge(0, 1, "Arrow");
        assertEquals("Arrow", graph.getEdgeObject(e));
    }

    @Test
    public void testGetEdgeObjects() {
        graph.addVertex();
        graph.addVertex();
        int a = graph.addEdge(0, 1, "Arrow");
        assertEquals(1, graph.getEdgeObjects(0, 1).size());
        assertEquals(1, graph.getEdgeObjects(1, 0).size());
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
    public void testPrintGraph() throws IOException {
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
        ByteArrayOutputStream testStream = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(testStream)) {
            graph.print(out, null, null);
        }
        String expectedPrint = String.join(System.lineSeparator(), "Vertices:",
                                                                   "0: null",
                                                                   "1: null",
                                                                   "2: null",
                                                                   "3: null",
                                                                   "4: null",
                                                                   "5: end",
                                                                   "Edges:",
                                                                   "0: 0<->1 null",
                                                                   "1: 0<->2 null",
                                                                   "2: 0<->3 null",
                                                                   "3: 1<->4 null",
                                                                   "4: 2<->4 null",
                                                                   "5: 4<->5 null",
                                                                   "6: 3<->5 null") + System.lineSeparator();
        assertEquals(expectedPrint, testStream.toString());
    }

    @Test
    public void testAddListener() {
        UndirectedGraphListener listener1 = Mockito.mock(UndirectedGraphListener.class);
        UndirectedGraphListener listener2 = Mockito.mock(UndirectedGraphListener.class);
        graph.addListener(listener1);
        graph.addListener(listener2);
        Mockito.verify(listener1, Mockito.never()).graphChanged();
        Mockito.verify(listener2, Mockito.never()).graphChanged();
        graph.addVertex();
        Mockito.verify(listener1, Mockito.atLeastOnce()).graphChanged();
        Mockito.verify(listener2, Mockito.atLeastOnce()).graphChanged();
    }

    @Test
    public void testRemoveListener() {
        UndirectedGraphListener listener1 = Mockito.mock(UndirectedGraphListener.class);
        UndirectedGraphListener listener2 = Mockito.mock(UndirectedGraphListener.class);
        graph.addListener(listener1);
        graph.addListener(listener2);
        graph.removeListener(listener1);
        graph.addVertex();
        Mockito.verify(listener1, Mockito.never()).graphChanged();
        Mockito.verify(listener2, Mockito.atLeastOnce()).graphChanged();
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
    public void testTraverse() {
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

        Traverser traverser = Mockito.mock(Traverser.class);
        // Stops  on 1, 2 and 3
        Mockito.when(traverser.traverse(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(TraverseResult.CONTINUE);
        Mockito.when(traverser.traverse(4, 3, 1)).thenReturn(TraverseResult.TERMINATE);
        Mockito.when(traverser.traverse(4, 4, 2)).thenReturn(TraverseResult.TERMINATE);
        Mockito.when(traverser.traverse(5, 6, 3)).thenReturn(TraverseResult.TERMINATE);
        boolean[] encountered = new boolean[graph.getVertexCount()];
        Arrays.fill(encountered, false);
        graph.traverse(5, traverser, encountered);
        // Only vertex 4 and 5 encontered
        assertArrayEquals(new boolean[] {false, false, false, false, true, true}, encountered);
        graph.traverse(4, traverser);
    }

    @Test
    public void testGetVertexObjectStream() {
        graph.addVertex();
        graph.addVertex();
        Vertex vertexObj1 = new Vertex("Vertex 1");
        Vertex vertexObj2 = new Vertex("Vertex 2");
        graph.setVertexObject(0, vertexObj1);
        graph.setVertexObject(1, vertexObj2);

        assertArrayEquals(new Vertex[] {vertexObj1, vertexObj2}, graph.getVertexObjectStream().toArray());
    }

    @Test
    public void testGetArrowObjectStream() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, "Edge 1");
        graph.addEdge(1, 2, "Edge 2");
        assertArrayEquals(new String[] {"Edge 1", "Edge 2"}, graph.getEdgeObjectStream().toArray());
    }
}
