/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterators;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.math.timeseries.json.TimeSeriesJsonModule;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StoredDoubleTimeSeriesTest {

    @Test
    public void test() throws IOException {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"),
                                                                     Duration.ofMinutes(15), 1, 1);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleArrayChunk chunk = new UncompressedDoubleArrayChunk(2, new double[] {1d, 2d});
        CompressedDoubleArrayChunk chunk2 = new CompressedDoubleArrayChunk(5, 3, new double[] {3d, 4d}, new int[] {1, 2});
        assertEquals(TimeSeriesDataType.DOUBLE, chunk.getDataType());
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk, chunk2);
        assertSame(metadata, timeSeries.getMetadata());
        assertEquals(Arrays.asList(chunk, chunk2), timeSeries.getChunks());
        assertArrayEquals(new double[] {Double.NaN, Double.NaN, 1d, 2d, Double.NaN, 3d, 4d, 4d}, timeSeries.toArray(), 0d);
        DoublePoint[] pointsRef = {new DoublePoint(0, Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), Double.NaN),
                                   new DoublePoint(2, Instant.parse("2015-01-01T00:30:00Z").toEpochMilli(), 1d),
                                   new DoublePoint(3, Instant.parse("2015-01-01T00:45:00Z").toEpochMilli(), 2d),
                                   new DoublePoint(4, Instant.parse("2015-01-01T01:00:00Z").toEpochMilli(), Double.NaN),
                                   new DoublePoint(5, Instant.parse("2015-01-01T01:15:00Z").toEpochMilli(), 3d),
                                   new DoublePoint(6, Instant.parse("2015-01-01T01:30:00Z").toEpochMilli(), 4d)};
        assertArrayEquals(pointsRef, timeSeries.stream().toArray());
        assertArrayEquals(pointsRef, Iterators.toArray(timeSeries.iterator(), DoublePoint.class));

        // json test
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"metadata\" : {",
                "    \"name\" : \"ts1\",",
                "    \"dataType\" : \"DOUBLE\",",
                "    \"tags\" : [ ],",
                "    \"regularIndex\" : {",
                "      \"startTime\" : 1420070400000,",
                "      \"endTime\" : 1420076700000,",
                "      \"spacing\" : 900000,",
                "      \"firstVersion\" : 1,",
                "      \"versionCount\" : 1",
                "    }",
                "  },",
                "  \"chunks\" : [ {",
                "    \"offset\" : 2,",
                "    \"values\" : [ 1.0, 2.0 ]",
                "  }, {",
                "    \"offset\" : 5,",
                "    \"uncompressedLength\" : 3,",
                "    \"stepValues\" : [ 3.0, 4.0 ],",
                "    \"stepLengths\" : [ 1, 2 ]",
                "  } ]",
                "}"
               );
        String json = JsonUtil.toJson(timeSeries::writeJson);
        assertEquals(jsonRef, json);
        List<TimeSeries> timeSeriesList = TimeSeries.parseJson(json);
        assertEquals(1, timeSeriesList.size());
        String json2 = JsonUtil.toJson(timeSeriesList.get(0)::writeJson);
        assertEquals(json, json2);

        // test json with object mapper
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new TimeSeriesJsonModule());

        assertEquals(timeSeries, objectMapper.readValue(objectMapper.writeValueAsString(timeSeries), DoubleTimeSeries.class));
    }
}
