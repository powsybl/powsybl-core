/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A utility class to help finding providers using ServiceLoader.
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 *
 */
//Don't bother with generics because serviceloader doesn't return them
//and we put them in an index where we can't propagate the generic types.
@SuppressWarnings("rawtypes")
public final class ExtensionAdderProviders {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionAdderProviders.class);

    private static final Supplier<ProviderIndex> ADDER_PROVIDERS = Suppliers
            .memoize(() -> indexProviders(ServiceLoader.load(ExtensionAdderProvider.class, ExtensionAdderProviders.class.getClassLoader())));

    private ExtensionAdderProviders() {
    }

    static final class ProviderIndex {
        private final Map<Pair<String, Class>, List<ExtensionAdderProvider>> byAdderClass;
        private final Map<Pair<String, String>, List<ExtensionAdderProvider>> byName;

        private ProviderIndex(Map<Pair<String, Class>, List<ExtensionAdderProvider>> byAdderClass,
                              Map<Pair<String, String>, List<ExtensionAdderProvider>> byName) {
            this.byAdderClass = byAdderClass;
            this.byName = byName;
        }
    }

    //package private for tests
    static ProviderIndex indexProviders(Iterable<ExtensionAdderProvider> providers) {
        Map<Pair<String, Class>, List<ExtensionAdderProvider>> byAdderClass = new HashMap<>();
        Map<Pair<String, String>, List<ExtensionAdderProvider>> byName = new HashMap<>();

        for (ExtensionAdderProvider provider : providers) {
            String implName = provider.getImplementationName();
            if (provider.getExtensionName() != null) {
                byName.computeIfAbsent(Pair.of(implName, provider.getExtensionName()), key -> new ArrayList<>()).add(provider);
            }
            for (Class<?> type : getAssignableTypes(provider.getAdderClass())) {
                byAdderClass.computeIfAbsent(Pair.of(implName, type), key -> new ArrayList<>()).add(provider);
            }
        }

        return new ProviderIndex(toImmutableListsMap(byAdderClass), toImmutableListsMap(byName));
    }

    private static <K> Map<K, List<ExtensionAdderProvider>> toImmutableListsMap(Map<K, List<ExtensionAdderProvider>> map) {
        Map<K, List<ExtensionAdderProvider>> immutableMap = new HashMap<>();
        map.forEach((key, providers) -> immutableMap.put(key, List.copyOf(providers)));
        return Map.copyOf(immutableMap);
    }

    private static Set<Class<?>> getAssignableTypes(Class<?> type) {
        Set<Class<?>> types = new HashSet<>();
        Deque<Class<?>> typesToVisit = new ArrayDeque<>();
        typesToVisit.add(type);
        while (!typesToVisit.isEmpty()) {
            Class<?> currentType = typesToVisit.remove();
            if (types.add(currentType)) {
                Class<?> superclass = currentType.getSuperclass();
                if (superclass != null) {
                    typesToVisit.add(superclass);
                }
                Collections.addAll(typesToVisit, currentType.getInterfaces());
            }
        }
        return types;
    }

    private static <O, E extends Extension<O>, B extends ExtensionAdder<O, E>> ExtensionAdderProvider findProvider(
            String implName, Class<B> extensionAdderType,
            ProviderIndex providerIndex) {
        return findProvider(implName, extensionAdderType.getSimpleName(),
                providerIndex.byAdderClass.getOrDefault(Pair.of(implName, extensionAdderType), Collections.emptyList()));
    }

    private static ExtensionAdderProvider findProvider(
            String implName, String extensionName,
            ProviderIndex providerIndex) {
        return findProvider(implName, extensionName,
                providerIndex.byName.getOrDefault(Pair.of(implName, extensionName), Collections.emptyList()));
    }

    private static ExtensionAdderProvider findProvider(
            String implName, String typeName, List<ExtensionAdderProvider> providers) {
        if (providers.isEmpty()) {
            LOGGER.error(
                    "ExtensionAdderProvider not found for ExtensionAdder {} for implementation {}",
                    typeName, implName);
            throw new PowsyblException("ExtensionAdderProvider not found");
        }

        if (providers.size() > 1) {
            LOGGER.error(
                    "Multiple ExtensionAdderProviders found for ExtensionAdder {} for implementation {} : {}",
                    typeName, implName, providers);
            throw new PowsyblException(
                    "Multiple ExtensionAdderProviders configuration providers found");
        }
        return providers.get(0);
    }

    // TODO use O extends Extendable<O> here ? For now we can't because Extendable
    // doesn't declare Extendable<O extends Extendable>
    // package private for tests
    static <O, E extends Extension<O>, B extends ExtensionAdder<O, E>> ExtensionAdderProvider findCachedProvider(
            String implName, Class<B> type, ProviderIndex providerIndex) {
        return findProvider(implName, type, providerIndex);
    }

    static ExtensionAdderProvider findCachedProvider(
            String implName, String typeName, ProviderIndex providerIndex) {
        return findProvider(implName, typeName, providerIndex);
    }

    public static <O, E extends Extension<O>, B extends ExtensionAdder<O, E>> ExtensionAdderProvider findCachedProvider(
            String implName, Class<B> type) {
        return findCachedProvider(implName, type, ADDER_PROVIDERS.get());
    }

    public static ExtensionAdderProvider findCachedProvider(String implName, String typeName) {
        return findCachedProvider(implName, typeName, ADDER_PROVIDERS.get());
    }
}
