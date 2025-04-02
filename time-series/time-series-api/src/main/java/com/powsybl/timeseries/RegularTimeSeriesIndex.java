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
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.powsybl.timeseries.TimeSeries.parseNanosToInstant;
import static com.powsybl.timeseries.TimeSeries.writeInstantToNanoString;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class RegularTimeSeriesIndex extends AbstractTimeSeriesIndex {

    public static final String TYPE = "regularIndex";

    private final Instant startInstant;
    private final Instant endInstant;
    private final Duration timeStep;

    // computed from the previous fields; startTime and endTime are inclusive,
    // with rounding to have easier interactions with calendar dates (the
    // number of milliseconds in a calendar date is not fixed, because of leap
    // seconds, daylight saving time, and leap years), so if we didn't round
    // and took the floor or the ceiling, it would give surprising results
    // between 2 calendar dates.
    private final int pointCount;

    public RegularTimeSeriesIndex(Instant startInstant, Instant endInstant, Duration timeStep) {
        if (timeStep.isNegative()) {
            throw new IllegalArgumentException("Bad timeStep value " + timeStep);
        }
        if (timeStep.compareTo(Duration.between(startInstant, endInstant)) > 0) {
            throw new IllegalArgumentException("TimeStep " + timeStep + " is longer than interval " + (Duration.between(startInstant, endInstant)));
        }
        long computedPointCount = computePointCount(startInstant, endInstant, timeStep);
        if (computedPointCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Point Count " + computedPointCount + " is bigger than max allowed value " + Integer.MAX_VALUE);
        }
        this.startInstant = startInstant;
        this.endInstant = endInstant;
        this.timeStep = timeStep;
        this.pointCount = (int) computedPointCount;
    }

    /**
     * @deprecated Replaced by {@link RegularTimeSeriesIndex#RegularTimeSeriesIndex(Instant, Instant, Duration)}
     */
    @Deprecated(since = "6.7.0")
    public RegularTimeSeriesIndex(long startTime, long endTime, long spacing) {
        this(Instant.ofEpochMilli(startTime),
            Instant.ofEpochMilli(endTime),
            Duration.ofMillis(spacing));
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
            Instant startInstant = null;
            Instant endInstant = null;
            Duration timeStep = null;
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case FIELD_NAME -> {
                        String fieldName = parser.currentName();
                        switch (fieldName) {
                            // Precision in ms
                            case "startTime" -> startInstant = Instant.ofEpochMilli(parser.nextLongValue(-1));
                            case "endTime" -> endInstant = Instant.ofEpochMilli(parser.nextLongValue(-1));
                            case "spacing" -> timeStep = Duration.ofMillis(parser.nextLongValue(-1));
                            // Precision in ns
                            case "startInstant" -> startInstant = parseNanoTokenToInstant(parser);
                            case "endInstant" -> endInstant = parseNanoTokenToInstant(parser);
                            case "timeStep" -> timeStep = Duration.ofNanos(parser.nextLongValue(-1));
                            default -> throw new IllegalStateException("Unexpected field " + fieldName);
                        }
                    }
                    case END_OBJECT -> {
                        if (startInstant == null || endInstant == null || timeStep == null) {
                            throw new IllegalStateException("Incomplete regular time series index json");
                        }
                        return new RegularTimeSeriesIndex(startInstant, endInstant, timeStep);
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
        return startInstant.toEpochMilli();
    }

    public Instant getStartInstant() {
        return startInstant;
    }

    /**
     * @deprecated Replaced by {@link RegularTimeSeriesIndex#getEndInstant()}
     */
    @Deprecated(since = "6.7.0")
    public long getEndTime() {
        return endInstant.toEpochMilli();
    }

    public Instant getEndInstant() {
        return endInstant;
    }

    /**
     * @deprecated Replaced by {@link RegularTimeSeriesIndex#getTimeStep()}
     */
    @Deprecated(since = "6.7.0")
    public long getSpacing() {
        return timeStep.toMillis();
    }

    public Duration getTimeStep() {
        return timeStep;
    }

    private static long computePointCount(Instant startTime, Instant endTime, Duration spacing) {
        // Checks to avoid invalid duration and instants
        if (startTime == null || endTime == null || spacing == null) {
            throw new IllegalArgumentException("startTime, endTime, and spacing cannot be null.");
        }

        Duration duration = Duration.between(startTime, endTime);
        Long maxDays = 365L * 200;
        if (duration > maxDays || spacing > maxDays) {
            throw new IllegalArgumentException("Time range or spacing exceeds " + maxDays + " days.");
        }

        return Math.round(((double) (duration.toNanos())) / spacing.toNanos()) + 1;
    }

    @Override
    public int getPointCount() {
        return pointCount;
    }

    @Override
    public Instant getInstantAt(int point) {
        return startInstant.plus(timeStep.multipliedBy(point));
    }

    @Override
    public int hashCode() {
        return Objects.hash(startInstant, endInstant, timeStep);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesIndex) {
            RegularTimeSeriesIndex otherIndex = (RegularTimeSeriesIndex) obj;
            return startInstant.equals(otherIndex.startInstant) &&
                endInstant.equals(otherIndex.endInstant) &&
                timeStep.equals(otherIndex.timeStep);
        }
        return false;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * <p>Writes the index in a JSON format.</p>
     * <p>If {@code timeFormat = ExportFormat.MILLISECONDS}, values are written in millisecond precision. Else, if
     * {@code timeFormat = ExportFormat.NANOSECONDS}, values are written in nanosecond precision</p>
     */
    @Override
    public void writeJson(JsonGenerator generator, ExportFormat timeFormat) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartObject();
            if (timeFormat == ExportFormat.MILLISECONDS) {
                generator.writeNumberField("startTime", startInstant.toEpochMilli());
                generator.writeNumberField("endTime", endInstant.toEpochMilli());
                generator.writeNumberField("spacing", timeStep.toMillis());
            } else {
                generator.writeNumberField("startInstant", new BigInteger(writeInstantToNanoString(startInstant)));
                generator.writeNumberField("endInstant", new BigInteger(writeInstantToNanoString(endInstant)));
                generator.writeNumberField("timeStep", timeStep.toNanos());
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
                Instant instant = time;
                time = time.plus(timeStep);
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
        return "RegularTimeSeriesIndex(startInstant=" + startInstant + ", endInstant=" + endInstant + ", timeStep=" + timeStep + ")";
    }

    private static Instant parseNanoTokenToInstant(JsonParser parser) throws IOException {
        // The next token contains the value
        parser.nextToken();

        // Parse the value
        return parseNanosToInstant(parser.getValueAsString());
    }
}
