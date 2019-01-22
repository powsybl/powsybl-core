package com.powsybl.cgmes.conversion;

import java.util.HashMap;
import java.util.Map;

import com.powsybl.cgmes.model.CgmesTerminal;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;

public class NodeMapping {

    public NodeMapping() {
        cgmes2iidm = new HashMap<>(100);
        voltageLevelNumNodes = new HashMap<>(100);
    }

    public int iidmNodeForTerminal(CgmesTerminal t, VoltageLevel vl) {
        int iidmNodeForConductingEquipment = cgmes2iidm.computeIfAbsent(t.id(), id -> add(id, vl));
        // Add internal connection from terminal to connectivity node
        int iidmNodeForConnectivityNode = cgmes2iidm.computeIfAbsent(t.connectivityNode(), id -> add(id, vl));

        // For node-breaker models we create an internal connection between
        // the terminal and its connectivity node
        // Because there are some node-breaker models that,
        // in addition to the information of opened switches also set
        // the terminal.connected property to false,
        // we have decided to create fictitious switches to precisely
        // map this situation to IIDM

        if (t.connected()) {
            // FIXME(Luma): do not add an internal connection if is has already been added?
            vl.getNodeBreakerView().newInternalConnection()
                    .setNode1(iidmNodeForConductingEquipment)
                    .setNode2(iidmNodeForConnectivityNode)
                    .add();
        } else {
            vl.getNodeBreakerView().newSwitch()
                    .setFictitious(true)
                    // Use the id and name of terminal
                    .setId(t.id())
                    .setName(t.name())
                    .setNode1(iidmNodeForConductingEquipment)
                    .setNode2(iidmNodeForConnectivityNode)
                    .setOpen(true)
                    .setKind(SwitchKind.BREAKER)
                    .add();
        }

        return iidmNodeForConductingEquipment;
    }

    public int iidmNodeForConnectivityNode(String id, VoltageLevel vl) {
        return cgmes2iidm.computeIfAbsent(id, k -> add(k, vl));
    }

    private int add(String id, VoltageLevel vl) {
        // The identifier id could be a connectivityNode id or a Terminal id
        int iidmNode = newNode(vl);
        cgmes2iidm.put(id, iidmNode);
        return iidmNode;
    }

    private int newNode(VoltageLevel vl) {
        // Reserve a node number in this voltage level
        // If the voltage level does not exist in the mapping, iidmNode will be zero,
        // If a previous value was stored, new value is computed with "sum" 1
        // We want a zero-based index, so -1 + ...
        int numNodes = voltageLevelNumNodes.merge(vl, 1, Integer::sum);
        if (vl.getNodeBreakerView().getNodeCount() < numNodes) {
            // +10 to avoid calling setNodeCount too many times
            vl.getNodeBreakerView().setNodeCount(numNodes + 10);
        }
        return numNodes - 1;
    }

    private final Map<String, Integer> cgmes2iidm;
    private final Map<VoltageLevel, Integer> voltageLevelNumNodes;
}
