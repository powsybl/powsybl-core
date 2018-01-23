package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class DirectedGraphImpl<V, E> extends AbstractGraph<V, E> implements DirectedGraph<V, E> {

    protected final Lock adjacencyListCacheLock = new ReentrantLock();

    @Override
    public int getEdgeTail(int e) {
        checkEdge(e);
        return edges.get(e).getV1();
    }

    @Override
    public int getEdgeHead(int e) {
        checkEdge(e);
        return edges.get(e).getV2();
    }

    @Override
    public List<E> getEdgeObjects(int tail, int head) {
        checkVertex(tail);
        checkVertex(head);
        List<E> edgeObjects = new ArrayList<>(1);
        TIntArrayList[] adjacencyList = getAdjacencyList();
        TIntArrayList adjacentEdges = adjacencyList[tail];
        for (int i = 0; i < adjacentEdges.size(); i++) {
            int e = adjacentEdges.getQuick(i);
            Edge<E> edge = edges.get(e);
            if (edge.getV1() == tail && edge.getV2() == head) {
                edgeObjects.add(edge.getObject());
            }
        }
        return edgeObjects;
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
                        int tail = edge.getV1();
                        adjacencyListCache[tail].add(e);
                    }
                }
            }
            return adjacencyListCache;
        } finally {
            adjacencyListCacheLock.unlock();
        }
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

    private boolean findAllPaths(int a, int v1or2, Function<V, Boolean> pathComplete, Function<E, Boolean> pathCanceled,
                                 TIntArrayList path, BitSet encountered, List<TIntArrayList> paths) {
        if (encountered.get(v1or2)) {
            return false;
        }
        Vertex<V> obj1or2 = vertices.get(v1or2);
        path.add(a);
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
            int head = edge.getV2();
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

            if (findAllPaths(e, head, pathComplete, pathCanceled, path2, encountered2, paths)) {
                continue;
            }
        }
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
            int tail = edge.getV1();
            int head = edge.getV2();
            if (!encountered[head] && traverser.traverse(tail, e, head) == TraverseResult.CONTINUE) {
                encountered[head] = true;
                traverse(head, traverser, encountered);
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
    public void print(PrintStream out, Function<V, String> vertexToString, Function<E, String> arrowToString) {
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
                String str = arrowToString == null ? Objects.toString(edge.getObject()) : arrowToString.apply(edge.getObject());
                out.append(Integer.toString(e)).append(": ")
                        .append(Integer.toString(edge.getV1())).append("-->")
                        .append(Integer.toString(edge.getV2())).append(" ")
                        .append(str).append("\n");
            }
        }
    }

    private boolean isCyclicUtil(int v, boolean[] visited, boolean[] recStack) {
        checkVertex(v);
        if (!visited[v]) {
            // Mark the current node as visited and part of recursion stack
            visited[v] = true;
            recStack[v] = true;

            // Recur for all the vertices adjacent to this vertex
            TIntArrayList[] adjacencyList = getAdjacencyList();
            TIntArrayList adjacentEdges = adjacencyList[v];
            for (int i = 0; i < adjacentEdges.size(); i++) {
                int e = adjacentEdges.getQuick(i);
                Edge<E> edge = edges.get(e);
                assert edge.getV1() == v;

                if (!visited[edge.getV2()] && isCyclicUtil(edge.getV2(), visited, recStack)) {
                    return true;
                } else if (recStack[edge.getV2()]) {
                    return true;
                }
            }

        }
        recStack[v] = false;  // remove the vertex from recursion stack
        return false;
    }

    // Returns true if the graph contains a cycle, else false.
    // This function is a variation of DFS() in http://www.geeksforgeeks.org/archives/18212
    @Override
    public boolean isCyclic() {
        // Mark all the vertices as not visited and not part of recursion
        // stack
        boolean[] visited = new boolean[getVertexCount()];
        boolean[] recStack = new boolean[getVertexCount()];
        Arrays.fill(visited, false);
        Arrays.fill(recStack, false);

        // Call the recursive helper function to detect cycle in different
        // DFS trees
        for (int i = 0; i < getVertexCount(); i++) {
            if (isCyclicUtil(i, visited, recStack)) {
                return true;
            }
        }

        return false;
    }
}
