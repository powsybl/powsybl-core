/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class GraphUtil {

    private GraphUtil() {
    }

    private static final class ConnectedComponent {

        private final int size;

        private int orderedNumber;

        private ConnectedComponent(int size) {
            this.size = size;
        }

    }

    public static class ConnectedComponentsComputationResult {

        private final int[] componentNumber;

        private final int[] componentSize;

        public ConnectedComponentsComputationResult(int[] componentNumber, int[] orderedComponents) {
            this.componentNumber = componentNumber;
            this.componentSize = orderedComponents;
        }

        public int[] getComponentNumber() {
            return componentNumber;
        }

        public int[] getComponentSize() {
            return componentSize;
        }

    }

    private static void computeConnectedComponents(int v1, int c, int[] componentSize, TIntArrayList[] adjacencyList, int[] componentNumber) {
        componentNumber[v1] = c;
        ++componentSize[c];
        TIntArrayList ls = adjacencyList[v1];
        for (int i = 0; i < ls.size(); i++) {
            int v2 = ls.getQuick(i);
            if (componentNumber[v2] == -1) {
                computeConnectedComponents(v2, c, componentSize, adjacencyList, componentNumber);
            }
        }
    }

    public static ConnectedComponentsComputationResult computeConnectedComponents(TIntArrayList[] adjacencyList) {
        int[] componentNumber = new int[adjacencyList.length];
        Arrays.fill(componentNumber, -1);
        int c = 0;
        int[] componentSize = new int[adjacencyList.length];
        Arrays.fill(componentSize, 0);
        for (int v = 0; v < adjacencyList.length; v++) {
            if (componentNumber[v] == -1) {
                computeConnectedComponents(v, c++, componentSize, adjacencyList, componentNumber);
            }
        }

        // sort components by size
        ConnectedComponent[] components = new ConnectedComponent[c];
        ConnectedComponent[] orderedComponents = new ConnectedComponent[c];
        for (int i = 0; i < c; i++) {
            ConnectedComponent comp = new ConnectedComponent(componentSize[i]);
            components[i] = comp;
            orderedComponents[i] = comp;
        }
        Arrays.sort(orderedComponents, (o1, o2) -> o2.size - o1.size);
        for (int i = 0; i < orderedComponents.length; i++) {
            orderedComponents[i].orderedNumber = i;
        }

        componentSize = new int[orderedComponents.length];
        for (ConnectedComponent cc : orderedComponents) {
            componentSize[cc.orderedNumber] = cc.size;
        }
        for (int i = 0; i < componentNumber.length; i++) {
            ConnectedComponent cc = components[componentNumber[i]];
            componentNumber[i] = cc.orderedNumber;
        }

        return new ConnectedComponentsComputationResult(componentNumber, componentSize);
    }

    /**
     * Remove from the {@param graph} vertices which are not connected to any edge,
     * and which have no associated object.
     */
    public static <V, E> void removeIsolatedVertices(UndirectedGraph<V, E> graph) {
        Objects.requireNonNull(graph, "Graph is null.");

        TIntSet connectedVertices = new TIntHashSet();
        for (int e : graph.getEdges()) {
            connectedVertices.add(graph.getEdgeVertex1(e));
            connectedVertices.add(graph.getEdgeVertex2(e));
        }

        for (int v : graph.getVertices()) {
            if (!connectedVertices.contains(v) && graph.getVertexObject(v) == null) {
                graph.removeVertex(v);
            }
        }
    }
}
