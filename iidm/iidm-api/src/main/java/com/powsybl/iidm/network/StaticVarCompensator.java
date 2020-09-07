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
 * <p>
 *  Characteristics
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Defaut value</th>
 *             <th style="border: 1px solid black">Description</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td style="border: 1px solid black">Id</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Unique identifier of the static VAR compensator</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the static VAR compensator</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Bmin</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The minimum susceptance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Bmax</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The maximum susceptance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">VoltageSetPoint</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">only if RegulationMode is set to VOLTAGE</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The voltage setpoint</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ReactivePowerSetpoint</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MVar</td>
 *             <td style="border: 1px solid black">only if RegulationMode is set to REACTIVE_POWER</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The reactive power setpoint</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">RegulatingTerminal</td>
 *             <td style="border: 1px solid black">Terminal</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> The static VAR compensator's terminal </td>
 *             <td style="border: 1px solid black">The terminal used for regulation</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">RegulationMode</td>
 *             <td style="border: 1px solid black">RegulationMode</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The regulation mode</td>
 *         </tr>
 *     </tbody>
 * </table>
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
    default double getVoltageSetpoint() {
        return getVoltageSetPoint();
    }

    /**
     * @deprecated use {@link #getVoltageSetpoint()} instead.
     */
    @Deprecated
    default double getVoltageSetPoint() {
        return getVoltageSetpoint();
    }

    /**
     * <p>Set the voltage setpoint in Kv.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#VOLTAGE}.</p>
     * <p>Depends on the working variant.</p>
     * @param voltageSetpoint the voltage setpoint
     * @return this to allow method chaining
     */
    default StaticVarCompensator setVoltageSetpoint(double voltageSetpoint) {
        return setVoltageSetPoint(voltageSetpoint);
    }

    /**
     * @deprecated use {@link #setVoltageSetpoint(double voltageSetpoint)} instead.
     */
    @Deprecated
    default StaticVarCompensator setVoltageSetPoint(double voltageSetPoint) {
        return setVoltageSetpoint(voltageSetPoint);
    }

    /**
     * <p>Get the reactive power setpoint in MVAR.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#REACTIVE_POWER}.</p>
     * <p>Depends on the working variant.</p>
     * @return the reactive power setpoint
     */
    default double getReactivePowerSetpoint() {
        return getReactivePowerSetPoint();
    }

    /**
     * @deprecated use {@link #getReactivePowerSetpoint()} instead.
     */
    @Deprecated
    default double getReactivePowerSetPoint() {
        return getReactivePowerSetpoint();
    }

    /**
     * <p>Set the reactive power setpoint in MVAR.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#REACTIVE_POWER}.</p>
     * <p>Depends on the working variant.</p>
     * @param reactivePowerSetpoint the reactive power setpoint
     * @return this to allow method chaining
     */
    default StaticVarCompensator setReactivePowerSetpoint(double reactivePowerSetpoint) {
        return setReactivePowerSetPoint(reactivePowerSetpoint);
    }

    /**
     * @deprecated use {@link #setReactivePowerSetpoint(double reactivePowerSetpoint)} instead.
     */
    @Deprecated
    default StaticVarCompensator setReactivePowerSetPoint(double reactivePowerSetPoint) {
        return setReactivePowerSetpoint(reactivePowerSetPoint);
    }

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
