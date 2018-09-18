/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.graph;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.powsybl.commons.PowsyblException;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UndirectedGraphImpl<V, E> implements UndirectedGraph<V, E> {

    private static final int VERTICES_CAPACITY = 10;

    private static final int EDGES_CAPACITY = 15;

    private static final int NEIGHBORS_CAPACITY = 2;

    private static final class Vertex<E> {

        private E object;

        private Vertex() {
        }

        public E getObject() {
            return object;
        }

        public void setObject(E object) {
            this.object = object;
        }

    }

    private static final class Edge<E> {

        private final int v1;

        private final int v2;

        private E object;

        private Edge(int v1, int v2, E object) {
            this.v1 = v1;
            this.v2 = v2;
            this.object = object;
        }

        public int getV1() {
            return v1;
        }

        public int getV2() {
            return v2;
        }

        public E getObject() {
            return object;
        }

        public void setObject(E object) {
            this.object = object;
        }

        @Override
        public String toString() {
            return object.toString();
        }

    }

    /* vertices */
    private final List<Vertex<V>> vertices = new ArrayList<>(VERTICES_CAPACITY);

    /* edges */
    private final List<Edge<E>> edges = new ArrayList<>(EDGES_CAPACITY);

    /* cached adjacency list */
    private TIntArrayList[] adjacencyListCache;

    private final Lock adjacencyListCacheLock = new ReentrantLock();

    private final TIntLinkedList removedVertices = new TIntLinkedList();

    private final TIntLinkedList removedEdges = new TIntLinkedList();

    private final List<UndirectedGraphListener> listeners = new CopyOnWriteArrayList<>();

    private void checkVertex(int v) {
        if (v < 0 || v >= vertices.size() || vertices.get(v) == null) {
            throw new PowsyblException("Vertex " + v + " not found");
        }
    }

    private void checkEdge(int e) {
        if (e < 0 || e >= edges.size() || edges.get(e) == null) {
            throw new PowsyblException("Edge " + e + " not found");
        }
    }

    @Override
    public int addVertex() {
        int v;
        if (removedVertices.isEmpty()) {
            v = vertices.size();
            vertices.add(new Vertex<V>());
        } else {
            v = removedVertices.removeAt(0);
        }
        invalidateAdjacencyList();
        notifyListener();
        return v;
    }

    @Override
    public V removeVertex(int v) {
        checkVertex(v);
        for (Edge<E> e : edges) {
            if (e != null && (e.getV1() == v || e.getV2() == v)) {
                throw new PowsyblException("An edge is connected to vertex " + v);
            }
        }
        V obj = vertices.get(v).getObject();
        if (v == vertices.size() - 1) {
            vertices.remove(v);
        } else {
            vertices.set(v, null);
            removedVertices.add(v);
        }
        invalidateAdjacencyList();
        notifyListener();
        return obj;
    }

    @Override
    public int getVertexCount() {
        return vertices.size() - removedVertices.size();
    }

    @Override
    public void removeAllVertices() {
        if (!edges.isEmpty()) {
            throw new PowsyblException("Cannot remove all vertices because there is still some edges in the graph");
        }
        vertices.clear();
        removedVertices.clear();
        invalidateAdjacencyList();
        notifyListener();
    }

    @Override
    public int addEdge(int v1, int v2, E obj) {
        checkVertex(v1);
        checkVertex(v2);
        int e;
        Edge<E> edge = new Edge<>(v1, v2, obj);
        if (removedEdges.isEmpty()) {
            e = edges.size();
            edges.add(edge);
        } else {
            e = removedEdges.removeAt(0);
            edges.set(e, edge);
        }
        invalidateAdjacencyList();
        notifyListener();
        return e;
    }

    @Override
    public E removeEdge(int e) {
        checkEdge(e);
        E obj = edges.get(e).getObject();
        if (e == edges.size() - 1) {
            edges.remove(e);
        } else {
            edges.set(e, null);
            removedEdges.add(e);
        }
        invalidateAdjacencyList();
        notifyListener();
        return obj;
    }

    @Override
    public void removeAllEdges() {
        edges.clear();
        removedEdges.clear();
        invalidateAdjacencyList();
        notifyListener();
    }

    @Override
    public int getEdgeCount() {
        return edges.size() - removedEdges.size();
    }

    @Override
    public int[] getVertices() {
        TIntArrayList t = new TIntArrayList(vertices.size());
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i) != null) {
                t.add(i);
            }
        }
        return t.toArray();
    }

    @Override
    public int[] getEdges() {
        TIntArrayList t = new TIntArrayList(getEdgeCount());
        for (int e = 0; e < edges.size(); e++) {
            if (edges.get(e) != null) {
                t.add(e);
            }
        }
        return t.toArray();
    }

    @Override
    public int getMaxVertex() {
        return vertices.size();
    }

    @Override
    public Iterable<V> getVerticesObj() {
        return FluentIterable.from(vertices)
                             .filter(Predicates.notNull())
                             .transform(Vertex::getObject);
    }

    @Override
    public Stream<V> getVertexObjectStream() {
        return vertices.stream().filter(Objects::nonNull).map(Vertex::getObject);
    }

    @Override
    public V getVertexObject(int v) {
        checkVertex(v);
        return vertices.get(v).getObject();
    }

    @Override
    public void setVertexObject(int v, V obj) {
        checkVertex(v);
        vertices.get(v).setObject(obj);
    }

    @Override
    public int getEdgeVertex1(int e) {
        checkEdge(e);
        return edges.get(e).getV1();
    }

    @Override
    public int getEdgeVertex2(int e) {
        checkEdge(e);
        return edges.get(e).getV2();
    }

    @Override
    public Iterable<E> getEdgesObject() {
        return FluentIterable.from(edges)
                             .filter(Predicates.notNull())
                             .transform(Edge::getObject);
    }

    @Override
    public Stream<E> getEdgeObjectStream() {
        return edges.stream().filter(Objects::nonNull).map(Edge::getObject);
    }

    @Override
    public E getEdgeObject(int e) {
        checkEdge(e);
        return edges.get(e).getObject();
    }

    @Override
    public List<E> getEdgeObjects(int v1, int v2) {
        checkVertex(v1);
        checkVertex(v2);
        List<E> edgeObjects = new ArrayList<>(1);
        TIntArrayList[] adjacencyList = getAdjacencyList();
        TIntArrayList adjacentEdges = adjacencyList[v1];
        for (int i = 0; i < adjacentEdges.size(); i++) {
            int e = adjacentEdges.getQuick(i);
            Edge<E> edge = edges.get(e);
            if ((edge.getV1() == v1 && edge.getV2() == v2)
                || (edge.getV1() == v2 && edge.getV2() == v1)) {
                edgeObjects.add(edge.getObject());
            }
        }
        return edgeObjects;
    }

    private TIntArrayList[] getAdjacencyList() {
        adjacencyListCacheLock.lock();
        try {
            if (adjacencyListCache == null) {
                adjacencyListCache = new TIntArrayList[vertices.size()];
                for (int v = 0; v < vertices.size(); v++) {
                    Vertex<V> vertex = vertices.get(v);
                    if (vertex != null) {
                        adjacencyListCache[v] = new TIntArrayList(NEIGHBORS_CAPACITY);
                    }
                }
                for (int e = 0; e < edges.size(); e++) {
                    Edge<E> edge = edges.get(e);
                    if (edge != null) {
                        int v1 = edge.getV1();
                        int v2 = edge.getV2();
                        adjacencyListCache[v1].add(e);
                        adjacencyListCache[v2].add(e);
                    }
                }
            }
            return adjacencyListCache;
        } finally {
            adjacencyListCacheLock.unlock();
        }
    }

    private void invalidateAdjacencyList() {
        adjacencyListCache = null;
    }

    @Override
    public void traverse(int v, Traverser traverser, boolean[] encountered) {
        checkVertex(v);
        Objects.requireNonNull(traverser);
        Objects.requireNonNull(encountered);

        if (encountered.length < vertices.size()) {
            throw new PowsyblException("Encountered array is too small");
        }

        TIntArrayList[] adjacencyList = getAdjacencyList();
        TIntArrayList adjacentEdges = adjacencyList[v];
        encountered[v] = true;
        for (int i = 0; i < adjacentEdges.size(); i++) {
            int e = adjacentEdges.getQuick(i);
            Edge<E> edge = edges.get(e);
            int v1 = edge.getV1();
            int v2 = edge.getV2();
            if (!encountered[v1]) {
                if (traverser.traverse(v2, e, v1) == TraverseResult.CONTINUE) {
                    encountered[v1] = true;
                    traverse(v1, traverser, encountered);
                }
            } else if (!encountered[v2] && traverser.traverse(v1, e, v2) == TraverseResult.CONTINUE) {
                encountered[v2] = true;
                traverse(v2, traverser, encountered);
            }
        }
    }

    @Override
    public void traverse(int v, Traverser traverser) {
        boolean[] encountered = new boolean[vertices.size()];
        Arrays.fill(encountered, false);
        traverse(v, traverser, encountered);
    }

    @Override
    public List<TIntArrayList> findAllPaths(int from, Function<V, Boolean> pathComplete, Function<E, Boolean> pathCanceled) {
        Objects.requireNonNull(pathComplete);
        List<TIntArrayList> paths = new ArrayList<>();
        BitSet encountered = new BitSet(vertices.size());
        TIntArrayList path = new TIntArrayList(1);
        findAllPaths(from, pathComplete, pathCanceled, path, encountered, paths);
        // sort paths by size
        paths.sort((o1, o2) -> o1.size() - o2.size());
        return paths;
    }

    private boolean findAllPaths(int e, int v1or2, Function<V, Boolean> pathComplete, Function<E, Boolean> pathCanceled,
                                 TIntArrayList path, BitSet encountered, List<TIntArrayList> paths) {
        if (encountered.get(v1or2)) {
            return false;
        }
        Vertex<V> obj1or2 = vertices.get(v1or2);
        path.add(e);
        if (pathComplete.apply(obj1or2.getObject())) {
            paths.add(path);
            return true;
        } else {
            findAllPaths(v1or2, pathComplete, pathCanceled, path, encountered, paths);
            return false;
        }
    }

    private void findAllPaths(int v, Function<V, Boolean> pathComplete, Function<E, Boolean> pathCanceled,
                              TIntArrayList path, BitSet encountered, List<TIntArrayList> paths) {
        checkVertex(v);
        encountered.set(v, true);
        TIntArrayList[] adjacencyList = getAdjacencyList();
        TIntArrayList adjacentEdges = adjacencyList[v];
        for (int i = 0; i < adjacentEdges.size(); i++) {
            int e = adjacentEdges.getQuick(i);
            Edge<E> edge = edges.get(e);
            if (pathCanceled != null && pathCanceled.apply(edge.getObject())) {
                continue;
            }
            int v1 = edge.getV1();
            int v2 = edge.getV2();
            TIntArrayList path2;
            BitSet encountered2;
            if (i < adjacentEdges.size() - 1) {
                path2 = new TIntArrayList(path);
                encountered2 = new BitSet(vertices.size());
                encountered2.or(encountered);
            } else {
                path2 = path;
                encountered2 = encountered;
            }
            if (v == v2) {
                findAllPaths(e, v1, pathComplete, pathCanceled, path2, encountered2, paths);
            } else if (v == v1) {
                findAllPaths(e, v2, pathComplete, pathCanceled, path2, encountered2, paths);
            } else {
                throw new AssertionError();
            }
        }
    }

    @Override
    public void addListener(UndirectedGraphListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListener(UndirectedGraphListener l) {
        listeners.remove(l);
    }

    private void notifyListener() {
        for (UndirectedGraphListener l : listeners) {
            l.graphChanged();
        }
    }

    @Override
    public void print(PrintStream out, Function<V, String> vertexToString, Function<E, String> edgeToString) {
        out.append("Vertices:").append(System.lineSeparator());
        for (int v = 0; v < vertices.size(); v++) {
            Vertex<V> vertex = vertices.get(v);
            if (vertex != null) {
                String str = vertexToString == null ? Objects.toString(vertex.getObject()) : vertexToString.apply(vertex.getObject());
                out.append(Integer.toString(v)).append(": ")
                        .append(str)
                        .append(System.lineSeparator());
            }
        }
        out.append("Edges:").append(System.lineSeparator());
        for (int e = 0; e < edges.size(); e++) {
            Edge<E> edge = edges.get(e);
            if (edge != null) {
                String str = edgeToString == null ? Objects.toString(edge.getObject()) : edgeToString.apply(edge.getObject());
                out.append(Integer.toString(e)).append(": ")
                        .append(Integer.toString(edge.getV1())).append("<->")
                        .append(Integer.toString(edge.getV2())).append(" ")
                        .append(str).append(System.lineSeparator());
            }
        }
    }

}
