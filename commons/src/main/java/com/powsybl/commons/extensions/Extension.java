/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface Extension<T> {

    /**
     * Return the name of this extension.
     */
    String getName();

    /**
     * Return the holder of this extension
     * @return the holder of this extension or null if this extension is not holded
     */
    T getExtendable();

    /**
     * Set the holder of this extension.
     * @param extendable The new holder of this extension, could be null
     * @throws a PowsyblException if this extension is already holded.
     */
    void setExtendable(T extendable);

}
