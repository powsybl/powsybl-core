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
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * This traverser provides methods to identify to which busbar section a terminal corresponds the most.<br>
 * 
 * The algorithm prioritizes in order:
 * <ul>
 *     <li>the paths with all switches closed</li>
 * 
 *     <li>the paths with the last switch closed</li>
 *     <li>all other paths</li>
 * </ul>
 * If multiple paths leading to busbar sections are found, the one with the lowest depth is returned.<br>
 * Since it's looking for a busbar section, it only works in Node Breaker view.
 *
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public final class BusbarSectionFinderTraverser {

    private BusbarSectionFinderTraverser() {
        throw new UnsupportedOperationException();
    }

    public record SwitchInfo(String id, boolean isOpen) { }

    public record BusbarSectionResult(String busbarSectionId, int depth, SwitchInfo lastSwitch, boolean allClosedSwitch) { }

    private record NodeState(int depth, int nbOpenSwitchesOnPath, SwitchInfo lastSwitch) { }

    private enum PathType {
        ALL_SWITCH_CLOSED, LAST_SWITCH_CLOSED, OTHER
    }

    /**
     * Returns the id of the busbar section the provided terminal corresponds the most<br>
     * The algorithm prioritizes in order:
     * <ul>
     *     <li>the paths with all switches closed</li>
     *     <li>the paths with the last switch closed</li>
     *     <li>all other paths</li>
     * </ul>
     * If multiple paths leading to busbar sections are found, the one with the lowest depth is returned.
     */
    public static String findBusbarSectionId(Terminal terminal) {
        checkIsNodeBreakerView(terminal.getVoltageLevel());
        BusbarSectionResult result = getBusbarSectionResult(terminal);
        return result != null ?
                result.busbarSectionId() :
                terminal.getVoltageLevel().getNodeBreakerView().getBusbarSectionStream().findFirst().map(BusbarSection::getId).orElse(null);
    }

    /**
     * Provides information related to the busbar section on which the provided terminal could be connected.<br/>
     *
     * The algorithm prioritizes in order:
     * <ul>
     *     <li>the paths with all switches closed</li>
     *     <li>the paths with the last switch closed</li>
     *     <li>all other paths</li>
     * </ul>
     * If multiple paths leading to busbar sections are found, the one with the lowest depth is returned.
     */
    public static BusbarSectionResult getBusbarSectionResult(Terminal terminal) {
        VoltageLevel voltageLevel = terminal.getVoltageLevel();
        checkIsNodeBreakerView(voltageLevel);

        int startNode = terminal.getNodeBreakerView().getNode();
        AtomicReference<PathType> pathType = new AtomicReference<>(null);
        List<BusbarSectionResult> results = new ArrayList<>();
        Integer[] resultsDepths = new Integer[1];

        Map<Integer, NodeState> visitedNodes = new HashMap<>();
        visitedNodes.put(startNode, new NodeState(0, 0, null));

        Terminal.TopologyTraverser traverser = new Terminal.TopologyTraverser() {

            @Override
            public TraverseResult traverse(Terminal traversedTerminal, boolean connected) {
                if (traversedTerminal.getVoltageLevel() != voltageLevel) {
                    return TraverseResult.TERMINATE_PATH;
                }
                if (traversedTerminal.getConnectable() instanceof BusbarSection busbarSection) {
                    return getTraverseResultOnBusbarSection(visitedNodes, traversedTerminal, busbarSection,
                            results, pathType, resultsDepths);
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
                NodeState newState = new NodeState(sourceState.depth + 1,
                        sourceState.nbOpenSwitchesOnPath + (aSwitch.isOpen() ? 1 : 0),
                        new SwitchInfo(aSwitch.getId(), aSwitch.isOpen()));
                visitedNodes.put(targetNode, newState);
                return TraverseResult.CONTINUE;
            }
        };

        terminal.traverse(traverser, TraversalType.BREADTH_FIRST);

        return results.stream().min(Comparator.comparing(BusbarSectionResult::busbarSectionId)).orElse(null);
    }

    private static TraverseResult getTraverseResultOnBusbarSection(Map<Integer, NodeState> visitedNodes,
                                                                   Terminal traversedTerminal,
                                                                   BusbarSection busbarSection,
                                                                   List<BusbarSectionResult> results,
                                                                   AtomicReference<PathType> pathType,
                                                                   Integer[] resultsDepths) {
        NodeState currentNodeState = visitedNodes.get(traversedTerminal.getNodeBreakerView().getNode());
        if (currentNodeState != null) {
            if (currentNodeState.nbOpenSwitchesOnPath == 0) {
                return getTraverseResultAllSwitchesClosed(busbarSection, currentNodeState,
                        results, pathType, resultsDepths);
            } else if (currentNodeState.lastSwitch != null && !currentNodeState.lastSwitch.isOpen()) {
                return getTraverseResultLastSwitchClosed(busbarSection, currentNodeState,
                        results, pathType, resultsDepths);
            } else if (currentNodeState.lastSwitch != null) {
                return getTraverseResultOthers(busbarSection, currentNodeState,
                        results, pathType, resultsDepths);
            }
        }

        return TraverseResult.TERMINATE_PATH;
    }

    private static TraverseResult getTraverseResultAllSwitchesClosed(BusbarSection busbarSection,
                                                                     NodeState currentNodeState,
                                                                     List<BusbarSectionResult> results,
                                                                     AtomicReference<PathType> pathType,
                                                                     Integer[] resultsDepths) {
        if (pathType.get() == null || pathType.get() != PathType.ALL_SWITCH_CLOSED) {
            // Previous results were less interesting, so they are dropped
            results.clear();
            pathType.set(PathType.ALL_SWITCH_CLOSED);
            resultsDepths[0] = currentNodeState.depth;
        }

        return addResultAndGetTraverseResult(busbarSection, currentNodeState, results, resultsDepths,
                true, TraverseResult.TERMINATE_TRAVERSER);
    }

    private static TraverseResult getTraverseResultLastSwitchClosed(BusbarSection busbarSection,
                                                                    NodeState currentNodeState,
                                                                    List<BusbarSectionResult> results,
                                                                    AtomicReference<PathType> pathType,
                                                                    Integer[] resultsDepths) {
        if (pathType.get() == null) {
            // First path found
            pathType.set(PathType.LAST_SWITCH_CLOSED);
        } else if (pathType.get() == PathType.ALL_SWITCH_CLOSED) {
            // Previous results were more interesting, so they are kept
            return TraverseResult.TERMINATE_PATH;
        } else if (pathType.get() == PathType.OTHER) {
            // Previous results were less interesting, so they are dropped
            results.clear();
            pathType.set(PathType.LAST_SWITCH_CLOSED);
            resultsDepths[0] = currentNodeState.depth;
        }

        return addResultAndGetTraverseResult(busbarSection, currentNodeState, results, resultsDepths,
                false, TraverseResult.TERMINATE_PATH);
    }

    private static TraverseResult getTraverseResultOthers(BusbarSection busbarSection,
                                                          NodeState currentNodeState,
                                                          List<BusbarSectionResult> results,
                                                          AtomicReference<PathType> pathType,
                                                          Integer[] resultsDepths) {
        if (pathType.get() == null) {
            // First path found
            pathType.set(PathType.OTHER);
        } else if (pathType.get() != PathType.OTHER) {
            // Previous results were more interesting, so they are kept
            return TraverseResult.TERMINATE_PATH;
        }

        return addResultAndGetTraverseResult(busbarSection, currentNodeState, results, resultsDepths,
                false, TraverseResult.TERMINATE_PATH);
    }

    private static TraverseResult addResultAndGetTraverseResult(BusbarSection busbarSection,
                                                                NodeState currentNodeState,
                                                                List<BusbarSectionResult> results,
                                                                Integer[] resultsDepths,
                                                                boolean allClosedSwitches,
                                                                TraverseResult traverseResultIfBetterPathAlreadyFound) {
        if (resultsDepths[0] != null && resultsDepths[0] < currentNodeState.depth) {
            // We have already found at least one busbar section at a lower depth
            return traverseResultIfBetterPathAlreadyFound;
        }

        results.add(new BusbarSectionResult(busbarSection.getId(), currentNodeState.depth,
                currentNodeState.lastSwitch, allClosedSwitches));
        resultsDepths[0] = currentNodeState.depth;

        return TraverseResult.TERMINATE_PATH;
    }

    private static void checkIsNodeBreakerView(VoltageLevel voltageLevel) {
        if (voltageLevel.getTopologyKind() != TopologyKind.NODE_BREAKER) {
            throw new PowsyblException("BusbarSectionFinderTraverser only works with Node Breaker view and voltage level " + voltageLevel.getId() + " is not in this topology kind");
        }
    }
}
