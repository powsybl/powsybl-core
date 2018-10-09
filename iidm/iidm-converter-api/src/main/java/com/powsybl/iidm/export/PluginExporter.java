/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.Plugin;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
@AutoService(Plugin.class)
public class PluginExporter implements Plugin<Exporter> {

    public static final String PLUGIN_NAME = "exporter";

    @Override
    public PluginInfo<Exporter> getPluginInfo() {
        return new PluginInfo<Exporter>(Exporter.class, PLUGIN_NAME) {
            @Override
            public String getId(Exporter exporter) {
                return exporter.getFormat();
            }
        };
    }
}
