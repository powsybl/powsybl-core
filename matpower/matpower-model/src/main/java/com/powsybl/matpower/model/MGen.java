/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

/**
 * Generator data
 *
 * <p>
 * @see <a href="https://matpower.org/doc/">https://matpower.org/doc/</a>
 * </p>
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
public class MGen {

    /**
     * bus number (positive integer)
     */
    private int number;

    /**
     * real power output (MW)
     */
    private double realPowerOutput;

    /**
     * reactive power output (MVAr)
     */
    private double reactivePowerOutput;

    /**
     *  maximum reactive power output (MVAr)
     */
    private double maximumReactivePowerOutput;

    /**
     * minimum reactive power output (MVAr)
     */
    private Double minimumReactivePowerOutput;

    /**
     * voltage magnitude setpoint (p.u.)
     */
    private double voltageMagnitudeSetpoint;

    /**
     * total MVA base of machine, defaults to baseMVA
     */
    private double totalMbase;

    /**
     * status
     */
    private int status;

    /**
     * maximum real power output (MW)
     */
    private double maximumRealPowerOutput;

    /**
     * minimum real power output (MW)
     */
    private double minimumRealPowerOutput;

    /**
     * lower real power output of PQ capability curve (MW)
     */
    private double pc1;

    /**
     * upper real power output of PQ capability curve (MW)
     */
    private double pc2;

    /**
     * minimum reactive power output at PC1 (MVAr)
     */
    private double qc1Min;

    /**
     * maximum reactive power output at PC1 (MVAr)
     */
    private double qc1Max;

    /**
     * minimum reactive power output at PC2 (MVAr)
     */
    private double qc2Min;

    /**
     * maximum reactive power output at PC2 (MVAr)
     */
    private double qc2Max;

    /**
     * ramp rate for load following/AGC (MW/min)
     */
    private double rampAgc;

    /**
     * ramp rate for 10 minute reserves (MW)
     */
    private double rampTenMinutes;

    /**
     * ramp rate for 30 minute reserves (MW)
     */
    private double rampThirtyMinutes;

    /**
     * ramp rate for reactive power (2 sec timescale) (MVAr/min)
     */
    private double rampQ;

    /**
     * area participation factor
     */
    private double apf;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getRealPowerOutput() {
        return realPowerOutput;
    }

    public void setRealPowerOutput(double realPowerOutput) {
        this.realPowerOutput = realPowerOutput;
    }

    public double getReactivePowerOutput() {
        return reactivePowerOutput;
    }

    public void setReactivePowerOutput(double reactivePowerOutput) {
        this.reactivePowerOutput = reactivePowerOutput;
    }

    public double getMaximumReactivePowerOutput() {
        return maximumReactivePowerOutput;
    }

    public void setMaximumReactivePowerOutput(double maximumReactivePowerOutput) {
        this.maximumReactivePowerOutput = maximumReactivePowerOutput;
    }

    public Double getMinimumReactivePowerOutput() {
        return minimumReactivePowerOutput;
    }

    public void setMinimumReactivePowerOutput(Double minimumReactivePowerOutput) {
        this.minimumReactivePowerOutput = minimumReactivePowerOutput;
    }

    public double getVoltageMagnitudeSetpoint() {
        return voltageMagnitudeSetpoint;
    }

    public void setVoltageMagnitudeSetpoint(double voltageMagnitudeSetpoint) {
        this.voltageMagnitudeSetpoint = voltageMagnitudeSetpoint;
    }

    public double getTotalMbase() {
        return totalMbase;
    }

    public void setTotalMbase(double totalMbase) {
        this.totalMbase = totalMbase;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getMaximumRealPowerOutput() {
        return maximumRealPowerOutput;
    }

    public void setMaximumRealPowerOutput(double maximumRealPowerOutput) {
        this.maximumRealPowerOutput = maximumRealPowerOutput;
    }

    public double getMinimumRealPowerOutput() {
        return minimumRealPowerOutput;
    }

    public void setMinimumRealPowerOutput(double minimumRealPowerOutput) {
        this.minimumRealPowerOutput = minimumRealPowerOutput;
    }

    public double getPc1() {
        return pc1;
    }

    public void setPc1(double pc1) {
        this.pc1 = pc1;
    }

    public double getPc2() {
        return pc2;
    }

    public void setPc2(double pc2) {
        this.pc2 = pc2;
    }

    public double getQc1Min() {
        return qc1Min;
    }

    public void setQc1Min(double qc1Min) {
        this.qc1Min = qc1Min;
    }

    public double getQc1Max() {
        return qc1Max;
    }

    public void setQc1Max(double qc1Max) {
        this.qc1Max = qc1Max;
    }

    public double getQc2Min() {
        return qc2Min;
    }

    public void setQc2Min(double qc2Min) {
        this.qc2Min = qc2Min;
    }

    public double getQc2Max() {
        return qc2Max;
    }

    public void setQc2Max(double qc2Max) {
        this.qc2Max = qc2Max;
    }

    public double getRampAgc() {
        return rampAgc;
    }

    public void setRampAgc(double rampAgc) {
        this.rampAgc = rampAgc;
    }

    public double getRampTenMinutes() {
        return rampTenMinutes;
    }

    public void setRampTenMinutes(double rampTenMinutes) {
        this.rampTenMinutes = rampTenMinutes;
    }

    public double getRampThirtyMinutes() {
        return rampThirtyMinutes;
    }

    public void setRampThirtyMinutes(double rampThirtyMinutes) {
        this.rampThirtyMinutes = rampThirtyMinutes;
    }

    public double getRampQ() {
        return rampQ;
    }

    public void setRampQ(double rampQ) {
        this.rampQ = rampQ;
    }

    public double getApf() {
        return apf;
    }

    public void setApf(double apf) {
        this.apf = apf;
    }
}
