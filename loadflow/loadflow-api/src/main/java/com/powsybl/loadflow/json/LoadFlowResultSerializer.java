/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.std.StdSerializer;

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
    public void serialize(LoadFlowResult result, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", VERSION);
        jsonGenerator.writeBooleanProperty("isOK", result.isOk());
        serializationContext.defaultSerializeProperty("metrics", result.getMetrics(), jsonGenerator);
        List<LoadFlowResult.ComponentResult> componentResults = result.getComponentResults();
        if (!componentResults.isEmpty()) {
            jsonGenerator.writeName("componentResults");
            jsonGenerator.writeStartArray();
            for (LoadFlowResult.ComponentResult componentResult : componentResults) {
                serialize(componentResult, jsonGenerator, serializationContext);
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeEndObject();
    }

    public void serialize(LoadFlowResult.ComponentResult componentResult, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberProperty("connectedComponentNum", componentResult.getConnectedComponentNum());
        jsonGenerator.writeNumberProperty("synchronousComponentNum", componentResult.getSynchronousComponentNum());
        jsonGenerator.writeStringProperty("status", componentResult.getStatus().name());
        jsonGenerator.writeStringProperty("statusText", componentResult.getStatusText());
        serializationContext.defaultSerializeProperty("metrics", componentResult.getMetrics(), jsonGenerator);
        jsonGenerator.writeNumberProperty("iterationCount", componentResult.getIterationCount());
        jsonGenerator.writeStringProperty("referenceBusId", componentResult.getReferenceBusId());
        List<LoadFlowResult.SlackBusResult> slackBusResults = componentResult.getSlackBusResults();
        if (!slackBusResults.isEmpty()) {
            jsonGenerator.writeName("slackBusResults");
            jsonGenerator.writeStartArray();
            for (LoadFlowResult.SlackBusResult slackBusResult : slackBusResults) {
                serialize(slackBusResult, jsonGenerator);
            }
            jsonGenerator.writeEndArray();
        }
        jsonGenerator.writeNumberProperty("distributedActivePower", componentResult.getDistributedActivePower());
        jsonGenerator.writeEndObject();
    }

    public void serialize(LoadFlowResult.SlackBusResult slackBusResult, JsonGenerator jsonGenerator) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("id", slackBusResult.getId());
        jsonGenerator.writeNumberProperty("activePowerMismatch", slackBusResult.getActivePowerMismatch());
        jsonGenerator.writeEndObject();
    }

    public static void write(LoadFlowResult result, Path jsonFile) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            JsonMapper jsonMapper = JsonUtil.createJsonMapperBuilder()
                .addModule(new LoadFlowResultJsonModule())
                .build();
            ObjectWriter writer = jsonMapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, result);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
