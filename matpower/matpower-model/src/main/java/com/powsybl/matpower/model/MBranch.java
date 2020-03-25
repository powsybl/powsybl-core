/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model;

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
public class MBranch {

    /**
     * from bus number
     */
    @Parsed(index = 0)
    private int from;

    /**
     * to bus number
     */
    @Parsed(index = 1)
    private int to;

    /**
     * BR R resistance (p.u.)
     */
    @Parsed(index = 2)
    private double r;

    /**
     * BR X reactance (p.u.)
     */
    @Parsed(index = 3)
    private double x;

    /**
     * BR B total line charging susceptance (p.u.)
     */
    @Parsed(index = 4)
    private double b;

    /**
     * MVA rating A (long term rating), set to 0 for unlimited
     */
    @Parsed(index = 5)
    private double rateA;

    /**
     * MVA rating B (short term rating), set to 0 for unlimited
     */
    @Parsed(index = 6)
    private double rateB;

    /**
     * MVA rating C (emergency rating), set to 0 for unlimited
     */
    @Parsed(index = 7)
    private double rateC;

    /**
     * transformer off nominal turns ratio
     */
    @Parsed(index = 8)
    private double ratio;

    /**
     * transformer phase shift angle (degrees), positive ) delay
     */
    @Parsed(index = 9)
    private double phaseShiftAngle;

    /**
     * initial branch status, 1 = in-service, 0 = out-of-service
     */
    @Parsed(index = 10)
    private double status;

    /**
     * minimum angle difference, ThetaF - ThetaT (degrees)
     */
    @Parsed(index = 11)
    private double angMin;

    /**
     * maximum angle difference, ThetaF - ThetaT (degrees)
     */
    @Parsed(index = 12)
    private double angMax;

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

    public double getRateA() {
        return rateA;
    }

    public void setRateA(double rateA) {
        this.rateA = rateA;
    }

    public double getRateB() {
        return rateB;
    }

    public void setRateB(double rateB) {
        this.rateB = rateB;
    }

    public double getRateC() {
        return rateC;
    }

    public void setRateC(double rateC) {
        this.rateC = rateC;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getPhaseShiftAngle() {
        return phaseShiftAngle;
    }

    public void setPhaseShiftAngle(double phaseShiftAngle) {
        this.phaseShiftAngle = phaseShiftAngle;
    }

    public double getStatus() {
        return status;
    }

    public void setStatus(double status) {
        this.status = status;
    }

    public double getAngMin() {
        return angMin;
    }

    public void setAngMin(double angMin) {
        this.angMin = angMin;
    }

    public double getAngMax() {
        return angMax;
    }

    public void setAngMax(double angMax) {
        this.angMax = angMax;
    }
}
