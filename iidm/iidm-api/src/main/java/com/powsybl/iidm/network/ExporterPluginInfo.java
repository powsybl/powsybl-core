/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.PluginInfo;

import java.util.Objects;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 */
@AutoService(PluginInfo.class)
public class ExporterPluginInfo extends PluginInfo<Exporter> {

    public ExporterPluginInfo() {
        super(Exporter.class);
    }

    @Override
    public String getId(Exporter exporter) {
        return Objects.requireNonNull(exporter).getFormat();
    }
}
