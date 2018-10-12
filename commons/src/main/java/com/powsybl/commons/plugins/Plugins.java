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

    public static Collection<PluginInfo> getPluginInfos() {
        return new ServiceLoaderCache<>(PluginInfo.class).getServices().stream()
                .sorted(Comparator.comparing(PluginInfo::getPluginName))
                .collect(Collectors.toList());
    }

    public static <T> PluginInfo<T> getPluginInfoByName(String name) {
        Objects.requireNonNull(name);
        return new ServiceLoaderCache<>(PluginInfo.class).getServices().stream()
                .filter(p -> p.getPluginName().equals(name))
                .findFirst().orElse(null);
    }

    public static <T> Collection<String> getPluginImplementationsIds(PluginInfo<T> pluginInfo) {
        Objects.requireNonNull(pluginInfo);
        List<T> pluginImpls = new ServiceLoaderCache<>(pluginInfo.getPluginClass()).getServices();
        return pluginImpls.stream()
                .map(pluginInfo::getId)
                .sorted()
                .collect(Collectors.toList());
    }
}
