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
public class SelectiveStudyFeederResult {
    private String busId;
    private String connectableId;
    private double feederThreePhaseCurrent;
    private double feederThreePhaseVoltage;
    private ValuesOnThreePhases feederCurrentPerPhase;
    private ValuesOnThreePhases feederVoltagePerPhase;
    private double voltageDrop;

    public SelectiveStudyFeederResult(String busId, String connectableId, double feederThreePhaseCurrent, double feederThreePhaseVoltage, ValuesOnThreePhases feederCurrentPerPhase, ValuesOnThreePhases feederVoltagePerPhase, double voltageDrop) {
        this.busId = busId;
        this.connectableId = connectableId;
        this.feederThreePhaseCurrent = feederThreePhaseCurrent;
        this.feederThreePhaseVoltage = feederThreePhaseVoltage;
        this.feederCurrentPerPhase = feederCurrentPerPhase;
        this.feederVoltagePerPhase = feederVoltagePerPhase;
        this.voltageDrop = voltageDrop;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getConnectableId() {
        return connectableId;
    }

    public void setConnectableId(String connectableId) {
        this.connectableId = connectableId;
    }

    public double getFeederThreePhaseCurrent() {
        return feederThreePhaseCurrent;
    }

    public void setFeederThreePhaseCurrent(double feederThreePhaseCurrent) {
        this.feederThreePhaseCurrent = feederThreePhaseCurrent;
    }

    public double getFeederThreePhaseVoltage() {
        return feederThreePhaseVoltage;
    }

    public void setFeederThreePhaseVoltage(double feederThreePhaseVoltage) {
        this.feederThreePhaseVoltage = feederThreePhaseVoltage;
    }

    public ValuesOnThreePhases getFeederCurrentPerPhase() {
        return feederCurrentPerPhase;
    }

    public void setFeederCurrentPerPhase(ValuesOnThreePhases feederCurrentPerPhase) {
        this.feederCurrentPerPhase = feederCurrentPerPhase;
    }

    public ValuesOnThreePhases getFeederVoltagePerPhase() {
        return feederVoltagePerPhase;
    }

    public void setFeederVoltagePerPhase(ValuesOnThreePhases feederVoltagePerPhase) {
        this.feederVoltagePerPhase = feederVoltagePerPhase;
    }

    public double getVoltageDrop() {
        return voltageDrop;
    }

    public void setVoltageDrop(double voltageDrop) {
        this.voltageDrop = voltageDrop;
    }
}
