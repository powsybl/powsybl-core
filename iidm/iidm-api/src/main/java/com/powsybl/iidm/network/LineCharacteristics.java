/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * A line with mutable characteristics.
 *
 * <p>
 *  Characteristics
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *            <tr>
 *                <td style="border: 1px solid black">G1</td>
 *                <td style="border: 1px solid black">double</td>
 *                <td style="border: 1px solid black">S</td>
 *                <td style="border: 1px solid black">yes</td>
 *                <td style="border: 1px solid black"> - </td>
 *                <td style="border: 1px solid black">The first side shunt conductance</td>
 *            </tr>
 *            <tr>
 *                <td style="border: 1px solid black">B1</td>
 *                <td style="border: 1px solid black">double</td>
 *                <td style="border: 1px solid black">S</td>
 *                <td style="border: 1px solid black">yes</td>
 *                <td style="border: 1px solid black"> - </td>
 *                <td style="border: 1px solid black">The first side shunt susceptance</td>
 *            </tr>
 *            <tr>
 *                <td style="border: 1px solid black">G2</td>
 *                <td style="border: 1px solid black">double</td>
 *                <td style="border: 1px solid black">S</td>
 *                <td style="border: 1px solid black">yes</td>
 *                <td style="border: 1px solid black"> - </td>
 *                <td style="border: 1px solid black">The second side shunt conductance</td>
 *            </tr>
 *            <tr>
 *                <td style="border: 1px solid black">B2</td>
 *                <td style="border: 1px solid black">double</td>
 *                <td style="border: 1px solid black">S</td>
 *                <td style="border: 1px solid black">yes</td>
 *                <td style="border: 1px solid black"> - </td>
 *                <td style="border: 1px solid black">The second side shunt susceptance</td>
 *            </tr>
 *     </tbody>
 * </table>
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface LineCharacteristics {

    /**
     * Get the first side shunt conductance in S.
     */
    double getG1();

    /**
     * Get the second side shunt conductance in S.
     */
    double getG2();

    /**
     * Get the first side shunt susceptance in S.
     */
    double getB1();

    /**
     * Get the second side shunt susceptance in S.
     */
    double getB2();
}
