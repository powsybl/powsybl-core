/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.plugins;

import com.google.auto.service.AutoService;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
@AutoService(PluginInfo.class)
public class PluginInfoB extends PluginInfo<B> {

    static final String PLUGIN_NAME = "PLUGIN_B";

    public PluginInfoB() {
        super(B.class, PLUGIN_NAME);
    }
}

