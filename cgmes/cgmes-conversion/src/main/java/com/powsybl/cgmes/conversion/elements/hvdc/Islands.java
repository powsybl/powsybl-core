/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class Islands {
    private final List<ArrayList<String>> islandsNodes;

    // The island includes dcNodes(topological or connectivity) and the
    // acNodes(topological or connectivity) at both ends of the transformer
    Islands(Adjacency adjacency) {
        islandsNodes = new ArrayList<>();

        Set<String> visitedNodes = new HashSet<>();
        adjacency.get().keySet().forEach(nodeId -> {
            if (visitedNodes.contains(nodeId)) {
                return;
            }
            ArrayList<String> adjacentNodes = computeAdjacentNodes(nodeId,
                adjacency, visitedNodes);
            islandsNodes.add(adjacentNodes);
        });
    }

    private static ArrayList<String> computeAdjacentNodes(String nodeId,
        Adjacency adjacency, Set<String> visitedNodes) {

        ArrayList<String> adjacentNodes = new ArrayList<>();
        adjacentNodes.add(nodeId);
        visitedNodes.add(nodeId);

        int k = 0;
        while (k < adjacentNodes.size()) {
            String node = adjacentNodes.get(k);
            if (adjacency.get().containsKey(node)) {
                adjacency.get().get(node).forEach(adjacent -> {
                    if (visitedNodes.contains(adjacent.node)) {
                        return;
                    }
                    adjacentNodes.add(adjacent.node);
                    visitedNodes.add(adjacent.node);
                });
            }
            k++;
        }
        return adjacentNodes;
    }

    List<ArrayList<String>> getIslandsNodes() {
        return islandsNodes;
    }
}
