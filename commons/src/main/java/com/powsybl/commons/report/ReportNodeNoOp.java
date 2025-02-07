/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * A default no-op implementation
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeNoOp implements ReportNode {

    @Override
    public ReportNodeAdder newReportNode() {
        return new ChildAdder();
    }

    @Override
    public TreeContext getTreeContext() {
        return null;
    }

    @Override
    public void include(ReportNode reportRoot) {
        // No-op
    }

    @Override
    public String getMessageKey() {
        return null;
    }

    @Override
    public String getMessageTemplate() {
        return null;
    }

    @Override
    public String getMessage(Formatter formatter) {
        return null;
    }

    @Override
    public Map<String, TypedValue> getValues() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<TypedValue> getValue(String valueKey) {
        return Optional.empty();
    }

    @Override
    public List<ReportNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public void writeJson(JsonGenerator generator) throws IOException {
        // No-op
    }

    @Override
    public void print(Writer writer, Formatter formatter) throws IOException {
        // No-op
    }

    private static class ChildAdder implements ReportNodeAdder {
        @Override
        public ReportNode add() {
            return new ReportNodeNoOp();
        }

        @Override
        public ReportNodeAdder withMessageTemplate(String key, String messageTemplate) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, String value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withUntypedValue(String key, String value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, double value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withUntypedValue(String key, double value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, float value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withUntypedValue(String key, float value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, int value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withUntypedValue(String key, int value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, long value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withUntypedValue(String key, long value) {
            return this;
        }

        @Override
        public ReportNodeAdder withTypedValue(String key, boolean value, String type) {
            return this;
        }

        @Override
        public ReportNodeAdder withUntypedValue(String key, boolean value) {
            return this;
        }

        @Override
        public ReportNodeAdder withSeverity(TypedValue severity) {
            return this;
        }

        @Override
        public ReportNodeAdder withSeverity(String severity) {
            return this;
        }
    }
}
