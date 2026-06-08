/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.Range;
import org.jspecify.annotations.NonNull;

import java.nio.DoubleBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
public final class FixedArrayDoubleTimeSeries implements DoubleTimeSeries {

    private final double[] values;

    FixedArrayDoubleTimeSeries(double... values) {
        this.values = values;
    }

    @Override
    public double[] toArray() {
        return values;
    }

    @Override
    public void fillBuffer(DoubleBuffer buffer, int timeSeriesOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillBuffer(BigDoubleBuffer buffer, long timeSeriesOffset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimeSeriesMetadata getMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void synchronize(TimeSeriesIndex newIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<DoublePoint> uncompressedStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<DoublePoint> uncompressedIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DoubleTimeSeries> split(int newChunkSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DoubleTimeSeries> splitByRanges(List<Range<@NonNull Integer>> newChunks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimeSeriesNameResolver(TimeSeriesNameResolver resolver) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toJson() {
        return "";
    }
}
