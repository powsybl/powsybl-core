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
    public ReportNodeChildAdder newReportNode() {
        return new ChildAdder();
    }

    @Override
    public void addChildren(ReportRoot reportRoot) {
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
    public Collection<Map<String, TypedValue>> getValuesMapsInheritance() {
        return EMPTY_MAP_DEQUE;
    }

    @Override
    public Optional<TypedValue> getValue(String valueKey) {
        return Optional.empty();
    }

    @Override
    public Collection<ReportNode> getReportNodes() {
        return Collections.emptyList();
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
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

    private static class ChildAdder implements ReportNodeChildAdder {
        @Override
        public ReportNode add() {
            return new ReportNodeNoOp();
        }

        @Override
        public ReportNodeChildAdder withMessageTemplate(String key, String messageTemplate) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withTypedValue(String key, String value, String type) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withUntypedValue(String key, String value) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withTypedValue(String key, double value, String type) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withUntypedValue(String key, double value) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withTypedValue(String key, float value, String type) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withUntypedValue(String key, float value) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withTypedValue(String key, int value, String type) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withUntypedValue(String key, int value) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withTypedValue(String key, long value, String type) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withUntypedValue(String key, long value) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withTypedValue(String key, boolean value, String type) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withUntypedValue(String key, boolean value) {
            return this;
        }

        @Override
        public ReportNodeChildAdder withSeverity(TypedValue severity) {
            return this;
        }
    }
}
