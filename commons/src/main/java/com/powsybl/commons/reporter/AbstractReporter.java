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
public abstract class AbstractReporter implements Reporter {

    @Override
    public Reporter createChild(String taskKey, String defaultName) {
        return createChild(taskKey, defaultName, Collections.emptyMap());
    }

    @Override
    public Reporter createChild(String taskKey, String defaultName, String key, Object value) {
        return createChild(taskKey, defaultName, Map.of(key, value));
    }

    @Override
    public void report(String reportKey, String defaultLog) {
        report(reportKey, defaultLog, Collections.emptyMap());
    }

    @Override
    public void report(String reportKey, String defaultLog, String valueKey, Object value) {
        report(reportKey, defaultLog, Map.of(valueKey, value));
    }

}
