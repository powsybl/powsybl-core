/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import eu.itesla_project.commons.net.ConnectionParameters;
import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbConfig {

    private final ConnectionParameters connectionParameters;

    private final ConnectionParameters proxyParameters;

    private final String storeName;

    private String sourceName;

    public static HistoDbConfig load() {
        return load("histodb");
    }

    public static HistoDbConfig load(String moduleName) {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig(moduleName);
        String histoDbHost = config.getStringProperty("histoDbHost");
        int histoDbPort = config.getIntProperty("histoDbPort");
        String histoDbUserName = config.getStringProperty("histoDbUserName");
        String histoDbPassword = config.getStringProperty("histoDbPassword");
        ConnectionParameters proxyParameters = null;
        String proxyHost = config.getStringProperty("proxyHost", null);
        if (proxyHost != null) {
            int proxyPort = config.getIntProperty("proxyPort");
            String proxyUserName = config.getStringProperty("proxyUserName");
            String proxyPassword = config.getStringProperty("proxyPassword");
            proxyParameters = new ConnectionParameters(proxyHost, proxyPort, proxyUserName, proxyPassword);
        }
        String histoDbStoreName = config.getStringProperty("histoDbStoreName");
        String histoDbSourceName = config.getStringProperty("histoDbSourceName", null);
        return new HistoDbConfig(new ConnectionParameters(histoDbHost, histoDbPort, histoDbUserName, histoDbPassword), proxyParameters, histoDbStoreName, histoDbSourceName);
    }

    public HistoDbConfig(ConnectionParameters connectionParameters, ConnectionParameters proxyParameters, String storeName, String sourceName) {
        this.connectionParameters = Objects.requireNonNull(connectionParameters);
        this.proxyParameters = proxyParameters;
        this.storeName = Objects.requireNonNull(storeName);
        this.sourceName= sourceName;
    }

    public ConnectionParameters getConnectionParameters() {
        return connectionParameters;
    }

    public ConnectionParameters getProxyParameters() {
        return proxyParameters;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public HistoDbConfig clone() {
        return new HistoDbConfig(connectionParameters, proxyParameters, storeName, sourceName);
    }
}
