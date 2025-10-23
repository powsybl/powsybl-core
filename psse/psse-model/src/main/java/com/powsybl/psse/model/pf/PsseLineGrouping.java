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
import de.siegmar.fastcsv.reader.CsvRecord;

import static com.powsybl.psse.model.io.Util.parseIntFromRecord;
import static com.powsybl.psse.model.io.Util.parseStringFromRecord;

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

    public static PsseLineGrouping fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        PsseLineGrouping psseLineGrouping = new PsseLineGrouping();
        psseLineGrouping.setI(parseIntFromRecord(rec, headers, "i", "ibus"));
        psseLineGrouping.setJ(parseIntFromRecord(rec, headers, "j", "jbus"));
        psseLineGrouping.setId(parseStringFromRecord(rec, "&1", headers, "id", "mslid"));
        psseLineGrouping.setMet(parseIntFromRecord(rec, headers, "met"));
        psseLineGrouping.setDum1(parseIntFromRecord(rec, null, headers, "dum1"));
        psseLineGrouping.setDum2(parseIntFromRecord(rec, null, headers, "dum2"));
        psseLineGrouping.setDum3(parseIntFromRecord(rec, null, headers, "dum3"));
        psseLineGrouping.setDum4(parseIntFromRecord(rec, null, headers, "dum4"));
        psseLineGrouping.setDum5(parseIntFromRecord(rec, null, headers, "dum5"));
        psseLineGrouping.setDum6(parseIntFromRecord(rec, null, headers, "dum6"));
        psseLineGrouping.setDum7(parseIntFromRecord(rec, null, headers, "dum7"));
        psseLineGrouping.setDum8(parseIntFromRecord(rec, null, headers, "dum8"));
        psseLineGrouping.setDum9(parseIntFromRecord(rec, null, headers, "dum9"));
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
