package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Graph<V, E> {

    int addVertex();

    V removeVertex(int v);

    int getVertexCount();

    int addEdge(int v1, int v2, E obj);

    E removeEdge(int e);

    void removeAllEdges();

    int getEdgeCount();

    int[] getEdges();

    int[] getVertices();

    int getMaxVertex();

    Iterable<V> getVerticesObj();

    Stream<V> getVertexObjectStream();

    V getVertexObject(int v);

    void setVertexObject(int v, V obj);

    void removeAllVertices();

    Iterable<E> getEdgesObject();

    Stream<E> getEdgeObjectStream();

    E getEdgeObject(int e);

    List<E> getEdgeObjects(int v1, int v2);

    void traverse(int v, Traverser<E> traverser, boolean[] encountered);

    void traverse(int v, Traverser<E> traverser);

    List<TIntArrayList> findAllPaths(int from, Function<V, Boolean> pathComplete, Function<E, Boolean> pathCanceled);

    void addListener(GraphListener l);

    void removeListener(GraphListener l);

    void print(PrintStream out, Function<V, String> vertexToString, Function<E, String> edgeToString);
}
