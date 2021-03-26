/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class Report {

    public static final String REPORT_SEVERITY_KEY = "reportSeverity";
    public static final String SEVERITY_TRACE = "TRACE";
    public static final String SEVERITY_DEBUG = "DEBUG";
    public static final String SEVERITY_INFO = "INFO";
    public static final String SEVERITY_WARN = "WARN";
    public static final String SEVERITY_ERROR = "ERROR";

    private final String reportKey;
    private final String defaultMessage;
    private final Map<String, Object> values;

    public Report(String reportKey, String defautlMessage, Map<String, Object> values) {
        this.reportKey = Objects.requireNonNull(reportKey);
        this.defaultMessage = defautlMessage;
        this.values = new HashMap<>(Objects.requireNonNull(values));
    }

    public static ReportBuilder builder() {
        return new ReportBuilder();
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getReportKey() {
        return reportKey;
    }

    public Object getValue(String valueKey) {
        return values.get(valueKey);
    }

    public Map<String, Object> getValues() {
        return Collections.unmodifiableMap(values);
    }

}
