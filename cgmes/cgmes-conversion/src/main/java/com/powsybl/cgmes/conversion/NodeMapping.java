/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class NodeMapping {

    public NodeMapping() {
        cgmes2iidm = new HashMap<>(100);
        voltageLevelNumNodes = new HashMap<>(100);
    }

    public void copyFromConvertToUpdate(NodeMapping convertNodeMapping) {
        this.cgmes2iidm.putAll(convertNodeMapping.cgmes2iidm);
        this.voltageLevelNumNodes.putAll(convertNodeMapping.voltageLevelNumNodes);
    }

    public int iidmNodeForTerminal(CgmesTerminal t, VoltageLevel vl) {
        int iidmNodeForConductingEquipment = iidmNodeForConnectivityNode(t.id(), vl);
        // Add internal connection from terminal to connectivity node
        int iidmNodeForConnectivityNode = iidmNodeForConnectivityNode(t.connectivityNode(), vl);

        // For node-breaker models we create an internal connection between
        // the terminal and its connectivity node

        // TODO(Luma): do not add an internal connection if is has already been added?
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(iidmNodeForConductingEquipment)
                .setNode2(iidmNodeForConnectivityNode)
                .add();

        return iidmNodeForConductingEquipment;
    }

    public int iidmNodeForConnectivityNode(String id, VoltageLevel vl) {
        if (cgmes2iidm.containsKey(id)) {
            return cgmes2iidm.get(id).node;
        } else {
            RNode rnode = new RNode(vl.getId(), newNode(vl));
            cgmes2iidm.put(id, rnode);
            return rnode.node;
        }
    }

    public OptionalInt getIidmNodeForConnectivityNode(String connectivityNode) {
        return cgmes2iidm.containsKey(connectivityNode) ? OptionalInt.of(cgmes2iidm.get(connectivityNode).node) : OptionalInt.empty();
    }

    public Optional<String> getVoltageLevelIdForConnectivityNode(String connectivityNode) {
        return cgmes2iidm.containsKey(connectivityNode) ? Optional.of(cgmes2iidm.get(connectivityNode).voltageLevelId) : Optional.empty();
    }

    // This new nodes are not assotiated with ConnectivityNodes
    public int newNodeForSvInjection(VoltageLevel vl) {
        return newNode(vl);
    }

    private int newNode(VoltageLevel vl) {
        // Reserve a node number in this voltage level
        // If the voltage level does not exist in the mapping, iidmNode will be zero,
        // If a previous value was stored, new value is computed with "sum" 1
        // We want a zero-based index, so -1 + ...
        int numNodes = voltageLevelNumNodes.merge(vl, 1, Integer::sum);
        return numNodes - 1;
    }

    private record RNode(String voltageLevelId, int node) {
    }

    private final Map<String, RNode> cgmes2iidm;
    private final Map<VoltageLevel, Integer> voltageLevelNumNodes;
}
