/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class RegularTimeSeriesIndex extends AbstractTimeSeriesIndex {

    public static final String TYPE = "regularIndex";

    private final long startTime; // in ms from epoch

    private final long endTime; // in ms from epoch

    private final long spacing; // in ms

    // computed from the previous fields; startTime and endTime are inclusive,
    // with rounding to have easier interactions with calendar dates (the
    // number of milliseconds in a calendar date is not fixed, because of leap
    // seconds, daylight saving time, and leap years), so if we didn't round
    // and took the floor or the ceiling, it would give surprising results
    // between 2 calendar dates.
    private final int pointCount;

    public RegularTimeSeriesIndex(long startTime, long endTime, long spacing) {
        if (startTime < 0) {
            throw new IllegalArgumentException("Bad start time value " + startTime);
        }
        if (endTime < 0) {
            throw new IllegalArgumentException("Bad end time value " + endTime);
        }
        if (spacing < 0) {
            throw new IllegalArgumentException("Bad spacing value " + spacing);
        }
        if (spacing > endTime - startTime) {
            throw new IllegalArgumentException("Spacing " + spacing + " is longer than interval " + (endTime - startTime));
        }
        long computedPointCount = computePointCount(startTime, endTime, spacing);
        if (computedPointCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Point Count " + computedPointCount + " is bigger than max allowed value " + Integer.MAX_VALUE);
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.spacing = spacing;
        this.pointCount = (int) computedPointCount;
    }

    public static RegularTimeSeriesIndex create(Instant start, Instant end, Duration spacing) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(spacing);
        return new RegularTimeSeriesIndex(start.toEpochMilli(), end.toEpochMilli(), spacing.toMillis());
    }

    public static RegularTimeSeriesIndex create(Interval interval, Duration spacing) {
        Objects.requireNonNull(interval);
        return create(interval.getStart(), interval.getEnd(), spacing);
    }

    public static RegularTimeSeriesIndex parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);
        JsonToken token;
        try {
            long startTime = -1;
            long endTime = -1;
            long spacing = -1;
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case FIELD_NAME -> {
                        String fieldName = parser.getCurrentName();
                        switch (fieldName) {
                            case "startTime" -> startTime = parser.nextLongValue(-1);
                            case "endTime" -> endTime = parser.nextLongValue(-1);
                            case "spacing" -> spacing = parser.nextLongValue(-1);
                            default -> throw new IllegalStateException("Unexpected field " + fieldName);
                        }
                    }
                    case END_OBJECT -> {
                        if (startTime == -1 || endTime == -1 || spacing == -1) {
                            throw new IllegalStateException("Incomplete regular time series index json");
                        }
                        return new RegularTimeSeriesIndex(startTime, endTime, spacing);
                    }
                    default -> {
                        // Do nothing
                    }
                }
            }
            throw new IllegalStateException("Should not happen");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getSpacing() {
        return spacing;
    }

    private static long computePointCount(long startTime, long endTime, long spacing) {
        return Math.round(((double) (endTime - startTime)) / spacing) + 1;
    }

    @Override
    public int getPointCount() {
        return pointCount;
    }

    @Override
    public long getTimeAt(int point) {
        return startTime + point * spacing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, spacing);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesIndex) {
            RegularTimeSeriesIndex otherIndex = (RegularTimeSeriesIndex) obj;
            return startTime == otherIndex.startTime &&
                    endTime == otherIndex.endTime &&
                    spacing == otherIndex.spacing;
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
            generator.writeStartObject();
            generator.writeNumberField("startTime", startTime);
            generator.writeNumberField("endTime", endTime);
            generator.writeNumberField("spacing", spacing);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Iterator<Instant> iterator() {
        return new Iterator<Instant>() {

            long time = startTime;

            @Override
            public boolean hasNext() {
                return time <= endTime;
            }

            @Override
            public Instant next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Instant instant = Instant.ofEpochMilli(time);
                time += spacing;
                return instant;
            }
        };
    }

    @Override
    public Stream<Instant> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(),
                                                                        Spliterator.ORDERED | Spliterator.IMMUTABLE),
                                    false);
    }

    @Override
    public String toString() {
        return "RegularTimeSeriesIndex(startTime=" + Instant.ofEpochMilli(startTime) + ", endTime=" + Instant.ofEpochMilli(endTime) +
                ", spacing=" + Duration.ofMillis(spacing) + ")";
    }
}
