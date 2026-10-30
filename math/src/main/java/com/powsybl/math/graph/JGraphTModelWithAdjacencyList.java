/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class JGraphTModelWithAdjacencyList<V, E> implements GraphModel<V, E> {

    private final JGraphTModel<V, E> delegate = new JGraphTModel<>();

    private final ToIntFunction<V> numGetter;

    private final Map<V, TIntArrayList> adjacencyList = new LinkedHashMap<>();

    public JGraphTModelWithAdjacencyList(ToIntFunction<V> numGetter) {
        this.numGetter = Objects.requireNonNull(numGetter);
    }

    @Override
    public void addEdge(V v1, V v2, E e) {
        delegate.addEdge(v1, v2, e);
        adjacencyList.get(v1).add(numGetter.applyAsInt(v2));
        adjacencyList.get(v2).add(numGetter.applyAsInt(v1));
    }

    @Override
    public void removeEdge(E e) {
        V edgeSource = getEdgeSource(e);
        V edgeTarget = getEdgeTarget(e);
        adjacencyList.get(edgeSource).remove(numGetter.applyAsInt(edgeTarget));
        adjacencyList.get(edgeTarget).remove(numGetter.applyAsInt(edgeSource));
        delegate.removeEdge(e);
    }

    @Override
    public void addVertex(V v) {
        delegate.addVertex(v);
        adjacencyList.put(v, new TIntArrayList(10));
    }

    @Override
    public void removeVertex(V v) {
        delegate.removeVertex(v);
        adjacencyList.remove(v);
    }

    @Override
    public boolean containsVertex(V vertex) {
        return delegate.containsVertex(vertex);
    }

    @Override
    public boolean containsEdge(E edge) {
        return delegate.containsEdge(edge);
    }

    @Override
    public V getEdgeSource(E edge) {
        return delegate.getEdgeSource(edge);
    }

    @Override
    public V getEdgeTarget(E edge) {
        return delegate.getEdgeTarget(edge);
    }

    public Set<E> getEdges() {
        return delegate.getEdges();
    }

    @Override
    public Set<E> getNeighborEdgesOf(V v) {
        return delegate.getNeighborEdgesOf(v);
    }

    public Set<V> getVertices() {
        return delegate.getVertices();
    }

    public TIntArrayList[] getAdjacencyList() {
        TIntArrayList[] adjacencyListArray = new TIntArrayList[adjacencyList.size()];
        for (Map.Entry<V, TIntArrayList> entry : adjacencyList.entrySet()) {
            V vertex = entry.getKey();
            TIntArrayList adj = entry.getValue();
            adjacencyListArray[numGetter.applyAsInt(vertex)] = adj;
        }
        return adjacencyListArray;
    }
}
