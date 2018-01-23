/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class TimeSeriesChunkKey {

    private final TimeSeriesKey timeSeriesKey;

    private final int chunk;

    public TimeSeriesChunkKey(TimeSeriesKey timeSeriesKey, int chunk) {
        this.timeSeriesKey = Objects.requireNonNull(timeSeriesKey);
        this.chunk = chunk;
    }

    public TimeSeriesKey getTimeSeriesKey() {
        return timeSeriesKey;
    }

    public int getChunk() {
        return chunk;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeSeriesKey, chunk);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesChunkKey) {
            TimeSeriesChunkKey other = (TimeSeriesChunkKey) obj;
            return timeSeriesKey.equals(other.timeSeriesKey) &&
                    chunk == other.chunk;
        }
        return false;
    }

    @Override
    public String toString() {
        return "TimeSeriesChunkKey(key=" + timeSeriesKey + ", chunk=" + chunk + ")";
    }
}
