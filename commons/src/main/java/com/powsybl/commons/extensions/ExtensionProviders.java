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
import java.util.stream.Stream;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public final class ExtensionProviders<T extends ExtensionProvider> {

    private final Map<String, T> providers;
    private final Map<String, T> alternativeProviders;

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
        this(clazz, null, null);
    }

    private ExtensionProviders(Class<T> clazz, String categoryName) {
        this(clazz, categoryName, null);
    }

    private ExtensionProviders(Class<T> clazz, String categoryName, Set<String> extensionNames) {
        Objects.requireNonNull(clazz);

        Stream<T> providersStream = new ServiceLoaderCache<>(clazz).getServices().stream();
        if (categoryName != null) {
            providersStream = providersStream.filter(s -> s.getCategoryName().equals(categoryName) &&
                    (extensionNames == null || extensionNames.contains(s.getExtensionName())));
        }
        providers = providersStream.collect(Collectors.toMap(T::getExtensionName, e -> e));

        Class<? extends ExtensionProviderAlternative> alternativeClass = getExtensionProviderAlternativeClass(clazz);
        if (alternativeClass != null) {
            Stream<? extends ExtensionProviderAlternative> providerAlternativeStream = new ServiceLoaderCache<>(alternativeClass).getServices().stream();
            if (categoryName != null) {
                providerAlternativeStream = providerAlternativeStream.filter(s -> s.getCategoryName().equals(categoryName) &&
                        (extensionNames == null || extensionNames.contains(s.getOriginalExtensionName())));
            }
            alternativeProviders = providerAlternativeStream
                    .collect(Collectors.toMap(ExtensionProviderAlternative::getOriginalExtensionName, e -> (T) e));
        } else {
            alternativeProviders = Collections.emptyMap();
        }
    }

    private Class<? extends ExtensionProviderAlternative> getExtensionProviderAlternativeClass(Class<T> clazz) {
        if (clazz.equals(ExtensionSerDe.class)) {
            return AlternativeExtensionSerDe.class;
        }
        return null;
    }

    public T findProvider(String name) {
        return findProvider(name, null);
    }

    public T findProvider(String name, ExtensionProvidersOptions options) {
        if (options != null && options.useAlternativeVersion(name)) {
            T alternative = alternativeProviders.get(name);
            if (alternative != null) {
                return alternative;
            }
        }
        return providers.get(name);
    }

    public T findProviderOrThrowException(String name) {
        return findProviderOrThrowException(name, null);
    }

    public T findProviderOrThrowException(String name, ExtensionProvidersOptions options) {
        T provider = findProvider(name, options);
        if (provider == null) {
            throw new PowsyblException("Provider not found for extension " + name);
        }
        return provider;
    }

    public Collection<T> getProviders() {
        return providers.keySet().stream()
                .map(this::findProvider)
                .toList();
    }

    public <T> void addExtensions(Extendable<T> extendable, Collection<Extension<T>> extensions) {
        Objects.requireNonNull(extendable);
        Objects.requireNonNull(extensions);
        extensions.forEach(e -> extendable.addExtension(findProvider(e.getName()).getExtensionClass(), e));
    }
}
