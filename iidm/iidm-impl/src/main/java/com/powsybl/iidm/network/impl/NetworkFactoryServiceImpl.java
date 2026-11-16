/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.NetworkFactoryConstants;
import com.powsybl.iidm.network.NetworkFactoryService;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(NetworkFactoryService.class)
public class NetworkFactoryServiceImpl implements NetworkFactoryService {

    @Override
    public String getName() {
        return NetworkFactoryConstants.DEFAULT;
    }

    @Override
    public NetworkFactory createNetworkFactory() {
        return new NetworkFactoryImpl();
    }
}
