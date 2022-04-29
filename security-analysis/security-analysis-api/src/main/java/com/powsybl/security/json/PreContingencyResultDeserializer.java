/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.results.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
class PreContingencyResultDeserializer extends StdDeserializer<PreContingencyResult> {

    private static final String CONTEXT_NAME = "PreContingencyResult";

    PreContingencyResultDeserializer() {
        super(PreContingencyResult.class);
    }

    @Override
    public PreContingencyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        LimitViolationsResult preContingencyResult = null;
        List<BranchResult> branchResults = Collections.emptyList();
        List<BusResult> busResults = Collections.emptyList();
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = Collections.emptyList();
        NetworkResult networkResult = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "limitViolationsResult":
                    parser.nextToken();
                    preContingencyResult = parser.readValueAs(LimitViolationsResult.class);
                    break;
                case "networkResult":
                    parser.nextToken();
                    if (JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE) != null) {
                        JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: networkResult",
                                JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE), "1.2");
                    }
                    networkResult = parser.readValueAs(NetworkResult.class);
                    break;
                case "branchResults":
                    parser.nextToken();
                    JsonUtil.checkVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE, "1.1",
                            "Tag: branchResults", CONTEXT_NAME);
                    branchResults = parser.readValueAs(new TypeReference<List<BranchResult>>() {
                    });
                    break;
                case "busResults":
                    parser.nextToken();
                    JsonUtil.checkVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE, "1.1",
                            "Tag: busResults", CONTEXT_NAME);
                    busResults = parser.readValueAs(new TypeReference<List<BusResult>>() {
                    });
                    break;
                case "threeWindingsTransformerResults":
                    parser.nextToken();
                    JsonUtil.checkVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE, "1.1",
                            "Tag: threeWindingsTransformerResults", CONTEXT_NAME);
                    threeWindingsTransformerResults = parser.readValueAs(new TypeReference<List<ThreeWindingsTransformerResult>>() {
                    });
                    break;
                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (networkResult != null) {
            return new PreContingencyResult(preContingencyResult, networkResult);
        } else {
            return new PreContingencyResult(preContingencyResult,
                    branchResults,
                    busResults,
                    threeWindingsTransformerResults);
        }
    }
}
