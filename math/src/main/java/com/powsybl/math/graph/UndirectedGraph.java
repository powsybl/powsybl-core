/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;

import java.io.PrintStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * An undirected graph implementation.
 * Note that there is no guarantee that the indices of the vertex or edges are contiguous. To iterate over the vertices
 * or the edges, it is recommended to use {@link #getVertices()} and {@link #getEdges()}.
 *
 * @tparam V The type of the value attached to a vertex.
 * @tparam E The type of the value attached to an edge.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface UndirectedGraph<V, E> {

    /**
     * Create a new vertex and notify the {@link UndirectedGraphListener}s.
     *
     * @return the index of the new vertex.
     */
    int addVertex();

    /**
     * If the specified vertex does not exist or is null, create it and notify the {@link UndirectedGraphListener}
     */
    default void addVertexIfNotPresent(int v) {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove the specified vertex and notify the {@link UndirectedGraphListener}s.
     * This method throws a {@link com.powsybl.commons.PowsyblException} if the vertex doesn't exist or if an edge is connected to this vertex.
     *
     * @param v the vertex index to remove.
     * @return the value attached to the vertex.
     */
    V removeVertex(int v);

    /**
     * Return the number of non-null vertices.
     * As the contiguity of vertices is not mandatory, the number of vertices can be less than the highest vertex index.
     *
     * @return the number of vertices.
     */
    int getVertexCount();

    /**
     * Create an edge between the two specified vertices and notify the {@link UndirectedGraphListener}s.
     * This method throws a {@link com.powsybl.commons.PowsyblException} if one of the vertices doesn't exist.
     *
     * @param v1 the first end of the edge.
     * @param v2 the second end of the edge.
     * @param obj the value attached to the edge.
     * @return the index of the new edge.
     */
    int addEdge(int v1, int v2, E obj);

    /**
     * Remove the specified edge and notify the {@link UndirectedGraphListener}s.
     * This method thows a {@link com.powsybl.commons.PowsyblException} if the edge doesn't exist.
     *
     * @param e the edge index to remove.
     * @return the value attached to the edge.
     */
    E removeEdge(int e);

    /**
     * Remove all the edges and notify the {@link UndirectedGraphListener}s.
     */
    void removeAllEdges();

    /**
     * Return the number of edges.
     *
     * @return the number of edges.
     */
    int getEdgeCount();

    /**
     * Return the indices of the edges.
     *
     * @return the indices of the edges.
     */
    int[] getEdges();

    /**
     * Return the indices of the vertices.
     *
     * @return the indices of the vertices.
     */
    int[] getVertices();

    /**
     * Return the maximum number of vertices that this graph can contain. The vertex indices are in the range [0, getVertexCapacity[
     *
     * As the contiguity of the vertices is not mandatory, do not use this method to iterate over the vertices. Use {@link #getVertices()} instead.
     *
     * To get the number of vertices in this graph, use {@link #getVertexCount()}.
     *
     * @return the maximum number of vertices contained in this graph.
     *
     * @deprecated Use {@link #getVertexCapacity} instead.
     */
    @Deprecated
    default int getMaxVertex() {
        return getVertexCapacity();
    }

    /**
     * Return the maximum number of vertices that this graph can contain. The vertex indices are in the range [0, getVertexCapacity[
     *
     * As the contiguity of the vertices is not mandatory, do not use this method to iterate over the vertices. Use {@link #getVertices()} instead.
     *
     * To get the number of vertices in this graph, use {@link #getVertexCount()}.
     *
     * @return the maximum number of vertices contained in this graph.
     */
    default int getVertexCapacity() {
        return getMaxVertex();
    }

    /**
     * Return an {@link Iterable} to iterate over the values attached to the vertices.
     *
     * @return an {@link Iterable} to iterate over the values attached to the vertices.
     */
    Iterable<V> getVerticesObj();

    /**
     * Return a {@link Stream} to iterate over the values attached to the vertices.
     *
     * @return a {@link Stream} to iterate over the values attached to the vertices.
     */
    Stream<V> getVertexObjectStream();

    /**
     * Return the value attached to the specified vertex.
     * This method throws a {@link com.powsybl.commons.PowsyblException} if the vertex doesn't exist.
     *
     * @param v the vertex index
     * @return the value attached to the specified vertex.
     */
    V getVertexObject(int v);

    /**
     * Set the value attached to the specified vertex.
     * This method throws a {@link com.powsybl.commons.PowsyblException} if the vertex doesn't exist.
     *
     * @param v the vertex index.
     * @param obj the value to attach to the vertex.
     */
    void setVertexObject(int v, V obj);

    /**
     * Return the index of the first vertex that the specified edge is connected to.
     * This method throws a {@link com.powsybl.commons.PowsyblException} if the edge doesn't exist.
     *
     * @param e the edge index.
     * @return the index of the first vertex that the specified edge is connected to.
     */
    int getEdgeVertex1(int e);

    /**
     * Return the index of the second vertex that the specified edge is connected to.
     * This method throws a {@link com.powsybl.commons.PowsyblException} if the edge doesn't exist.
     *
     * @param e the edge index.
     * @return the index of the second vertex that the specified edge is connected to.
     */
    int getEdgeVertex2(int e);

    /**
     * Remove all the vertices of this graph.
     * This method throws a {@link com.powsybl.commons.PowsyblException} if edges exist.
     */
    void removeAllVertices();

    /**
     * Return an {@link Iterable} to iterate over the values attached to the edges.
     *
     * @return an {@link Iterable} to iterate over the values attached to the edges.
     */
    Iterable<E> getEdgesObject();

    /**
     * Return a {@link Stream} to iterate over the values attached to the edges.
     *
     * @return a {@link Stream} to iterate over the values attached to the edges.
     */
    Stream<E> getEdgeObjectStream();

    /**
     * Return the value attached to the specified edge.
     *
     * @param e the index of an edge.
     * @return the value attached to the specified edge.
     */
    E getEdgeObject(int e);

    /**
     * Return a {@link List} containing the values attached to the edges between the vertices v1 and v2.
     *
     * @return a {@link List} containing the values attached to the edges between the vertices v1 and v2.
     */
    List<E> getEdgeObjects(int v1, int v2);

    /**
     * Traverse the entire graph, starting at the specified vertex v.
     * This method relies on a {@link Traverser} instance to know if the traverse of the graph should continue or stop.
     * This method throws a {@link com.powsybl.commons.PowsyblException} if the encountered table size is less than the maximum vertex index.
     *
     * At the end of the method, the encountered array contains {@literal true} for all the traversed vertices, {@literal false} otherwise.
     *
     * @param v the vertex index where the traverse has to start.
     * @param traverser the {@link Traverser} instance to use to know if the traverse should continue or stop.
     * @param encountered the list of traversed vertices.
     */
    void traverse(int v, Traverser traverser, boolean[] encountered);

    /**
     * Traverse the entire graph, starting at the specified vertex v.
     * This method allocates a boolean array and calls {@link #traverse(int, Traverser, boolean[])}.
     *
     * @param v the vertex index where the traverse has to start.
     * @param traverser the {@link Traverser} instance to use to know if the traverse should continue or stop.
     */
    void traverse(int v, Traverser traverser);

    /**
     * Find all paths from the specified vertex.
     * This method relies on two functions to stop the traverse when the target vertex is found or when an edge must not be traversed.
     *
     * @param from the vertex index where the traverse has to start.
     * @param pathComplete a function that returns true when the target vertex is found.
     * @param pathCancelled a function that returns true when the edge must not be traversed.
     * @return a list that contains the index of the traversed edges.
     */
    List<TIntArrayList> findAllPaths(int from, Function<V, Boolean> pathComplete, Function<E, Boolean> pathCancelled);

    /**
     * Add a {@link UndirectedGraphListener} to get notified when the graph changes.
     *
     * @param l the listener to add.
     */
    void addListener(UndirectedGraphListener l);

    /**
     * Remove a {@link UndirectedGraphListener} to stop listening the graph changes.
     *
     * @param l the listener to remove.
     */
    void removeListener(UndirectedGraphListener l);

    /**
     * Prints the entire graph to the specified {@link PrintStream}.
     * The printing relies on the two specified functions to render the values attached to a vertex or an edge.
     *
     * @param out the output stream.
     * @param vertexToString the function to use to render the values of vertices.
     * @param edgeToString the function to use to render the values of edges.
     */
    void print(PrintStream out, Function<V, String> vertexToString, Function<E, String> edgeToString);
}
