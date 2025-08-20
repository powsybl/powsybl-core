/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class TerminalMapping {

    private final Map<String, Terminal> terminals = new HashMap<>();
    private final Map<String, Boundary> boundaries = new HashMap<>();
    // This is a somewhat dirty way of storing the side for the CGMES terminal
    // (only mapped when the terminal is connected to a branch)
    private final Map<String, Integer> terminalNumbers = new HashMap<>();
    private final Map<String, List<String>> topologicalNodesMapping = new HashMap<>();
    private final Map<String, List<CgmesTerminal>> connectivityNodeTerminalsMapping = new HashMap<>();
    private final Map<String, String> cgmesTerminalsMapping = new HashMap<>();

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

    public Terminal get(String cgmesTerminalId) {
        if (terminals.get(cgmesTerminalId) != null) {
            return terminals.get(cgmesTerminalId);
        }
        return null;
    }

    public String getTopologicalNode(String cgmesTerminalId) {
        return cgmesTerminalsMapping.get(cgmesTerminalId);
    }

    public Terminal findForFlowLimits(String cgmesTerminalId) {
        // We only considered Terminals assigned to an iidm equipment
        // Terminals not included in the iidm model are:
        // - boundary terminal of danglingLines (paired an unpaired)
        // - terminal of switches
        // Limits associated to these terminals are discarded
        return terminals.get(cgmesTerminalId);
    }

    public Terminal findForVoltageLimits(String cgmesTerminalId) {
        if (terminals.get(cgmesTerminalId) != null) {
            return terminals.get(cgmesTerminalId);
        }
        return findFromTopologicalNode(cgmesTerminalsMapping.get(cgmesTerminalId));
    }

    /**
     * @deprecated Not used anymore. Use {@link #findForVoltageLimits(String)}
     * or {@link #findForFlowLimits(String)} instead.
     */
    @Deprecated(since = "6.1.2")
    public Terminal find(String cgmesTerminalId, boolean loadingLimits) {
        throw new ConversionException("Deprecated. Not used anymore");
    }

    /**
     * @deprecated Not used anymore.
     */
    @Deprecated(since = "4.8.0")
    public Terminal find(String cgmesTerminalId, CgmesModel cgmesModel, Network network) {
        throw new ConversionException("Deprecated. Not used anymore");
    }

    public Boundary findBoundary(String cgmesTerminalId) {
        return boundaries.get(cgmesTerminalId);
    }

    public Boundary findBoundary(String cgmesTerminalId, CgmesModel cgmesModel) {
        CgmesTerminal cgmesTerminal = cgmesModel.terminal(cgmesTerminalId);
        if (cgmesTerminal != null && cgmesTerminal.conductingEquipmentType().equals(CgmesNames.EQUIVALENT_INJECTION)) {
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

    public void buildConnectivityNodeCgmesTerminalsMapping(CgmesTerminal t, Conversion.Config config) {
        // To avoid creating unnecessary internal connections, we need to compute the mapping of the connectivity nodes to the list
        // of CgmesTerminals
        // - this list should not contain switches
        // - if CgmesImport.FictitiousSwitchesCreationMode.ALWAYS or CgmesImport.FictitiousSwitchesCreationMode.ALWAYS_EXCEPT_SWITCHES,
        // this list should not contain the disconnected terminals
        boolean onlyConnected = config.getCreateFictitiousSwitchesForDisconnectedTerminalsMode() != CgmesImport.FictitiousSwitchesCreationMode.NEVER;
        if (CgmesNames.SWITCH_TYPES.contains(t.conductingEquipmentType())) {
            return; // switches do not have terminals in iidm, so we should not count them
        }
        String connectivityNode = t.connectivityNode();
        if (connectivityNode != null && (!onlyConnected || t.connected())) {
            connectivityNodeTerminalsMapping.computeIfAbsent(connectivityNode, k -> new ArrayList<>(1)).add(t);
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

    public List<CgmesTerminal> getConnectivityNodeTerminals(String id) {
        return connectivityNodeTerminalsMapping.getOrDefault(id, List.of());
    }
}
