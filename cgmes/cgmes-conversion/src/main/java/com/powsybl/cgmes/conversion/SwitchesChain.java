/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.*;
import java.util.function.Function;

import com.powsybl.iidm.network.Bus;
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
        Bus end1 = getSwitchEndBusBranch(vl, sw, true);
        Bus end2 = getSwitchEndBusBranch(vl, sw, false);

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

    private static Bus getSwitchEndBusBranch(VoltageLevel vl, Switch sw, boolean end1) {
        if (end1) {
            return vl.getBusBreakerView().getBus1(sw.getId());
        } else {
            return vl.getBusBreakerView().getBus2(sw.getId());
        }
    }

    private Terminal findTerminalChainEndNodeBreaker(int end, int otherEnd) {

        List<Integer> nodes = expandNodeBreaker(vl, end, sw);
        if (nodes.contains(otherEnd)) {
            return null;
        }
        return uniqueTerminalAssociatedConductingEquipment(vl, nodes, t -> t.getNodeBreakerView().getNode());
    }

    private Terminal findTerminalChainEndBusBranch(Bus end, Bus otherEnd) {

        List<Bus> buses = expandBusBranch(vl, end, sw);
        if (buses.contains(otherEnd)) {
            return null;
        }
        return uniqueTerminalAssociatedConductingEquipment(vl, buses, t -> t.getBusBreakerView().getBus());
    }

    private static List<Integer> expandNodeBreaker(VoltageLevel voltageLevel, int node, Switch swTerminal) {
        List<Integer> nodes = new ArrayList<>();
        nodes.add(node);

        // Expand using opened and closed switches
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

    private static List<Bus> expandBusBranch(VoltageLevel voltageLevel, Bus bus, Switch swTerminal) {
        List<Bus> buses = new ArrayList<>();
        buses.add(bus);

        // Expand using opened and closed switches
        VoltageLevel.BusBreakerView.TopologyTraverser traverser = (bus1, sw, bus2) -> {
            if (sw == swTerminal) {
                return TraverseResult.TERMINATE_PATH;
            }
            buses.add(bus2);
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getBusBreakerView().traverse(bus, traverser);
        return buses;
    }

    private static <T> Terminal uniqueTerminalAssociatedConductingEquipment(VoltageLevel vl, List<T> vertices, Function<Terminal, T> terminalToVertex) {
        List<Terminal> terminals = new ArrayList<>();

        vl.getConnectableStream().forEach(c -> {
            if (c.getType() == IdentifiableType.BUSBAR_SECTION) {
                return;
            }

            Connectable<?> c1 = c;
            c1.getTerminals().forEach(terminal -> {
                if (isSameVoltageLevel(terminal, vl) && vertices.contains(terminalToVertex.apply(terminal))) {
                    terminals.add(terminal);
                }
            });
        });

        if (terminals.size() == 1) {
            return terminals.get(0);
        }
        return null;
    }

    private static boolean isSameVoltageLevel(Terminal terminal, VoltageLevel vl) {
        return terminal.getVoltageLevel().equals(vl);
    }

    /**
     * The best terminal is the terminal associated to the line, branch at the border
     * one end inside the controlArea and the other outside.
     * At this moment we only know this information in danglingLines so
     * the terminal will only be accepted if it is a danglingLine
     */
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
