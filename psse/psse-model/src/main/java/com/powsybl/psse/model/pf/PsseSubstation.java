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
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import java.util.ArrayList;
import java.util.List;

import static com.powsybl.psse.model.io.Util.defaultIfEmpty;
import static com.powsybl.psse.model.io.Util.getFieldFromMultiplePotentialHeaders;
import static com.powsybl.psse.model.io.Util.parseDoubleOrDefault;
import static com.powsybl.psse.model.io.Util.parseIntOrDefault;

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
        return substationRecord.is;
    }

    public String getName() {
        return substationRecord.name;
    }

    public double getLati() {
        return substationRecord.lati;
    }

    public double getLong() {
        return substationRecord.longi;
    }

    public double getSrg() {
        return substationRecord.srg;
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

    public static class PsseSubstationRecord {

        private int is;
        private String name;
        private double lati = 0.0;
        private double longi = 0.0;
        private double srg = 0.0;

        public static PsseSubstationRecord fromRecord(NamedCsvRecord rec, PsseVersion version) {
            PsseSubstationRecord psseSubstationRecord = new PsseSubstationRecord();
            psseSubstationRecord.setIs(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "is", "isub")));
            psseSubstationRecord.setName(defaultIfEmpty(rec.getField("name"), "                                        "));
            psseSubstationRecord.setLati(Double.parseDouble(rec.getField("lati")));
            psseSubstationRecord.setLong(Double.parseDouble(rec.getField("long")));
            psseSubstationRecord.setSrg(Double.parseDouble(rec.getField("srg")));
            return psseSubstationRecord;
        }

        public static String[] toRecord(PsseSubstationRecord psseSubstationRecord, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "is" -> String.valueOf(psseSubstationRecord.getIs());
                    case "name" -> psseSubstationRecord.getName();
                    case "lati" -> String.valueOf(psseSubstationRecord.getLati());
                    case "longi" -> String.valueOf(psseSubstationRecord.getLong());
                    case "srg" -> String.valueOf(psseSubstationRecord.getSrg());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

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

        private int ni;
        private String name;
        private int i;
        private int status = 1;
        private double vm = 1.0;
        private double va = 0.0;

        public static PsseSubstationNode fromRecord(NamedCsvRecord rec, PsseVersion version) {
            PsseSubstationNode psseSubstationNode = new PsseSubstationNode();
            psseSubstationNode.setNi(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "ni", "inode")));
            psseSubstationNode.setName(defaultIfEmpty(rec.getField("name"), "                                        "));
            psseSubstationNode.setI(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "i", "ibus")));
            psseSubstationNode.setStatus(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "stat", "status")));
            psseSubstationNode.setVm(parseDoubleOrDefault(rec.getField("vm"), 1.0));
            psseSubstationNode.setVa(parseDoubleOrDefault(rec.getField("va"), 0.0));
            return psseSubstationNode;
        }

        public static String[] toRecord(PsseSubstationNode psseSubstationNode, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "ni", "inode" -> String.valueOf(psseSubstationNode.getNi());
                    case "name" -> psseSubstationNode.getName();
                    case "i", "ibus" -> String.valueOf(psseSubstationNode.getI());
                    case "stat", "status" -> String.valueOf(psseSubstationNode.getStatus());
                    case "vm" -> String.valueOf(psseSubstationNode.getVm());
                    case "va" -> String.valueOf(psseSubstationNode.getVa());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

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

        private int ni;
        private int nj = 0;
        private String ckt;
        private String name;
        private int type = 1;
        private int status = 1;
        private int nstat = 1;
        private double x = 0.0001;
        private double rate1 = 0.0;
        private double rate2 = 0.0;
        private double rate3 = 0.0;

        public static PsseSubstationSwitchingDevice fromRecord(NamedCsvRecord rec, PsseVersion version) {
            PsseSubstationSwitchingDevice psseSubstationSwitchingDevice = new PsseSubstationSwitchingDevice();
            psseSubstationSwitchingDevice.setNi(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "ni", "inode")));
            psseSubstationSwitchingDevice.setNj(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "nj", "jnode")));
            psseSubstationSwitchingDevice.setCkt(defaultIfEmpty(getFieldFromMultiplePotentialHeaders(rec, "ckt", "swdid"), "1 "));
            psseSubstationSwitchingDevice.setName(defaultIfEmpty(rec.getField("name"), "                                        "));
            psseSubstationSwitchingDevice.setType(Integer.parseInt(rec.getField("type")));
            psseSubstationSwitchingDevice.setStatus(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "stat", "status")));
            psseSubstationSwitchingDevice.setNstat(Integer.parseInt(rec.getField("nstat")));
            psseSubstationSwitchingDevice.setX(Double.parseDouble(getFieldFromMultiplePotentialHeaders(rec, "x", "xpu")));
            psseSubstationSwitchingDevice.setRate1(Double.parseDouble(rec.getField("rate1")));
            psseSubstationSwitchingDevice.setRate2(Double.parseDouble(rec.getField("rate2")));
            psseSubstationSwitchingDevice.setRate3(Double.parseDouble(rec.getField("rate3")));
            return psseSubstationSwitchingDevice;
        }

        public static String[] toRecord(PsseSubstationSwitchingDevice psseSubstationSwitchingDevice, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "ni", "inode" -> String.valueOf(psseSubstationSwitchingDevice.getNi());
                    case "nj", "jnode" -> String.valueOf(psseSubstationSwitchingDevice.getNj());
                    case "ckt", "swdid" -> String.valueOf(psseSubstationSwitchingDevice.getCkt());
                    case "name" -> psseSubstationSwitchingDevice.getName();
                    case "type" -> String.valueOf(psseSubstationSwitchingDevice.getType());
                    case "stat", "status" -> String.valueOf(psseSubstationSwitchingDevice.getStatus());
                    case "nstat" -> String.valueOf(psseSubstationSwitchingDevice.getNstat());
                    case "x", "xpu" -> String.valueOf(psseSubstationSwitchingDevice.getX());
                    case "rate1" -> String.valueOf(psseSubstationSwitchingDevice.getRate1());
                    case "rate2" -> String.valueOf(psseSubstationSwitchingDevice.getRate2());
                    case "rate3" -> String.valueOf(psseSubstationSwitchingDevice.getRate3());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

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

        private int i;
        private int ni;
        private String type;
        private String id;
        private int j = 0;
        private int k = 0;

        public static PsseSubstationEquipmentTerminal fromRecord(NamedCsvRecord rec, PsseVersion version) {
            PsseSubstationEquipmentTerminal psseSubstationEquipmentTerminal = new PsseSubstationEquipmentTerminal();
            psseSubstationEquipmentTerminal.setI(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "i", "ibus")));
            psseSubstationEquipmentTerminal.setNi(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "ni", "inode")));
            psseSubstationEquipmentTerminal.setType(rec.getField("type"));
            psseSubstationEquipmentTerminal.setId(defaultIfEmpty(getFieldFromMultiplePotentialHeaders(rec, "id", "eqid"), "1 "));
            psseSubstationEquipmentTerminal.setJ(parseIntOrDefault(getFieldFromMultiplePotentialHeaders(rec, "j", "jbus"), 0));
            psseSubstationEquipmentTerminal.setK(parseIntOrDefault(getFieldFromMultiplePotentialHeaders(rec, "k", "kbus"), 0));
            return psseSubstationEquipmentTerminal;
        }

        public static String[] toRecord(PsseSubstationEquipmentTerminal psseSubstationEquipmentTerminal, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "i", "ibus" -> String.valueOf(psseSubstationEquipmentTerminal.getI());
                    case "ni", "inode" -> String.valueOf(psseSubstationEquipmentTerminal.getNi());
                    case "type" -> psseSubstationEquipmentTerminal.getType();
                    case "id", "eqid" -> psseSubstationEquipmentTerminal.getId();
                    case "j", "jbus" -> String.valueOf(psseSubstationEquipmentTerminal.getJ());
                    case "k", "kbus" -> String.valueOf(psseSubstationEquipmentTerminal.getK());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

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

        private int i;
        private int ni;
        private String type;

        public static PsseSubstationEquipmentTerminalCommonStart fromRecord(NamedCsvRecord rec, PsseVersion version) {
            PsseSubstationEquipmentTerminalCommonStart psseSubstationEquipmentTerminalCommonStart = new PsseSubstationEquipmentTerminalCommonStart();
            psseSubstationEquipmentTerminalCommonStart.setI(Integer.parseInt(rec.getField("i")));
            psseSubstationEquipmentTerminalCommonStart.setNi(Integer.parseInt(rec.getField("ni")));
            psseSubstationEquipmentTerminalCommonStart.setType(rec.getField("type"));
            return psseSubstationEquipmentTerminalCommonStart;
        }

        public static String[] toRecord(PsseSubstationEquipmentTerminalCommonStart psseSubstationEquipmentTerminalCommonStart, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "i" -> String.valueOf(psseSubstationEquipmentTerminalCommonStart.getI());
                    case "ni" -> String.valueOf(psseSubstationEquipmentTerminalCommonStart.getNi());
                    case "type" -> psseSubstationEquipmentTerminalCommonStart.getType();
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

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
    }

    public static class PsseSubstationNodex {

        public PsseSubstationNodex() {
        }

        public PsseSubstationNodex(int isub, PsseSubstationNode node) {
            this.isub = isub;
            this.node = node;
        }

        private int isub;
        private PsseSubstationNode node;

        public static PsseSubstationNodex fromRecord(NamedCsvRecord rec, PsseVersion version) {
            PsseSubstationNodex psseSubstationNodex = new PsseSubstationNodex();
            psseSubstationNodex.setIsub(Integer.parseInt(rec.getField("isub")));
            psseSubstationNodex.setNode(PsseSubstationNode.fromRecord(rec, version));
            return psseSubstationNodex;
        }

        public static String[] toRecord(PsseSubstationNodex psseSubstationNodex, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "isub" -> String.valueOf(psseSubstationNodex.getIsub());
                    case "ni", "inode" -> String.valueOf(psseSubstationNodex.getNode().getNi());
                    case "name" -> psseSubstationNodex.getNode().getName();
                    case "i", "ibus" -> String.valueOf(psseSubstationNodex.getNode().getI());
                    case "stat", "status" -> String.valueOf(psseSubstationNodex.getNode().getStatus());
                    case "vm" -> String.valueOf(psseSubstationNodex.getNode().getVm());
                    case "va" -> String.valueOf(psseSubstationNodex.getNode().getVa());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

        public int getIsub() {
            return isub;
        }

        public void setIsub(int isub) {
            this.isub = isub;
        }

        public PsseSubstationNode getNode() {
            return node;
        }

        public void setNode(PsseSubstationNode node) {
            this.node = node;
        }
    }

    public static class PsseSubstationSwitchingDevicex {

        public PsseSubstationSwitchingDevicex() {

        }

        public PsseSubstationSwitchingDevicex(int isub, PsseSubstationSwitchingDevice switchingDevice) {
            this.isub = isub;
            this.switchingDevice = switchingDevice;
        }

        private int isub;
        private PsseSubstationSwitchingDevice switchingDevice;

        public static PsseSubstationSwitchingDevicex fromRecord(NamedCsvRecord rec, PsseVersion version) {
            PsseSubstationSwitchingDevicex psseSubstationSwitchingDevicex = new PsseSubstationSwitchingDevicex();
            psseSubstationSwitchingDevicex.setIsub(Integer.parseInt(rec.getField("isub")));
            psseSubstationSwitchingDevicex.setSwitchingDevice(PsseSubstationSwitchingDevice.fromRecord(rec, version));
            return psseSubstationSwitchingDevicex;
        }

        public static String[] toRecord(PsseSubstationSwitchingDevicex psseSubstationSwitchingDevicex, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "isub" -> String.valueOf(psseSubstationSwitchingDevicex.getIsub());
                    case "ni", "inode" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getNi());
                    case "nj", "jnode" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getNj());
                    case "ckt", "swdid" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getCkt());
                    case "name" -> psseSubstationSwitchingDevicex.getSwitchingDevice().getName();
                    case "type" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getType());
                    case "stat", "status" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getStatus());
                    case "nstat" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getNstat());
                    case "x", "xpu" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getX());
                    case "rate1" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getRate1());
                    case "rate2" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getRate2());
                    case "rate3" -> String.valueOf(psseSubstationSwitchingDevicex.getSwitchingDevice().getRate3());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

        public int getIsub() {
            return isub;
        }

        public void setIsub(int isub) {
            this.isub = isub;
        }

        public PsseSubstationSwitchingDevice getSwitchingDevice() {
            return switchingDevice;
        }

        public void setSwitchingDevice(PsseSubstationSwitchingDevice switchingDevice) {
            this.switchingDevice = switchingDevice;
        }
    }

    public static class PsseSubstationEquipmentTerminalx {

        public PsseSubstationEquipmentTerminalx() {

        }

        public PsseSubstationEquipmentTerminalx(int isub, PsseSubstationEquipmentTerminal equipmentTerminal) {
            this.isub = isub;
            this.equipmentTerminal = equipmentTerminal;
        }

        private int isub;
        private PsseSubstationEquipmentTerminal equipmentTerminal;

        public static PsseSubstationEquipmentTerminalx fromRecord(NamedCsvRecord rec, PsseVersion version) {
            PsseSubstationEquipmentTerminalx psseSubstationEquipmentTerminalx = new PsseSubstationEquipmentTerminalx();
            psseSubstationEquipmentTerminalx.setIsub(Integer.parseInt(rec.getField("isub")));
            psseSubstationEquipmentTerminalx.setEquipmentTerminal(PsseSubstationEquipmentTerminal.fromRecord(rec, version));
            return psseSubstationEquipmentTerminalx;
        }

        public static String[] toRecord(PsseSubstationEquipmentTerminalx psseSubstationEquipmentTerminalx, String[] headers) {
            String[] row = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                row[i] = switch (headers[i]) {
                    case "isub" -> String.valueOf(psseSubstationEquipmentTerminalx.getIsub());
                    case "i", "ibus" -> String.valueOf(psseSubstationEquipmentTerminalx.getEquipmentTerminal().getI());
                    case "ni", "inode" -> String.valueOf(psseSubstationEquipmentTerminalx.getEquipmentTerminal().getNi());
                    case "type" -> psseSubstationEquipmentTerminalx.getEquipmentTerminal().getType();
                    case "id", "eqid" -> psseSubstationEquipmentTerminalx.getEquipmentTerminal().getId();
                    case "j", "jbus" -> String.valueOf(psseSubstationEquipmentTerminalx.getEquipmentTerminal().getJ());
                    case "k", "kbus" -> String.valueOf(psseSubstationEquipmentTerminalx.getEquipmentTerminal().getK());
                    default -> throw new PsseException("Unsupported header: " + headers[i]);
                };
            }
            return row;
        }

        public int getIsub() {
            return isub;
        }

        public void setIsub(int isub) {
            this.isub = isub;
        }

        public PsseSubstationEquipmentTerminal getEquipmentTerminal() {
            return equipmentTerminal;
        }

        public void setEquipmentTerminal(PsseSubstationEquipmentTerminal equipmentTerminal) {
            this.equipmentTerminal = equipmentTerminal;
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
