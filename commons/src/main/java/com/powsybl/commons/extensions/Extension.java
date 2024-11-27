/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

/**
 * Extension data for extendables.
 *
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface Extension<T> {

    /**
     * Return the name of this extension.
     */
    String getName();

    /**
     * Return the holder of this extension
     *
     * @return the holder of this extension or null if this extension is not held
     */
    T getExtendable();

    /**
     * Set the holder of this extension.
     *
     * @param extendable The new holder of this extension, could be null
     * @throws com.powsybl.commons.PowsyblException if this extension is already held.
     */
    void setExtendable(T extendable);

    /**
     * Method called just before the extension is removed from its holder.
     * Can be used for e.g. resource cleanup.
     */
    void cleanup();
}
