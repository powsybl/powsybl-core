package com.powsybl.cgmes.model;

import com.powsybl.triplestore.api.PropertyBag;

public class CgmesTerminal {
    public CgmesTerminal(PropertyBag t) {
        this.id = t.getId(CgmesNames.TERMINAL);
        this.conductingEquipment = t.getId("ConductingEquipment");
        this.conductingEquipmentType = t.getLocal("conductingEquipmentType");

        this.connectivityNode = t.getId("ConnectivityNode");
        this.connectivityNodeName = t.getLocal("connectivityNodeName");
        this.connectivityNodeContainer = t.getId("ConnectivityNodeContainer");

        // If no TopologicalNode is specified for the Terminal
        // Use the TopologicalNode of the ConnectivityNode
        if (t.containsKey("TopologicalNodeT")) {
            this.topologicalNode = t.getId("TopologicalNodeT");
            this.topologicalNodeName = t.getId("topologicalNodeTName");
            this.topologicalNodeBaseVoltage = t.getId("topologicalNodeTBaseVoltage");
            this.connectivityNodeContainerTopo = t.getId("ConnectivityNodeContainerTTopo");
            this.v = t.get("vT");
            this.angle = t.get("angleT");
        } else {
            this.topologicalNode = t.getId("TopologicalNodeCN");
            this.topologicalNodeName = t.getId("topologicalNodeCNName");
            this.topologicalNodeBaseVoltage = t.getId("topologicalNodeCNBaseVoltage");
            this.connectivityNodeContainerTopo = t.getId("ConnectivityNodeContainerCNTopo");
            this.v = t.get("vCN");
            this.angle = t.get("angleCN");
        }
        // FIXME(Luma): another possibility: the two topo nodes are different and have
        // different voltages
        // which one should we take ?

        // FIXME(Luma): We could check for inconsistencies
        // If both TopologicalNodes are present, check that they are the same

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

    public String connectivityNodeName() {
        return connectivityNodeName;
    }

    public String topologicalNode() {
        return topologicalNode;
    }

    public String topologicalNodeName() {
        return topologicalNodeName;
    }

    public String topologicalNodeBaseVoltage() {
        return topologicalNodeBaseVoltage;
    }

    public String v() {
        return v;
    }

    public String angle() {
        return angle;
    }

    public String connectivityNodeContainer() {
        return connectivityNodeContainer;
    }

    public String connectivityNodeContainerTopo() {
        return connectivityNodeContainerTopo;
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
    private final String connectivityNodeName;
    private final String topologicalNodeName;
    private final String topologicalNodeBaseVoltage;
    private final String connectivityNodeContainer;
    private final String connectivityNodeContainerTopo;

    // FIXME(Luma) topological node data, temporal
    private final String v;
    private final String angle;
}
