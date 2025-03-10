/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseFacts extends PsseVersioned {

    @Parsed(defaultNullRead = "")
    private String name;

    @Parsed(field = {"i", "ibus"})
    private int i;

    @Parsed(field = {"j", "jbus"})
    private int j = 0;

    @Parsed
    private int mode = 1;

    @Parsed
    private double pdes = 0.0;

    @Parsed
    private double qdes = 0.0;

    @Parsed
    private double vset = 1.0;

    @Parsed
    private double shmx = 9999.0;

    @Parsed
    private double trmx = 9999.0;

    @Parsed
    private double vtmn = 0.9;

    @Parsed
    private double vtmx = 1.1;

    @Parsed
    private double vsmx = 1.0;

    @Parsed
    private double imx = 0.0;

    @Parsed
    private double linx = 0.05;

    @Parsed
    private double rmpct = 100.0;

    @Parsed
    private int owner = 1;

    @Parsed
    private double set1 = 0.0;

    @Parsed
    private double set2 = 0.0;

    @Parsed
    private int vsref = 0;

    @Parsed
    @Revision(until = 33)
    private int remot = 0;

    @Parsed(defaultNullRead = "")
    private String mname;

    @Parsed
    @Revision(since = 35)
    private int fcreg = 0;

    @Parsed
    @Revision(since = 35)
    private int nreg = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public double getPdes() {
        return pdes;
    }

    public void setPdes(double pdes) {
        this.pdes = pdes;
    }

    public double getQdes() {
        return qdes;
    }

    public void setQdes(double qdes) {
        this.qdes = qdes;
    }

    public double getVset() {
        return vset;
    }

    public void setVset(double vset) {
        this.vset = vset;
    }

    public double getShmx() {
        return shmx;
    }

    public void setShmx(double shmx) {
        this.shmx = shmx;
    }

    public double getTrmx() {
        return trmx;
    }

    public void setTrmx(double trmx) {
        this.trmx = trmx;
    }

    public double getVtmn() {
        return vtmn;
    }

    public void setVtmn(double vtmn) {
        this.vtmn = vtmn;
    }

    public double getVtmx() {
        return vtmx;
    }

    public void setVtmx(double vtmx) {
        this.vtmx = vtmx;
    }

    public double getVsmx() {
        return vsmx;
    }

    public void setVsmx(double vsmx) {
        this.vsmx = vsmx;
    }

    public double getImx() {
        return imx;
    }

    public void setImx(double imx) {
        this.imx = imx;
    }

    public double getLinx() {
        return linx;
    }

    public void setLinx(double linx) {
        this.linx = linx;
    }

    public double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public double getSet1() {
        return set1;
    }

    public void setSet1(double set1) {
        this.set1 = set1;
    }

    public double getSet2() {
        return set2;
    }

    public void setSet2(double set2) {
        this.set2 = set2;
    }

    public int getVsref() {
        return vsref;
    }

    public void setVsref(int vsref) {
        this.vsref = vsref;
    }

    public int getRemot() {
        checkVersion("remot");
        return remot;
    }

    public void setRemot(int remot) {
        this.remot = remot;
    }

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public int getFcreg() {
        checkVersion("fcreg");
        return fcreg;
    }

    public void setFcreg(int fcreg) {
        this.fcreg = fcreg;
    }

    public int getNreg() {
        checkVersion("nreg");
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }

    public PsseFacts copy() {
        PsseFacts copy = new PsseFacts();
        copy.name = this.name;
        copy.i = this.i;
        copy.j = this.j;
        copy.mode = this.mode;
        copy.pdes = this.pdes;
        copy.qdes = this.qdes;
        copy.vset = this.vset;
        copy.shmx = this.shmx;
        copy.trmx = this.trmx;
        copy.vtmn = this.vtmn;
        copy.vtmx = this.vtmx;
        copy.vsmx = this.vsmx;
        copy.imx = this.imx;
        copy.linx = this.linx;
        copy.rmpct = this.rmpct;
        copy.owner = this.owner;
        copy.set1 = this.set1;
        copy.set2 = this.set2;
        copy.vsref = this.vsref;
        copy.remot = this.remot;
        copy.mname = this.mname;
        copy.fcreg = this.fcreg;
        copy.nreg = this.nreg;
        return copy;
    }
}
