/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.timeseries.*;
import com.powsybl.timeseries.ast.NodeCalc;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class TimeSeriesJsonModule extends SimpleModule {

    public TimeSeriesJsonModule() {
        addSerializer(TimeSeriesMetadata.class, new TimeSeriesMetadataJsonSerializer());
        addSerializer(DataChunk.class, new DataChunkJsonSerializer());
        addSerializer(TimeSeries.class, new TimeSeriesJsonSerializer());
        addSerializer(NodeCalc.class, new NodeCalcJsonSerializer());

        addDeserializer(TimeSeriesMetadata.class, new TimeSeriesMetadataJsonDeserializer());
        addDeserializer(DataChunk.class, new DataChunkJsonDeserializer());
        addDeserializer(DoubleDataChunk.class, new DoubleDataChunkJsonDeserializer());
        addDeserializer(StringDataChunk.class, new StringDataChunkJsonDeserializer());
        addDeserializer(TimeSeries.class, new TimeSeriesJsonDeserializer());
        addDeserializer(DoubleTimeSeries.class, new DoubleTimeSeriesJsonDeserializer());
        addDeserializer(StringTimeSeries.class, new StringTimeSeriesJsonDeserializer());
        addDeserializer(NodeCalc.class, new NodeCalcJsonDeserializer());
    }
}
