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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        providers = loadProviders(clazz, null, null);
    }

    private ExtensionProviders(Class<T> clazz, String categoryName) {
        this(clazz, categoryName, null);
    }

    private ExtensionProviders(Class<T> clazz, String categoryName, Set<String> extensionNames) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(categoryName);

        providers = loadProviders(clazz, categoryName, extensionNames);
    }

    private Map<String, T> loadProviders(Class<T> clazz, String categoryName, Set<String> extensionNames) {
        final Map<String, T> providersMap = new HashMap<>();
        Predicate<String> validateName = extensionNames == null ? n -> true : extensionNames::contains;

        Stream<T> stream = new ServiceLoaderCache<>(clazz).getServices().stream();
        if (categoryName != null) {
            stream = stream.filter(s -> s.getCategoryName().equals(categoryName));
        }
        if (clazz.equals(ExtensionSerDe.class)) {
            stream.forEach(service ->
                ((ExtensionSerDe<?, ?>) service).getSerializationNames().forEach(name -> addService(providersMap, name, service, validateName))
            );
        } else {
            stream.forEach(service -> addService(providersMap, service.getExtensionName(), service, validateName));
        }
        return providersMap;
    }

    private void addService(Map<String, T> providersMap, String name, T service, Predicate<String> validateName) {
        if (validateName.test(name)) {
            providersMap.put(name, service);
        }
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
        return providers.values().stream().distinct().collect(Collectors.toList());
    }

    public <T> void addExtensions(Extendable<T> extendable, Collection<Extension<T>> extensions) {
        Objects.requireNonNull(extendable);
        Objects.requireNonNull(extensions);
        extensions.forEach(e -> extendable.addExtension(findProvider(e.getName()).getExtensionClass(), e));
    }
}
