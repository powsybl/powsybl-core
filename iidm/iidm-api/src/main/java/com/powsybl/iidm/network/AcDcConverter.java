/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * AcDcConverter is the base interface for LineCommutatedConverter and VoltageSourceConverter.
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
 *             <td style="border: 1px solid black">The converter's control mode: P_PCC or V_DC</td>
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
 *     </tbody>
 * </table>
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface AcDcConverter<I extends AcDcConverter<I>> extends Connectable<I>, DcConnectable<I> {

    /**
     * Control Mode of the DC converter
     */
    enum ControlMode {
        /**
         * Controlling active power at Point of Common Coupling
         */
        P_PCC,
        /**
         * Controlling DC Voltage
         */
        V_DC
    }

    /**
     * Get the first AC terminal.
     */
    Terminal getTerminal1();

    /**
     * Get the optional second AC terminal.
     */
    Optional<Terminal> getTerminal2();

    /**
     * Get the side the AC terminal is connected to.
     */
    TwoSides getSide(Terminal terminal);

    /**
     * Get the AC terminal at provided side.
     */
    Terminal getTerminal(TwoSides side);

    /**
     * Get the first DC terminal.
     */
    DcTerminal getDcTerminal1();

    /**
     * Get the second DC terminal.
     */
    DcTerminal getDcTerminal2();

    /**
     * Get the side the DC terminal is connected to.
     */
    TwoSides getSide(DcTerminal dcTerminal);

    /**
     * Get the DC terminal at provided side.
     */
    DcTerminal getDcTerminal(TwoSides side);

    /**
     * Set the idle loss (MW).
     */
    I setIdleLoss(double idleLoss);

    /**
     * Get the idle loss (MW).
     */
    double getIdleLoss();

    /**
     * Set the switching loss (MW/A).
     */
    I setSwitchingLoss(double switchingLoss);

    /**
     * Get the switching loss (MW/A).
     */
    double getSwitchingLoss();

    /**
     * Set the resistive loss (&#937;).
     */
    I setResistiveLoss(double resistiveLoss);

    /**
     * Get the resistive loss (&#937;).
     */
    double getResistiveLoss();

    /**
     * Set the point of common coupling terminal
     */
    I setPccTerminal(Terminal pccTerminal);

    /**
     * Get the point of common coupling terminal
     */
    Terminal getPccTerminal();

    /**
     * Set the control mode of the converter
     */
    I setControlMode(ControlMode controlMode);

    /**
     * Get the control mode of the converter
     */
    ControlMode getControlMode();

    /**
     * Set the target active power at point of common coupling (MW)
     */
    I setTargetP(double targetP);

    /**
     * Get the target active power at point of common coupling (MW)
     */
    double getTargetP();

    /**
     * Set the target DC voltage (kV DC)
     */
    I setTargetVdc(double targetVdc);

    /**
     * Get the target DC voltage (kV DC)
     */
    double getTargetVdc();
}
