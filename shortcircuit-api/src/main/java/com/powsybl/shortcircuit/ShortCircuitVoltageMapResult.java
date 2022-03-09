/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ShortCircuitVoltageMapResult {
    private String id;
    private double voltageValue;
    private double voltageAngle;
    private ValuesOnThreePhases voltageValuePerPhase;
    private ValuesOnThreePhases voltageAnglePerPhase;
    private ValuesOnThreePhases phaseToNeutralVoltageDropPerPhase; //in %
    private ValuesOnThreePhases voltageDropPerPhase;

    public ShortCircuitVoltageMapResult(String id, double voltageValue, double voltageAngle, ValuesOnThreePhases voltageValuePerPhase, ValuesOnThreePhases voltageAnglePerPhase, ValuesOnThreePhases phaseToNeutralVoltageDropPerPhase, ValuesOnThreePhases voltageDropPerPhase) {
        this.id = id;
        this.voltageValue = voltageValue;
        this.voltageAngle = voltageAngle;
        this.voltageValuePerPhase = voltageValuePerPhase;
        this.voltageAnglePerPhase = voltageAnglePerPhase;
        this.phaseToNeutralVoltageDropPerPhase = phaseToNeutralVoltageDropPerPhase;
        this.voltageDropPerPhase = voltageDropPerPhase;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public ValuesOnThreePhases getPhaseToNeutralVoltageDropPerPhase() {
        return phaseToNeutralVoltageDropPerPhase;
    }

    public void setPhaseToNeutralVoltageDropPerPhase(ValuesOnThreePhases phaseToNeutralVoltageDropPerPhase) {
        this.phaseToNeutralVoltageDropPerPhase = phaseToNeutralVoltageDropPerPhase;
    }

    public ValuesOnThreePhases getVoltageDropPerPhase() {
        return voltageDropPerPhase;
    }

    public void setVoltageDropPerPhase(ValuesOnThreePhases voltageDropPerPhase) {
        this.voltageDropPerPhase = voltageDropPerPhase;
    }
}
