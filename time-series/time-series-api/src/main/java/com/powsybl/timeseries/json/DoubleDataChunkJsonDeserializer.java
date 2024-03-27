/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.timeseries.DataChunk;
import com.powsybl.timeseries.DoubleDataChunk;
import com.powsybl.timeseries.TimeSeriesException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DoubleDataChunkJsonDeserializer extends StdDeserializer<DoubleDataChunk> {

    public DoubleDataChunkJsonDeserializer() {
        super(DoubleDataChunk.class);
    }

    @Override
    public DoubleDataChunk deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        List<DoubleDataChunk> chunks = new ArrayList<>();
        DataChunk.parseJson(jsonParser, chunks, Collections.emptyList(), true);
        if (chunks.size() != 1) {
            throw new TimeSeriesException("Double array chunk JSON deserialization error");
        }
        return chunks.get(0);
    }
}
