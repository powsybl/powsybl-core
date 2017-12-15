/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastien MURGEY <sebastien.murgey at rte-france.com>
 */
public class DirectedGraphImplTest {

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

    private DirectedGraph<Vertex, Object> graph;

    public DirectedGraphImplTest() {
    }

    @Before
    public void setUp() {
        graph = new DirectedGraphImpl<>();
    }

    @After
    public void tearDown() {
        graph = null;
    }

    @Test
    public void testConstructor() {
        assertEquals(0, graph.getVertexCount());
        assertEquals(0, graph.getArrowCount());
    }

    @Test
    public void testAddVertex() {
        graph.addVertex();
        assertEquals(1, graph.getVertexCount());
    }

    @Test
    public void testAddArrow() {
        graph.addVertex();
        graph.addVertex();
        int a = graph.addArrow(0, 1, null);
        assertEquals(2, graph.getVertexCount());
        assertEquals(0, a);
        assertEquals(1, graph.getArrowCount());
        assertEquals(0, graph.getArrowTail(a));
        assertEquals(1, graph.getArrowHead(a));
        int a2 = graph.addArrow(1, 0, null);
        assertEquals(2, graph.getVertexCount());
        assertEquals(1, a2);
        assertEquals(2, graph.getArrowCount());
        assertEquals(1, graph.getArrowTail(a2));
        assertEquals(0, graph.getArrowHead(a2));
    }

