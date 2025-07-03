/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * VSC converter station.
 *
 * <p>
 * Characteristics
 * </p>
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
 *             <td style="border: 1px solid black">Unique identifier of the VSC converter station</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the VSC converter station</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">VoltageRegulatorOn</td>
 *             <td style="border: 1px solid black">boolean</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The voltage regulator status</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">VoltageSetpoint</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">only if VoltageRegulatorOn is set to true</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The voltage setpoint</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ReactivePowerSetpoint</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MVar</td>
 *             <td style="border: 1px solid black">only if VoltageRegulatorOn is set to false</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">The reactive power setpoint</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface VscConverterStation extends HvdcConverterStation<VscConverterStation>, ReactiveLimitsHolder {

    /**
     * Check if voltage regulator is on.
     * @return true if voltage regulator is on, false otherwise
     */
    boolean isVoltageRegulatorOn();

    /**
     * Set voltage regulator status.
     * @param voltageRegulatorOn the new voltage regulator status
     * @return the converter itself to allow method chaining
     */
    VscConverterStation setVoltageRegulatorOn(boolean voltageRegulatorOn);

    /**
     * Get the voltage setpoint (kV).
     * @return the voltage setpoint
     */
    double getVoltageSetpoint();

    /**
     * Set the voltage setpoint (kV).
     * @param voltageSetpoint the voltage setpoint
     * @return the converter itself to allow method chaining
     */
    VscConverterStation setVoltageSetpoint(double voltageSetpoint);

    /**
     * Get the reactive power setpoint (MVar).
     * @return the reactive power setpoint
     */
    double getReactivePowerSetpoint();

    /**
     * Set the reactive power setpoint (MVar).
     * @param reactivePowerSetpoint the reactive power setpoint
     * @return the converter itself to allow method chaining
     */
    VscConverterStation setReactivePowerSetpoint(double reactivePowerSetpoint);

    /**
     * Get the terminal used for regulation.
     * @return the terminal used for regulation
     */
    default Terminal getRegulatingTerminal() {
        return this.getTerminal();
    }

    default VscConverterStation setRegulatingTerminal(Terminal regulatingTerminal) {
        return this;
    }
}
