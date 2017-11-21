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
public class StringTimeSeriesTest {

    @Test
    public void test() throws IOException {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"),
                                                                     Duration.ofMinutes(15), 1, 1);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.STRING, Collections.emptyMap(), index);
        UncompressedStringArrayChunk chunk = new UncompressedStringArrayChunk(2, new String[] {"a", "b"});
        CompressedStringArrayChunk chunk2 = new CompressedStringArrayChunk(5, 3, new String[] {"c", "d"}, new int[] {1, 2});
        assertEquals(TimeSeriesDataType.STRING, chunk.getDataType());
        StringTimeSeries timeSeries = new StringTimeSeries(metadata, chunk, chunk2);
        assertSame(metadata, timeSeries.getMetadata());
        assertEquals(Arrays.asList(chunk, chunk2), timeSeries.getChunks());
        assertArrayEquals(new String[] {null, null, "a", "b", null, "c", "d", "d"}, timeSeries.toArray());
        StringPoint[] pointsRef = {new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z").toEpochMilli(), null),
                                   new StringPoint(2, Instant.parse("2015-01-01T00:30:00Z").toEpochMilli(), "a"),
                                   new StringPoint(3, Instant.parse("2015-01-01T00:45:00Z").toEpochMilli(), "b"),
                                   new StringPoint(4, Instant.parse("2015-01-01T01:00:00Z").toEpochMilli(), null),
                                   new StringPoint(5, Instant.parse("2015-01-01T01:15:00Z").toEpochMilli(), "c"),
                                   new StringPoint(6, Instant.parse("2015-01-01T01:30:00Z").toEpochMilli(), "d")};
        assertArrayEquals(pointsRef, timeSeries.stream().toArray());
        assertArrayEquals(pointsRef, Iterators.toArray(timeSeries.iterator(), StringPoint.class));

        // json test
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"metadata\" : {",
                "    \"name\" : \"ts1\",",
                "    \"dataType\" : \"STRING\",",
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
                "    \"values\" : [ \"a\", \"b\" ]",
                "  }, {",
                "    \"offset\" : 5,",
                "    \"uncompressedLength\" : 3,",
                "    \"stepValues\" : [ \"c\", \"d\" ],",
                "    \"stepLengths\" : [ 1, 2 ]",
                "  } ]",
                "}");
        String json = JsonUtil.toJson(timeSeries::writeJson);
        assertEquals(jsonRef, json);
        List<TimeSeries> timeSeriesList = TimeSeries.parseJson(json);
        assertEquals(1, timeSeriesList.size());
        String json2 = JsonUtil.toJson(timeSeriesList.get(0)::writeJson);
        assertEquals(json, json2);

        // test json with object mapper
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new TimeSeriesJsonModule());

        assertEquals(timeSeries, objectMapper.readValue(objectMapper.writeValueAsString(timeSeries), StringTimeSeries.class));
    }
}
