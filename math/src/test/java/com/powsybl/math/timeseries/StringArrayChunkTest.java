/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.math.timeseries.json.TimeSeriesJsonModule;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringArrayChunkTest {

    @Test
    public void baseTest() throws IOException {
        UncompressedStringArrayChunk chunk = new UncompressedStringArrayChunk(1, new String[] {"a", "b", "c"});
        assertEquals(1, chunk.getOffset());
        assertEquals(3, chunk.getLength());
        assertArrayEquals(new String[]{"a", "b", "c"}, chunk.getValues());
        assertEquals(6, chunk.getEstimatedSize());
        assertFalse(chunk.isCompressed());
        assertEquals(1d, chunk.getCompressionFactor(), 0d);
        String[] array = new String[4];
        chunk.fillArray(array);
        assertArrayEquals(new String[] {null, "a", "b", "c"}, array);

        // json test
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"offset\" : 1,",
                "  \"values\" : [ \"a\", \"b\", \"c\" ]",
                "}");
        assertEquals(jsonRef, JsonUtil.toJson(chunk::writeJson));

        // test json with object mapper
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new TimeSeriesJsonModule());

        List<StringArrayChunk> chunks = objectMapper.readValue(objectMapper.writeValueAsString(Arrays.asList(chunk)),
                                                               TypeFactory.defaultInstance().constructCollectionType(List.class, StringArrayChunk.class));
        assertEquals(1, chunks.size());
        assertEquals(chunk, chunks.get(0));

        // check base class (ArrayChunk) deserializer
        assertTrue(objectMapper.readValue(objectMapper.writeValueAsString(chunk), ArrayChunk.class) instanceof StringArrayChunk);

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
        UncompressedStringArrayChunk chunk = new UncompressedStringArrayChunk(1, new String[] {"aaa", "bbb", "bbb", "bbb", "bbb", "ccc"});
        StringArrayChunk maybeCompressedChunk = chunk.tryToCompress();
        assertTrue(maybeCompressedChunk instanceof CompressedStringArrayChunk);
        CompressedStringArrayChunk compressedChunk = (CompressedStringArrayChunk) maybeCompressedChunk;
        assertEquals(1, compressedChunk.getOffset());
        assertEquals(6, compressedChunk.getLength());
        assertTrue(compressedChunk.isCompressed());
        assertEquals(30, compressedChunk.getEstimatedSize());
        assertEquals(30d / 36, compressedChunk.getCompressionFactor(), 0d);
        assertArrayEquals(new String[] {"aaa", "bbb", "ccc"}, compressedChunk.getStepValues());
        assertArrayEquals(new int[] {1, 4, 1}, compressedChunk.getStepLengths());
        String[] array = new String[7];
        compressedChunk.fillArray(array);
        assertArrayEquals(new String[] {null, "aaa", "bbb", "bbb", "bbb", "bbb", "ccc"}, array);
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
        UncompressedStringArrayChunk chunk = new UncompressedStringArrayChunk(1, new String[] {"aaa", "bbb", "bbb", "ccc"});
        assertSame(chunk, chunk.tryToCompress());
    }
}
