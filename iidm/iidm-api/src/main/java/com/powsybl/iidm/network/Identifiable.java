/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Properties;

/**
 * An object that is part of the network model and that is identified uniquely
 * by a <code>String</code> id.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Identifiable<I extends Identifiable<I>> {

    interface Extension<I> {

        String getName();

        I getIdentifiable();
    }

    /**
     * Get the unique identifier of the object.
     */
    String getId();

    /**
     * Get an the (optional) name  of the object.
     */
    String getName();

    /**
     * Check that this object has some properties.
     */
    boolean hasProperty();

    /**
     * Get properties associated to the object.
     */
    Properties getProperties();

    /**
     * Add en extension to this identifiable.
     * @param type the extension class type
     * @param extension the extension
     * @param <E> the extension type
     */
    <E extends Extension<I>> void addExtension(Class<? super E> type, E extension);

    /**
     * Get an extension based on its class type.
     * @param type the extension class type
     * @param <E> the extension type
     * @return the extension mapped to the class type or null if not found
     */
    <E extends Extension<I>> E getExtension(Class<E> type);

    /**
     * Get an extension based on its name.
     * @param name the extension name
     * @return the extension mapped to the name or null if not found
     */
    <E extends Extension<I>> E getExtensionByName(String name);

    /**
     * Remove an extension based on its class type.
     * @param type the extension class type
     * @param <E> the extension type
     * @return true if the extension has been removed false if extension has not been found
     */
    <E extends Extension<I>> boolean removeExtension(Class<E> type);

    /**
     * Get all extensions associated with this identifiable.
     * @return all extensions associated to this identifiable
     */
    Collection<Extension<I>> getExtensions();
}
