/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
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
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface MutableLineCharacteristics<T> extends LineCharacteristics {

    /**
     * Set the first side shunt conductance in S.
     */
    T setG1(double g1);

    /**
     * Set the second side shunt conductance in S.
     */
    T setG2(double g2);

    /**
     * Set the first side shunt susceptance in S.
     */
    T setB1(double b1);

    /**
     * Set the second side shunt susceptance in S.
     */
    T setB2(double b2);
}
