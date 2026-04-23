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
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;
import com.powsybl.sensitivity.SensitivityAnalysisResult;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.SensitivityValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class SensitivityAnalysisResultDeserializer extends StdDeserializer<SensitivityAnalysisResult> {

    public static final String SOURCE_VERSION_ATTRIBUTE = "sourceVersionAttribute";

    protected SensitivityAnalysisResultDeserializer() {
        super(SensitivityAnalysisResult.class);
    }

    @Override
    public SensitivityAnalysisResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String version = null;
        JsonNode sensitivityValuesNode = null;
        JsonNode contingencyStatusNode = null;
        JsonNode stateStatusNode = null;
        List<String> contingencyIds = null;
        List<String> operatorStrategyIds = null;
        JsonNode factorsNode = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version":
                    parser.nextToken(); // skip
                    version = parser.getValueAsString();
                    JsonUtil.setSourceVersion(deserializationContext, version, SOURCE_VERSION_ATTRIBUTE);
                    break;

                case "sensitivityFactors":
                    parser.nextToken();
                    factorsNode = parser.readValueAsTree();
                    break;

                case "sensitivityValues":
                    parser.nextToken();
                    sensitivityValuesNode = parser.readValueAsTree();
                    break;

                case "contingencyStatus":
                    parser.nextToken();
                    contingencyStatusNode = parser.readValueAsTree();
                    break;

                case "stateStatus":
                    parser.nextToken();
                    stateStatusNode = parser.readValueAsTree();
                    break;

                case "contingencyIds":
                    parser.nextToken();
                    contingencyIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    break;

                case "operatorStrategyIds":
                    parser.nextToken();
                    operatorStrategyIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        List<SensitivityFactor> factors = readFromNode(factorsNode, deserializationContext, SensitivityFactor.class, parser.getCodec());
        List<SensitivityAnalysisResult.SensitivityStateStatus> stateStatus = Collections.emptyList();
        if (contingencyStatusNode != null) {
            JsonUtil.assertLessThanOrEqualToReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: contingencyStatus", version, "1.0");
            stateStatus = readFromNode(contingencyStatusNode, deserializationContext, SensitivityAnalysisResult.SensitivityStateStatus.class, parser.getCodec());
        }
        if (stateStatusNode != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: stateStatus", version, "1.1");
            stateStatus = readFromNode(stateStatusNode, deserializationContext, SensitivityAnalysisResult.SensitivityStateStatus.class, parser.getCodec());
        }
        List<SensitivityValue> sensitivityValues = readFromNode(sensitivityValuesNode, deserializationContext, SensitivityValue.class, parser.getCodec());
        if (contingencyIds != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: contingencyIds", version, "1.1");
        }
        if (operatorStrategyIds != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(SensitivityAnalysisResult.CONTEXT_NAME, "Tag: operatorStrategyIds", version, "1.1");
        }

        if (version == null || !version.equals("1.0") && !version.equals("1.1")) {
            throw new IllegalStateException("Only version 1.0 and 1.1 are supported.");
        }
        if ("1.0".equals(version)) {
            // In 1.0 the contingency IDs and the mapping contingency index -> ID were directly taken from 'contingencyStatus' list.
            // Therefore for this version, the "contingencyIds" tag was not encountered.
            // Note that these elements cannot be computed from the state statuses (for versions >= 1.1) because
            // they also contain some post operator strategy statuses and are not indexed by contingency
            contingencyIds = stateStatus.stream().map(s -> s.getState().contingencyId()).toList();
        }
        return new SensitivityAnalysisResult(factors, stateStatus, contingencyIds, operatorStrategyIds, sensitivityValues);
    }

    private static <T> List<T> readFromNode(JsonNode node, DeserializationContext deserializationContext,
                                            Class<T> clazz, ObjectCodec codec) throws IOException {
        List<T> result = Collections.emptyList();
        if (node != null) {
            JsonParser subParser = node.traverse(codec);
            subParser.nextToken(); // positioned on START_ARRAY
            result = JsonUtil.readList(deserializationContext, subParser, clazz);
        }
        return result;
    }
}
