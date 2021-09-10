/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface StaticVarCompensatorAdder extends InjectionAdder<StaticVarCompensatorAdder> {

    StaticVarCompensatorAdder setBmin(double bMin);

    StaticVarCompensatorAdder setBmax(double bMax);

    default StaticVarCompensatorAdder setVoltageSetpoint(double voltageSetpoint) {
        return setVoltageSetPoint(voltageSetpoint);
    }

    /**
     * @deprecated use {@link #setVoltageSetpoint(double voltageSetPoint)} instead.
     */
    @Deprecated
    default StaticVarCompensatorAdder setVoltageSetPoint(double voltageSetPoint) {
        return setVoltageSetpoint(voltageSetPoint);
    }

    default StaticVarCompensatorAdder setReactivePowerSetpoint(double reactivePowerSetpoint) {
        return setReactivePowerSetPoint(reactivePowerSetpoint);
    }

    /**
     * @deprecated use {@link #setReactivePowerSetpoint(double reactivePowerSetPoint)} instead.
     */
    @Deprecated
    default StaticVarCompensatorAdder setReactivePowerSetPoint(double reactivePowerSetPoint) {
        return setReactivePowerSetpoint(reactivePowerSetPoint);
    }

    StaticVarCompensatorAdder setRegulationMode(StaticVarCompensator.RegulationMode regulationMode);

    default StaticVarCompensatorAdder setRegulatingTerminal(Terminal regulatingTerminal) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * To express local regulation. When the adder.add() method was invoked,
     * the adder must set the regulating terminal to the terminal of the resulting staticVarCompensator added to the Network.
     */
    default StaticVarCompensatorAdder useLocalRegulation(boolean use) {
        return this; // does nothing
    }

    StaticVarCompensator add();
}
