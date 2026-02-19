/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.univocity.parsers.annotations.NullString;
import com.univocity.parsers.annotations.Parsed;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseRates extends PsseVersioned {

    @Parsed(field = {"ratea", "rata"})
    @Revision(until = 33)
    private double ratea = 0;

    @Parsed(field = {"rateb", "ratb"})
    @Revision(until = 33)
    private double rateb = 0;

    @Parsed(field = {"ratec", "ratc"})
    @Revision(until = 33)
    private double ratec = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate1", "wdgrate1"})
    @Revision(since = 35)
    private double rate1 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate2", "wdgrate2"})
    @Revision(since = 35)
    private double rate2 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate3", "wdgrate3"})
    @Revision(since = 35)
    private double rate3 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate4", "wdgrate4"})
    @Revision(since = 35)
    private double rate4 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate5", "wdgrate5"})
    @Revision(since = 35)
    private double rate5 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate6", "wdgrate6"})
    @Revision(since = 35)
    private double rate6 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate7", "wdgrate7"})
    @Revision(since = 35)
    private double rate7 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate8", "wdgrate8"})
    @Revision(since = 35)
    private double rate8 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate9", "wdgrate9"})
    @Revision(since = 35)
    private double rate9 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate10", "wdgrate10"})
    @Revision(since = 35)
    private double rate10 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate11", "wdgrate11"})
    @Revision(since = 35)
    private double rate11 = 0;

    @NullString(nulls = {"null"})
    @Parsed(field = {"rate12", "wdgrate12"})
    @Revision(since = 35)
    private double rate12 = 0;

    public double getRatea() {
        checkVersion("ratea");
        return ratea;
    }

    public void setRatea(double ratea) {
        checkVersion("ratea");
        this.ratea = ratea;
    }

    public double getRateb() {
        checkVersion("rateb");
        return rateb;
    }

    public void setRateb(double rateb) {
        checkVersion("rateb");
        this.rateb = rateb;
    }

    public double getRatec() {
        checkVersion("ratec");
        return ratec;
    }

    public void setRatec(double ratec) {
        checkVersion("ratec");
        this.ratec = ratec;
    }

    public double getRate1() {
        checkVersion("rate1");
        return rate1;
    }

    public void setRate1(double rate1) {
        checkVersion("rate1");
        this.rate1 = rate1;
    }

    public double getRate2() {
        checkVersion("rate2");
        return rate2;
    }

    public void setRate2(double rate2) {
        checkVersion("rate2");
        this.rate2 = rate2;
    }

    public double getRate3() {
        checkVersion("rate3");
        return rate3;
    }

    public void setRate3(double rate3) {
        checkVersion("rate3");
        this.rate3 = rate3;
    }

    public double getRate4() {
        checkVersion("rate4");
        return rate4;
    }

    public void setRate4(double rate4) {
        checkVersion("rate4");
        this.rate4 = rate4;
    }

    public double getRate5() {
        checkVersion("rate5");
        return rate5;
    }

    public void setRate5(double rate5) {
        checkVersion("rate5");
        this.rate5 = rate5;
    }

    public double getRate6() {
        checkVersion("rate6");
        return rate6;
    }

    public void setRate6(double rate6) {
        checkVersion("rate6");
        this.rate6 = rate6;
    }

    public double getRate7() {
        checkVersion("rate7");
        return rate7;
    }

    public void setRate7(double rate7) {
        checkVersion("rate7");
        this.rate7 = rate7;
    }

    public double getRate8() {
        checkVersion("rate8");
        return rate8;
    }

    public void setRate8(double rate8) {
        checkVersion("rate8");
        this.rate8 = rate8;
    }

    public double getRate9() {
        checkVersion("rate9");
        return rate9;
    }

    public void setRate9(double rate9) {
        checkVersion("rate9");
        this.rate9 = rate9;
    }

    public double getRate10() {
        checkVersion("rate10");
        return rate10;
    }

    public void setRate10(double rate10) {
        checkVersion("rate10");
        this.rate10 = rate10;
    }

    public double getRate11() {
        checkVersion("rate11");
        return rate11;
    }

    public void setRate11(double rate11) {
        checkVersion("rate11");
        this.rate11 = rate11;
    }

    public double getRate12() {
        checkVersion("rate12");
        return rate12;
    }

    public void setRate12(double rate12) {
        checkVersion("rate12");
        this.rate12 = rate12;
    }

    public PsseRates copy() {
        PsseRates copy = new PsseRates();
        copy.ratea = this.ratea;
        copy.rateb = this.rateb;
        copy.ratec = this.ratec;
        copy.rate1 = this.rate1;
        copy.rate2 = this.rate2;
        copy.rate3 = this.rate3;
        copy.rate4 = this.rate4;
        copy.rate5 = this.rate5;
        copy.rate6 = this.rate6;
        copy.rate7 = this.rate7;
        copy.rate8 = this.rate8;
        copy.rate9 = this.rate9;
        copy.rate10 = this.rate10;
        copy.rate11 = this.rate11;
        copy.rate12 = this.rate12;
        return copy;
    }
}
