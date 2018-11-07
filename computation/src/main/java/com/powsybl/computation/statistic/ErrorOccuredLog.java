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
public class ErrorOccuredLog extends ProgramStateLog {

    private final int exitCode;

    public ErrorOccuredLog(String log) {
        super(log);
        String[] split = log.split("\\s");
        exitCode = Integer.parseInt(split[4]);
    }

    public int getExitCode() {
        return exitCode;
    }
}
