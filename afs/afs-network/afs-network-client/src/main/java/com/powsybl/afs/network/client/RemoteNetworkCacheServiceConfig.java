/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.network.client;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.afs.ws.client.utils.AbstractRemoteServiceConfig;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteNetworkCacheServiceConfig extends AbstractRemoteServiceConfig<RemoteNetworkCacheServiceConfig> {

    public RemoteNetworkCacheServiceConfig(String hostName, String appName, int port, boolean secure) {
        super(hostName, appName, port, secure);
    }

    public static RemoteNetworkCacheServiceConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static RemoteNetworkCacheServiceConfig load(PlatformConfig platformConfig) {
        ModuleConfig moduleConfig = platformConfig.getModuleConfig("remote-network-cache-service");
        String hostName = moduleConfig.getStringProperty("host-name");
        String appName = moduleConfig.getStringProperty("app-name");
        int port = moduleConfig.getIntProperty("port");
        boolean secure = moduleConfig.getBooleanProperty("secure");
        return new RemoteNetworkCacheServiceConfig(hostName, appName, port, secure);
    }
}
