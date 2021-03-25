/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class ReportBuilder {

    private final Map<String, Object> values = new HashMap<>();
    private String reportKey;
    private String defaultLog;

    public Report build() {
        return new Report(reportKey, defaultLog, values);
    }

    public ReportBuilder withKey(String reportKey) {
        this.reportKey = reportKey;
        return this;
    }

    public ReportBuilder withDefaultMessage(String defaultLog) {
        this.defaultLog = defaultLog;
        return this;
    }

    public ReportBuilder withValue(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public ReportBuilder withSeverity(String severity) {
        values.put(Reporter.REPORT_SEVERITY, severity);
        return this;
    }

}
