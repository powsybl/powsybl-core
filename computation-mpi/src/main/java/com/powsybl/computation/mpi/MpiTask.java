/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import org.joda.time.DateTime;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MpiTask {

    private final int id;

    private final Core core;

    private final int index;

    private final byte[] message;

    private final DateTime startTime;

    private DateTime endTime;

    private byte[] resultMessage;

    MpiTask(int id, Core core, int index, byte[] message, DateTime startTime) {
        this.id = id;
        this.core = core;
        this.index = index;
        this.message = message;
        this.startTime = startTime;
    }

    int getId() {
        return id;
    }

    Core getCore() {
        return core;
    }

    int getRank() {
        return core.rank.num;
    }

    int getThread() {
        return core.thread;
    }

    int getIndex() {
        return index;
    }

    byte[] getMessage() {
        return message;
    }

    DateTime getStartTime() {
        return startTime;
    }

    DateTime getEndTime() {
        return endTime;
    }

    void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    void setResultMessage(byte[] resultMessage) {
        this.resultMessage = resultMessage;
    }

    byte[] getResultMessage() {
        return resultMessage;
    }

}
