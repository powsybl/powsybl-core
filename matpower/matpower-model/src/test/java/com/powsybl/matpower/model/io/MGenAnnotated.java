/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model.io;

import com.powsybl.matpower.model.MGen;
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
 * -------------------------------------------------------------------------
 * </pre>
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MGenAnnotated extends MGen {
    @Parsed(index = 0)
    public int getNumber() {
        return super.getNumber();
    }

    @Parsed(index = 0, field = "number")
    public void setNumber(int number) {
        super.setNumber(number);
    }

    @Parsed(index = 1)
    public double getRealPowerOutput() {
        return super.getRealPowerOutput();
    }

    @Parsed(index = 1, field = "realPowerOutput")
    public void setRealPowerOutput(double realPowerOutput) {
        super.setRealPowerOutput(realPowerOutput);
    }

    @Parsed(index = 2)
    public double getReactivePowerOutput() {
        return super.getReactivePowerOutput();
    }

    @Parsed(index = 2, field = "reactivePowerOutput")
    public void setReactivePowerOutput(double reactivePowerOutput) {
        super.setReactivePowerOutput(reactivePowerOutput);
    }

    @Parsed(index = 3)
    @Convert(conversionClass = MDoubleConversion.class)
    public double getMaximumReactivePowerOutput() {
        return super.getMaximumReactivePowerOutput();
    }

    @Parsed(index = 3, field = "maximumReactivePowerOutput")
    @Convert(conversionClass = MDoubleConversion.class)
    public void setMaximumReactivePowerOutput(double maximumReactivePowerOutput) {
        super.setMaximumReactivePowerOutput(maximumReactivePowerOutput);
    }

    @Parsed(index = 4)
    @Convert(conversionClass = MDoubleConversion.class)
    public Double getMinimumReactivePowerOutput() {
        return super.getMinimumReactivePowerOutput();
    }

    @Parsed(index = 4, field = "minimumReactivePowerOutput")
    @Convert(conversionClass = MDoubleConversion.class)
    public void setMinimumReactivePowerOutput(Double minimumReactivePowerOutput) {
        super.setMinimumReactivePowerOutput(minimumReactivePowerOutput);
    }

    @Parsed(index = 5)
    public double getVoltageMagnitudeSetpoint() {
        return super.getVoltageMagnitudeSetpoint();
    }

    @Parsed(index = 5, field = "voltageMagnitudeSetpoint")
    public void setVoltageMagnitudeSetpoint(double voltageMagnitudeSetpoint) {
        super.setVoltageMagnitudeSetpoint(voltageMagnitudeSetpoint);
    }

    @Parsed(index = 6)
    public double getTotalMbase() {
        return super.getTotalMbase();
    }

    @Parsed(index = 6, field = "totalMbase")
    public void setTotalMbase(double totalMbase) {
        super.setTotalMbase(totalMbase);
    }

    @Parsed(index = 7)
    public int getStatus() {
        return super.getStatus();
    }

    @Parsed(index = 7, field = "status")
    public void setStatus(int status) {
        super.setStatus(status);
    }

    @Parsed(index = 8)
    public double getMaximumRealPowerOutput() {
        return super.getMaximumRealPowerOutput();
    }

    @Parsed(index = 8, field = "maximumRealPowerOutput")
    public void setMaximumRealPowerOutput(double maximumRealPowerOutput) {
        super.setMaximumRealPowerOutput(maximumRealPowerOutput);
    }

    @Parsed(index = 9)
    public double getMinimumRealPowerOutput() {
        return super.getMinimumRealPowerOutput();
    }

    @Parsed(index = 9, field = "minimumRealPowerOutput")
    public void setMinimumRealPowerOutput(double minimumRealPowerOutput) {
        super.setMinimumRealPowerOutput(minimumRealPowerOutput);
    }

    @Parsed(index = 10)
    public double getPc1() {
        return super.getPc1();
    }

    @Parsed(index = 10, field = "pc1")
    public void setPc1(double pc1) {
        super.setPc1(pc1);
    }

    @Parsed(index = 11)
    public double getPc2() {
        return super.getPc2();
    }

    @Parsed(index = 11, field = "pc2")
    public void setPc2(double pc2) {
        super.setPc2(pc2);
    }

    @Parsed(index = 12)
    public double getQc1Min() {
        return super.getQc1Min();
    }

    @Parsed(index = 12, field = "qc1Min")
    public void setQc1Min(double qc1Min) {
        super.setQc1Min(qc1Min);
    }

    @Parsed(index = 13)
    public double getQc1Max() {
        return super.getQc1Max();
    }

    @Parsed(index = 13, field = "qc1Max")
    public void setQc1Max(double qc1Max) {
        super.setQc1Max(qc1Max);
    }

    @Parsed(index = 14)
    public double getQc2Min() {
        return super.getQc2Min();
    }

    @Parsed(index = 14, field = "qc2Min")
    public void setQc2Min(double qc2Min) {
        super.setQc2Min(qc2Min);
    }

    @Parsed(index = 15)
    public double getQc2Max() {
        return super.getQc2Max();
    }

    @Parsed(index = 15, field = "qc2Max")
    public void setQc2Max(double qc2Max) {
        super.setQc2Max(qc2Max);
    }

    @Parsed(index = 16)
    public double getRampAgc() {
        return super.getRampAgc();
    }

    @Parsed(index = 16, field = "rampAgc")
    public void setRampAgc(double rampAgc) {
        super.setRampAgc(rampAgc);
    }

    @Parsed(index = 17)
    public double getRampTenMinutes() {
        return super.getRampTenMinutes();
    }

    @Parsed(index = 17, field = "rampTenMinutes")
    public void setRampTenMinutes(double rampTenMinutes) {
        super.setRampTenMinutes(rampTenMinutes);
    }

    @Parsed(index = 18)
    public double getRampThirtyMinutes() {
        return super.getRampThirtyMinutes();
    }

    @Parsed(index = 18, field = "rampThirtyMinutes")
    public void setRampThirtyMinutes(double rampThirtyMinutes) {
        super.setRampThirtyMinutes(rampThirtyMinutes);
    }

    @Parsed(index = 19)
    public double getRampQ() {
        return super.getRampQ();
    }

    @Parsed(index = 19, field = "rampQ")
    public void setRampQ(double rampQ) {
        super.setRampQ(rampQ);
    }

    @Parsed(index = 20)
    public double getApf() {
        return super.getApf();
    }

    @Parsed(index = 20, field = "apf")
    public void setApf(double apf) {
        super.setApf(apf);
    }
}
