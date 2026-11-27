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
import com.google.common.collect.Range;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.json.TimeSeriesJsonModule;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.*;

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
        assertArrayEquals(pointsRef, timeSeries.compressedStream().toArray());
        assertArrayEquals(pointsRef, Iterators.toArray(timeSeries.compressedIterator(), DoublePoint.class));

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

    private void doSplitTestByRanges(Range<@NonNull Integer> firstRange, Range<@NonNull Integer> secondRange, int chunkLength) {
        List<Range<@NonNull Integer>> ranges = List.of(firstRange, secondRange);
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(chunkLength);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        double valueInChunk = 28d;
        CompressedDoubleDataChunk chunk = new CompressedDoubleDataChunk(0, chunkLength, new double[]{valueInChunk}, new int[]{chunkLength});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk);
        List<DoubleTimeSeries> split = timeSeries.splitByRanges(ranges);

        // check there is 2 new chunks
        assertEquals(2, split.size());

        // check first chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.getFirst());
        assertEquals(1, ((StoredDoubleTimeSeries) split.getFirst()).getChunks().size());
        assertInstanceOf(CompressedDoubleDataChunk.class, ((StoredDoubleTimeSeries) split.getFirst()).getChunks().getFirst());
        assertEquals(firstRange.lowerEndpoint(), ((StoredDoubleTimeSeries) split.getFirst()).getChunks().getFirst().getOffset());
        assertEquals(firstRange.upperEndpoint() - firstRange.lowerEndpoint() + 1, ((StoredDoubleTimeSeries) split.getFirst()).getChunks().getFirst().getLength());
        split.getFirst().toArray();

        // check second chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(1));
        assertEquals(1, ((StoredDoubleTimeSeries) split.get(1)).getChunks().size());
        assertInstanceOf(CompressedDoubleDataChunk.class, ((StoredDoubleTimeSeries) split.get(1)).getChunks().getFirst());
        assertEquals(secondRange.lowerEndpoint(), ((StoredDoubleTimeSeries) split.get(1)).getChunks().getFirst().getOffset());
        assertEquals(secondRange.upperEndpoint() - secondRange.lowerEndpoint() + 1, ((StoredDoubleTimeSeries) split.get(1)).getChunks().getFirst().getLength());
    }

    @Test
    void rangesSplitTestBigChunk() {
        Range<@NonNull Integer> firstRange = Range.closed(100, 123);
        Range<@NonNull Integer> secondRange = Range.closed(150, 872);
        int chunkLength = 100000;
        doSplitTestByRanges(firstRange, secondRange, chunkLength);
    }

    @Test
    void rangesSplitTest() {
        Range<@NonNull Integer> firstRange = Range.closed(0, 499);
        Range<@NonNull Integer> secondRange = Range.closed(700, 899);
        int chunkLength = 900;
        doSplitTestByRanges(firstRange, secondRange, chunkLength);
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
        RegularTimeSeriesIndex index = new RegularTimeSeriesIndex(Instant.ofEpochMilli(0), Instant.ofEpochMilli(2), Duration.ofMillis(1));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> TimeSeries.createDouble("ts1", index, 0d, 1d));
        assertTrue(e.getMessage().contains("Bad number of values 2, expected 3"));
    }

    @Test
    void splitTimeSeriesWithMultipleChunksByRangeTest() {
        // Prepare the TimeSeries
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(20);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(0,
            new double[]{0d, 1d, 2d, 3d, 4d});
        UncompressedDoubleDataChunk chunk2 = new UncompressedDoubleDataChunk(5,
            new double[]{5d, 6d, 7d, 8d, 9d});
        UncompressedDoubleDataChunk chunk3 = new UncompressedDoubleDataChunk(10,
            new double[]{10d, 11d, 12d, 13d, 14d});
        UncompressedDoubleDataChunk chunk4 = new UncompressedDoubleDataChunk(15,
            new double[]{15d, 16d, 17d, 18d, 19d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1, chunk2, chunk3, chunk4);
        // Prepare the Ranges
        List<Range<@NonNull Integer>> ranges = List.of(Range.closed(3, 7), Range.closed(9, 17));
        List<DoubleTimeSeries> split = timeSeries.splitByRanges(ranges);

        // check there are 2 new time series
        assertEquals(2, split.size());

        // check first chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.getFirst());
        StoredDoubleTimeSeries ts = (StoredDoubleTimeSeries) split.getFirst();
        assertEquals(1, ts.getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ts.getChunks().getFirst());
        assertEquals(3, ts.getChunks().getFirst().getOffset());
        assertEquals(5, ts.getChunks().getFirst().getLength());

        // check second chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(1));
        ts = (StoredDoubleTimeSeries) split.get(1);
        assertEquals(1, ts.getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ts.getChunks().getFirst());
        assertEquals(9, ts.getChunks().getFirst().getOffset());
        assertEquals(9, ts.getChunks().getFirst().getLength());
    }

    @Test
    void splitTimeSeriesWithMultipleChunksAndIgnoredChunkByRangeTest() {
        // Prepare the TimeSeries
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(20);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(0,
            new double[]{0d, 1d, 2d, 3d, 4d});
        UncompressedDoubleDataChunk chunk2 = new UncompressedDoubleDataChunk(5,
            new double[]{5d, 6d, 7d, 8d, 9d});
        UncompressedDoubleDataChunk chunk3 = new UncompressedDoubleDataChunk(10,
            new double[]{10d, 11d, 12d, 13d, 14d});
        UncompressedDoubleDataChunk chunk4 = new UncompressedDoubleDataChunk(15,
            new double[]{15d, 16d, 17d, 18d, 19d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1, chunk2, chunk3, chunk4);
        // Prepare the Ranges
        List<Range<@NonNull Integer>> ranges = List.of(Range.closed(6, 10), Range.closed(13, 14));
        List<DoubleTimeSeries> split = timeSeries.splitByRanges(ranges);

        // check there are 2 new time series
        assertEquals(2, split.size());

        // check first chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.getFirst());
        StoredDoubleTimeSeries ts = (StoredDoubleTimeSeries) split.getFirst();
        assertEquals(1, ts.getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ts.getChunks().getFirst());
        assertEquals(6, ts.getChunks().getFirst().getOffset());
        assertEquals(5, ts.getChunks().getFirst().getLength());

        // check second chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.get(1));
        ts = (StoredDoubleTimeSeries) split.get(1);
        assertEquals(1, ts.getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ts.getChunks().getFirst());
        assertEquals(13, ts.getChunks().getFirst().getOffset());
        assertEquals(2, ts.getChunks().getFirst().getLength());
    }

    @Test
    void splitTimeSeriesWithNoChunkException() {
        // Prepare the TimeSeries
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(20);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(0,
            new double[]{0d, 1d, 2d, 3d, 4d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1);
        // Prepare the Ranges
        List<Range<@NonNull Integer>> ranges = List.of(Range.closed(9, 17));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> timeSeries.splitByRanges(ranges));
        assertEquals("No chunk found for ranges [[9..17]]", exception.getMessage());
    }

    @Test
    void splitTimeSeriesWithPartialChunkOnFirstIndex() {
        // Prepare the TimeSeries
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(20);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(2,
            new double[]{2d, 3d, 4d, 5d, 6d, 7d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1);
        // Prepare the Ranges
        List<Range<@NonNull Integer>> ranges = List.of(Range.closed(1, 4));
        List<DoubleTimeSeries> split = timeSeries.splitByRanges(ranges);

        // check there are 1 new time series
        assertEquals(1, split.size());

        // check the chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.getFirst());
        StoredDoubleTimeSeries ts = (StoredDoubleTimeSeries) split.getFirst();
        assertEquals(1, ts.getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ts.getChunks().getFirst());
        assertEquals(2, ts.getChunks().getFirst().getOffset());
        assertEquals(3, ts.getChunks().getFirst().getLength());
    }

    @Test
    void splitTimeSeriesWithPartialChunkOnLastIndex() {
        // Prepare the TimeSeries
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(20);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(2,
            new double[]{2d, 3d, 4d, 5d, 6d, 7d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1);
        // Prepare the Ranges
        List<Range<@NonNull Integer>> ranges = List.of(Range.closed(3, 200));
        List<DoubleTimeSeries> split = timeSeries.splitByRanges(ranges);

        // check there are 1 new time series
        assertEquals(1, split.size());

        // check the chunk
        assertInstanceOf(StoredDoubleTimeSeries.class, split.getFirst());
        StoredDoubleTimeSeries ts = (StoredDoubleTimeSeries) split.getFirst();
        assertEquals(1, ts.getChunks().size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, ts.getChunks().getFirst());
        assertEquals(3, ts.getChunks().getFirst().getOffset());
        assertEquals(5, ts.getChunks().getFirst().getLength());
    }

    @Test
    void toArrayWhenNoTimeSeriesData() {
        // Given
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(3);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata);
        // When
        double[] timeSeriesArray = timeSeries.toArray();
        // Then
        assertArrayEquals(new double[]{NaN, NaN, NaN}, timeSeriesArray, 0d);
    }

    @Test
    void toCompactArrayWhenNoTimeSeriesDataShouldReturnEmpty() {
        // Given
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(3);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata);
        // When
        double[] timeSeriesCompactArray = timeSeries.toCompactArray();
        // Then
        assertArrayEquals(new double[]{}, timeSeriesCompactArray, 0d);
    }

    @Test
    void testToArray() {
        // Given
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(8);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(0, new double[]{1d, 2d, 3d, 4d, 5d, 6d});
        UncompressedDoubleDataChunk chunk2 = new UncompressedDoubleDataChunk(6, new double[]{7d, 8d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1, chunk2);

        // When
        List<DoubleTimeSeries> chunks = timeSeries.split(4);
        double[] timeSeriesArray = timeSeries.toArray();
        double[] tsArray1 = chunks.get(0).toArray();
        double[] tsArray2 = chunks.get(1).toArray();

        // Then
        assertArrayEquals(new double[]{1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d}, timeSeriesArray, 0d);
        assertArrayEquals(new double[]{1d, 2d, 3d, 4d, NaN, NaN, NaN, NaN}, tsArray1, 0d);
        assertArrayEquals(new double[]{NaN, NaN, NaN, NaN, 5d, 6d, 7d, 8d}, tsArray2, 0d);
    }

    @Test
    void testToCompactArray() {
        // Given
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(8);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(0, new double[]{1d, 2d, 3d, 4d, 5d, 6d});
        UncompressedDoubleDataChunk chunk2 = new UncompressedDoubleDataChunk(6, new double[]{7d, 8d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1, chunk2);

        // When
        List<DoubleTimeSeries> chunks = timeSeries.split(4);
        double[] timeSeriesCompactArray = timeSeries.toCompactArray();
        double[] tsCompactArray1 = chunks.get(0).toCompactArray();
        double[] tsCompactArray2 = chunks.get(1).toCompactArray();

        // Then
        assertArrayEquals(new double[]{1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d}, timeSeriesCompactArray, 0d);
        assertArrayEquals(new double[]{1d, 2d, 3d, 4d}, tsCompactArray1, 0d);
        assertArrayEquals(new double[]{5d, 6d, 7d, 8d}, tsCompactArray2, 0d);
    }

    @Test
    void testToCompactArrayWhenNaNExistsAtTheMiddle() {
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(9);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, index);
        DoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(0, new double[]{1d, 2d, 3d});
        DoubleDataChunk chunk2 = new UncompressedDoubleDataChunk(6, new double[]{7d, 8d});
        StoredDoubleTimeSeries timeSeries = new StoredDoubleTimeSeries(metadata, chunk1, chunk2);
        assertArrayEquals(new double[] {1d, 2d, 3d, NaN, NaN, NaN, 7d, 8d, NaN}, timeSeries.toArray());
        assertArrayEquals(new double[] {1d, 2d, 3d, NaN, NaN, NaN, 7d, 8d}, timeSeries.toCompactArray());
    }

    private static StoredDoubleTimeSeries createTimeSeries(int pointCount, DoubleDataChunk... chunks) {
        TimeSeriesIndex index = Mockito.mock(TimeSeriesIndex.class);
        Mockito.when(index.getPointCount()).thenReturn(pointCount);
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        return new StoredDoubleTimeSeries(metadata, new ArrayList<>(Arrays.asList(chunks)));
    }

    private static DoubleDataChunk getSingleChunk(DoubleTimeSeries timeSeries) {
        List<DoubleDataChunk> chunks = ((StoredDoubleTimeSeries) timeSeries).getChunks();
        assertEquals(1, chunks.size());
        return chunks.getFirst();
    }

    /**
     * input chunks {offset, length}, new chunk size, expected chunks {offset, length}
     */
    private static Stream<Arguments> splitWithGapsArguments() {
        int[][] chunksWithFarGap = {{0, 3}, {8, 2}};
        int[][] chunksWithNearGap = {{0, 3}, {6, 4}};
        return Stream.of(
            Arguments.of(chunksWithFarGap, 2, new int[][] {{0, 2}, {2, 1}, {8, 2}}),
            Arguments.of(chunksWithFarGap, 3, new int[][] {{0, 3}, {8, 1}, {9, 1}}),
            Arguments.of(chunksWithFarGap, 4, new int[][] {{0, 3}, {8, 2}}),
            Arguments.of(chunksWithNearGap, 2, new int[][] {{0, 2}, {2, 1}, {6, 2}, {8, 2}}),
            Arguments.of(chunksWithNearGap, 3, new int[][] {{0, 3}, {6, 3}, {9, 1}}),
            Arguments.of(chunksWithNearGap, 4, new int[][] {{0, 3}, {6, 2}, {8, 2}})
        );
    }

    @ParameterizedTest
    @MethodSource("splitWithGapsArguments")
    void splitWithGapsTest(int[][] inputChunks, int newChunkSize, int[][] expectedChunks) {
        // value at index i is i
        DoubleDataChunk[] chunks = Arrays.stream(inputChunks)
            .map(c -> new UncompressedDoubleDataChunk(c[0], IntStream.range(c[0], c[0] + c[1]).asDoubleStream().toArray()))
            .toArray(DoubleDataChunk[]::new);
        StoredDoubleTimeSeries timeSeries = createTimeSeries(10, chunks);

        List<DoubleTimeSeries> split = timeSeries.split(newChunkSize);

        assertEquals(expectedChunks.length, split.size());
        for (int i = 0; i < expectedChunks.length; i++) {
            int offset = expectedChunks[i][0];
            int length = expectedChunks[i][1];
            DoubleDataChunk chunk = getSingleChunk(split.get(i));
            assertEquals(offset, chunk.getOffset());
            assertEquals(length, chunk.getLength());
            double[] expectedCompactArray = IntStream.range(offset, offset + length).asDoubleStream().toArray();
            assertArrayEquals(expectedCompactArray, split.get(i).toCompactArray(), 0d);
            double[] expectedArray = new double[10];
            Arrays.fill(expectedArray, NaN);
            System.arraycopy(expectedCompactArray, 0, expectedArray, offset, length);
            assertArrayEquals(expectedArray, split.get(i).toArray(), 0d);
        }
    }

    @Test
    void splitWithEdgeGapTest() {
        // issue #3941: used to throw "Chunks are not successive"
        StoredDoubleTimeSeries timeSeries = createTimeSeries(8,
            new UncompressedDoubleDataChunk(0, new double[] {0d, 1d, 2d}),
            new UncompressedDoubleDataChunk(6, new double[] {6d, 7d}));

        List<DoubleTimeSeries> split = timeSeries.split(4);

        assertEquals(2, split.size());
        DoubleDataChunk chunk1 = getSingleChunk(split.get(0));
        assertEquals(0, chunk1.getOffset());
        assertArrayEquals(new double[] {0d, 1d, 2d}, split.get(0).toCompactArray(), 0d);
        DoubleDataChunk chunk2 = getSingleChunk(split.get(1));
        assertEquals(6, chunk2.getOffset());
        assertArrayEquals(new double[] {6d, 7d}, split.get(1).toCompactArray(), 0d);
    }

    @Test
    void splitWithInnerGapTest() {
        // issue #3941: used to throw "Chunks are not successive"
        StoredDoubleTimeSeries timeSeries = createTimeSeries(8,
            new UncompressedDoubleDataChunk(0, new double[] {0d, 1d}),
            new UncompressedDoubleDataChunk(3, new double[] {3d}));

        List<DoubleTimeSeries> split = timeSeries.split(4);

        assertEquals(1, split.size());
        DoubleDataChunk chunk = getSingleChunk(split.getFirst());
        assertInstanceOf(UncompressedDoubleDataChunk.class, chunk);
        assertEquals(0, chunk.getOffset());
        assertArrayEquals(new double[] {0d, 1d, NaN, 3d}, ((UncompressedDoubleDataChunk) chunk).getValues(), 0d);
    }

    @Test
    void splitCompressedChunkTest() {
        // index  0  1  2  3  4  5
        // value  1  1  1  2  2  2
        CompressedDoubleDataChunk chunk = new CompressedDoubleDataChunk(0, 6, new double[] {1d, 2d}, new int[] {3, 3});
        StoredDoubleTimeSeries timeSeries = createTimeSeries(6, chunk);

        List<DoubleTimeSeries> split = timeSeries.split(4);

        assertEquals(2, split.size());
        assertEquals(new CompressedDoubleDataChunk(0, 4, new double[] {1d, 2d}, new int[] {3, 1}), getSingleChunk(split.get(0)));
        assertEquals(new CompressedDoubleDataChunk(4, 2, new double[] {2d}, new int[] {2}), getSingleChunk(split.get(1)));

        // a chunk lying entirely in a window is not copied
        assertSame(chunk, getSingleChunk(timeSeries.split(6).getFirst()));
        assertSame(chunk, getSingleChunk(timeSeries.split(100).getFirst()));
    }

    @Test
    void splitSuccessiveCompressedChunksTest() {
        StoredDoubleTimeSeries timeSeries = createTimeSeries(4,
            new CompressedDoubleDataChunk(0, 2, new double[] {1d}, new int[] {2}),
            new CompressedDoubleDataChunk(2, 2, new double[] {1d}, new int[] {2}));

        List<DoubleTimeSeries> split = timeSeries.split(4);

        assertEquals(1, split.size());
        assertEquals(new CompressedDoubleDataChunk(0, 4, new double[] {1d}, new int[] {4}), getSingleChunk(split.getFirst()));

        // as with append, steps are merged at the chunk boundaries only when their values are equal (NaN != NaN)
        CompressedDoubleDataChunk chunk1 = new CompressedDoubleDataChunk(0, 3, new double[] {1d, 2d}, new int[] {1, 2});
        CompressedDoubleDataChunk chunk2 = new CompressedDoubleDataChunk(3, 3, new double[] {2d, NaN}, new int[] {1, 2});
        CompressedDoubleDataChunk chunk3 = new CompressedDoubleDataChunk(6, 2, new double[] {NaN, 3d}, new int[] {1, 1});
        split = createTimeSeries(8, chunk1, chunk2, chunk3).split(8);

        assertEquals(1, split.size());
        DoubleDataChunk merged = getSingleChunk(split.getFirst());
        assertEquals(new CompressedDoubleDataChunk(0, 8, new double[] {1d, 2d, NaN, NaN, 3d}, new int[] {1, 3, 2, 1, 1}), merged);
        assertEquals(chunk1.append(chunk2).append(chunk3), merged);
    }

    @Test
    void splitCompressedChunksWithInnerGapTest() {
        StoredDoubleTimeSeries timeSeries = createTimeSeries(6,
            new CompressedDoubleDataChunk(0, 2, new double[] {1d}, new int[] {2}),
            new CompressedDoubleDataChunk(3, 2, new double[] {2d}, new int[] {2}));

        List<DoubleTimeSeries> split = timeSeries.split(6);

        assertEquals(1, split.size());
        assertEquals(new UncompressedDoubleDataChunk(0, new double[] {1d, 1d, NaN, 2d, 2d}), getSingleChunk(split.getFirst()));
    }

    @Test
    void splitMixedKindChunksTest() {
        StoredDoubleTimeSeries timeSeries = createTimeSeries(4,
            new CompressedDoubleDataChunk(0, 2, new double[] {1d}, new int[] {2}),
            new UncompressedDoubleDataChunk(2, new double[] {3d, 4d}));

        List<DoubleTimeSeries> split = timeSeries.split(4);

        assertEquals(1, split.size());
        assertEquals(new UncompressedDoubleDataChunk(0, new double[] {1d, 1d, 3d, 4d}), getSingleChunk(split.getFirst()));
    }

    @Test
    void splitInvalidChunkSizeTest() {
        StoredDoubleTimeSeries timeSeries = createTimeSeries(4, new UncompressedDoubleDataChunk(0, new double[] {1d, 2d}));
        assertThrows(IllegalArgumentException.class, () -> timeSeries.split(0));
        assertThrows(IllegalArgumentException.class, () -> timeSeries.split(-1));
    }

    @Test
    void splitOverlappingChunksTest() {
        StoredDoubleTimeSeries timeSeries = createTimeSeries(4,
            new UncompressedDoubleDataChunk(0, new double[] {1d, 2d}),
            new UncompressedDoubleDataChunk(1, new double[] {3d}));
        assertThrows(TimeSeriesException.class, () -> timeSeries.split(2));
    }

    @Test
    void splitEmptyTimeSeriesTest() {
        assertTrue(createTimeSeries(4).split(2).isEmpty());
    }

    @Test
    void splitFragmentedPerformanceTest() {
        // issue #1634: split used to be quadratic with many small chunks
        int size = 200_000;
        int newChunkSize = 100_000;
        DoubleDataChunk[] chunks = IntStream.range(0, size)
            .mapToObj(i -> new UncompressedDoubleDataChunk(i, new double[] {i}))
            .toArray(DoubleDataChunk[]::new);
        StoredDoubleTimeSeries timeSeries = createTimeSeries(size, chunks);

        List<DoubleTimeSeries> split = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> timeSeries.split(newChunkSize));

        assertEquals(2, split.size());
        for (int i = 0; i < split.size(); i++) {
            DoubleDataChunk chunk = getSingleChunk(split.get(i));
            assertEquals(i * newChunkSize, chunk.getOffset());
            assertArrayEquals(IntStream.range(i * newChunkSize, (i + 1) * newChunkSize).asDoubleStream().toArray(),
                ((UncompressedDoubleDataChunk) chunk).getValues(), 0d);
        }
    }

    @Test
    void splitSingleChunkPerformanceTest() {
        // issue #1634: split used to be quadratic with a single big chunk and a small new chunk size
        int size = 1_000_000;
        int newChunkSize = 10;
        double[] values = IntStream.range(0, size).asDoubleStream().toArray();
        StoredDoubleTimeSeries timeSeries = createTimeSeries(size, new UncompressedDoubleDataChunk(0, values));

        List<DoubleTimeSeries> split = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> timeSeries.split(newChunkSize));

        assertEquals(size / newChunkSize, split.size());
        for (int i = 0; i < split.size(); i++) {
            DoubleDataChunk chunk = getSingleChunk(split.get(i));
            assertEquals(i * newChunkSize, chunk.getOffset());
            assertEquals(newChunkSize, chunk.getLength());
            assertEquals(i * newChunkSize, ((UncompressedDoubleDataChunk) chunk).getValues()[0], 0d);
        }
    }

    @Test
    void splitCompressedChunkPerformanceTest() {
        // a big poorly compressed chunk, split into many windows
        int size = 1_000_000;
        int newChunkSize = 10;
        double[] stepValues = IntStream.range(0, size / 2).mapToDouble(i -> i % 2).toArray();
        int[] stepLengths = new int[size / 2];
        Arrays.fill(stepLengths, 2);
        StoredDoubleTimeSeries timeSeries = createTimeSeries(size, new CompressedDoubleDataChunk(0, size, stepValues, stepLengths));

        List<DoubleTimeSeries> split = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> timeSeries.split(newChunkSize));

        assertEquals(size / newChunkSize, split.size());
        for (int i = 0; i < split.size(); i++) {
            DoubleDataChunk chunk = getSingleChunk(split.get(i));
            assertEquals(i * newChunkSize, chunk.getOffset());
            assertEquals(newChunkSize, chunk.getLength());
            assertEquals(5, ((CompressedDoubleDataChunk) chunk).getStepLengths().length);
        }
    }

    @Test
    void splitSuccessiveCompressedChunksPerformanceTest() {
        // many successive compressed chunks, merged in a single window
        int size = 200_000;
        DoubleDataChunk[] chunks = IntStream.range(0, size)
            .mapToObj(i -> new CompressedDoubleDataChunk(i, 1, new double[] {i / 2}, new int[] {1}))
            .toArray(DoubleDataChunk[]::new);
        StoredDoubleTimeSeries timeSeries = createTimeSeries(size, chunks);

        List<DoubleTimeSeries> split = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> timeSeries.split(size));

        assertEquals(1, split.size());
        CompressedDoubleDataChunk chunk = (CompressedDoubleDataChunk) getSingleChunk(split.getFirst());
        assertEquals(0, chunk.getOffset());
        assertEquals(size, chunk.getLength());
        assertEquals(size / 2, chunk.getStepLengths().length);
        assertTrue(IntStream.of(chunk.getStepLengths()).allMatch(length -> length == 2));
    }

    @Test
    void splitListWithMissingWindowTest() {
        List<DoubleTimeSeries> timeSeriesList = List.of(createTimeSeries(4, new UncompressedDoubleDataChunk(0, new double[] {1d, 2d})));

        TimeSeriesException e = assertThrows(TimeSeriesException.class, () -> TimeSeries.split(timeSeriesList, 2));
        assertEquals("Time series 'ts1' has no data in 1 window(s) of size 2: cannot align it with the other time series", e.getMessage());
    }

}
