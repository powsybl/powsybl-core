/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.powsybl.commons.PowsyblException;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.hash.TIntHashSet;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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

    private final TIntHashSet availableVertices = new TIntHashSet();

    private final TIntLinkedList removedEdges = new TIntLinkedList();

    private final List<UndirectedGraphListener<V, E>> listeners = new CopyOnWriteArrayList<>();

    private final int vertexLimit;

    public UndirectedGraphImpl(int vertexLimit) {
        if (vertexLimit < 1) {
            throw new PowsyblException("Vertex limit should be positive");
        }
        this.vertexLimit = vertexLimit;
    }

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

    public int addVertex() {
        return addVertex(true);
    }

    @Override
    public int addVertex(boolean notify) {
        int v;
        if (availableVertices.isEmpty()) {
            v = vertices.size();
            vertices.add(new Vertex<>());
        } else {
            v = availableVertices.iterator().next();
            availableVertices.remove(v);
            vertices.set(v, new Vertex<>());
        }
        invalidateAdjacencyList();
        if (notify) {
            notifyVertexAdded(v);
        }
        return v;
    }

    @Override
    public void addVertexIfNotPresent(int v) {
        addVertexIfNotPresent(v, true);
    }

    @Override
    public void addVertexIfNotPresent(int v, boolean notify) {
        if (v < 0) {
            throw new PowsyblException("Invalid vertex " + v);
        }
        if (v >= this.vertexLimit) {
            throw new PowsyblException("Vertex index too high: " + v + ". Limit is " + this.vertexLimit);
        }
        if (v < vertices.size()) {
            if (availableVertices.contains(v)) {
                vertices.set(v, new Vertex<>());
                availableVertices.remove(v);
                invalidateAdjacencyList();
                if (notify) {
                    notifyVertexAdded(v);
                }
            }
        } else {
            for (int i = vertices.size(); i < v; i++) {
                vertices.add(null);
                availableVertices.add(i);
            }
            vertices.add(new Vertex<>());
            invalidateAdjacencyList();
            if (notify) {
                notifyVertexAdded(v);
            }
        }
    }

    @Override
    public boolean vertexExists(int v) {
        if (v < 0) {
            throw new PowsyblException("Invalid vertex " + v);
        }
        return v < vertices.size() && vertices.get(v) != null;
    }

    private V removeVertexInternal(int v, boolean notify) {
        V obj = vertices.get(v).getObject();
        if (v == vertices.size() - 1) {
            vertices.remove(v);
            cleanVertices(v - 1);
        } else {
            vertices.set(v, null);
            availableVertices.add(v);
        }
        if (notify) {
            notifyVertexRemoved(v, obj);
        }
        return obj;
    }

    @Override
    public V removeVertex(int v) {
        return removeVertex(v, true);
    }

    @Override
    public V removeVertex(int v, boolean notify) {
        checkVertex(v);
        for (Edge<E> e : edges) {
            if (e != null && (e.getV1() == v || e.getV2() == v)) {
                throw new PowsyblException("An edge is connected to vertex " + v);
            }
        }
        V obj = removeVertexInternal(v, notify);
        invalidateAdjacencyList();
        return obj;
    }

    private void cleanVertices(int v) {
        for (int i = v; i >= 0; i--) {
            if (!availableVertices.contains(i)) {
                return;
            }
            availableVertices.remove(i);
            vertices.remove(i);
        }
    }

    @Override
    public int getVertexCount() {
        return vertices.size() - availableVertices.size();
    }

    @Override
    public void removeAllVertices() {
        removeAllVertices(true);
    }

    @Override
    public void removeAllVertices(boolean notify) {
        if (!edges.isEmpty()) {
            throw new PowsyblException("Cannot remove all vertices because there is still some edges in the graph");
        }
        vertices.clear();
        availableVertices.clear();
        invalidateAdjacencyList();
        if (notify) {
            notifyAllVerticesRemoved();
        }
    }

    @Override
    public int addEdge(int v1, int v2, E obj) {
        return addEdge(v1, v2, obj, true);
    }

    @Override
    public int addEdge(int v1, int v2, E obj, boolean notify) {
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
        if (notify) {
            notifyEdgeAdded(e, obj);
        }
        return e;
    }

    private E removeEdgeInternal(int e, boolean notify) {
        E obj = edges.get(e).getObject();
        if (notify) {
            notifyEdgeBeforeRemoval(e, obj);
        }
        if (e == edges.size() - 1) {
            edges.remove(e);
        } else {
            edges.set(e, null);
            removedEdges.add(e);
        }
        if (notify) {
            notifyEdgeRemoved(e, obj);
        }
        return obj;
    }

    @Override
    public E removeEdge(int e) {
        return removeEdge(e, true);
    }

    @Override
    public E removeEdge(int e, boolean notify) {
        checkEdge(e);
        E obj = removeEdgeInternal(e, notify);
        invalidateAdjacencyList();
        return obj;
    }

    @Override
    public void removeAllEdges() {
        removeAllEdges(true);
    }

    @Override
    public void removeAllEdges(boolean notify) {
        Collection<E> allEdges = edges.stream().filter(Objects::nonNull).map(Edge::getObject).collect(Collectors.toList());
        if (notify) {
            notifyAllEdgesBeforeRemoval(allEdges);
        }
        edges.clear();
        removedEdges.clear();
        invalidateAdjacencyList();
        if (notify) {
            notifyAllEdgesRemoved(allEdges);
        }
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
    public int getVertexCapacity() {
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
        setVertexObject(v, obj, true);
    }

    @Override
    public void setVertexObject(int v, V obj, boolean notify) {
        checkVertex(v);
        vertices.get(v).setObject(obj);
        if (notify) {
            notifyVertexObjectSet(v, obj);
        }
    }

    @Override
    public int getEdgeVertex1(int e) {
        checkEdge(e);
        return edges.get(e).getV1();
    }

    @Override
    public List<E> getEdgeObjectsConnectedToVertex(int v) {
        return getEdgeObjectConnectedToVertexStream(v).collect(Collectors.toList());
    }

    @Override
    public Stream<E> getEdgeObjectConnectedToVertexStream(int v) {
        return getEdgeConnectedToVertexStream(v).mapToObj(this::getEdgeObject);
    }

    @Override
    public List<Integer> getEdgesConnectedToVertex(int v) {
        return getEdgeConnectedToVertexStream(v).boxed().collect(Collectors.toList());
    }

    @Override
    public IntStream getEdgeConnectedToVertexStream(int v) {
        checkVertex(v);
        TIntArrayList[] adjacencyList = getAdjacencyList();
        TIntArrayList adjacentEdges = adjacencyList[v];
        return IntStream.range(0, adjacentEdges.size()).map(adjacentEdges::getQuick);
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
            if (edge.getV1() == v1 && edge.getV2() == v2
                    || edge.getV1() == v2 && edge.getV2() == v1) {
                edgeObjects.add(edge.getObject());
            }
        }
        return edgeObjects;
    }

    /**
     * Create the adjacency list of this graph.
     * @return the adjacency list as a {@link TIntArrayList} of {@link TIntArrayList}.
     */
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

    /**
     * Invalidate the adjacency list.
     */
    private void invalidateAdjacencyList() {
        adjacencyListCache = null;
    }

    private void traverseVertex(int vToTraverse, boolean[] vEncountered, boolean[] eEncountered, Deque<EdgeToTraverse> edgesToTraverse,
                                TIntArrayList[] adjacencyList, TraversalType traversalType) {
        if (vEncountered[vToTraverse]) {
            return;
        }
        vEncountered[vToTraverse] = true;
        TIntArrayList adjacentEdges = adjacencyList[vToTraverse];
        for (int i = 0; i < adjacentEdges.size(); i++) {
            // For depth-first traversal, we're going to poll the last element added in the deque. Hence, edges have to
            // be added in reverse order, otherwise the depth-first traversal will be "on the right side" instead of
            // "on the left side" of the tree. That is, it would always go deeper taking with last neighbour of visited vertex.
            int iEdge = switch (traversalType) {
                case DEPTH_FIRST -> adjacentEdges.size() - i - 1;
                case BREADTH_FIRST -> i;
            };

            int adjacentEdgeIndex = adjacentEdges.getQuick(iEdge);

            if (!eEncountered[adjacentEdgeIndex]) {
                eEncountered[adjacentEdgeIndex] = true;
                Edge<E> edge = edges.get(adjacentEdgeIndex);
                boolean flippedEdge = edge.v1 != vToTraverse;
                edgesToTraverse.add(new EdgeToTraverse(adjacentEdgeIndex, flippedEdge));
            }
        }
    }

    /**
     * Record to store which edge has to be traversed and in which direction
     * @param index index of the edge within the edges list
     * @param flippedDirection if true, edge.getNode2() has already been visited, otherwise it's edge.getNode1()
     */
    private record EdgeToTraverse(int index, boolean flippedDirection) {
    }

    @Override
    public boolean traverse(int v, TraversalType traversalType, Traverser traverser, boolean[] encounteredVertices) {
        checkVertex(v);
        Objects.requireNonNull(traverser);
        Objects.requireNonNull(encounteredVertices);

        if (encounteredVertices.length < vertices.size()) {
            throw new PowsyblException("Encountered array is too small");
        }

        boolean[] encounteredEdges = new boolean[edges.size()];
        TIntArrayList[] adjacencyList = getAdjacencyList();
        boolean keepGoing = true;

        Deque<EdgeToTraverse> edgesToTraverse = new ArrayDeque<>();
        traverseVertex(v, encounteredVertices, encounteredEdges, edgesToTraverse, adjacencyList, traversalType);
        while (!edgesToTraverse.isEmpty() && keepGoing) {
            EdgeToTraverse edgeToTraverse = switch (traversalType) {
                case DEPTH_FIRST -> edgesToTraverse.pollLast();
                case BREADTH_FIRST -> edgesToTraverse.pollFirst();
            };

            Edge<E> edge = edges.get(edgeToTraverse.index);
            int vOrigin = edgeToTraverse.flippedDirection ? edge.getV2() : edge.getV1();
            int vDest = edgeToTraverse.flippedDirection ? edge.getV1() : edge.getV2();
            TraverseResult traverserResult = traverser.traverse(vOrigin, edgeToTraverse.index, vDest);
            switch (traverserResult) {
                case CONTINUE -> traverseVertex(vDest, encounteredVertices, encounteredEdges, edgesToTraverse, adjacencyList, traversalType);
                case TERMINATE_TRAVERSER -> keepGoing = false; // the whole traversing needs to stop
                case TERMINATE_PATH -> {
                    // Path ends on edge e before reaching vDest, continuing with next edge in the deque
                }
            }
        }
        return keepGoing;
    }

    @Override
    public boolean traverse(int v, TraversalType traversalType, Traverser traverser) {
        boolean[] encountered = new boolean[vertices.size()];
        Arrays.fill(encountered, false);
        return traverse(v, traversalType, traverser, encountered);
    }

    @Override
    public boolean traverse(int[] startingVertices, TraversalType traversalType, Traverser traverser) {
        boolean[] encountered = new boolean[vertices.size()];
        Arrays.fill(encountered, false);
        for (int startingVertex : startingVertices) {
            if (!encountered[startingVertex] && !traverse(startingVertex, traversalType, traverser, encountered)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method allocates a {@link List} of {@link TIntArrayList} to store the paths, a {@link BitSet} to store the encountered vertices
     * and calls {@link #findAllPaths(int, Predicate, Predicate, TIntArrayList, BitSet, List)}.
     * </p>
     * In the output, the paths are sorted by size considering the number of switches in each path.
     */
    @Override
    public List<TIntArrayList> findAllPaths(int from, Predicate<V> pathComplete, Predicate<? super E> pathCancelled) {
        return findAllPaths(from, pathComplete, pathCancelled, Comparator.comparing(TIntArrayList::size));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method allocates a {@link List} of {@link TIntArrayList} to store the paths, a {@link BitSet} to store the encountered vertices
     * and calls {@link #findAllPaths(int, Predicate, Predicate, TIntArrayList, BitSet, List)}.
     * In the output, the paths are sorted by using the given comparator.
     * </p>
     */
    public List<TIntArrayList> findAllPaths(int from, Predicate<V> pathComplete, Predicate<? super E> pathCancelled, Comparator<TIntArrayList> comparator) {
        Objects.requireNonNull(pathComplete);
        List<TIntArrayList> paths = new ArrayList<>();
        BitSet encountered = new BitSet(vertices.size());
        TIntArrayList path = new TIntArrayList(1);
        findAllPaths(from, pathComplete, pathCancelled, path, encountered, paths);

        // sort paths by size according to the given comparator
        paths.sort(comparator);
        return paths;
    }

    /**
     * This method is called by {@link #findAllPaths(int, Predicate, Predicate, TIntArrayList, BitSet, List)} each time a vertex is traversed.
     * The path is added to the paths list if it's complete, otherwise this method calls the {@link #findAllPaths(int, Predicate, Predicate, TIntArrayList, BitSet, List)}
     * to continue the recursion.
     *
     * @param e the index of the current edge.
     * @param v1or2 the index of the current vertex.
     * @param pathComplete a function that returns true when the target vertex is found.
     * @param pathCancelled pathCanceled a function that returns true when the edge must not be traversed.
     * @param path a list that contains the traversed edges.
     * @param encountered a BitSet that contains the traversed vertex.
     * @param paths a list that contains the complete paths.
     * @return true if the path is complete, false otherwise.
     */
    private boolean findAllPaths(int e, int v1or2, Predicate<V> pathComplete, Predicate<? super E> pathCancelled,
                                 TIntArrayList path, BitSet encountered, List<TIntArrayList> paths) {
        if (encountered.get(v1or2)) {
            return false;
        }
        Vertex<V> obj1or2 = vertices.get(v1or2);
        path.add(e);
        if (Boolean.TRUE.equals(pathComplete.test(obj1or2.getObject()))) {
            paths.add(path);
            return true;
        } else {
            findAllPaths(v1or2, pathComplete, pathCancelled, path, encountered, paths);
            return false;
        }
    }

    /**
     * This method is called by {@link #findAllPaths(int, Predicate, Predicate)} or {@link #findAllPaths(int, int, Predicate, Predicate, TIntArrayList, BitSet, List)}.
     * For each adjacent edges for which the pathCanceled returns {@literal false}, traverse the other vertex calling {@link #findAllPaths(int, int, Predicate, Predicate, TIntArrayList, BitSet, List)}.
     *
     * @param v the current vertex
     * @param pathComplete a function that returns true when the target vertex is found.
     * @param pathCancelled a function that returns true when the edge must not be traversed.
     * @param path a list that contains the traversed edges.
     * @param encountered a BitSet that contains the traversed vertex.
     * @param paths a list that contains the complete paths.
     */
    private void findAllPaths(int v, Predicate<V> pathComplete, Predicate<? super E> pathCancelled,
                              TIntArrayList path, BitSet encountered, List<TIntArrayList> paths) {
        checkVertex(v);
        encountered.set(v, true);
        TIntArrayList[] adjacencyList = getAdjacencyList();
        TIntArrayList adjacentEdges = adjacencyList[v];
        for (int i = 0; i < adjacentEdges.size(); i++) {
            int e = adjacentEdges.getQuick(i);
            Edge<E> edge = edges.get(e);
            if (pathCancelled != null && pathCancelled.test(edge.getObject())) {
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
                findAllPaths(e, v1, pathComplete, pathCancelled, path2, encountered2, paths);
            } else if (v == v1) {
                findAllPaths(e, v2, pathComplete, pathCancelled, path2, encountered2, paths);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public void addListener(UndirectedGraphListener<V, E> l) {
        listeners.add(l);
    }

    @Override
    public void removeListener(UndirectedGraphListener<V, E> l) {
        listeners.remove(l);
    }

    private void notifyVertexAdded(int v) {
        for (UndirectedGraphListener<V, E> l : listeners) {
            l.vertexAdded(v);
        }
    }

    private void notifyVertexObjectSet(int v, V obj) {
        for (UndirectedGraphListener<V, E> l : listeners) {
            l.vertexObjectSet(v, obj);
        }
    }

    private void notifyVertexRemoved(int v, V obj) {
        for (UndirectedGraphListener<V, E> l : listeners) {
            l.vertexRemoved(v, obj);
        }
    }

    private void notifyAllVerticesRemoved() {
        for (UndirectedGraphListener<V, E> l : listeners) {
            l.allVerticesRemoved();
        }
    }

    private void notifyEdgeAdded(int e, E obj) {
        for (UndirectedGraphListener<V, E> l : listeners) {
            l.edgeAdded(e, obj);
        }
    }

    private void notifyEdgeRemoved(int e, E obj) {
        for (UndirectedGraphListener<V, E> l : listeners) {
            l.edgeRemoved(e, obj);
        }
    }

    private void notifyEdgeBeforeRemoval(int e, E obj) {
        for (UndirectedGraphListener<V, E> l : listeners) {
            l.edgeBeforeRemoval(e, obj);
        }
    }

    private void notifyAllEdgesBeforeRemoval(Collection<E> obj) {
        for (UndirectedGraphListener<V, E> l : listeners) {
            l.allEdgesBeforeRemoval(obj);
        }
    }

    private void notifyAllEdgesRemoved(Collection<E> obj) {
        for (UndirectedGraphListener<V, E> l : listeners) {
            l.allEdgesRemoved(obj);
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

    private void removeIsolatedVertices(int v, TIntArrayList[] adjacencyList, boolean notify) {
        Vertex<V> vertex = vertices.get(v);
        if (vertex != null && vertex.getObject() == null) {
            TIntArrayList adjacentEdges = adjacencyList[v];
            if (adjacentEdges.isEmpty()) {

                removeVertexInternal(v, notify);
                adjacencyList[v] = null;

                if (!adjacentEdges.isEmpty()) {
                    int e = adjacentEdges.getQuick(0);
                    removeDanglingEdgeAndPropagate(e, v, adjacencyList, notify);
                }
            }
        }
    }

    private void removeDanglingEdgeAndPropagate(int edgeToRemove, int vFrom, TIntArrayList[] adjacencyList, boolean notify) {
        Edge<E> edge = edges.get(edgeToRemove);
        int v1 = edge.getV1();
        int v2 = edge.getV2();
        int vTo = v1 == vFrom ? v2 : v1;

        // updating adjacency list of vFrom & vTo is not done here, as:
        //  - vFrom adjacency list has been set to null when vertex vFrom has been removed
        //  - vTo adjacency list is updated hereafter
        removeEdgeInternal(edgeToRemove, notify);

        Vertex<V> vertex = vertices.get(vTo);
        TIntArrayList adjacentEdges = adjacencyList[vTo];
        if (vertex == null || vertex.getObject() != null || adjacentEdges.size() > 2) {
            // propagation stops: update adjacency list of vertex
            adjacentEdges.remove(edgeToRemove);
            return;
        }

        // propagate: we know that one of the neighbours (vFrom) of this vertex has been removed, hence:
        //  - if only one adjacent edge, this is a newly isolated vertex
        //  - if only two adjacent edges, this is a newly dangling vertex
        removeVertexInternal(vTo, notify);
        adjacencyList[vTo] = null;

        // find the other edge to remove if dangling vertex
        if (adjacentEdges.size() == 2) {
            int otherEdgeToRemove = adjacentEdges.getQuick(0) == edgeToRemove
                    ? adjacentEdges.getQuick(1)
                    : adjacentEdges.getQuick(0);
            removeDanglingEdgeAndPropagate(otherEdgeToRemove, vTo, adjacencyList, notify);
        }

    }

    @Override
    public void removeIsolatedVertices() {
        removeIsolatedVertices(true);
    }

    @Override
    public void removeIsolatedVertices(boolean notify) {
        TIntArrayList[] adjacencyList = getAdjacencyList();
        for (int v = 0; v < vertices.size(); v++) {
            removeIsolatedVertices(v, adjacencyList, notify);
        }
    }
}
