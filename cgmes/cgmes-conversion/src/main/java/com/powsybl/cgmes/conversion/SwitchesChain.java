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
    private final VoltageLevel vl;
    private final Switch sw;
    private final boolean isNodeBreakerModel;

    SwitchesChain(VoltageLevel vl, Switch sw, boolean isNodeBreakerModel) {
        this.vl = vl;
        this.sw = sw;
        this.isNodeBreakerModel = isNodeBreakerModel;
    }

    Terminal getBestTerminalChain() {
        if (isNodeBreakerModel) {
            return getBestTerminalChainNodeBreaker();
        } else {
            return getBestTerminalChainBusBranch();
        }
    }

    private Terminal getBestTerminalChainNodeBreaker() {
        int end1 = getSwitchEndNodeBreaker(vl, sw, true);
        int end2 = getSwitchEndNodeBreaker(vl, sw, false);

        Terminal terminalEnd1 = findTerminalChainEndNodeBreaker(end1, end2);
        Terminal terminalEnd2 = findTerminalChainEndNodeBreaker(end2, end1);
        return bestTerminal(terminalEnd1, terminalEnd2);
    }

    private Terminal getBestTerminalChainBusBranch() {
        String end1 = getSwitchEndBusBranch(vl, sw, true);
        String end2 = getSwitchEndBusBranch(vl, sw, false);

        Terminal terminalEnd1 = findTerminalChainEndBusBranch(end1, end2);
        Terminal terminalEnd2 = findTerminalChainEndBusBranch(end2, end1);
        return bestTerminal(terminalEnd1, terminalEnd2);
    }

    private static int getSwitchEndNodeBreaker(VoltageLevel vl, Switch sw, boolean end1) {
        if (end1) {
            return vl.getNodeBreakerView().getNode1(sw.getId());
        } else {
            return vl.getNodeBreakerView().getNode2(sw.getId());
        }
    }

    private static String getSwitchEndBusBranch(VoltageLevel vl, Switch sw, boolean end1) {
        if (end1) {
            return vl.getBusBreakerView().getBus1(sw.getId()).getId();
        } else {
            return vl.getBusBreakerView().getBus2(sw.getId()).getId();
        }
    }

    private Terminal findTerminalChainEndNodeBreaker(int end, int otherEnd) {

        List<Integer> nodes = expandNodeBreaker(vl, end, sw);
        if (nodes.contains(otherEnd)) {
            return null;
        }
        return uniqueTerminalAssociatedConductingEquipmentNodeBreaker(vl, nodes);
    }

    private Terminal findTerminalChainEndBusBranch(String end, String otherEnd) {

        List<String> configuredBuses = expandBusBranch(vl, end, sw);
        if (configuredBuses.contains(otherEnd)) {
            return null;
        }
        return uniqueTerminalAssociatedConductingEquipmentBusBranch(vl, configuredBuses);
    }

    private static List<Integer> expandNodeBreaker(VoltageLevel voltageLevel, int node, Switch swTerminal) {
        List<Integer> nodes = new ArrayList<>();
        nodes.add(node);

        VoltageLevel.NodeBreakerView.TopologyTraverser traverser = (node1, sw, node2) -> {
            if (sw == swTerminal) {
                return TraverseResult.TERMINATE_PATH;
            }
            nodes.add(node2);
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getNodeBreakerView().traverse(node, traverser);
        return nodes;
    }

    private static List<String> expandBusBranch(VoltageLevel voltageLevel, String configuredBusId, Switch swTerminal) {
        List<String> configuredBuses = new ArrayList<>();
        configuredBuses.add(configuredBusId);

        VoltageLevel.BusBreakerView.TopologyTraverser traverser = (busId1, sw, busId2) -> {
            if (sw == swTerminal) {
                return TraverseResult.TERMINATE_PATH;
            }
            configuredBuses.add(busId2);
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getBusBreakerView().traverse(configuredBusId, traverser);
        return configuredBuses;
    }

    private static Terminal uniqueTerminalAssociatedConductingEquipmentNodeBreaker(VoltageLevel vl, List<Integer> nodes) {
        List<Terminal> terminals = new ArrayList<>();

        vl.getConnectables().forEach(c -> {
            if (isDiscarded(c)) {
                return;
            }

            c.getTerminals().forEach(terminal -> {
                if (!isSameVoltageLevel((Terminal) terminal, vl)
                    || !isTerminalInNodes((Terminal) terminal, nodes)) {
                    return;
                }
                terminals.add((Terminal) terminal);
            });
        });

        if (terminals.size() == 1) {
            return terminals.get(0);
        }
        return null;
    }

    private static Terminal uniqueTerminalAssociatedConductingEquipmentBusBranch(VoltageLevel vl, List<String> configuredBuses) {
        List<Terminal> terminals = new ArrayList<>();

        vl.getConnectables().forEach(c -> {
            if (isDiscarded(c)) {
                return;
            }

            c.getTerminals().forEach(terminal -> {
                if (!isSameVoltageLevel((Terminal) terminal, vl)
                    || !isTerminalInConfiguredBuses((Terminal) terminal, configuredBuses)) {
                    return;
                }
                terminals.add((Terminal) terminal);
            });
        });

        if (terminals.size() == 1) {
            return terminals.get(0);
        }
        return null;
    }

    private static boolean isTerminalInNodes(Terminal terminal, List<Integer> nodes) {
        return nodes.contains(terminal.getNodeBreakerView().getNode());
    }

    private static boolean isTerminalInConfiguredBuses(Terminal terminal, List<String> configuredBuses) {
        return configuredBuses.contains(terminal.getBusBreakerView().getBus().getId());
    }

    private static boolean isSameVoltageLevel(Terminal terminal, VoltageLevel vl) {
        return terminal.getVoltageLevel().equals(vl);
    }

    private static boolean isDiscarded(Connectable<?> connectable) {
        return connectable.getType() == IdentifiableType.BUSBAR_SECTION;
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
}
