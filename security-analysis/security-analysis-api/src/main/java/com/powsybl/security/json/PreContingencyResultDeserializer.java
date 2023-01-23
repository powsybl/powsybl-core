/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.results.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        LoadFlowResult.ComponentResult.Status status = null;
        String version = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        if (version == null) {  // assuming current version...
            version = SecurityAnalysisResultSerializer.VERSION;
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "limitViolationsResult":
                    parser.nextToken();
                    preContingencyResult = JsonUtil.readValueWithContext(deserializationContext, parser, LimitViolationsResult.class);
                    break;
                case "networkResult":
                    parser.nextToken();
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: networkResult",
                            version, "1.2");
                    networkResult = JsonUtil.readValueWithContext(deserializationContext, parser, NetworkResult.class);
                    break;
                case "busResults":
                    parser.nextToken();
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: busResults",
                            version, "1.1");
                    busResults = JsonUtil.readList(deserializationContext, parser, BusResult.class);
                    break;
                case "branchResults":
                    parser.nextToken();
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: branchResults",
                            version, "1.1");
                    branchResults = JsonUtil.readList(deserializationContext, parser, BranchResult.class);
                    break;
                case "threeWindingsTransformerResults":
                    parser.nextToken();
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: threeWindingsTransformerResults",
                            version, "1.1");
                    threeWindingsTransformerResults = JsonUtil.readList(deserializationContext, parser, ThreeWindingsTransformerResult.class);
                    break;
                case "status":
                    parser.nextToken();
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: status",
                            version, "1.3");
                    status = JsonUtil.readValueWithContext(deserializationContext, parser, LoadFlowResult.ComponentResult.Status.class);
                    break;
                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (version.compareTo("1.3") < 0) {
            Objects.requireNonNull(preContingencyResult);
            status = preContingencyResult.isComputationOk() ? LoadFlowResult.ComponentResult.Status.CONVERGED : LoadFlowResult.ComponentResult.Status.FAILED;
        }
        if (networkResult != null) {
            return new PreContingencyResult(status, preContingencyResult, networkResult);
        } else {
            return new PreContingencyResult(status, preContingencyResult,
                    branchResults,
                    busResults,
                    threeWindingsTransformerResults);
        }
    }
}
