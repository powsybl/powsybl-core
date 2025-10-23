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
public class PsseSubstationRecord {

    private int is;
    private String name;
    private double lati = 0.0;
    private double longi = 0.0;
    private double srg = 0.0;

    public static PsseSubstationRecord fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseSubstationRecord psseSubstationRecord = new PsseSubstationRecord();
        psseSubstationRecord.setIs(parseIntFromRecord(rec, headers, "is", "isub"));
        psseSubstationRecord.setName(parseStringFromRecord(rec, "                                        ", headers, "name"));
        psseSubstationRecord.setLati(parseDoubleFromRecord(rec, 0.0, headers, "lati"));
        psseSubstationRecord.setLong(parseDoubleFromRecord(rec, 0.0, headers, "long"));
        psseSubstationRecord.setSrg(parseDoubleFromRecord(rec, 0.0, headers, "srg"));
        return psseSubstationRecord;
    }

    public static String[] toRecord(PsseSubstationRecord psseSubstationRecord, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "is", "isub" -> String.valueOf(psseSubstationRecord.getIs());
                case "name" -> psseSubstationRecord.getName();
                case "lati" -> String.valueOf(psseSubstationRecord.getLati());
                case "long" -> String.valueOf(psseSubstationRecord.getLong());
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
