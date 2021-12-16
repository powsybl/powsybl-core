/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.*;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.math.graph.TraverseResult;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class SwitchesChain {
    private final NodeConnectables nodeConnectables;
    private final VoltageLevel vl;
    private final Switch sw;
    private final boolean isNodeBreakerModel;

    SwitchesChain(VoltageLevel vl, Switch sw, boolean isNodeBreakerModel) {
        this.vl = vl;
        this.sw = sw;
        this.isNodeBreakerModel = isNodeBreakerModel;
        nodeConnectables = new NodeConnectables(vl, isNodeBreakerModel);
    }

    Terminal getBestTerminalChain() {
        String end1 = getSwitchEnd(vl, sw, true, isNodeBreakerModel);
        String end2 = getSwitchEnd(vl, sw, false, isNodeBreakerModel);

        Terminal terminalEnd1 = findTerminalChainEnd(end1, end2, nodeConnectables, isNodeBreakerModel);
        Terminal terminalEnd2 = findTerminalChainEnd(end2, end1, nodeConnectables, isNodeBreakerModel);
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

    private Terminal findTerminalChainEnd(String end, String otherEnd, NodeConnectables nodeConnectables, boolean isNodeBreakerModel) {

        List<String> nodes = expand(vl, end, sw, isNodeBreakerModel);
        if (nodes.contains(otherEnd)) {
            return null;
        }
        if (onlyOneConnectable(nodes, nodeConnectables)) {
            return terminalAssociatedToFirstConductingEquipmentConnectable(nodes, nodeConnectables, isNodeBreakerModel);
        }
        return null;
    }

    private static List<String> expand(VoltageLevel voltageLevel, String node, Switch swTerminal, boolean isNodeBreakerModel) {
        if (isNodeBreakerModel) {
            return expandNodeBreaker(voltageLevel, node, swTerminal);
        }
        return expandBusBranch(voltageLevel, node, swTerminal);
    }

    private static List<String> expandNodeBreaker(VoltageLevel voltageLevel, String node, Switch swTerminal) {
        List<String> nodes = new ArrayList<>();
        nodes.add(node);

        VoltageLevel.NodeBreakerView.TopologyTraverser traverser = (node1, sw, node2) -> {
            if (sw == swTerminal) {
                return TraverseResult.TERMINATE_PATH;
            }
            nodes.add(String.valueOf(node2));
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getNodeBreakerView().traverse(Integer.valueOf(node), traverser);
        return nodes;
    }

    private static List<String> expandBusBranch(VoltageLevel voltageLevel, String busId, Switch swTerminal) {
        List<String> buses = new ArrayList<>();
        buses.add(busId);

        VoltageLevel.BusBreakerView.TopologyTraverser traverser = (busId1, sw, busId2) -> {
            if (sw == swTerminal) {
                return TraverseResult.TERMINATE_PATH;
            }
            buses.add(busId2);
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getBusBreakerView().traverse(busId, traverser);

        return buses;
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
                    .filter(terminal -> isTerminalNode(terminal, node, isNodeBreakerModel)).findFirst()
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
        if (terminalEnd1 != null && terminalEnd1.getConnectable().getType() == IdentifiableType.DANGLING_LINE) {
            return terminalEnd1;
        }
        if (terminalEnd2 != null && terminalEnd2.getConnectable().getType() == IdentifiableType.DANGLING_LINE) {
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
                c.getTerminals().stream().filter(t -> isSameVoltageLevel((Terminal) t, vl)).forEach(
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
                    .forEach(t -> {
                        if (!((Terminal) t).getVoltageLevel().equals(vl)) {
                            return;
                        }
                        connectables.computeIfAbsent(((Terminal) t).getBusBreakerView().getBus().getId(), k -> new ArrayList<>()).add(c);
                    });
            });
        }

        private static boolean isSameVoltageLevel(Terminal terminal, VoltageLevel vl) {
            return terminal.getVoltageLevel().equals(vl);
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
            return connectable.getType() == IdentifiableType.BUSBAR_SECTION;
        }

        private static boolean isConductingEquipment(Connectable<?> connectable) {
            return connectable.getType() == IdentifiableType.LINE
                || connectable.getType() == IdentifiableType.DANGLING_LINE;
        }
    }
}
