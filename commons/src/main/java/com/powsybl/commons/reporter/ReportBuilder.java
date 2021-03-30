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

    private final Map<String, TypedValue> values = new HashMap<>();
    private String reportKey;
    private String defaultMessage;

    public Report build() {
        return new Report(reportKey, defaultMessage, values);
    }

    public ReportBuilder withKey(String reportKey) {
        this.reportKey = reportKey;
        return this;
    }

    public ReportBuilder withDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
        return this;
    }

    public ReportBuilder withTypedValue(String key, String value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    public ReportBuilder withValue(String key, String value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    public ReportBuilder withTypedValue(String key, double value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    public ReportBuilder withValue(String key, double value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    public ReportBuilder withTypedValue(String key, float value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    public ReportBuilder withValue(String key, float value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    public ReportBuilder withTypedValue(String key, int value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    public ReportBuilder withValue(String key, int value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    public ReportBuilder withTypedValue(String key, long value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    public ReportBuilder withValue(String key, long value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    public ReportBuilder withTypedValue(String key, boolean value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    public ReportBuilder withValue(String key, boolean value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    public ReportBuilder withSeverity(TypedValue severity) {
        values.put(Report.REPORT_SEVERITY_KEY, severity);
        return this;
    }

}
