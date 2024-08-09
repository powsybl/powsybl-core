/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.NullString;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */

@JsonIgnoreProperties({"record"})
@JsonPropertyOrder({"is", "name", "lati", "long", "srg"})

public class PsseSubstation {

    public PsseSubstation(PsseSubstationRecord srecord,
                          List<PsseSubstationNode> nodes, List<PsseSubstationSwitchingDevice> switchingDevices,
                          List<PsseSubstationEquipmentTerminal> equipmentTerminals) {
        this.srecord = srecord;
        this.nodes = nodes;
        this.switchingDevices = switchingDevices;
        this.equipmentTerminals = equipmentTerminals;
    }

    private final PsseSubstationRecord srecord;
    private final List<PsseSubstationNode> nodes;
    private final List<PsseSubstationSwitchingDevice> switchingDevices;
    private final List<PsseSubstationEquipmentTerminal> equipmentTerminals;

    public int getIs() {
        return srecord.is;
    }

    public String getName() {
        return srecord.name;
    }

    public double getLati() {
        return srecord.lati;
    }

    public double getLong() {
        return srecord.longi;
    }

    public double getSrg() {
        return srecord.srg;
    }

    public PsseSubstationRecord getRecord() {
        return srecord;
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
        PsseSubstationRecord copyRecord = this.srecord.copy();

        List<PsseSubstationNode> copyNodes = new ArrayList<>();
        this.nodes.forEach(node -> copyNodes.add(node.copy()));

        List<PsseSubstationSwitchingDevice> copySwitchingDevices = new ArrayList<>();
        this.switchingDevices.forEach(switchingDevice -> copySwitchingDevices.add(switchingDevice.copy()));

        List<PsseSubstationEquipmentTerminal> copyEquipmentTerminals = new ArrayList<>();
        this.equipmentTerminals.forEach(equipmentTerminal -> copyEquipmentTerminals.add(equipmentTerminal.copy()));

        return new PsseSubstation(copyRecord, copyNodes, copySwitchingDevices, copyEquipmentTerminals);
    }

    public static class PsseSubstationRecord {
        @Parsed(field = {"is", "isub"})
        private int is;
        @Parsed(defaultNullRead = "                                        ")
        private String name;

        @Parsed
        private double lati = 0.0;

        @Parsed(field = {"long"})
        private double longi = 0.0;

        @Parsed
        private double srg = 0.0;

        public int getIs() {
            return is;
        }

        public void setIs(int is) {
            this.is = is;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getLati() {
            return lati;
        }

        public void setLati(double lati) {
            this.lati = lati;
        }

        public double getLong() {
            return longi;
        }

        public void setLong(double longi) {
            this.longi = longi;
        }

        public double getSrg() {
            return srg;
        }

        public void setSrg(double srg) {
            this.srg = srg;
        }

        public PsseSubstationRecord copy() {
            PsseSubstationRecord copy = new PsseSubstationRecord();
            copy.is = this.is;
            copy.name = this.name;
            copy.lati = this.lati;
            copy.longi = this.longi;
            copy.srg = this.srg;
            return copy;
        }
    }

    public static class PsseSubstationNode {

        @Parsed(field = {"ni", "inode"})
        private int ni;

        @Parsed(defaultNullRead = "                                        ")
        private String name;

        @Parsed(field = {"i", "ibus"})
        private int i;

        @Parsed(field = {"stat", "status"})
        private int status = 1;

        @Parsed
        private double vm = 1.0;

        @Parsed
        private double va = 0.0;

        public int getNi() {
            return ni;
        }

        public void setNi(int ni) {
            this.ni = ni;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public double getVm() {
            return vm;
        }

        public void setVm(double vm) {
            this.vm = vm;
        }

        public double getVa() {
            return va;
        }

        public void setVa(double va) {
            this.va = va;
        }

        public PsseSubstationNode copy() {
            PsseSubstationNode copy = new PsseSubstationNode();
            copy.ni = this.ni;
            copy.name = this.name;
            copy.i = this.i;
            copy.status = this.status;
            copy.vm = this.vm;
            copy.va = this.va;
            return copy;
        }
    }

    public static class PsseSubstationSwitchingDevice {

        @Parsed(field = {"ni", "inode"})
        private int ni;

        @Parsed(field = {"nj", "jnode"})
        private int nj = 0;

        @Parsed(field = {"ckt", "swdid"}, defaultNullRead = "1 ")
        private String ckt;

        @Parsed(defaultNullRead = "                                        ")
        private String name;

        @Parsed
        private int type = 1;

        @Parsed(field = {"status", "stat"})
        private int status = 1;

        @Parsed
        private int nstat = 1;

