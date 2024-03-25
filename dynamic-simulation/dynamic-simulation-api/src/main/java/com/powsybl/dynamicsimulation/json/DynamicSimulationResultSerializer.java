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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.DynamicSimulationResult;
import com.powsybl.dynamicsimulation.TimelineEvent;
import com.powsybl.timeseries.DoubleTimeSeries;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DynamicSimulationResultSerializer extends StdSerializer<DynamicSimulationResult> {

    private static final String VERSION = "1.0";

    DynamicSimulationResultSerializer() {
        super(DynamicSimulationResult.class);
    }

    @Override
    public void serialize(DynamicSimulationResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", VERSION);
        jsonGenerator.writeStringField("status", result.getStatus().name());
        if (!result.getStatusText().isEmpty()) {
            jsonGenerator.writeStringField("error", result.getStatusText());
        }
        jsonGenerator.writeFieldName("curves");
        jsonGenerator.writeStartArray();
        for (Entry<String, DoubleTimeSeries> entry : result.getCurves().entrySet()) {
            entry.getValue().writeJson(jsonGenerator);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeFieldName("timeLine");
        jsonGenerator.writeStartArray();
        for (TimelineEvent event : result.getTimeLine()) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeNumberField("time", event.time());
            jsonGenerator.writeStringField("modelName", event.modelName());
            jsonGenerator.writeStringField("message", event.message());
            jsonGenerator.writeEndObject();
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.writeEndObject();
    }

    public static void write(DynamicSimulationResult result, Path jsonFile) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(DynamicSimulationResult.class, new DynamicSimulationResultSerializer());
            objectMapper.registerModule(module);
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, result);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
