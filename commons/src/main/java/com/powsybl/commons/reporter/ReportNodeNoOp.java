/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * A default no-op implementation
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeNoOp implements ReportNode {

    private static final Deque<Map<String, TypedValue>> EMPTY_MAP_DEQUE = new ArrayDeque<>();

    @Override
    public ReportNode report(String key, String messageTemplate, Map<String, TypedValue> values) {
        return new ReportNodeNoOp();
    }

    @Override
    public ReportNode report(String key, String messageTemplate) {
        return new ReportNodeNoOp();
    }

    @Override
    public ReportNode report(String key, String messageTemplate, String valueKey, Object value) {
        return new ReportNodeNoOp();
    }

    @Override
    public ReportNode report(String key, String messageTemplate, String valueKey, Object value, String type) {
        return new ReportNodeNoOp();
    }

    @Override
    public ReportNodeAdder newReportNode() {
        return new Adder();
    }

    @Override
    public void addChild(ReportNode reportNode) {
        // No-op
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public Deque<Map<String, TypedValue>> getValuesDeque() {
        return EMPTY_MAP_DEQUE;
    }

    @Override
    public Optional<TypedValue> getValue(String valueKey) {
        return Optional.empty();
    }

    @Override
    public Collection<ReportNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public void writeJson(JsonGenerator generator, Map<String, String> dictionary) throws IOException {
        // No-op
    }

    @Override
    public void print(Writer writer) throws IOException {
        // No-op
    }

    @Override
    public void print(Writer writer, String indentationStart) throws IOException {
        // No-op
    }

    private static class Adder implements ReportNodeAdder {
        @Override
        public ReportNode add() {
            return new ReportNodeNoOp();
        }

        @Override
        public ReportNodeAdder withKey(String key) {
            return this;
        }

        @Override
        public ReportNodeAdder withMessageTemplate(String messageTemplate) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, String value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withValue(String key, String value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, double value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withValue(String key, double value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, float value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withValue(String key, float value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, int value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withValue(String key, int value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, long value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withValue(String key, long value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, boolean value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withValue(String key, boolean value) {
            return this;
        }

        @Override
        public ReportNodeAdder withSeverity(TypedValue severity) {
            return this;
        }
    }
}
