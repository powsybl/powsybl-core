/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.util.SwitchPredicates;

import java.util.function.Predicate;

/**
 * A tie line is an AC line sharing power between two neighbouring regional grids. It is constituted of two {@link BoundaryLine}
 * <p>
 * The tie line is always oriented in the same way, <br>
 * The network model node of the boundaryLine1 is always at end 1. <br>
 * The network model node of the boundaryLine2 is always at end 2. <br>
 * </p>
 * As there is no injection at the boundary node, by applying kron reduction, this node can be
 * removed getting an equivalent branch between both network model nodes.
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
 *             <td style="border: 1px solid black">BoundaryLine1</td>
 *             <td style="border: 1px solid black">BoundaryLine</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first half of the line characteristics</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">BoundaryLine2</td>
 *             <td style="border: 1px solid black">BoundaryLine</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second half of the line characteristics</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>
 * A tie line is created by matching two {@link BoundaryLine} with the same pairing key. <br>
 * We have two Boundary Lines within the Tie Line. <br>
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
     * Get first boundary line of this tie line
     */
    BoundaryLine getBoundaryLine1();

    /**
     * Get second boundary line of this tie line
     */
    BoundaryLine getBoundaryLine2();

    /**
     * Get the boundary line of this tie line corresponding to the given side
     */
    BoundaryLine getBoundaryLine(TwoSides side);

    /**
     * Get the boundary line of this tie line corresponding to the given voltage level
     */
    BoundaryLine getBoundaryLine(String voltageLevelId);

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.TIE_LINE;
    }

    void remove();

    /**
     * Remove the tie line with an update of underlying boundary lines to reflect the tie line flows.
     */
    void remove(boolean updateBoundaryLines);

    /**
     * Try to connect the two boundary lines of the tie line.<br/>
     * By default, this method only operates on non-fictitious breakers. If you wish to operate on other switches,
     * use {@link #connectBoundaryLines(Predicate)} with another specific {@link com.powsybl.iidm.network.util.SwitchPredicates}
     * such as {@link com.powsybl.iidm.network.util.SwitchPredicates#IS_BREAKER}
     * @return true if the boundary lines have been connected by this operation, false otherwise (any of the two boundary lines could not be connected, or was already connected)
     */
    default boolean connectBoundaryLines() {
        return connectBoundaryLines(SwitchPredicates.IS_NONFICTIONAL_BREAKER);
    }

    /**
     * Try to connect the two boundary lines of the tie-line by operating the switches matching the <code>isSwitchOpenable</code>
     * predicate.
     * @param isTypeSwitchToOperate which switches to operate on to connect the boundary lines of the tie-line.
     * @return true if both boundary lines were connected by this operation, false otherwise (any of the two boundary lines could not be connected, or was already connected)
     */
    default boolean connectBoundaryLines(Predicate<Switch> isTypeSwitchToOperate) {
        return connectBoundaryLines(isTypeSwitchToOperate, null);
    }

    /**
     * Try to connect the boundary line on <code>side</code> by operating the switches matching the predicate. No operation
     * should be performed if the connection is not possible.
     * @param isTypeSwitchToOperate the type of switch to operate
     * @param side the side of the tie line to connect. If the side is null, both sides should be connected.
     * @return true if the connection on <code>side</code> by this operation succeeded, false otherwise (the boundary line
     * on the given side could not be connected, or was already connected)
     */
    boolean connectBoundaryLines(Predicate<Switch> isTypeSwitchToOperate, TwoSides side);

    /**
     * Try to disconnect the two boundary lines of the tie line.<br/>
     * By default, this method only operates on non-fictitious breakers. If you wish to operate on other switches,
     * use {@link #disconnectBoundaryLines(Predicate)} with another specific {@link com.powsybl.iidm.network.util.SwitchPredicates}
     * such as {@link com.powsybl.iidm.network.util.SwitchPredicates#IS_CLOSED_BREAKER}
     * @return true if the boundary lines have been disconnected by this operation, false otherwise (any of the two boundary
     * lines could not be disconnected, or was already disconnected)
     */
    default boolean disconnectBoundaryLines() {
        return disconnectBoundaryLines(SwitchPredicates.IS_NONFICTIONAL_CLOSED_BREAKER);
    }

    /**
     * Try to disconnect the two boundary lines of the tie line by operating the switches matching the <code>isSwitchOpenable</code>
     * predicate.
     * @param isSwitchOpenable which switches to operate on to disconnect the boundary lines of the tie-line.
     * @return true if both boundary lines were disconnected by this operation, false otherwise (any of the two boundary lines could not be disconnected, or was already disconnected)
     */
    default boolean disconnectBoundaryLines(Predicate<Switch> isSwitchOpenable) {
        return disconnectBoundaryLines(isSwitchOpenable, null);
    }

    /**
     * Try to disconnect the boundary line on <code>side</code> by operating the switches matching the predicate. No operation
     * should be performed if the disconnection is not possible.
     * @param isSwitchOpenable the type of switch to operate
     * @param side the side of the tie line to disconnect. If the side is null, both sides should be disconnected.
     * @return true if the disconnection on <code>side</code> by this operation succeeded, false otherwise (the boundary line on the given side could not be disconnected, or was already disconnected)
     */
    boolean disconnectBoundaryLines(Predicate<Switch> isSwitchOpenable, TwoSides side);

    Network getNetwork();
}
