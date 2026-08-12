/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import java.util.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public abstract class AbstractExtendable<T> implements Extendable<T> {

    private Map<Class<?>, Extension<T>> extensions = null;

    private Map<String, Extension<T>> extensionsByName = null;

    @Override
    public <E extends Extension<T>> void addExtension(Class<? super E> type, E extension) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(extension);
        // remove any existing, which will trigger extension own cleanup if any
        this.removeExtension((Class<E>) type);
        extension.setExtendable((T) this);
        if (extensions == null) {
            extensions = new HashMap<>();
        }
        extensions.put(type, extension);
        if (extensionsByName == null) {
            extensionsByName = new HashMap<>();
        }
        extensionsByName.put(extension.getName(), extension);
    }

    @Override
    public <E extends Extension<T>> E getExtension(Class<? super E> type) {
        Objects.requireNonNull(type);
        if (extensions == null) {
            return null;
        }
        return (E) extensions.get(type);
    }

    @Override
    public <E extends Extension<T>> E getExtensionByName(String name) {
        Objects.requireNonNull(name);
        if (extensionsByName == null) {
            return null;
        }
        return (E) extensionsByName.get(name);
    }

    protected <E extends Extension<T>> void removeExtension(Class<E> type, E extension) {
        if (extensions != null) {
            extensions.remove(type);
        }
        if (extensionsByName != null) {
            extensionsByName.remove(extension.getName());
        }
        extension.setExtendable(null);
    }

    @Override
    public <E extends Extension<T>> boolean removeExtension(Class<E> type) {
        boolean removed = false;

        E extension = getExtension(type);
        if (extension != null) {
            removeExtension(type, extension);
            removed = true;
        }

        return removed;
    }

    @Override
    public Collection<Extension<T>> getExtensions() {
        if (extensions == null) {
            return Collections.emptyList();
        }
        return extensionsByName.values();
    }

    @Override
    public String getImplementationName() {
        return "Default";
    }
}
