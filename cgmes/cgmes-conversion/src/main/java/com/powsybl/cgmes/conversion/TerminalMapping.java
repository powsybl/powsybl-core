/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.triplestore.api.PropertyBag;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TerminalMapping {

    public TerminalMapping() {
        terminals = new HashMap<>();
        terminalNumbers = new HashMap<>();
        topologicalNodesMapping = new HashMap<>();
    }

    public void add(String cgmesTerminal, Terminal iidmTerminal, int terminalNumber) {
        if (terminals.containsKey(cgmesTerminal)) {
            throw new CgmesModelException("Terminal already added, CGMES id : " + cgmesTerminal);
        }
        terminals.put(cgmesTerminal, iidmTerminal);
        terminalNumbers.put(cgmesTerminal, terminalNumber);
    }

    public Terminal find(String cgmesTerminalId) {
        return terminals.get(cgmesTerminalId);
    }

    public int number(String cgmesTerminalId) {
        return terminalNumbers.get(cgmesTerminalId);
    }

    public void buildTopologicalNodesMapping(PropertyBag p) {
        String tp = CgmesTerminal.topologicalNode(p);
        if (tp != null) {
            topologicalNodesMapping.computeIfAbsent(tp, t -> new ArrayList<>()).add(p.getId("Terminal"));
        }
    }

    public boolean areAssociated(String cgmesTerminalId, String topologicalNode) {
        return topologicalNodesMapping.get(topologicalNode).contains(cgmesTerminalId);
    }

    public Terminal findFromTopologicalNode(String topologicalNode) {
        Terminal disconnectedTerminal = null;
        if (topologicalNodesMapping.containsKey(topologicalNode)) {
            for (String cgmesTerminalId : topologicalNodesMapping.get(topologicalNode)) {
                Terminal terminal = find(cgmesTerminalId);
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
    // This is a somewhat dirty way of storing the side for the CGMES terminal
    // (only mapped when the terminal is connected to a branch)
    private final Map<String, Integer>  terminalNumbers;
    private final Map<String, List<String>> topologicalNodesMapping;
}
