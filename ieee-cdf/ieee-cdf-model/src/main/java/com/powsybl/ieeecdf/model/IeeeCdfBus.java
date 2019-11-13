/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.FixedWidth;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.fixed.FieldAlignment;

/**
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
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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

    @FixedWidth(from = 0, to = 4, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int number;

    @FixedWidth(from = 5, to = 17)
    @Parsed
    private String name;

    @FixedWidth(from = 18, to = 20, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int areaNumber;

    @FixedWidth(from = 20, to = 23, alignment = FieldAlignment.RIGHT)
    @Parsed
    private int lossZoneNumber;

    @FixedWidth(from = 24, to = 26, alignment = FieldAlignment.RIGHT)
    @Parsed
    @Convert(conversionClass = BusTypeConversion.class)
    private Type type;

    @FixedWidth(from = 27, to = 33)
    @Parsed
    private float finalVoltage;

    @FixedWidth(from = 33, to = 40)
    @Parsed
    private float finalAngle;

    @FixedWidth(from = 40, to = 49)
    @Parsed
    private float activeLoad;

    @FixedWidth(from = 49, to = 59)
    @Parsed
    private float reactiveLoad;

    @FixedWidth(from = 59, to = 67)
    @Parsed
    private float activeGeneration;

    @FixedWidth(from = 67, to = 75)
    @Parsed
    private float reactiveGeneration;

    @FixedWidth(from = 76, to = 83)
    @Parsed
    private float baseVoltage;

    @FixedWidth(from = 84, to = 90)
    @Parsed
    private float desiredVoltage;

    @FixedWidth(from = 90, to = 98)
    @Parsed
    private float maxReactivePowerOrVoltageLimit;

    @FixedWidth(from = 98, to = 106)
    @Parsed
    private float minReactivePowerOrVoltageLimit;

    @FixedWidth(from = 106, to = 114)
    @Parsed
    private float shuntConductance;

    @FixedWidth(from = 114, to = 122)
    @Parsed
    private float shuntSusceptance;

    @FixedWidth(from = 123, to = 127)
    @Parsed
    private int remoteControlledBusNumber;

    @FixedWidth(from = 127, to = 133)
    @Parsed
    private int unused;

    /**
     * Bus number
     */
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Load flow area number
     */
    public int getAreaNumber() {
        return areaNumber;
    }

    public void setAreaNumber(int areaNumber) {
        this.areaNumber = areaNumber;
    }

    /**
     * Loss zone number
     */
    public int getLossZoneNumber() {
        return lossZoneNumber;
    }

    public void setLossZoneNumber(int lossZoneNumber) {
        this.lossZoneNumber = lossZoneNumber;
    }

    /**
     * Type
     */
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Final voltage, p.u.
     */
    public float getFinalVoltage() {
        return finalVoltage;
    }

    public void setFinalVoltage(float finalVoltage) {
        this.finalVoltage = finalVoltage;
    }

    /**
     * Final angle, degrees
     */
    public float getFinalAngle() {
        return finalAngle;
    }

    public void setFinalAngle(float finalAngle) {
        this.finalAngle = finalAngle;
    }

    /**
     * Load MW
     */
    public float getActiveLoad() {
        return activeLoad;
    }

    public void setActiveLoad(float activeLoad) {
        this.activeLoad = activeLoad;
    }

    /**
     * Load MVAR
     */
    public float getReactiveLoad() {
        return reactiveLoad;
    }

    public void setReactiveLoad(float reactiveLoad) {
        this.reactiveLoad = reactiveLoad;
    }

    /**
     * Generation MW
     */
    public float getActiveGeneration() {
        return activeGeneration;
    }

    public void setActiveGeneration(float activeGeneration) {
        this.activeGeneration = activeGeneration;
    }

    /**
     * Generation MVAR
     */
    public float getReactiveGeneration() {
        return reactiveGeneration;
    }

    public void setReactiveGeneration(float reactiveGeneration) {
        this.reactiveGeneration = reactiveGeneration;
    }

    /**
     * Base KV
     */
    public float getBaseVoltage() {
        return baseVoltage;
    }

    public void setBaseVoltage(float baseVoltage) {
        this.baseVoltage = baseVoltage;
    }

    /**
     * Desired volts (pu) (F) (This is desired remote voltage if this bus is controlling another bus.
     */
    public float getDesiredVoltage() {
        return desiredVoltage;
    }

    public void setDesiredVoltage(float desiredVoltage) {
        this.desiredVoltage = desiredVoltage;
    }

    /**
     * Maximum MVAR or voltage limit
     */
    public float getMaxReactivePowerOrVoltageLimit() {
        return maxReactivePowerOrVoltageLimit;
    }

    public void setMaxReactivePowerOrVoltageLimit(float maxReactivePowerOrVoltageLimit) {
        this.maxReactivePowerOrVoltageLimit = maxReactivePowerOrVoltageLimit;
    }

    /**
     * Minimum MVAR or voltage limit
     */
    public float getMinReactivePowerOrVoltageLimit() {
        return minReactivePowerOrVoltageLimit;
    }

    public void setMinReactivePowerOrVoltageLimit(float minReactivePowerOrVoltageLimit) {
        this.minReactivePowerOrVoltageLimit = minReactivePowerOrVoltageLimit;
    }

    /**
     * Shunt conductance G (per unit)
     */
    public float getShuntConductance() {
        return shuntConductance;
    }

    public void setShuntConductance(float shuntConductance) {
        this.shuntConductance = shuntConductance;
    }

    /**
     * Shunt susceptance B (per unit)
     */
    public float getShuntSusceptance() {
        return shuntSusceptance;
    }

    public void setShuntSusceptance(float shuntSusceptance) {
        this.shuntSusceptance = shuntSusceptance;
    }

    /**
     * Remote controlled bus number
     */
    public int getRemoteControlledBusNumber() {
        return remoteControlledBusNumber;
    }

    public void setRemoteControlledBusNumber(int remoteControlledBusNumber) {
        this.remoteControlledBusNumber = remoteControlledBusNumber;
    }
}
