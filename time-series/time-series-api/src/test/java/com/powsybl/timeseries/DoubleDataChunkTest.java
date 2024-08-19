/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.json.TimeSeriesJsonModule;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class DoubleDataChunkTest {

    @Test
    void baseTest() throws IOException {
        UncompressedDoubleDataChunk chunk = new UncompressedDoubleDataChunk(1, new double[] {1d, 2d, 3d});
        assertEquals(1, chunk.getOffset());
        assertEquals(3, chunk.getLength());
        assertArrayEquals(new double[]{1d, 2d, 3d}, chunk.getValues(), 0d);
        assertEquals(24, chunk.getEstimatedSize());
        assertFalse(chunk.isCompressed());
        assertEquals(1d, chunk.getCompressionFactor(), 0d);
        DoubleBuffer buffer = DoubleBuffer.allocate(4);
        for (int i = 0; i < 4; i++) {
            buffer.put(i, Double.NaN);
        }
        chunk.fillBuffer(buffer, 0);
        assertArrayEquals(new double[] {Double.NaN, 1d, 2d, 3d}, buffer.array(), 0d);
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"offset\" : 1,",
                "  \"values\" : [ 1.0, 2.0, 3.0 ]",
                "}");
        assertEquals(jsonRef, JsonUtil.toJson(chunk::writeJson));
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T00:45:00Z"),
                                                                     Duration.ofMinutes(15));
        assertEquals(ImmutableList.of(new DoublePoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), 1d),
                                      new DoublePoint(2, Instant.parse("2015-01-01T00:30:00Z").toEpochMilli(), 2d),
                                      new DoublePoint(3, Instant.parse("2015-01-01T00:45:00Z").toEpochMilli(), 3d)),
                chunk.stream(index).toList());
    }

    @Test
    void compressTest() throws IOException {
        UncompressedDoubleDataChunk chunk = new UncompressedDoubleDataChunk(1, new double[] {1d, 2d, 2d, 2d, 2d, 3d});
        DoubleDataChunk maybeCompressedChunk = chunk.tryToCompress();
        assertInstanceOf(CompressedDoubleDataChunk.class, maybeCompressedChunk);
        CompressedDoubleDataChunk compressedChunk = (CompressedDoubleDataChunk) maybeCompressedChunk;
        assertEquals(1, compressedChunk.getOffset());
        assertEquals(6, compressedChunk.getLength());
        assertTrue(compressedChunk.isCompressed());
        assertEquals(36, compressedChunk.getEstimatedSize());
        assertEquals(36d / 48, compressedChunk.getCompressionFactor(), 0d);
        assertArrayEquals(new double[] {1d, 2d, 3d}, compressedChunk.getStepValues(), 0d);
        assertArrayEquals(new int[] {1, 4, 1}, compressedChunk.getStepLengths());
        DoubleBuffer buffer = DoubleBuffer.allocate(7);
        for (int i = 0; i < 7; i++) {
            buffer.put(i, Double.NaN);
        }
        compressedChunk.fillBuffer(buffer, 0);
        assertArrayEquals(new double[] {Double.NaN, 1d, 2d, 2d, 2d, 2d, 3d}, buffer.array(), 0d);

        // json test
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"offset\" : 1,",
                "  \"uncompressedLength\" : 6,",
                "  \"stepValues\" : [ 1.0, 2.0, 3.0 ],",
                "  \"stepLengths\" : [ 1, 4, 1 ]",
                "}");
        assertEquals(jsonRef, JsonUtil.toJson(compressedChunk::writeJson));

        // test json with object mapper
        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new TimeSeriesJsonModule());

        List<DoubleDataChunk> chunks = objectMapper.readValue(objectMapper.writeValueAsString(Arrays.asList(chunk, compressedChunk)),
                                                               TypeFactory.defaultInstance().constructCollectionType(List.class, DoubleDataChunk.class));
        assertEquals(2, chunks.size());
        assertEquals(chunk, chunks.get(0));
        assertEquals(compressedChunk, chunks.get(1));

        // check base class (DataChunk) deserializer
        assertInstanceOf(DoubleDataChunk.class, objectMapper.readValue(objectMapper.writeValueAsString(chunk), DataChunk.class));

        // stream test
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:30:00Z"),
                                                                     Duration.ofMinutes(15));
        assertEquals(ImmutableList.of(new DoublePoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), 1d),
                                      new DoublePoint(2, Instant.parse("2015-01-01T00:30:00Z").toEpochMilli(), 2d),
                                      new DoublePoint(6, Instant.parse("2015-01-01T01:30:00Z").toEpochMilli(), 3d)),
                     compressedChunk.stream(index).toList());
    }

    @Test
    void compressConstrTest() {
        assertThrows(IllegalArgumentException.class, () -> new CompressedDoubleDataChunk(-3, 1, new double[] {1d}, new int[] {1}));
    }

    @Test
    void compressConstrTest2() {
        assertThrows(IllegalArgumentException.class, () -> new CompressedDoubleDataChunk(0, 0, new double[] {1d}, new int[] {1}));
    }

    @Test
    void compressConstrTest3() {
        assertThrows(IllegalArgumentException.class, () -> new CompressedDoubleDataChunk(0, 1, new double[] {}, new int[] {}));
    }

    @Test
    void compressConstrTest4() {
        assertThrows(IllegalArgumentException.class, () -> new CompressedDoubleDataChunk(0, 1, new double[] {1d}, new int[] {}));
    }

    @Test
    void compressFailureTest() throws IOException {
        UncompressedDoubleDataChunk chunk = new UncompressedDoubleDataChunk(1, new double[] {1d, 2d, 2d, 3d});
        assertSame(chunk, chunk.tryToCompress());
    }

    @Test
    void uncompressedSplitTest() throws IOException {
        UncompressedDoubleDataChunk chunk = new UncompressedDoubleDataChunk(1, new double[]{1d, 2d, 3d});
        try {
            chunk.splitAt(1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            chunk.splitAt(4);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        DataChunk.Split<DoublePoint, DoubleDataChunk> split = chunk.splitAt(2);
        assertNotNull(split.getChunk1());
        assertNotNull(split.getChunk2());
        assertEquals(1, split.getChunk1().getOffset());
        assertInstanceOf(UncompressedDoubleDataChunk.class, split.getChunk1());
        assertArrayEquals(new double[] {1d}, ((UncompressedDoubleDataChunk) split.getChunk1()).getValues(), 0d);
        assertEquals(2, split.getChunk2().getOffset());
        assertInstanceOf(UncompressedDoubleDataChunk.class, split.getChunk2());
        assertArrayEquals(new double[] {2d, 3d}, ((UncompressedDoubleDataChunk) split.getChunk2()).getValues(), 0d);
    }

    @Test
    void compressedSplitTest() throws IOException {
        // index  0   1   2   3   4   5
        // value  NaN 1   1   2   2   2
        //            [-------]   [---]
        CompressedDoubleDataChunk chunk = new CompressedDoubleDataChunk(1, 5, new double[] {1d, 2d}, new int[] {2, 3});
        try {
            chunk.splitAt(0);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            chunk.splitAt(6);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        DataChunk.Split<DoublePoint, DoubleDataChunk> split = chunk.splitAt(4);
        assertNotNull(split.getChunk1());
        assertNotNull(split.getChunk2());
        assertEquals(1, split.getChunk1().getOffset());
        assertInstanceOf(CompressedDoubleDataChunk.class, split.getChunk1());
        assertEquals(3, ((CompressedDoubleDataChunk) split.getChunk1()).getUncompressedLength());
        assertArrayEquals(new double[] {1d, 2d}, ((CompressedDoubleDataChunk) split.getChunk1()).getStepValues(), 0d);
        assertArrayEquals(new int[] {2, 1}, ((CompressedDoubleDataChunk) split.getChunk1()).getStepLengths());
        assertEquals(4, split.getChunk2().getOffset());
        assertInstanceOf(CompressedDoubleDataChunk.class, split.getChunk2());
        assertEquals(2, ((CompressedDoubleDataChunk) split.getChunk2()).getUncompressedLength());
        assertArrayEquals(new double[] {2d}, ((CompressedDoubleDataChunk) split.getChunk2()).getStepValues(), 0d);
        assertArrayEquals(new int[] {2}, ((CompressedDoubleDataChunk) split.getChunk2()).getStepLengths());
    }

    @Test
    void uncompressedMergeTest() {
        UncompressedDoubleDataChunk chunk1 = new UncompressedDoubleDataChunk(1, new double[]{1d, 2d, 3d, 4d, 5d});
        UncompressedDoubleDataChunk chunk2 = new UncompressedDoubleDataChunk(6, new double[]{6d, 7d, 8d});
        UncompressedDoubleDataChunk chunk3 = new UncompressedDoubleDataChunk(1, new double[]{6d, 7d, 8d});

        //Merge chunk1 + chunk2
        DoubleDataChunk merge = chunk1.append(chunk2);
        assertNotNull(merge);
        assertInstanceOf(UncompressedDoubleDataChunk.class, merge);
        assertEquals(1, merge.getOffset());
        assertArrayEquals(new double[] {1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d}, ((UncompressedDoubleDataChunk) merge).getValues(), 0d);

        //Merge chunk1 + chunk3
        try {
            chunk1.append(chunk3);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

    }

    @Test
    void compressedMergeTest() {
        CompressedDoubleDataChunk chunk1 = new CompressedDoubleDataChunk(1, 5, new double[]{1d, 2d}, new int[]{2, 3});
        CompressedDoubleDataChunk chunk2 = new CompressedDoubleDataChunk(6, 5, new double[]{3d, 4d}, new int[]{2, 3});
        CompressedDoubleDataChunk chunk3 = new CompressedDoubleDataChunk(11, 3, new double[]{4d, 5d}, new int[]{2, 1});

        //Merge chunk1 + chunk2
        DoubleDataChunk merge = chunk1.append(chunk2);
        assertNotNull(merge);
        assertInstanceOf(CompressedDoubleDataChunk.class, merge);
        assertEquals(1, merge.getOffset());
        assertEquals(10, merge.getLength());
        assertArrayEquals(new double[] {1d, 2d, 3d, 4d}, ((CompressedDoubleDataChunk) merge).getStepValues(), 0d);
        assertArrayEquals(new int[] {2, 3, 2, 3}, ((CompressedDoubleDataChunk) merge).getStepLengths());

        //Merge chunk2 + chunk3
        merge = chunk2.append(chunk3);
        assertNotNull(merge);
        assertInstanceOf(CompressedDoubleDataChunk.class, merge);
        assertEquals(6, merge.getOffset());
        assertEquals(8, merge.getLength());
        assertArrayEquals(new double[] {3d, 4d, 5d}, ((CompressedDoubleDataChunk) merge).getStepValues(), 0d);
        assertArrayEquals(new int[] {2, 5, 1}, ((CompressedDoubleDataChunk) merge).getStepLengths());

        //Merge chunk1 + chunk3
        try {
            chunk1.append(chunk3);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

    }

    @Test
    void nanIssueTest() {
        UncompressedDoubleDataChunk chunk = new UncompressedDoubleDataChunk(0, new double[] {1d, Double.NaN, Double.NaN});
        String json = JsonUtil.toJson(chunk::writeJson);

        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"offset\" : 0,",
                "  \"values\" : [ 1.0, NaN, NaN ]",
                "}");
        assertEquals(jsonRef, json);

        List<DoubleDataChunk> doubleChunks = new ArrayList<>();
        List<StringDataChunk> stringChunks = new ArrayList<>();
        JsonUtil.parseJson(json, parser -> {
            DataChunk.parseJson(parser, doubleChunks, stringChunks);
            return null;
        });
        assertEquals(1, doubleChunks.size());
        assertEquals(0, stringChunks.size());
        assertInstanceOf(UncompressedDoubleDataChunk.class, doubleChunks.get(0));
        assertArrayEquals(new double[] {1d, Double.NaN, Double.NaN}, ((UncompressedDoubleDataChunk) doubleChunks.get(0)).getValues(), 0d);
    }
}
