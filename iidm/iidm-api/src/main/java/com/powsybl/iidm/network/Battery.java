/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A battery system.
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
 *             <td style="border: 1px solid black">Unique identifier of the battery</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the battery</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">P0</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The constant active power</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Q0</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MVar</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The constant reactive power</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">MinP</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The minimum active power</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">MaxP</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The maximum active power</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>
 * To create a battery, see {@link BatteryAdder}
 *
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 * @see BatteryAdder
 * @see MinMaxReactiveLimits
 * @see ReactiveCapabilityCurve
 */
public interface Battery extends Injection<Battery>, ReactiveLimitsHolder {

    /**
     * Get the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getP0();

    /**
     * Set the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    Battery setP0(double p0);

    /**
     * Get the constant reactive power in MVar.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getQ0();

    /**
     * Set the constant reactive power in MVar.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    Battery setQ0(double q0);

    /**
     * Get the minimal active power in MW.
     */
    double getMinP();

    /**
     * Set the minimal active power in MW.
     */
    Battery setMinP(double minP);

    /**
     * Get the maximal active power in MW.
     */
    double getMaxP();

    /**
     * Set the maximal active power in MW.
     */
    Battery setMaxP(double maxP);
}
