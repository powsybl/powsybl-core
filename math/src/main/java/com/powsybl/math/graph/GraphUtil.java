/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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

    private static void computeConnectedComponents(int v1, List<Integer> componentSizes, TIntArrayList[] adjacencyList, int[] componentNumbers) {
        int c = componentSizes.size();
        int componentSize = 0;
        Queue<Integer> nodes = new ArrayDeque<>();
        nodes.add(v1);
        while (!nodes.isEmpty()) {
            int node = nodes.poll();
            if (componentNumbers[node] == -1) {
                componentSize++;
                componentNumbers[node] = c;
                adjacencyList[node].forEach(e -> {
                    if (componentNumbers[e] == -1) {
                        nodes.add(e);
                    }
                    return true;
                });
            }
        }
        componentSizes.add(componentSize);
    }

    public static ConnectedComponentsComputationResult computeConnectedComponents(TIntArrayList[] adjacencyList) {
        int[] componentNumber = new int[adjacencyList.length];
        Arrays.fill(componentNumber, -1);
        List<Integer> componentSizes = new ArrayList<>();

        for (int v = 0; v < adjacencyList.length; v++) {
            if (componentNumber[v] == -1) {
                computeConnectedComponents(v, componentSizes, adjacencyList, componentNumber);
            }
        }

        // sort components by size
        int nbComponents = componentSizes.size();
        ConnectedComponent[] components = new ConnectedComponent[nbComponents];
        ConnectedComponent[] orderedComponents = new ConnectedComponent[nbComponents];
        for (int i = 0; i < nbComponents; i++) {
            ConnectedComponent comp = new ConnectedComponent(componentSizes.get(i));
            components[i] = comp;
            orderedComponents[i] = comp;
        }
        Arrays.sort(orderedComponents, (o1, o2) -> o2.size - o1.size);
        for (int i = 0; i < orderedComponents.length; i++) {
            orderedComponents[i].orderedNumber = i;
        }

        int[] orderedComponentSizes = new int[nbComponents];
        for (ConnectedComponent cc : orderedComponents) {
            orderedComponentSizes[cc.orderedNumber] = cc.size;
        }
        for (int i = 0; i < componentNumber.length; i++) {
            ConnectedComponent cc = components[componentNumber[i]];
            componentNumber[i] = cc.orderedNumber;
        }

        return new ConnectedComponentsComputationResult(componentNumber, orderedComponentSizes);
    }
}
