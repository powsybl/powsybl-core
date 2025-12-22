/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Objects;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.dynamicsimulation.TimelineEvent;
import com.powsybl.timeseries.DoubleTimeSeries;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DynamicSimulationResultSerializer extends StdSerializer<DynamicSimulationResult> {

    private static final String VERSION = "1.0";

    DynamicSimulationResultSerializer() {
        super(DynamicSimulationResult.class);
    }

    @Override
    public void serialize(DynamicSimulationResult result, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", VERSION);
        jsonGenerator.writeStringProperty("status", result.getStatus().name());
        if (!result.getStatusText().isEmpty()) {
            jsonGenerator.writeStringProperty("error", result.getStatusText());
        }
        jsonGenerator.writeName("curves");
        jsonGenerator.writeStartArray();
        for (Entry<String, DoubleTimeSeries> entry : result.getCurves().entrySet()) {
            entry.getValue().writeJson(jsonGenerator);
        }
        jsonGenerator.writeEndArray();
        // fsv
        jsonGenerator.writeName("finalStateValues");
        jsonGenerator.writeStartArray();
        for (Entry<String, Double> fsv : result.getFinalStateValues().entrySet()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringProperty("name", fsv.getKey());
            jsonGenerator.writeNumberProperty("value", fsv.getValue());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        // timeline
        jsonGenerator.writeName("timeLine");
        jsonGenerator.writeStartArray();
        for (TimelineEvent event : result.getTimeLine()) {
            writeTimeline(event, jsonGenerator);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }

    private void writeTimeline(TimelineEvent event, JsonGenerator jsonGenerator) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberProperty("time", event.time());
        jsonGenerator.writeStringProperty("modelName", event.modelName());
        jsonGenerator.writeStringProperty("message", event.message());
        jsonGenerator.writeEndObject();
    }

    public static void write(DynamicSimulationResult result, Path jsonFile) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            SimpleModule module = new SimpleModule();
            module.addSerializer(DynamicSimulationResult.class, new DynamicSimulationResultSerializer());
            JsonMapper jsonMapper = JsonUtil.createJsonMapperBuilder()
                .addModule(module)
                .build();
            ObjectWriter writer = jsonMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, result);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
