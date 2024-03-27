/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.auto.service.AutoService;
import com.powsybl.commons.plugins.PluginInfo;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(PluginInfo.class)
public class ContingenciesProviderFactoryPluginInfo extends PluginInfo<ContingenciesProviderFactory> {

    public ContingenciesProviderFactoryPluginInfo() {
        super(ContingenciesProviderFactory.class);
    }
}
