/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.time.Instant;

import static com.powsybl.timeseries.TimeSeriesIndex.longToInstant;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractPoint {

    protected final int index;

    protected final Instant instant;

    protected AbstractPoint(int index, long time) {
        this(index, longToInstant(time, 1_000L));
    }

    protected AbstractPoint(int index, Instant instant) {
        if (index < 0) {
            throw new IllegalArgumentException("Bad index value " + index);
        }
        this.index = index;
        this.instant = instant;
    }

    public int getIndex() {
        return index;
    }

    /**
     * @deprecated Replaced by {@link #getInstant()}
     */
    @Deprecated(since = "6.7.0")
    public long getTime() {
        return instant.toEpochMilli();
    }

    public Instant getInstant() {
        return instant;
    }
}
