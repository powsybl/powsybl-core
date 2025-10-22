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
public class PsseMultiTerminalDcMain {

    private String name;
    private int nconv;
    private int ndcbs;
    private int ndcln;
    private int mdc = 0;
    private int vconv;
    private double vcmod = 0.0;
    private int vconvn = 0;

    public static PsseMultiTerminalDcMain fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseMultiTerminalDcMain psseMultiTerminalDcMain = new PsseMultiTerminalDcMain();
        psseMultiTerminalDcMain.setName(parseStringFromRecord(rec, headers, "name"));
        psseMultiTerminalDcMain.setNconv(parseIntFromRecord(rec, headers, "nconv"));
        psseMultiTerminalDcMain.setNdcbs(parseIntFromRecord(rec, headers, "ndcbs"));
        psseMultiTerminalDcMain.setNdcln(parseIntFromRecord(rec, headers, "ndcln"));
        psseMultiTerminalDcMain.setMdc(parseIntFromRecord(rec, 0, headers, "mdc"));
        psseMultiTerminalDcMain.setVconv(parseIntFromRecord(rec, headers, "vconv"));
        psseMultiTerminalDcMain.setVcmod(parseDoubleFromRecord(rec, 0d, headers, "vcmod"));
        psseMultiTerminalDcMain.setVconvn(parseIntFromRecord(rec, 0, headers, "vconvn"));
        return psseMultiTerminalDcMain;
    }

    public static String[] toRecord(PsseMultiTerminalDcMain psseMultiTerminalDcMain, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "name" -> psseMultiTerminalDcMain.getName();
                case "nconv" -> String.valueOf(psseMultiTerminalDcMain.getNconv());
                case "ndcbs" -> String.valueOf(psseMultiTerminalDcMain.getNdcbs());
                case "ndcln" -> String.valueOf(psseMultiTerminalDcMain.getNdcln());
                case "mdc" -> String.valueOf(psseMultiTerminalDcMain.getMdc());
                case "vconv" -> String.valueOf(psseMultiTerminalDcMain.getVconv());
                case "vcmod" -> String.valueOf(psseMultiTerminalDcMain.getVcmod());
                case "vconvn" -> String.valueOf(psseMultiTerminalDcMain.getVconvn());
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
    }

    public String getName() {
        return name;
    }

    public int getNconv() {
        return nconv;
    }

    public int getNdcbs() {
        return ndcbs;
    }

    public int getNdcln() {
        return ndcln;
    }

    public int getMdc() {
        return mdc;
    }

    public int getVconv() {
        return vconv;
    }

    public double getVcmod() {
        return vcmod;
    }

    public int getVconvn() {
        return vconvn;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNconv(int nconv) {
        this.nconv = nconv;
    }

    public void setNdcbs(int ndcbs) {
        this.ndcbs = ndcbs;
    }

    public void setNdcln(int ndcln) {
        this.ndcln = ndcln;
    }

    public void setMdc(int mdc) {
        this.mdc = mdc;
    }

    public void setVconv(int vconv) {
        this.vconv = vconv;
    }

    public void setVcmod(double vcmod) {
        this.vcmod = vcmod;
    }

    public void setVconvn(int vconvn) {
        this.vconvn = vconvn;
    }
}
