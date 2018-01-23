package com.powsybl.math.graph;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.powsybl.commons.PowsyblException;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public abstract class AbstractGraph<V, E> implements Graph<V, E> {
    private static final int VERTICES_CAPACITY = 10;
    private static final int EDGES_CAPACITY = 15;
    protected static final int NEIGHBORS_CAPACITY = 2;

    /* vertices */
    protected final List<Vertex<V>> vertices = new ArrayList<>(VERTICES_CAPACITY);
    /* edges */
    protected final List<Edge<E>> edges = new ArrayList<>(EDGES_CAPACITY);

    private final TIntLinkedList removedVertices = new TIntLinkedList();

    private final TIntLinkedList removedEdges = new TIntLinkedList();

    private final List<GraphListener> listeners = new CopyOnWriteArrayList<>();

    /* cached adjacency list */
    protected TIntArrayList[] adjacencyListCache = null;

    protected void checkVertex(int v) {
        if (v < 0 || v >= vertices.size() || vertices.get(v) == null) {
            throw new PowsyblException("Vertex " + v + " not found");
        }
    }

    protected void checkEdge(int e) {
        if (e < 0 || e >= edges.size() || edges.get(e) == null) {
            throw new PowsyblException("Edge " + e + " not found");
        }
    }

    protected class Vertex<E> {

        private E object;

        protected Vertex() {
        }

        public E getObject() {
            return object;
        }

        public void setObject(E object) {
            this.object = object;
        }

    }

    protected class Edge<E> {

        private final int v1;

        private final int v2;

        private E object;

        protected Edge(int v1, int v2, E object) {
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
            if (e.getV1() == v || e.getV2() == v) {
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
        invalidateAdjacencyList();
        notifyListener();
    }

    @Override
    public int addEdge(int v1, int v2, E obj) {
        checkVertex(v1);
        checkVertex(v2);
        int e;
        Edge<E> edge = new Edge<E>(v1, v2, obj);
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
    public int getMaxVertex() {
        return vertices.size();
    }

    @Override
    public void addListener(GraphListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListener(GraphListener l) {
        listeners.remove(l);
    }

    private void notifyListener() {
        for (GraphListener l : listeners) {
            l.graphChanged();
        }
    }

    private void invalidateAdjacencyList() {
        adjacencyListCache = null;
    }
}
