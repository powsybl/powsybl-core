/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.json.TimeSeriesJsonModule;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringDataChunkTest {

    @Test
    public void baseTest() throws IOException {
        UncompressedStringDataChunk chunk = new UncompressedStringDataChunk(1, new String[] {"a", "b", "c"});
        assertEquals(1, chunk.getOffset());
        assertEquals(3, chunk.getLength());
        assertArrayEquals(new String[]{"a", "b", "c"}, chunk.getValues());
        assertEquals(6, chunk.getEstimatedSize());
        assertFalse(chunk.isCompressed());
        assertEquals(1d, chunk.getCompressionFactor(), 0d);
        CompactStringBuffer buffer = new CompactStringBuffer(ByteBuffer::allocate, 4);
        chunk.fillBuffer(buffer, 0);
        assertArrayEquals(new String[] {null, "a", "b", "c"}, buffer.toArray());

        // json test
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"offset\" : 1,",
                "  \"values\" : [ \"a\", \"b\", \"c\" ]",
                "}");
        assertEquals(jsonRef, JsonUtil.toJson(chunk::writeJson));

        // test json with object mapper
        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new TimeSeriesJsonModule());

        List<StringDataChunk> chunks = objectMapper.readValue(objectMapper.writeValueAsString(Arrays.asList(chunk)),
                                                               TypeFactory.defaultInstance().constructCollectionType(List.class, StringDataChunk.class));
        assertEquals(1, chunks.size());
        assertEquals(chunk, chunks.get(0));

        // check base class (DataChunk) deserializer
        assertTrue(objectMapper.readValue(objectMapper.writeValueAsString(chunk), DataChunk.class) instanceof StringDataChunk);

        // stream test
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T00:45:00Z"),
                                                                     Duration.ofMinutes(15));
        assertEquals(ImmutableList.of(new StringPoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), "a"),
                                      new StringPoint(2, Instant.parse("2015-01-01T00:30:00Z").toEpochMilli(), "b"),
                                      new StringPoint(3, Instant.parse("2015-01-01T00:45:00Z").toEpochMilli(), "c")),
                chunk.stream(index).collect(Collectors.toList()));
    }

    @Test
    public void compressTest() throws IOException {
        UncompressedStringDataChunk chunk = new UncompressedStringDataChunk(1, new String[] {"aaa", "bbb", "bbb", "bbb", "bbb", "ccc"});
        StringDataChunk maybeCompressedChunk = chunk.tryToCompress();
        assertTrue(maybeCompressedChunk instanceof CompressedStringDataChunk);
        CompressedStringDataChunk compressedChunk = (CompressedStringDataChunk) maybeCompressedChunk;
        assertEquals(1, compressedChunk.getOffset());
        assertEquals(6, compressedChunk.getLength());
        assertTrue(compressedChunk.isCompressed());
        assertEquals(30, compressedChunk.getEstimatedSize());
        assertEquals(30d / 36, compressedChunk.getCompressionFactor(), 0d);
        assertArrayEquals(new String[] {"aaa", "bbb", "ccc"}, compressedChunk.getStepValues());
        assertArrayEquals(new int[] {1, 4, 1}, compressedChunk.getStepLengths());
        CompactStringBuffer buffer = new CompactStringBuffer(ByteBuffer::allocate, 7);
        compressedChunk.fillBuffer(buffer, 0);
        assertArrayEquals(new String[] {null, "aaa", "bbb", "bbb", "bbb", "bbb", "ccc"}, buffer.toArray());
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"offset\" : 1,",
                "  \"uncompressedLength\" : 6,",
                "  \"stepValues\" : [ \"aaa\", \"bbb\", \"ccc\" ],",
                "  \"stepLengths\" : [ 1, 4, 1 ]",
                "}");
        assertEquals(jsonRef, JsonUtil.toJson(compressedChunk::writeJson));
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:30:00Z"),
                                                                     Duration.ofMinutes(15));
        assertEquals(ImmutableList.of(new StringPoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), "aaa"),
                                      new StringPoint(2, Instant.parse("2015-01-01T00:30:00Z").toEpochMilli(), "bbb"),
                                      new StringPoint(6, Instant.parse("2015-01-01T01:30:00Z").toEpochMilli(), "ccc")),
                     compressedChunk.stream(index).collect(Collectors.toList()));
    }

    @Test
    public void compressFailureTest() throws IOException {
        UncompressedStringDataChunk chunk = new UncompressedStringDataChunk(1, new String[] {"aaa", "bbb", "bbb", "ccc"});
        assertSame(chunk, chunk.tryToCompress());
    }

    @Test
    public void splitTest() throws IOException {
        UncompressedStringDataChunk chunk = new UncompressedStringDataChunk(1, new String[]{"a", "b", "c"});
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
        DataChunk.Split<StringPoint, StringDataChunk> split = chunk.splitAt(2);
        assertNotNull(split.getChunk1());
        assertNotNull(split.getChunk2());
        assertEquals(1, split.getChunk1().getOffset());
        assertTrue(split.getChunk1() instanceof UncompressedStringDataChunk);
        assertArrayEquals(new String[] {"a"}, ((UncompressedStringDataChunk) split.getChunk1()).getValues());
        assertEquals(2, split.getChunk2().getOffset());
        assertTrue(split.getChunk2() instanceof UncompressedStringDataChunk);
        assertArrayEquals(new String[] {"b", "c"}, ((UncompressedStringDataChunk) split.getChunk2()).getValues());
    }

    @Test
    public void compressedSplitTest() throws IOException {
        // index  0   1   2   3   4   5
        // value  NaN a   a   b   b   b
        //            [-------]   [---]
        CompressedStringDataChunk chunk = new CompressedStringDataChunk(1, 5, new String[] {"a", "b"}, new int[] {2, 3});
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
        DataChunk.Split<StringPoint, StringDataChunk> split = chunk.splitAt(4);
        assertNotNull(split.getChunk1());
        assertNotNull(split.getChunk2());
        assertEquals(1, split.getChunk1().getOffset());
        assertTrue(split.getChunk1() instanceof CompressedStringDataChunk);
        assertEquals(3, ((CompressedStringDataChunk) split.getChunk1()).getUncompressedLength());
        assertArrayEquals(new String[] {"a", "b"}, ((CompressedStringDataChunk) split.getChunk1()).getStepValues());
        assertArrayEquals(new int[] {2, 1}, ((CompressedStringDataChunk) split.getChunk1()).getStepLengths());
        assertEquals(4, split.getChunk2().getOffset());
        assertTrue(split.getChunk2() instanceof CompressedStringDataChunk);
        assertEquals(2, ((CompressedStringDataChunk) split.getChunk2()).getUncompressedLength());
        assertArrayEquals(new String[] {"b"}, ((CompressedStringDataChunk) split.getChunk2()).getStepValues());
        assertArrayEquals(new int[] {2}, ((CompressedStringDataChunk) split.getChunk2()).getStepLengths());
    }

    @Test
    public void nullIssueTest() {
        String json = String.join(System.lineSeparator(),
                "{",
                "  \"offset\" : 0,",
                "  \"values\" : [ \"a\", null, null ]",
                "}");
        List<DoubleDataChunk> doubleChunks = new ArrayList<>();
        List<StringDataChunk> stringChunks = new ArrayList<>();
        JsonUtil.parseJson(json, parser -> {
            DataChunk.parseJson(parser, doubleChunks, stringChunks);
            return null;
        });
        assertEquals(0, doubleChunks.size());
        assertEquals(1, stringChunks.size());
        assertTrue(stringChunks.get(0) instanceof UncompressedStringDataChunk);
        assertArrayEquals(new String[] {"a", null, null}, ((UncompressedStringDataChunk) stringChunks.get(0)).getValues());
    }
}
