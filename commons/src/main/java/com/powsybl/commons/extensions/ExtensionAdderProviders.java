/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;

/**
 * A utility class to help finding providers using ServiceLoader.
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 *
 */
//Don't bother with generics because serviceloader doesn't return them
//and we put them in a cache where we can't propagate the generic types.
@SuppressWarnings("rawtypes")
public final class ExtensionAdderProviders {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionAdderProviders.class);

    private static final Supplier<ConcurrentMap<String, List<ExtensionAdderProvider>>> ADDER_PROVIDERS = Suppliers
            .memoize(() -> groupProvidersByName(ServiceLoader.load(ExtensionAdderProvider.class, ExtensionAdderProviders.class.getClassLoader())));

    private static final ConcurrentMap<Pair<String, Class>, ExtensionAdderProvider> BY_CLASS_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Pair<String, String>, ExtensionAdderProvider> BY_NAME_CACHE = new ConcurrentHashMap<>();

    //package private for tests
    static ConcurrentMap<String, List<ExtensionAdderProvider>> groupProvidersByName(Iterable<ExtensionAdderProvider> i) {
        return StreamSupport.stream(i.spliterator(), false).collect(Collectors
                .groupingByConcurrent(ExtensionAdderProvider::getImplementationName));
    }

    private ExtensionAdderProviders() {
    }

    private static <O, E extends Extension<O>, B extends ExtensionAdder<O, E>> ExtensionAdderProvider findProvider(
            String implName, Class<B> extensionAdderType,
            ConcurrentMap<String, List<ExtensionAdderProvider>> providersMap) {
        return findProvider(implName, s -> extensionAdderType.isAssignableFrom(s.getAdderClass()), extensionAdderType.getSimpleName(), providersMap);
    }

    private static ExtensionAdderProvider findProvider(
            String implName, String extensionName,
            ConcurrentMap<String, List<ExtensionAdderProvider>> providersMap) {
        return findProvider(implName, s -> s.getExtensionName() != null && extensionName.equals(s.getExtensionName()), extensionName, providersMap);
    }

    private static ExtensionAdderProvider findProvider(
            String implName, Predicate<ExtensionAdderProvider> typeFilter, String typeName,
            ConcurrentMap<String, List<ExtensionAdderProvider>> providersMap) {

        List<ExtensionAdderProvider> providersForName = providersMap.get(implName);
        if (providersForName == null) {
            providersForName = Collections.emptyList();
        }
        List<ExtensionAdderProvider> providers = providersForName.stream()
                .filter(typeFilter)
                .toList();

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
            String implName, Class<B> type,
            ConcurrentMap<String, List<ExtensionAdderProvider>> providersMap,
            ConcurrentMap<Pair<String, Class>, ExtensionAdderProvider> cache) {
        return cache.computeIfAbsent(Pair.of(implName, type), k -> findProvider(implName, type, providersMap));
    }

    static ExtensionAdderProvider findCachedProvider(
            String implName, String typeName,
            ConcurrentMap<String, List<ExtensionAdderProvider>> providersMap,
            ConcurrentMap<Pair<String, String>, ExtensionAdderProvider> cache) {
        return cache.computeIfAbsent(Pair.of(implName, typeName), k -> findProvider(implName, typeName, providersMap));
    }

    public static <O, E extends Extension<O>, B extends ExtensionAdder<O, E>> ExtensionAdderProvider findCachedProvider(
            String implName, Class<B> type) {
        return findCachedProvider(implName, type, ADDER_PROVIDERS.get(), BY_CLASS_CACHE);
    }

    public static ExtensionAdderProvider findCachedProvider(String implName, String typeName) {
        return findCachedProvider(implName, typeName, ADDER_PROVIDERS.get(), BY_NAME_CACHE);
    }
}
