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

import static com.powsybl.commons.test.ComparisonUtils.assertIteratorsEquals;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class StringTimeSeriesTest {

    @Test
    void test() throws IOException {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"),
                                                                     Duration.ofMinutes(15));
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.STRING, Collections.emptyMap(), index);
        UncompressedStringDataChunk chunk = new UncompressedStringDataChunk(2, new String[] {"a", "b"});
        CompressedStringDataChunk chunk2 = new CompressedStringDataChunk(5, 3, new String[] {"c", "d"}, new int[] {1, 2});
        assertEquals(TimeSeriesDataType.STRING, chunk.getDataType());
        StringTimeSeries timeSeries = new StringTimeSeries(metadata, chunk, chunk2);
        assertSame(metadata, timeSeries.getMetadata());
        assertEquals(Arrays.asList(chunk, chunk2), timeSeries.getChunks());
        assertArrayEquals(new String[] {null, null, "a", "b", null, "c", "d", "d"}, timeSeries.toArray());
        StringPoint[] pointsRef = {new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z"), null),
                                   new StringPoint(2, Instant.parse("2015-01-01T00:30:00Z"), "a"),
                                   new StringPoint(3, Instant.parse("2015-01-01T00:45:00Z"), "b"),
                                   new StringPoint(4, Instant.parse("2015-01-01T01:00:00Z"), null),
                                   new StringPoint(5, Instant.parse("2015-01-01T01:15:00Z"), "c"),
                                   new StringPoint(6, Instant.parse("2015-01-01T01:30:00Z"), "d")};
        assertArrayEquals(pointsRef, timeSeries.compressedStream().toArray());
        assertArrayEquals(pointsRef, Iterators.toArray(timeSeries.compressedIterator(), StringPoint.class));

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
                "      \"spacing\" : 900000",
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
        String json2 = JsonUtil.toJson(timeSeriesList.getFirst()::writeJson);
        assertEquals(json, json2);

        // test json with object mapper
        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new TimeSeriesJsonModule());

        assertEquals(timeSeries, objectMapper.readValue(objectMapper.writeValueAsString(timeSeries), StringTimeSeries.class));
    }

    @Test
    void testCreate() {
        TimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(2), Duration.ofMillis(1));
        StringTimeSeries ts1 = TimeSeries.createString("ts1", index, "a", "b", "c");
        assertEquals("ts1", ts1.getMetadata().getName());
        assertEquals(TimeSeriesDataType.STRING, ts1.getMetadata().getDataType());
        assertArrayEquals(new String[] {"a", "b", "c"}, ts1.toArray());
    }

    @Test
    void testCreateError() {
        RegularTimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(2), Duration.ofMillis(1));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> TimeSeries.createString("ts1", index, "a", "b"));
        assertTrue(e.getMessage().contains("Bad number of values 2, expected 3"));
    }

    @Test
    void testStream() {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"),
            Duration.ofMinutes(15));
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.STRING, Collections.emptyMap(), index);
        CompressedStringDataChunk chunk1 = new CompressedStringDataChunk(0, 5, new String[] {"a", "b"}, new int[] {3, 2});
        CompressedStringDataChunk chunk2 = new CompressedStringDataChunk(5, 3, new String[] {"c", "d"}, new int[] {1, 2});
        StringTimeSeries timeSeries = new StringTimeSeries(metadata, chunk1, chunk2);
        assertArrayEquals(new String[] {"a", "a", "a", "b", "b", "c", "d", "d"}, timeSeries.toArray());

        // Uncompressed points
        StringPoint[] uncompressedPointsRef = {
            new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z"), "a"),
            new StringPoint(1, Instant.parse("2015-01-01T00:15:00Z"), "a"),
            new StringPoint(2, Instant.parse("2015-01-01T00:30:00Z"), "a"),
            new StringPoint(3, Instant.parse("2015-01-01T00:45:00Z"), "b"),
            new StringPoint(4, Instant.parse("2015-01-01T01:00:00Z"), "b"),
            new StringPoint(5, Instant.parse("2015-01-01T01:15:00Z"), "c"),
            new StringPoint(6, Instant.parse("2015-01-01T01:30:00Z"), "d"),
            new StringPoint(7, Instant.parse("2015-01-01T01:45:00Z"), "d")
        };
        assertArrayEquals(uncompressedPointsRef, timeSeries.stream().toArray());
        assertArrayEquals(uncompressedPointsRef, timeSeries.uncompressedStream().toArray());
        assertIteratorsEquals(List.of(uncompressedPointsRef).iterator(), timeSeries.uncompressedIterator());

        // Compressed points
        StringPoint[] compressedPointsRef = {
            new StringPoint(0, Instant.parse("2015-01-01T00:00:00Z"), "a"),
            new StringPoint(3, Instant.parse("2015-01-01T00:45:00Z"), "b"),
            new StringPoint(5, Instant.parse("2015-01-01T01:15:00Z"), "c"),
            new StringPoint(6, Instant.parse("2015-01-01T01:30:00Z"), "d")
        };
        assertArrayEquals(compressedPointsRef, timeSeries.compressedStream().toArray());
        assertIteratorsEquals(List.of(compressedPointsRef).iterator(), timeSeries.compressedIterator());
    }

    @Test
    void toArrayWhenNoTimeSeriesData() {
        // Given
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(3);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.STRING, index);
        StringTimeSeries timeSeries = new StringTimeSeries(metadata);

        // When
        String[] timeSeriesArray = timeSeries.toArray();
        String[] timeSeriesCompactArray = timeSeries.toCompactArray();

        // Then
        assertArrayEquals(new String[]{null, null, null}, timeSeriesArray);
        assertArrayEquals(new String[]{}, timeSeriesCompactArray);
    }

    @Test
    void toCompactArrayWhenNoTimeSeriesDataShouldReturnEmpty() {
        // Given
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(3);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.STRING, index);
        StringTimeSeries timeSeries = new StringTimeSeries(metadata);

        // When
        String[] timeSeriesCompactArray = timeSeries.toCompactArray();

        // Then
        assertArrayEquals(new String[]{}, timeSeriesCompactArray);
    }

    @Test
    void testToArray() {
        // Given
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(8);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedStringDataChunk chunk1 = new UncompressedStringDataChunk(0, new String[]{"a", "b", "c", "d", "e", "f"});
        UncompressedStringDataChunk chunk2 = new UncompressedStringDataChunk(6, new String[]{"g", "h"});
        StringTimeSeries timeSeries = new StringTimeSeries(metadata, chunk1, chunk2);

        // When
        List<StringTimeSeries> chunks = timeSeries.split(4);
        String[] timeSeriesArray = timeSeries.toArray();
        String[] tsArray1 = chunks.get(0).toArray();
        String[] tsArray2 = chunks.get(1).toArray();

        // Then
        assertArrayEquals(new String[]{"a", "b", "c", "d", "e", "f", "g", "h"}, timeSeriesArray);
        assertArrayEquals(new String[]{"a", "b", "c", "d", null, null, null, null}, tsArray1);
        assertArrayEquals(new String[]{null, null, null, null, "e", "f", "g", "h"}, tsArray2);
    }

    @Test
    void testToCompactArray() {
        // Given
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(8);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedStringDataChunk chunk1 = new UncompressedStringDataChunk(0, new String[]{"a", "b", "c", "d", "e", "f"});
        UncompressedStringDataChunk chunk2 = new UncompressedStringDataChunk(6, new String[]{"g", "h"});
        StringTimeSeries timeSeries = new StringTimeSeries(metadata, chunk1, chunk2);

        // When
        List<StringTimeSeries> chunks = timeSeries.split(4);
        String[] timeSeriesArray = timeSeries.toCompactArray();
        String[] tsArray1 = chunks.get(0).toCompactArray();
        String[] tsArray2 = chunks.get(1).toCompactArray();

        // Then
        assertArrayEquals(new String[]{"a", "b", "c", "d", "e", "f", "g", "h"}, timeSeriesArray);
        assertArrayEquals(new String[]{"a", "b", "c", "d"}, tsArray1);
        assertArrayEquals(new String[]{"e", "f", "g", "h"}, tsArray2);
    }

    @Test
    void testToCompactArrayWhenNaNExistsAtTheMiddle() {
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(9);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        StringDataChunk chunk1 = new UncompressedStringDataChunk(0, new String[]{"a", "b", "c"});
        StringDataChunk chunk2 = new UncompressedStringDataChunk(6, new String[]{"g", "h"});
        StringTimeSeries timeSeries = new StringTimeSeries(metadata, chunk1, chunk2);
        assertArrayEquals(new String[] {"a", "b", "c", null, null, null, "g", "h", null}, timeSeries.toArray());
        assertArrayEquals(new String[] {"a", "b", "c", null, null, null, "g", "h"}, timeSeries.toCompactArray());
    }

    @Test
    void testGetByIndex() {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:45:00Z"), Duration.ofMinutes(15));
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        UncompressedStringDataChunk chunk1 = new UncompressedStringDataChunk(0, new String[]{"a", "b", "c", "d", "e", "f"});
        UncompressedStringDataChunk chunk2 = new UncompressedStringDataChunk(6, new String[]{"g", "h"});
        StringTimeSeries timeSeries = new StringTimeSeries(metadata, chunk1, chunk2);
        List<StringTimeSeries> splitTimeSeries = timeSeries.split(4);
        StringTimeSeries timeSeries1 = splitTimeSeries.get(0);
        StringTimeSeries timeSeries2 = splitTimeSeries.get(1);
        // Original time series
        assertTimeSerie(timeSeries, "b", "f", "g");
        // First split time series
        assertTimeSerie(timeSeries1, "b", null, null);
        // Second split time series
        assertTimeSerie(timeSeries2, null, "f", "g");
    }

    private static void assertTimeSerie(StringTimeSeries timeSeries, String expectedAtIndex1, String expectedAtIndex5, String expectedAtIndex6) {
        assertEquals(expectedAtIndex1, timeSeries.get(1));
        assertEquals(expectedAtIndex5, timeSeries.get(5));
        assertEquals(expectedAtIndex6, timeSeries.get(6));
    }

}
