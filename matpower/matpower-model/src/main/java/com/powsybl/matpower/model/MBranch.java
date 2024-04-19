/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

/**
 * Branch data
 *
 * <p>
 * @see <a href="https://matpower.org/doc/">https://matpower.org/doc/</a>
 * </p>
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
public class MBranch {

    /**
     * from bus number
     */
    private int from;

    /**
     * to bus number
     */
    private int to;

    /**
     * BR R resistance (p.u.)
     */
    private double r;

    /**
     * BR X reactance (p.u.)
     */
    private double x;

    /**
     * BR B total line charging susceptance (p.u.)
     */
    private double b;

    /**
     * MVA rating A (long term rating), set to 0 for unlimited
     */
    private double rateA;

    /**
     * MVA rating B (short term rating), set to 0 for unlimited
     */
    private double rateB;

    /**
     * MVA rating C (emergency rating), set to 0 for unlimited
     */
    private double rateC;

    /**
     * transformer off nominal turns ratio
     */
    private double ratio;

    /**
     * transformer phase shift angle (degrees), positive ) delay
     */
    private double phaseShiftAngle;

    /**
     * initial branch status, 1 = in-service, 0 = out-of-service
     */
    private int status;

    /**
     * minimum angle difference, ThetaF - ThetaT (degrees)
     */
    private double angMin;

    /**
     * maximum angle difference, ThetaF - ThetaT (degrees)
     */
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

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
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
