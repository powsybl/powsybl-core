/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.plugins;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 */
public final class Plugins {

    private Plugins() {
    }

    public static Collection<PluginInfo> getPluginInfos() {
        return new ServiceLoaderCache<>(PluginInfo.class).getServices().stream()
                .sorted(Comparator.comparing(PluginInfo::getPluginName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public static PluginInfo getPluginInfoByName(String name) {
        Objects.requireNonNull(name);
        return new ServiceLoaderCache<>(PluginInfo.class).getServices().stream()
                .filter(p -> p.getPluginName().equals(name))
                .findFirst().orElse(null);
    }

    public static <T> List<String> getPluginImplementationsIds(PluginInfo<T> pluginInfo) {
        Objects.requireNonNull(pluginInfo);
        return new ServiceLoaderCache<>(pluginInfo.getPluginClass()).getServices().stream()
                .map(pluginInfo::getId)
                .sorted()
                .collect(Collectors.toList());
    }
}
