/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.dynamicsimulation.DynamicSimulationResultImpl;
import com.powsybl.dynamicsimulation.TimelineEvent;
import com.powsybl.timeseries.DoubleTimeSeries;
import com.powsybl.timeseries.json.DoubleTimeSeriesJsonDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DynamicSimulationResultDeserializer extends StdDeserializer<DynamicSimulationResult> {

    private final DoubleTimeSeriesJsonDeserializer doubleTimeSeriesJsonDeserializer = new DoubleTimeSeriesJsonDeserializer();

    DynamicSimulationResultDeserializer() {
        super(DynamicSimulationResult.class);
    }

    @Override
    public DynamicSimulationResult deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        DynamicSimulationResult.Status status = null;
        String error = "";
        Map<String, DoubleTimeSeries> curves = new HashMap<>();
        List<TimelineEvent> timeLine = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version" -> parser.nextToken(); // skip
                case "status" -> {
                    parser.nextToken();
                    status = parser.readValueAs(DynamicSimulationResult.Status.class);
                }
                case "error" -> {
                    parser.nextToken();
                    error = parser.readValueAs(String.class);
                }
                case "curves" -> {
                    parser.nextToken();
                    deserializeCurves(parser, curves);
                }
                case "timeLine" -> {
                    parser.nextToken();
                    deserializeTimeline(parser, timeLine);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new DynamicSimulationResultImpl(status, error, curves, timeLine);
    }

    private void deserializeCurves(JsonParser parser, Map<String, DoubleTimeSeries> curves) throws IOException {
        DoubleTimeSeries curve;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            curve = doubleTimeSeriesJsonDeserializer.deserialize(parser, null);
            if (curve != null) {
                curves.put(curve.getMetadata().getName(), curve);
            }
        }
    }

    private void deserializeTimeline(JsonParser parser, List<TimelineEvent> timelineEvents) throws IOException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            timelineEvents.add(deserializeTimelineEvent(parser));
        }
    }

    private TimelineEvent deserializeTimelineEvent(JsonParser parser) throws IOException {
        double time = 0;
        String modelName = null;
        String message = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "time" -> time = parser.getValueAsDouble();
                case "modelName" -> modelName = parser.getValueAsString();
                case "message" -> message = parser.getValueAsString();
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new TimelineEvent(time, modelName, message);
    }

    public static DynamicSimulationResult read(InputStream is) throws IOException {
        Objects.requireNonNull(is);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(DynamicSimulationResult.class, new DynamicSimulationResultDeserializer());
        objectMapper.registerModule(module);

        return objectMapper.readValue(is, DynamicSimulationResult.class);
    }

    public static DynamicSimulationResult read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
