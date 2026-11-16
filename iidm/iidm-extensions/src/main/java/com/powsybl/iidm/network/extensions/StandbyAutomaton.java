/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface StandbyAutomaton extends Extension<StaticVarCompensator> {

    String NAME = "standbyAutomaton";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Get the status of the automaton. Use true if in service, false otherwise.
     */
    boolean isStandby();

    StandbyAutomaton setStandby(boolean standby);

    /**
     * Get the fix part of the susceptance (in S) used when the static var compensator is in stand by. Should be between the mininal
     * and the maximal susceptance of the static var compensator.
     */
    double getB0();

    /**
     * Set the fix part of the susceptance (in S) used when the static var compensator is in stand by. Should be between the mininal
     * and the maximal susceptance of the static var compensator.
     */
    StandbyAutomaton setB0(double b0);

    /**
     * Get the voltage setpoint (in kV) used when the high voltage threshold is reached.
     */
    double getHighVoltageSetpoint();

    /**
     * Set the voltage setpoint (in kV) used when the high voltage threshold is reached.
     */
    StandbyAutomaton setHighVoltageSetpoint(double highVoltageSetpoint);

    /**
     * Get the high voltage threshold (in kV). Above this value, the static var compensator controls voltage at high voltage setpoint.
     */
    double getHighVoltageThreshold();

    /**
     * Set the high voltage threshold (in kV). Above this value, the static var compensator controls voltage at high voltage setpoint.
     */
    StandbyAutomaton setHighVoltageThreshold(double highVoltageThreshold);

    /**
     * Get the voltage setpoint (in kV) used when the low voltage threshold is reached.
     */
    double getLowVoltageSetpoint();

    /**
     * Set the voltage setpoint (in kV) used when the low voltage threshold is reached.
     */
    StandbyAutomaton setLowVoltageSetpoint(double lowVoltageSetpoint);

    /**
     * Get the low voltage threshold (in kV). Under this value, the static var compensator controls voltage at low voltage setpoint.
     */
    double getLowVoltageThreshold();

    /**
     * Set the low voltage threshold (in kV). Under this value, the static var compensator controls voltage at low voltage setpoint.
     */
    StandbyAutomaton setLowVoltageThreshold(double lowVoltageThreshold);
}
