/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.*;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class SwitchesChain {
    private final NodeConnectables nodeConnectables;
    private final Adjacency adjacency;
    private final VoltageLevel vl;
    private final Switch sw;
    private final boolean isNodeBreakerModel;

    SwitchesChain(VoltageLevel vl, Switch sw, boolean isNodeBreakerModel) {
        this.vl = vl;
        this.sw = sw;
        this.isNodeBreakerModel = isNodeBreakerModel;
        nodeConnectables = new NodeConnectables(vl, isNodeBreakerModel);
        adjacency = new Adjacency(vl, sw.getId(), isNodeBreakerModel);
    }

    Terminal getBestTerminalChain() {
        String end1 = getSwitchEnd(vl, sw, true, isNodeBreakerModel);
        String end2 = getSwitchEnd(vl, sw, false, isNodeBreakerModel);

        Terminal terminalEnd1 = findTerminalChainEnd(end1, end2, nodeConnectables, adjacency, isNodeBreakerModel);
        Terminal terminalEnd2 = findTerminalChainEnd(end2, end1, nodeConnectables, adjacency, isNodeBreakerModel);
        return bestTerminal(terminalEnd1, terminalEnd2);
    }

    private static String getSwitchEnd(VoltageLevel vl, Switch sw, boolean end1, boolean isNodeBreakerModel) {
        if (isNodeBreakerModel) {
            if (end1) {
                return String.valueOf(vl.getNodeBreakerView().getNode1(sw.getId()));
            } else {
                return String.valueOf(vl.getNodeBreakerView().getNode2(sw.getId()));
            }
        } else {
            if (end1) {
                return vl.getBusBreakerView().getBus1(sw.getId()).getId();
            } else {
                return vl.getBusBreakerView().getBus2(sw.getId()).getId();
            }
        }
    }

    private static Terminal findTerminalChainEnd(String end, String otherEnd, NodeConnectables nodeConnectables,
        Adjacency adjacency, boolean isNodeBreakerModel) {

        List<String> nodes = expand(end, adjacency);
        if (nodes.contains(otherEnd)) {
            return null;
        }
        if (onlyOneConnectable(nodes, nodeConnectables)) {
            return terminalAssociatedToFirstConductingEquipmentConnectable(nodes, nodeConnectables, isNodeBreakerModel);
        }
        return null;
    }

    private static List<String> expand(String initialNode, Adjacency adjacency) {
        List<String> nodes = new ArrayList<>();
        Set<String> visitedNodes = new HashSet<>();
        nodes.add(initialNode);
        visitedNodes.add(initialNode);

        int k = 0;
        while (k < nodes.size()) {
            String node = nodes.get(k);
            List<String> adjacents = adjacency.getAdjacents(node);
            adjacents.forEach(adjacentNode -> {
                if (!visitedNodes.contains(adjacentNode)) {
                    nodes.add(adjacentNode);
                    visitedNodes.add(adjacentNode);
                }
            });
            k++;
        }

        return nodes;
    }

    private static boolean onlyOneConnectable(List<String> nodes, NodeConnectables connectables) {
        int count = 0;
        for (String node : nodes) {
            count = count + connectables.getConnectablesNodeCount(node);
        }
        return count == 1;
    }

    private static Terminal terminalAssociatedToFirstConductingEquipmentConnectable(List<String> nodes,
        NodeConnectables connectables, boolean isNodeBreakerModel) {
        for (String node : nodes) {
            Connectable<?> connectable = connectables.getFirstConductingEquipment(node);
            if (connectable != null) {
                return connectable.getTerminals().stream()
                    .filter(terminal -> isTerminalNode((Terminal) terminal, node, isNodeBreakerModel)).findFirst()
                    .orElse(null);
            }
        }
        return null;
    }

    private static boolean isTerminalNode(Terminal terminal, String node, boolean isNodeBreakerModel) {
        if (isNodeBreakerModel) {
            return String.valueOf(terminal.getNodeBreakerView().getNode()).equals(node);
        }
        return terminal.getBusBreakerView().getBus().getId().equals(node);
    }

    // The best terminal is the terminal associated to the line, branch at the border
    // one end inside the controlArea and the other outside.
    // At this moment we only know this information in danglingLines so
    // the terminal will only be accepted if it is a danglingLine
    private static Terminal bestTerminal(Terminal terminalEnd1, Terminal terminalEnd2) {
        if (terminalEnd1 != null && terminalEnd1.getConnectable().getType() == ConnectableType.DANGLING_LINE) {
            return terminalEnd1;
        }
        if (terminalEnd2 != null && terminalEnd2.getConnectable().getType() == ConnectableType.DANGLING_LINE) {
            return terminalEnd2;
        }
        return null;
    }

    private static class NodeConnectables {

        private final Map<String, List<Connectable<?>>> connectables;

        NodeConnectables(VoltageLevel vl, boolean isNodeBreakerModel) {
            connectables = new HashMap<>();

            if (isNodeBreakerModel) {
                connectablesNodeBreaker(vl, connectables);
            } else {
                connectablesBusBreaker(vl, connectables);
            }
        }

        private static void connectablesNodeBreaker(VoltageLevel vl, Map<String, List<Connectable<?>>> connectables) {
            vl.getConnectables().forEach(c -> {
                if (isDiscarded(c)) {
                    return;
                }
                c.getTerminals().forEach(
                    t -> connectables.computeIfAbsent(String.valueOf(((Terminal) t).getNodeBreakerView().getNode()),
                        k -> new ArrayList<>()).add(c));
            });
        }

        private static void connectablesBusBreaker(VoltageLevel vl, Map<String, List<Connectable<?>>> connectables) {
            vl.getConnectables().forEach(c -> {
                if (isDiscarded(c)) {
                    return;
                }
                c.getTerminals()
                    .forEach(t -> connectables
                        .computeIfAbsent(((Terminal) t).getBusBreakerView().getBus().getId(), k -> new ArrayList<>())
                        .add(c));
            });
        }

        private int getConnectablesNodeCount(String nodeId) {
            if (connectables.containsKey(nodeId)) {
                return connectables.get(nodeId).size();
            }
            return 0;
        }

        private Connectable<?> getFirstConductingEquipment(String nodeId) {
            if (connectables.containsKey(nodeId)) {
                return connectables.get(nodeId).stream().filter(NodeConnectables::isConductingEquipment).findFirst().orElse(null);
            }
            return null;
        }

        private static boolean isDiscarded(Connectable<?> connectable) {
            return connectable.getType() == ConnectableType.BUSBAR_SECTION;
        }

        private static boolean isConductingEquipment(Connectable<?> connectable) {
            return connectable.getType() == ConnectableType.LINE
                || connectable.getType() == ConnectableType.DANGLING_LINE;
        }
    }

    private static class Adjacency {

        private final Map<String, List<String>> adjacencies;

        Adjacency(VoltageLevel vl, String switchId, boolean isNodeBreakerModel) {
            adjacencies = new HashMap<>();

            if (isNodeBreakerModel) {
                adjacencyNodeBreaker(vl, switchId, adjacencies);
            } else {
                adjacencyBusBreaker(vl, switchId, adjacencies);
            }
        }

        private static void adjacencyNodeBreaker(VoltageLevel vl, String switchId, Map<String, List<String>> adjacency) {
            vl.getSwitches().forEach(sw -> {
                if (sw.getId().equals(switchId)) {
                    return;
                }
                addAdjacency(adjacency, vl.getNodeBreakerView().getNode1(sw.getId()), vl.getNodeBreakerView().getNode2(sw.getId()));
            });
            vl.getNodeBreakerView().getInternalConnections().forEach(ic -> addAdjacency(adjacency, ic.getNode1(), ic.getNode2()));
        }

        private static void addAdjacency(Map<String, List<String>> adjacency, int nodeEnd1, int nodeEnd2) {
            String end1 = String.valueOf(nodeEnd1);
            String end2 = String.valueOf(nodeEnd2);
            adjacency.computeIfAbsent(end1, k -> new ArrayList<>()).add(end2);
            adjacency.computeIfAbsent(end2, k -> new ArrayList<>()).add(end1);
        }

        private static void adjacencyBusBreaker(VoltageLevel vl, String switchId, Map<String, List<String>> adjacency) {
            vl.getSwitches().forEach(sw -> {
                if (sw.getId().equals(switchId)) {
                    return;
                }
                addAdjacency(adjacency, vl.getBusBreakerView().getBus1(sw.getId()), vl.getBusBreakerView().getBus2(sw.getId()));
            });
        }

        private static void addAdjacency(Map<String, List<String>> adjacency, Bus busEnd1, Bus busEnd2) {
            String end1 = busEnd1.getId();
            String end2 = busEnd2.getId();
            adjacency.computeIfAbsent(end1, k -> new ArrayList<>()).add(end2);
            adjacency.computeIfAbsent(end2, k -> new ArrayList<>()).add(end1);
        }

        private List<String> getAdjacents(String nodeId) {
            if (adjacencies.containsKey(nodeId)) {
                return adjacencies.get(nodeId);
            }
            return Collections.<String>emptyList();
        }
    }
}
