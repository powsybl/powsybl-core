/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractReportNodeAdderOrBuilder<T extends ReportNodeAdderOrBuilder<T>> implements ReportNodeAdderOrBuilder<T> {

    protected final Map<String, TypedValue> values = new LinkedHashMap<>();
    protected String key;
    protected String messageTemplate;
    protected boolean withTimestamp = false;

    @Override
    public T withMessageTemplate(String key, String messageTemplate) {
        this.key = key;
        this.messageTemplate = messageTemplate;
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

    protected abstract T self();
}
