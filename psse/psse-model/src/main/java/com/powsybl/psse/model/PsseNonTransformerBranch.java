/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Validate;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@JsonFilter("PsseVersionFilter")
public class PsseNonTransformerBranch extends PsseVersioned {

    @Parsed(field = {"i", "ibus"})
    @Validate
    private int i;

    @Parsed(field = {"j", "jbus"})
    @Validate
    private int j;

    @Parsed(defaultNullRead = "1")
    private String ckt;

    @Parsed(field = {"r", "rpu"})
    private double r = 0.0;

    @Parsed(field = {"x", "xpu"})
    @Validate
    private double x;

    @Parsed(field = {"b", "bpu"})
    private double b = 0;

    @Parsed
    @Revision(until = 33)
    private double ratea = 0;

    @Parsed
    @Revision(until = 33)
    private double rateb = 0;

    @Parsed
    @Revision(until = 33)
    private double ratec = 0;

    @Parsed
    private double gi = 0;

    @Parsed
    private double bi = 0;

    @Parsed
    private double gj = 0;

    @Parsed
    private double bj = 0;

    @Parsed(field = {"st", "stat"})
    private int st = 1;

    @Parsed
    private int met = 1;

    @Parsed
    private double len = 0;

    @Parsed
    private int o1 = -1;

    @Parsed
    private double f1 = 1;

    @Parsed
    private int o2 = 0;

    @Parsed
    private double f2 = 1;

    @Parsed
    private int o3 = 0;

    @Parsed
    private double f3 = 1;

    @Parsed
    private int o4 = 0;

    @Parsed
    private double f4 = 1;

    @Parsed(defaultNullRead = " ")
    @Revision(since = 35)
    private String name;

    @Parsed
    @Revision(since = 35)
    private double rate1 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate2 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate3 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate4 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate5 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate6 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate7 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate8 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate9 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate10 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate11 = 0;

    @Parsed
    @Revision(since = 35)
    private double rate12 = 0;

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

    public String getCkt() {
        return ckt;
    }

    public void setCkt(String ckt) {
        this.ckt = ckt;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

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

    public double getGi() {
        return gi;
    }

    public void setGi(double gi) {
        this.gi = gi;
    }

    public double getBi() {
        return bi;
    }

    public void setBi(double bi) {
        this.bi = bi;
    }

    public double getGj() {
        return gj;
    }

    public void setGj(double gj) {
        this.gj = gj;
    }

    public double getBj() {
        return bj;
    }

    public void setBj(double bj) {
        this.bj = bj;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    public int getMet() {
        return met;
    }

    public void setMet(int met) {
        this.met = met;
    }

    public double getLen() {
        return len;
    }

    public void setLen(double len) {
        this.len = len;
    }

    public int getO1() {
        return o1;
    }

    public void setO1(int o1) {
        this.o1 = o1;
    }

    public double getF1() {
        return f1;
    }

    public void setF1(double f1) {
        this.f1 = f1;
    }

    public int getO2() {
        return o2;
    }

    public void setO2(int o2) {
        this.o2 = o2;
    }

    public double getF2() {
        return f2;
    }

    public void setF2(double f2) {
        this.f2 = f2;
    }

    public int getO3() {
        return o3;
    }

    public void setO3(int o3) {
        this.o3 = o3;
    }

    public double getF3() {
        return f3;
    }

    public void setF3(double f3) {
        this.f3 = f3;
    }

    public int getO4() {
        return o4;
    }

    public void setO4(int o4) {
        this.o4 = o4;
    }

    public double getF4() {
        return f4;
    }

    public void setF4(double f4) {
        this.f4 = f4;
    }

    public String getName() {
        checkVersion("name");
        return name;
    }

    public void setName(String name) {
        checkVersion("name");
        this.name = Objects.requireNonNull(name);
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
}
