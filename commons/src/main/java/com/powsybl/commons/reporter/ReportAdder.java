/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public interface ReportAdder {
    ReportAdder NO_OP = new NoOpImpl();

    ReportAdder setKey(String reportKey);

    ReportAdder setDefaultLog(String defaultLog);

    ReportAdder addValue(String key, Object value);

    ReportAdder setSeverity(String severity);

    void add();

    class NoOpImpl implements ReportAdder {
        @Override
        public ReportAdder setKey(String reportKey) {
            return this;
        }

        @Override
        public ReportAdder setDefaultLog(String defaultLog) {
            return this;
        }

        @Override
        public ReportAdder addValue(String key, Object value) {
            return this;
        }

        @Override
        public ReportAdder setSeverity(String severity) {
            return this;
        }

        @Override
        public void add() {
            // No-op
        }
    }
}
