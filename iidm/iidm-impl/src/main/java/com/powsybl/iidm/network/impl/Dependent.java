/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Dependent<T> {

    /**
     * Register an object so that {@link Dependent#onReferencedRemoval(Object)}  is called when
     * the connectable is removed from the network.
     *
     * @param referenced The referenced object to register.
     */
    void registerReferenced(T referenced);

    /**
     * Unregisters a previously registered terminal. This ensures that
     * {@link Dependent#onReferencedRemoval(Object)} won't be called anymore if the connectable
     * is removed from the network.
     *
     * @param referenced The referenced to unregister.
     */
    void unregisterReferenced(T referenced);

    /**
     * Called when a referenced terminal is removed because of a connectable removal.
     * Implementations of this method should handle any required cleanup or updates
     * necessary when the terminal is no longer part of the network.
     *
     * @param referenced The referenced that has been removed from the network.
     */
    void onReferencedRemoval(T referenced);
}
