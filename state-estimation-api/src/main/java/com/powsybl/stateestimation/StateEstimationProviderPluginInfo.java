/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.PluginInfo;

/**
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
@AutoService(PluginInfo.class)
public class StateEstimationProviderPluginInfo extends PluginInfo<StateEstimationProvider> {

    public StateEstimationProviderPluginInfo() {
        super(StateEstimationProvider.class);
    }
}
