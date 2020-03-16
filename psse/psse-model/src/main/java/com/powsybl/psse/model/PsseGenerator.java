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
public class PsseGenerator {

    @Parsed(index = 0)
    @Validate
    private int i;

    @Parsed(index = 1)
    private String id = "1";

    @Parsed(index = 2)
    private double pg = 0;

    @Parsed(index = 3)
    private double qg = 0;

    @Parsed(index = 4)
    private double qt = 9999;

    @Parsed(index = 5)
    private double qb = -9999;

    @Parsed(index = 6)
    private double vs = 1;

    @Parsed(index = 7)
    private int ireg = 0;

    @Parsed(index = 8)
    private double mbase = Double.NaN;

    @Parsed(index = 9)
    private double zr = 0;

    @Parsed(index = 10)
    private double zx = 1;

    @Parsed(index = 11)
    private double rt = 0;

    @Parsed(index = 12)
    private double xt = 0;

    @Parsed(index = 13)
    private double gtap = 1;

    @Parsed(index = 14)
    private int stat = 1;

    @Parsed(index = 15)
    private double rmpct = 100;

    @Parsed(index = 16)
    private double pt = 9999;

    @Parsed(index = 17)
    private double pb = -9999;

    @Parsed(index = 18)
    private int o1 = -1;

    @Parsed(index = 19)
    private double f1 = 1;

    @Parsed(index = 20)
    private int o2 = 0;

    @Parsed(index = 21)
    private double f2 = 1;

    @Parsed(index = 22)
    private int o3 = 0;

    @Parsed(index = 23)
    private double f3 = 1;

    @Parsed(index = 24)
    private int o4 = 0;

    @Parsed(index = 25)
    private double f4 = 1;

    @Parsed(index = 26)
    private int wmod = 0;

    @Parsed(index = 27)
    private double wpf = 1;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPg() {
        return pg;
    }

    public void setPg(double pg) {
        this.pg = pg;
    }

    public double getQg() {
        return qg;
    }

    public void setQg(double qg) {
        this.qg = qg;
    }

    public double getQt() {
        return qt;
    }

    public void setQt(double qt) {
        this.qt = qt;
    }

    public double getQb() {
        return qb;
    }

    public void setQb(double qb) {
        this.qb = qb;
    }

    public double getVs() {
        return vs;
    }

    public void setVs(double vs) {
        this.vs = vs;
    }

    public int getIreg() {
        return ireg;
    }

    public void setIreg(int ireg) {
        this.ireg = ireg;
    }

    public double getMbase() {
        return mbase;
    }

    public void setMbase(double mbase) {
        this.mbase = mbase;
    }

    public double getZr() {
        return zr;
    }

    public void setZr(double zr) {
        this.zr = zr;
    }

    public double getZx() {
        return zx;
    }

    public void setZx(double zx) {
        this.zx = zx;
    }

    public double getRt() {
        return rt;
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public double getXt() {
        return xt;
    }

    public void setXt(double xt) {
        this.xt = xt;
    }

    public double getGtap() {
        return gtap;
    }

    public void setGtap(double gtap) {
        this.gtap = gtap;
    }

    public int getStat() {
        return stat;
    }

    public void setStat(int stat) {
        this.stat = stat;
    }

    public double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public double getPt() {
        return pt;
    }

    public void setPt(double pt) {
        this.pt = pt;
    }

    public double getPb() {
        return pb;
    }

    public void setPb(double pb) {
        this.pb = pb;
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

    public int getWmod() {
        return wmod;
    }

    public void setWmod(int wmod) {
        this.wmod = wmod;
    }

    public double getWpf() {
        return wpf;
    }

    public void setWpf(double wpf) {
        this.wpf = wpf;
    }
}
