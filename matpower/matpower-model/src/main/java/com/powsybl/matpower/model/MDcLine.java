/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class MDcLine {

    /**
     * from bus number
     */
    private int from;

    /**
     * to bus number
     */
    private int to;

    /**
     * initial branch status, 1 = in-service, 0 = out-of-service
     */
    private int status;

    /**
     * real power flow at “from” bus end (MW), “from” → “to”
     */
    private double pf;

    /**
     * real power flow at “to” bus end (MW), “from” → “to”
     */
    private double pt;

    /**
     * reactive power injected into “from” bus (MVAr)
     */
    private double qf;

    /**
     * reactive power injected into “to” bus (MVAr)
     */
    private double qt;

    /**
     * voltage magnitude setpoint at “from” bus (p.u.)
     */
    private double vf;

    /**
     * voltage magnitude setpoint at “to” bus (p.u.)
     */
    private double vt;

    /**
     * if positive (negative), lower limit on PF (PT)
     */
    private double pmin;

    /**
     * if positive (negative), upper limit on PF (PT)
     */
    private double pmax;

    /**
     * lower limit on reactive power injection into “from” bus (MVAr)
     */
    private double qminf;

    /**
     * upper limit on reactive power injection into “from” bus (MVAr)
     */
    private double qmaxf;

    /**
     * lower limit on reactive power injection into “to” bus (MVAr)
     */
    private double qmint;

    /**
     * upper limit on reactive power injection into “to” bus (MVAr)
     */
    private double qmaxt;

    /**
     * coefficient l0 of constant term of linear loss function (MW)
     */
    private double loss0;

    /**
     * coefficient l1 of linear term of linear loss function (MW/MW)
     * (ploss = l0 + l1pf , where pf is the flow at the “from” end)
     */
    private double loss1;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getPf() {
        return pf;
    }

    public void setPf(double pf) {
        this.pf = pf;
    }

    public double getPt() {
        return pt;
    }

    public void setPt(double pt) {
        this.pt = pt;
    }

    public double getQf() {
        return qf;
    }

    public void setQf(double qf) {
        this.qf = qf;
    }

    public double getQt() {
        return qt;
    }

    public void setQt(double qt) {
        this.qt = qt;
    }

    public double getVf() {
        return vf;
    }

    public void setVf(double vf) {
        this.vf = vf;
    }

    public double getVt() {
        return vt;
    }

    public void setVt(double vt) {
        this.vt = vt;
    }

    public double getPmin() {
        return pmin;
    }

    public void setPmin(double pmin) {
        this.pmin = pmin;
    }

    public double getPmax() {
        return pmax;
    }

    public void setPmax(double pmax) {
        this.pmax = pmax;
    }

    public double getQminf() {
        return qminf;
    }

    public void setQminf(double qminf) {
        this.qminf = qminf;
    }

    public double getQmaxf() {
        return qmaxf;
    }

    public void setQmaxf(double qmaxf) {
        this.qmaxf = qmaxf;
    }

    public double getQmint() {
        return qmint;
    }

    public void setQmint(double qmint) {
        this.qmint = qmint;
    }

    public double getQmaxt() {
        return qmaxt;
    }

    public void setQmaxt(double qmaxt) {
        this.qmaxt = qmaxt;
    }

    public double getLoss0() {
        return loss0;
    }

    public void setLoss0(double loss0) {
        this.loss0 = loss0;
    }

    public double getLoss1() {
        return loss1;
    }

    public void setLoss1(double loss1) {
        this.loss1 = loss1;
    }
}
