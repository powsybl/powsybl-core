/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IIDM does not have terminals at ends of switches.
 * When we found a CGMES regulating terminal that corresponds to the end of a switch,
 * we have to map it to an equivalent IIDM terminal.
 * The mapping has to take into account if the controlled magnitude corresponds to a node (voltage)
 * or if it is a flow (active/reactive power)
r *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
final class RegulatingTerminalMapper {

    private RegulatingTerminalMapper() {
        // Empty constructor for utility class
    }

    public static Optional<Terminal> mapForVoltageControl(String cgmesTerminalId, Context context) {
        return cgmesTerminalId == null ?
                Optional.empty() :
                mapped(cgmesTerminalId, context)
                        .or(() -> new EquivalentTerminalFinderVoltageControl(cgmesTerminalId, context).find());
    }

    public static Optional<TerminalAndSign> mapForFlowControl(String cgmesTerminalId, Context context) {
        return cgmesTerminalId == null ?
                Optional.empty() :
                mapped(cgmesTerminalId, context)
                        .map(t -> new TerminalAndSign(t, 1))
                        .or(() -> new EquivalentTerminalFinderFlowControl(cgmesTerminalId, context).findWithSign());
    }

    public static Optional<Terminal> mapForTieFlow(String cgmesTerminalId, Context context) {
        return cgmesTerminalId == null ?
                Optional.empty() :
                mapped(cgmesTerminalId, context)
                        .or(() -> new EquivalentTerminalFinderTieFlow(cgmesTerminalId, context).find());
    }

    private static Optional<Terminal> mapped(String cgmesTerminalId, Context context) {
        return Optional.ofNullable(context.terminalMapping().get(cgmesTerminalId));
    }

    private static boolean isBusbarSection(Terminal t) {
        return t.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION;
    }

    private static boolean isEquipmentCapableOfVoltageControl(Terminal t) {
        IdentifiableType type = t.getConnectable().getType();
        return type == IdentifiableType.GENERATOR
                || type == IdentifiableType.SHUNT_COMPENSATOR
                || type == IdentifiableType.STATIC_VAR_COMPENSATOR
                || type == IdentifiableType.HVDC_CONVERTER_STATION;
    }

