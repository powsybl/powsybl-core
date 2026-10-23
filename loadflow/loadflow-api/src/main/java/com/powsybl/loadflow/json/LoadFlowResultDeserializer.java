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
import com.powsybl.loadflow.LoadFlowResultImpl;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class LoadFlowResultDeserializer extends StdDeserializer<LoadFlowResult> {

    private static final String CONTEXT_NAME = "LoadFlowResult";
    public static final String UNEXPECTED_FIELD = "Unexpected field: ";

    LoadFlowResultDeserializer() {
        super(LoadFlowResult.class);
    }

    public LoadFlowResult.ComponentResult deserializeComponentResult(JsonParser parser, String version) throws JacksonException {
        ComponentResultParsedData parsedData = parseComponentResult(parser, version);

        if (parsedData.getConnectedComponentNum() == null) {
            if (JsonUtil.compareVersions(version, "1.2") < 0) {
                parsedData.setConnectedComponentNum(0);
            } else {
                throw new IllegalStateException("Connected component number field not found.");
            }
        }

        if (parsedData.getSynchronousComponentNum() == null) {
            throw new IllegalStateException("Synchronous component number field not found.");
        }
        if (parsedData.getIterationCount() == null) {
            throw new IllegalStateException("Iteration count field not found.");
        }
        if (parsedData.getDistributedActivePower() == null) {
            if (JsonUtil.compareVersions(version, "1.3") < 0) {
                parsedData.setDistributedActivePower(Double.NaN);
            } else {
                throw new IllegalStateException("Distributed active power field not found.");
            }
        }
        if (JsonUtil.compareVersions(version, "1.4") < 0) {
            if (parsedData.getSlackBusId() == null) {
                throw new IllegalStateException("Slack bus id field not found.");
            }
            if (parsedData.getSlackBusActivePowerMismatch() == null) {
                throw new IllegalStateException("Slack bus active power mismatch field not found.");
            }
            parsedData.setReferenceBusId(parsedData.getSlackBusId());
            parsedData.setSlackBusResults(List.of(new LoadFlowResultImpl.SlackBusResultImpl(parsedData.getSlackBusId(), parsedData.getSlackBusActivePowerMismatch())));
        }

        return new LoadFlowResultImpl.ComponentResultImpl(
            parsedData.getConnectedComponentNum(), parsedData.getSynchronousComponentNum(),
            parsedData.getStatus(), parsedData.getStatusText(), parsedData.getMetrics(), parsedData.getIterationCount(),
            parsedData.getReferenceBusId(), parsedData.getSlackBusResults(), parsedData.getDistributedActivePower()
        );
    }

    public LoadFlowResult.SlackBusResult deserializeSlackBusResult(JsonParser parser) throws JacksonException {
        String id = null;
        Double activePowerMismatch = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "id" -> {
                    parser.nextToken();
                    id = parser.getValueAsString();
                }
                case "activePowerMismatch" -> {
                    parser.nextToken();
                    activePowerMismatch = parser.getValueAsDouble();
                }
                default -> throw new IllegalStateException(UNEXPECTED_FIELD + parser.currentName());
            }
        }

        if (id == null) {
            throw new IllegalStateException("Slack bus result: id field not found.");
        }
        if (activePowerMismatch == null) {
            throw new IllegalStateException("Slack bus result: active power mismatch field not found.");
        }

        return new LoadFlowResultImpl.SlackBusResultImpl(id, activePowerMismatch);
    }

    public void deserializeComponentResults(JsonParser parser, List<LoadFlowResult.ComponentResult> componentResults, String version) throws JacksonException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            componentResults.add(deserializeComponentResult(parser, version));
        }
    }

    public void deserializeSlackBusResults(JsonParser parser, List<LoadFlowResult.SlackBusResult> slackBusResults) throws JacksonException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            slackBusResults.add(deserializeSlackBusResult(parser));
        }
    }

    @Override
    public LoadFlowResult deserialize(JsonParser parser, DeserializationContext ctx) throws JacksonException {
        String version = null;
        Boolean ok = null;
        Map<String, String> metrics = null;
        String log = null;
        List<LoadFlowResult.ComponentResult> componentResults = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
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
                default -> throw new IllegalStateException(UNEXPECTED_FIELD + parser.currentName());
            }
        }

        if (ok == null) {
            throw new IllegalStateException("Ok field not found");
        }

        return new LoadFlowResultImpl(ok, metrics, log, componentResults);
    }

    public static LoadFlowResult read(InputStream is) {
        Objects.requireNonNull(is);
        JsonMapper jsonMapper = JsonUtil.createJsonMapperBuilder()
            .addModule(new LoadFlowResultJsonModule())
            .build();
        return jsonMapper.readValue(is, LoadFlowResult.class);
    }

    public static LoadFlowResult read(Path jsonFile) {
        Objects.requireNonNull(jsonFile);
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ComponentResultParsedData parseComponentResult(JsonParser parser, String version) {

        ComponentResultParsedData parsedData = new ComponentResultParsedData();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "connectedComponentNum" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.currentName(), version, "1.2");
                    parser.nextToken();
                    parsedData.setConnectedComponentNum(parser.getValueAsInt());
                }
                case "synchronousComponentNum" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.currentName(), version, "1.2");
                    parser.nextToken();
                    parsedData.setSynchronousComponentNum(parser.getValueAsInt());
                }
                case "componentNum" -> {
                    JsonUtil.assertLessThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.currentName(), version, "1.2");
                    parser.nextToken();
                    parsedData.setSynchronousComponentNum(parser.getValueAsInt());
                }
                case "status" -> {
                    parser.nextToken();
                    String tempStatus = parser.getValueAsString();
                    if (JsonUtil.compareVersions(version, "1.4") < 0 && "SOLVER_FAILED".equals(tempStatus)) {
                        // SOLVER_FAILED removed in v1.4, translated to FAILED, information kept in statusText which didn't exist before v1.4
                        parsedData.setStatusText(tempStatus);
                        tempStatus = LoadFlowResult.ComponentResult.Status.FAILED.name();
                    }
                    parsedData.setStatus(LoadFlowResult.ComponentResult.Status.valueOf(tempStatus));
                }
                case "statusText" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: statusText", version, "1.4");
                    parser.nextToken();
                    parsedData.setStatusText(parser.getValueAsString());
                }
                case "metrics" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: metrics", version, "1.4");
                    parser.nextToken();
                    parsedData.setMetrics(parser.readValueAs(HashMap.class));
                }
                case "iterationCount" -> {
                    parser.nextToken();
                    parsedData.setIterationCount(parser.getValueAsInt());
                }
                case "referenceBusId" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: referenceBusId", version, "1.4");
                    parser.nextToken();
                    parsedData.setReferenceBusId(parser.getValueAsString());
                }
                case "slackBusResults" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: slackBusResults", version, "1.4");
                    parser.nextToken();
                    deserializeSlackBusResults(parser, parsedData.getSlackBusResults());
                }
                case "slackBusId" -> {
                    JsonUtil.assertLessThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.currentName(), version, "1.4");
                    parser.nextToken();
                    parsedData.setSlackBusId(parser.getValueAsString());
                }
                case "slackBusActivePowerMismatch" -> {
                    JsonUtil.assertLessThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.currentName(), version, "1.4");
                    parser.nextToken();
                    parsedData.setSlackBusActivePowerMismatch(parser.getValueAsDouble());
                }
                case "distributedActivePower" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(LoadFlowResultDeserializer.class.getName(), parser.currentName(), version, "1.3");
                    parser.nextToken();
                    parsedData.setDistributedActivePower(parser.getValueAsDouble());
                }
                default -> throw new IllegalStateException(UNEXPECTED_FIELD + parser.currentName());
            }
        }
        return parsedData;
    }

    private static final class ComponentResultParsedData {

        Integer connectedComponentNum = null;
        Integer synchronousComponentNum = null;
        LoadFlowResult.ComponentResult.Status status = null;
        String statusText = null;
        Map<String, String> metrics = Collections.emptyMap();
        Integer iterationCount = null;
        List<LoadFlowResult.SlackBusResult> slackBusResults = new ArrayList<>();
        String referenceBusId = null;
        String slackBusId = null;
        Double slackBusActivePowerMismatch = null;
        Double distributedActivePower = null;

        private ComponentResultParsedData() {
        }

        public Integer getConnectedComponentNum() {
            return connectedComponentNum;
        }

        public void setConnectedComponentNum(Integer connectedComponentNum) {
            this.connectedComponentNum = connectedComponentNum;
        }

        public Integer getSynchronousComponentNum() {
            return synchronousComponentNum;
        }

        public void setSynchronousComponentNum(Integer synchronousComponentNum) {
            this.synchronousComponentNum = synchronousComponentNum;
        }

        public LoadFlowResult.ComponentResult.Status getStatus() {
            return status;
        }

        public void setStatus(LoadFlowResult.ComponentResult.Status status) {
            this.status = status;
        }

        public String getStatusText() {
            return statusText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }

        public Map<String, String> getMetrics() {
            return metrics;
        }

        public void setMetrics(Map<String, String> metrics) {
            this.metrics = metrics;
        }

        public Integer getIterationCount() {
            return iterationCount;
        }

        public void setIterationCount(Integer iterationCount) {
            this.iterationCount = iterationCount;
        }

        public List<LoadFlowResult.SlackBusResult> getSlackBusResults() {
            return slackBusResults;
        }

        public void setSlackBusResults(List<LoadFlowResult.SlackBusResult> slackBusResults) {
            this.slackBusResults = slackBusResults;
        }

        public String getReferenceBusId() {
            return referenceBusId;
        }

        public void setReferenceBusId(String referenceBusId) {
            this.referenceBusId = referenceBusId;
        }

        public String getSlackBusId() {
            return slackBusId;
        }

        public void setSlackBusId(String slackBusId) {
            this.slackBusId = slackBusId;
        }

        public Double getSlackBusActivePowerMismatch() {
            return slackBusActivePowerMismatch;
        }

        public void setSlackBusActivePowerMismatch(Double slackBusActivePowerMismatch) {
            this.slackBusActivePowerMismatch = slackBusActivePowerMismatch;
        }

        public Double getDistributedActivePower() {
            return distributedActivePower;
        }

        public void setDistributedActivePower(Double distributedActivePower) {
            this.distributedActivePower = distributedActivePower;
        }

    }
}
