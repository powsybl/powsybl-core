/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class LoadFlowResultSerializer extends StdSerializer<LoadFlowResult> {

    private static final String VERSION = "1.4";

    LoadFlowResultSerializer() {
        super(LoadFlowResult.class);
    }

    @Override
    public void serialize(LoadFlowResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", VERSION);
        jsonGenerator.writeBooleanField("isOK", !result.isFailed());
        serializerProvider.defaultSerializeField("metrics", result.getMetrics(), jsonGenerator);
        List<LoadFlowResult.ComponentResult> componentResults = result.getComponentResults();
        if (!componentResults.isEmpty()) {
            jsonGenerator.writeFieldName("componentResults");
            jsonGenerator.writeStartArray();
            for (LoadFlowResult.ComponentResult componentResult : componentResults) {
                serialize(componentResult, jsonGenerator, serializerProvider);
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndObject();
    }

    public void serialize(LoadFlowResult.ComponentResult componentResult, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("connectedComponentNum", componentResult.getConnectedComponentNum());
        jsonGenerator.writeNumberField("synchronousComponentNum", componentResult.getSynchronousComponentNum());
        jsonGenerator.writeStringField("status", componentResult.getStatus().name());
        jsonGenerator.writeStringField("statusText", componentResult.getStatusText());
        serializerProvider.defaultSerializeField("metrics", componentResult.getMetrics(), jsonGenerator);
        jsonGenerator.writeNumberField("iterationCount", componentResult.getIterationCount());
        jsonGenerator.writeStringField("referenceBusId", componentResult.getReferenceBusId());
        List<LoadFlowResult.SlackBusResult> slackBusResults = componentResult.getSlackBusResults();
        if (!slackBusResults.isEmpty()) {
            jsonGenerator.writeFieldName("slackBusResults");
            jsonGenerator.writeStartArray();
            for (LoadFlowResult.SlackBusResult slackBusResult : slackBusResults) {
                serialize(slackBusResult, jsonGenerator);
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeNumberField("distributedActivePower", componentResult.getDistributedActivePower());
        jsonGenerator.writeEndObject();
    }

    public void serialize(LoadFlowResult.SlackBusResult slackBusResult, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", slackBusResult.getId());
        jsonGenerator.writeNumberField("activePowerMismatch", slackBusResult.getActivePowerMismatch());
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
