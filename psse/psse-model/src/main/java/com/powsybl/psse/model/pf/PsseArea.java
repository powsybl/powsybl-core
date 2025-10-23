/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseArea {

    private int i;
    private int isw = 0;
    private double pdes = 0;
    private double ptol = 10;
    private String arname;

    public static PsseArea fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseArea psseArea = new PsseArea();
        psseArea.setI(parseIntFromRecord(rec, headers, "i", "iarea"));
        psseArea.setIsw(parseIntFromRecord(rec, 0, headers, "isw"));
        psseArea.setPdes(parseDoubleFromRecord(rec, 0d, headers, "pdes"));
        psseArea.setPtol(parseDoubleFromRecord(rec, 10d, headers, "ptol"));
        psseArea.setArname(parseStringFromRecord(rec, "            ", headers, "arname"));
        return psseArea;
    }

    public static String[] toRecord(PsseArea psseArea, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i", "iarea" -> String.valueOf(psseArea.getI());
                case "arname" -> "            ".equals(psseArea.getArname()) ? "" : psseArea.getArname();
                case "isw" -> String.valueOf(psseArea.getIsw());
                case "pdes" -> String.valueOf(psseArea.getPdes());
                case "ptol" -> String.valueOf(psseArea.getPtol());
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

    public int getIsw() {
        return isw;
    }

    public void setIsw(int isw) {
        this.isw = isw;
    }

    public double getPdes() {
        return pdes;
    }

    public void setPdes(double pdes) {
        this.pdes = pdes;
    }

    public double getPtol() {
        return ptol;
    }

    public void setPtol(double ptol) {
        this.ptol = ptol;
    }

    public String getArname() {
        return arname;
    }

    public void setArname(String arname) {
        this.arname = arname;
    }
}
