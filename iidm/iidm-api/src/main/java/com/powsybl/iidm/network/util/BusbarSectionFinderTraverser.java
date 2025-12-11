/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;

import java.util.*;

/**
 * @author Ghazwa Rehili <ghazwa.rehili at rte-france.com>
 */
public final class BusbarSectionFinderTraverser {

    private BusbarSectionFinderTraverser() {
        throw new UnsupportedOperationException();
    }

    public record SwitchInfo(String id, boolean isOpen) { }

    public record BusbarSectionResult(String busbarSectionId, int depth, SwitchInfo lastSwitch, boolean allClosedSwitch) { }

    public static String findBusbarSectionId(Terminal terminal) {
        BusbarSectionResult result = getBusbarSectionResult(terminal);
        return result != null ? result.busbarSectionId() : terminal.getVoltageLevel().getNodeBreakerView().getBusbarSections().iterator().next().getId();
    }

    public static BusbarSectionResult getBusbarSectionResult(Terminal terminal) {
        int startNode = terminal.getNodeBreakerView().getNode();
        List<BusbarSectionResult> allResults = searchAllBusbars(terminal.getVoltageLevel(), startNode);
        if (allResults.isEmpty()) {
            return null;
        }
        return selectBestBusbar(allResults);
    }

    private static BusbarSectionResult selectBestBusbar(List<BusbarSectionResult> results) {
        List<BusbarSectionResult> withAllClosedSwitch = results.stream().filter(r -> r.allClosedSwitch).toList();
        if (!withAllClosedSwitch.isEmpty()) {
            return withAllClosedSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)
                    .thenComparing(BusbarSectionResult::busbarSectionId)).orElse(null);
        }
        List<BusbarSectionResult> withClosedSwitch = results.stream().filter(r -> r.lastSwitch() != null && !r.lastSwitch().isOpen()).toList();
        if (!withClosedSwitch.isEmpty()) {
            return withClosedSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)
                    .thenComparing(BusbarSectionResult::busbarSectionId)).orElse(null);
        }
        List<BusbarSectionResult> withOpenSwitch = results.stream().filter(r -> r.lastSwitch() != null && r.lastSwitch().isOpen()).toList();
        if (!withOpenSwitch.isEmpty()) {
            return withOpenSwitch.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)
                    .thenComparing(BusbarSectionResult::busbarSectionId)).orElse(null);
        }
        return results.getFirst();
    }

    private static List<BusbarSectionResult> searchAllBusbars(VoltageLevel voltageLevel, int startNode) {
        List<BusbarSectionResult> results = new ArrayList<>();
        record NodeState(int depth, boolean allClosed) { }
        Map<Integer, NodeState> visitedNodes = new HashMap<>();
        visitedNodes.put(startNode, new NodeState(0, true));
        voltageLevel.getNodeBreakerView().getTerminal(startNode).traverse(new Terminal.TopologyTraverser() {
            SwitchInfo lastSwitch = null;
            @Override
            public TraverseResult traverse(Terminal terminal, boolean connected) {
                if (terminal.getVoltageLevel() != voltageLevel) {
                    return TraverseResult.TERMINATE_PATH;
                }
                NodeState currentNodeState = visitedNodes.get(terminal.getNodeBreakerView().getNode());
                if (terminal.getConnectable() instanceof BusbarSection busbarSection) {
                    if (currentNodeState != null) {
                        results.add(new BusbarSectionResult(busbarSection.getId(), currentNodeState.depth, lastSwitch, currentNodeState.allClosed));
                    }
                    return TraverseResult.TERMINATE_PATH;
                }
                return TraverseResult.CONTINUE;
            }

            @Override
            public TraverseResult traverse(Switch aSwitch) {
                int node1 = voltageLevel.getNodeBreakerView().getNode1(aSwitch.getId());
                int node2 = voltageLevel.getNodeBreakerView().getNode2(aSwitch.getId());
                int sourceNode = visitedNodes.containsKey(node1) ? node1 : node2;
                int targetNode = visitedNodes.containsKey(node1) ? node2 : node1;
                NodeState sourceState = visitedNodes.get(sourceNode);
                if (sourceState == null) {
                    return TraverseResult.TERMINATE_PATH;
                }
                NodeState newState = new NodeState(sourceState.depth + 1, sourceState.allClosed && !aSwitch.isOpen());
                visitedNodes.put(targetNode, newState);
                lastSwitch = new SwitchInfo(aSwitch.getId(), aSwitch.isOpen());
                return TraverseResult.CONTINUE;
            }
        }, TraversalType.BREADTH_FIRST);
        return results;
    }
}
