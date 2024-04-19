/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import java.util.Collection;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface Extendable<O> {

    /**
     * Add en extension to this extendable object.
     *
     * @param type      the extension class type
     * @param extension the extension
     * @param <E>       the extension type
     */
    <E extends Extension<O>> void addExtension(Class<? super E> type, E extension);

    /**
     * Get an extension based on its class type.
     *
     * @param type the extension class type
     * @param <E>  the extension type
     * @return the extension mapped to the class type or null if not found
     */
    <E extends Extension<O>> E getExtension(Class<? super E> type);

    /**
     * Get an extension based on its name.
     *
     * @param name the extension name
     * @return the extension mapped to the name or null if not found
     */
    <E extends Extension<O>> E getExtensionByName(String name);

    /**
     * Remove an extension based on its class type.
     *
     * @param type the extension class type
     * @param <E>  the extension type
     * @return true if the extension has been removed false if extension has not been found
     */
    <E extends Extension<O>> boolean removeExtension(Class<E> type);

    /**
     * Get all extensions associated with this extendable object..
     *
     * @return all extensions associated to this extendable object.
     */
    <E extends Extension<O>> Collection<E> getExtensions();

    /**
     * Returns a name that is used to find matching {@link ExtensionAdderProvider}s
     * when selecting implementations of extensions in {@link #newExtension}. This
     * is meant to be overriden by extendables when multiple implementations exist.
     *
     * @return the name
     */
    default String getImplementationName() {
        return "Default";
    }

    /**
     * Returns an extensionAdder to build and add an extension for this extendable.
     * <p>
     * The extension implementation is selected at runtime based on matching the
     * {@link #getImplementationName} of this extendable to the
     * {@link ExtensionAdderProvider#getImplementationName} of a provider.
     * Implementations are loaded with java's {@link java.util.ServiceLoader} using
     * the ExtensionAdderProvider interface.
     *
     * @param type The interface of the ExtensionAdder
     * @return the adder
     */
    // Don't bother all the way with generics because the this is a runtime system
    // that doesn't know generics. We do check that the builder is from the the same
    // extendable class T as "this".
    default <E extends Extension<O>, B extends ExtensionAdder<O, E>> B newExtension(Class<B> type) {
        ExtensionAdderProvider provider = ExtensionAdderProviders.findCachedProvider(getImplementationName(), type);
        return (B) provider.newAdder(this);
    }

}
