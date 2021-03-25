/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class ReportAdderImpl implements ReportAdder {

    private final Reporter reporter;
    private final Map<String, Object> values = new HashMap<>();
    private String reportKey;
    private String defaultLog;

    protected ReportAdderImpl(Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public ReportAdder setKey(String reportKey) {
        this.reportKey = reportKey;
        return this;
    }

    @Override
    public ReportAdder setDefaultLog(String defaultLog) {
        this.defaultLog = defaultLog;
        return this;
    }

    @Override
    public ReportAdder addValue(String key, Object value) {
        values.put(key, value);
        return this;
    }

    @Override
    public ReportAdder setGravity(String gravity) {
        values.put(Reporter.REPORT_GRAVITY, gravity);
        return this;
    }

    @Override
    public void add() {
        this.reporter.report(reportKey, defaultLog, values);
    }
}
