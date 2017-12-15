/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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
 * Directed graph interface
 *
 * Directed graph is composed of vertices connected by arrows.
 * Each vertex and arrow can be associated to an object.
 *
 * @author Sebastien MURGEY <sebastien.murgey at rte-france.com>
 */
public interface DirectedGraph<V, A> {
    int addVertex();

    V removeVertex(int v);

    int getVertexCount();

    int addArrow(int tail, int head, A obj);

    A removeArrow(int a);

    void removeAllArrows();

    int getArrowCount();

    int[] getArrows();

    int[] getVertices();

    int getMaxVertex();

    Iterable<V> getVerticesObject();

    Stream<V> getVertexObjectStream();

    V getVertexObject(int v);

    void setVertexObject(int v, V obj);

    int getArrowHead(int a);

    int getArrowTail(int a);

    void removeAllVertices();

    Iterable<A> getArrowsObject();

    Stream<A> getArrowObjectStream();

    A getArrowObject(int a);

    List<A> getArrowObjects(int tail, int head);

    void traverse(int v, Traverser<A> traverser, boolean[] encountered);

    void traverse(int v, Traverser<A> traverser);

    List<TIntArrayList> findAllPaths(int from, Function<V, Boolean> pathComplete, Function<A, Boolean> pathCanceled);

    void addListener(DirectedGraphListener l);

    void removeListener(DirectedGraphListener l);

    void print(PrintStream out, Function<V, String> vertexToString, Function<A, String> edgeToString);
}
