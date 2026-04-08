/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.univocity.parsers.annotations.NullString;
import com.univocity.parsers.annotations.Parsed;

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

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed(field = {"j", "jbus"})
    private int j;

    @Parsed(field = {"id", "mslid"}, defaultNullRead = "&1")
    private String id;

    @Parsed
    private int met = 1;

    @NullString(nulls = {"null"})
    @Parsed
    private Integer dum1;

    @NullString(nulls = {"null"})
    @Parsed
    private Integer dum2;

    @NullString(nulls = {"null"})
    @Parsed
    private Integer dum3;

    @NullString(nulls = {"null"})
    @Parsed
    private Integer dum4;

    @NullString(nulls = {"null"})
    @Parsed
    private Integer dum5;

    @NullString(nulls = {"null"})
    @Parsed
    private Integer dum6;

    @NullString(nulls = {"null"})
    @Parsed
    private Integer dum7;

    @NullString(nulls = {"null"})
    @Parsed
    private Integer dum8;

    @NullString(nulls = {"null"})
    @Parsed
    private Integer dum9;

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
