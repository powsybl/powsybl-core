/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.dynamicsimulation.DynamicSimulationResultImpl;
import com.powsybl.dynamicsimulation.TimelineEvent;
import com.powsybl.timeseries.DoubleTimeSeries;
import com.powsybl.timeseries.json.DoubleTimeSeriesJsonDeserializer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

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
    public DynamicSimulationResult deserialize(JsonParser parser, DeserializationContext ctx) throws JacksonException {
        DynamicSimulationResult.Status status = null;
        String error = "";
        Map<String, DoubleTimeSeries> curves = new LinkedHashMap<>();
        Map<String, Double> fsv = new LinkedHashMap<>();
        List<TimelineEvent> timeLine = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
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
                case "finalStateValues" -> {
                    parser.nextToken();
                    deserializeFinalStateValues(parser, fsv);
                }
                case "timeLine" -> {
                    parser.nextToken();
                    deserializeTimeline(parser, timeLine);
                }
                default -> throw getUnexpectedFieldException(parser);
            }
        }

        return new DynamicSimulationResultImpl(status, error, curves, fsv, timeLine);
    }

    private void deserializeCurves(JsonParser parser, Map<String, DoubleTimeSeries> curves) throws JacksonException {
        DoubleTimeSeries curve;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            curve = doubleTimeSeriesJsonDeserializer.deserialize(parser, null);
            if (curve != null) {
                curves.put(curve.getMetadata().getName(), curve);
            }
        }
    }

    private void deserializeFinalStateValues(JsonParser parser, Map<String, Double> fsvs) throws JacksonException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            String name = null;
            double value = 0;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                switch (parser.currentName()) {
                    case "name" -> name = parser.getValueAsString();
                    case "value" -> value = parser.getValueAsDouble();
                    default -> throw getUnexpectedFieldException(parser);
                }
            }
            fsvs.put(name, value);
        }
    }

    private void deserializeTimeline(JsonParser parser, List<TimelineEvent> timelineEvents) throws JacksonException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            timelineEvents.add(deserializeTimelineEvent(parser));
        }
    }

    private TimelineEvent deserializeTimelineEvent(JsonParser parser) throws JacksonException {
        double time = 0;
        String modelName = null;
        String message = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "time" -> time = parser.getValueAsDouble();
                case "modelName" -> modelName = parser.getValueAsString();
                case "message" -> message = parser.getValueAsString();
                default -> throw getUnexpectedFieldException(parser);
            }
        }
        return new TimelineEvent(time, modelName, message);
    }

    public static DynamicSimulationResult read(InputStream is) throws JacksonException {
        Objects.requireNonNull(is);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(DynamicSimulationResult.class, new DynamicSimulationResultDeserializer());
        JsonMapper jsonMapper = JsonUtil.createJsonMapperBuilder()
            .addModule(module)
            .build();

        return jsonMapper.readValue(is, DynamicSimulationResult.class);
    }

    public static DynamicSimulationResult read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static IllegalStateException getUnexpectedFieldException(JsonParser parser) throws JacksonException {
        return new IllegalStateException("Unexpected field: " + parser.currentName());
    }
}
