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
public abstract class AbstractTimestampedLog {

    protected long timestamp = 0L;

    AbstractTimestampedLog(long timestamp) {
        this.timestamp = Objects.requireNonNull(timestamp);
    }

    protected AbstractTimestampedLog() {
    }

    protected final void updateTimestamp(long ts) {
        if (ts > timestamp) {
            timestamp = ts;
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

}
