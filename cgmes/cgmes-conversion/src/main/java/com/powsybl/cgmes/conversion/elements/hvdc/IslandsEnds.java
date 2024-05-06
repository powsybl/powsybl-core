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
class IslandsEnds {
    private final List<IslandEnd> islandsEndsNodes;

    // The island includes dcNodes and first acNode
    IslandsEnds() {
        islandsEndsNodes = new ArrayList<>();
    }

    void add(Adjacency adjacency, NodeEquipment nodeEquipment, List<String> islandNodes) {
        if (islandNodes.isEmpty()) {
            return;
        }
        Set<String> visitedNodes = new HashSet<>();

        String nodeEnd1 = islandNodes.get(0);
        List<String> adjacentNodeEnd1 = computeAdjacentNodes(nodeEnd1, adjacency, visitedNodes);

        String nodeEnd2 = getNodeOtherEnd(nodeEquipment, islandNodes, visitedNodes);
        if (nodeEnd2 == null) {
            return;
        }
        List<String> adjacentNodeEnd2 = computeAdjacentNodes(nodeEnd2, adjacency, visitedNodes);

        // Consider configurations where DcLinks are only connected to AC network only in one of both substations
        // In this configuration DcLink1 and DcLink2 are connected in s2 but not in s1,
        // on this side they are connected in an adjacent substation sn.
        // Hvdc configuration analysis only considers the sub-network between s1 and s2
        // so we can have disconnected components in one side that are connected trough the other side
        //
        //     --- LN1 --- s1 --- AcDc11 --- DcLink 1 --- AcDc12 ----
        //  sn |                                                     |- s2
        //     --- LN2 --- s1 --- AcDc21 --- DcLink 2 --- AcDc22 ----
        //
        // Process unassigned components and assign them to the right end.
        for (String node : islandNodes) {
            if (visitedNodes.contains(node)) {
                continue;
            }
            List<String> adjacentNodeEnd = computeAdjacentNodes(node, adjacency, visitedNodes);
            addToRightEnd(nodeEquipment, adjacentNodeEnd1, adjacentNodeEnd2, adjacentNodeEnd);
        }

        IslandEnd islandEnd = new IslandEnd(adjacentNodeEnd1, adjacentNodeEnd2);
        islandsEndsNodes.add(islandEnd);
    }

    private static String getNodeOtherEnd(NodeEquipment nodeEquipment, List<String> islandNodes, Set<String> visitedNodes) {
        return islandNodes.stream()
            .filter(n -> isNodeOtherEnd(nodeEquipment, visitedNodes, n))
            .findFirst()
            .orElse(null);
    }

    private static boolean isNodeOtherEnd(NodeEquipment nodeEquipment, Set<String> visitedNodes, String node) {
        if (visitedNodes.contains(node)) {
            return false;
        }
        return visitedNodes.stream().anyMatch(n -> nodeEquipment.existDcLineSegmentBetweenBothNodes(n, node));
    }

    private static void addToRightEnd(NodeEquipment nodeEquipment, List<String> nodesEnd1, List<String> nodesEnd2, List<String> nodes) {
        List<String> nodesConnectedToEnd2 = nodes.stream()
            .filter(n -> nodesEnd2.stream().anyMatch(n2 -> nodeEquipment.existDcLineSegmentBetweenBothNodes(n, n2)))
            .toList();
        List<String> nodesConnectedToEnd1 = nodes.stream()
            .filter(n -> nodesEnd1.stream().anyMatch(n1 -> nodeEquipment.existDcLineSegmentBetweenBothNodes(n, n1)))
            .toList();

        if (nodesConnectedToEnd1.isEmpty() && !nodesConnectedToEnd2.isEmpty()) {
            nodesEnd1.addAll(nodes);
        } else if (!nodesConnectedToEnd1.isEmpty() && nodesConnectedToEnd2.isEmpty()) {
            nodesEnd2.addAll(nodes);
        }
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
    }
}
