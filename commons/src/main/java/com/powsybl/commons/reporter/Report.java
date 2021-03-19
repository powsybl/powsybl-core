/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.Map;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class Report {
    private final String reportKey;
    private final String defaultLog;
    private final Map<String, Object> values;
    private final Marker marker;

    public Report(String reportKey, String defaultLog, Map<String, Object> values, Marker marker) {
        this.reportKey = reportKey;
        this.defaultLog = defaultLog;
        this.values = values;
        this.marker = marker;
    }

    public String getDefaultLog() {
        return defaultLog;
    }

    public String getReportKey() {
        return reportKey;
    }

    public Object getValue(String valueKey) {
        return values.get(valueKey);
    }

    public Marker getMarker() {
        return marker;
    }
}
