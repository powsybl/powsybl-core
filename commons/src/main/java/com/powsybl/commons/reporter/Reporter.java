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

    void startTask(String taskKey, String defaultName, Map<String, Object> values);

    default void startTask(String taskKey, String defaultName) {
        startTask(taskKey, defaultName, Collections.emptyMap());
    }

    default void startTask(String taskKey, String defaultName, String key, Object value) {
        startTask(taskKey, defaultName, Map.of(key, value));
    }

    void addTaskValue(String key, Object value);

    void endTask();

    void report(String reportKey, String defaultLog, Map<String, Object> values);

    void report(String reportKey, String defaultLog, Map<String, Object> values, Marker marker);

    default void report(String reportKey, String defaultLog) {
        report(reportKey, defaultLog, Collections.emptyMap());
    }

    default void report(String reportKey, String defaultLog, String valueKey, Object value) {
        report(reportKey, defaultLog, Map.of(valueKey, value));
    }

    default void report(String reportKey, String defaultLog, Marker marker) {
        report(reportKey, defaultLog, Collections.emptyMap(), marker);
    }

    default void report(String reportKey, String defaultLog, String valueKey, Object value, Marker marker) {
        report(reportKey, defaultLog, Map.of(valueKey, value), marker);
    }

    class NoOpImpl implements Reporter {
        @Override
        public void startTask(String taskKey, String defaultName, Map<String, Object> values) {
        }

        @Override
        public void addTaskValue(String key, Object value) {
        }

        @Override
        public void endTask() {
        }

        @Override
        public void report(String reportKey, String defaultLog, Map<String, Object> values) {
        }

        @Override
        public void report(String reportKey, String defaultLog, Map<String, Object> values, Marker marker) {
        }
    }
}
