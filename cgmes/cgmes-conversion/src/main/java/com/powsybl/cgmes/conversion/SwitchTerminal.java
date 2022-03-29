/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.Branch;
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
class SwitchTerminal {
    private final VoltageLevel vl;
    private final Switch sw;
    private final boolean isNodeBreakerModel;

    SwitchTerminal(VoltageLevel vl, Switch sw, boolean isNodeBreakerModel) {
        this.vl = vl;
        this.sw = sw;
        this.isNodeBreakerModel = isNodeBreakerModel;
    }

    // Find a terminal in the topological node
    Optional<Terminal> getTerminalInTopologicalNode() {
        if (isNodeBreakerModel) {
            return getTerminalInTopologicalNodeNodeBreaker();
        } else {
            return getTerminalInTopologicalNodeBusBranch();
        }
    }

    private Optional<Terminal> getTerminalInTopologicalNodeNodeBreaker() {
        int end1 = getSwitchEndNodeBreaker(vl, sw, true);
        int end2 = getSwitchEndNodeBreaker(vl, sw, false);

        List<Terminal> terminals = findTerminalsNodeBreaker(end1, end2);
        return bestTerminalInTopologicalNode(terminals);
    }

    private Optional<Terminal> getTerminalInTopologicalNodeBusBranch() {
        Bus end1 = getSwitchEndBusBranch(vl, sw, true);
        Bus end2 = getSwitchEndBusBranch(vl, sw, false);

        List<Terminal> terminals = findTerminalsBusBranch(end1, end2);
        return bestTerminalInTopologicalNode(terminals);
    }

    // First busbarSections
    private static Optional<Terminal> bestTerminalInTopologicalNode(List<Terminal> terminals) {
        Optional<Terminal> ot = terminals.stream().filter(t -> t.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION).findFirst();
        if (ot.isPresent()) {
            return ot;
        }
        return terminals.stream().findFirst();
    }

    // Find a terminal in a switches chain.
    Optional<TerminalAndSign> getTerminalInSwitchesChain(Branch.Side terminalSide) {
        if (isNodeBreakerModel) {
            return getTerminalInSwitchesChainNodeBreaker(terminalSide);
        } else {
            return getTerminalInSwitchesChainBusBranch(terminalSide);
        }
    }

    private Optional<TerminalAndSign> getTerminalInSwitchesChainNodeBreaker(Branch.Side terminalSide) {
        int end1 = getSwitchEndNodeBreaker(vl, sw, true);
        int end2 = getSwitchEndNodeBreaker(vl, sw, false);

        Terminal terminalEnd1 = findTerminalChainEndNodeBreaker(end1, end2);
        Terminal terminalEnd2 = findTerminalChainEndNodeBreaker(end2, end1);
        return bestTerminalInChain(terminalSide, terminalEnd1, terminalEnd2);
    }

    private Optional<TerminalAndSign> getTerminalInSwitchesChainBusBranch(Branch.Side terminalSide) {
        Bus end1 = getSwitchEndBusBranch(vl, sw, true);
        Bus end2 = getSwitchEndBusBranch(vl, sw, false);

        Terminal terminalEnd1 = findTerminalChainEndBusBranch(end1, end2);
        Terminal terminalEnd2 = findTerminalChainEndBusBranch(end2, end1);
        return bestTerminalInChain(terminalSide, terminalEnd1, terminalEnd2);
    }

    // CgmesTerminal is defined at end1 of the switch and the terminal is obtained at the switchChain from end2 then same sign
    // CgmesTerminal is defined at end2 of the switch and the terminal is obtained at the switchChain from end1 then same sign
    //
    // N1---sw---N2 N2---sw2---N3 N3---sw3---N4 ... Nn---swn---Nn+1 Nn+1--- 
    // ct = cgmesTerminal defined at N1, t = terminal defined at Nn+1, ct and t same sign
    //

    private static Optional<TerminalAndSign> bestTerminalInChain(Branch.Side terminalSide, Terminal terminalEnd1, Terminal terminalEnd2) {
        if (terminalSide == Branch.Side.ONE && terminalEnd2 != null) {
            return Optional.of(new TerminalAndSign(terminalEnd2, 1));
        } else if (terminalSide == Branch.Side.TWO && terminalEnd1 != null) {
            return Optional.of(new TerminalAndSign(terminalEnd1, 1));
        } else if (terminalSide == Branch.Side.ONE && terminalEnd1 != null) {
            return Optional.of(new TerminalAndSign(terminalEnd1, -1));
        } else if (terminalSide == Branch.Side.TWO && terminalEnd2 != null) {
            return Optional.of(new TerminalAndSign(terminalEnd2, -1));
        } else {
            return Optional.empty();
        }
    }

    // Find a dangingLine terminal in a switches chain
    Optional <Terminal> getDanglingLineTerminalInSwitchesChain() {
        if (isNodeBreakerModel) {
            return getDanglingLineTerminalInSwitchesChainNodeBreaker();
        } else {
            return getDanglingLineTerminalInSwitchesChainBusBranch();
        }
    }

    private Optional<Terminal> getDanglingLineTerminalInSwitchesChainNodeBreaker() {
        int end1 = getSwitchEndNodeBreaker(vl, sw, true);
        int end2 = getSwitchEndNodeBreaker(vl, sw, false);

        Terminal terminalEnd1 = findTerminalChainEndNodeBreaker(end1, end2);
        Terminal terminalEnd2 = findTerminalChainEndNodeBreaker(end2, end1);
        return bestDanglingLineTerminal(terminalEnd1, terminalEnd2);
    }

