/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.*;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TerminalMapping {

    public TerminalMapping() {
        boundaries = new HashMap<>();
        terminals = new HashMap<>();
        terminalNumbers = new HashMap<>();
        topologicalNodesMapping = new HashMap<>();
        cgmesTerminalsMapping = new HashMap<>();
    }

    public void add(String cgmesTerminal, Terminal iidmTerminal, int terminalNumber) {
        if (terminals.containsKey(cgmesTerminal) || boundaries.containsKey(cgmesTerminal)) {
            throw new CgmesModelException("Terminal already added, CGMES id : " + cgmesTerminal);
        }
        terminals.put(cgmesTerminal, iidmTerminal);
        terminalNumbers.put(cgmesTerminal, terminalNumber);
    }

    public void add(String cgmesTerminal, Boundary iidmBoundary, int terminalNumber) {
        if (terminals.containsKey(cgmesTerminal) || boundaries.containsKey(cgmesTerminal)) {
            throw new CgmesModelException("Terminal already added, CGMES id : " + cgmesTerminal);
        }
        boundaries.put(cgmesTerminal, iidmBoundary);
        terminalNumbers.put(cgmesTerminal, terminalNumber);
    }

    public Terminal find(String cgmesTerminalId) {
        if (terminals.get(cgmesTerminalId) != null) {
            return terminals.get(cgmesTerminalId);
        }
        return findFromTopologicalNode(cgmesTerminalsMapping.get(cgmesTerminalId));
    }

    public Terminal find(String cgmesTerminalId, CgmesModel cgmesModel, Network network) {
        CgmesTerminal cgmesTerminal = cgmesModel.terminal(cgmesTerminalId);

        if (isSwitch(cgmesTerminal.conductingEquipmentType())) {
            Switch sw = network.getSwitch(cgmesTerminal.conductingEquipment());
            if (sw == null) {
                return find(cgmesTerminalId);
            }
            Terminal terminal = new SwitchesChain(sw.getVoltageLevel(), sw, cgmesModel.isNodeBreaker()).getBestTerminalChain();
            if (terminal != null) {
                return terminal;
            }
        }
        return find(cgmesTerminalId);
    }

    private static boolean isSwitch(String conductingEquipmentType) {
        return conductingEquipmentType.equals("Breaker") || conductingEquipmentType.equals("Disconnector");
    }

    public Boundary findBoundary(String cgmesTerminalId) {
        return boundaries.get(cgmesTerminalId);
    }

    public Boundary findBoundary(String cgmesTerminalId, CgmesModel cgmesModel) {
        CgmesTerminal cgmesTerminal = cgmesModel.terminal(cgmesTerminalId);
        if (cgmesTerminal.conductingEquipmentType().equals("EquivalentInjection")) {
            String acLineSegmentCgmesTerminalId = findAssociatedAcLineSegmentCgmesTerminalId(cgmesModel, cgmesTerminal);
            if (acLineSegmentCgmesTerminalId != null) {
                return findBoundary(acLineSegmentCgmesTerminalId);
            }
        }
        return findBoundary(cgmesTerminalId);
    }

    private static String findAssociatedAcLineSegmentCgmesTerminalId(CgmesModel cgmesModel, CgmesTerminal cgmesTerminal) {
        CgmesTerminal acLineSegmentCgmesTerminal = cgmesModel.computedTerminals().stream()
            .filter(computedTerminal -> cgmesTerminalOk(computedTerminal, cgmesTerminal)).findFirst().orElse(null);
        if (acLineSegmentCgmesTerminal != null) {
            return acLineSegmentCgmesTerminal.id();
        }
        return null;
    }

    private static boolean cgmesTerminalOk(CgmesTerminal acLineSegmentCgmesTerminal, CgmesTerminal cgmesTerminal) {
        if (!acLineSegmentCgmesTerminal.conductingEquipmentType().equals("ACLineSegment")) {
            return false;
        }
        if (acLineSegmentCgmesTerminal.connectivityNode() != null
            && acLineSegmentCgmesTerminal.connectivityNode().equals(cgmesTerminal.connectivityNode())) {
            return true;
        }
        return acLineSegmentCgmesTerminal.topologicalNode() != null
            && acLineSegmentCgmesTerminal.topologicalNode().equals(cgmesTerminal.topologicalNode());
    }

    public int number(String cgmesTerminalId) {
        if (terminalNumbers.get(cgmesTerminalId) != null) {
            return terminalNumbers.get(cgmesTerminalId);
        }
        return -1;
    }

    public void buildTopologicalNodeCgmesTerminalsMapping(CgmesTerminal t) {
        String tp = t.topologicalNode();
        if (tp != null) {
            topologicalNodesMapping.computeIfAbsent(tp, tpnode -> new ArrayList<>()).add(t.id());
            cgmesTerminalsMapping.put(t.id(), tp);
        }
    }

    public boolean areAssociated(String cgmesTerminalId, String topologicalNode) {
        return topologicalNodesMapping.get(topologicalNode).contains(cgmesTerminalId);
    }

    public Terminal findFromTopologicalNode(String topologicalNode) {
        Terminal disconnectedTerminal = null;
        if (topologicalNodesMapping.containsKey(topologicalNode)) {
            for (String cgmesTerminalId : topologicalNodesMapping.get(topologicalNode)) {
                Terminal terminal = terminals.get(cgmesTerminalId);
                if (terminal != null) {
                    if (terminal.isConnected()) { // returns the first connected terminal associated with the given topological node
                        return terminal;
                    } else if (disconnectedTerminal == null) {
                        disconnectedTerminal = terminal;
                    }
                }
            }
        }
        // if no connected terminal is found associated with the given topological node
        // returns a disconnected terminal associated with the given topological node
        // if no terminal found, returns null
        return disconnectedTerminal;
    }

    public String findCgmesTerminalFromTopologicalNode(String topologicalNode) {
        return topologicalNodesMapping.containsKey(topologicalNode) ? topologicalNodesMapping.get(topologicalNode).get(0) : null;
    }

    private final Map<String, Terminal> terminals;
    private final Map<String, Boundary> boundaries;
    // This is a somewhat dirty way of storing the side for the CGMES terminal
    // (only mapped when the terminal is connected to a branch)
    private final Map<String, Integer>  terminalNumbers;
    private final Map<String, List<String>> topologicalNodesMapping;
    private final Map<String, String> cgmesTerminalsMapping;
}
