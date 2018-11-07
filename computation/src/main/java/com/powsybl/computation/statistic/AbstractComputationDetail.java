/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.statistic;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public abstract class AbstractComputationDetail implements ComputationDetail {

    protected ProgramStartedLog programStartedLog;
    protected ProgramDoneLog programDoneLog;
    protected ErrorOccuredLog errorOccuredLog;

    protected long lastTimestamp = 0L;

    private final List<String> raw = new ArrayList<>();

    protected final void addRaw(String log) {
        raw.add(log);
    }

    @Override
    public ProgramStartedLog getProgramStartedLog() {
        return programStartedLog;
    }

    @Override
    public ProgramDoneLog getProgramDoneLog() {
        return programDoneLog;
    }

    @Override
    public ErrorOccuredLog getErrorOccuredLog() {
        return errorOccuredLog;
    }
}
