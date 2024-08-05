/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.HashMap;
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
        int iidmNodeForConductingEquipment = cgmes2iidm.computeIfAbsent(t.id(), id -> newNode(vl));
        // Add internal connection from terminal to connectivity node
        int iidmNodeForConnectivityNode = cgmes2iidm.computeIfAbsent(t.connectivityNode(), id -> newNode(vl));

        // For node-breaker models we create an internal connection between
        // the terminal and its connectivity node
        // Because there are some node-breaker models that,
        // in addition to the information of opened switches also set
        // the terminal.connected property to false,
        // we have decided to create fictitious switches to precisely
        // map this situation to IIDM.
        // This behavior can be disabled through configuration.

        boolean connected = t.connected() && equipmentIsConnected;

        if (connected || !createFictitiousSwitch(context.config().getCreateFictitiousSwitchesForDisconnectedTerminalsMode(), isSwitchEnd)) {
            // TODO(Luma): do not add an internal connection if is has already been added?
            vl.getNodeBreakerView().newInternalConnection()
                    .setNode1(iidmNodeForConductingEquipment)
                    .setNode2(iidmNodeForConnectivityNode)
                    .add();
        } else {
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

        return iidmNodeForConductingEquipment;
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
