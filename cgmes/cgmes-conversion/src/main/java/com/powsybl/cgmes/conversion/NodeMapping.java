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
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class NodeMapping {

    public NodeMapping(Context context) {
        this.context = context;
        cgmes2iidm = new HashMap<>(100);
        voltageLevelNumNodes = new HashMap<>(100);
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
        return iidmNodeForTerminal(t, isSwitchEnd, vl, true);
    }

    public int iidmNodeForTerminal(CgmesTerminal t, boolean isSwitchEnd, VoltageLevel vl, boolean equipmentIsConnected) {
        // Because there are some node-breaker models that, in addition to the information of opened switches also set
        // the terminal.connected property to false, we have decided to create fictitious switches to precisely
        // map this situation to IIDM.
        // This behavior can be disabled through configuration.

        var fictSwCreationMode = context.config().getCreateFictitiousSwitchesForDisconnectedTerminalsMode();
        boolean bbsForEveryConnectivityNode = context.config().createBusbarSectionForEveryConnectivityNode();
        if (!bbsForEveryConnectivityNode && fictSwCreationMode == CgmesImport.FictitiousSwitchesCreationMode.NEVER) {
            if (isSwitchEnd) {
                // for switches the iidm node is the one from the connectivity node
                return cgmes2iidm.get(t.connectivityNode());
            } else if (!cgmes2iidm.containsKey(t.id())) {
                // Create internal connections but only if too many terminals on connectivity node
                createInternalConnectionsIfNeeded(t.connectivityNode(), vl);
            }
        } else {
            boolean connected = t.connected() && equipmentIsConnected;
            if (!connected && createFictitiousSwitch(fictSwCreationMode, isSwitchEnd)) {
                createFictitiousSwitch(t, vl);
            } else {
                createInternalConnection(t, vl);
            }
        }

        return cgmes2iidm.get(t.id());
    }

    private void createInternalConnectionsIfNeeded(String connectivityNode, VoltageLevel vl) {
        List<CgmesTerminal> connectivityNodeTerminals = context.terminalMapping().getConnectivityNodeTerminals(connectivityNode);
        switch (connectivityNodeTerminals.size()) {
            case 0 -> throw new PowsyblException("Erroneous connectivity node terminals mapping");
            case 1 ->
                // Only one terminal: no need to create an internal connection
                // Connectivity node and terminal share the same iidm node
                oneIidmNodeForBothTerminalAndConnectivityNode(connectivityNodeTerminals.get(0), vl);
            case 2 -> {
                // There are two terminals on the same connectivity node. We need one (and only one!) internal connection
                // between the two terminals as iidm can only handle one terminal per node.
                // If there's one bbs, it is placed at the connectivity node: in iidm we want the node of the bbs terminal
                // (and not the connectivity point!) to be where all feeders connect.
                // If there are two bbs on the same connectivity node (that is, two united bbs), this is only done for
                // the first bbs of the list.
                CgmesTerminal t0 = connectivityNodeTerminals.get(0);
                CgmesTerminal t1 = connectivityNodeTerminals.get(1);
                if (!isBusbarSectionTerminal(t0) && isBusbarSectionTerminal(t1)) {
                    oneIidmNodeForBothTerminalAndConnectivityNode(t1, vl);
                    createInternalConnection(t0, vl);
                } else {
                    oneIidmNodeForBothTerminalAndConnectivityNode(t0, vl);
                    createInternalConnection(t1, vl);
                }
            }
            default -> {
                // We need the connectivity node as connecting point between terminals
                // Similarly to previous case, if there's only one bbs, it is placed at the connectivity node.
                // If there are several bbs (that is, several united bbs), this is only done for the first one encountered,
                // the other ones are connected to the first one with internal connections.
                boolean bbsEncountered = false;
                for (CgmesTerminal t : connectivityNodeTerminals) {
                    if (isBusbarSectionTerminal(t) && !bbsEncountered) {
                        oneIidmNodeForBothTerminalAndConnectivityNode(t, vl);
                        bbsEncountered = true;
                    } else {
                        // Add internal connection from terminal to connectivity node as iidm can only handle one terminal per node
                        // For node-breaker models we create an internal connection between the terminal and its connectivity node
                        createInternalConnection(t, vl);
                    }
                }
            }
        }
    }

    private void createInternalConnection(CgmesTerminal t, VoltageLevel vl) {
        int iidmNodeForConnectivityNode = cgmes2iidm.computeIfAbsent(t.connectivityNode(), id -> newNode(vl));
        int iidmNodeForConductingEquipment = cgmes2iidm.computeIfAbsent(t.id(), id -> newNode(vl));
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(iidmNodeForConnectivityNode)
                .setNode2(iidmNodeForConductingEquipment)
                .add();
    }

    private void oneIidmNodeForBothTerminalAndConnectivityNode(CgmesTerminal t, VoltageLevel vl) {
        int iidmNodeForConnectivityNode = cgmes2iidm.computeIfAbsent(t.connectivityNode(), id -> newNode(vl));
        cgmes2iidm.put(t.id(), iidmNodeForConnectivityNode); // connectivity node and terminal share the same iidm node
    }

    private void createFictitiousSwitch(CgmesTerminal t, VoltageLevel vl) {
        int iidmNodeForConductingEquipment = cgmes2iidm.computeIfAbsent(t.id(), id -> newNode(vl));
        int iidmNodeForConnectivityNode = cgmes2iidm.computeIfAbsent(t.connectivityNode(), id -> newNode(vl));

        // Only add fictitious switches for disconnected terminals if not already added
        // Use the id and name of terminal
        String switchId = t.id() + "_SW_fict";
        if (vl.getNetwork().getSwitch(switchId) == null) {
            Switch sw = vl.getNodeBreakerView().newSwitch()
                    .setFictitious(true)
                    .setId(switchId)
                    .setName(t.name())
                    .setNode1(iidmNodeForConductingEquipment)
                    .setNode2(iidmNodeForConnectivityNode)
                    .setOpen(true)
                    .setKind(SwitchKind.BREAKER)
                    .setEnsureIdUnicity(context.config().isEnsureIdAliasUnicity())
                    .add();
            sw.setProperty(Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL, "true");
        }
    }

    private boolean isFirstBbsAtConnectivityNode(CgmesTerminal t, List<CgmesTerminal> connectivityNodeTerminals) {
        return connectivityNodeTerminals.stream()
                .filter(t1 -> t != t1)
                .filter(NodeMapping::isBusbarSectionTerminal)
                .noneMatch(t1 -> cgmes2iidm.containsKey(t1.id()));
    }

    private static boolean isBusbarSectionTerminal(CgmesTerminal t) {
        return t.conductingEquipmentType().equals("BusbarSection");
    }

    private static boolean createFictitiousSwitch(CgmesImport.FictitiousSwitchesCreationMode mode, boolean isSwitchEnd) {
        return switch (mode) {
            case ALWAYS -> true;
            case NEVER -> false;
            case ALWAYS_EXCEPT_SWITCHES -> !isSwitchEnd;
        };
    }

    public int iidmNodeForConnectivityNode(String id, VoltageLevel vl) {
        return cgmes2iidm.computeIfAbsent(id, k -> newNode(vl));
    }

    private int newNode(VoltageLevel vl) {
        // Reserve a node number in this voltage level
        // If the voltage level does not exist in the mapping, iidmNode will be zero,
        // If a previous value was stored, new value is computed with "sum" 1
        // We want a zero-based index, so -1 + ...
        int numNodes = voltageLevelNumNodes.merge(vl, 1, Integer::sum);
        return numNodes - 1;
    }

    private final Map<String, Integer> cgmes2iidm;
    private final Map<VoltageLevel, Integer> voltageLevelNumNodes;
    private final Context context;
}
