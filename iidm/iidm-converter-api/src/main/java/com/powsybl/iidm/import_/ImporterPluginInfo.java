/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.PluginInfo;

import java.util.Objects;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
@AutoService(PluginInfo.class)
public class ImporterPluginInfo extends PluginInfo<Importer> {

    private static final String PLUGIN_NAME = "importer";

    public ImporterPluginInfo() {
        super(Importer.class, PLUGIN_NAME);
    }

    @Override
    public String getId(Importer importer) {
        return Objects.requireNonNull(importer).getFormat();
    }
}
