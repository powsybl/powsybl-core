/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.Pseudograph;

import java.util.List;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class JGraphTModel<V, E> implements GraphModel<V, E> {

    private final Graph<V, E> graph = new Pseudograph<>(null, null, false);

    @Override
    public void addEdge(V v1, V v2, E e) {
        graph.addEdge(v1, v2, e);
    }

    @Override
    public void removeEdge(E e) {
        graph.removeEdge(e);
    }

    @Override
    public void addVertex(V v) {
        graph.addVertex(v);
    }

    @Override
    public void removeVertex(V v) {
        graph.removeVertex(v);
    }

    @Override
    public boolean containsVertex(V vertex) {
        return graph.containsVertex(vertex);
    }

    @Override
    public boolean containsEdge(E edge) {
        return graph.containsEdge(edge);
    }

    @Override
    public V getEdgeSource(E edge) {
        return graph.getEdgeSource(edge);
    }

    @Override
    public V getEdgeTarget(E edge) {
        return graph.getEdgeTarget(edge);
    }

    public Set<E> getEdgesBetween(V vertex1, V vertex2) {
        return graph.getAllEdges(vertex1, vertex2);
    }

    public Set<E> getEdges() {
        return graph.edgeSet();
    }

    @Override
    public Set<E> getNeighborEdgesOf(V v) {
        return graph.edgesOf(v);
    }

    public int getNeighborEdgeCountOf(V v) {
        return graph.degreeOf(v);
    }

    public Set<V> getVertices() {
        return graph.vertexSet();
    }

    public List<V> getNeighborVerticesOf(V v) {
        return Graphs.neighborListOf(graph, v);
    }
}
