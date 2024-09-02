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
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.NullString;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseGenerator extends PsseVersioned {

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed(field = {"id", "machid"}, defaultNullRead = "1")
    private String id;

    @Parsed
    private double pg = 0;

    @Parsed
    private double qg = 0;

    @Parsed
    private double qt = 9999;

    @Parsed
    private double qb = -9999;

    @Parsed
    private double vs = 1;

    @Parsed
    private int ireg = 0;

    @Parsed
    private double mbase = Double.NaN;

    @Parsed
    private double zr = 0;

    @Parsed
    private double zx = 1;

    @Parsed
    private double rt = 0;

    @Parsed
    private double xt = 0;

    @Parsed
    private double gtap = 1;

    @Parsed
    private int stat = 1;

    @Parsed
    private double rmpct = 100;

    @Parsed
    private double pt = 9999;

    @Parsed
    private double pb = -9999;

    @Nested
    private PsseOwnership ownership;

    @NullString(nulls = {"null"})
    @Parsed
    private int wmod = 0;

    @NullString(nulls = {"null"})
    @Parsed
    private double wpf = 1;

    @Parsed
    @Revision(since = 35)
    private int nreg = 0;

    @Parsed
    @Revision(since = 35)
    private int baslod = 0;

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

    public int getNreg() {
        checkVersion("nreg");
        return nreg;
    }

    public void setNreg(int nreg) {
        checkVersion("nreg");
        this.nreg = nreg;
    }

    public int getBaslod() {
        checkVersion("baslod");
        return baslod;
    }

    public void setBaslod(int baslod) {
        checkVersion("baslod");
        this.baslod = baslod;
    }

    public PsseOwnership getOwnership() {
        return ownership;
    }

    public PsseGenerator copy() {
        PsseGenerator copy = new PsseGenerator();
        copy.i = this.i;
        copy.id = this.id;
        copy.pg = this.pg;
        copy.qg = this.qg;
        copy.qt = this.qt;
        copy.qb = this.qb;
        copy.vs = this.vs;
        copy.ireg = this.ireg;
        copy.mbase = this.mbase;
        copy.zr = this.zr;
        copy.zx = this.zx;
        copy.rt = this.rt;
        copy.xt = this.xt;
        copy.gtap = this.gtap;
        copy.stat = this.stat;
        copy.rmpct = this.rmpct;
        copy.pt = this.pt;
        copy.pb = this.pb;
        copy.ownership = this.ownership;
        copy.wmod = this.wmod;
        copy.wpf = this.wpf;
        copy.nreg = this.nreg;
        copy.baslod = this.baslod;
        return copy;
    }
}
