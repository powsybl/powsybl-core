/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineUIConfig {

    public static OfflineUIConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("offlineui");
        String jmxHost = config.getStringProperty("jmxHost");
        int jmxPort = config.getIntProperty("jmxPort");
        return new OfflineUIConfig(jmxHost, jmxPort);
    }

    private final String jmxHost;

    private final int jmxPort;

    public OfflineUIConfig(String jmxHost, int jmxPort) {
        Objects.requireNonNull(jmxHost);
        this.jmxHost = jmxHost;
        this.jmxPort = jmxPort;
    }

    public String getJmxHost() {
        return jmxHost;
    }

    public int getJmxPort() {
        return jmxPort;
    }
}
