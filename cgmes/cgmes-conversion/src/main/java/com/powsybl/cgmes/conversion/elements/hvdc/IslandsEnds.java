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

    // The island includes dcNodes and first acNode
    IslandsEnds() {
        islandsEndsNodes = new ArrayList<>();
    }

    void add(Adjacency adjacency, List<String> islandNodes) {
        if (islandNodes.isEmpty()) {
            return;
        }
        Set<String> visitedNodes = new HashSet<>();

        String nodeEnd1 = islandNodes.get(0);
        List<String> adjacentNodeEnd1 = computeAdjacentNodes(nodeEnd1,
            adjacency, visitedNodes);

        String nodeEnd2 = getNodeOtherEnd(islandNodes, visitedNodes);
        if (nodeEnd2 == null) {
            return;
        }
        List<String> adjacentNodeEnd2 = computeAdjacentNodes(nodeEnd2,
            adjacency, visitedNodes);

        IslandEnd islandEnd = new IslandEnd(adjacentNodeEnd1, adjacentNodeEnd2);

        islandsEndsNodes.add(islandEnd);
    }

    private static String getNodeOtherEnd(List<String> islandNodes, Set<String> visitedNodes) {
        return islandNodes.stream()
            .filter(n -> !visitedNodes.contains(n))
            .findFirst()
            .orElse(null);
    }

    private static List<String> computeAdjacentNodes(String nodeId,
        Adjacency adjacency, Set<String> visitedNodes) {

        List<String> adjacentNodes = new ArrayList<>();
        adjacentNodes.add(nodeId);
        visitedNodes.add(nodeId);

        int k = 0;
        while (k < adjacentNodes.size()) {
            String node = adjacentNodes.get(k);
            if (adjacency.get().containsKey(node)) {
                adjacency.get().get(node).forEach(adjacent -> {
                    if (Adjacency.isDcLineSegment(adjacent.type)) {
                        return;
                    }
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

    List<IslandEnd> getIslandsEndsNodes() {
        return islandsEndsNodes;
    }

    void debug() {
        LOG.debug("IslandsEnds");
        islandsEndsNodes.forEach(IslandEnd::debug);
    }

    static class IslandEnd {
        private final List<String> nodes1;
        private final List<String> nodes2;

        IslandEnd(List<String> nodes1, List<String> nodes2) {
            this.nodes1 = nodes1;
            this.nodes2 = nodes2;
        }

        List<String> getNodes1() {
            return nodes1;
        }

        List<String> getNodes2() {
            return nodes2;
        }

        void debug() {
            LOG.debug("    Nodes1: {}", this.nodes1);
            LOG.debug("    Nodes2: {}", this.nodes2);
            LOG.debug("---");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(IslandsEnds.class);
}
