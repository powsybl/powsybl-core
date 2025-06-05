/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * DC Line Commutated Converter, also called Current Source Converter
 *
 * <p> To create a DcLineCommutatedConverter, see {@link DcLineCommutatedConverterAdder}
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
 *             <td style="border: 1px solid black">The converter's point of common coupling (PCC) terminal</td>
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
 *             <td style="border: 1px solid black">ReactiveModel</td>
 *             <td style="border: 1px solid black">ReactiveModel</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The converter's reactive model</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">PowerFactor</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The power factor</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @see DcLineCommutatedConverterAdder
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface DcLineCommutatedConverter extends DcConverter<DcLineCommutatedConverter> {

    /**
     * LCC reactive power consumption model
     */
    enum ReactiveModel {
        /**
         * use a fixed configured power factor
         */
        FIXED_POWER_FACTOR,
        /**
         * use a calculated power factor
         */
        CALCULATED_POWER_FACTOR,
    }

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.DC_LINE_COMMUTATED_CONVERTER;
    }

    /**
     * @return the reactive model
     */
    ReactiveModel getReactiveModel();

    /**
     * @param reactiveModel new reactive model
     * @return self for method chaining
     */
    DcLineCommutatedConverter setReactiveModel(ReactiveModel reactiveModel);

    /**
     * Get power factor (ratio of the active power and the apparent power)
     * @return the power factor.
     */
    double getPowerFactor();

    /**
     * Set the power factor. Has to be greater that zero.
     * @param powerFactor the new power factor
     * @return self for method chaining
     */
    DcLineCommutatedConverter setPowerFactor(double powerFactor);
}
