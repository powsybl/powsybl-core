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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.powsybl.timeseries.TimeSeries.parseNanosToInstant;
import static com.powsybl.timeseries.TimeSeries.writeInstantToNanoString;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class IrregularTimeSeriesIndex extends AbstractTimeSeriesIndex {

    public static final String TYPE = "irregularIndex";

    private final Instant[] instants;

    public IrregularTimeSeriesIndex(Instant[] instants) {
        this.instants = Objects.requireNonNull(instants);
        if (instants.length == 0) {
            throw new IllegalArgumentException("Empty time list");
        }
    }

    /**
     * @param times array of dates given in ms
     * @deprecated Replaced by {@link IrregularTimeSeriesIndex#IrregularTimeSeriesIndex(Instant[])}
     */
    @Deprecated(since = "6.7.0")
    public IrregularTimeSeriesIndex(long[] times) {
        this(Arrays.stream(times).mapToObj(Instant::ofEpochMilli).toArray(Instant[]::new));
    }

    public static IrregularTimeSeriesIndex create(Instant... instants) {
        return new IrregularTimeSeriesIndex(instants);
    }

    public static IrregularTimeSeriesIndex create(List<Instant> instants) {
        Objects.requireNonNull(instants);
        return create(instants.toArray(new Instant[0]));
    }

    public static IrregularTimeSeriesIndex parseJson(JsonParser parser) {
        return parseJson(parser, ExportFormat.MILLISECONDS);
    }

    public static IrregularTimeSeriesIndex parseJson(JsonParser parser, ExportFormat timeFormat) {
        Objects.requireNonNull(parser);
        JsonToken token;
        try {
            // Times parsed and converted to ns
            List<Instant> instants = new ArrayList<>();
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.VALUE_NUMBER_INT) {
                    instants.add(timeFormat == ExportFormat.MILLISECONDS ?
                        Instant.ofEpochMilli(parser.getLongValue()) :
                        parseNanosToInstant(parser.getValueAsString()));
                } else if (token == JsonToken.END_ARRAY) {
                    return IrregularTimeSeriesIndex.create(instants);
                }
            }
            throw new IllegalStateException("Should not happen");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int getPointCount() {
        return instants.length;
    }

    @Override
    public Instant getInstantAt(int point) {
        return instants[point];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(instants);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IrregularTimeSeriesIndex otherIndex) {
            return Arrays.equals(instants, otherIndex.instants);
        }
        return false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void writeJson(JsonGenerator generator, ExportFormat timeFormat) {
        Objects.requireNonNull(generator);
        try {
            if (timeFormat == ExportFormat.MILLISECONDS) {
                generator.writeArray(Arrays.stream(instants)
                    .mapToLong(Instant::toEpochMilli)
                    .toArray(), 0, instants.length);
            } else {
                generator.writeStartArray();
                for (Instant instant : instants) {
                    generator.writeNumber(new BigInteger(writeInstantToNanoString(instant)));
                }
                generator.writeEndArray();
            }
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
        return Arrays.stream(instants);
    }

    @Override
    public String toString() {
        return "IrregularTimeSeriesIndex(times=" + stream().toList() + ")";
    }
}
