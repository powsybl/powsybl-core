/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model;

import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.Parsed;

/**
 * <p>
 * @see <a href="https://matpower.org/doc/">https://matpower.org/doc/</a>
 * </p>
 *
 * <pre>
 * Generator Data (mpc.gen)
 * -------------------------------------------------------------------------
 * Name         Column  Description
 * -------------------------------------------------------------------------
 * GEN BUS      1       bus number
 * PG           2       real power output (MW)
 * QG           3       reactive power output (MVAr)
 * QMAX         4       maximum reactive power output (MVAr)
 * QMIN         5       minimum reactive power output (MVAr)
 * VG           6       voltage magnitude setpoint (p.u.)
 * MBASE        7       total MVA base of machine, defaults to baseMVA
 * GEN STATUS   8       machine status:
 *                       - greater than 0:  machine in-service;
  *                      - less or equal to 0:  machine out-of-service)
 * PMAX         9       maximum real power output (MW)
 * PMIN         10      minimum real power output (MW)
 * PC1          11      lower real power output of PQ capability curve (MW)
 * PC2          12      upper real power output of PQ capability curve (MW)
 * QC1MIN       13      minimum reactive power output at PC1 (MVAr)
 * QC1MAX       14      maximum reactive power output at PC1 (MVAr)
 * QC2MIN       15      minimum reactive power output at PC2 (MVAr)
 * QC2MAX       16      maximum reactive power output at PC2 (MVAr)
 * RAMP AGC     17      ramp rate for load following/AGC (MW/min)
 * RAMP 10      18      ramp rate for 10 minute reserves (MW)
 * RAMP 30      19      ramp rate for 30 minute reserves (MW)
 * RAMP Q       20      ramp rate for reactive power (2 sec timescale) (MVAr/min)
 * APF          21      area participation factor
 * MU PMAX(+)   22      Kuhn-Tucker multiplier on upper Pg limit (u/MW)
 * MU PMIN(+)   23      Kuhn-Tucker multiplier on lower Pg limit (u/MW)
 * MU QMAX(+)   24      Kuhn-Tucker multiplier on upper Qg limit (u/MVAr)
 * MU QMIN(+)   25      Kuhn-Tucker multiplier on lower Qg limit (u/MVAr)
 * -------------------------------------------------------------------------
 * (+) Included in OPF output, typically not included (or ignored) in input matrix.
 * </pre>
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MGen {

    /**
     * bus number (positive integer)
     */
    @Parsed(index = 0)
    private int number;

    /**
     * real power output (MW)
     */
    @Parsed(index = 1)
    private double realPowerOutput;

    /**
     * reactive power output (MVAr)
     */
    @Parsed(index = 2)
    private double reactivePowerOutput;

    /**
     *  maximum reactive power output (MVAr)
     */
    @Parsed(index = 3)
    @Convert(conversionClass = MDoubleConversion.class)
    private double maximumReactivePowerOutput;

    /**
     * minimum reactive power output (MVAr)
     */
    @Parsed(index = 4)
    @Convert(conversionClass = MDoubleConversion.class)
    private Double minimumReactivePowerOutput;

    /**
     * voltage magnitude setpoint (p.u.)
     */
    @Parsed(index = 5)
    private double voltageMagnitudeSetpoint;

    /**
     * total MVA base of machine, defaults to baseMVA
     */
    @Parsed(index = 6)
    private double totalMbase;

    /**
     * status
     */
    @Parsed(index = 7)
    private int status;

    /**
     * maximum real power output (MW)
     */
    @Parsed(index = 8)
    private double maximumRealPowerOutput;

    /**
     * minimum real power output (MW)
     */
    @Parsed(index = 9)
    private double minimumRealPowerOutput;

    /**
     * lower real power output of PQ capability curve (MW)
     */
    @Parsed(index = 10)
    private double pc1;

    /**
     * upper real power output of PQ capability curve (MW)
     */
    @Parsed(index = 11)
    private double pc2;

    /**
     * minimum reactive power output at PC1 (MVAr)
     */
    @Parsed(index = 12)
    private double qc1Min;

    /**
     * maximum reactive power output at PC1 (MVAr)
     */
    @Parsed(index = 13)
    private double qc1Max;

    /**
     * minimum reactive power output at PC2 (MVAr)
     */
    @Parsed(index = 14)
    private double qc2Min;

    /**
     * maximum reactive power output at PC2 (MVAr)
     */
    @Parsed(index = 15)
    private double qc2Max;

    /**
     * ramp rate for load following/AGC (MW/min)
     */
    @Parsed(index = 16)
    private double rampAgc;

    /**
     * ramp rate for 10 minute reserves (MW)
     */
    @Parsed(index = 17)
    private double rampTenMinutes;

    /**
     * ramp rate for 30 minute reserves (MW)
     */
    @Parsed(index = 18)
    private double rampThirtyMinutes;

    /**
     * ramp rate for reactive power (2 sec timescale) (MVAr/min)
     */
    @Parsed(index = 19)
    private double rampQ;

    /**
     * area participation factor
     */
    @Parsed(index = 20)
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
