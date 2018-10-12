/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.tools.plugins;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.PluginInfo;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
@AutoService(PluginInfo.class)
public class DummyPluginInfo extends PluginInfo<Dummy> {

    public DummyPluginInfo() {
        super(Dummy.class, "dummy");
    }
}