    @Test
    public void testRemoveArrow() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        int a = graph.addArrow(0, 1, null);
        graph.removeArrow(a);
        assertEquals(0, graph.getArrowCount());
        a = graph.addArrow(0, 1, null);
        assertEquals(0, a);
        int a2 = graph.addArrow(1, 2, null);
        graph.removeArrow(a);
        assertEquals(1, graph.getArrowCount());
        a = graph.addArrow(0, 1, null);
        assertEquals(0, a);
        graph.removeArrow(a);
        graph.removeArrow(a2);
        assertEquals(0, graph.getArrowCount());
    }

    @Test
    public void testRemoveVertex() {
        graph.addVertex();
        graph.addVertex();
        graph.setVertexObject(0, new Vertex("Vertex 0"));
        graph.setVertexObject(1, new Vertex("Vertex 1"));
        assertEquals(2, graph.getVertexCount());
        assertEquals("Vertex 0", graph.removeVertex(0).toString());
        assertEquals(1, graph.getVertexCount());
        assertEquals("Vertex 1", graph.removeVertex(1).toString());
        assertEquals(0, graph.getVertexCount());
    }

    @Test
    public void testRemoveVertexException() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addArrow(0, 1, null);
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
        graph.addArrow(0, 1, null);
        exception.expect(PowsyblException.class);
        graph.removeAllVertices();
    }

    @Test
    public void testRemoveAllArrows() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addArrow(0, 1, null);
        graph.addArrow(1, 0, null);
        assertEquals(2, graph.getArrowCount());
        graph.removeAllArrows();
        assertEquals(0, graph.getArrowCount());
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
        graph.addArrow(0, 1, "Arrow 1");
        graph.addArrow(1, 2, "Arrow 2");
        assertArrayEquals(new String[] {"Arrow 1", "Arrow 2"}, graph.getArrowObjectStream().toArray());
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
    public void testGetArrows() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addArrow(0, 1, null);
        graph.addArrow(1, 2, null);
        assertArrayEquals(new int[]{0, 1}, graph.getArrows());
    }

    @Test
    public void testGetArrowObject() {
        graph.addVertex();
        graph.addVertex();
        int a = graph.addArrow(0, 1, "Arrow");
        assertEquals("Arrow", graph.getArrowObject(a));
    }

    @Test
    public void testGetArrowObjects() {
        graph.addVertex();
        graph.addVertex();
        int a = graph.addArrow(0, 1, "Arrow");
        assertEquals(1, graph.getArrowObjects(0, 1).size());
        assertEquals(0, graph.getArrowObjects(1, 0).size());
    }

    @Test
    public void testAddListener() {
        DirectedGraphListener listener1 = Mockito.mock(DirectedGraphListener.class);
        DirectedGraphListener listener2 = Mockito.mock(DirectedGraphListener.class);
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
        DirectedGraphListener listener1 = Mockito.mock(DirectedGraphListener.class);
        DirectedGraphListener listener2 = Mockito.mock(DirectedGraphListener.class);
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
     *         v   v   v
     *         1   2   3
     *         ^   |   ^
     *         |   |   |
     *         -----   |
     *           ||    |
     *           |v    |
     *           4     |
     *           ^     |
     *           |     |
     *           -------
     *              ||
     *              5
     *
     *  arrows:
     *  0 --> 1 : 0
     *  0 --> 2 : 1
     *  0 --> 3 : 2
     *  1 <-- 4 : 3
     *  2 --> 4 : 4
     *  4 <-- 5 : 5
     *  3 --> 5 : 6
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
        graph.addArrow(0, 1, null); // 0
        graph.addArrow(0, 2, null); // 1
        graph.addArrow(0, 3, null); // 2
        graph.addArrow(4, 1, null); // 3
        graph.addArrow(2, 4, null); // 4
        graph.addArrow(5, 4, null); // 5
        graph.addArrow(5, 3, null); // 6

        Traverser<Object> traverser = Mockito.mock(Traverser.class);
        // Stops  on 1, 2 and 3
        Mockito.when(traverser.traverse(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(TraverseResult.CONTINUE);
        boolean[] encountered = new boolean[graph.getVertexCount()];
        Arrays.fill(encountered, false);
        graph.traverse(5, traverser, encountered);
        assertArrayEquals(new boolean[] {false, true, false, true, true, true}, encountered);
        graph.traverse(0, traverser);
    }

    /**
     *           0
     *           |
     *         ---------
     *         |   |   |
     *         v   v   v
     *         1   2   3
     *         ^   |   ^
     *         |   |   |
     *         -----   |
     *           ||    |
     *           |v    |
     *           4     |
     *           ^     |
     *           |     |
     *           -------
     *              ||
     *              5
     *
     *  arrows:
     *  0 --> 1 : 0
     *  0 --> 2 : 1
     *  0 --> 3 : 2
     *  1 <-- 4 : 3
     *  2 --> 4 : 4
     *  4 <-- 5 : 5
     *  3 <-- 5 : 6
     *
     *  all paths (edge numbers) between vertex 0 and 5:
     *  0, 3, 5
     *  1, 4, 5
     *  2, 6
     */
    @Test
    public void testPrintGraph() {
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.addVertex();
        graph.setVertexObject(5, new Vertex("end"));
        graph.addArrow(0, 1, null); // 0
        graph.addArrow(0, 2, null); // 1
        graph.addArrow(0, 3, null); // 2
        graph.addArrow(4, 1, null); // 3
        graph.addArrow(2, 4, null); // 4
        graph.addArrow(5, 4, null); // 5
        graph.addArrow(5, 3, null); // 6
        graph.print(System.out, null, null);
    }


    /**
     *           0
     *           |
     *         ---------
     *         |   |   |
     *         v   v   v
     *         1   2   3
     *         ^   |   ^
     *         |   |   |
     *         -----   |
     *           ||    |
     *           |v    |
     *           4     |
     *           |     |
     *           -------
     *              v|
     *              5
     *
     *  arrows:
     *  0 --> 1 : 0
     *  0 --> 2 : 1
     *  0 --> 3 : 2
     *  1 <-- 4 : 3
     *  2 --> 4 : 4
     *  4 --> 5 : 5
     *  3 <-- 5 : 6
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
        graph.addArrow(0, 1, null); // 0
        graph.addArrow(0, 2, null); // 1
        graph.addArrow(0, 3, null); // 2
        graph.addArrow(4, 1, null); // 3
        graph.addArrow(2, 4, null); // 4
        graph.addArrow(4, 5, null); // 5
        graph.addArrow(5, 3, null); // 6
        List<TIntArrayList> paths = graph.findAllPaths(0, vertex -> vertex != null && "end".equals(vertex.name), null);
        assertTrue(paths.size() == 1);
        assertArrayEquals(paths.get(0).toArray(), new int[] {1, 4, 5});
    }
}
