/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.PluginInfo;

@AutoService(PluginInfo.class)
public class SecurityAnalysisFactoryPluginInfo extends PluginInfo<SecurityAnalysisFactory> {

    public SecurityAnalysisFactoryPluginInfo() {
        super(SecurityAnalysisFactory.class);
    }
}
