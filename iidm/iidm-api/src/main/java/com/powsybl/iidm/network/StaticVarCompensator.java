/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * Static VAR compensator model.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface StaticVarCompensator extends Injection<StaticVarCompensator> {

    enum RegulationMode {
        VOLTAGE,
        REACTIVE_POWER,
        OFF
    }

    /**
     * Get the minimum susceptance in S.
     * @return the minimum susceptance
     */
    double getBmin();

    /**
     * Set the minimum susceptance in S.
     * @param bMin minimum susceptance
     * @return this to allow method chaining
     */
    StaticVarCompensator setBmin(double bMin);

    /**
     * Get the maximum susceptance in S.
     * @return the maximum susceptance
     */
    double getBmax();

    /**
     * Set the maximum susceptance in S.
     * @param bMax the maximum susceptance
     * @return this to allow method chaining
     */
    StaticVarCompensator setBmax(double bMax);

    /**
     * <p>Get the voltage setpoint in Kv.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#VOLTAGE}.</p>
     * <p>Depends on the working variant.</p>
     * @return the voltage setpoint
     */
    double getVoltageSetPoint();

    /**
     * <p>Set the voltage setpoint in Kv.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#VOLTAGE}.</p>
     * <p>Depends on the working variant.</p>
     * @param voltageSetPoint the voltage setpoint
     * @return this to allow method chaining
     */
    StaticVarCompensator setVoltageSetPoint(double voltageSetPoint);

    /**
     * <p>Get the reactive power setpoint in MVAR.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#REACTIVE_POWER}.</p>
     * <p>Depends on the working variant.</p>
     * @return the reactive power setpoint
     */
    double getReactivePowerSetPoint();

    /**
     * <p>Set the reactive power setpoint in MVAR.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#REACTIVE_POWER}.</p>
     * <p>Depends on the working variant.</p>
     * @param reactivePowerSetPoint the reactive power setpoint
     * @return this to allow method chaining
     */
    StaticVarCompensator setReactivePowerSetPoint(double reactivePowerSetPoint);

    /**
     * <p>Get the regulating mode.</p>
     * <p>Depends on the working variant.</p>
     * @return the regulating mode
     */
    RegulationMode getRegulationMode();

    /**
     * <p>set the regulating mode.</p>
     * <p>Depends on the working variant.</p>
     * @param regulationMode the regulating mode
     * @return this to allow method chaining
     */
    StaticVarCompensator setRegulationMode(RegulationMode regulationMode);

    /**
     * <p>Get the terminal used for regulation.</p>
     * @return the terminal used for regulation
     */
    default Terminal getRegulatingTerminal() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * <p>Set the terminal used for regulation.</p>
     * @return this to allow method chaining
     */
    default StaticVarCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
