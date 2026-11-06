/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;

import java.util.*;
import java.util.function.ToIntFunction;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NaiveGraphConnectivity<V, E> extends AbstractGraphConnectivity<V, E, JGraphTModelWithAdjacencyList<V, E>> {

    private int[] components;

    private final ToIntFunction<V> numGetter;

    public NaiveGraphConnectivity(ToIntFunction<V> vertexNumGetter) {
        super(new JGraphTModelWithAdjacencyList<>(vertexNumGetter));
        this.numGetter = Objects.requireNonNull(vertexNumGetter);
    }

    @Override
    public boolean supportTemporaryChangesNesting() {
        return true;
    }

    private List<ComponentSet<V>> calculateConnectedSets() {
        TIntArrayList[] adjacencyList = getGraph().getAdjacencyList();
        GraphUtil.ConnectedComponentsComputationResult result = GraphUtil.computeConnectedComponents(adjacencyList);
        List<ComponentSet<V>> connectedSets = new ArrayList<>();
        int[] componentSize = result.getComponentSize();
        for (int i = 0; i < componentSize.length; i++) {
            int size = componentSize[i];
            connectedSets.add(new ComponentSet<>(HashSet.newHashSet(size), i));
        }

        int[] componentNum = result.getComponentNumber();
        for (V vertex : getGraph().getVertices()) {
            int v = numGetter.applyAsInt(vertex);
            connectedSets.get(componentNum[v]).set().add(vertex);
        }
        return connectedSets;
    }

    protected void updateComponents() {
        if (components == null) {
            components = new int[getGraph().getVertices().size()];
            componentSets = calculateConnectedSets();
            for (int componentIndex = 0; componentIndex < componentSets.size(); componentIndex++) {
                Component<V> vertices = componentSets.get(componentIndex);
                for (V vertex : vertices) {
                    components[numGetter.applyAsInt(vertex)] = componentIndex;
                }
            }
        }
    }

    @Override
    protected void resetConnectivity(Deque<GraphModification<V, E>> m) {
        invalidateComponents();
    }

    @Override
    protected int getQuickComponentNumber(V vertex) {
        return components[numGetter.applyAsInt(vertex)];
    }

    @Override
    protected void updateConnectivity(EdgeRemove<V, E> edgeRemove) {
        invalidateComponents();
    }

    @Override
    protected void updateConnectivity(EdgeAdd<V, E> edgeAdd) {
        invalidateComponents();
    }

    @Override
    protected void updateConnectivity(VertexAdd<V, E> vertexAdd) {
        invalidateComponents();
    }

    private void invalidateComponents() {
        components = null;
    }
}
