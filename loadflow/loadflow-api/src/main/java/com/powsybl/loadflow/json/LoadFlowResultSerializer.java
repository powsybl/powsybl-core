/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class LoadFlowResultSerializer extends StdSerializer<LoadFlowResult> {

    private static final String VERSION = "1.1";

    LoadFlowResultSerializer() {
        super(LoadFlowResult.class);
    }

    @Override
    public void serialize(LoadFlowResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", VERSION);
        jsonGenerator.writeBooleanField("isOK", result.isOk());
        jsonGenerator.writeObjectField("metrics", result.getMetrics());
        List<LoadFlowResult.ComponentResult> componentResults = result.getComponentResults();
        if (!componentResults.isEmpty()) {
            jsonGenerator.writeFieldName("componentResults");
            jsonGenerator.writeStartArray();
            for (LoadFlowResult.ComponentResult componentResult : componentResults) {
                serialize(componentResult, jsonGenerator);
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndObject();
    }

    public void serialize(LoadFlowResult.ComponentResult componentResult, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("componentNum", componentResult.getComponentNum());
        jsonGenerator.writeStringField("status", componentResult.getStatus().name());
        jsonGenerator.writeNumberField("iterationCount", componentResult.getIterationCount());
        jsonGenerator.writeStringField("slackBusId", componentResult.getSlackBusId());
        jsonGenerator.writeNumberField("slackBusActivePowerMismatch", componentResult.getSlackBusActivePowerMismatch());
        jsonGenerator.writeEndObject();
    }

    public static void write(LoadFlowResult result, Path jsonFile) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            objectMapper.registerModule(new LoadFlowResultJsonModule());
            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, result);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
