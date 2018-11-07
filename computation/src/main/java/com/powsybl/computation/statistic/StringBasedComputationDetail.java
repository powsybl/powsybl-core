/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.statistic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class StringBasedComputationDetail extends AbstractComputationDetail {

    public static final char PROGRAM_STARTED = 'S';
    public static final char PROGRAM_DONE = 'D';
    public static final char PROGRAM_ERROR_OCCURED = 'E';

    private final List<String> rawLogs = new ArrayList<>();

    public boolean updateProgramStarted(String log) {
        insertRaw(log);
        return updateStateLog(log, () -> programStartedLog, ProgramStartedLog::new, l -> programStartedLog = (ProgramStartedLog) l);
    }

    public boolean updateProgramDone(String log) {
        insertRaw(log);
        return updateStateLog(log, () -> programDoneLog, ProgramDoneLog::new, l -> programDoneLog = (ProgramDoneLog) l);
    }

    public boolean updateProgramError(String log) {
        insertRaw(log);
        return updateStateLog(log, () -> errorOccuredLog, ErrorOccuredLog::new, l -> errorOccuredLog = (ErrorOccuredLog) l);
    }

    private boolean updateStateLog(String log, Supplier<ProgramStateLog> logSupplier, Function<String, ProgramStateLog> stateInitializer, Consumer<ProgramStateLog> logUpdater) {
        String stateLogStr = log.substring(2);
        String[] split = stateLogStr.split("\\s");
        long l = Long.parseLong(split[0]);
        lastTimestamp = l > lastTimestamp ? l : lastTimestamp;
        String programId = split[1];
        int idx = Integer.parseInt(split[2]);
        ProgramStateLog stateLog = logSupplier.get();
        if (stateLog == null) {
            logUpdater.accept(stateInitializer.apply(stateLogStr));
            return true;
        }

        if (programId.equals(stateLog.getProgramId())) {
            // a new execution job event
            stateLog.update(l, idx);
            return true;
        } else {
            if (l > stateLog.getTimestamp()) {
                // following job event
                logUpdater.accept(stateInitializer.apply(stateLogStr));
                return true;
            } else {
                // an old log detected after following job started
                // ignore
                return false;
            }
        }
    }

    private void insertRaw(String log) {
        rawLogs.add(Objects.requireNonNull(log));
    }

    public enum LogType {

        STARTED('S'),
        DONE('D'),
        ERROR('E');

        final char flag;

        LogType(char flag) {
            this.flag = flag;
        }

        public char getFlag() {
            return flag;
        }
    }
}
