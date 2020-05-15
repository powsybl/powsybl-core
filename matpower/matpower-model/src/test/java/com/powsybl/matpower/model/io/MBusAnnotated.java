/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model.io;

import com.powsybl.matpower.model.MBus;
import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.Parsed;

/**
 * <p>
 * @see <a href="https://matpower.org/doc/">https://matpower.org/doc/</a>
 * </p>
 *
 * <pre>
 * Bus Data (mpc.bus)
 * -------------------------------------------------------------------------
 * Name         Column  Description
 * -------------------------------------------------------------------------
 * BUS I            01  bus number (positive integer)
 * BUS TYPE         02  bus type (1 = PQ, 2 = PV, 3 = ref, 4 = isolated)
 * PD               03  real power demand (MW)
 * QD               04  reactive power demand (MVAr)
 * GS               05  shunt conductance (MW demanded at V = 1.0 p.u.)
 * BS               06  shunt susceptance (MVAr injected at V = 1.0 p.u.)
 * BUS AREA         07  area number (positive integer)
 * VM               08  voltage magnitude (p.u.)
 * VA               09  voltage angle (degrees)
 * BASE KV          10  base voltage (kV)
 * ZONE             11  loss zone (positive integer)
 * VMAX             12  maximum voltage magnitude (p.u.)
 * VMIN             13  minimum voltage magnitude (p.u.)
 * -------------------------------------------------------------------------
 * </pre>
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MBusAnnotated extends MBus {
    @Parsed(field = "number")
    public int getNumber() {
        return super.getNumber();
    }

    @Parsed(index = 0, field = "number")
    public void setNumber(int number) {
        super.setNumber(number);
    }

    @Parsed(index = 1)
    @Convert(conversionClass = MBusTypeConversion.class)
    public Type getType() {
        return super.getType();
    }

    @Parsed(index = 1, field = "type")
    @Convert(conversionClass = MBusTypeConversion.class)
    public void setType(Type type) {
        super.setType(type);
    }

    @Parsed(index = 2)
    public double getRealPowerDemand() {
        return super.getRealPowerDemand();
    }

    @Parsed(index = 2, field = "realPowerDemand")
    public void setRealPowerDemand(double realPowerDemand) {
        super.setRealPowerDemand(realPowerDemand);
    }

    @Parsed(index = 3)
    public double getReactivePowerDemand() {
        return super.getReactivePowerDemand();
    }

    @Parsed(index = 3, field = "reactivePowerDemand")
    public void setReactivePowerDemand(double reactivePowerDemand) {
        super.setReactivePowerDemand(reactivePowerDemand);
    }

    @Parsed(index = 4)
    public double getShuntConductance() {
        return super.getShuntConductance();
    }

    @Parsed(index = 4, field = "shuntConductance")
    public void setShuntConductance(double shuntConductance) {
        super.setShuntConductance(shuntConductance);
    }

    @Parsed(index = 5)
    public double getShuntSusceptance() {
        return super.getShuntSusceptance();
    }

    @Parsed(index = 5, field = "shuntSusceptance")
    public void setShuntSusceptance(double shuntSusceptance) {
        super.setShuntSusceptance(shuntSusceptance);
    }

    @Parsed(index = 6)
    public int getAreaNumber() {
        return super.getAreaNumber();
    }

    @Parsed(index = 6, field = "areaNumber")
    public void setAreaNumber(int areaNumber) {
        super.setAreaNumber(areaNumber);
    }

    @Parsed(index = 7)
    public double getVoltageMagnitude() {
        return super.getVoltageMagnitude();
    }

    @Parsed(index = 7, field = "voltageMagnitude")
    public void setVoltageMagnitude(double voltageMagnitude) {
        super.setVoltageMagnitude(voltageMagnitude);
    }

    @Parsed(index = 8)
    public double getVoltageAngle() {
        return super.getVoltageAngle();
    }

    @Parsed(index = 8, field = "voltageAngle")
    public void setVoltageAngle(double voltageAngle) {
        super.setVoltageAngle(voltageAngle);
    }

    @Parsed(index = 9)
    public double getBaseVoltage() {
        return super.getBaseVoltage();
    }

    @Parsed(index = 9, field = "baseVoltage")
    public void setBaseVoltage(double baseVoltage) {
        super.setBaseVoltage(baseVoltage);
    }

    @Parsed(index = 10)
    public int getLossZone() {
        return super.getLossZone();
    }

    @Parsed(index = 10, field = "lossZone")
    public void setLossZone(int lossZone) {
        super.setLossZone(lossZone);
    }

    @Parsed(index = 11)
    public double getMaximumVoltageMagnitude() {
        return super.getMaximumVoltageMagnitude();
    }

    @Parsed(index = 11, field = "maximumVoltageMagnitude")
    public void setMaximumVoltageMagnitude(double maximumVoltageMagnitude) {
        super.setMaximumVoltageMagnitude(maximumVoltageMagnitude);
    }

    @Parsed(index = 12)
    public double getMinimumVoltageMagnitude() {
        return super.getMinimumVoltageMagnitude();
    }

    @Parsed(index = 12, field = "minimumVoltageMagnitude")
    public void setMinimumVoltageMagnitude(double minimumVoltageMagnitude) {
        super.setMinimumVoltageMagnitude(minimumVoltageMagnitude);
    }
}
