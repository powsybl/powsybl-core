/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.extensions;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
 * @author Jon Harper <jon.harper at rte-france.com>
 *
 */
//Don't bother with generics because serviceloader doesn't return them
//and we put them in a cache where we can't propagate the generic types.
@SuppressWarnings("rawtypes")
public final class ExtensionAdderProviders {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionAdderProviders.class);

    private static final Supplier<ConcurrentMap<String, List<ExtensionAdderProvider>>> ADDER_PROVIDERS = Suppliers
            .memoize(() -> groupProvidersByName(ServiceLoader.load(ExtensionAdderProvider.class, ExtensionAdderProviders.class.getClassLoader())))::get;

    private static final ConcurrentMap<Pair<String, Class>, ExtensionAdderProvider> CACHE = new ConcurrentHashMap<>();

    //package private for tests
    static ConcurrentMap<String, List<ExtensionAdderProvider>> groupProvidersByName(Iterable<ExtensionAdderProvider> i) {
        return StreamSupport.stream(i.spliterator(), false).collect(Collectors
                .groupingByConcurrent(ExtensionAdderProvider::getImplementationName));
    }

    private ExtensionAdderProviders() {
    }

    private static <O, E extends Extension<O>, B extends ExtensionAdder<O, E>> ExtensionAdderProvider findProvider(
            String name, Class<B> type,
            ConcurrentMap<String, List<ExtensionAdderProvider>> providersMap) {

        List<ExtensionAdderProvider> providersForName = providersMap.get(name);
        if (providersForName == null) {
            providersForName = Collections.emptyList();
        }
        List<ExtensionAdderProvider> providers = providersForName.stream()
                .filter(s -> type.isAssignableFrom(s.getAdderClass()))
                .collect(Collectors.toList());

        if (providers.isEmpty()) {
            LOGGER.error(
                    "ExtensionAdderProvider not found for ExtensionAdder {} for implementation {}",
                    type.getSimpleName(), name);
            throw new PowsyblException("ExtensionAdderProvider not found");
        }

        if (providers.size() > 1) {
            LOGGER.error(
                    "Multiple ExtensionAdderProviders found for ExtensionAdder {} for implementation {} : {}",
                    type.getSimpleName(), name, providers);
            throw new PowsyblException(
                    "Multiple ExtensionAdderProviders configuration providers found");
        }
        return providers.get(0);
    }

    // TODO use O extends Extendable<O> here ? For now we can't because Extendable
    // doesn't declare Extendable<O extends Extendable>
    // package private for tests
    static <O, E extends Extension<O>, B extends ExtensionAdder<O, E>> ExtensionAdderProvider findCachedProvider(
            String name, Class<B> type,
            ConcurrentMap<String, List<ExtensionAdderProvider>> providersMap,
            ConcurrentMap<Pair<String, Class>, ExtensionAdderProvider> cache) {
        return cache.computeIfAbsent(Pair.of(name, type), k -> findProvider(name, type, providersMap));
    }

    public static <O, E extends Extension<O>, B extends ExtensionAdder<O, E>> ExtensionAdderProvider findCachedProvider(
            String name, Class<B> type) {
        return findCachedProvider(name, type, ADDER_PROVIDERS.get(), CACHE);
    }
}
