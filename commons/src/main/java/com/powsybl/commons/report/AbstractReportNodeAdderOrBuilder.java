/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractReportNodeAdderOrBuilder<S extends ReportNode, T extends ReportNodeAdderOrBuilder<T>>
        implements ReportNodeAdderOrBuilder<T> {

    protected final ReportNodeFactory<S> reportNodeFactory;
    protected final Map<String, TypedValue> values = new LinkedHashMap<>();
    protected String key;
    protected boolean withTimestamp = false;
    protected String timestampPattern;

    public AbstractReportNodeAdderOrBuilder(ReportNodeFactory<S> reportNodeFactory) {
        this.reportNodeFactory = reportNodeFactory;
    }

    @Override
    public T withMessageTemplate(String key) {
        this.key = key;
        return self();
    }

    @Override
    public T withTypedValue(String key, String value, String type) {
        values.put(key, TypedValue.of(value, type));
        return self();
    }

    @Override
    public T withUntypedValue(String key, String value) {
        values.put(key, TypedValue.untyped(value));
        return self();
    }

    @Override
    public T withTypedValue(String key, double value, String type) {
        values.put(key, TypedValue.of(value, type));
        return self();
    }

    @Override
    public T withUntypedValue(String key, double value) {
        values.put(key, TypedValue.untyped(value));
        return self();
    }

    @Override
    public T withTypedValue(String key, float value, String type) {
        values.put(key, TypedValue.of(value, type));
        return self();
    }

    @Override
    public T withUntypedValue(String key, float value) {
        values.put(key, TypedValue.untyped(value));
        return self();
    }

    @Override
    public T withTypedValue(String key, int value, String type) {
        values.put(key, TypedValue.of(value, type));
        return self();
    }

    @Override
    public T withUntypedValue(String key, int value) {
        values.put(key, TypedValue.untyped(value));
        return self();
    }

    @Override
    public T withTypedValue(String key, long value, String type) {
        values.put(key, TypedValue.of(value, type));
        return self();
    }

    @Override
    public T withUntypedValue(String key, long value) {
        values.put(key, TypedValue.untyped(value));
        return self();
    }

    @Override
    public T withTypedValue(String key, boolean value, String type) {
        values.put(key, TypedValue.of(value, type));
        return self();
    }

    @Override
    public T withUntypedValue(String key, boolean value) {
        values.put(key, TypedValue.untyped(value));
        return self();
    }

    @Override
    public T withSeverity(TypedValue severity) {
        TypedValue.checkSeverityType(severity);
        values.put(ReportConstants.SEVERITY_KEY, severity);
        return self();
    }

    @Override
    public T withSeverity(String severity) {
        values.put(ReportConstants.SEVERITY_KEY, TypedValue.of(severity, TypedValue.SEVERITY));
        return self();
    }

    @Override
    public T withTimestamp() {
        this.withTimestamp = true;
        return self();
    }

    @Override
    public T withTimestamp(String pattern) {
        this.withTimestamp = true;
        this.timestampPattern = Objects.requireNonNull(pattern);
        return self();
    }

    protected void addTimeStampValue(TreeContext treeContext) {
        DateTimeFormatter formatter = timestampPattern != null
                ? DateTimeFormatter.ofPattern(timestampPattern, treeContext.getLocale())
                : treeContext.getDefaultTimestampFormatter();
        values.put(ReportConstants.TIMESTAMP_KEY, TypedValue.getTimestamp(formatter));
    }

    protected abstract T self();
}
