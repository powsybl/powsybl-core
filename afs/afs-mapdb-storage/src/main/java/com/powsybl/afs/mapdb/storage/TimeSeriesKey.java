/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesKey {

    private final UUID nodeUuid;

    private final int version;

    private final String timeSeriesName;

    public TimeSeriesKey(UUID nodeUuid, int version, String timeSeriesName) {
        this.nodeUuid = Objects.requireNonNull(nodeUuid);
        this.version = version;
        this.timeSeriesName = Objects.requireNonNull(timeSeriesName);
    }

    public UUID getNodeUuid() {
        return nodeUuid;
    }

    public int getVersion() {
        return version;
    }

    public String getTimeSeriesName() {
        return timeSeriesName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeUuid, version, timeSeriesName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesKey) {
            TimeSeriesKey other = (TimeSeriesKey) obj;
            return nodeUuid.equals(other.nodeUuid) &&
                    version == other.version &&
                    timeSeriesName.equals(other.timeSeriesName);
        }
        return false;
    }

    @Override
    public String toString() {
        return "TimeSeriesKey(nodeUuid=" + nodeUuid + ", version=" + version + ", timeSeriesName=" + timeSeriesName + ")";
    }
}
