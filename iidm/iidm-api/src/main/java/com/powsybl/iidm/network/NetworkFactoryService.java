/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.config.PlatformConfigNamedProvider;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface NetworkFactoryService extends PlatformConfigNamedProvider {

    /**
     * Get network factory name.
     *
     * @return network factory name
     */
    String getName();

    /**
     * Create network factory instance.
     *
     * @return network factor instance
     */
    NetworkFactory createNetworkFactory();
}
