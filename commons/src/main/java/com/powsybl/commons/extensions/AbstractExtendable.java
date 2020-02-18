/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public abstract class AbstractExtendable<T> implements Extendable<T> {

    private final Map<Class<?>, Extension<T>> extensions = new HashMap<>();

    private final Map<String, Extension<T>> extensionsByName = new HashMap<>();

    @Override
    public <E extends Extension<T>> void addExtension(Class<? super E> type, E extension) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(extension);
        extension.setExtendable((T) this);
        extensions.put(type, extension);
        extensionsByName.put(extension.getName(), extension);
    }

    @Override
    public <E extends Extension<T>> E getExtension(Class<? super E> type) {
        Objects.requireNonNull(type);
        return (E) extensions.get(type);
    }

    @Override
    public <E extends Extension<T>> E getExtensionByName(String name) {
        Objects.requireNonNull(name);
        return (E) extensionsByName.get(name);
    }

    @Override
    public <E extends Extension<T>> boolean removeExtension(Class<E> type) {
        boolean removed = false;

        E extension = getExtension(type);
        if (extension != null) {
            extensions.remove(type);
            extensionsByName.remove(extension.getName());
            extension.setExtendable(null);
            removed = true;
        }

        return removed;
    }

    @Override
    public Collection<Extension<T>> getExtensions() {
        return extensionsByName.values();
    }

    // Don't bother all the way with generics because the this is a runtime system
    // that doesn't know generics. We do check that the builder is from the the same
    // extendable class T as "this".
    @Override
    public <E extends Extension<T>, B extends ExtensionAdder<T, E>> B newExtension(Class<B> type) {
        ExtensionAdderProvider provider = ExtensionAdderProviders.findCachedProvider(getImplementationName(), type);
        return (B) provider.newAdder(this);
    }

}
