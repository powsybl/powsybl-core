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
public class PsseMultiTerminalDcBus {

    private int idc;
    private int ib = 0;
    private int area = 1;
    private int zone = 1;
    private String dcname;
    private int idc2 = 0;
    private double rgrnd = 0.0;
    private int owner = 1;

    public static PsseMultiTerminalDcBus fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseMultiTerminalDcBus psseMultiTerminalDcBus = new PsseMultiTerminalDcBus();
        psseMultiTerminalDcBus.setIdc(parseIntFromRecord(rec, headers, "idc"));
        psseMultiTerminalDcBus.setIb(parseIntFromRecord(rec, headers, "ib"));
        psseMultiTerminalDcBus.setArea(parseIntFromRecord(rec, headers, "area"));
        psseMultiTerminalDcBus.setZone(parseIntFromRecord(rec, headers, "zone"));
        psseMultiTerminalDcBus.setDcname(parseStringFromRecord(rec, "            ", headers, "dcname"));
        psseMultiTerminalDcBus.setIdc2(parseIntFromRecord(rec, headers, "idc2"));
        psseMultiTerminalDcBus.setRgrnd(parseDoubleFromRecord(rec, headers, "rgrnd"));
        psseMultiTerminalDcBus.setOwner(parseIntFromRecord(rec, headers, "owner"));
        return psseMultiTerminalDcBus;
    }

    public static String[] toRecord(PsseMultiTerminalDcBus psseMultiTerminalDcBus, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "idc" -> String.valueOf(psseMultiTerminalDcBus.getIdc());
                case "ib" -> String.valueOf(psseMultiTerminalDcBus.getIb());
                case "area" -> String.valueOf(psseMultiTerminalDcBus.getArea());
                case "zone" -> String.valueOf(psseMultiTerminalDcBus.getZone());
                case "dcname" -> String.valueOf(psseMultiTerminalDcBus.getDcname());
                case "idc2" -> String.valueOf(psseMultiTerminalDcBus.getIdc2());
                case "rgrnd" -> String.valueOf(psseMultiTerminalDcBus.getRgrnd());
                case "owner" -> String.valueOf(psseMultiTerminalDcBus.getOwner());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
    }

    public int getIdc() {
        return idc;
    }

    public void setIdc(int idc) {
        this.idc = idc;
    }

    public int getIb() {
        return ib;
    }

    public void setIb(int ib) {
        this.ib = ib;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }

    public String getDcname() {
        return dcname;
    }

    public void setDcname(String dcname) {
        this.dcname = dcname;
    }

    public int getIdc2() {
        return idc2;
    }

    public void setIdc2(int idc2) {
        this.idc2 = idc2;
    }

    public double getRgrnd() {
        return rgrnd;
    }

    public void setRgrnd(double rgrnd) {
        this.rgrnd = rgrnd;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }
}
