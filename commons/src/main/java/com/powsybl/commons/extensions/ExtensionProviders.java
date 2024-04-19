/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public final class ExtensionProviders<T extends ExtensionProvider> {

    private final Map<String, T> providers;

    public static <T extends ExtensionProvider> ExtensionProviders<T> createProvider(Class<T> clazz) {
        return new ExtensionProviders<>(clazz);
    }

    public static <T extends ExtensionProvider> ExtensionProviders<T> createProvider(Class<T> clazz, String categoryName) {
        return new ExtensionProviders<>(clazz, categoryName);
    }

    public static <T extends ExtensionProvider> ExtensionProviders<T> createProvider(Class<T> clazz, String categoryName, Set<String> extensionNames) {
        return new ExtensionProviders<>(clazz, categoryName, extensionNames);
    }

    private ExtensionProviders(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        providers = new ServiceLoaderCache<>(clazz).getServices().stream()
                .collect(Collectors.toMap(T::getExtensionName, e -> e));
    }

    private ExtensionProviders(Class<T> clazz, String categoryName) {
        this(clazz, categoryName, null);
    }

    private ExtensionProviders(Class<T> clazz, String categoryName, Set<String> extensionNames) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(categoryName);

        List<T> services = new ServiceLoaderCache<>(clazz).getServices();
        providers = services.stream()
                .filter(s -> s.getCategoryName().equals(categoryName) && (extensionNames == null || extensionNames.contains(s.getExtensionName())))
                .collect(Collectors.toMap(T::getExtensionName, e -> e));
    }

    public T findProvider(String name) {
        return providers.get(name);
    }

    public T findProviderOrThrowException(String name) {
        T serializer = findProvider(name);
        if (serializer == null) {
            throw new PowsyblException("Provider not found for extension " + name);
        }

        return serializer;
    }

    public Collection<T> getProviders() {
        return providers.values();
    }

    public <T> void addExtensions(Extendable<T> extendable, Collection<Extension<T>> extensions) {
        Objects.requireNonNull(extendable);
        Objects.requireNonNull(extensions);
        extensions.forEach(e -> extendable.addExtension(findProvider(e.getName()).getExtensionClass(), e));
    }
}
