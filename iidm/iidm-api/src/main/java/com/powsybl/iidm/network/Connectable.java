/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.util.SwitchPredicates;

import java.util.List;
import java.util.function.Predicate;

/**
 * AC equipment that is part of a substation topology.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Connectable<I extends Connectable<I>> extends Identifiable<I> {

    List<? extends Terminal> getTerminals();

    /**
     * Remove the connectable from the voltage level (boundary switches are kept).
     */
    void remove();

    /**
     * Connects the connectable by operating real breakers.<br>
     * By default, this method only operates on non-fictitious breakers. If you wish to operate on other switches, use
     * {@link #connect(Predicate)} with another specific {@link com.powsybl.iidm.network.util.SwitchPredicates} such as {@link com.powsybl.iidm.network.util.SwitchPredicates#IS_BREAKER}.
     * @return true if the connectable has been connected by this operation, false otherwise (the connectable could not be connected, or it was already connected)
     */
    default boolean connect() {
        return connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER);
    }

    /**
     * Connects the connectable on all sides by operating the breakers that match the <code>isTypeSwitchToOperate</code> predicate.
     * @param isTypeSwitchToOperate which switch to operate to perform the connection
     * @return true if the connectable has been connected by this operation, false otherwise (the connectable could not be connected, or it was already connected)
     */
    default boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        return connect(isTypeSwitchToOperate, null);
    }

    /**
     * Try to connect the connectable on the given <code>side</code> by operating switches that match the <code>isTypeSwitchToOperate</code>
     * predicate. No operation shall be performed if the connection is not possible.
     * @param isTypeSwitchToOperate a predicate to define which switches to operate to make the connection
     * @param side the of the connectable to connect. If this is null, all sides should be connected.
     * @return true if the connection by this operation succeeded, false otherwise (the connectable on the given <code>side</code> could not be connected, or it was already connected)
     */
    boolean connect(Predicate<Switch> isTypeSwitchToOperate, ThreeSides side);

    /**
     * Disconnects the connectable by operating real breakers.<br>
     * By default, this method only operates on non-fictitious breakers. If you wish to operate on other switches, use
     * {@link #disconnect(Predicate)} with another specific {@link com.powsybl.iidm.network.util.SwitchPredicates} such as {@link com.powsybl.iidm.network.util.SwitchPredicates#IS_CLOSED_BREAKER}.
     * @return  true if the connectable has been disconnected by this operation, false otherwise (the connectable could not be disconnected, or it was already disconnected)
     */
    default boolean disconnect() {
        return disconnect(SwitchPredicates.IS_NONFICTIONAL_CLOSED_BREAKER);
    }

    /**
     * Disconnects the connectable on all sides by operating the breakers that match the <code>isTypeSwitchToOperate</code> predicate.
     * @param isSwitchOpenable which switch to operate to perform the disconnection
     * @return true if the connectable has been disconnected by this operation, false otherwise (the connectable could not be disconnected, or it was already disconnected)
     */
    default boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        return disconnect(isSwitchOpenable, null);
    }

    /**
     * Try to disconnect the connectable on the given <code>side</code> by operating switches that match the <code>isTypeSwitchToOperate</code>
     * predicate. No operation shall be performed if the disconnection is not possible.
     * @param isSwitchOpenable a predicate to define which switches to operate to make the disconnection
     * @param side the of the connectable to disconnect. If this is null, all sides should be disconnected.
     * @return true if the disconnection succeeded, false otherwise (the connectable on the given <code>side</code> could not be disconnected, or it was already disconnected)
     */
    boolean disconnect(Predicate<Switch> isSwitchOpenable, ThreeSides side);

    /**
     * Removes all loadflow output values of this connectable (e.g., P and Q on terminals).
     */
    default void unsetSolvedValues() {
        this.getTerminals().forEach(Terminal::unsetSolvedValues);
    }
}
