/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbUrl {

    private static final int MAX_VALUE_LENGTH = 100;

    private final HistoDbConfig config;

    private final String path;

    private final Map<String, String> query;

    public HistoDbUrl(HistoDbConfig config, String path, Map<String, String> query) {
        this.config = config;
        this.path = path;
        this.query = query;
    }

    public HistoDbConfig getConfig() {
        return config;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    private static String formatQuery(Map<String, String> query, boolean pretty) {
        StringBuilder str = new StringBuilder();
        for (Iterator<Map.Entry<String, String>> it = query.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            String value = pretty && entry.getValue().length() > MAX_VALUE_LENGTH ? entry.getValue().substring(0, MAX_VALUE_LENGTH) + "..." : entry.getValue();
            str.append(entry.getKey()).append("=").append(value);
            if (it.hasNext()) {
                str.append("&");
            }
        }
        return str.toString();
    }

    private static String formatUrl(HistoDbConfig config, String path, Map<String, String> query, boolean pretty) {
        StringBuilder url = new StringBuilder();
        url.append("https://")
                .append(config.getConnectionParameters().getHost())
                .append(":").append(config.getConnectionParameters().getPort())
                .append("/histodb/rest/")
                .append(config.getStoreName()).append("/");
        if (config.getSourceName() != null) {
            url.append(config.getSourceName()).append("/");
        }
        url.append(path);
        if (query.size() > 0) {
            url.append("?").append(formatQuery(query, pretty));
        }
        return url.toString();
    }

    public String format() {
        return formatUrl(config, path, query, false);
    }

    public String prettyFormat() {
        return formatUrl(config, path, query, true);
    }

    @Override
    public String toString() {
        return format();
    }

}
