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
    Set<List<String>> islandsNodes;

    // The island includes dcTopologicalNodes and first acTopologicalNode
    Islands(Adjacency adjacency) {
        islandsNodes = new HashSet<>();

        Set<String> visitedTopologicalNodes = new HashSet<>();
        adjacency.adjacency.keySet().forEach(topologicalNodeId -> {
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
            if (adjacency.adjacency.containsKey(topologicalNode)) {
                adjacency.adjacency.get(topologicalNode).forEach(adjacent -> {
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

    void print() {
        LOG.info("Islands");
        islandsNodes.forEach(island -> LOG.info(" {} ", island));
    }

    private static final Logger LOG = LoggerFactory.getLogger(Islands.class);
}
