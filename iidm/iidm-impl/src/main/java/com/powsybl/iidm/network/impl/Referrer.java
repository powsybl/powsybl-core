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
public interface Referrer<T> {

    /**
     * Called when a referenced object is removed because of a connectable removal.
     * Implementations of this method should handle any required cleanup or updates
     * necessary when the referenced object is no longer part of the network.
     *
     * @param removedReferenced The referenced that has been removed from the network.
     */
    void onReferencedRemoval(T removedReferenced);

    /**
     * Called when a referenced object is replaced with another one. Implementations of this method
     * should handle any required updates or transfers necessary when the referenced object is
     * replaced.
     *
     * @param oldReferenced The original referenced object that is being replaced.
     * @param newReferenced The new referenced object that is taking the place of the old one.
     */
    void onReferencedReplacement(T oldReferenced, T newReferenced);
}
