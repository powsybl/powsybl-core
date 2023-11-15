/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.dynamicsimulation.DynamicSimulationResultImpl;
import com.powsybl.timeseries.StringTimeSeries;
import com.powsybl.timeseries.TimeSeries;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DynamicSimulationResultDeserializer extends StdDeserializer<DynamicSimulationResult> {

    DynamicSimulationResultDeserializer() {
        super(DynamicSimulationResult.class);
    }

    @Override
    public DynamicSimulationResult deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        DynamicSimulationResult.Status status = null;
        String error = "";
        Map<String, TimeSeries> curves = new HashMap<>();
        StringTimeSeries timeLine = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    break;

                case "status":
                    parser.nextToken();
                    status = parser.readValueAs(DynamicSimulationResult.Status.class);
                    break;

                case "error":
                    parser.nextToken();
                    error = parser.readValueAs(String.class);
                    break;

                case "curves":
                    parser.nextToken();
                    deserializeCurves(parser, curves);
                    break;

                case "timeLine":
                    parser.nextToken();
                    timeLine = (StringTimeSeries) deserializeTimeSeries(parser);
                    if (timeLine == null) {
                        timeLine = DynamicSimulationResult.emptyTimeLine();
                    }
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new DynamicSimulationResultImpl(status, error, curves, timeLine);
    }

    private void deserializeCurves(JsonParser parser, Map<String, TimeSeries> curves) throws IOException {
        TimeSeries curve;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            curve = deserializeTimeSeries(parser);
            if (curve != null) {
                curves.put(curve.getMetadata().getName(), curve);
            }
        }
    }

    private TimeSeries deserializeTimeSeries(JsonParser parser) {
        List<TimeSeries> timeseries = TimeSeries.parseJson(parser, true);
        if (!timeseries.isEmpty()) {
            return timeseries.get(0);
        }
        return null;
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
