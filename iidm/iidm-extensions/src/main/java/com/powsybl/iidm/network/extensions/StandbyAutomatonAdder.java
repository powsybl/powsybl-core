/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.StaticVarCompensator;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 */
public interface StandbyAutomatonAdder extends ExtensionAdder<StaticVarCompensator, StandbyAutomaton> {

    @Override
    default Class<StandbyAutomaton> getExtensionClass() {
        return StandbyAutomaton.class;
    }

    /**
     * Define the status of the automaton. Use true if in service, false otherwise.
     */
    StandbyAutomatonAdder withStandbyStatus(boolean standby);

    /**
     * Define the fix part of the susceptance (in S) used when the static var compensator is in stand by. Should be between the mininal
     * and the maximal susceptance of the static var compensator.
     */
    StandbyAutomatonAdder withB0(double b0);

    /**
     * Define the voltage setpoint (in kV) used when the high voltage threshold is reached.
     */
    StandbyAutomatonAdder withHighVoltageSetpoint(double highVoltageSetpoint);

    /**
     * @deprecated Use {@link #withHighVoltageSetpoint(double)} instead.
     */
    @Deprecated(since = "4.11.0")
    default StandbyAutomatonAdder withHighVoltageSetPoint(float highVoltageSetpoint) {
        return withHighVoltageSetpoint(highVoltageSetpoint);
    }

    /**
     * Define the high voltage threshold (in kV). Above this value, the static var compensator controls voltage at high voltage setpoint.
     */
    StandbyAutomatonAdder withHighVoltageThreshold(double highVoltageThreshold);

    /**
     * Define the voltage setpoint (in kV) used when the low voltage threshold is reached.
     */
    StandbyAutomatonAdder withLowVoltageSetpoint(double lowVoltageSetpoint);

    /**
     * @deprecated Use {@link #withLowVoltageSetpoint(double)} instead.
     */
    @Deprecated(since = "4.11.0")
    default StandbyAutomatonAdder withLowVoltageSetPoint(float lowVoltageSetpoint) {
        return withLowVoltageSetpoint(lowVoltageSetpoint);
    }

    /**
     * Define the low voltage threshold (in kV). Under this value, the static var compensator controls voltage at low voltage setpoint.
     */
    StandbyAutomatonAdder withLowVoltageThreshold(double lowVoltageThreshold);
}
