/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

/**
 * <p>
 * @see <a href="https://labs.ece.uw.edu/pstca/formats/cdf.txt">https://labs.ece.uw.edu/pstca/formats/cdf.txt</a>
 * </p>
 *
 * <pre>
 * Columns  1- 4   Bus number (I) *
 * Columns  7-17   Name (A) (left justify) *
 * Columns 19-20   Load flow area number (I) Don't use zero! *
 * Columns 21-23   Loss zone number (I)
 * Columns 25-26   Type (I) *
 * 0 - Unregulated (load, PQ)
 * 1 - Hold MVAR generation within voltage limits, (PQ)
 * 2 - Hold voltage within VAR limits (gen, PV)
 * 3 - Hold voltage and angle (swing, V-Theta) (must always have one)
 * Columns 28-33   Final voltage, p.u. (F) *
 * Columns 34-40   Final angle, degrees (F) *
 * Columns 41-49   Load MW (F) *
 * Columns 50-59   Load MVAR (F) *
 * Columns 60-67   Generation MW (F) *
 * Columns 68-75   Generation MVAR (F) *
 * Columns 77-83   Base KV (F)
 * Columns 85-90   Desired volts (pu) (F) (This is desired remote voltage if this bus is controlling another bus.
 * Columns 91-98   Maximum MVAR or voltage limit (F)
 * Columns 99-106  Minimum MVAR or voltage limit (F)
 * Columns 107-114 Shunt conductance G (per unit) (F) *
 * Columns 115-122 Shunt susceptance B (per unit) (F) *
 * Columns 124-127 Remote controlled bus number
 * </pre>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IeeeCdfBus {

    /**
     * 0 - Unregulated (load, PQ)
     * 1 - Hold MVAR generation within voltage limits, (PQ)
     * 2 - Hold voltage within VAR limits (gen, PV)
     * 3 - Hold voltage and angle (swing, V-Theta) (must always have one)
     */
    public enum Type {
        UNREGULATED,
        HOLD_MVAR_GENERATION_WITHIN_VOLTAGE_LIMITS,
        HOLD_VOLTAGE_WITHIN_VAR_LIMITS,
        HOLD_VOLTAGE_AND_ANGLE
    }

    /**
     * Bus number
     */
    private int number;

    /**
     * Name
     */
    private String name;

    /**
     * Load flow area number
     */
    private int areaNumber;

    /**
     * Loss zone number
     */
    private int lossZoneNumber;

    /**
     * Type
     */
    private Type type;

    /**
     * Final voltage, p.u.
     */
    private double finalVoltage;

    /**
     * Final angle, degrees
     */
    private double finalAngle;

    /**
     * Load MW
     */
    private double activeLoad;

    /**
     * Load MVAR
     */
    private double reactiveLoad;

    /**
     * Generation MW
     */
    private double activeGeneration;

    /**
     * Generation MVAR
     */
    private double reactiveGeneration;

    /**
     * Base KV
     */
    private double baseVoltage;

    /**
     * Desired volts (pu) (F) (This is desired remote voltage if this bus is controlling another bus.
     */
    private double desiredVoltage;

    /**
     * Maximum MVAR or voltage limit
     */
    private double maxReactivePowerOrVoltageLimit;

    /**
     * Minimum MVAR or voltage limit
     */
    private double minReactivePowerOrVoltageLimit;

    /**
     * Shunt conductance G (per unit)
     */
    private double shuntConductance;

    /**
     * Shunt susceptance B (per unit)
     */
    private double shuntSusceptance;

    /**
     * Remote controlled bus number
     */
    private int remoteControlledBusNumber;

    private int unused;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAreaNumber() {
        return areaNumber;
    }

    public void setAreaNumber(int areaNumber) {
        this.areaNumber = areaNumber;
    }

    public int getLossZoneNumber() {
        return lossZoneNumber;
    }

    public void setLossZoneNumber(int lossZoneNumber) {
        this.lossZoneNumber = lossZoneNumber;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getFinalVoltage() {
        return finalVoltage;
    }

    public void setFinalVoltage(double finalVoltage) {
        this.finalVoltage = finalVoltage;
    }

    public double getFinalAngle() {
        return finalAngle;
    }

    public void setFinalAngle(double finalAngle) {
        this.finalAngle = finalAngle;
    }

    public double getActiveLoad() {
        return activeLoad;
    }

    public void setActiveLoad(double activeLoad) {
        this.activeLoad = activeLoad;
    }

    public double getReactiveLoad() {
        return reactiveLoad;
    }

    public void setReactiveLoad(double reactiveLoad) {
        this.reactiveLoad = reactiveLoad;
    }

    public double getActiveGeneration() {
        return activeGeneration;
    }

    public void setActiveGeneration(double activeGeneration) {
        this.activeGeneration = activeGeneration;
    }

    public double getReactiveGeneration() {
        return reactiveGeneration;
    }

    public void setReactiveGeneration(double reactiveGeneration) {
        this.reactiveGeneration = reactiveGeneration;
    }

    public double getBaseVoltage() {
        return baseVoltage;
    }

    public void setBaseVoltage(double baseVoltage) {
        this.baseVoltage = baseVoltage;
    }

    public double getDesiredVoltage() {
        return desiredVoltage;
    }

    public void setDesiredVoltage(double desiredVoltage) {
        this.desiredVoltage = desiredVoltage;
    }

    public double getMaxReactivePowerOrVoltageLimit() {
        return maxReactivePowerOrVoltageLimit;
    }

    public void setMaxReactivePowerOrVoltageLimit(double maxReactivePowerOrVoltageLimit) {
        this.maxReactivePowerOrVoltageLimit = maxReactivePowerOrVoltageLimit;
    }

    public double getMinReactivePowerOrVoltageLimit() {
        return minReactivePowerOrVoltageLimit;
    }

    public void setMinReactivePowerOrVoltageLimit(double minReactivePowerOrVoltageLimit) {
        this.minReactivePowerOrVoltageLimit = minReactivePowerOrVoltageLimit;
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

    public int getRemoteControlledBusNumber() {
        return remoteControlledBusNumber;
    }

    public void setRemoteControlledBusNumber(int remoteControlledBusNumber) {
        this.remoteControlledBusNumber = remoteControlledBusNumber;
    }
}
