/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

/**
 * Extension data for extendables.
 * <p>
 * A generic Extension interface should have a generic static method called
 * "clazz" returning its .class as a generic type with a wildcard bounded to the
 * expected extendables to allow clients to type check that they are using the
 * correct Extension for an extendable. For example,
 *
 * <pre>
 * public interface ConnectablePosition<C extends Connectable<C> extends Extension<C> {
 *
 * //repeat the bounds "<C extends Connectable>" bounds here here
 * <C extends Connectable<C>> Class<ConnectablePosition<C>> clazz() {
 *     return Class<ConnectablePosition<C>> (Class) ConnectablePosition.class;
 * }
 *
 * [...]
 * }
 * </pre>
 *
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface Extension<T> {

    /**
     * Return the name of this extension.
     */
    String getName();

    /**
     * Return the holder of this extension
     *
     * @return the holder of this extension or null if this extension is not holded
     */
    T getExtendable();

    /**
     * Set the holder of this extension.
     *
     * @param extendable The new holder of this extension, could be null
     * @throws a PowsyblException if this extension is already holded.
     */
    void setExtendable(T extendable);

}
