/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IrregularTimeSeriesIndex extends AbstractTimeSeriesIndex {

    public static final String TYPE = "irregularIndex";

    private final long[] times; // time array in ms from epoch

    public IrregularTimeSeriesIndex(long[] times) {
        this.times = Objects.requireNonNull(times);
        if (times.length == 0) {
            throw new IllegalArgumentException("Empty time list");
        }
    }

    public static IrregularTimeSeriesIndex create(Instant... instants) {
        return create(Arrays.asList(instants));
    }

    public static IrregularTimeSeriesIndex create(List<Instant> instants) {
        Objects.requireNonNull(instants);
        return new IrregularTimeSeriesIndex(instants.stream().mapToLong(Instant::toEpochMilli).toArray());
    }

    public static IrregularTimeSeriesIndex parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);
        JsonToken token;
        try {
            TLongArrayList times = new TLongArrayList();
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.VALUE_NUMBER_INT) {
                    times.add(parser.getLongValue());
                } else if (token == JsonToken.END_ARRAY) {
                    return new IrregularTimeSeriesIndex(times.toArray());
                }
            }
            throw new IllegalStateException("Should not happen");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int getPointCount() {
        return times.length;
    }

    @Override
    public long getTimeAt(int point) {
        return times[point];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(times);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IrregularTimeSeriesIndex otherIndex) {
            return Arrays.equals(times, otherIndex.times);
        }
        return false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        Objects.requireNonNull(generator);
        try {
            generator.writeArray(times, 0, times.length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Iterator<Instant> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<Instant> stream() {
        return Arrays.stream(times).mapToObj(Instant::ofEpochMilli);
    }

    @Override
    public String toString() {
        return "IrregularTimeSeriesIndex(times=" + stream().toList() + ")";
    }
}
