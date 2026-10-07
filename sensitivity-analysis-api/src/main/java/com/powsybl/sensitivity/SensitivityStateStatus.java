/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.powsybl.sensitivity.SensitivityAnalysisResult.CONTEXT_NAME;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityStateStatus {

    public record ComponentStatus(int connectedComponentNum, int synchronousComponentNum, LoadFlowResult.ComponentResult.Status status, String statusText) { }

    static final String COMPONENT_STATUSES = "componentStatuses";
    static final String LOAD_FLOW_STATUS = "loadFlowStatus";
    static final String LOAD_FLOW_STATUS_TEXT = "loadFlowStatusText";
    static final String NUM_CC = "numCC";
    static final String NUM_SC = "numSC";

    private final SensitivityState state;

    /**
     * Per-component load flow status (one entry per (connectedComponentNum, synchronousComponentNum) for which a load flow has run).
     */
    private final List<ComponentStatus> componentsLoadFlowStatusList;

    public SensitivityState getState() {
        return state;
    }

    /**
     * @deprecated Use {@link SensitivityStateStatus#getComponentsLoadFlowStatusList()} instead.
     */
    @Deprecated(since = "7.4.0")
    public SensitivityAnalysisResult.Status getStatus() {
        if (!getComponentsLoadFlowStatusList().isEmpty()) {
            switch (getComponentsLoadFlowStatusList().getFirst().status()) {
                case CONVERGED -> {
                    return SensitivityAnalysisResult.Status.SUCCESS;
                }
                case NO_CALCULATION -> {
                    return SensitivityAnalysisResult.Status.NO_IMPACT;
                }
                default -> {
                    return SensitivityAnalysisResult.Status.FAILURE;
                }
            }
        } else {
            return SensitivityAnalysisResult.Status.FAILURE;
        }
    }

    public List<ComponentStatus> getComponentsLoadFlowStatusList() {
        return componentsLoadFlowStatusList;
    }

    public SensitivityStateStatus(SensitivityState state, List<ComponentStatus> statusList) {
        this.state = Objects.requireNonNull(state);
        this.componentsLoadFlowStatusList = new ArrayList<>(statusList);
    }

    /**
     * @deprecated Use {@link SensitivityStateStatus} instead.
     */
    @Deprecated(since = "7.4.0")
    public SensitivityStateStatus(SensitivityState state, SensitivityAnalysisResult.Status status) {
        this(state, List.of(new ComponentStatus(
                -1, -1, toLoadFlowStatus(status), "")));
    }

    private static LoadFlowResult.ComponentResult.Status toLoadFlowStatus(SensitivityAnalysisResult.Status status) {
        switch (status) {
            case SUCCESS -> {
                return LoadFlowResult.ComponentResult.Status.CONVERGED;
            }
            case NO_IMPACT -> {
                return LoadFlowResult.ComponentResult.Status.NO_CALCULATION;
            }
            default -> {
                return LoadFlowResult.ComponentResult.Status.FAILED;
            }
        }
    }

    public static void writeJson(JsonGenerator jsonGenerator, SensitivityStateStatus stateStatus) {
        writeJson(jsonGenerator, stateStatus.state, stateStatus.componentsLoadFlowStatusList);
    }

    public static void writeJson(JsonGenerator jsonGenerator, SensitivityState state,
                                 List<ComponentStatus> componentsLoadFlowStatusList) {
        try {
            jsonGenerator.writeStartObject();
            if (state.contingencyId() != null) {
                jsonGenerator.writeStringField("contingencyId", state.contingencyId());
            }
            if (state.operatorStrategyId() != null) {
                jsonGenerator.writeStringField("operatorStrategyId", state.operatorStrategyId());
            }
            if (componentsLoadFlowStatusList != null && !componentsLoadFlowStatusList.isEmpty()) {
                jsonGenerator.writeArrayFieldStart(COMPONENT_STATUSES);
                for (ComponentStatus componentStatus : componentsLoadFlowStatusList) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(LOAD_FLOW_STATUS, componentStatus.status().toString());
                    jsonGenerator.writeStringField(LOAD_FLOW_STATUS_TEXT, componentStatus.statusText());
                    jsonGenerator.writeNumberField(NUM_CC, componentStatus.connectedComponentNum());
                    jsonGenerator.writeNumberField(NUM_SC, componentStatus.synchronousComponentNum());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static final class ParsingContext {
        private String contingencyId;
        private String operatorStrategyId;
        private SensitivityAnalysisResult.Status status;
        private List<ComponentStatus> componentsLoadFlowStatusList;
    }

    public static SensitivityStateStatus parseJson(JsonParser parser, String version) {
        Objects.requireNonNull(parser);

        var context = new SensitivityStateStatus.ParsingContext();
        try {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    parseJson(parser, context, version == null ? SensitivityAnalysisResult.VERSION : version);
                } else if (token == JsonToken.END_OBJECT) {
                    if (version != null && JsonUtil.compareVersions(version, "1.1") <= 0) {
                        return new SensitivityStateStatus(
                                new SensitivityState(context.contingencyId, context.operatorStrategyId), context.status);
                    } else {
                        return new SensitivityStateStatus(
                                new SensitivityState(context.contingencyId, context.operatorStrategyId),
                                context.componentsLoadFlowStatusList != null ? context.componentsLoadFlowStatusList : Collections.emptyList());
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        throw new PowsyblException("Parsing error");
    }

    private static void parseJson(JsonParser parser, SensitivityStateStatus.ParsingContext context, String version) throws IOException {
        String fieldName = parser.currentName();
        switch (fieldName) {
            case "contingencyId":
                parser.nextToken();
                context.contingencyId = parser.getValueAsString();
                break;
            case "operatorStrategyId":
                JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: operatorStrategyId", version, "1.1");
                parser.nextToken();
                context.operatorStrategyId = parser.getValueAsString();
                break;
            case "contingencyStatus":
                JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: contingencyStatus", version, "1.0");
                parser.nextToken();
                context.status = SensitivityAnalysisResult.Status.valueOf(parser.getValueAsString());
                break;
            case "status":
                JsonUtil.assertEqualToReferenceVersion(CONTEXT_NAME, "Tag: status", version, "1.1");
                parser.nextToken();
                context.status = SensitivityAnalysisResult.Status.valueOf(parser.getValueAsString());
                break;
            case COMPONENT_STATUSES:
                JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: " + COMPONENT_STATUSES, version, "1.2");
                context.componentsLoadFlowStatusList = parseComponentLoadFlowStatuses(parser);
                break;
            default:
                throw new PowsyblException("Unexpected field: " + fieldName);
        }
    }

    private static List<ComponentStatus> parseComponentLoadFlowStatuses(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new PowsyblException("Expected start of array for component loadflow statuses");
        }
        List<ComponentStatus> statuses = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                statuses.add(parseSingleComponentStatus(parser));
            }
        }
        return statuses;
    }

    private static ComponentStatus parseSingleComponentStatus(JsonParser parser) throws IOException {
        String statusStr = null;
        String descStr = null;
        int connectedComponentNum = 0;
        int synchronousComponentNum = 0;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();
            switch (fieldName) {
                case LOAD_FLOW_STATUS -> statusStr = parser.getText();
                case LOAD_FLOW_STATUS_TEXT -> descStr = parser.getText();
                case NUM_CC -> connectedComponentNum = parser.getIntValue();
                case NUM_SC -> synchronousComponentNum = parser.getIntValue();
                default -> parser.skipChildren();
            }
        }
        return new ComponentStatus(connectedComponentNum, synchronousComponentNum, LoadFlowResult.ComponentResult.Status.valueOf(statusStr), descStr);
    }
}
