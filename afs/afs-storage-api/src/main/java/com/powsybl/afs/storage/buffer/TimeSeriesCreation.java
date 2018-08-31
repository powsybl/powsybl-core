/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.buffer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.timeseries.TimeSeriesMetadata;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesCreation extends AbstractStorageChange {

    @JsonProperty("metadata")
    private TimeSeriesMetadata metadata;

    @JsonCreator
    public TimeSeriesCreation(@JsonProperty("nodeId") String nodeId,
                              @JsonProperty("metadata") TimeSeriesMetadata metadata) {
        super(nodeId);
        this.metadata = Objects.requireNonNull(metadata);
    }

    @Override
    public StorageChangeType getType() {
        return StorageChangeType.TIME_SERIES_CREATION;
    }

    @Override
    public long getEstimatedSize() {
        return 0;
    }

    public TimeSeriesMetadata getMetadata() {
        return metadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesCreation) {
            TimeSeriesCreation other = (TimeSeriesCreation) obj;
            return nodeId.equals(other.nodeId)
                    && metadata.equals(other.metadata);
        }
        return false;
    }
}

