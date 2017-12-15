package com.powsybl.math.graph;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.powsybl.commons.PowsyblException;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Stream;

public class DirectedGraphImpl<V, A> implements DirectedGraph<V, A> {
    private static final int VERTICES_CAPACITY = 10;

    private static final int ARROWS_CAPACITY = 15;

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

    private static final class Arrow<E> {

        private final int tail;

        private final int head;

        private E object;

        private Arrow(int tail, int head, E object) {
            this.tail = tail;
            this.head = head;
            this.object = object;
        }

        public int getTail() {
            return tail;
        }

        public int getHead() {
            return head;
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

    /* arrows */
    private final List<Arrow<A>> arrows = new ArrayList<>(ARROWS_CAPACITY);

    /* cached adjacency list */
    private TIntArrayList[] adjacencyListCache;

    private final Lock adjacencyListCacheLock = new ReentrantLock();

    private final TIntLinkedList removedVertices = new TIntLinkedList();

    private final TIntLinkedList removedArrows = new TIntLinkedList();

    private final List<DirectedGraphListener> listeners = new CopyOnWriteArrayList<>();

    private void checkVertex(int v) {
        if (v < 0 || v >= vertices.size() || vertices.get(v) == null) {
            throw new PowsyblException("Vertex " + v + " not found");
        }
    }

    private void checkArrow(int a) {
        if (a < 0 || a >= arrows.size() || arrows.get(a) == null) {
            throw new PowsyblException("Arrow " + a + " not found");
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
        for (Arrow<A> a : arrows) {
            if (a.getTail() == v || a.getHead() == v) {
                throw new PowsyblException("An arrow is connected to vertex " + v);
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
    public Iterable<V> getVerticesObject() {
        return FluentIterable.from(vertices)
                .filter(Predicates.notNull())
                .transform(Vertex::getObject);
    }

    @Override
    public Stream<V> getVertexObjectStream() {
        return vertices.stream().filter(Objects::nonNull).map(Vertex::getObject);
    }

    @Override
    public int getVertexCount() {
        return vertices.size() - removedVertices.size();
    }

    @Override
    public void removeAllVertices() {
        if (!arrows.isEmpty()) {
            throw new PowsyblException("Cannot remove all vertices because there are still some arrows in the graph");
        }
        vertices.clear();
        invalidateAdjacencyList();
        notifyListener();
    }

    @Override
    public int addArrow(int tail, int head, A obj) {
        checkVertex(tail);
        checkVertex(head);
        int a;
        Arrow<A> arrow = new Arrow<>(tail, head, obj);
        if (removedArrows.isEmpty()) {
            a = arrows.size();
            arrows.add(arrow);
        } else {
            a = removedArrows.removeAt(0);
            arrows.set(a, arrow);
        }
        invalidateAdjacencyList();
        notifyListener();
        return a;
    }

    @Override
    public A removeArrow(int a) {
        checkArrow(a);
        A obj = arrows.get(a).getObject();
        if (a == arrows.size() - 1) {
            arrows.remove(a);
        } else {
            arrows.set(a, null);
            removedArrows.add(a);
        }
        invalidateAdjacencyList();
        notifyListener();
        return obj;
    }

    @Override
    public void removeAllArrows() {
        arrows.clear();
        removedArrows.clear();
        invalidateAdjacencyList();
        notifyListener();
    }

    @Override
    public int getArrowCount() {
        return arrows.size() - removedArrows.size();
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
    public int[] getArrows() {
        TIntArrayList t = new TIntArrayList(getArrowCount());
        for (int a = 0; a < arrows.size(); a++) {
            if (arrows.get(a) != null) {
                t.add(a);
            }
        }
        return t.toArray();
    }

    @Override
    public int getMaxVertex() {
        return vertices.size();
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
    public int getArrowTail(int a) {
        checkArrow(a);
        return arrows.get(a).getTail();
    }

    @Override
    public int getArrowHead(int a) {
        checkArrow(a);
        return arrows.get(a).getHead();
    }

    @Override
    public Iterable<A> getArrowsObject() {
        return FluentIterable.from(arrows)
                .filter(Predicates.notNull())
                .transform(Arrow::getObject);
    }

    @Override
    public Stream<A> getArrowObjectStream() {
        return arrows.stream().filter(Objects::nonNull).map(Arrow::getObject);
    }

    @Override
    public A getArrowObject(int a) {
        checkArrow(a);
        return arrows.get(a).getObject();
    }

    @Override
    public List<A> getArrowObjects(int tail, int head) {
        checkVertex(tail);
        checkVertex(head);
        List<A> arrowObjects = new ArrayList<>(1);
        TIntArrayList[] adjacencyList = getAdjacencyList();
        TIntArrayList adjacentArrows = adjacencyList[tail];
        for (int i = 0; i < adjacentArrows.size(); i++) {
            int a = adjacentArrows.getQuick(i);
            Arrow<A> arrow = arrows.get(a);
            if (arrow.getTail() == tail && arrow.getHead() == head) {
                arrowObjects.add(arrow.getObject());
            }
        }
        return arrowObjects;
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
                for (int a = 0; a < arrows.size(); a++) {
                    Arrow<A> arrow = arrows.get(a);
                    if (arrow != null) {
                        int tail = arrow.getTail();
                        adjacencyListCache[tail].add(a);
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
    public List<TIntArrayList> findAllPaths(int from, Function<V, Boolean> pathComplete, Function<A, Boolean> pathCanceled) {
        Objects.requireNonNull(pathComplete);
        List<TIntArrayList> paths = new ArrayList<>();
        BitSet encountered = new BitSet(vertices.size());
        TIntArrayList path = new TIntArrayList(1);
        findAllPaths(from, pathComplete, pathCanceled, path, encountered, paths);
        // sort paths by size
        paths.sort((o1, o2) -> o1.size() - o2.size());
        return paths;
    }

    private boolean findAllPaths(int a, int v1or2, Function<V, Boolean> pathComplete, Function<A, Boolean> pathCanceled,
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

    private void findAllPaths(int v, Function<V, Boolean> pathComplete, Function<A, Boolean> pathCanceled,
                              TIntArrayList path, BitSet encountered, List<TIntArrayList> paths) {
        checkVertex(v);
        encountered.set(v, true);
        TIntArrayList[] adjacencyList = getAdjacencyList();
        TIntArrayList adjacentArrows = adjacencyList[v];
        for (int i = 0; i < adjacentArrows.size(); i++) {
            int a = adjacentArrows.getQuick(i);
            Arrow<A> arrow = arrows.get(a);
            if (pathCanceled != null && pathCanceled.apply(arrow.getObject())) {
                continue;
            }
            int head = arrow.getHead();
            TIntArrayList path2;
            BitSet encountered2;
            if (i < adjacentArrows.size() - 1) {
                path2 = new TIntArrayList(path);
                encountered2 = new BitSet(vertices.size());
                encountered2.or(encountered);
            } else {
                path2 = path;
                encountered2 = encountered;
            }

            if (findAllPaths(a, head, pathComplete, pathCanceled, path2, encountered2, paths)) {
                continue;
            }
        }
    }

    @Override
    public void traverse(int v, Traverser<A> traverser, boolean[] encountered) {
        checkVertex(v);
        TIntArrayList[] adjacencyList = getAdjacencyList();
        TIntArrayList adjacentArrows = adjacencyList[v];
        encountered[v] = true;
        for (int i = 0; i < adjacentArrows.size(); i++) {
            int a = adjacentArrows.getQuick(i);
            Arrow<A> arrow = arrows.get(a);
            int tail = arrow.getTail();
            int head = arrow.getHead();
            if (!encountered[head] && traverser.traverse(tail, a, head) == TraverseResult.CONTINUE) {
                encountered[head] = true;
                traverse(head, traverser, encountered);
            }
        }
    }

    @Override
    public void traverse(int v, Traverser<A> traverser) {
        boolean[] encountered = new boolean[vertices.size()];
        Arrays.fill(encountered, false);
        traverse(v, traverser, encountered);
    }

    @Override
    public void addListener(DirectedGraphListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListener(DirectedGraphListener l) {
        listeners.remove(l);
    }

    private void notifyListener() {
        for (DirectedGraphListener l : listeners) {
            l.graphChanged();
        }
    }

    @Override
    public void print(PrintStream out, Function<V, String> vertexToString, Function<A, String> arrowToString) {
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
        out.append("Arrows:\n");
        for (int a = 0; a < arrows.size(); a++) {
            Arrow<A> arrow = arrows.get(a);
            if (arrow != null) {
                String str = arrowToString == null ? Objects.toString(arrow.getObject()) : arrowToString.apply(arrow.getObject());
                out.append(Integer.toString(a)).append(": ")
                        .append(Integer.toString(arrow.getTail())).append("-->")
                        .append(Integer.toString(arrow.getHead())).append(" ")
                        .append(str).append("\n");
            }
        }
    }
}
