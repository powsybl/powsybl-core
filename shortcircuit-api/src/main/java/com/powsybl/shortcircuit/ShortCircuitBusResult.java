/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ShortCircuitBusResult {
    private double threePhaseFaultCurrent;
    private double threePhaseFaultPower;
    private double timeConstant;
    private double faultResistance;
    private double faultReactance;
    private double currentValue;
    private double currentAngle;
    private double voltageValue;
    private double voltageAngle;
    private ValuesOnThreePhases currentValuePerPhase;
    private ValuesOnThreePhases currentAnglePerPhase;
    private ValuesOnThreePhases voltageValuePerPhase;
    private ValuesOnThreePhases voltageAnglePerPhase;

    private final List<ShortCircuitVoltageMapResult> shortCircuitVoltageMapResults = new ArrayList<>();
    private final List<SelectiveStudyFeederResult> feederResults = new ArrayList<>();

    public ShortCircuitBusResult(double threePhaseFaultCurrent, double threePhaseFaultPower, double timeConstant, double faultResistance, double faultReactance, double currentValue, double currentAngle, double voltageValue, double voltageAngle, ValuesOnThreePhases currentValuePerPhase, ValuesOnThreePhases currentAnglePerPhase, ValuesOnThreePhases voltageValuePerPhase, ValuesOnThreePhases voltageAnglePerPhase) {
        this.threePhaseFaultCurrent = threePhaseFaultCurrent;
        this.threePhaseFaultPower = threePhaseFaultPower;
        this.timeConstant = timeConstant;
        this.faultResistance = faultResistance;
        this.faultReactance = faultReactance;
        this.currentValue = currentValue;
        this.currentAngle = currentAngle;
        this.voltageValue = voltageValue;
        this.voltageAngle = voltageAngle;
        this.currentValuePerPhase = currentValuePerPhase;
        this.currentAnglePerPhase = currentAnglePerPhase;
        this.voltageValuePerPhase = voltageValuePerPhase;
        this.voltageAnglePerPhase = voltageAnglePerPhase;
    }

    public double getThreePhaseFaultCurrent() {
        return threePhaseFaultCurrent;
    }

    public void setThreePhaseFaultCurrent(double threePhaseFaultCurrent) {
        this.threePhaseFaultCurrent = threePhaseFaultCurrent;
    }

    public double getThreePhaseFaultPower() {
        return threePhaseFaultPower;
    }

    public void setThreePhaseFaultPower(double threePhaseFaultPower) {
        this.threePhaseFaultPower = threePhaseFaultPower;
    }

    public double getTimeConstant() {
        return timeConstant;
    }

    public void setTimeConstant(double timeConstant) {
        this.timeConstant = timeConstant;
    }

    public double getFaultResistance() {
        return faultResistance;
    }

    public void setFaultResistance(double faultResistance) {
        this.faultResistance = faultResistance;
    }

    public double getFaultReactance() {
        return faultReactance;
    }

    public void setFaultReactance(double faultReactance) {
        this.faultReactance = faultReactance;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getCurrentAngle() {
        return currentAngle;
    }

    public void setCurrentAngle(double currentAngle) {
        this.currentAngle = currentAngle;
    }

    public double getVoltageValue() {
        return voltageValue;
    }

    public void setVoltageValue(double voltageValue) {
        this.voltageValue = voltageValue;
    }

    public double getVoltageAngle() {
        return voltageAngle;
    }

    public void setVoltageAngle(double voltageAngle) {
        this.voltageAngle = voltageAngle;
    }

    public ValuesOnThreePhases getCurrentValuePerPhase() {
        return currentValuePerPhase;
    }

    public void setCurrentValuePerPhase(ValuesOnThreePhases currentValuePerPhase) {
        this.currentValuePerPhase = currentValuePerPhase;
    }

    public ValuesOnThreePhases getCurrentAnglePerPhase() {
        return currentAnglePerPhase;
    }

    public void setCurrentAnglePerPhase(ValuesOnThreePhases currentAnglePerPhase) {
        this.currentAnglePerPhase = currentAnglePerPhase;
    }

    public ValuesOnThreePhases getVoltageValuePerPhase() {
        return voltageValuePerPhase;
    }

    public void setVoltageValuePerPhase(ValuesOnThreePhases voltageValuePerPhase) {
        this.voltageValuePerPhase = voltageValuePerPhase;
    }

    public ValuesOnThreePhases getVoltageAnglePerPhase() {
        return voltageAnglePerPhase;
    }

    public void setVoltageAnglePerPhase(ValuesOnThreePhases voltageAnglePerPhase) {
        this.voltageAnglePerPhase = voltageAnglePerPhase;
    }

    public List<ShortCircuitVoltageMapResult> getShortCircuitVoltageMapResults() {
        return shortCircuitVoltageMapResults;
    }

    public List<SelectiveStudyFeederResult> getFeederResults() {
        return feederResults;
    }
}
