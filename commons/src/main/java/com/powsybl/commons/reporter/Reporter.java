/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.Map;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface Reporter {

    Reporter NO_OP = new NoOpImpl();
    String REPORT_GRAVITY = "reportGravity";

    Reporter createChild(String taskKey, String defaultName, Map<String, Object> values);

    Reporter createChild(String taskKey, String defaultName);

    Reporter createChild(String taskKey, String defaultName, String key, Object value);

    void addTaskValue(String key, Object value);

    void report(String reportKey, String defaultLog, Map<String, Object> values);

    void report(String reportKey, String defaultLog);

    void report(String reportKey, String defaultLog, String valueKey, Object value);

    class NoOpImpl extends AbstractReporter {
        @Override
        public Reporter createChild(String taskKey, String defaultName, Map<String, Object> values) {
            return new NoOpImpl();
        }

        @Override
        public void addTaskValue(String key, Object value) {
            // No-op
        }

        @Override
        public void report(String reportKey, String defaultLog, Map<String, Object> values) {
            // No-op
        }

    }
}
