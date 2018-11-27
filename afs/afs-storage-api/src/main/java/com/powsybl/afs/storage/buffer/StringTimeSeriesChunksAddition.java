/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.buffer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.timeseries.StringDataChunk;
import com.powsybl.timeseries.StringPoint;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringTimeSeriesChunksAddition extends AbstractTimeSeriesChunksAddition<StringPoint, StringDataChunk> {

    @JsonCreator
    public StringTimeSeriesChunksAddition(@JsonProperty("nodeId") String nodeId,
                                          @JsonProperty("version") int version,
                                          @JsonProperty("timeSeriesName") String timeSeriesName,
                                          @JsonProperty("chunks") List<StringDataChunk> chunks) {
        super(nodeId, version, timeSeriesName, chunks);
    }

    @Override
    public StorageChangeType getType() {
        return StorageChangeType.STRING_TIME_SERIES_CHUNKS_ADDITION;
    }
}
