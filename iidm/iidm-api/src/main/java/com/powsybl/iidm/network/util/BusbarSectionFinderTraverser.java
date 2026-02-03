/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;

import java.util.*;

/**
 *
 * Class that provides a traverser to find which busbar section a terminal (connectable) belongs to.
 * it only works in Node Breaker view
 *
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public final class BusbarSectionFinderTraverser {

    private BusbarSectionFinderTraverser() {
        throw new UnsupportedOperationException();
    }

    public record SwitchInfo(String id, boolean isOpen) { }

    public record BusbarSectionResult(String busbarSectionId, int depth, SwitchInfo lastSwitch, boolean allClosedSwitch) { }

    /**
     * find the busbar section Id that a terminal (connectable) belongs to.
     */
    public static String findBusbarSectionId(Terminal terminal) {
        checkIsNodeBreakerView(terminal.getVoltageLevel());
        BusbarSectionResult result = getBusbarSectionResult(terminal);
        if (result != null) {
            return result.busbarSectionId();
        }
        if (terminal.getVoltageLevel().getNodeBreakerView().getBusbarSections() == null) {
            return null;
        }
        BusbarSection busbarSection = terminal.getVoltageLevel().getNodeBreakerView().getBusbarSections().iterator().next();
        return busbarSection == null ? null : busbarSection.getId();
    }

    /**
     * provide information on the connexion between a terminal (connectable) and its busbar section.
     */
    public static BusbarSectionResult getBusbarSectionResult(Terminal terminal) {
        checkIsNodeBreakerView(terminal.getVoltageLevel());
        int startNode = terminal.getNodeBreakerView().getNode();
        List<BusbarSectionResult> allResults = searchAllBusbars(terminal.getVoltageLevel(), startNode);
        return allResults.isEmpty() ? null : selectBestBusbar(allResults);
    }

    private static BusbarSectionResult selectBestBusbar(List<BusbarSectionResult> results) {
        List<BusbarSectionResult> withAllClosedSwitch = results.stream().filter(r -> r.allClosedSwitch).toList();
        // all closed switch
        if (!withAllClosedSwitch.isEmpty()) {
            return searchAllBusbars(withAllClosedSwitch);
        }
        // closed switch
        List<BusbarSectionResult> withClosedSwitch = results.stream().filter(r -> r.lastSwitch() != null && !r.lastSwitch().isOpen()).toList();
        if (!withClosedSwitch.isEmpty()) {
            return searchAllBusbars(withClosedSwitch);
        }
        // open switch
        List<BusbarSectionResult> withOpenSwitch = results.stream().filter(r -> r.lastSwitch() != null && r.lastSwitch().isOpen()).toList();
        if (!withOpenSwitch.isEmpty()) {
            return searchAllBusbars(withOpenSwitch);
        }
        return results.getFirst();
    }

    private static BusbarSectionResult searchAllBusbars(List<BusbarSectionResult> results) {
            return results.stream().min(Comparator.comparingInt(BusbarSectionResult::depth)
                    .thenComparing(BusbarSectionResult::busbarSectionId)).orElse(null);
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
                if (terminal.getConnectable() instanceof BusbarSection busbarSection) {
                    NodeState currentNodeState = visitedNodes.get(terminal.getNodeBreakerView().getNode());
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

    private static void checkIsNodeBreakerView(VoltageLevel voltageLevel) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new PowsyblException("BusbarSectionFinderTraverser only works with Node Breaker view and voltage level " + voltageLevel.getId() + " is not in this topology kind");
        }
    }
}
