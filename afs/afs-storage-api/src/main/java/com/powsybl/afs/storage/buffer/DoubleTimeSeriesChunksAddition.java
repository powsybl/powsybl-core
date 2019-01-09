/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.buffer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.timeseries.DoubleDataChunk;
import com.powsybl.timeseries.DoublePoint;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DoubleTimeSeriesChunksAddition extends AbstractTimeSeriesChunksAddition<DoublePoint, DoubleDataChunk> {

    @JsonCreator
    public DoubleTimeSeriesChunksAddition(@JsonProperty("nodeId") String nodeId,
                                          @JsonProperty("version") int version,
                                          @JsonProperty("timeSeriesName") String timeSeriesName,
                                          @JsonProperty("chunks") List<DoubleDataChunk> chunks) {
        super(nodeId, version, timeSeriesName, chunks);
    }

    @Override
    public StorageChangeType getType() {
        return StorageChangeType.DOUBLE_TIME_SERIES_CHUNKS_ADDITION;
    }
}
