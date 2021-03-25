/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.*;

import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.Boundary;
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
        return topologicalNodesMapping.entrySet().stream()
                .filter(entry -> entry.getValue().contains(cgmesTerminalId))
                .map(Map.Entry::getKey)
                .map(this::findFromTopologicalNode)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public Boundary findBoundary(String cgmesTerminalId) {
        return boundaries.get(cgmesTerminalId);
    }

    public int number(String cgmesTerminalId) {
        if (terminalNumbers.get(cgmesTerminalId) != null) {
            return terminalNumbers.get(cgmesTerminalId);
        }
        return -1;
    }

    public void buildTopologicalNodesMapping(CgmesTerminal t) {
        String tp = t.topologicalNode();
        if (tp != null) {
            topologicalNodesMapping.computeIfAbsent(tp, tpnode -> new ArrayList<>()).add(t.id());
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
}
