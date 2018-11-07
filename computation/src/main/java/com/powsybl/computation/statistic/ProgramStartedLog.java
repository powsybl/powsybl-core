/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.statistic;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ProgramStartedLog extends ProgramStateLog {

    public ProgramStartedLog(String log) {
        super(log);
    }

    public boolean[] executionsStarted() {
        return getExecutions();
    }
}
