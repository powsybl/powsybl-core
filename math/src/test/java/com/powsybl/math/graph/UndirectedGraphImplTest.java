/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import com.powsybl.commons.PowsyblException;
import gnu.trove.list.array.TIntArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class UndirectedGraphImplTest {

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

    UndirectedGraphImplTest() {
    }

    @BeforeEach
    void setUp() {
        graph = new UndirectedGraphImpl<>(VERTEX_LIMIT);
    }

    @AfterEach
    void tearDown() {
        graph = null;
    }

    @Test
    void testConstructor() {
        assertEquals(0, graph.getVertexCount());
        assertEquals(0, graph.getEdgeCount());
        PowsyblException e = assertThrows(PowsyblException.class, () -> new UndirectedGraphImpl<>(0));
        assertEquals("Vertex limit should be positive", e.getMessage());
    }

    @Test
    void testAddVertex() {
        graph.addVertex();
        assertEquals(1, graph.getVertexCount());
    }

    @Test
    void testAddVertexIfNotPresent() {
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
    void testAddVertexIfNotPresentNegative() {
        assertThrows(PowsyblException.class, () -> graph.addVertexIfNotPresent(-1));
    }

    @Test
    void testAddVertexIfNotPresentLimit() {
        assertThrows(PowsyblException.class, () -> graph.addVertexIfNotPresent(VERTEX_LIMIT));
    }

    @Test
    void testGetVertices() {
        graph.addVertex();
        graph.addVertex();
        assertArrayEquals(new int[]{0, 1}, graph.getVertices());
    }

    @Test
    void testGetVertexObject() {
        graph.addVertex();
        graph.setVertexObject(0, new Vertex("Test"));
        assertEquals("Test", graph.getVertexObject(0).toString());
    }

    @Test
    void testGetMaxVertex() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.removeVertex(1);
        assertEquals(3, graph.getVertexCapacity());
    }

    @Test
    void testRemoveVertexException() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        graph.removeVertex(2);
        assertThrows(PowsyblException.class, () -> graph.removeVertex(0));
    }

    @Test
    void testRemoveVertexAndAdd() {
        graph.addVertex();
        graph.addVertex();
        graph.removeVertex(0);
        graph.addVertex();
        assertEquals(2, graph.getVertexCount());
        assertEquals(2, graph.getVertices().length);
        graph.setVertexObject(0, new Vertex("test"));
    }

    @Test
    void testRemoveAllVertices() {
        graph.addVertex();
        graph.addVertex();
        assertEquals(2, graph.getVertexCount());
        graph.removeAllVertices();
        assertEquals(0, graph.getVertexCount());
    }

    @Test
    void testRemoveAllVerticesException() {
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        assertThrows(PowsyblException.class, () -> graph.removeAllVertices());
    }

    @Test
    void testAddEdge() {
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
    void testVertexNotFoundWhenAddingEdge() {
        graph.addVertex();
        graph.addVertex();
        var e = assertThrows(PowsyblException.class, () -> graph.addEdge(0, 2, null));
        assertEquals("Vertex 2 not found", e.getMessage());
    }

    @Test
    void testRemoveEdge() {
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
    void testRemoveVertex() {
        assertDoesNotThrow(() -> {
            graph.addVertex();
            graph.addVertex();
            graph.addVertex();
            int e1 = graph.addEdge(0, 1, null);
            int e2 = graph.addEdge(1, 2, null);
            graph.removeEdge(e1);
            graph.removeVertex(0);
        });
    }

    @Test
    void testRemoveAllEdges() {
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
    void testGetEdges() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        graph.addEdge(1, 2, null);
        assertArrayEquals(new int[]{0, 1}, graph.getEdges());
    }

    @Test
    void testEdgeNotFound() {
        var e = assertThrows(PowsyblException.class, () -> graph.getEdgeObject(0));
        assertEquals("Edge 0 not found", e.getMessage());
    }

    @Test
    void testGetEdgesFromVertex() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null);
        graph.addEdge(1, 2, null);
        assertEquals(Arrays.asList(0, 1), graph.getEdgesConnectedToVertex(1));
        assertEquals(Collections.singletonList(0), graph.getEdgesConnectedToVertex(0));
    }

    @Test
    void testGetEdgeObject() {
        graph.addVertex();
        graph.addVertex();
        int e = graph.addEdge(0, 1, "Arrow");
        assertEquals("Arrow", graph.getEdgeObject(e));
    }

    @Test
    void testGetEdgeObjectFromVertex() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, "Arrow01");
        graph.addEdge(1, 2, "Arrow12");
        assertEquals(Collections.singletonList("Arrow01"), graph.getEdgeObjectsConnectedToVertex(0));
        assertEquals(Arrays.asList("Arrow01", "Arrow12"), graph.getEdgeObjectsConnectedToVertex(1));
    }

    @Test
    void testGetEdgeObjects() {
        graph.addVertex();
        graph.addVertex();
        int a = graph.addEdge(0, 1, "Arrow");
        assertEquals(1, graph.getEdgeObjects(0, 1).size());
        assertEquals(1, graph.getEdgeObjects(1, 0).size());
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
     *  2, 6</pre>
     */
    @Test
    void testFindAllPaths() {
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
        assertArrayEquals(new int[] {2, 6}, paths.get(0).toArray());
        assertArrayEquals(new int[] {0, 3, 5}, paths.get(1).toArray());
        assertArrayEquals(new int[] {1, 4, 5}, paths.get(2).toArray());
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
     *  2, 6</pre>
     */
    @Test
    void testPrintGraph() {
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
    void testAddListener() {
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
    void testAddListenerButDisableNotification() {
        UndirectedGraphListener<Vertex, Object> listener = Mockito.mock(UndirectedGraphListener.class);
        graph.addListener(listener);
        Mockito.verify(listener, Mockito.never()).vertexAdded(Mockito.anyInt());
        int v1 = graph.addVertex(false);
        int v2 = graph.addVertex(false);
        Mockito.verify(listener, Mockito.never()).vertexAdded(Mockito.anyInt());
        int e = graph.addEdge(v1, v2, "test", false);
        Mockito.verify(listener, Mockito.never()).edgeAdded(Mockito.anyInt(), Mockito.any());
        graph.removeEdge(e, false);
        Mockito.verify(listener, Mockito.never()).edgeRemoved(Mockito.anyInt(), Mockito.any());
        graph.removeVertex(v1, false);
        Mockito.verify(listener, Mockito.never()).vertexRemoved(Mockito.anyInt(), Mockito.any());
        graph.removeAllVertices(false);
        Mockito.verify(listener, Mockito.never()).allVerticesRemoved();
    }

    @Test
    void testRemoveListener() {
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
     *  2, 6</pre>
     */
    @Test
    void testTraverse() {
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
        boolean[] vEncountered = new boolean[graph.getVertexCount()];
        graph.traverse(5, TraversalType.DEPTH_FIRST, traverser, vEncountered);
        // Only vertex 4 and 5 encountered
        assertArrayEquals(new boolean[] {false, false, false, false, true, true}, vEncountered);

        Arrays.fill(vEncountered, false);
        Traverser traverser2 = (v1, e, v2) -> {
            vEncountered[v1] = true;
            return v2 == 1 || v2 == 2 || v2 == 3 ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE;
        };

        graph.traverse(4, TraversalType.DEPTH_FIRST, traverser2);
        // Only vertex 4 and 5 encountered
        assertArrayEquals(new boolean[] {false, false, false, false, true, true}, vEncountered);

        Arrays.fill(vEncountered, false);
        Traverser traverser3 = (v1, e, v2) -> v2 == 0 ? TraverseResult.TERMINATE_TRAVERSER : TraverseResult.CONTINUE;

        graph.traverse(5, TraversalType.DEPTH_FIRST, traverser3, vEncountered);
        // Only vertices on first path encountering 0 are encountered
        assertArrayEquals(new boolean[] {false, true, false, false, true, true}, vEncountered);

        Arrays.fill(vEncountered, false);
        boolean[] eEncountered = new boolean[graph.getEdgeCount()];
        Traverser traverser4 = (v1, e, v2) -> {
            eEncountered[e] = true;
            return TraverseResult.CONTINUE;
        };
        graph.traverse(5, TraversalType.DEPTH_FIRST, traverser4, vEncountered);
        // All vertices and edges are encountered
        assertArrayEquals(new boolean[] {true, true, true, true, true, true}, vEncountered);
        assertArrayEquals(new boolean[] {true, true, true, true, true, true, true}, eEncountered);

        List<GraphPath> breadthFirstexpected = List.of(
                new GraphPath(5, 5, 4),
                new GraphPath(5, 6, 3),
                new GraphPath(4, 3, 1),
                new GraphPath(4, 4, 2),
                new GraphPath(3, 2, 0),
                new GraphPath(1, 0, 0),
                new GraphPath(2, 1, 0));
        List<GraphPath> depthFirstExpected = List.of(
                new GraphPath(5, 5, 4),
                new GraphPath(4, 3, 1),
                new GraphPath(1, 0, 0),
                new GraphPath(0, 1, 2),
                new GraphPath(2, 4, 4),
                new GraphPath(0, 2, 3),
                new GraphPath(3, 6, 5));

        // Check that all edges and vertices are traversed in the right order when traversing the graph with no stopping point
        List<GraphPath> pathsBf = new ArrayList<>();
        List<GraphPath> pathsDf = new ArrayList<>();
        graph.traverse(5, TraversalType.BREADTH_FIRST, (v1, e, v2) -> {
            pathsBf.add(new GraphPath(v1, e, v2));
            return TraverseResult.CONTINUE;
        });
        graph.traverse(5, TraversalType.DEPTH_FIRST, (v1, e, v2) -> {
            pathsDf.add(new GraphPath(v1, e, v2));
            return TraverseResult.CONTINUE;
        });
        assertEquals(breadthFirstexpected, pathsBf);
        assertEquals(depthFirstExpected, pathsDf);

        // Check all calls done when traversing the graph with one stopping point at vertex 0 when arriving from 3
        // to ensure the edge 0 and 1 still get traversed even if the destination vertex 0 is already encountered
        List<GraphPath> pathsWithStoppingPoint = new ArrayList<>();
        graph.traverse(5, TraversalType.BREADTH_FIRST, (v1, e, v2) -> {
            pathsWithStoppingPoint.add(new GraphPath(v1, e, v2));
            return v1 == 3 && v2 == 0 ? TraverseResult.TERMINATE_PATH : TraverseResult.CONTINUE;
        });
        assertEquals(breadthFirstexpected, pathsWithStoppingPoint);
    }

    /**
     * <pre>
     *           0
     *           |
     *         -----
     *         |   |
     *         -----
     *           |
     *           1
     *           |
     *           2
     *
     *  edges:
     *  0 <-> 1 : 0
     *  0 <-> 1 : 1
     *  1 <-> 2 : 2</pre>
     */
    @Test
    void testTraverseParallelEdges() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, null); // 0
        graph.addEdge(1, 0, null); // 1
        graph.addEdge(1, 2, null); // 2

        List<GraphPath> paths = new ArrayList<>();
        graph.traverse(0, TraversalType.BREADTH_FIRST, (v1, e, v2) -> {
            paths.add(new GraphPath(v1, e, v2));
            return TraverseResult.CONTINUE;
        });

        List<GraphPath> expected = List.of(
                new GraphPath(0, 0, 1),
                new GraphPath(0, 1, 1),
                new GraphPath(1, 2, 2));
        assertEquals(expected, paths);
    }

    @Test
    void testGetVertexObjectStream() {
        graph.addVertex();
        graph.addVertex();
        Vertex vertexObj1 = new Vertex("Vertex 1");
        Vertex vertexObj2 = new Vertex("Vertex 2");
        graph.setVertexObject(0, vertexObj1);
        graph.setVertexObject(1, vertexObj2);

        assertArrayEquals(new Vertex[] {vertexObj1, vertexObj2}, graph.getVertexObjectStream().toArray());
    }

    @Test
    void testGetArrowObjectStream() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(0, 1, "Edge 1");
        graph.addEdge(1, 2, "Edge 2");
        assertArrayEquals(new String[] {"Edge 1", "Edge 2"}, graph.getEdgeObjectStream().toArray());
    }

    @Test
    void testVertexExists() {
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
    void removeIsolatedVertices() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addEdge(1, 2, null);
        graph.setVertexObject(0, new Vertex(""));

        assertEquals(3, graph.getVertexCount());
        // Vertex 0 is isolated but has an associated object: it must not be removed.
        graph.removeIsolatedVertices();
        assertEquals(3, graph.getVertexCount());
        graph.setVertexObject(0, null);
        // Now vertex 0 must be removed.
        graph.removeIsolatedVertices();
        assertEquals(2, graph.getVertexCount());
    }

    private record GraphPath(int v1, int e, int v2) {
    }
}
