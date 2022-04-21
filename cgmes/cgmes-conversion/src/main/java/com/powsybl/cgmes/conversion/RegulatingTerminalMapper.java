/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * IIDM does not have terminals at ends of switches.
 * When we found a CGMES regulating terminal that corresponds to the end of a switch,
 * we have to map it to an equivalent IIDM terminal.
 * The mapping has to take into account if the controlled magnitude corresponds to a node (voltage)
 * or if it is a flow (active/reactive power)
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class RegulatingTerminalMapper {
    private final VoltageLevel vl;
    private final Switch sw;
    private final boolean isNodeBreakerModel;

    public static Optional<Terminal> mapForVoltageControl(String cgmesTerminalId, Context context) {
        if (cgmesTerminalId == null) {
            return Optional.empty();
        }
        Switch sw = atSwitchEnd(cgmesTerminalId, context);
        if (sw != null) {
            return new RegulatingTerminalMapper(sw).findTerminalInTopologicalNode();
        } else {
            return Optional.ofNullable(context.terminalMapping().find(cgmesTerminalId));
        }
    }

    public static Optional<TerminalAndSign> mapForFlowControl(String cgmesTerminalId, Context context) {
        if (cgmesTerminalId == null) {
            return Optional.empty();
        }
        Switch sw = atSwitchEnd(cgmesTerminalId, context);
        if (sw != null) {
            Branch.Side side = sequenceNumberToSide(context.cgmes().terminal(cgmesTerminalId).getSequenceNumber());
            return new RegulatingTerminalMapper(sw).findTerminalInSwitchesChain(side);
        }
        return Optional.ofNullable(context.terminalMapping().find(cgmesTerminalId)).map(t -> new TerminalAndSign(t, 1));
    }

    public static Optional<Terminal> mapForTieFlow(String cgmesTerminalId, Context context) {
        if (cgmesTerminalId == null) {
            return Optional.empty();
        }
        Switch sw = atSwitchEnd(cgmesTerminalId, context);
        if (sw != null) {
            return new RegulatingTerminalMapper(sw).findDanglingLineTerminalInSwitchesChain();
        } else {
            return Optional.ofNullable(context.terminalMapping().find(cgmesTerminalId));
        }
    }

    RegulatingTerminalMapper(Switch sw) {
        this.vl = sw.getVoltageLevel();
        this.sw = sw;
        this.isNodeBreakerModel = vl.getTopologyKind() == TopologyKind.NODE_BREAKER;
    }

    // Find a terminal in the topological node
    Optional<Terminal> findTerminalInTopologicalNode() {
        if (isNodeBreakerModel) {
            return findTerminalInTopologicalNodeNodeBreaker();
        } else {
            return findTerminalInTopologicalNodeBusBranch();
        }
    }

    private Optional<Terminal> findTerminalInTopologicalNodeNodeBreaker() {
        int end1 = vl.getNodeBreakerView().getNode1(sw.getId());
        int end2 = vl.getNodeBreakerView().getNode2(sw.getId());

        List<Terminal> terminals = findTerminalsNodeBreaker(end1, end2);
        return bestTerminalInTopologicalNode(terminals);
    }

    private Optional<Terminal> findTerminalInTopologicalNodeBusBranch() {
        Bus end1 = vl.getBusBreakerView().getBus1(sw.getId());
        Bus end2 = vl.getBusBreakerView().getBus2(sw.getId());

        List<Terminal> terminals = findTerminalsBusBranch(end1, end2);
        return bestTerminalInTopologicalNode(terminals);
    }

    // First busbarSections, then voltage control equipment
    private static Optional<Terminal> bestTerminalInTopologicalNode(List<Terminal> terminals) {
        Optional<Terminal> ot = terminals.stream().filter(t -> t.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION).findFirst();
        if (ot.isPresent()) {
            return ot;
        }
        ot = terminals.stream().filter(t -> t.getConnectable().getType() == IdentifiableType.GENERATOR
            || t.getConnectable().getType() == IdentifiableType.SHUNT_COMPENSATOR
            || t.getConnectable().getType() == IdentifiableType.STATIC_VAR_COMPENSATOR
            || t.getConnectable().getType() == IdentifiableType.HVDC_CONVERTER_STATION).findFirst();
        if (ot.isPresent()) {
            return ot;
        }
        return terminals.stream().findFirst();
    }

    // Find a terminal in a chain of switches.
    Optional<TerminalAndSign> findTerminalInSwitchesChain(Branch.Side terminalSide) {
        if (isNodeBreakerModel) {
            return findTerminalInSwitchesChainNodeBreaker(terminalSide);
        } else {
            return findTerminalInSwitchesChainBusBranch(terminalSide);
        }
    }

    private Optional<TerminalAndSign> findTerminalInSwitchesChainNodeBreaker(Branch.Side terminalSide) {
        int end1 = vl.getNodeBreakerView().getNode1(sw.getId());
        int end2 = vl.getNodeBreakerView().getNode2(sw.getId());

        Terminal terminalEnd1 = findTerminalChainEndNodeBreaker(end1, end2);
        Terminal terminalEnd2 = findTerminalChainEndNodeBreaker(end2, end1);
        return bestTerminalInChain(terminalSide, terminalEnd1, terminalEnd2);
    }

    private Optional<TerminalAndSign> findTerminalInSwitchesChainBusBranch(Branch.Side terminalSide) {
        Bus end1 = vl.getBusBreakerView().getBus1(sw.getId());
        Bus end2 = vl.getBusBreakerView().getBus2(sw.getId());

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
    Optional <Terminal> findDanglingLineTerminalInSwitchesChain() {
        if (isNodeBreakerModel) {
            return findDanglingLineTerminalInSwitchesChainNodeBreaker();
        } else {
            return findDanglingLineTerminalInSwitchesChainBusBranch();
        }
    }

    private Optional<Terminal> findDanglingLineTerminalInSwitchesChainNodeBreaker() {
        int end1 = vl.getNodeBreakerView().getNode1(sw.getId());
        int end2 = vl.getNodeBreakerView().getNode2(sw.getId());

        Terminal terminalEnd1 = findTerminalChainEndNodeBreaker(end1, end2);
        Terminal terminalEnd2 = findTerminalChainEndNodeBreaker(end2, end1);
        return bestDanglingLineTerminal(terminalEnd1, terminalEnd2);
    }

    private Optional<Terminal> findDanglingLineTerminalInSwitchesChainBusBranch() {
        Bus end1 = vl.getBusBreakerView().getBus1(sw.getId());
        Bus end2 = vl.getBusBreakerView().getBus2(sw.getId());

        Terminal terminalEnd1 = findTerminalChainEndBusBranch(end1, end2);
        Terminal terminalEnd2 = findTerminalChainEndBusBranch(end2, end1);
        return bestDanglingLineTerminal(terminalEnd1, terminalEnd2);
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

    private static Switch atSwitchEnd(String cgmesTerminalId, Context context) {
        Objects.requireNonNull(cgmesTerminalId);
        CgmesTerminal cgmesTerminal = context.cgmes().terminal(cgmesTerminalId);
        if (cgmesTerminal != null && isSwitch(cgmesTerminal.conductingEquipmentType())) {
            return context.network().getSwitch(cgmesTerminal.conductingEquipment());
        }
        return null;
    }

    private static boolean isSwitch(String conductingEquipmentType) {
        return CgmesNames.SWITCH_TYPES.contains(conductingEquipmentType);
    }

    private static Branch.Side sequenceNumberToSide(int sequenceNumber) {
        if (sequenceNumber == 1) {
            return Branch.Side.ONE;
        } else if (sequenceNumber == 2) {
            return Branch.Side.TWO;
        } else {
            throw new PowsyblException(String.format("Unexpected sequenceNumber %d", sequenceNumber));
        }
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
