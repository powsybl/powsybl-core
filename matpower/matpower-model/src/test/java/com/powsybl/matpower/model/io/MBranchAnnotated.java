/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model.io;

import com.powsybl.matpower.model.MBranch;
import com.univocity.parsers.annotations.Parsed;

/**
 * <p>
 * @see <a href="https://matpower.org/doc/">https://matpower.org/doc/</a>
 * </p>
 *
 * <pre>
 * Branch Data (mpc.branch)
 * -------------------------------------------------------------------------
 * Name         Column  Description
 * -------------------------------------------------------------------------
 * F BUS        1       from bus number
 * T BUS        2       to bus number
 * BR R         3       resistance (p.u.)
 * BR X         4       reactance (p.u.)
 * BR B         5       total line charging susceptance (p.u.)
 * RATE A       6       MVA rating A (long term rating), set to 0 for unlimited
 * RATE B       7       MVA rating B (short term rating), set to 0 for unlimited
 * RATE C       8       MVA rating C (emergency rating), set to 0 for unlimited
 * TAP          9       transformer off nominal turns ratio, if non-zero (taps at from
 *                       bus, impedance at to bus, i.e. if r = x = b = 0, tap = abs(Vf)/abs(Vt)
 *                       tap = 0 used to indicate transmission line rather than transformer,
 *                       i.e. mathematically equivalent to transformer with tap = 1)
 * SHIFT        10      transformer phase shift angle (degrees), positive ) delay
 * BR STATUS    11      initial branch status, 1 = in-service, 0 = out-of-service
 * ANGMIN       12      minimum angle difference, ThetaF - ThetaT (degrees)
 * ANGMAX       13      maximum angle difference, ThetaF - ThetaT (degrees)
 * -------------------------------------------------------------------------
 * </pre>
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MBranchAnnotated extends MBranch {
    @Parsed(index = 0)
    public int getFrom() {
        return super.getFrom();
    }

    @Parsed(index = 0, field = "from")
    public void setFrom(int from) {
        super.setFrom(from);
    }

    @Parsed(index = 1)
    public int getTo() {
        return super.getTo();
    }

    @Parsed(index = 1, field = "to")
    public void setTo(int to) {
        super.setTo(to);
    }

    @Parsed(index = 2)
    public double getR() {
        return super.getR();
    }

    @Parsed(index = 2, field = "r")
    public void setR(double r) {
        super.setR(r);
    }

    @Parsed(index = 3)
    public double getX() {
        return super.getX();
    }

    @Parsed(index = 3, field = "x")
    public void setX(double x) {
        super.setX(x);
    }

    @Parsed(index = 4)
    public double getB() {
        return super.getB();
    }

    @Parsed(index = 4, field = "b")
    public void setB(double b) {
        super.setB(b);
    }

    @Parsed(index = 5)
    public double getRateA() {
        return super.getRateA();
    }

    @Parsed(index = 5, field = "rateA")
    public void setRateA(double rateA) {
        super.setRateA(rateA);
    }

    @Parsed(index = 6)
    public double getRateB() {
        return super.getRateB();
    }

    @Parsed(index = 6, field = "rateB")
    public void setRateB(double rateB) {
        super.setRateB(rateB);
    }

    @Parsed(index = 7)
    public double getRateC() {
        return super.getRateC();
    }

    @Parsed(index = 7, field = "rateC")
    public void setRateC(double rateC) {
        super.setRateC(rateC);
    }

    @Parsed(index = 8)
    public double getRatio() {
        return super.getRatio();
    }

    @Parsed(index = 8, field = "ratio")
    public void setRatio(double ratio) {
        super.setRatio(ratio);
    }

    @Parsed(index = 9)
    public double getPhaseShiftAngle() {
        return super.getPhaseShiftAngle();
    }

    @Parsed(index = 9, field = "phaseShiftAngle")
    public void setPhaseShiftAngle(double phaseShiftAngle) {
        super.setPhaseShiftAngle(phaseShiftAngle);
    }

    @Parsed(index = 10)
    public double getStatus() {
        return super.getStatus();
    }

    @Parsed(index = 10, field = "status")
    public void setStatus(double status) {
        super.setStatus(status);
    }

    @Parsed(index = 11)
    public double getAngMin() {
        return super.getAngMin();
    }

    @Parsed(index = 11, field = "angMin")
    public void setAngMin(double angMin) {
        super.setAngMin(angMin);
    }

    @Parsed(index = 12)
    public double getAngMax() {
        return super.getAngMax();
    }

    @Parsed(index = 12, field = "angMax")
    public void setAngMax(double angMax) {
        super.setAngMax(angMax);
    }
}
