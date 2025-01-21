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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class RegularTimeSeriesIndex extends AbstractTimeSeriesIndex {

    public static final String TYPE = "regularIndex";

    private final Instant startInstant;
    private final Instant endInstant;
    private final Duration deltaT;

    // computed from the previous fields; startTime and endTime are inclusive,
    // with rounding to have easier interactions with calendar dates (the
    // number of milliseconds in a calendar date is not fixed, because of leap
    // seconds, daylight saving time, and leap years), so if we didn't round
    // and took the floor or the ceiling, it would give surprising results
    // between 2 calendar dates.
    private final int pointCount;

    public RegularTimeSeriesIndex(Instant startInstant, Instant endInstant, Duration deltaT) {
        if (deltaT.isNegative()) {
            throw new IllegalArgumentException("Bad spacing value " + deltaT);
        }
        if (deltaT.compareTo(Duration.between(startInstant, endInstant)) > 0) {
            throw new IllegalArgumentException("Spacing " + deltaT + " is longer than interval " + (Duration.between(startInstant, endInstant)));
        }
        long computedPointCount = computePointCount(startInstant, endInstant, deltaT);
        if (computedPointCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Point Count " + computedPointCount + " is bigger than max allowed value " + Integer.MAX_VALUE);
        }
        this.startInstant = startInstant;
        this.endInstant = endInstant;
        this.deltaT = deltaT;
        this.pointCount = (int) computedPointCount;
    }

    /**
     * @deprecated Replaced by {@link RegularTimeSeriesIndex#RegularTimeSeriesIndex(Instant, Instant, Duration)}
     */
    @Deprecated(since = "6.7.0")
    public RegularTimeSeriesIndex(long startTime, long endTime, long spacing) {
        this(TimeSeriesIndex.longToInstant(startTime, 1_000L),
            TimeSeriesIndex.longToInstant(endTime, 1_000L),
            Duration.ofMillis(spacing));
//        if (startTime < 0) {
//            throw new IllegalArgumentException("Bad start time value " + startTime);
//        }
//        if (endTime < 0) {
//            throw new IllegalArgumentException("Bad end time value " + endTime);
//        }
//        if (spacing < 0) {
//            throw new IllegalArgumentException("Bad spacing value " + spacing);
//        }
//        if (spacing > endTime - startTime) {
//            throw new IllegalArgumentException("Spacing " + spacing + " is longer than interval " + (endTime - startTime));
//        }
//        long computedPointCount = computePointCount(startTime, endTime, spacing);
//        if (computedPointCount > Integer.MAX_VALUE) {
//            throw new IllegalArgumentException("Point Count " + computedPointCount + " is bigger than max allowed value " + Integer.MAX_VALUE);
//        }
//        this.startInstant = TimeSeriesIndex.longToInstant(startTime, 1_000L);
//        this.endInstant = TimeSeriesIndex.longToInstant(endTime, 1_000L);
//        this.deltaT = Duration.ofMillis(spacing);
//        this.pointCount = (int) computedPointCount;
    }

    public static RegularTimeSeriesIndex create(Instant start, Instant end, Duration spacing) {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(spacing);
        return new RegularTimeSeriesIndex(start, end, spacing);
    }

    public static RegularTimeSeriesIndex create(Interval interval, Duration spacing) {
        Objects.requireNonNull(interval);
        return create(interval.getStart(), interval.getEnd(), spacing);
    }

    public static RegularTimeSeriesIndex parseJson(JsonParser parser) {
        Objects.requireNonNull(parser);
        JsonToken token;
        try {
            Instant startInstant = Instant.ofEpochMilli(Long.MIN_VALUE);
            Instant endInstant = Instant.ofEpochMilli(Long.MIN_VALUE);
            Duration deltaT = Duration.ofNanos(Long.MIN_VALUE);
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case FIELD_NAME -> {
                        String fieldName = parser.currentName();
                        switch (fieldName) {
                            // Precision in ms
                            case "startTime" -> startInstant = TimeSeriesIndex.longToInstant(parser.nextLongValue(-1), 1_000L);
                            case "endTime" -> endInstant = TimeSeriesIndex.longToInstant(parser.nextLongValue(-1), 1_000L);
                            case "spacing" -> deltaT = Duration.ofMillis(parser.nextLongValue(-1));
                            // Precision in ns
                            case "startInstant" -> startInstant = TimeSeriesIndex.longToInstant(parser.nextLongValue(-1), 1_000_000_000L);
                            case "endInstant" -> endInstant = TimeSeriesIndex.longToInstant(parser.nextLongValue(-1), 1_000_000_000L);
                            case "deltaT" -> deltaT = Duration.ofNanos(parser.nextLongValue(-1));
                            default -> throw new IllegalStateException("Unexpected field " + fieldName);
                        }
                    }
                    case END_OBJECT -> {
                        if (startInstant.equals(Instant.ofEpochMilli(Long.MIN_VALUE)) ||
                            endInstant.equals(Instant.ofEpochMilli(Long.MIN_VALUE)) ||
                            deltaT.equals(Duration.ofNanos(Long.MIN_VALUE))) {
                            throw new IllegalStateException("Incomplete regular time series index json");
                        }
                        return new RegularTimeSeriesIndex(startInstant, endInstant, deltaT);
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

    /**
     * @deprecated Replaced by {@link RegularTimeSeriesIndex#getStartInstant()}
     */
    @Deprecated(since = "6.7.0")
    public long getStartTime() {
        return TimeSeriesIndex.instantToLong(startInstant, 1_000L);
    }

    public Instant getStartInstant() {
        return startInstant;
    }

    /**
     * @deprecated Replaced by {@link RegularTimeSeriesIndex#getEndInstant()}
     */
    @Deprecated(since = "6.7.0")
    public long getEndTime() {
        return TimeSeriesIndex.instantToLong(endInstant, 1_000L);
    }

    public Instant getEndInstant() {
        return endInstant;
    }

    /**
     * @deprecated Replaced by {@link RegularTimeSeriesIndex#getDeltaT()}
     */
    @Deprecated(since = "6.7.0")
    public long getSpacing() {
        return deltaT.toMillis();
    }

    public Duration getDeltaT() {
        return deltaT;
    }

    private static long computePointCount(long startTime, long endTime, long spacing) {
        return Math.round(((double) (endTime - startTime)) / spacing) + 1;
    }

    private static long computePointCount(Instant startTime, Instant endTime, Duration spacing) {
        return Math.round(((double) (Duration.between(startTime, endTime).toNanos())) / spacing.toNanos()) + 1;
    }

    @Override
    public int getPointCount() {
        return pointCount;
    }

    @Override
    public Instant getInstantAt(int point) {
        return startInstant.plus(deltaT.multipliedBy(point));
    }

    @Override
    public int hashCode() {
        return Objects.hash(startInstant, endInstant, deltaT);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesIndex) {
            RegularTimeSeriesIndex otherIndex = (RegularTimeSeriesIndex) obj;
            return startInstant.equals(otherIndex.startInstant) &&
                endInstant.equals(otherIndex.endInstant) &&
                deltaT.equals(otherIndex.deltaT);
        }
        return false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void writeJson(JsonGenerator generator, TimeSeries.TimeFormat timeFormat) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartObject();
            if (timeFormat == TimeSeries.TimeFormat.MILLIS) {
                generator.writeNumberField("startTime", TimeSeriesIndex.instantToLong(startInstant, 1_000L));
                generator.writeNumberField("endTime", TimeSeriesIndex.instantToLong(endInstant, 1_000L));
                generator.writeNumberField("spacing", deltaT.toMillis());
            } else {
                generator.writeNumberField("startInstant", TimeSeriesIndex.instantToLong(startInstant, 1_000_000_000L));
                generator.writeNumberField("endInstant", TimeSeriesIndex.instantToLong(endInstant, 1_000_000_000L));
                generator.writeNumberField("deltaT", deltaT.toNanos());
            }
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Iterator<Instant> iterator() {
        return new Iterator<>() {

            Instant time = startInstant;

            @Override
            public boolean hasNext() {
                return time.compareTo(endInstant) <= 0;
            }

            @Override
            public Instant next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Instant instant = Instant.ofEpochSecond(time.getEpochSecond(), time.getNano());
                time = time.plus(deltaT);
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
        return "RegularTimeSeriesIndex(startInstant=" + startInstant + ", endInstant=" + endInstant + ", deltaT=" + deltaT + ")";
    }
}
