/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * AC/DC Voltage Source Converter
 *
 * <p> To create a VoltageSourceConverter, see {@link VoltageSourceConverterAdder}
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
 *             <td style="border: 1px solid black">Unique identifier of the Converter</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the Converter</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">IdleLoss</td>
 *             <td style="border: 1px solid black">Double</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black">0 MW</td>
 *             <td style="border: 1px solid black">Converter Idle loss</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">SwitchingLoss</td>
 *             <td style="border: 1px solid black">Double</td>
 *             <td style="border: 1px solid black">MW/A</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black">0 MW/A</td>
 *             <td style="border: 1px solid black">Converter Switching loss</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ResistiveLoss</td>
 *             <td style="border: 1px solid black">Double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black">0 &Omega;</td>
 *             <td style="border: 1px solid black">Converter Resistive loss</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">PccTerminal</td>
 *             <td style="border: 1px solid black">Terminal</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The converter's point of common coupling (PCC) AC terminal</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ControlMode</td>
 *             <td style="border: 1px solid black">ControlMode</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The converter's control mode</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">TargetP</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Active power target at point of common coupling, load sign convention</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">TargetVdc</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">DC voltage target</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">VoltageRegulatorOn</td>
 *             <td style="border: 1px solid black">boolean</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">True if the converter regulates voltage</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">VoltageSetpoint</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">only if VoltageRegulatorOn is set to true</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The AC voltage setpoint</td>
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
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface VoltageSourceConverter extends AcDcConverter<VoltageSourceConverter>, ReactiveLimitsHolder {

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.VOLTAGE_SOURCE_CONVERTER;
    }

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
    VoltageSourceConverter setVoltageRegulatorOn(boolean voltageRegulatorOn);

    /**
     * Get the AC voltage setpoint (kV).
     * @return the voltage setpoint
     */
    double getVoltageSetpoint();

    /**
     * Set the AC voltage setpoint (kV).
     * @param voltageSetpoint the voltage setpoint
     * @return the converter itself to allow method chaining
     */
    VoltageSourceConverter setVoltageSetpoint(double voltageSetpoint);

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
    VoltageSourceConverter setReactivePowerSetpoint(double reactivePowerSetpoint);
}
