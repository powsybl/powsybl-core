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
    private double voltageValuePhase1;
    private double voltageAnglePhase1;
    private double voltageValuePhase2;
    private double voltageAnglePhase2;
    private double voltageValuePhase3;
    private double voltageAnglePhase3;
    private double phaseToNeutralVoltageDropPhase1; //in %
    private double phaseToNeutralVoltageDropPhase2;
    private double phaseToNeutralVoltageDropPhase3;
    private double voltageDropPhase12;
    private double voltageDropPhase23;
    private double voltageDropPhase31;

}
