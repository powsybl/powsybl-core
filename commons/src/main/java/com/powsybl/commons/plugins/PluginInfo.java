/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.plugins;

import java.util.Objects;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class PluginInfo<T> {

    private final Class<T> pluginClass;

    private final String pluginName;

    public PluginInfo(Class<T> pluginClass, String pluginName) {
        this.pluginClass = Objects.requireNonNull(pluginClass);
        this.pluginName = Objects.requireNonNull(pluginName);
    }

    public Class<T> getPluginClass() {
        return pluginClass;
    }

    /**
     * identifies the plugin 'type' name (e.g. importer, exporter).
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * identifies a plugin object with an ID (default is its class name)
     */
    public String getId(T pluginImpl) {
        return pluginImpl.getClass().getName();
    }

    @Override
    public String toString() {
        return "PluginInfo(" + pluginClass.getName() + " - " + pluginName + ")";
    }
}
