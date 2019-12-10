/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesDataUpdated extends NodeEvent {

    public static final String TIME_SERIES_DATA_UPDATED = "TIME_SERIES_DATA_UPDATED";

    @JsonProperty("timeSeriesName")
    private final String timeSeriesName;

    @JsonCreator
    public TimeSeriesDataUpdated(@JsonProperty("id") String id,
                                 @JsonProperty("timeSeriesName") String timeSeriesName) {
        super(id, TIME_SERIES_DATA_UPDATED);
        this.timeSeriesName = Objects.requireNonNull(timeSeriesName);
    }

    public String getTimeSeriesName() {
        return timeSeriesName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timeSeriesName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesDataUpdated) {
            TimeSeriesDataUpdated other = (TimeSeriesDataUpdated) obj;
            return id.equals(other.id) && timeSeriesName.equals(other.timeSeriesName);
        }
        return false;
    }

    @Override
    public String toString() {
        return "TimeSeriesDataUpdated(id=" + id + ", timeSeriesName=" + timeSeriesName + ")";
    }
}
