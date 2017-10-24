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
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesMetadata {

    private final String name;

    private final TimeSeriesDataType dataType;

    private final Map<String, String> tags;

    private final TimeSeriesIndex index;

    public TimeSeriesMetadata(String name, TimeSeriesDataType dataType, TimeSeriesIndex index) {
        this(name, dataType, ImmutableMap.of(), index);
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
        try {
            generator.writeFieldName("metadata");
            generator.writeStartObject();
            generator.writeStringField("name", name);
            generator.writeStringField("dataType", dataType.name());
            generator.writeFieldName("tags");
            generator.writeStartArray();
            for (Map.Entry<String, String> e : tags.entrySet()) {
                generator.writeStartObject();
                generator.writeStringField(e.getKey(), e.getValue());
                generator.writeEndObject();
            }
            generator.writeEndArray();
            index.writeJson(generator);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static TimeSeriesMetadata parseJson(JsonParser parser) {
        try {
            String name = null;
            TimeSeriesDataType dataType = null;
            Map<String, String> tags = new HashMap<>();
            TimeSeriesIndex index = null;
            boolean insideTags = false;
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                switch (token) {
                    case FIELD_NAME:
                        String fieldName = parser.getCurrentName();
                        switch (fieldName) {
                            case "metadata":
                                break;
                            case "name":
                                name = parser.nextTextValue();
                                break;
                            case "dataType":
                                dataType = TimeSeriesDataType.valueOf(parser.nextTextValue());
                                break;
                            case "tags":
                                insideTags = true;
                                break;
                            case "regularIndex":
                                index = RegularTimeSeriesIndex.parseJson(parser);
                                break;
                            default:
                                if (insideTags) {
                                    tags.put(fieldName, parser.nextTextValue());
                                } else {
                                    throw new IllegalStateException("Unexpected field name " + fieldName);
                                }
                        }
                        break;
                    case END_ARRAY:
                        if (insideTags) {
                            insideTags = false;
                        }
                        break;
                    case END_OBJECT:
                        if (!insideTags) {
                            if (name == null || dataType == null || index == null) {
                                throw new IllegalStateException("Incomplete time series metadata json");
                            } else {
                                return new TimeSeriesMetadata(name, dataType, tags, index);
                            }
                        }
                        break;
                }
            }
            throw new IllegalStateException("should not happen");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType, tags, index);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeSeriesMetadata) {
            TimeSeriesMetadata other = (TimeSeriesMetadata) obj;
            return name.equals(other.name) &&
                    dataType == other.dataType &&
                    tags.equals(other.tags) &&
                    index.equals(other.index);
        }
        return false;
    }
}
