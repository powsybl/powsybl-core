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
public class TimeSeriesCreated extends NodeEvent {

    public static final String TYPE = "TIME_SERIES_CREATED";

    @JsonProperty("timeSeriesName")
    private final String timeSeriesName;

    @JsonCreator
    public TimeSeriesCreated(@JsonProperty("id") String id,
                             @JsonProperty("timeSeriesName") String timeSeriesName) {
        super(id, TYPE);
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
        if (obj instanceof TimeSeriesCreated) {
            TimeSeriesCreated other = (TimeSeriesCreated) obj;
            return id.equals(other.id) && timeSeriesName.equals(other.timeSeriesName);
        }
        return false;
    }

    @Override
    public String toString() {
        return "TimeSeriesCreated(id=" + id + ", timeSeriesName=" + timeSeriesName + ")";
    }
}
