/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.json.JsonUtil;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DoubleArrayChunkTest {

    @Test
    public void baseTest() throws IOException {
        UncompressedDoubleArrayChunk chunk = new UncompressedDoubleArrayChunk(1, new double[] {1d, 2d, 3d});
        assertEquals(1, chunk.getOffset());
        assertEquals(3, chunk.getLength());
        assertArrayEquals(new double[]{1d, 2d, 3d}, chunk.getValues(), 0d);
        assertEquals(24, chunk.getEstimatedSize());
        assertFalse(chunk.isCompressed());
        assertEquals(1d, chunk.getCompressionFactor(), 0d);
        double[] array = new double[4];
        Arrays.fill(array, Double.NaN);
        chunk.fillArray(array);
        assertArrayEquals(new double[] {Double.NaN, 1d, 2d, 3d}, array, 0d);
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"offset\" : 1,",
                "  \"values\" : [ 1.0, 2.0, 3.0 ]",
                "}");
        assertEquals(jsonRef, JsonUtil.toJson(chunk::writeJson));
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T00:45:00Z"),
                                                                     Duration.ofMinutes(15), 1, 1);
        assertEquals(ImmutableList.of(new DoublePoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), 1d),
                                      new DoublePoint(2, Instant.parse("2015-01-01T00:30:00Z").toEpochMilli(), 2d),
                                      new DoublePoint(3, Instant.parse("2015-01-01T00:45:00Z").toEpochMilli(), 3d)),
                chunk.stream(index).collect(Collectors.toList()));
    }

    @Test
    public void compressTest() throws IOException {
        UncompressedDoubleArrayChunk chunk = new UncompressedDoubleArrayChunk(1, new double[] {1d, 2d, 2d, 2d, 2d, 3d});
        DoubleArrayChunk maybeCompressedChunk = chunk.tryToCompress();
        assertTrue(maybeCompressedChunk instanceof CompressedDoubleArrayChunk);
        CompressedDoubleArrayChunk compressedChunk = (CompressedDoubleArrayChunk) maybeCompressedChunk;
        assertEquals(1, compressedChunk.getOffset());
        assertEquals(6, compressedChunk.getLength());
        assertTrue(compressedChunk.isCompressed());
        assertEquals(36, compressedChunk.getEstimatedSize());
        assertEquals(36d / 48, compressedChunk.getCompressionFactor(), 0d);
        assertArrayEquals(new double[] {1d, 2d, 3d}, compressedChunk.getStepValues(), 0d);
        assertArrayEquals(new int[] {1, 4, 1}, compressedChunk.getStepLengths());
        double[] array = new double[7];
        Arrays.fill(array, Double.NaN);
        compressedChunk.fillArray(array);
        assertArrayEquals(new double[] {Double.NaN, 1d, 2d, 2d, 2d, 2d, 3d}, array, 0d);
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"offset\" : 1,",
                "  \"uncompressedLength\" : 6,",
                "  \"stepValues\" : [ 1.0, 2.0, 3.0 ],",
                "  \"stepLengths\" : [ 1, 4, 1 ]",
                "}");
        assertEquals(jsonRef, JsonUtil.toJson(compressedChunk::writeJson));
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:30:00Z"),
                                                                     Duration.ofMinutes(15), 1, 1);
        assertEquals(ImmutableList.of(new DoublePoint(1, Instant.parse("2015-01-01T00:15:00Z").toEpochMilli(), 1d),
                                      new DoublePoint(2, Instant.parse("2015-01-01T00:30:00Z").toEpochMilli(), 2d),
                                      new DoublePoint(6, Instant.parse("2015-01-01T01:30:00Z").toEpochMilli(), 3d)),
                     compressedChunk.stream(index).collect(Collectors.toList()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void compressConstrTest() {
        new CompressedDoubleArrayChunk(-3, 1, new double[] {1d}, new int[] {1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void compressConstrTest2() {
        new CompressedDoubleArrayChunk(0, 0, new double[] {1d}, new int[] {1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void compressConstrTest3() {
        new CompressedDoubleArrayChunk(0, 1, new double[] {}, new int[] {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void compressConstrTest4() {
        new CompressedDoubleArrayChunk(0, 1, new double[] {1d}, new int[] {});
    }

    @Test
    public void compressFailureTest() throws IOException {
        UncompressedDoubleArrayChunk chunk = new UncompressedDoubleArrayChunk(1, new double[] {1d, 2d, 2d, 3d});
        assertSame(chunk, chunk.tryToCompress());
    }
}
