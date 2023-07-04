/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.extensions;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSortedMap;
import com.powsybl.dataframe.network.NetworkDataframeMapper;
import com.powsybl.dataframe.network.adders.NetworkElementAdder;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@soft.it>
 */

public final class NetworkExtensions {

    private static final Map<String, NetworkExtensionDataframeProvider> EXTENSIONS_PROVIDERS = createExtensionsProviders();

    private NetworkExtensions() {
    }

    private static Map<String, NetworkExtensionDataframeProvider> createExtensionsProviders() {
        return Suppliers
            .memoize(() -> ServiceLoader.load(NetworkExtensionDataframeProvider.class)
                .stream().map(ServiceLoader.Provider::get).collect(Collectors.toList()))
            .get().stream()
            .collect(ImmutableSortedMap.toImmutableSortedMap(String::compareTo,
                NetworkExtensionDataframeProvider::getExtensionName, Function.identity()));
    }

    public static List<String> getExtensionsNames() {
        return new ArrayList<>(EXTENSIONS_PROVIDERS.keySet());
    }

    public static List<NetworkExtensionDataframeProvider> getExtensionsProviders() {
        return new ArrayList<>(EXTENSIONS_PROVIDERS.values());
    }

    public static Map<ExtensionDataframeKey, NetworkDataframeMapper> createExtensionsMappers() {
        Map<ExtensionDataframeKey, NetworkDataframeMapper> extensionMappers = new HashMap<>();
        EXTENSIONS_PROVIDERS.values().forEach(ext -> {
            String extensionName = ext.getExtensionName();
            Map<String, NetworkDataframeMapper> dataframeMappers = ext.createMappers();
            for (Map.Entry<String, NetworkDataframeMapper> entry : dataframeMappers.entrySet()) {
                extensionMappers.put(new ExtensionDataframeKey(extensionName, entry.getKey()),
                    entry.getValue());
            }
        });
        return extensionMappers;
    }

    public static Map<String, NetworkElementAdder> createExtensionsAdders() {
        return EXTENSIONS_PROVIDERS.values().stream()
            .collect(Collectors.toMap(NetworkExtensionDataframeProvider::getExtensionName,
                NetworkExtensionDataframeProvider::createAdder));
    }

    public static void removeExtensions(Network network, String name, List<String> ids) {
        NetworkExtensionDataframeProvider provider = EXTENSIONS_PROVIDERS.get(name);
        if (provider != null) {
            provider.removeExtensions(network, ids);
        }
    }

}

