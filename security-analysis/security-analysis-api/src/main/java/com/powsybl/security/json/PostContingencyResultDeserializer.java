/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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
import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.results.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class PostContingencyResultDeserializer extends StdDeserializer<PostContingencyResult> {

    private static final String CONTEXT_NAME = "PostContingencyResult";

    PostContingencyResultDeserializer() {
        super(PostContingencyResult.class);
    }

    @Override
    public PostContingencyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        Contingency contingency = null;
        LimitViolationsResult limitViolationsResult = null;
        List<BranchResult> branchResults = Collections.emptyList();
        List<BusResult> busResults = Collections.emptyList();
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = Collections.emptyList();
        NetworkResult networkResult = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "contingency":
                    parser.nextToken();
                    contingency = parser.readValueAs(Contingency.class);
                    break;

                case "limitViolationsResult":
                    parser.nextToken();
                    limitViolationsResult = parser.readValueAs(LimitViolationsResult.class);
                    break;
                case "busResults":
                    parser.nextToken();
                    JsonUtil.checkVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE, "1.1",
                            "Tag: busResults", CONTEXT_NAME);
                    busResults = parser.readValueAs(new TypeReference<List<BusResult>>() {
                    });
                    break;
                case "branchResults":
                    parser.nextToken();
                    JsonUtil.checkVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE, "1.1",
                            "Tag: branchResults", CONTEXT_NAME);
                    branchResults = parser.readValueAs(new TypeReference<List<BranchResult>>() {
                    });
                    break;
                case "threeWindingsTransformerResults":
                    parser.nextToken();
                    JsonUtil.checkVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE, "1.1",
                            "Tag: threeWindingsTransformerResults", CONTEXT_NAME);
                    threeWindingsTransformerResults = parser.readValueAs(new TypeReference<List<ThreeWindingsTransformerResult>>() {
                    });
                    break;
                case "networkResult":
                    parser.nextToken();
                    if (JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE) != null) {
                        JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: networkResult",
                                JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE), "1.2");
                    }
                    networkResult = parser.readValueAs(NetworkResult.class);
                    break;
                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (networkResult != null) {
            return new PostContingencyResult(contingency, limitViolationsResult, networkResult);
        } else {
            return new PostContingencyResult(contingency, limitViolationsResult, branchResults, busResults, threeWindingsTransformerResults);
        }
    }
}
