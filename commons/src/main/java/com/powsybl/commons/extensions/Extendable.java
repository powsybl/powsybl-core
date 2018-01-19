/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import java.util.Collection;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public interface Extendable<O> {

    /**
     * Add en extension to this extendable object.
     * @param type the extension class type
     * @param extension the extension
     * @param <E> the extension type
     */
    <E extends Extension<O>> void addExtension(Class<? super E> type, E extension);

    /**
     * Get an extension based on its class type.
     * @param type the extension class type
     * @param <E> the extension type
     * @return the extension mapped to the class type or null if not found
     */
    <E extends Extension<O>> E getExtension(Class<E> type);

    /**
     * Get an extension based on its name.
     * @param name the extension name
     * @return the extension mapped to the name or null if not found
     */
    <E extends Extension<O>> E getExtensionByName(String name);

    /**
     * Remove an extension based on its class type.
     * @param type the extension class type
     * @param <E> the extension type
     * @return true if the extension has been removed false if extension has not been found
     */
    <E extends Extension<O>> boolean removeExtension(Class<E> type);

    /**
     * Get all extensions associated with this extendable object..
     * @return all extensions associated to this extendable object.
     */
    <E extends Extension<O>> Collection<E> getExtensions();
}
