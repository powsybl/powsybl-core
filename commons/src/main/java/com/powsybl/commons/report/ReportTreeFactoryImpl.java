/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.powsybl.commons.PowsyblException;

import java.util.Locale;
import java.util.Map;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportTreeFactoryImpl implements ReportTreeFactory {

    private final Locale locale;
    private final String defaultTimestampPattern;

    public ReportTreeFactoryImpl() {
        this(null);
    }

    public ReportTreeFactoryImpl(Locale locale) {
        this(locale, null);
    }

    public ReportTreeFactoryImpl(Locale locale, String defaultTimestampPattern) {
        this.locale = locale;
        this.defaultTimestampPattern = defaultTimestampPattern;
    }

    @Override
    public ReportNodeImpl createRoot(String key, Map<String, TypedValue> values, TreeContext treeContext, MessageTemplateProvider messageTemplateProvider) {
        return ReportNodeImpl.createRootReportNode(key, values, treeContext, messageTemplateProvider);
    }

    @Override
    public ReportNode createChild(String key, Map<String, TypedValue> values, ReportNode parent, MessageTemplateProvider messageTemplateProvider) {
        if (!(parent instanceof ReportNodeImpl parentReportNodeImpl)) {
            throw new PowsyblException("Unexpected parent class: " + parent.getClass().getSimpleName() + " (expected: " + ReportNodeImpl.class.getName() + ")");
        }
        return ReportNodeImpl.createChildReportNode(key, values, parentReportNodeImpl, messageTemplateProvider);
    }

    @Override
    public TreeContext createTreeContext() {
        return new TreeContextImpl(locale, defaultTimestampPattern, this);
    }

}
