/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class LoadFlowResultDeserializer extends StdDeserializer<LoadFlowResult> {

    private static final String CONTEXT_NAME = "LoadFlowResult";

    LoadFlowResultDeserializer() {
        super(LoadFlowResult.class);
    }

    public LoadFlowResult.ComponentResult deserializeComponentResult(JsonParser parser) throws IOException {
        Integer componentNum = null;
        int synchronousComponentNum = 0;
        LoadFlowResult.ComponentResult.Status status = null;
        Integer iterationCount = null;
        String slackBusId = null;
        Double slackBusActivePowerMismatch = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "componentNum":
                    parser.nextToken();
                    componentNum = parser.getValueAsInt();
                    break;

                case "synchronousComponentNum":
                    parser.nextToken();
                    synchronousComponentNum = parser.getValueAsInt();
                    break;

                case "status":
                    parser.nextToken();
                    status = LoadFlowResult.ComponentResult.Status.valueOf(parser.getValueAsString());
                    break;

                case "iterationCount":
                    parser.nextToken();
                    iterationCount = parser.getValueAsInt();
                    break;

                case "slackBusId":
                    parser.nextToken();
                    slackBusId = parser.getValueAsString();
                    break;

                case "slackBusActivePowerMismatch":
                    parser.nextToken();
                    slackBusActivePowerMismatch = parser.getValueAsDouble();
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        if (componentNum == null) {
            throw new IllegalStateException("Component number field not found");
        }
        if (iterationCount == null) {
            throw new IllegalStateException("Iteration count field not found");
        }
        if (slackBusActivePowerMismatch == null) {
            throw new IllegalStateException("Slack bus active power mismatch field not found");
        }

        return new LoadFlowResultImpl.ComponentResultImpl(componentNum, synchronousComponentNum, status, iterationCount, slackBusId, slackBusActivePowerMismatch);
    }

    public void deserializeComponentResults(JsonParser parser, List<LoadFlowResult.ComponentResult> componentResults) throws IOException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            componentResults.add(deserializeComponentResult(parser));
        }
    }

    @Override
    public LoadFlowResult deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String version = null;
        Boolean ok = null;
        Map<String, String> metrics = null;
        String log = null;
        List<LoadFlowResult.ComponentResult> componentResults = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;

                case "isOK":
                    parser.nextToken();
                    ok = parser.readValueAs(Boolean.class);
                    break;

                case "metrics":
                    parser.nextToken();
                    metrics = parser.readValueAs(HashMap.class);
                    break;

                case "componentResults":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: componentResults", version, "1.0");
                    parser.nextToken();
                    deserializeComponentResults(parser, componentResults);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        if (ok == null) {
            throw new IllegalStateException("Ok field not found");
        }

        return new LoadFlowResultImpl(ok, metrics, log, componentResults);
    }

    public static LoadFlowResult read(InputStream is) throws IOException {
        Objects.requireNonNull(is);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper();
        objectMapper.registerModule(new LoadFlowResultJsonModule());
        return objectMapper.readValue(is, LoadFlowResult.class);
    }

    public static LoadFlowResult read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
