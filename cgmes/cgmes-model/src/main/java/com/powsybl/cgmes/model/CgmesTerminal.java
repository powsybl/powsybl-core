package com.powsybl.cgmes.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CgmesTerminal {
    public CgmesTerminal(
            String id,
            String conductingEquipment,
            String conductingEquipmentType,
            boolean connected,
            PowerFlow flow) {
        this.id = id;
        this.conductingEquipment = conductingEquipment;
        this.conductingEquipmentType = conductingEquipmentType;
        this.connected = connected;
        this.flow = flow;
    }

    public void assignTP(String topologicalNode, String voltageLevel, String substation) {
        checkAssign(topologicalNode, voltageLevel, substation);
    }

    public void assignCN(String connectivityNode, String topologicalNode, String voltageLevel,
            String substation) {
        this.connectivityNode = connectivityNode;
        checkAssign(topologicalNode, voltageLevel, substation);
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

    public String voltageLevel() {
        return voltageLevel;
    }

    public String substation() {
        return substation;
    }

    public boolean connected() {
        return connected;
    }

    public PowerFlow flow() {
        return flow;
    }

    private void checkAssign(String topologicalNode, String voltageLevel, String substation) {
        checkAssignAttr("topologicalNode", this.topologicalNode, topologicalNode);
        this.topologicalNode = topologicalNode;
        checkAssignAttr("voltageLevel", this.voltageLevel, voltageLevel);
        this.voltageLevel = voltageLevel;
        checkAssignAttr("substation", this.substation, substation);
        this.substation = substation;
    }

    private void checkAssignAttr(String attribute, String value0, String value1) {
        if (value0 == null || value0.equals(value1)) {
            return;
        }
        String msg = String.format(
                "Terminal %s, TopologicalNode %s, ConnectivityNode %s. Inconsistent values for %s: previous %s, now %s",
                id,
                topologicalNode,
                connectivityNode,
                attribute, value0, value1);
        if (THROW_EXCEPTION_IF_INCONSISTENT_VALUES) {
            throw new CgmesModelException(msg);
        } else {
            LOG.warn(msg);
        }
    }

    private final String id;
    private final String conductingEquipment;
    private final String conductingEquipmentType;
    private final boolean connected;
    private final PowerFlow flow;

    private String connectivityNode;
    private String topologicalNode;
    private String voltageLevel;
    private String substation;

    private static final boolean THROW_EXCEPTION_IF_INCONSISTENT_VALUES = false;
    private static final Logger LOG = LoggerFactory.getLogger(CgmesTerminal.class);
}
