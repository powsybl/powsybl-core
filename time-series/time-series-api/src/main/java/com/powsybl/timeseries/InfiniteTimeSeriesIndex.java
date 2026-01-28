/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;

import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class InfiniteTimeSeriesIndex extends AbstractTimeSeriesIndex {

    public static final InfiniteTimeSeriesIndex INSTANCE = new InfiniteTimeSeriesIndex();

    public static final String TYPE = "infiniteIndex";
    public static final Instant START_INSTANT = Instant.ofEpochMilli(0L);
    public static final Instant END_INSTANT = Instant.ofEpochMilli(Long.MAX_VALUE);

    @Override
    public int getPointCount() {
        return 2;
    }

    /**
     * @deprecated Replaced by {@link #getInstantAt(int)}}
     */
    @Deprecated(since = "6.7.0")
    @Override
    public long getTimeAt(int point) {
        return getInstantAt(point).toEpochMilli();
    }

    @Override
    public Instant getInstantAt(int point) {
        if (point == 0) {
            return START_INSTANT;
        } else if (point == 1) {
            return END_INSTANT;
        } else {
            throw new TimeSeriesException("Point " + point + " not found");
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void writeJson(JsonGenerator generator, ExportFormat timeFormat) {
        Objects.requireNonNull(generator);
        generator.writeStartObject();
        generator.writeEndObject();
    }

    public static InfiniteTimeSeriesIndex parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);
        JsonToken token;
        while ((token = parser.nextToken()) != null) {
            if (token == JsonToken.END_OBJECT) {
                return InfiniteTimeSeriesIndex.INSTANCE;
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    @Override
    public Iterator<Instant> iterator() {
        return Arrays.asList(START_INSTANT, END_INSTANT).iterator();
    }

    @Override
    public Stream<Instant> stream() {
        return Stream.of(START_INSTANT, END_INSTANT);
    }

    @Override
    public String toString() {
        return "InfiniteTimeSeriesIndex()";
    }
}
