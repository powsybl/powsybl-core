/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.powsybl.psse.model.pf.internal.PsseSubstationEquipmentTerminal;
import com.powsybl.psse.model.pf.internal.PsseSubstationNode;
import com.powsybl.psse.model.pf.internal.PsseSubstationRecord;
import com.powsybl.psse.model.pf.internal.PsseSubstationSwitchingDevice;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

@JsonIgnoreProperties({"record"})
@JsonPropertyOrder({"is", "name", "lati", "long", "srg"})

public class PsseSubstation {

    public PsseSubstation(PsseSubstationRecord substationRecord,
                          List<PsseSubstationNode> nodes, List<PsseSubstationSwitchingDevice> switchingDevices,
                          List<PsseSubstationEquipmentTerminal> equipmentTerminals) {
        this.substationRecord = substationRecord;
        this.nodes = nodes;
        this.switchingDevices = switchingDevices;
        this.equipmentTerminals = equipmentTerminals;
    }

    private final PsseSubstationRecord substationRecord;
    private final List<PsseSubstationNode> nodes;
    private final List<PsseSubstationSwitchingDevice> switchingDevices;
    private final List<PsseSubstationEquipmentTerminal> equipmentTerminals;

    public int getIs() {
        return substationRecord.getIs();
    }

    public String getName() {
        return substationRecord.getName();
    }

    public double getLati() {
        return substationRecord.getLati();
    }

    public double getLong() {
        return substationRecord.getLong();
    }

    public double getSrg() {
        return substationRecord.getSrg();
    }

    public PsseSubstationRecord getRecord() {
        return substationRecord;
    }

    public List<PsseSubstationNode> getNodes() {
        return nodes;
    }

    public List<PsseSubstationSwitchingDevice> getSwitchingDevices() {
        return switchingDevices;
    }

    public List<PsseSubstationEquipmentTerminal> getEquipmentTerminals() {
        return equipmentTerminals;
    }

    public PsseSubstation copy() {
        PsseSubstationRecord copyRecord = this.substationRecord.copy();

        List<PsseSubstationNode> copyNodes = new ArrayList<>();
        this.nodes.forEach(node -> copyNodes.add(node.copy()));

        List<PsseSubstationSwitchingDevice> copySwitchingDevices = new ArrayList<>();
        this.switchingDevices.forEach(switchingDevice -> copySwitchingDevices.add(switchingDevice.copy()));

        List<PsseSubstationEquipmentTerminal> copyEquipmentTerminals = new ArrayList<>();
        this.equipmentTerminals.forEach(equipmentTerminal -> copyEquipmentTerminals.add(equipmentTerminal.copy()));

        return new PsseSubstation(copyRecord, copyNodes, copySwitchingDevices, copyEquipmentTerminals);
    }

    public static boolean isOneBus(String type) {
        return type.equals("L") || type.equals("F") || type.equals("M")
                || type.equals("S") || type.equals("I") || type.equals("D")
                || type.equals("V") || type.equals("N") || type.equals("A");
    }

    public static boolean isTwoBuses(String type) {
        return type.equals("B") || type.equals("2");
    }

    public static boolean isThreeBuses(String type) {
        return type.equals("3");
    }
}