    private Optional<Terminal> getDanglingLineTerminalInSwitchesChainBusBranch() {
        Bus end1 = getSwitchEndBusBranch(vl, sw, true);
        Bus end2 = getSwitchEndBusBranch(vl, sw, false);

        Terminal terminalEnd1 = findTerminalChainEndBusBranch(end1, end2);
        Terminal terminalEnd2 = findTerminalChainEndBusBranch(end2, end1);
        return bestDanglingLineTerminal(terminalEnd1, terminalEnd2);
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

    private List<Terminal> findTerminalsNodeBreaker(int end, int otherEnd) {

        List<Integer> nodes = expandNodeBreaker(vl, end);
        if (!nodes.contains(otherEnd)) {
            return new ArrayList<>();
        }
        return allTerminalsAssociatedConductingEquipment(vl, nodes, t -> t.getNodeBreakerView().getNode());
    }

    private Terminal findTerminalChainEndNodeBreaker(int end, int otherEnd) {

        List<Integer> nodes = expandNodeBreaker(vl, end, sw);
        if (nodes.contains(otherEnd)) {
            return null;
        }
        return uniqueTerminalAssociatedConductingEquipment(vl, nodes, t -> t.getNodeBreakerView().getNode());
    }

    private List<Terminal> findTerminalsBusBranch(Bus end, Bus otherEnd) {

        List<Bus> buses = expandBusBranch(vl, end);
        if (!buses.contains(otherEnd)) {
            return new ArrayList<>();
        }
        return allTerminalsAssociatedConductingEquipment(vl, buses, t -> t.getBusBreakerView().getBus());
    }

    private Terminal findTerminalChainEndBusBranch(Bus end, Bus otherEnd) {

        List<Bus> buses = expandBusBranch(vl, end, sw);
        if (buses.contains(otherEnd)) {
            return null;
        }
        return uniqueTerminalAssociatedConductingEquipment(vl, buses, t -> t.getBusBreakerView().getBus());
    }

    private static List<Integer> expandNodeBreaker(VoltageLevel voltageLevel, int node) {
        List<Integer> nodes = new ArrayList<>();
        nodes.add(node);

        // Expand using opened and closed switches
        VoltageLevel.NodeBreakerView.TopologyTraverser traverser = (node1, sw, node2) -> {
            nodes.add(node2);
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getNodeBreakerView().traverse(node, traverser);
        return nodes;
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

    private static List<Bus> expandBusBranch(VoltageLevel voltageLevel, Bus bus) {
        List<Bus> buses = new ArrayList<>();
        buses.add(bus);

        // Expand using opened and closed switches
        VoltageLevel.BusBreakerView.TopologyTraverser traverser = (bus1, sw, bus2) -> {
            buses.add(bus2);
            return TraverseResult.CONTINUE;
        };

        voltageLevel.getBusBreakerView().traverse(bus, traverser);
        return buses;
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

    private static <T> List<Terminal> allTerminalsAssociatedConductingEquipment(VoltageLevel vl, List<T> vertices, Function<Terminal, T> terminalToVertex) {

        return vl.getConnectableStream()
                .map(c -> (Connectable<?>) c)
                .flatMap(c -> c.getTerminals().stream())
                .filter(terminal -> isSameVoltageLevel(terminal, vl))
                .filter(terminal -> vertices.contains(terminalToVertex.apply(terminal)))
                .collect(Collectors.toList());
    }

    private static <T> Terminal uniqueTerminalAssociatedConductingEquipment(VoltageLevel vl, List<T> vertices, Function<Terminal, T> terminalToVertex) {

        List<Terminal> terminals = vl.getConnectableStream()
                .filter(c -> c.getType() != IdentifiableType.BUSBAR_SECTION)
                .map(c -> (Connectable<?>) c)
                .flatMap(c -> c.getTerminals().stream())
                .filter(terminal -> isSameVoltageLevel(terminal, vl))
                .filter(terminal -> vertices.contains(terminalToVertex.apply(terminal)))
                .collect(Collectors.toList());

        return terminals.size() == 1 ? terminals.get(0) : null;
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
    private static Optional<Terminal> bestDanglingLineTerminal(Terminal terminalEnd1, Terminal terminalEnd2) {
        if (terminalEnd1 != null && terminalEnd1.getConnectable().getType() == IdentifiableType.DANGLING_LINE) {
            return Optional.of(terminalEnd1);
        }
        if (terminalEnd2 != null && terminalEnd2.getConnectable().getType() == IdentifiableType.DANGLING_LINE) {
            return Optional.of(terminalEnd2);
        }
        return Optional.empty();
    }

    static boolean isSwitch(String conductingEquipmentType) {
        return conductingEquipmentType.equals("Breaker") || conductingEquipmentType.equals("Disconnector");
    }

    static class TerminalAndSign {
        private final Terminal terminal;
        private final int sign;

        TerminalAndSign(Terminal terminal, int sign) {
            this.terminal = terminal;
            this.sign = sign;
        }

        Terminal getTerminal() {
            return terminal;
        }

        int getSign() {
            return sign;
        }
    }
}
