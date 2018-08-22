/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.timeseries.ArrayChunk;
import com.powsybl.timeseries.DoubleArrayChunk;
import com.powsybl.timeseries.StringArrayChunk;
import com.powsybl.timeseries.TimeSeriesException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ArrayChunkJsonDeserializer extends StdDeserializer<ArrayChunk> {

    public ArrayChunkJsonDeserializer() {
        super(ArrayChunk.class);
    }

    @Override
    public ArrayChunk deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        List<DoubleArrayChunk> doubleChunks = new ArrayList<>();
        List<StringArrayChunk> stringChunks = new ArrayList<>();
        ArrayChunk.parseJson(jsonParser, doubleChunks, stringChunks, true);
        if (doubleChunks.size() == 1) {
            return doubleChunks.get(0);
        } else if (stringChunks.size() == 1) {
            return stringChunks.get(0);
        } else {
            throw new TimeSeriesException("Array chunk JSON deserialization error");
        }
    }
}
