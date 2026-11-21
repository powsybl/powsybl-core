/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class NodeMapping {

    private final Map<String, Integer> cgmes2iidm = HashMap.newHashMap(100);
    private final Map<VoltageLevel, Integer> voltageLevelNumNodes = HashMap.newHashMap(100);
    private final Map<String, ReservedNode> reservedTerminalNodes = HashMap.newHashMap(100);
    private final Context context;

    public NodeMapping(Context context) {
        this.context = context;
    }

    public int iidmNodeForTopologicalNode(String id, int associatedNode, VoltageLevel vl) {
        String uniqueId;
        int i = 0;
        do {
            uniqueId = id + "#" + i++;
        } while (cgmes2iidm.containsKey(uniqueId) && i < Integer.MAX_VALUE);
        int iidmNodeForTopologicalNode = newNode(vl);
        cgmes2iidm.put(uniqueId, iidmNodeForTopologicalNode);
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(iidmNodeForTopologicalNode)
                .setNode2(associatedNode)
                .add();
        return iidmNodeForTopologicalNode;
    }

    public int iidmNodeForTerminal(CgmesTerminal t, boolean isSwitchEnd, VoltageLevel vl) {
        if (isSwitchEnd) {
            // For switches the iidm node is the one from the connectivity node
            return cgmes2iidm.get(t.connectivityNode());
        } else if (context.config().createBusbarSectionForEveryConnectivityNode()) {
            createInternalConnection(t, vl);
        } else if (!cgmes2iidm.containsKey(t.id())) {
            // Create internal connections but only if too many terminals on connectivity node
            createInternalConnectionsIfNeeded(t.connectivityNode(), vl);
        }

        return cgmes2iidm.get(t.id());
    }

    private void createInternalConnectionsIfNeeded(String connectivityNode, VoltageLevel vl) {
        List<CgmesTerminal> connectivityNodeTerminals = context.terminalMapping().getConnectivityNodeTerminals(connectivityNode);
        if (connectivityNodeTerminals.isEmpty()) {
            throw new PowsyblException("Erroneous connectivity node terminals mapping");
        }
        CgmesTerminal terminalAtConnectivityNode = terminalSharingConnectivityNodeIidmNode(connectivityNodeTerminals);
        for (CgmesTerminal t : connectivityNodeTerminals) {
            if (t == terminalAtConnectivityNode) {
                oneIidmNodeForBothTerminalAndConnectivityNode(t, vl);
            } else {
                // Add internal connection from terminal to connectivity node as iidm can only handle one terminal per node
                createInternalConnection(t, vl);
            }
        }
    }

    /**
     * Get the terminal that shares the IIDM node of its connectivity node, if any. The other terminals of the
     * connectivity node have their own IIDM node, linked to the connectivity node one with an internal connection.
     *
     * @param connectivityNodeTerminals the terminals of a connectivity node, sorted by id
     * @return the terminal sharing the IIDM node of the connectivity node, or null if there is none
     */
    private CgmesTerminal terminalSharingConnectivityNodeIidmNode(List<CgmesTerminal> connectivityNodeTerminals) {
        if (context.config().createBusbarSectionForEveryConnectivityNode()) {
            // The node of the connectivity node is used by the busbar section created for it
            return null;
        }
        return switch (connectivityNodeTerminals.size()) {
            case 0 -> null;
            // Only one terminal: no need to create an internal connection
            case 1 -> connectivityNodeTerminals.get(0);
            case 2 -> {
                // There are two terminals on the same connectivity node. We need one (and only one!) internal connection
                // between the two terminals as iidm can only handle one terminal per node.
                // There's a special treatment for BusbarSections described below. Indeed, a bbs is a central equipment,
                // to which bays/cells connect. We want to keep this physical centrality in iidm, especially for
                // easy graph traversals and visualization. Note that there are no other central equipments of this type.
                // - if there's one bbs, it is placed at the connectivity node: in iidm we want the node of the bbs terminal
                // (and not the connectivity point!) to be where all feeders connect.
                // - if there are two bbs on the same connectivity node (that is, two united bbs), this is only done for
                // the first bbs of the list.
                CgmesTerminal t0 = connectivityNodeTerminals.get(0);
                CgmesTerminal t1 = connectivityNodeTerminals.get(1);
                yield !isBusbarSectionTerminal(t0) && isBusbarSectionTerminal(t1) ? t1 : t0;
            }
            // We need the connectivity node as connecting point between terminals
            // Similarly to previous case, there's a special treatment for BusbarSections:
            // - if there's only one bbs, it is placed at the connectivity node.
            // - if there are several bbs (that is, several united bbs), this is only done for the first one
            // encountered, the other ones are connected to the first one with internal connections.
            default -> connectivityNodeTerminals.stream()
                    .filter(NodeMapping::isBusbarSectionTerminal)
                    .findFirst()
                    .orElse(null);
        };
    }

    private void createInternalConnection(CgmesTerminal t, VoltageLevel vl) {
        int iidmNodeForConnectivityNode = cgmes2iidm.computeIfAbsent(t.connectivityNode(), id -> newNode(vl));
        int iidmNodeForTerminal = cgmes2iidm.computeIfAbsent(t.id(), id -> reservedOrNewNode(id, vl));
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(iidmNodeForConnectivityNode)
                .setNode2(iidmNodeForTerminal)
                .add();
    }

    private void oneIidmNodeForBothTerminalAndConnectivityNode(CgmesTerminal t, VoltageLevel vl) {
        int iidmNodeForConnectivityNode = cgmes2iidm.computeIfAbsent(t.connectivityNode(), id -> newNode(vl));
        cgmes2iidm.put(t.id(), iidmNodeForConnectivityNode); // connectivity node and terminal share the same iidm node
    }

    private static boolean isBusbarSectionTerminal(CgmesTerminal t) {
        return t.conductingEquipmentType().equals("BusbarSection");
    }

    /**
     * Get the IIDM node of a connectivity node, creating it if needed. When it is created, IIDM nodes are also reserved
     * for the terminals of the connectivity node that will need their own node. This way, the IIDM node numbers only
     * depend on the order in which the connectivity nodes are converted, and not on the order of the equipment.
     */
    public int iidmNodeForConnectivityNode(String id, VoltageLevel vl) {
        Integer node = cgmes2iidm.get(id);
        if (node == null) {
            node = newNode(vl);
            cgmes2iidm.put(id, node);
            reserveTerminalNodes(id, vl);
        }
        return node;
    }

    private void reserveTerminalNodes(String connectivityNode, VoltageLevel vl) {
        List<CgmesTerminal> connectivityNodeTerminals = context.terminalMapping().getConnectivityNodeTerminals(connectivityNode);
        CgmesTerminal terminalAtConnectivityNode = terminalSharingConnectivityNodeIidmNode(connectivityNodeTerminals);
        for (CgmesTerminal t : connectivityNodeTerminals) {
            if (t != terminalAtConnectivityNode) {
                reservedTerminalNodes.put(t.id(), new ReservedNode(vl, newNode(vl)));
            }
        }
    }

    private int reservedOrNewNode(String terminalId, VoltageLevel vl) {
        ReservedNode reservedNode = reservedTerminalNodes.remove(terminalId);
        // The reservation is only valid in the voltage level where it was made
        return reservedNode != null && reservedNode.voltageLevel() == vl ? reservedNode.node() : newNode(vl);
    }

    private record ReservedNode(VoltageLevel voltageLevel, int node) {
    }

    private int newNode(VoltageLevel vl) {
        // Reserve a node number in this voltage level
        // If the voltage level does not exist in the mapping, iidmNode will be zero,
        // If a previous value was stored, new value is computed with "sum" 1
        // We want a zero-based index, so -1 + ...
        int numNodes = voltageLevelNumNodes.merge(vl, 1, Integer::sum);
        return numNodes - 1;
    }
}
