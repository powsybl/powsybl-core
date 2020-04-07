/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.annotations.Validate;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseNonTransformerBranch {

    @Parsed(index = 0)
    @Validate
    private int i;

    @Parsed(index = 1)
    @Validate
    private int j;

    @Parsed(index = 2)
    private String ckt = "1";

    @Parsed(index = 3)
    @Validate
    private double r;

    @Parsed(index = 4)
    @Validate
    private double x;

    @Parsed(index = 5)
    private double b = 0;

    @Parsed(index = 6)
    private double ratea = 0;

    @Parsed(index = 7)
    private double rateb = 0;

    @Parsed(index = 8)
    private double ratec = 0;

    @Parsed(index = 9)
    private double gi = 0;

    @Parsed(index = 10)
    private double bi = 0;

    @Parsed(index = 11)
    private double gj = 0;

    @Parsed(index = 12)
    private double bj = 0;

    @Parsed(index = 13)
    private int st = 1;

    @Parsed(index = 14)
    private int met = 1;

    @Parsed(index = 15)
    private double len = 0;

    @Parsed(index = 16)
    private int o1 = -1;

    @Parsed(index = 17)
    private double f1 = 1;

    @Parsed(index = 18)
    private int o2 = 0;

    @Parsed(index = 19)
    private double f2 = 1;

    @Parsed(index = 20)
    private int o3 = 0;

    @Parsed(index = 21)
    private double f3 = 1;

    @Parsed(index = 22)
    private int o4 = 0;

    @Parsed(index = 23)
    private double f4 = 1;

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
        return ratea;
    }

    public void setRatea(double ratea) {
        this.ratea = ratea;
    }

    public double getRateb() {
        return rateb;
    }

    public void setRateb(double rateb) {
        this.rateb = rateb;
    }

    public double getRatec() {
        return ratec;
    }

    public void setRatec(double ratec) {
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
}
