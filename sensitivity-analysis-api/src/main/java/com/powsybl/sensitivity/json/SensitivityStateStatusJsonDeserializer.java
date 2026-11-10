/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.sensitivity.SensitivityAnalysisResult;
import com.powsybl.sensitivity.SensitivityState;
import com.powsybl.sensitivity.SensitivityStateStatus;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.powsybl.commons.json.JsonUtil.compareVersions;
import static com.powsybl.sensitivity.SensitivityAnalysisResult.CONTEXT_NAME;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityStateStatusJsonDeserializer extends StdDeserializer<SensitivityStateStatus> {

    static final String COMPONENT_STATUSES = "componentStatuses";
    static final String LOAD_FLOW_STATUS = "loadFlowStatus";
    static final String LOAD_FLOW_STATUS_TEXT = "loadFlowStatusText";
    static final String NUM_CC = "numCC";
    static final String NUM_SC = "numSC";

    public SensitivityStateStatusJsonDeserializer() {
        super(SensitivityStateStatus.class);
    }

    @Override
    public SensitivityStateStatus deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        String sourceVersion = JsonUtil.getSourceVersion(deserializationContext, SensitivityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE);
        String version = sourceVersion == null ? SensitivityAnalysisResult.VERSION : sourceVersion;
        return parseJson(jsonParser, version);
    }

    static final class ParsingContext {
        private String contingencyId;
        private String operatorStrategyId;
        private String oldStatus;
        private List<SensitivityStateStatus.ComponentStatus> componentsLoadFlowStatusList;
    }

    public static SensitivityStateStatus parseJson(JsonParser parser, String version) {
        Objects.requireNonNull(parser);
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> parseChildNode(parser, context, version, fieldName));

        if (compareVersions(version, "1.2") <= 0) {
            LoadFlowResult.ComponentResult.Status status = toLoadFlowStatus(context.oldStatus);
            SensitivityStateStatus.ComponentStatus componentStatus;
            if (LoadFlowResult.ComponentResult.Status.NO_CALCULATION.equals(status)) {
                componentStatus = new SensitivityStateStatus.ComponentStatus(-1, -1, status, "");
            } else {
                componentStatus = new SensitivityStateStatus.ComponentStatus(0, 0, status, "");
            }
            return new SensitivityStateStatus(
                    new SensitivityState(context.contingencyId, context.operatorStrategyId), List.of(componentStatus));
        } else {
            return new SensitivityStateStatus(
                    new SensitivityState(context.contingencyId, context.operatorStrategyId),
                    context.componentsLoadFlowStatusList != null ? context.componentsLoadFlowStatusList : Collections.emptyList());
        }
    }

    private static boolean parseChildNode(JsonParser parser, ParsingContext context, String version, String fieldName) throws IOException {
        switch (fieldName) {
            case "contingencyId":
                parser.nextToken();
                context.contingencyId = parser.getValueAsString();
                return true;
            case "operatorStrategyId":
                JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: operatorStrategyId", version, "1.1");
                parser.nextToken();
                context.operatorStrategyId = parser.getValueAsString();
                return true;
            case "contingencyStatus":
                JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: contingencyStatus", version, "1.0");
                parser.nextToken();
                context.oldStatus = parser.getValueAsString();
                return true;
            case "status":
                JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: status", version, "1.2");
                parser.nextToken();
                context.oldStatus = parser.getValueAsString();
                return true;
            case COMPONENT_STATUSES:
                JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: " + COMPONENT_STATUSES, version, "1.3");
                context.componentsLoadFlowStatusList = parseComponentLoadFlowStatuses(parser);
                return true;
            default:
                return false;
        }
    }

    private static List<SensitivityStateStatus.ComponentStatus> parseComponentLoadFlowStatuses(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new PowsyblException("Expected start of array for component loadflow statuses");
        }
        List<SensitivityStateStatus.ComponentStatus> statuses = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.currentToken() == JsonToken.START_OBJECT) {
                statuses.add(parseSingleComponentStatus(parser));
            }
        }
        return statuses;
    }

    private static SensitivityStateStatus.ComponentStatus parseSingleComponentStatus(JsonParser parser) throws IOException {
        LoadFlowResult.ComponentResult.Status status = LoadFlowResult.ComponentResult.Status.CONVERGED;
        String descStr = "";
        Integer connectedComponentNum = null;
        Integer synchronousComponentNum = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.currentName();
            parser.nextToken();
            switch (fieldName) {
                case LOAD_FLOW_STATUS -> status = LoadFlowResult.ComponentResult.Status.valueOf(parser.getText());
                case LOAD_FLOW_STATUS_TEXT -> descStr = parser.getText();
                case NUM_CC -> connectedComponentNum = parser.getIntValue();
                case NUM_SC -> synchronousComponentNum = parser.getIntValue();
                default -> throw new PowsyblException("Unexpected field: " + fieldName);
            }
        }
        return new SensitivityStateStatus.ComponentStatus(Objects.requireNonNull(connectedComponentNum),
                Objects.requireNonNull(synchronousComponentNum), Objects.requireNonNull(status), descStr);
    }

    private static LoadFlowResult.ComponentResult.Status toLoadFlowStatus(String oldStatus) {
        switch (oldStatus) {
            case "SUCCESS" -> {
                return LoadFlowResult.ComponentResult.Status.CONVERGED;
            }
            case "NO_IMPACT" -> {
                return LoadFlowResult.ComponentResult.Status.NO_CALCULATION;
            }
            default -> {
                return LoadFlowResult.ComponentResult.Status.FAILED;
            }
        }
    }
}
