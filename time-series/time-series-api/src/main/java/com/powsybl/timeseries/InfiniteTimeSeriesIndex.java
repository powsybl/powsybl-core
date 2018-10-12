/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@rte-france.com>
 */
public enum InfiniteTimeSeriesIndex implements TimeSeriesIndex {

    INSTANCE;

    public static final String TYPE = "infiniteIndex";
    public static final long START_TIME = 0L;
    public static final long END_TIME = Long.MAX_VALUE;

    @Override
    public int getPointCount() {
        return 2;
    }

    @Override
    public long getTimeAt(int point) {
        if (point == 0) {
            return START_TIME;
        } else if (point == 1) {
            return END_TIME;
        } else {
            throw new TimeSeriesException("Point " + point + " not found");
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartObject();
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static InfiniteTimeSeriesIndex parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);
        JsonToken token;
        try {
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.END_OBJECT) {
                    return InfiniteTimeSeriesIndex.INSTANCE;
                }
            }
            throw new IllegalStateException("Should not happen");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "InfiniteTimeSeriesIndex()";
    }
}
