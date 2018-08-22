/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.timeseries.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesJsonModule extends SimpleModule {

    public TimeSeriesJsonModule() {
        addSerializer(TimeSeriesMetadata.class, new TimeSeriesMetadataJsonSerializer());
        addSerializer(ArrayChunk.class, new ArrayChunkJsonSerializer());
        addSerializer(TimeSeries.class, new TimeSeriesJsonSerializer());

        addDeserializer(TimeSeriesMetadata.class, new TimeSeriesMetadataJsonDeserializer());
        addDeserializer(ArrayChunk.class, new ArrayChunkJsonDeserializer());
        addDeserializer(DoubleArrayChunk.class, new DoubleArrayChunkJsonDeserializer());
        addDeserializer(StringArrayChunk.class, new StringArrayChunkJsonDeserializer());
        addDeserializer(TimeSeries.class, new TimeSeriesJsonDeserializer());
        addDeserializer(DoubleTimeSeries.class, new DoubleTimeSeriesJsonDeserializer());
        addDeserializer(StringTimeSeries.class, new StringTimeSeriesJsonDeserializer());
    }
}
