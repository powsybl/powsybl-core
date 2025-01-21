/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterators;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.json.TimeSeriesJsonModule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.powsybl.timeseries.TimeSeriesIndex.dateParsedToNano;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class StoredDoubleTimeSeriesTest {

    @Test
    void test() throws IOException {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"),
                                                                     Duration.ofMinutes(15));
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleDataChunk chunk = new UncompressedDoubleDataChunk(2, new double[] {1d, 2d});
        CompressedDoubleDataChunk chunk2 = new CompressedDoubleDataChunk(5, 3, new double[] {3d, 4d}, new int[] {1, 2});
        assertEquals(TimeSeriesDataType.DOUBLE, chunk.getDataType());
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk, chunk2);
        assertSame(metadata, timeSeries.getMetadata());
        assertEquals(Arrays.asList(chunk, chunk2), timeSeries.getChunks());
        assertArrayEquals(new double[] {Double.NaN, Double.NaN, 1d, 2d, Double.NaN, 3d, 4d, 4d}, timeSeries.toArray(), 0d);
        DoublePoint[] pointsRef = {new DoublePoint(0, Instant.parse("2015-01-01T00:00:00Z"), Double.NaN),
                                   new DoublePoint(2, Instant.parse("2015-01-01T00:30:00Z"), 1d),
                                   new DoublePoint(3, Instant.parse("2015-01-01T00:45:00Z"), 2d),
                                   new DoublePoint(4, Instant.parse("2015-01-01T01:00:00Z"), Double.NaN),
                                   new DoublePoint(5, Instant.parse("2015-01-01T01:15:00Z"), 3d),
                                   new DoublePoint(6, Instant.parse("2015-01-01T01:30:00Z"), 4d)};
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
                "      \"spacing\" : 900000",
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
        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new TimeSeriesJsonModule());

        assertEquals(timeSeries, objectMapper.readValue(objectMapper.writeValueAsString(timeSeries), DoubleTimeSeries.class));
    }

    @Test
    void splitTest() {
        doSplitTest(3, 10);
    }

    @Test
    void splitTestHuge() {
        doSplitTest(100000003, 100000010);
    }

    private void doSplitTest(int chunkposition, int totalsize) {
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(totalsize);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleDataChunk chunk = new UncompressedDoubleDataChunk(chunkposition,
                new double[] {0d, 0d, 0d, 0d, 0d });
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk);
        List<DoubleTimeSeries> split = timeSeries.split(2);

        // check there is 3 new chunks
        assertEquals(3, split.size());

        // check first chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(0));
        assertEquals(1, ((StoredDoubleTimeSeries) split.get(0)).getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ((StoredDoubleTimeSeries) split.get(0)).getChunks().get(0));
        assertEquals(chunkposition, ((StoredDoubleTimeSeries) split.get(0)).getChunks().get(0).getOffset());
        assertEquals(1, ((StoredDoubleTimeSeries) split.get(0)).getChunks().get(0).getLength());

        // check second chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(1));
        assertEquals(1, ((StoredDoubleTimeSeries) split.get(1)).getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ((StoredDoubleTimeSeries) split.get(1)).getChunks().get(0));
        assertEquals(chunkposition + 1, ((StoredDoubleTimeSeries) split.get(1)).getChunks().get(0).getOffset());
        assertEquals(2, ((StoredDoubleTimeSeries) split.get(1)).getChunks().get(0).getLength());

        // check third chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(2));
        assertEquals(1, ((StoredDoubleTimeSeries) split.get(2)).getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ((StoredDoubleTimeSeries) split.get(2)).getChunks().get(0));
        assertEquals(chunkposition + 3, ((StoredDoubleTimeSeries) split.get(2)).getChunks().get(0).getOffset());
        assertEquals(2, ((StoredDoubleTimeSeries) split.get(2)).getChunks().get(0).getLength());
    }

    @Test
    void testCreate() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(2), Duration.ofMillis(1));
        DoubleTimeSeries ts1 = TimeSeries.createDouble("ts1", index, 0d, 1d, 2d);
        assertEquals("ts1", ts1.getMetadata().getName());
        assertEquals(TimeSeriesDataType.DOUBLE, ts1.getMetadata().getDataType());
        assertArrayEquals(new double[] {0d, 1d, 2d}, ts1.toArray(), 0d);
    }

    @Test
    void splitMultiChunkTimeSeriesTest() {
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(6);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleDataChunk chunk = new UncompressedDoubleDataChunk(0,
                new double[]{0d, 1d, 2d, 3d, 4d, 5d});
        DataChunk.Split<DoublePoint, DoubleDataChunk> splitChunk = chunk.splitAt(3);
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, splitChunk.getChunk1(), splitChunk.getChunk2());
        List<List<DoubleTimeSeries>> split = TimeSeries.split(Collections.singletonList(timeSeries), 2);

        // check there is 3 new time series
        assertEquals(3, split.size());

        // check first chunk
        assertEquals(1, split.get(0).size());
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(0).get(0));
        StoredDoubleTimeSeries ts = (StoredDoubleTimeSeries) split.get(0).get(0);
        assertEquals(1, ts.getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ts.getChunks().get(0));
        assertEquals(0, ts.getChunks().get(0).getOffset());
        assertEquals(2, ts.getChunks().get(0).getLength());

        // check second chunk
        assertEquals(1, split.get(1).size());
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(1).get(0));
        ts = (StoredDoubleTimeSeries) split.get(1).get(0);
        assertEquals(1, ts.getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ts.getChunks().get(0));
        assertEquals(2, ts.getChunks().get(0).getOffset());
        assertEquals(2, ts.getChunks().get(0).getLength());

        // check third chunk
        assertEquals(1, split.get(2).size());
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(2).get(0));
        ts = (StoredDoubleTimeSeries) split.get(2).get(0);
        assertEquals(1, ts.getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ts.getChunks().get(0));
        assertEquals(4, ts.getChunks().get(0).getOffset());
        assertEquals(2, ts.getChunks().get(0).getLength());
    }

    @Test
    void testCreateError() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> TimeSeries.createDouble("ts1", new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(2), Duration.ofMillis(1)), 0d, 1d));
        assertTrue(e.getMessage().contains("Bad number of values 2, expected 3"));
    }
}
