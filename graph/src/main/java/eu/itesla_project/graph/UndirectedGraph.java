/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.graph;

import com.google.common.base.Function;
import gnu.trove.list.array.TIntArrayList;

import java.io.PrintStream;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface UndirectedGraph<V, E> {

    int addVertex();

    V removeVertex(int v);

    int getVertexCount();

    int addEdge(int v1, int v2, E obj);

    E removeEdge(int e);

    void removeAllEdges();

    int getEdgeCount();

    int[] getVertices();

    int getMaxVertex();

    Iterable<V> getVerticesObj();

    V getVertexObject(int v);

    void setVertexObject(int v, V obj);

    int getEdgeVertex1(int e);

    int getEdgeVertex2(int e);

    void removeAllVertices();

    Iterable<E> getEdgesObject();

    E getEdgeObject(int e);

    List<E> getEdgeObjects(int v1, int v2);

    void traverse(int v, Traverser<E> traverser, boolean[] encountered);

    void traverse(int v, Traverser<E> traverser);

    List<TIntArrayList> findAllPaths(int from, Function<V, Boolean> pathComplete, Function<E, Boolean> pathCanceled);

    void addListener(UndirectedGraphListener l);

    void removeListener(UndirectedGraphListener l);

    void print(PrintStream out, Function<V, String> vertexToString, Function<E, String> edgeToString);
}
