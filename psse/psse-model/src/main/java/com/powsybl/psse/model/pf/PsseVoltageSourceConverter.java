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
import com.univocity.parsers.annotations.Parsed;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseVoltageSourceConverter extends PsseVersioned {

    @Parsed
    private int ibus;

    @Parsed
    private int type;

    @Parsed
    private int mode = 1;

    @Parsed
    private double dcset;

    @Parsed
    private double acset = 1.0;

    @Parsed
    private double aloss = 0.0;

    @Parsed
    private double bloss = 0.0;

    @Parsed
    private double minloss = 0.0;

    @Parsed
    private double smax = 0.0;

    @Parsed
    private double imax = 0.0;

    @Parsed
    private double pwf = 1.0;

    @Parsed
    private double maxq = 9999.0;

    @Parsed
    private double minq = -9999.0;

    @Parsed
    @Revision(until = 33)
    private int remot = 0;

    @Parsed
    private double rmpct = 100.0;

    @Parsed
    @Revision(since = 35)
    private int vsreg = 0;

    @Parsed
    @Revision(since = 35)
    private int nreg = 0;

    public int getIbus() {
        return ibus;
    }

    public void setIbus(int ibus) {
        this.ibus = ibus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public double getDcset() {
        return dcset;
    }

    public void setDcset(double dcset) {
        this.dcset = dcset;
    }

    public double getAcset() {
        return acset;
    }

    public void setAcset(double acset) {
        this.acset = acset;
    }

    public double getAloss() {
        return aloss;
    }

    public void setAloss(double aloss) {
        this.aloss = aloss;
    }

    public double getBloss() {
        return bloss;
    }

    public void setBloss(double bloss) {
        this.bloss = bloss;
    }

    public double getMinloss() {
        return minloss;
    }

    public void setMinloss(double minloss) {
        this.minloss = minloss;
    }

    public double getSmax() {
        return smax;
    }

    public void setSmax(double smax) {
        this.smax = smax;
    }

    public double getImax() {
        return imax;
    }

    public void setImax(double imax) {
        this.imax = imax;
    }

    public double getPwf() {
        return pwf;
    }

    public void setPwf(double pwf) {
        this.pwf = pwf;
    }

    public double getMaxq() {
        return maxq;
    }

    public void setMaxq(double maxq) {
        this.maxq = maxq;
    }

    public double getMinq() {
        return minq;
    }

    public void setMinq(double minq) {
        this.minq = minq;
    }

    public int getRemot() {
        checkVersion("remot");
        return remot;
    }

    public void setRemot(int remot) {
        this.remot = remot;
    }

    public double getRmpct() {
        return rmpct;
    }

    public void setRmpct(double rmpct) {
        this.rmpct = rmpct;
    }

    public int getVsreg() {
        checkVersion("vsreg");
        return vsreg;
    }

    public void setVsreg(int vsreg) {
        this.vsreg = vsreg;
    }

    public int getNreg() {
        checkVersion("nreg");
        return nreg;
    }

    public void setNreg(int nreg) {
        this.nreg = nreg;
    }

    public PsseVoltageSourceConverter copy() {
        PsseVoltageSourceConverter copy = new PsseVoltageSourceConverter();
        copy.ibus = this.ibus;
        copy.type = this.type;
        copy.mode = this.mode;
        copy.dcset = this.dcset;
        copy.acset = this.acset;
        copy.aloss = this.aloss;
        copy.bloss = this.bloss;
        copy.minloss = this.minloss;
        copy.smax = this.smax;
        copy.imax = this.imax;
        copy.pwf = this.pwf;
        copy.maxq = this.maxq;
        copy.minq = this.minq;
        copy.remot = this.remot;
        copy.rmpct = this.rmpct;
        copy.vsreg = this.vsreg;
        copy.nreg = this.nreg;
        return copy;
    }
}
