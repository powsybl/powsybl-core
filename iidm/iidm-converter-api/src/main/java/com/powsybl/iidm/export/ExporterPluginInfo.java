/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.PluginInfo;

import java.util.Objects;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
@AutoService(PluginInfo.class)
public class ExporterPluginInfo extends PluginInfo<Exporter> {

    private static final String PLUGIN_NAME = "exporter";

    public ExporterPluginInfo() {
        super(Exporter.class, PLUGIN_NAME);
    }

    @Override
    public String getId(Exporter exporter) {
        return Objects.requireNonNull(exporter).getFormat();
    }
}
