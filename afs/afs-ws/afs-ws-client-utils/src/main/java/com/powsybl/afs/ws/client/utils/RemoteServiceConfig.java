/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client.utils;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.exceptions.UncheckedUriSyntaxException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteServiceConfig {

    public static Supplier<RemoteServiceConfig> INSTANCE = Suppliers.memoize(RemoteServiceConfig::load);

    private String hostName;

    private String appName;

    private int port;

    private boolean secure;

    public RemoteServiceConfig(String hostName, String appName, int port, boolean secure) {
        this.hostName = Objects.requireNonNull(hostName);
        this.appName = Objects.requireNonNull(appName);
        this.port = checkPort(port);
        this.secure = secure;
    }

    public static RemoteServiceConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static RemoteServiceConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        ModuleConfig moduleConfig = platformConfig.getModuleConfig("remote-service");
        String hostName = moduleConfig.getStringProperty("host-name");
        String appName = moduleConfig.getStringProperty("app-name");
        int port = moduleConfig.getIntProperty("port", 443);
        boolean secure = moduleConfig.getBooleanProperty("secure", true);
        return new RemoteServiceConfig(hostName, appName, port, secure);
    }

    private static int checkPort(int port) {
        if (port <= 0) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        return port;
    }

    public String getHostName() {
        return hostName;
    }

    public RemoteServiceConfig setHostName(String hostName) {
        this.hostName = Objects.requireNonNull(hostName);
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public RemoteServiceConfig setAppName(String appName) {
        this.appName = Objects.requireNonNull(appName);
        return this;
    }

    public int getPort() {
        return port;
    }

    public RemoteServiceConfig setPort(int port) {
        this.port = checkPort(port);
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public RemoteServiceConfig setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public URI getRestUri() {
        try {
            return new URI(secure ? "https" : "http", null, hostName, port, "/" + appName, null, null);
        } catch (URISyntaxException e) {
            throw new UncheckedUriSyntaxException(e);
        }
    }

    public URI getWsUri() {
        try {
            return new URI(secure ? "wss" : "ws", null, hostName, port, "/" + appName, null, null);
        } catch (URISyntaxException e) {
            throw new UncheckedUriSyntaxException(e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(hostName=" + hostName + ", appName=" + appName + ", port=" + port + ", secure=" + secure + ")";
    }
}
