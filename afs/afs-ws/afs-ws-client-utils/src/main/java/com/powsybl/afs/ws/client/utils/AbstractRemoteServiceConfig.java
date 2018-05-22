/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.client.utils;

import com.powsybl.commons.exceptions.UncheckedUriSyntaxException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractRemoteServiceConfig<T extends AbstractRemoteServiceConfig<T>> {

    protected String hostName;

    protected String appName;

    protected int port;

    protected boolean secure;

    protected AbstractRemoteServiceConfig(String hostName, String appName, int port, boolean secure) {
        this.hostName = Objects.requireNonNull(hostName);
        this.appName = Objects.requireNonNull(appName);
        this.port = checkPort(port);
        this.secure = secure;
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

    public T setHostName(String hostName) {
        this.hostName = Objects.requireNonNull(hostName);
        return (T) this;
    }

    public String getAppName() {
        return appName;
    }

    public T setAppName(String appName) {
        this.appName = Objects.requireNonNull(appName);
        return (T) this;
    }

    public int getPort() {
        return port;
    }

    public T setPort(int port) {
        this.port = checkPort(port);
        return (T) this;
    }

    public boolean isSecure() {
        return secure;
    }

    public T setSecure(boolean secure) {
        this.secure = secure;
        return (T) this;
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
