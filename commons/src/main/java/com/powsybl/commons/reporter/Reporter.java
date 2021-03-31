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
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface Reporter {

    Reporter NO_OP = new NoOpImpl();

    Reporter createSubReporter(String taskKey, String defaultName, Map<String, TypedValue> values);

    Reporter createSubReporter(String taskKey, String defaultName);

    Reporter createSubReporter(String taskKey, String defaultName, String key, Object value);

    Reporter createSubReporter(String taskKey, String defaultName, String key, Object value, String type);

    void report(String reportKey, String defaultMessage, Map<String, TypedValue> values);

    void report(String reportKey, String defaultMessage);

    void report(String reportKey, String defaultMessage, String valueKey, Object value);

    void report(String reportKey, String defaultMessage, String valueKey, Object value, String type);

    void report(Report report);

    class NoOpImpl extends AbstractReporter {
        public NoOpImpl() {
            super("noOp", "NoOp", Collections.emptyMap());
        }

        @Override
        public Reporter createSubReporter(String taskKey, String defaultName, Map<String, TypedValue> values) {
            return new NoOpImpl();
        }

        @Override
        public void report(Report report) {
            // No-op
        }

    }
}
