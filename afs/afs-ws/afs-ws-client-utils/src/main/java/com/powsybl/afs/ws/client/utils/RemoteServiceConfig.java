/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client.utils;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.exceptions.UncheckedUriSyntaxException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RemoteServiceConfig {

    private String hostName;

    private String appName;

    private int port;

    private boolean secure;

    private boolean autoreconnectEnabled;

    private int reconnectionInitialInterval;

    private int reconnectionIntervalMutiplier;

    private int reconnectionTimeout;

    private int reconnectionMax;

    public RemoteServiceConfig(String hostName, String appName, int port, boolean secure) {
        this.hostName = Objects.requireNonNull(hostName);
        this.appName = Objects.requireNonNull(appName);
        this.port = checkPort(port);
        this.secure = secure;
    }

    public static Optional<RemoteServiceConfig> load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static Optional<RemoteServiceConfig> load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        return platformConfig.getOptionalModuleConfig("remote-service").map(moduleConfig -> {
            String hostName = moduleConfig.getStringProperty("host-name");
            String appName = moduleConfig.getStringProperty("app-name");
            boolean secure = moduleConfig.getBooleanProperty("secure", true);
            int port = moduleConfig.getIntProperty("port", secure ? 443 : 80);
            boolean autoreconnectEnabled = moduleConfig.getBooleanProperty("autoreconnect-enabled", false);
            int reconnectionInitialInterval = moduleConfig.getIntProperty("reconnection-initial-interval", 5);
            int reconnectionIntervalMutiplier = moduleConfig.getIntProperty("reconnection-interval-mutiplier", 2);
            int reconnectionTimeout = moduleConfig.getIntProperty("reconnection-timeout", 3600);
            int reconnectionMax = moduleConfig.getIntProperty("reconnection-max", Integer.MAX_VALUE);

            RemoteServiceConfig remoteServiceConfig = new RemoteServiceConfig(hostName, appName, port, secure);
            remoteServiceConfig.setAutoreconnectEnabled(autoreconnectEnabled)
                    .setReconnectionInitialInterval(reconnectionInitialInterval)
                    .setReconnectionIntervalMutiplier(reconnectionIntervalMutiplier)
                    .setReconnectionTimeout(reconnectionTimeout)
                    .setReconnectionMax(reconnectionMax);

            return remoteServiceConfig;
        });
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

    public boolean isAutoreconnectEnabled() {
        return autoreconnectEnabled;
    }

    public RemoteServiceConfig setAutoreconnectEnabled(boolean autoreconnectEnabled) {
        this.autoreconnectEnabled = autoreconnectEnabled;
        return this;
    }

    public int getReconnectionInitialInterval() {
        return reconnectionInitialInterval;
    }

    public RemoteServiceConfig setReconnectionInitialInterval(int reconnectionInitialInterval) {
        this.reconnectionInitialInterval = reconnectionInitialInterval;
        return this;
    }

    public int getReconnectionIntervalMutiplier() {
        return reconnectionIntervalMutiplier;
    }

    public RemoteServiceConfig setReconnectionIntervalMutiplier(int reconnectionIntervalMutiplier) {
        this.reconnectionIntervalMutiplier = reconnectionIntervalMutiplier;
        return this;
    }

    public int getReconnectionTimeout() {
        return reconnectionTimeout;
    }

    public RemoteServiceConfig setReconnectionTimeout(int reconnectionTimeout) {
        this.reconnectionTimeout = reconnectionTimeout;
        return this;
    }

    public int getReconnectionMax() {
        return reconnectionMax;
    }

    public RemoteServiceConfig setReconnectionMax(int reconnectionMax) {
        this.reconnectionMax = reconnectionMax;
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
