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
     * By default, this method does not change the state of fictitious breakers. If you wish to do that, use
     * {@link #connect(Predicate)} with {@link com.powsybl.iidm.network.util.SwitchPredicates#IS_BREAKER}.
     */
    default boolean connect() {
        return connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER);
    }

    default boolean connect(Predicate<Switch> isTypeSwitchToOperate) {
        return connect(isTypeSwitchToOperate, null);
    }

    /**
     * Try to connect the connectable on the given <code>side</code> by operating switches that match the <code>isTypeSwitchToOperate</code>
     * predicate. No operation shall be performed if the connection is not possible.
     * @param isTypeSwitchToOperate a predicate to define which switches to operate to make the connection
     * @param side the of the connectable to connect. If this is null, all sides should be connected.
     * @return true if the connection succeeded, false otherwise.
     */
    boolean connect(Predicate<Switch> isTypeSwitchToOperate, ThreeSides side);

    /**
     * Disconnects the connectable by operating real breakers.<br>
     * By default, this method does not change the state of fictitious breakers. If you wish to do that, use
     * {@link #disconnect(Predicate)} with {@link com.powsybl.iidm.network.util.SwitchPredicates#IS_CLOSED_BREAKER}.
     */
    default boolean disconnect() {
        return disconnect(SwitchPredicates.IS_NONFICTIONAL_CLOSED_BREAKER);
    }

    default boolean disconnect(Predicate<Switch> isSwitchOpenable) {
        return disconnect(isSwitchOpenable, null);
    }

    /**
     * Try to disconnect the connectable on the given <code>side</code> by operating switches that match the <code>isTypeSwitchToOperate</code>
     * predicate. No operation shall be performed if the disconnection is not possible.
     * @param isSwitchOpenable a predicate to define which switches to operate to make the disconnection
     * @param side the of the connectable to disconnect. If this is null, all sides should be disconnected.
     * @return true if the disconnection succeeded, false otherwise.
     */
    boolean disconnect(Predicate<Switch> isSwitchOpenable, ThreeSides side);
}
