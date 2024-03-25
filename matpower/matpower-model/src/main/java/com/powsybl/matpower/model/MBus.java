/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bus data
 *
 * <p>
 * @see <a href="https://matpower.org/doc/">https://matpower.org/doc/</a>
 * </p>
 *
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
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
            if (iType < 1 || iType > 4) {
                throw new IllegalArgumentException("No type with id " + iType + " found");
            }
            return values()[iType - 1];
        }
    }

    /**
     * bus number (positive integer)
     */
    private int number;

    /**
     * bus type (1 = PQ, 2 = PV, 3 = ref, 4 = isolated)
     */
    private Type type;

    /**
     * bus name (optional)
     */
    private String name;

    /**
     * real power demand (MW)
     */
    private double realPowerDemand;

    /**
     * reactive power demand (MVAr)
     */
    private double reactivePowerDemand;

    /**
     * shunt conductance (MW demanded at V = 1.0 p.u.)
     */
    private double shuntConductance;

    /**
     * shunt susceptance (MVAr injected at V = 1.0 p.u.)
     */
    private double shuntSusceptance;

    /**
     * area number (positive integer)
     */
    private int areaNumber;

    /**
     * voltage magnitude (p.u.)
     */
    private double voltageMagnitude;

    /**
     * voltage angle (degrees)
     */
    private double voltageAngle;

    /**
     * base voltage (kV)
     */
    private double baseVoltage;

    /**
     * loss zone (positive integer)
     */
    private int lossZone;

    /**
     * maximum voltage magnitude (p.u.)
     */
    private double maximumVoltageMagnitude;

    /**
     * minimum voltage magnitude (p.u.)
     */
    private double minimumVoltageMagnitude;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
