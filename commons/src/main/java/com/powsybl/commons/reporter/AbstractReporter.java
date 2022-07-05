/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.Collections;
import java.util.Map;

/**
 * An abstract class providing some default method implementations for {@link Reporter} implementations.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractReporter extends AbstractReportNode implements Reporter {

    protected AbstractReporter(String key, String defaultTitle, Map<String, TypedValue> values) {
        super(key, defaultTitle, values);
    }

    @Override
    public Reporter createSubReporter(String key, String defaultTitle) {
        return createSubReporter(key, defaultTitle, Collections.emptyMap());
    }

    @Override
    public Reporter createSubReporter(String reporterKey, String defaultTitle, String valueKey, Object value) {
        return createSubReporter(reporterKey, defaultTitle, valueKey, value, TypedValue.UNTYPED);
    }

    @Override
    public Reporter createSubReporter(String reporterKey, String defaultTitle, String valueKey, Object value, String type) {
        return createSubReporter(reporterKey, defaultTitle, Map.of(valueKey, new TypedValue(value, type)));
    }

    @Override
    public void report(String messageKey, String defaultMessage, Map<String, TypedValue> values) {
        report(new ReportMessage(messageKey, defaultMessage, values));
    }

    @Override
    public void report(String messageKey, String defaultMessage) {
        report(messageKey, defaultMessage, Collections.emptyMap());
    }

    @Override
    public void report(String messageKey, String defaultMessage, String valueKey, Object value) {
        report(messageKey, defaultMessage, valueKey, value, TypedValue.UNTYPED);
    }

    @Override
    public void report(String messageKey, String defaultMessage, String valueKey, Object value, String type) {
        report(messageKey, defaultMessage, Map.of(valueKey, new TypedValue(value, type)));
    }
}
