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
    private final Set<List<String>> islandsNodes;

    // The island includes dcTopologicalNodes and the acTopologicalNodes at both ends of the transformer
    Islands(Adjacency adjacency) {
        islandsNodes = new HashSet<>();

        Set<String> visitedTopologicalNodes = new HashSet<>();
        adjacency.getAdjacency().keySet().forEach(topologicalNodeId -> {
            if (visitedTopologicalNodes.contains(topologicalNodeId)) {
                return;
            }
            List<String> adjacentTopologicalNodes = computeAdjacentTopologicalNodes(topologicalNodeId,
                adjacency, visitedTopologicalNodes);
            islandsNodes.add(adjacentTopologicalNodes);
        });
    }

    private static List<String> computeAdjacentTopologicalNodes(String topologicalNodeId,
        Adjacency adjacency, Set<String> visitedTopologicalNodes) {

        List<String> adjacentTopologicalNodes = new ArrayList<>();
        adjacentTopologicalNodes.add(topologicalNodeId);
        visitedTopologicalNodes.add(topologicalNodeId);

        int k = 0;
        while (k < adjacentTopologicalNodes.size()) {
            String topologicalNode = adjacentTopologicalNodes.get(k);
            if (adjacency.getAdjacency().containsKey(topologicalNode)) {
                adjacency.getAdjacency().get(topologicalNode).forEach(adjacent -> {
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

    Set<List<String>> getIslandsNodes() {
        return islandsNodes;
    }

    void print() {
        LOG.info("Islands");
        islandsNodes.forEach(island -> LOG.info(" {} ", island));
    }

    private static final Logger LOG = LoggerFactory.getLogger(Islands.class);
}
