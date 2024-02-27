/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.reporter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractReportNodeAdder implements ReportNodeAdder {

    private final ReportNode parent;
    private final Map<String, TypedValue> values = new LinkedHashMap<>();
    private String key;
    private String messageTemplate;

    protected AbstractReportNodeAdder(ReportNode parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    protected abstract ReportNode createReportNode(String key, String messageTemplate, Map<String, TypedValue> values, ReportNode parent);

    @Override
    public ReportNode add() {
        ReportNode node = createReportNode(key, messageTemplate, values, parent);
        parent.addChild(node);
        return node;
    }

    @Override
    public ReportNodeAdder withKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public ReportNodeAdder withMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
        return this;
    }

    @Override
    public ReportNodeAdder withTypedValue(String key, String value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    @Override
    public ReportNodeAdder withValue(String key, String value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    @Override
    public ReportNodeAdder withTypedValue(String key, double value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    @Override
    public ReportNodeAdder withValue(String key, double value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    @Override
    public ReportNodeAdder withTypedValue(String key, float value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    @Override
    public ReportNodeAdder withValue(String key, float value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    @Override
    public ReportNodeAdder withTypedValue(String key, int value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    @Override
    public ReportNodeAdder withValue(String key, int value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    @Override
    public ReportNodeAdder withTypedValue(String key, long value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    @Override
    public ReportNodeAdder withValue(String key, long value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    @Override
    public ReportNodeAdder withTypedValue(String key, boolean value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    @Override
    public ReportNodeAdder withValue(String key, boolean value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    @Override
    public ReportNodeAdder withSeverity(TypedValue severity) {
        if (!severity.getType().equals(TypedValue.SEVERITY)) {
            throw new IllegalArgumentException("Expected a " + TypedValue.SEVERITY + " but received " + severity.getType());
        }
        values.put(ReportConstants.REPORT_SEVERITY_KEY, severity);
        return this;
    }
}
