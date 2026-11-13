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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class TimeSeriesMetadata {

    private final String name;

    private final TimeSeriesDataType dataType;

    private final Map<String, String> tags;

    private final TimeSeriesIndex index;

    public TimeSeriesMetadata(String name, TimeSeriesDataType dataType, TimeSeriesIndex index) {
        // The order of the Map.of() elements is not constant/predictable so we have to build an unmodifiableMap LinkedHashMap ourselves
        this(name, dataType, Collections.unmodifiableMap(new LinkedHashMap<>()), index);
    }

    public TimeSeriesMetadata(String name, TimeSeriesDataType dataType, Map<String, String> tags, TimeSeriesIndex index) {
        this.name = Objects.requireNonNull(name);
        this.dataType = Objects.requireNonNull(dataType);
        this.tags = Collections.unmodifiableMap(Objects.requireNonNull(tags));
        this.index = Objects.requireNonNull(index);
    }

    public String getName() {
        return name;
    }

    public TimeSeriesDataType getDataType() {
        return dataType;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public TimeSeriesIndex getIndex() {
        return index;
    }

    public void writeJson(JsonGenerator generator) {
        generator.writeStartObject();

        generator.writeStringProperty("name", name);
        generator.writeStringProperty("dataType", dataType.name());

        generator.writeName("tags");
        generator.writeStartArray();
        for (Map.Entry<String, String> e : tags.entrySet()) {
            generator.writeStartObject();
            generator.writeStringProperty(e.getKey(), e.getValue());
            generator.writeEndObject();
        }
        generator.writeEndArray();

        generator.writeName(index.getType());
        index.writeJson(generator);

        generator.writeEndObject();
    }

    private static final class JsonParsingContext {
        private String name;
        private TimeSeriesDataType dataType;
        private final Map<String, String> tags = new LinkedHashMap<>();
        private TimeSeriesIndex index;
        private boolean insideTags = false;

        public boolean isComplete() {
            return name != null && dataType != null && index != null;
        }
    }

    static void parsePropertyName(JsonParser parser, JsonParsingContext context, TimeSeries.TimeFormat timeFormat) {
        String fieldName = parser.currentName();
        if (context.insideTags) {
            context.tags.put(fieldName, parser.nextStringValue());
        } else {
            switch (fieldName) {
                case "metadata" -> {
                    // Do nothing
                }
                case "name" -> context.name = parser.nextStringValue();
                case "dataType" -> context.dataType = TimeSeriesDataType.valueOf(parser.nextStringValue());
                case "tags" -> context.insideTags = true;
                case RegularTimeSeriesIndex.TYPE -> context.index = RegularTimeSeriesIndex.parseJson(parser);
                case IrregularTimeSeriesIndex.TYPE -> context.index = IrregularTimeSeriesIndex.parseJson(parser,
                    timeFormat == TimeSeries.TimeFormat.MILLIS ? TimeSeriesIndex.ExportFormat.MILLISECONDS : TimeSeriesIndex.ExportFormat.NANOSECONDS);
                case InfiniteTimeSeriesIndex.TYPE -> context.index = InfiniteTimeSeriesIndex.parseJson(parser);
                default -> throw new IllegalStateException("Unexpected field name " + fieldName);
            }
        }
    }

    public static TimeSeriesMetadata parseJson(JsonParser parser) {
        return parseJson(parser, TimeSeries.TimeFormat.MILLIS);
    }

    public static TimeSeriesMetadata parseJson(JsonParser parser, TimeSeries.TimeFormat timeFormat) {
        JsonToken token;
        JsonParsingContext context = new JsonParsingContext();
        while ((token = parser.nextToken()) != null) {
            switch (token) {
                case PROPERTY_NAME -> parsePropertyName(parser, context, timeFormat);
                case END_ARRAY -> {
                    if (context.insideTags) {
                        context.insideTags = false;
                    }
                }
                case END_OBJECT -> {
                    if (!context.insideTags) {
                        if (context.isComplete()) {
                            return new TimeSeriesMetadata(context.name, context.dataType, context.tags, context.index);
                        } else {
                            throw new IllegalStateException("Incomplete time series metadata json");
                        }
                    }
                }
                default -> {
                    // Do nothing
                }
            }
        }
        throw new IllegalStateException("should not happen");
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType, tags, index);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesMetadata other) {
            return name.equals(other.name) &&
                    dataType == other.dataType &&
                    tags.equals(other.tags) &&
                    index.equals(other.index);
        }
        return false;
    }

    @Override
    public String toString() {
        return "TimeSeriesMetadata(name=" + name + ", dataType=" + dataType + ", tags=" + tags + ", index=" + index + ")";
    }
}
