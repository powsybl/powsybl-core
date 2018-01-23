/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UndirectedGraphImpl<V, E> extends AbstractGraph<V, E> implements UndirectedGraph<V, E> {

    private final Lock adjacencyListCacheLock = new ReentrantLock();

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

    @Override
    public void traverse(int v, Traverser<E> traverser, boolean[] encountered) {
        checkVertex(v);
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
    public void traverse(int v, Traverser<E> traverser) {
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

    protected TIntArrayList[] getAdjacencyList() {
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
                if (findAllPaths(e, v1, pathComplete, pathCanceled, path2, encountered2, paths)) {
                    continue;
                }
            } else if (v == v1) {
                if (findAllPaths(e, v2, pathComplete, pathCanceled, path2, encountered2, paths)) {
                    continue;
                }
            } else {
                throw new AssertionError();
            }
        }
    }

    @Override
    public void print(PrintStream out, Function<V, String> vertexToString, Function<E, String> edgeToString) {
        out.append("Vertices:\n");
        for (int v = 0; v < vertices.size(); v++) {
            Vertex<V> vertex = vertices.get(v);
            if (vertex != null) {
                String str = vertexToString == null ? Objects.toString(vertex.getObject()) : vertexToString.apply(vertex.getObject());
                out.append(Integer.toString(v)).append(": ")
                        .append(str)
                        .append("\n");
            }
        }
        out.append("Edges:\n");
        for (int e = 0; e < edges.size(); e++) {
            Edge<E> edge = edges.get(e);
            if (edge != null) {
                String str = edgeToString == null ? Objects.toString(edge.getObject()) : edgeToString.apply(edge.getObject());
                out.append(Integer.toString(e)).append(": ")
                        .append(Integer.toString(edge.getV1())).append("<->")
                        .append(Integer.toString(edge.getV2())).append(" ")
                        .append(str).append("\n");
            }
        }
    }
}
