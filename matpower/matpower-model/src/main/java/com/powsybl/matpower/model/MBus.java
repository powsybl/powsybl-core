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
 * LAM P(+)         14  Lagrange multiplier on real power mismatch (u/MW)
 * LAM Q(+)         15  Lagrange multiplier on reactive power mismatch (u/MVAr)
 * MU VMAX(+)       16  Kuhn-Tucker multiplier on upper voltage limit (u/p.u.)
 * MU VMIN(+)       17  Kuhn-Tucker multiplier on lower voltage limit (u/p.u.)
 * -------------------------------------------------------------------------
 * (+) Included in OPF output, typically not included (or ignored) in input matrix.
 * </pre>
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MBus {

    /**
     * bus type (1 = PQ, 2 = PV, 3 = ref, 4 = isolated)
     */
    public enum Type {
        PQ(1),
        PV(2),
        REF(3),
        ISOLATED(4);

        private final int id;

        Type(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }

        public static Type fromInt(int iType) {
            for (Type b : Type.values()) {
                if (b.getValue() == iType) {
                    return b;
                }
            }
            throw new IllegalArgumentException("No type with id " + iType + " found");
        }
    }

    /**
     * bus number (positive integer)
     */
    @Parsed(index = 0)
    private int number;

    /**
     * bus type (1 = PQ, 2 = PV, 3 = ref, 4 = isolated)
     */
    @Parsed(index = 1)
    @Convert(conversionClass = MBusTypeConversion.class)
    private Type type;

    /**
     * real power demand (MW)
     */
    @Parsed(index = 2)
    private double realPowerDemand;

    /**
     * reactive power demand (MVAr)
     */
    @Parsed(index = 3)
    private double reactivePowerDemand;

    /**
     * shunt conductance (MW demanded at V = 1.0 p.u.)
     */
    @Parsed(index = 4)
    private double shuntConductance;

    /**
     * shunt susceptance (MVAr injected at V = 1.0 p.u.)
     */
    @Parsed(index = 5)
    private double shuntSusceptance;

    /**
     * area number (positive integer)
     */
    @Parsed(index = 6)
    private int areaNumber;

    /**
     * voltage magnitude (p.u.)
     */
    @Parsed(index = 7)
    private double voltageMagnitude;

    /**
     * voltage angle (degrees)
     */
    @Parsed(index = 8)
    private double voltageAngle;

    /**
     * base voltage (kV)
     */
    @Parsed(index = 9)
    private double baseVoltage;

    /**
     * loss zone (positive integer)
     */
    @Parsed(index = 10)
    private int lossZone;

    /**
     * maximum voltage magnitude (p.u.)
     */
    @Parsed(index = 11)
    private double maximumVoltageMagnitude;

    /**
     * minimum voltage magnitude (p.u.)
     */
    @Parsed(index = 12)
    private double minimumVoltageMagnitude;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getRealPowerDemand() {
        return realPowerDemand;
    }

    public void setRealPowerDemand(double realPowerDemand) {
        this.realPowerDemand = realPowerDemand;
    }

    public double getReactivePowerDemand() {
        return reactivePowerDemand;
    }

    public void setReactivePowerDemand(double reactivePowerDemand) {
        this.reactivePowerDemand = reactivePowerDemand;
    }

    public double getShuntConductance() {
        return shuntConductance;
    }

    public void setShuntConductance(double shuntConductance) {
        this.shuntConductance = shuntConductance;
    }

    public double getShuntSusceptance() {
        return shuntSusceptance;
    }

    public void setShuntSusceptance(double shuntSusceptance) {
        this.shuntSusceptance = shuntSusceptance;
    }

    public int getAreaNumber() {
        return areaNumber;
    }

    public void setAreaNumber(int areaNumber) {
        this.areaNumber = areaNumber;
    }

    public double getVoltageMagnitude() {
        return voltageMagnitude;
    }

    public void setVoltageMagnitude(double voltageMagnitude) {
        this.voltageMagnitude = voltageMagnitude;
    }

    public double getVoltageAngle() {
        return voltageAngle;
    }

    public void setVoltageAngle(double voltageAngle) {
        this.voltageAngle = voltageAngle;
    }

    public double getBaseVoltage() {
        return baseVoltage;
    }

    public void setBaseVoltage(double baseVoltage) {
        this.baseVoltage = baseVoltage;
    }

    public int getLossZone() {
        return lossZone;
    }

    public void setLossZone(int lossZone) {
        this.lossZone = lossZone;
    }

    public double getMaximumVoltageMagnitude() {
        return maximumVoltageMagnitude;
    }

    public void setMaximumVoltageMagnitude(double maximumVoltageMagnitude) {
        this.maximumVoltageMagnitude = maximumVoltageMagnitude;
    }

    public double getMinimumVoltageMagnitude() {
        return minimumVoltageMagnitude;
    }

    public void setMinimumVoltageMagnitude(double minimumVoltageMagnitude) {
        this.minimumVoltageMagnitude = minimumVoltageMagnitude;
    }

    @Override
    public String toString() {
        return "MBus{" +
                "number=" + number +
                ", type=" + type +
                ", realPowerDemand=" + realPowerDemand +
                ", reactivePowerDemand=" + reactivePowerDemand +
                ", shuntConductance=" + shuntConductance +
                ", shuntSusceptance=" + shuntSusceptance +
                ", areaNumber=" + areaNumber +
                ", voltageMagnitude=" + voltageMagnitude +
                ", voltageAngle=" + voltageAngle +
                ", baseVoltage=" + baseVoltage +
                ", lossZone=" + lossZone +
                ", maximumVoltageMagnitude=" + maximumVoltageMagnitude +
                ", minimumVoltageMagnitude=" + minimumVoltageMagnitude +
                '}';
    }
}
