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
import static com.powsybl.psse.model.io.Util.getFieldFromMultiplePotentialHeaders;
import static com.powsybl.psse.model.io.Util.parseIntOrNull;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseLineGrouping {

    public PsseLineGrouping() {
    }

    public PsseLineGrouping(int i, int j, String id, int met) {
        this.i = i;
        this.j = j;
        this.id = id;
        this.met = met;
    }

    private int i;
    private int j;
    private String id;
    private int met = 1;
    private Integer dum1;
    private Integer dum2;
    private Integer dum3;
    private Integer dum4;
    private Integer dum5;
    private Integer dum6;
    private Integer dum7;
    private Integer dum8;
    private Integer dum9;

    public static PsseLineGrouping fromRecord(NamedCsvRecord rec, PsseVersion version) {
        PsseLineGrouping psseLineGrouping = new PsseLineGrouping();
        psseLineGrouping.setI(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "i", "ibus")));
        psseLineGrouping.setJ(Integer.parseInt(getFieldFromMultiplePotentialHeaders(rec, "j", "jbus")));
        psseLineGrouping.setId(defaultIfEmpty(getFieldFromMultiplePotentialHeaders(rec, "id", "mslid"), "&1"));
        psseLineGrouping.setMet(Integer.parseInt(rec.getField("met")));
        psseLineGrouping.setDum1(parseIntOrNull(rec.getField("dum1"), "null"));
        psseLineGrouping.setDum2(parseIntOrNull(rec.getField("dum2"), "null"));
        psseLineGrouping.setDum3(parseIntOrNull(rec.getField("dum3"), "null"));
        psseLineGrouping.setDum4(parseIntOrNull(rec.getField("dum4"), "null"));
        psseLineGrouping.setDum5(parseIntOrNull(rec.getField("dum1"), "null"));
        psseLineGrouping.setDum6(parseIntOrNull(rec.getField("dum6"), "null"));
        psseLineGrouping.setDum7(parseIntOrNull(rec.getField("dum7"), "null"));
        psseLineGrouping.setDum8(parseIntOrNull(rec.getField("dum8"), "null"));
        psseLineGrouping.setDum9(parseIntOrNull(rec.getField("dum9"), "null"));
        return psseLineGrouping;
    }

    public static String[] toRecord(PsseLineGrouping psseLineGrouping, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "i", "ibus" -> String.valueOf(psseLineGrouping.getI());
                case "j", "jbus" -> String.valueOf(psseLineGrouping.getJ());
                case "id", "mslid" -> psseLineGrouping.getId();
                case "met" -> String.valueOf(psseLineGrouping.getMet());
                case "dum1" -> String.valueOf(psseLineGrouping.getDum1());
                case "dum2" -> String.valueOf(psseLineGrouping.getDum2());
                case "dum3" -> String.valueOf(psseLineGrouping.getDum3());
                case "dum4" -> String.valueOf(psseLineGrouping.getDum4());
                case "dum5" -> String.valueOf(psseLineGrouping.getDum5());
                case "dum6" -> String.valueOf(psseLineGrouping.getDum6());
                case "dum7" -> String.valueOf(psseLineGrouping.getDum7());
                case "dum8" -> String.valueOf(psseLineGrouping.getDum8());
                case "dum9" -> String.valueOf(psseLineGrouping.getDum9());
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

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMet() {
        return met;
    }

    public void setMet(int met) {
        this.met = met;
    }

    public Integer getDum1() {
        return dum1;
    }

    public void setDum1(Integer dum1) {
        this.dum1 = dum1;
    }

    public Integer getDum2() {
        return dum2;
    }

    public void setDum2(Integer dum2) {
        this.dum2 = dum2;
    }

    public Integer getDum3() {
        return dum3;
    }

    public void setDum3(Integer dum3) {
        this.dum3 = dum3;
    }

    public Integer getDum4() {
        return dum4;
    }

    public void setDum4(Integer dum4) {
        this.dum4 = dum4;
    }

    public Integer getDum5() {
        return dum5;
    }

    public void setDum5(Integer dum5) {
        this.dum5 = dum5;
    }

    public Integer getDum6() {
        return dum6;
    }

    public void setDum6(Integer dum6) {
        this.dum6 = dum6;
    }

    public Integer getDum7() {
        return dum7;
    }

    public void setDum7(Integer dum7) {
        this.dum7 = dum7;
    }

    public Integer getDum8() {
        return dum8;
    }

    public void setDum8(Integer dum8) {
        this.dum8 = dum8;
    }

    public Integer getDum9() {
        return dum9;
    }

    public void setDum9(Integer dum9) {
        this.dum9 = dum9;
    }
}
