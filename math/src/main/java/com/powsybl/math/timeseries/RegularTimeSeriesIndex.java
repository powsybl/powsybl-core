/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class RegularTimeSeriesIndex implements TimeSeriesIndex {

    private final long startTime; // in ms from epoch

    private final long endTime; // in ms from epoch

    private final long spacing; // in ms

    private final int firstVersion;

    private final int versionCount;

    public RegularTimeSeriesIndex(long startTime, long endTime, long spacing, int firstVersion, int versionCount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.spacing = spacing;
        if (spacing > endTime - startTime) {
            throw new IllegalArgumentException("Spacing " + spacing + " is longer than interval " + (endTime - startTime));
        }
        this.firstVersion = firstVersion;
        this.versionCount = versionCount;
    }

    public static RegularTimeSeriesIndex create(Interval interval, Duration spacing, int firstVersion, int versionCount) {
        return new RegularTimeSeriesIndex(interval.getStart().toEpochMilli(), interval.getEnd().toEpochMilli(),
                                          spacing.toMillis(), firstVersion, versionCount);
    }

    public static RegularTimeSeriesIndex parseJson(JsonParser parser) {
        JsonToken token;
        try {
            long startTime = -1;
            long endTime = -1;
            long spacing = -1;
            int firstVersion = -1;
            int versionCount = -1;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String fieldName = parser.getCurrentName();
                    switch (fieldName) {
                        case "regularIndex":
                            break;
                        case "startTime":
                            startTime = parser.nextLongValue(-1);
                            break;
                        case "endTime":
                            endTime = parser.nextLongValue(-1);
                            break;
                        case "spacing":
                            spacing = parser.nextLongValue(-1);
                            break;
                        case "firstVersion":
                            firstVersion = parser.nextIntValue(-1);
                            break;
                        case "versionCount":
                            versionCount = parser.nextIntValue(-1);
                            break;
                        default:
                            throw new IllegalStateException("Unexpected field " + fieldName);
                    }
                } else if (token == JsonToken.END_OBJECT) {
                    if (startTime == -1 || endTime == -1 || spacing == -1 || firstVersion == -1 || versionCount == -1) {
                        throw new IllegalStateException("Incomplete regular time series index json");
                    }
                    return new RegularTimeSeriesIndex(startTime, endTime, spacing, firstVersion, versionCount);
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

    @Override
    public int getFirstVersion() {
        return firstVersion;
    }

    @Override
    public int getVersionCount() {
        return versionCount;
    }

    @Override
    public int getPointCount() {
        return Math.round(((float) (endTime - startTime)) / spacing) + 1;
    }

    @Override
    public long getTimeAt(int point) {
        return startTime + point * spacing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, spacing, firstVersion, versionCount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesIndex) {
            RegularTimeSeriesIndex otherIndex = (RegularTimeSeriesIndex) obj;
            return startTime == otherIndex.startTime &&
                    endTime == otherIndex.endTime &&
                    spacing == otherIndex.spacing &&
                    firstVersion == otherIndex.firstVersion &&
                    versionCount == otherIndex.versionCount;
        }
        return false;
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        try {
            generator.writeFieldName("regularIndex");
            generator.writeStartObject();
            generator.writeNumberField("startTime", startTime);
            generator.writeNumberField("endTime", endTime);
            generator.writeNumberField("spacing", spacing);
            generator.writeNumberField("firstVersion", firstVersion);
            generator.writeNumberField("versionCount", versionCount);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "index(startTime=" + Instant.ofEpochMilli(startTime) + ", endTime=" + Instant.ofEpochMilli(endTime) +
                ", spacing=" + Duration.ofMillis(spacing) + ", firstVersion=" + firstVersion + ", versionCount=" + versionCount + ")";
    }
}
