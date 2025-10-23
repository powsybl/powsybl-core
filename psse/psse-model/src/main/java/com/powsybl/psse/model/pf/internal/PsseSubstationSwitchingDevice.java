/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.internal;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class PsseSubstationSwitchingDevice {

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

    public static PsseSubstationSwitchingDevice fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseSubstationSwitchingDevice psseSubstationSwitchingDevice = new PsseSubstationSwitchingDevice();
        psseSubstationSwitchingDevice.setNi(parseIntFromRecord(rec, headers, "ni", "inode"));
        psseSubstationSwitchingDevice.setNj(parseIntFromRecord(rec, 0, headers, "nj", "jnode"));
        psseSubstationSwitchingDevice.setCkt(parseStringFromRecord(rec, "1 ", headers, "ckt", "swdid"));
        psseSubstationSwitchingDevice.setName(parseStringFromRecord(rec, "                                        ", headers, "name"));
        psseSubstationSwitchingDevice.setType(parseIntFromRecord(rec, 1, headers, "type"));
        psseSubstationSwitchingDevice.setStatus(parseIntFromRecord(rec, 1, headers, "stat", "status"));
        psseSubstationSwitchingDevice.setNstat(parseIntFromRecord(rec, 1, headers, "nstat"));
        psseSubstationSwitchingDevice.setX(parseDoubleFromRecord(rec, 0.0001, headers, "x", "xpu"));
        psseSubstationSwitchingDevice.setRate1(parseDoubleFromRecord(rec, 0.0, headers, "rate1"));
        psseSubstationSwitchingDevice.setRate2(parseDoubleFromRecord(rec, 0.0, headers, "rate2"));
        psseSubstationSwitchingDevice.setRate3(parseDoubleFromRecord(rec, 0.0, headers, "rate3"));
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
