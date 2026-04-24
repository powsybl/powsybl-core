/**
 * Copyright (c) 2016-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

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
 *             <th style="border: 1px solid black">Default value</th>
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface StaticVarCompensator extends Injection<StaticVarCompensator>, VoltageRegulationHolder {

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
     * @deprecated use {@link #getRegulatingTargetV()} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    double getVoltageSetpoint();

    /**
     * <p>Set the voltage setpoint in Kv.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#VOLTAGE}.</p>
     * <p>Depends on the working variant.</p>
     * @param voltageSetpoint the voltage setpoint
     * @return this to allow method chaining
     * @deprecated use {@link VoltageRegulation#setTargetValue(double)} and {@link VoltageRegulation#setMode(RegulationMode)} with {@link RegulationMode#VOLTAGE} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    StaticVarCompensator setVoltageSetpoint(double voltageSetpoint);

    StaticVarCompensator setTargetQ(double targetQ);

    StaticVarCompensator setTargetV(double targetV);

    /**
     * <p>Get the reactive power setpoint in MVAR.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#REACTIVE_POWER}.</p>
     * <p>Depends on the working variant.</p>
     * @return the reactive power setpoint
     * @deprecated use {@link #getRegulatingTargetQ()} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    double getReactivePowerSetpoint();

    /**
     * <p>Set the reactive power setpoint in MVAR.</p>
     * <p>Needed only when regulating mode is set to {@link RegulationMode#REACTIVE_POWER}.</p>
     * <p>Depends on the working variant.</p>
     * @param reactivePowerSetpoint the reactive power setpoint
     * @return this to allow method chaining
     * @deprecated use {@link VoltageRegulation#setTargetValue(double)} and {@link VoltageRegulation#setMode(RegulationMode)} with {@link RegulationMode#REACTIVE_POWER} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    StaticVarCompensator setReactivePowerSetpoint(double reactivePowerSetpoint);

    /**
     * <p>Get the regulating mode.</p>
     * <p>Depends on the working variant.</p>
     * @return the regulating mode
     * @deprecated use {@link VoltageRegulation#getMode()} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    RegulationMode getRegulationMode();

    /**
     * <p>set the regulating mode.</p>
     * <p>Depends on the working variant.</p>
     * @param regulationMode the regulating mode
     * @return this to allow method chaining
     * @deprecated use {@link VoltageRegulation#setMode(RegulationMode)} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    StaticVarCompensator setRegulationMode(RegulationMode regulationMode);

    /**
     * Get the regulating status.
     * @deprecated use {@link VoltageRegulation#isRegulating()} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    boolean isRegulating();

    /**
     * Set the regulating status.
     * @deprecated use {@link VoltageRegulation#setRegulating(boolean)} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    StaticVarCompensator setRegulating(boolean regulating);

    /**
     * <p>Set the terminal used for regulation.</p>
     * @return this to allow method chaining
     * @deprecated use {@link VoltageRegulation#setTerminal(Terminal)} instead
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    default StaticVarCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.STATIC_VAR_COMPENSATOR;
    }
}
