/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.powsybl.commons.PowsyblException;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public final class ExtensionProviders<T extends ExtensionProvider> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionProviders.class);

    private final Map<String, T> providers;

    public static <T extends ExtensionProvider> ExtensionProviders<T> createProvider(Class<T> clazz) {
        return new ExtensionProviders<>(clazz);
    }

    public static <T extends ExtensionProvider> ExtensionProviders<T> createProvider(Class<T> clazz, String categoryName) {
        return new ExtensionProviders<>(clazz, categoryName);
    }

    public static <T extends ExtensionProvider> ExtensionProviders<T> createProvider(Class<T> clazz, String categoryName, ExtensionProvidersLoader loader) {
        return new ExtensionProviders<>(clazz, categoryName, null, loader);
    }

    public static <T extends ExtensionProvider> ExtensionProviders<T> createProvider(Class<T> clazz, String categoryName, Set<String> extensionNames) {
        return new ExtensionProviders<>(clazz, categoryName, extensionNames);
    }

    private ExtensionProviders(Class<T> clazz) {
        Objects.requireNonNull(clazz);
        providers = loadProviders(clazz, null, null, new DefaultExtensionProvidersLoader());
    }

    private ExtensionProviders(Class<T> clazz, String categoryName) {
        this(clazz, categoryName, null);
    }

    private ExtensionProviders(Class<T> clazz, String categoryName, Set<String> extensionNames) {
        this(clazz, categoryName, extensionNames, new DefaultExtensionProvidersLoader());
    }

    private ExtensionProviders(Class<T> clazz, String categoryName, Set<String> extensionNames, ExtensionProvidersLoader loader) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(categoryName);

        providers = loadProviders(clazz, categoryName, extensionNames, loader);
    }

    private Map<String, T> loadProviders(Class<T> clazz, String categoryName, Set<String> extensionNames, ExtensionProvidersLoader loader) {
        final Map<String, T> providersMap = new HashMap<>();
        ToBooleanBiFunction<String, String> validateNames = extensionNames == null ?
                (n1, n2) -> true :
                (n1, n2) -> extensionNames.contains(n1) || extensionNames.contains(n2);

        Stream<T> servicesStream = loader.getServicesStream(clazz);
        if (categoryName != null) {
            servicesStream = servicesStream.filter(s -> s.getCategoryName().equals(categoryName));
        }
        Set<T> services = servicesStream.collect(Collectors.toSet());
        services.forEach(service -> addService(providersMap, service.getExtensionName(), service, validateNames));
        if (clazz.equals(ExtensionSerDe.class)) {
            // Add the alternative serialization names for extension SerDes
            services.forEach(service -> ((ExtensionSerDe<?, ?>) service).getAlternativeSerializationNames()
                    .forEach(name -> addService(providersMap, name, service, validateNames)));
        }
        return providersMap;
    }

    private void addService(Map<String, T> providersMap, String nameToAdd,
                            T service, ToBooleanBiFunction<String, String> validateNames) {
        if (validateNames.applyAsBoolean(nameToAdd, service.getExtensionName())) {
            T previousService = providersMap.get(nameToAdd);
            if (previousService != null) {
                if (service.getExtensionName().equals(nameToAdd)) {
                    // There should not be two extensions of the same real name
                    throw new IllegalStateException("Several providers were found for extension '" + nameToAdd + "'");
                } else {
                    // For alternative names, a duplicate does not replace the previous provider, to avoid replacing
                    // providers mapped to their real extension names by providers mapped to an alternative name.
                    LOGGER.warn("Alternative extension name {} for extension {} is already used for real extension {} - Skipping",
                            nameToAdd, service.getExtensionName(), previousService.getExtensionName());
                }
            } else {
                providersMap.put(nameToAdd, service);
            }
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

    public <E> void addExtensions(Extendable<E> extendable, Collection<Extension<E>> extensions) {
        Objects.requireNonNull(extendable);
        Objects.requireNonNull(extensions);
        extensions.forEach(e -> extendable.addExtension(findProvider(e.getName()).getExtensionClass(), e));
    }
}