    private static <T> Stream<? extends Terminal> allTerminals(VoltageLevel vl, Set<T> vertices, Function<Terminal, T> terminalToVertex) {
        return vl.getConnectableStream()
                .map(c -> (Connectable<?>) c)
                .flatMap(c -> c.getTerminals().stream())
                .filter(terminal -> terminal.getVoltageLevel() == vl)
                .filter(terminal -> vertices.contains(terminalToVertex.apply(terminal)));
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

    abstract static class AbstractEquivalentTerminalFinder {
        protected final VoltageLevel vl;
        protected final Switch sw;
        protected final boolean isNodeBreakerModel;

        AbstractEquivalentTerminalFinder(String cgmesTerminalId, Context context) {
            this.sw = atSwitchEnd(cgmesTerminalId, context);
            this.vl = this.sw == null ? null : sw.getVoltageLevel();
            this.isNodeBreakerModel = this.vl != null && vl.getTopologyKind() == TopologyKind.NODE_BREAKER;
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

        private static Set<Integer> expandNodeBreaker(VoltageLevel voltageLevel, int node, Switch swTerminal) {
            Set<Integer> nodes = new HashSet<>();
            nodes.add(node);
            // Expand using opened and closed switches and internal connections, avoiding the given switch
            voltageLevel.getNodeBreakerView().traverse(node, (node1, sw, node2) -> {
                if (sw == swTerminal) {
                    return TraverseResult.TERMINATE_PATH;
                }
                nodes.add(node2);
                return TraverseResult.CONTINUE;
            });
            return nodes;
        }

        private static Set<Bus> expandBusBranch(VoltageLevel voltageLevel, Bus bus, Switch swTerminal) {
            Set<Bus> buses = new HashSet<>();
            buses.add(bus);
            // Expand using opened and closed switches
            voltageLevel.getBusBreakerView().traverse(bus, (bus1, sw, bus2) -> {
                if (sw == swTerminal) {
                    return TraverseResult.TERMINATE_PATH;
                }
                buses.add(bus2);
                return TraverseResult.CONTINUE;
            });
            return buses;
        }

        private static <T> Terminal uniqueTerminalAssociatedConductingEquipment(VoltageLevel vl, Set<T> vertices, Function<Terminal, T> terminalToVertex) {
            List<Terminal> terminals = allTerminals(vl, vertices, terminalToVertex)
                    .filter(terminal -> terminal.getConnectable().getType() != IdentifiableType.BUSBAR_SECTION)
                    .collect(Collectors.toList());
            return terminals.size() == 1 ? terminals.get(0) : null;
        }

        Optional<Terminal> find() {
            return Optional.empty();
        }

        Optional<TerminalAndSign> findWithSign() {
            return Optional.empty();
        }

        protected Terminal findTerminalChainEndNodeBreaker(int end, int otherEnd) {
            // TODO(Luma) rename: allNodesConnectedBySwitchesFromEndOfSwitch...
            // We are looking an equivalent terminal for a flow control magnitude
            // if we found the node of the other end of the switch it means there is a bypass,
            // we do not have a chain
            Set<Integer> nodes = expandNodeBreaker(vl, end, sw);
            if (nodes.contains(otherEnd)) {
                return null;
            }
            // TODO(Luma) rename, doc: If we find multiple terminals in the expansion we won't be able to decide which for a flow control magnitude
            return uniqueTerminalAssociatedConductingEquipment(vl, nodes, t -> t.getNodeBreakerView().getNode());
        }

        protected Terminal findTerminalChainEndBusBranch(Bus end, Bus otherEnd) {
            Set<Bus> buses = expandBusBranch(vl, end, sw);
            if (buses.contains(otherEnd)) {
                return null;
            }
            return uniqueTerminalAssociatedConductingEquipment(vl, buses, t -> t.getBusBreakerView().getBus());
        }
    }

    static class EquivalentTerminalFinderFlowControl extends AbstractEquivalentTerminalFinder {
        private final int cgmesTerminalEnd;

        EquivalentTerminalFinderFlowControl(String cgmesTerminalId, Context context) {
            super(cgmesTerminalId, context);
            this.cgmesTerminalEnd = context.cgmes().terminal(cgmesTerminalId).getSequenceNumber();
        }

        // CgmesTerminal is defined at end1 of the switch and the terminal is obtained at the switchChain from end2 then same sign
        // CgmesTerminal is defined at end2 of the switch and the terminal is obtained at the switchChain from end1 then same sign
        //
        // N1---sw---N2 N2---sw2---N3 N3---sw3---N4 ... Nn---swn---Nn+1 Nn+1---
        // ct = cgmesTerminal defined at N1, t = terminal defined at Nn+1, ct and t same sign
        //
        private static Optional<TerminalAndSign> bestTerminalInChain(int cgmesTerminalEnd, Terminal iidmTerminalEnd1, Terminal iidmTerminalEnd2) {
            if (cgmesTerminalEnd == 1 && iidmTerminalEnd2 != null) {
                return Optional.of(new TerminalAndSign(iidmTerminalEnd2, 1));
            } else if (cgmesTerminalEnd == 2 && iidmTerminalEnd1 != null) {
                return Optional.of(new TerminalAndSign(iidmTerminalEnd1, 1));
            } else if (cgmesTerminalEnd == 1 && iidmTerminalEnd1 != null) {
                return Optional.of(new TerminalAndSign(iidmTerminalEnd1, -1));
            } else if (cgmesTerminalEnd == 2 && iidmTerminalEnd2 != null) {
                return Optional.of(new TerminalAndSign(iidmTerminalEnd2, -1));
            } else {
                return Optional.empty();
            }
        }

        @Override
        Optional<TerminalAndSign> findWithSign() {
            return sw == null ? Optional.empty() : findTerminalInSwitchesChain(cgmesTerminalEnd);
        }

        // Find a terminal in a chain of switches.
        Optional<TerminalAndSign> findTerminalInSwitchesChain(int cgmesTerminalEnd) {
            if (isNodeBreakerModel) {
                return findTerminalInSwitchesChainNodeBreaker(cgmesTerminalEnd);
            } else {
                return findTerminalInSwitchesChainBusBranch(cgmesTerminalEnd);
            }
        }

        private Optional<TerminalAndSign> findTerminalInSwitchesChainNodeBreaker(int cgmesTerminalEnd) {
            int end1 = vl.getNodeBreakerView().getNode1(sw.getId());
            int end2 = vl.getNodeBreakerView().getNode2(sw.getId());

            Terminal terminalEnd1 = findTerminalChainEndNodeBreaker(end1, end2);
            Terminal terminalEnd2 = findTerminalChainEndNodeBreaker(end2, end1);
            return bestTerminalInChain(cgmesTerminalEnd, terminalEnd1, terminalEnd2);
        }

        private Optional<TerminalAndSign> findTerminalInSwitchesChainBusBranch(int cgmesTerminalEnd) {
            Bus end1 = vl.getBusBreakerView().getBus1(sw.getId());
            Bus end2 = vl.getBusBreakerView().getBus2(sw.getId());

            Terminal terminalEnd1 = findTerminalChainEndBusBranch(end1, end2);
            Terminal terminalEnd2 = findTerminalChainEndBusBranch(end2, end1);
            return bestTerminalInChain(cgmesTerminalEnd, terminalEnd1, terminalEnd2);
        }
    }

    static class EquivalentTerminalFinderVoltageControl extends AbstractEquivalentTerminalFinder {
        EquivalentTerminalFinderVoltageControl(String cgmesTerminalId, Context context) {
            super(cgmesTerminalId, context);
        }

        private static Set<Integer> expandNodeBreaker(VoltageLevel voltageLevel, int node) {
            Set<Integer> nodes = new HashSet<>();
            nodes.add(node);
            // Expand using opened and closed switches and internal connections
            voltageLevel.getNodeBreakerView().traverse(node, (node1, sw, node2) -> {
                nodes.add(node2);
                return TraverseResult.CONTINUE;
            });
            return nodes;
        }

        private static Set<Bus> expandBusBranch(VoltageLevel voltageLevel, Bus bus) {
            Set<Bus> buses = new HashSet<>();
            buses.add(bus);
            // Expand using opened and closed switches
            voltageLevel.getBusBreakerView().traverse(bus, (bus1, sw, bus2) -> {
                buses.add(bus2);
                return TraverseResult.CONTINUE;
            });
            return buses;
        }

        private static <T> List<Terminal> allTerminalsAssociatedConductingEquipment(VoltageLevel vl, Set<T> vertices, Function<Terminal, T> terminalToVertex) {
            // TODO(Luma) check if it is possible to refactor current implementation,
            //  instead of streaming from all terminals in the voltage level
            //  iterate only over terminals from vertices

            // For all connectables inside the voltage level:
            // For all terminals of connectable:
            // filter terminals in the voltage level
            // filter terminals with vertex in the list of vertices

            return allTerminals(vl, vertices, terminalToVertex).collect(Collectors.toList());
        }

        private static Optional<Terminal> bestTerminalInTopologicalNode(List<Terminal> terminals) {
            // Prefer first terminals corresponding to busbar sections,
            // then terminals of equipment capable of voltage control,
            // then any terminal available
            return terminals.stream()
                    .filter(RegulatingTerminalMapper::isBusbarSection).findFirst()
                    .or(() -> terminals.stream().filter(RegulatingTerminalMapper::isEquipmentCapableOfVoltageControl).findFirst())
                    .or(() -> terminals.stream().findFirst());
        }

        @Override
        Optional<Terminal> find() {
            return sw == null ? Optional.empty() : findTerminalInTopologicalNode();
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
            List<Terminal> terminals = findTerminalsNodeBreaker(end1);
            return bestTerminalInTopologicalNode(terminals);
        }

        private Optional<Terminal> findTerminalInTopologicalNodeBusBranch() {
            Bus end1 = vl.getBusBreakerView().getBus1(sw.getId());
            List<Terminal> terminals = findTerminalsBusBranch(end1);
            return bestTerminalInTopologicalNode(terminals);
        }

        protected List<Terminal> findTerminalsNodeBreaker(int node) {
            // TODO(Luma) rename: This is the list of all nodes reachable by switches or internal connections from this "end"
            //  The node of the other end of the switch will always be included in the expansion list,
            //  there is no need to check it
            Set<Integer> nodes = expandNodeBreaker(vl, node);
            // TODO(Luma) rename: from all the reachable nodes, we obtain a list of potential terminal candidates
            return allTerminalsAssociatedConductingEquipment(vl, nodes, t -> t.getNodeBreakerView().getNode());
        }

        private List<Terminal> findTerminalsBusBranch(Bus end) {
            Set<Bus> buses = expandBusBranch(vl, end);
            return allTerminalsAssociatedConductingEquipment(vl, buses, t -> t.getBusBreakerView().getBus());
        }
    }

    static class EquivalentTerminalFinderTieFlow extends AbstractEquivalentTerminalFinder {
        EquivalentTerminalFinderTieFlow(String cgmesTerminalId, Context context) {
            super(cgmesTerminalId, context);
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

        @Override
        Optional<Terminal> find() {
            return sw == null ? Optional.empty() : findDanglingLineTerminalInSwitchesChain();
        }

        // Find a dangingLine terminal in a switches chain
        Optional<Terminal> findDanglingLineTerminalInSwitchesChain() {
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
    }
}
