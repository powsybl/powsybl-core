/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.statistic;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ProgramStateLog extends AbstractTimestampedLog {

    private final String programId;
    private final int total;

    private final boolean[] executions;

    public ProgramStateLog(String log) {
        Objects.requireNonNull(log);
        String[] split = log.split("\\s");
        String pid = split[1];
        int idx = Integer.parseInt(split[2]);
        int t = Integer.parseInt(split[3]);
        this.programId = pid;
        this.total = t;
        executions = new boolean[t];
        executions[idx] = true;
    }

    public String getProgramId() {
        return programId;
    }

    public int getTotal() {
        return total;
    }

    public boolean[] getExecutions() {
        return executions;
    }

    void update(long ts, int idx) {
        updateTimestamp(ts);
        executions[idx] = true;
    }

}
