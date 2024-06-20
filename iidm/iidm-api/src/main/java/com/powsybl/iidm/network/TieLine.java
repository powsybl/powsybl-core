/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.List;
import java.util.function.Predicate;

/**
 * A tie line is an AC line sharing power between two neighbouring regional grids. It is constituted of two {@link DanglingLine}
 * <p>
 * The tie line is always oriented in the same way, <br>
 * The network model node of the danglingLine1 is always at end 1. <br>
 * The network model node of the danglingLine2 is always at end 2. <br>
 * </p>
 * As there is no injection at the boundary node, by applying kron reduction, this node can be
 * removed getting an equivalent branch between both network model nodes.
 * </p>
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
 *             <td style="border: 1px solid black">Unique identifier of the tie line</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the tie line</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">DanglingLine1</td>
 *             <td style="border: 1px solid black">DanglingLine</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first half of the line characteristics</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">DanglingLine2</td>
 *             <td style="border: 1px solid black">DanglingLine</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second half of the line characteristics</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>
 * A tie line is created by matching two {@link DanglingLine} with the same pairing key. <br>
 * We have two Dangling Lines within the Tie Line. <br>
 * </p>
 *
 * <p>
 * In the CGMES import of an assembled model : <br>
 * A tie line is created by matching two links with the same boundary node. <br>
 * Each link can be: <br>
 * <ul>
 <li>A line.</li>
 <li>A Breaker or Disconnector.</li>
 <li>An equivalent branch.</li>
 <li>A transformer with fixed ratio and zero phase shift angle.</li>
</ul>
 * </p>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public interface TieLine extends Branch<TieLine>, LineCharacteristics {

    /**
     * Get the pairing key corresponding to this tie line in the case where the
     * line is a boundary, return null otherwise.
     */
    String getPairingKey();

    /**
     * Get first dangling line of this tie line
     */
    DanglingLine getDanglingLine1();

    /**
     * Get second dangling line of this tie line
     */
    DanglingLine getDanglingLine2();

    /**
     * Get the dangling line of this tie line corresponding to the given side
     */
    DanglingLine getDanglingLine(TwoSides side);

    /**
     * Get the dangling line of this tie line corresponding to the given voltage level
     */
    DanglingLine getDanglingLine(String voltageLevelId);

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.TIE_LINE;
    }

    void remove();

    /**
     * Remove the tie line with an update of underlying dangling lines to reflect the tie line flows.
     */
    void remove(boolean updateDanglingLines);

    boolean connect();

    boolean connect(Predicate<Switch> isTypeSwitchToOperate);

    boolean connect(Predicate<Switch> isTypeSwitchToOperate, ThreeSides side);

    boolean disconnect();

    boolean disconnect(Predicate<Switch> isSwitchOpenable);

    boolean disconnect(Predicate<Switch> isSwitchOpenable, ThreeSides side);

    List<Terminal> getTerminals(ThreeSides side);

    Network getNetwork();
}
