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
public class PsseMultiTerminalDcLink {

    private int idc;
    private int jdc;
    private String dcckt;
    private int met = 1;
    private double rdc;
    private double ldc = 0.0;

    public static PsseMultiTerminalDcLink fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseMultiTerminalDcLink psseMultiTerminalDcLink = new PsseMultiTerminalDcLink();
        psseMultiTerminalDcLink.setIdc(parseIntFromRecord(rec, headers, "idc"));
        psseMultiTerminalDcLink.setJdc(parseIntFromRecord(rec, headers, "jdc"));
        psseMultiTerminalDcLink.setDcckt(parseStringFromRecord(rec, "1", headers, "dcckt"));
        psseMultiTerminalDcLink.setMet(parseIntFromRecord(rec, headers, "met"));
        psseMultiTerminalDcLink.setRdc(parseDoubleFromRecord(rec, headers, "rdc"));
        psseMultiTerminalDcLink.setLdc(parseDoubleFromRecord(rec, headers, "ldc"));
        return psseMultiTerminalDcLink;
    }

    public static String[] toRecord(PsseMultiTerminalDcLink psseMultiTerminalDcLink, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "idc" -> String.valueOf(psseMultiTerminalDcLink.getIdc());
                case "jdc" -> String.valueOf(psseMultiTerminalDcLink.getJdc());
                case "dcckt" -> String.valueOf(psseMultiTerminalDcLink.getDcckt());
                case "met" -> String.valueOf(psseMultiTerminalDcLink.getMet());
                case "rdc" -> String.valueOf(psseMultiTerminalDcLink.getRdc());
                case "ldc" -> String.valueOf(psseMultiTerminalDcLink.getLdc());
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

    public int getJdc() {
        return jdc;
    }

    public void setJdc(int jdc) {
        this.jdc = jdc;
    }

    public String getDcckt() {
        return dcckt;
    }

    public void setDcckt(String dcckt) {
        this.dcckt = dcckt;
    }

    public int getMet() {
        return met;
    }

    public void setMet(int met) {
        this.met = met;
    }

    public double getRdc() {
        return rdc;
    }

    public void setRdc(double rdc) {
        this.rdc = rdc;
    }

    public double getLdc() {
        return ldc;
    }

    public void setLdc(double ldc) {
        this.ldc = ldc;
    }
}
