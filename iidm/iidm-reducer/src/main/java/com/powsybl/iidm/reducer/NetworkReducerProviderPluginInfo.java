/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.PluginInfo;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
@AutoService(PluginInfo.class)
public class NetworkReducerProviderPluginInfo extends PluginInfo<NetworkReducerProvider> {

    public NetworkReducerProviderPluginInfo() {
        super(NetworkReducerProvider.class);
    }
}
