/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.PluginInfo;

import java.util.Objects;

@AutoService(PluginInfo.class)
public class SecurityAnalysisPluginInfo extends PluginInfo<SecurityAnalysis> {
    private static final String PLUGIN_NAME = "security analysis";

    public SecurityAnalysisPluginInfo() {
        super(SecurityAnalysis.class, PLUGIN_NAME);
    }

    @Override
    public String getId(SecurityAnalysis securityAnalysis) {
        return Objects.requireNonNull(securityAnalysis).getId();
    }
}
