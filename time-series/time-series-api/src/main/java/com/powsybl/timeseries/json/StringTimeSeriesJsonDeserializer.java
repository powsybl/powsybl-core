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
import com.powsybl.timeseries.StringTimeSeries;
import com.powsybl.timeseries.TimeSeries;
import com.powsybl.timeseries.TimeSeriesException;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringTimeSeriesJsonDeserializer extends StdDeserializer<StringTimeSeries> {

    public StringTimeSeriesJsonDeserializer() {
        super(StringTimeSeries.class);
    }

    @Override
    public StringTimeSeries deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        List<TimeSeries> timeSeriesList = TimeSeries.parseJson(jsonParser, true);
        if (timeSeriesList.size() != 1 || !(timeSeriesList.get(0) instanceof StringTimeSeries)) {
            throw new TimeSeriesException("String time series JSON deserialization error");
        }
        return (StringTimeSeries) timeSeriesList.get(0);
    }
}
