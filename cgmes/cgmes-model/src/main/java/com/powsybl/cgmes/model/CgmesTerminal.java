package com.powsybl.cgmes.model;

import com.powsybl.triplestore.api.PropertyBag;

public class CgmesTerminal {
    public CgmesTerminal(PropertyBag t) {
        this.id = t.getId(CgmesNames.TERMINAL);
        this.conductingEquipment = t.getId("ConductingEquipment");
        this.conductingEquipmentType = t.getLocal("conductingEquipmentType");

        this.connectivityNode = t.getId("ConnectivityNode");
        this.topologicalNode = t.getId("TopologicalNode");

        // A TopologicalNode can be obtained from a Terminal using
        // relationship Terminal.TopologicalNode or
        // Terminal.ConnectivityNode.TopologicalNode

        // In some cases the two TopologicalNode's obtained through these
        // two relationships may be different
        // (FIXME(Luma): missing reference here)

        this.connected = t.asBoolean("connected", false);
        this.flow = new PowerFlow(t, "p", "q");
    }

    public String id() {
        return id;
    }

    public String conductingEquipment() {
        return conductingEquipment;
    }

    public String conductingEquipmentType() {
        return conductingEquipmentType;
    }

    public String connectivityNode() {
        return connectivityNode;
    }

    public String topologicalNode() {
        return topologicalNode;
    }

    public boolean connected() {
        return connected;
    }

    public PowerFlow flow() {
        return flow;
    }

    private final String id;
    private final String conductingEquipment;
    private final String conductingEquipmentType;
    private final boolean connected;
    private final PowerFlow flow;
    private final String connectivityNode;
    private final String topologicalNode;
}
