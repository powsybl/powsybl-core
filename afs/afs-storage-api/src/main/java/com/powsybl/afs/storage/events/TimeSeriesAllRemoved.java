/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesAllRemoved extends NodeEvent {

    @JsonCreator
    public TimeSeriesAllRemoved(@JsonProperty("id") String id) {
        super(id, NodeEventType.TIME_SERIES_ALL_REMOVED);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesAllRemoved) {
            TimeSeriesAllRemoved other = (TimeSeriesAllRemoved) obj;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public String toString() {
        return "TimeSeriesAllRemoved(id=" + id + ")";
    }
}
