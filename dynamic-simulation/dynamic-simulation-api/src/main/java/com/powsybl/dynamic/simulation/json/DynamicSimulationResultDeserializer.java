/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamic.simulation.DynamicSimulationResult;
import com.powsybl.dynamic.simulation.DynamicSimulationResultImpl;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationResultDeserializer extends StdDeserializer<DynamicSimulationResult> {

    DynamicSimulationResultDeserializer() {
        super(DynamicSimulationResult.class);
    }

    @Override
    public DynamicSimulationResult deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        Boolean isOK = null;
        Map<String, String> metrics = null;
        String log = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    break;

                case "isOK":
                    parser.nextToken();
                    isOK = parser.readValueAs(Boolean.class);
                    break;

                case "metrics":
                    parser.nextToken();
                    metrics = parser.readValueAs(HashMap.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new DynamicSimulationResultImpl(isOK, metrics, log);
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
