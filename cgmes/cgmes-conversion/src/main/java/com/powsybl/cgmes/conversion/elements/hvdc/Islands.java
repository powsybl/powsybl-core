/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class Islands {
    private final List<ArrayList<String>> islandsNodes;

    // The island includes dcTopologicalNodes and the acTopologicalNodes at both ends of the transformer
    Islands(Adjacency adjacency) {
        islandsNodes = new ArrayList<>();

        Set<String> visitedTopologicalNodes = new HashSet<>();
        adjacency.get().keySet().forEach(topologicalNodeId -> {
            if (visitedTopologicalNodes.contains(topologicalNodeId)) {
                return;
            }
            ArrayList<String> adjacentTopologicalNodes = computeAdjacentTopologicalNodes(topologicalNodeId,
                adjacency, visitedTopologicalNodes);
            islandsNodes.add(adjacentTopologicalNodes);
        });
    }

    private static ArrayList<String> computeAdjacentTopologicalNodes(String topologicalNodeId,
        Adjacency adjacency, Set<String> visitedTopologicalNodes) {

        ArrayList<String> adjacentTopologicalNodes = new ArrayList<>();
        adjacentTopologicalNodes.add(topologicalNodeId);
        visitedTopologicalNodes.add(topologicalNodeId);

        int k = 0;
        while (k < adjacentTopologicalNodes.size()) {
            String topologicalNode = adjacentTopologicalNodes.get(k);
            if (adjacency.get().containsKey(topologicalNode)) {
                adjacency.get().get(topologicalNode).forEach(adjacent -> {
                    if (visitedTopologicalNodes.contains(adjacent.topologicalNode)) {
                        return;
                    }
                    adjacentTopologicalNodes.add(adjacent.topologicalNode);
                    visitedTopologicalNodes.add(adjacent.topologicalNode);
                });
            }
            k++;
        }
        return adjacentTopologicalNodes;
    }

    List<ArrayList<String>> getIslandsNodes() {
        return islandsNodes;
    }

    void debug() {
        LOG.debug("Islands");
        islandsNodes.forEach(island -> LOG.debug(" {} ", island));
    }

    private static final Logger LOG = LoggerFactory.getLogger(Islands.class);
}
