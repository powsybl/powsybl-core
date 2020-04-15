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
class IslandsEnds {
    private final List<IslandEnd> islandsEndsNodes;

    // The island includes dcTopologicalNodes and first acTopologicalNode
    IslandsEnds() {
        islandsEndsNodes = new ArrayList<>();
    }

    void add(Adjacency adjacency, List<String> islandNodes) {
        if (islandNodes.isEmpty()) {
            return;
        }
        Set<String> visitedTopologicalNodes = new HashSet<>();

        String topologicalNodeEnd1 = islandNodes.get(0);
        List<String> adjacentTopologicalNodeEnd1 = computeAdjacentTopologicalNodes(topologicalNodeEnd1,
            adjacency, visitedTopologicalNodes);

        String topologicalNodeEnd2 = getTopologicalNodeOtherEnd(islandNodes, visitedTopologicalNodes);
        if (topologicalNodeEnd2 == null) {
            return;
        }
        List<String> adjacentTopologicalNodeEnd2 = computeAdjacentTopologicalNodes(topologicalNodeEnd2,
            adjacency, visitedTopologicalNodes);

        IslandEnd islandEnd = new IslandEnd(adjacentTopologicalNodeEnd1, adjacentTopologicalNodeEnd2);

        islandsEndsNodes.add(islandEnd);
    }

    private static String getTopologicalNodeOtherEnd(List<String> islandNodes, Set<String> visitedTopologicalNodes) {
        return islandNodes.stream()
            .filter(n -> !visitedTopologicalNodes.contains(n))
            .findFirst()
            .orElse(null);
    }

    private static List<String> computeAdjacentTopologicalNodes(String topologicalNodeId,
        Adjacency adjacency, Set<String> visitedTopologicalNodes) {

        List<String> adjacentTopologicalNodes = new ArrayList<>();
        adjacentTopologicalNodes.add(topologicalNodeId);
        visitedTopologicalNodes.add(topologicalNodeId);

        int k = 0;
        while (k < adjacentTopologicalNodes.size()) {
            String topologicalNode = adjacentTopologicalNodes.get(k);
            if (adjacency.get().containsKey(topologicalNode)) {
                adjacency.get().get(topologicalNode).forEach(adjacent -> {
                    if (Adjacency.isDcLineSegment(adjacent.type)) {
                        return;
                    }
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

    List<IslandEnd> getIslandsEndsNodes() {
        return islandsEndsNodes;
    }

    void debug() {
        LOG.debug("IslandsEnds");
        islandsEndsNodes.forEach(IslandEnd::debug);
    }

    static class IslandEnd {
        private final List<String> topologicalNodes1;
        private final List<String> topologicalNodes2;

        IslandEnd(List<String> topologicalNodes1, List<String> topologicalNodes2) {
            this.topologicalNodes1 = topologicalNodes1;
            this.topologicalNodes2 = topologicalNodes2;
        }

        List<String> getTopologicalNodes1() {
            return topologicalNodes1;
        }

        List<String> getTopologicalNodes2() {
            return topologicalNodes2;
        }

        void debug() {
            LOG.debug("    topologicalNodes1: {}", this.topologicalNodes1);
            LOG.debug("    topologicalNodes2: {}", this.topologicalNodes2);
            LOG.debug("---");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(IslandsEnds.class);
}