        @Parsed(field = {"x", "xpu"})
        private double x = 0.0001;

        @Parsed
        private double rate1 = 0.0;

        @Parsed
        private double rate2 = 0.0;

        @Parsed
        private double rate3 = 0.0;

        public int getNi() {
            return ni;
        }

        public void setNi(int ni) {
            this.ni = ni;
        }

        public int getNj() {
            return nj;
        }

        public void setNj(int nj) {
            this.nj = nj;
        }

        public String getCkt() {
            return ckt;
        }

        public void setCkt(String ckt) {
            this.ckt = ckt;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getNstat() {
            return nstat;
        }

        public void setNstat(int nstat) {
            this.nstat = nstat;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getRate1() {
            return rate1;
        }

        public void setRate1(double rate1) {
            this.rate1 = rate1;
        }

        public double getRate2() {
            return rate2;
        }

        public void setRate2(double rate2) {
            this.rate2 = rate2;
        }

        public double getRate3() {
            return rate3;
        }

        public void setRate3(double rate3) {
            this.rate3 = rate3;
        }

        public PsseSubstationSwitchingDevice copy() {
            PsseSubstationSwitchingDevice copy = new PsseSubstationSwitchingDevice();
            copy.ni = this.ni;
            copy.nj = this.nj;
            copy.ckt = this.ckt;
            copy.name = this.name;
            copy.type = this.type;
            copy.status = this.status;
            copy.nstat = this.nstat;
            copy.x = this.x;
            copy.rate1 = this.rate1;
            copy.rate2 = this.rate2;
            copy.rate3 = this.rate3;
            return copy;
        }
    }

    public static class PsseSubstationEquipmentTerminal {

        @Parsed(field = {"i", "ibus"})
        private int i;

        @Parsed(field = {"ni", "inode"})
        private int ni;

        @Parsed
        private String type;

        @Parsed(field = {"id", "eqid"}, defaultNullRead = "1 ")
        private String id;

        @NullString(nulls = {"null"})
        @Parsed(field = {"j", "jbus"})
        private int j = 0;

        @NullString(nulls = {"null"})
        @Parsed(field = {"k", "kbus"})
        private int k = 0;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public int getNi() {
            return ni;
        }

        public void setNi(int ni) {
            this.ni = ni;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getJ() {
            return j;
        }

        public void setJ(int j) {
            this.j = j;
        }

        public int getK() {
            return k;
        }

        public void setK(int k) {
            this.k = k;
        }

        public PsseSubstationEquipmentTerminal copy() {
            PsseSubstationEquipmentTerminal copy = new PsseSubstationEquipmentTerminal();
            copy.i = this.i;
            copy.ni = this.ni;
            copy.type = this.type;
            copy.id = this.id;
            copy.j = this.j;
            copy.k = this.k;
            return copy;
        }
    }

    public static class PsseSubstationEquipmentTerminalCommonStart {

        @Parsed
        private int i;

        @Parsed
        private int ni;

        @Parsed
        private String type;

        public String getType() {
            return type;
        }
    }

    public static class PsseSubstationNodex {

        public PsseSubstationNodex() {
        }

        public PsseSubstationNodex(int isub, PsseSubstationNode node) {
            this.isub = isub;
            this.node = node;
        }

        @Parsed
        private int isub;
        @Nested
        private PsseSubstationNode node;

        public int getIsub() {
            return isub;
        }

        public PsseSubstationNode getNode() {
            return node;
        }
    }

    public static class PsseSubstationSwitchingDevicex {

        public PsseSubstationSwitchingDevicex() {

        }

        public PsseSubstationSwitchingDevicex(int isub, PsseSubstationSwitchingDevice switchingDevice) {
            this.isub = isub;
            this.switchingDevice = switchingDevice;
        }

        @Parsed
        private int isub;
        @Nested
        private PsseSubstationSwitchingDevice switchingDevice;

        public int getIsub() {
            return isub;
        }

        public PsseSubstationSwitchingDevice getSwitchingDevice() {
            return switchingDevice;
        }
    }

    public static class PsseSubstationEquipmentTerminalx {

        public PsseSubstationEquipmentTerminalx() {

        }

        public PsseSubstationEquipmentTerminalx(int isub, PsseSubstationEquipmentTerminal equipmentTerminal) {
            this.isub = isub;
            this.equipmentTerminal = equipmentTerminal;
        }

        @Parsed
        private int isub;
        @Nested
        private PsseSubstationEquipmentTerminal equipmentTerminal;

        public int getIsub() {
            return isub;
        }

        public PsseSubstationEquipmentTerminal getEquipmentTerminal() {
            return equipmentTerminal;
        }
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
