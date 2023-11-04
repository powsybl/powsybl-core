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
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 */
public class LoadFlowResultDeserializer extends StdDeserializer<LoadFlowResult> {

    private static final String CONTEXT_NAME = "LoadFlowResult";
    public static final String UNEXPECTED_FIELD = "Unexpected field: ";

    LoadFlowResultDeserializer() {
        super(LoadFlowResult.class);
    }

    public LoadFlowResult.ComponentResult deserializeComponentResult(JsonParser parser, String version) throws IOException {
        Integer connectedComponentNum = null;
        Integer synchronousComponentNum = null;
        LoadFlowResult.ComponentResult.Status status = null;
        Map<String, String> metrics = Collections.emptyMap();
        Integer iterationCount = null;
        List<LoadFlowResult.SlackResult> slackResults = new ArrayList<>();
        String referenceBusId = null;
        String slackBusId = null;
        Double slackBusActivePowerMismatch = null;
        Double distributedActivePower = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "connectedComponentNum" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    connectedComponentNum = parser.getValueAsInt();
                }
                case "synchronousComponentNum" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    synchronousComponentNum = parser.getValueAsInt();
                }
                case "componentNum" -> {
                    JsonUtil.assertLessThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    synchronousComponentNum = parser.getValueAsInt();
                }
                case "status" -> {
                    parser.nextToken();
                    status = LoadFlowResult.ComponentResult.Status.valueOf(parser.getValueAsString());
                }
                case "metrics" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: metrics", version, "1.4");
                    parser.nextToken();
                    metrics = parser.readValueAs(HashMap.class);
                }
                case "iterationCount" -> {
                    parser.nextToken();
                    iterationCount = parser.getValueAsInt();
                }
                case "referenceBusId" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: referenceBusId", version, "1.4");
                    parser.nextToken();
                    referenceBusId = parser.getValueAsString();
                }
                case "slackResults" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: slackResults", version, "1.4");
                    parser.nextToken();
                    deserializeSlackResults(parser, slackResults);
                }
                case "slackBusId" -> {
                    JsonUtil.assertLessThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.getCurrentName(), version, "1.4");
                    parser.nextToken();
                    slackBusId = parser.getValueAsString();
                }
                case "slackBusActivePowerMismatch" -> {
                    JsonUtil.assertLessThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.getCurrentName(), version, "1.4");
                    parser.nextToken();
                    slackBusActivePowerMismatch = parser.getValueAsDouble();
                }
                case "distributedActivePower" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.getCurrentName(), version, "1.3");
                    parser.nextToken();
                    distributedActivePower = parser.getValueAsDouble();
                }
                default -> throw new IllegalStateException(UNEXPECTED_FIELD + parser.getCurrentName());
            }
        }

        if (connectedComponentNum == null) {
            if (version.compareTo("1.2") < 0) {
                connectedComponentNum = 0;
            } else {
                throw new IllegalStateException("Connected component number field not found.");
            }
        }

        if (synchronousComponentNum == null) {
            throw new IllegalStateException("Synchronous component number field not found.");
        }
        if (iterationCount == null) {
            throw new IllegalStateException("Iteration count field not found.");
        }
        if (distributedActivePower == null) {
            if (version.compareTo("1.3") < 0) {
                distributedActivePower = Double.NaN;
            } else {
                throw new IllegalStateException("Distributed active power field not found.");
            }
        }
        if (version.compareTo("1.4") < 0) {
            if (slackBusId == null) {
                throw new IllegalStateException("Slack bus id field not found.");
            }
            if (slackBusActivePowerMismatch == null) {
                throw new IllegalStateException("Slack bus active power mismatch field not found.");
            }
            referenceBusId = slackBusId;
            slackResults = List.of(new LoadFlowResultImpl.SlackResultImpl(slackBusId, slackBusActivePowerMismatch));
        }

        return new LoadFlowResultImpl.ComponentResultImpl(connectedComponentNum, synchronousComponentNum, status, metrics, iterationCount, referenceBusId, slackResults, distributedActivePower);
    }

    public LoadFlowResult.SlackResult deserializeSlackResult(JsonParser parser) throws IOException {
        String busId = null;
        Double busActivePowerMismatch = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "busId" -> {
                    parser.nextToken();
                    busId = parser.getValueAsString();
                }
                case "busActivePowerMismatch" -> {
                    parser.nextToken();
                    busActivePowerMismatch = parser.getValueAsDouble();
                }
                default -> throw new IllegalStateException(UNEXPECTED_FIELD + parser.getCurrentName());
            }
        }

        if (busId == null) {
            throw new IllegalStateException("Slack bus id field not found.");
        }
        if (busActivePowerMismatch == null) {
            throw new IllegalStateException("Slack bus active power mismatch field not found.");
        }

        return new LoadFlowResultImpl.SlackResultImpl(busId, busActivePowerMismatch);
    }

    public void deserializeComponentResults(JsonParser parser, List<LoadFlowResult.ComponentResult> componentResults, String version) throws IOException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            componentResults.add(deserializeComponentResult(parser, version));
        }
    }

    public void deserializeSlackResults(JsonParser parser, List<LoadFlowResult.SlackResult> slackResults) throws IOException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            slackResults.add(deserializeSlackResult(parser));
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
                case "version" -> {
                    parser.nextToken();
                    version = parser.getValueAsString();
                }
                case "isOK" -> {
                    parser.nextToken();
                    ok = parser.readValueAs(Boolean.class);
                }
                case "metrics" -> {
                    parser.nextToken();
                    metrics = parser.readValueAs(HashMap.class);
                }
                case "componentResults" -> {
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: componentResults", version, "1.0");
                    parser.nextToken();
                    deserializeComponentResults(parser, componentResults, version);
                }
                default -> throw new IllegalStateException(UNEXPECTED_FIELD + parser.getCurrentName());
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
