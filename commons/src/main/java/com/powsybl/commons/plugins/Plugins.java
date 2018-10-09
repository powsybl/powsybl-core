/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.plugins;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public final class Plugins {

    private Plugins() {
    }

    public static Collection<Plugin> getPlugins() {
        return new ArrayList<>(new ServiceLoaderCache<>(Plugin.class).getServices()).stream()
                .sorted(Comparator.comparing((Plugin p) -> p.getPluginInfo().getPluginName())).collect(Collectors.toList());
    }

    public static Plugin getPluginByName(String name) {
        Objects.requireNonNull(name);
        return new ServiceLoaderCache<>(Plugin.class).getServices().stream()
                .filter(p -> p.getPluginInfo().getPluginName().equals(name)).findFirst().orElse(null);
    }

    public static Collection<String> getPluginImplementationsIds(Plugin plugin) {
        Objects.requireNonNull(plugin);
        List<?> pluginImpls = new ServiceLoaderCache<>(plugin.getPluginInfo().getPluginClass()).getServices();
        return pluginImpls.stream().map(pluginImpl -> plugin.getPluginInfo().getId(pluginImpl)).sorted().collect(Collectors.toList());
    }
}
