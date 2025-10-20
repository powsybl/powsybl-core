/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import static com.powsybl.psse.model.io.Util.defaultIfEmpty;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseInterareaTransfer {

    private int arfrom;
    private int arto;
    private String trid;
    private double ptran = 0.0;

    public static PsseInterareaTransfer fromRecord(NamedCsvRecord rec, PsseVersion version) {
        PsseInterareaTransfer psseInterareaTransfer = new PsseInterareaTransfer();
        psseInterareaTransfer.setArfrom(Integer.parseInt(rec.getField("arfrom")));
        psseInterareaTransfer.setArto(Integer.parseInt(rec.getField("arto")));
        psseInterareaTransfer.setTrid(defaultIfEmpty(rec.getField("trid"), "1"));
        psseInterareaTransfer.setPtran(Double.parseDouble(rec.getField("ptran")));
        return psseInterareaTransfer;
    }

    public static String[] toRecord(PsseInterareaTransfer psseInterareaTransfer, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i];
            row[i] = switch (h) {
                case "arfrom" -> String.valueOf(psseInterareaTransfer.getArfrom());
                case "arto" -> String.valueOf(psseInterareaTransfer.getArto());
                case "trid" -> psseInterareaTransfer.getTrid();
                case "ptran" -> String.valueOf(psseInterareaTransfer.getPtran());
                default -> throw new PsseException("Unsupported header: " + h);
            };
        }
        return row;
    }

    public int getArfrom() {
        return arfrom;
    }

    public void setArfrom(int arfrom) {
        this.arfrom = arfrom;
    }

    public int getArto() {
        return arto;
    }

    public void setArto(int arto) {
        this.arto = arto;
    }

    public String getTrid() {
        return trid;
    }

    public void setTrid(String trid) {
        this.trid = trid;
    }

    public double getPtran() {
        return ptran;
    }

    public void setPtran(double ptran) {
        this.ptran = ptran;
    }
}
