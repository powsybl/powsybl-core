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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

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

    private static final int VERTEX_LIMIT = 100;

    private UndirectedGraph<Vertex, Object> graph;

    public UndirectedGraphImplTest() {
    }

    @Before
    public void setUp() {
        graph = new UndirectedGraphImpl<>(VERTEX_LIMIT);
    }

    @After
    public void tearDown() {
        graph = null;
    }

    @Test
    public void testConstructor() {
        assertEquals(0, graph.getVertexCount());
        assertEquals(0, graph.getEdgeCount());
        PowsyblException e = assertThrows(PowsyblException.class, () -> new UndirectedGraphImpl<>(0));
        assertEquals("Vertex limit should be positive", e.getMessage());
    }

    @Test
    public void testAddVertex() {
        graph.addVertex();
        assertEquals(1, graph.getVertexCount());
    }

    @Test
    public void testAddVertexIfNotPresent() {
        graph.addVertexIfNotPresent(0);
        assertEquals(1, graph.getVertexCount());
        graph.addVertexIfNotPresent(0);
        assertEquals(1, graph.getVertexCount());

        graph.addVertexIfNotPresent(VERTEX_LIMIT - 1);
        assertEquals(2, graph.getVertexCount());
        graph.addVertexIfNotPresent(VERTEX_LIMIT - 1);
        assertEquals(2, graph.getVertexCount());
    }

    @Test
    public void testAddVertexIfNotPresentNegative() {
        exception.expect(PowsyblException.class);
        graph.addVertexIfNotPresent(-1);
    }

    @Test
    public void testAddVertexIfNotPresentLimit() {
        exception.expect(PowsyblException.class);
        graph.addVertexIfNotPresent(VERTEX_LIMIT);
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
    public void testRemoveVertexAndAdd() {
        graph.addVertex();
        graph.addVertex();
        graph.removeVertex(0);
        graph.addVertex();
        assertEquals(2, graph.getVertexCount());
        assertEquals(2, graph.getVertices().length);
        graph.setVertexObject(0, new Vertex("test"));
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
        assertEquals(2, graph.getVertexCount());
        assertEquals(0, e);
        assertEquals(1, graph.getEdgeCount());
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
        assertEquals(0, graph.getEdgeCount());
        e = graph.addEdge(0, 1, null);
        assertEquals(0, e);
        int e2 = graph.addEdge(1, 2, null);
        graph.removeEdge(e);
        assertEquals(1, graph.getEdgeCount());
        e = graph.addEdge(0, 1, null);
        assertEquals(0, e);
        graph.removeEdge(e);
        graph.removeEdge(e2);
        assertEquals(0, graph.getEdgeCount());
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
    public void testGetEdgesFromVertex() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        graph.addEdge(1, 2, null);
        assertEquals(Arrays.asList(0, 1), graph.getEdgesConnectedToVertex(1));
        assertEquals(Collections.singletonList(0), graph.getEdgesConnectedToVertex(0));
    }

    @Test
    public void testGetEdgeObject() {
        graph.addVertex();
        graph.addVertex();
        int e = graph.addEdge(0, 1, "Arrow");
        assertEquals("Arrow", graph.getEdgeObject(e));
    }

    @Test
    public void testGetEdgeObjectFromVertex() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, "Arrow01");
        graph.addEdge(1, 2, "Arrow12");
        assertEquals(Collections.singletonList("Arrow01"), graph.getEdgeObjectsConnectedToVertex(0));
        assertEquals(Arrays.asList("Arrow01", "Arrow12"), graph.getEdgeObjectsConnectedToVertex(1));
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
        assertEquals(3, paths.size());
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
        UndirectedGraphListener<Vertex, Object> listener1 = Mockito.mock(UndirectedGraphListener.class);
        UndirectedGraphListener<Vertex, Object> listener2 = Mockito.mock(UndirectedGraphListener.class);
        graph.addListener(listener1);
        graph.addListener(listener2);
        Mockito.verify(listener1, Mockito.never()).vertexAdded(Mockito.anyInt());
        Mockito.verify(listener2, Mockito.never()).vertexAdded(Mockito.anyInt());
        graph.addVertex();
        Mockito.verify(listener1, Mockito.atLeastOnce()).vertexAdded(Mockito.anyInt());
        Mockito.verify(listener2, Mockito.atLeastOnce()).vertexAdded(Mockito.anyInt());
    }

    @Test
    public void testRemoveListener() {
        UndirectedGraphListener<Vertex, Object> listener1 = Mockito.mock(UndirectedGraphListener.class);
        UndirectedGraphListener<Vertex, Object> listener2 = Mockito.mock(UndirectedGraphListener.class);
        graph.addListener(listener1);
        graph.addListener(listener2);
        graph.removeListener(listener1);
        graph.addVertex();
        Mockito.verify(listener1, Mockito.never()).vertexAdded(Mockito.anyInt());
        Mockito.verify(listener2, Mockito.atLeastOnce()).vertexAdded(Mockito.anyInt());
    }


    /**
     * <pre>
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
     * </pre>
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
        Mockito.when(traverser.traverse(4, 3, 1)).thenReturn(TraverseResult.TERMINATE_PATH);
        Mockito.when(traverser.traverse(4, 4, 2)).thenReturn(TraverseResult.TERMINATE_PATH);
        Mockito.when(traverser.traverse(5, 6, 3)).thenReturn(TraverseResult.TERMINATE_PATH);
        boolean[] encountered = new boolean[graph.getVertexCount()];
        Arrays.fill(encountered, false);
        graph.traverse(5, traverser, encountered);
        // Only vertex 4 and 5 encountered
        assertArrayEquals(new boolean[] {false, false, false, false, true, true}, encountered);

        Arrays.fill(encountered, false);
        Traverser traverser2 = (v1, e, v2) -> {
            encountered[v1] = true;
            return v2 == 1 || v2 == 2 || v2 == 3 ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE;
        };

        graph.traverse(4, traverser2);
        // Only vertex 4 and 5 encountered
        assertArrayEquals(new boolean[] {false, false, false, false, true, true}, encountered);

        Arrays.fill(encountered, false);
        Traverser traverser3 = (v1, e, v2) -> {
            return v2 == 0 ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE;
        };

        graph.traverse(5, traverser3, encountered);
        // Only vertices on first path encountering 0 are encountered
        assertArrayEquals(new boolean[] {false, true, false, false, true, true}, encountered);
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

    @Test
    public void testVertexExists() {
        try {
            graph.vertexExists(-1);
            fail();
        } catch (PowsyblException e) {
            assertEquals("Invalid vertex -1", e.getMessage());
        }
        assertFalse(graph.vertexExists(0));
        graph.addVertex();
        assertTrue(graph.vertexExists(0));
    }

    @Test
    public void removeIsolatedVertices() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(1, 2, null);
        graph.setVertexObject(0, new Vertex(""));

        assertEquals(3, graph.getVertexCount());
        // Vertex 0 is isolated but has an associated object: it must not be removed.
        graph.removeIsolatedVertices(false);
        assertEquals(3, graph.getVertexCount());
        graph.setVertexObject(0, null);
        // Now vertex 0 must be removed.
        graph.removeIsolatedVertices(false);
        assertEquals(2, graph.getVertexCount());
    }

    /**
     * <pre>
     *    0   1
     *    |   |
     *      2
     *      |
     *      3 </pre>
     */
    @Test
    public void removeIsolatedVerticesAndDanglingEdges() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 2, null);
        graph.addEdge(1, 2, null);
        graph.addEdge(2, 3, null);
        graph.setVertexObject(0, new Vertex("V1"));
        graph.setVertexObject(3, new Vertex("V2"));
        assertEquals(4, graph.getVertexCount());
        assertEquals(3, graph.getEdgeCount());
        graph.removeIsolatedVertices(true);
        assertEquals(3, graph.getVertexCount());
        assertArrayEquals(new int[] {0, 2, 3}, graph.getVertices()); // 1 has been removed
        assertEquals(2, graph.getEdgeCount());
        assertArrayEquals(new int[] {0, 2}, graph.getEdges()); // 1-2 has been removed

        graph.addVertexIfNotPresent(1); // restore vertex 1
        graph.addEdge(1, 2, null); // and corresponding edge
        graph.setVertexObject(0, null); // 0 is now a dangling vertex
        graph.removeIsolatedVertices(true);
        assertEquals(1, graph.getVertexCount());
        assertArrayEquals(new int[] {3}, graph.getVertices()); // 0, 1 and 2 have been removed
        assertEquals(0, graph.getEdgeCount());
    }

    /**
     * <pre>
     *    0 -- 1 -- 2</pre>
     */
    @Test
    public void removeIsolatedVerticesAndTwoDanglingEdges() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        graph.addEdge(1, 2, null);
        graph.setVertexObject(0, new Vertex("V1"));
        assertEquals(3, graph.getVertexCount());
        assertEquals(2, graph.getEdgeCount());
        graph.removeIsolatedVertices(true);
        assertEquals(1, graph.getVertexCount());
        assertArrayEquals(new int[] {0}, graph.getVertices()); // 1 and 2 have been removed, 0 is kept (has an associated object)
        assertEquals(0, graph.getEdgeCount()); // 0-1 and 1-2 have both been removed
    }
}
