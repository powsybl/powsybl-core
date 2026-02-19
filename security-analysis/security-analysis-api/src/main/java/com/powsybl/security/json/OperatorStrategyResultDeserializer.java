/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.results.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyResultDeserializer extends StdDeserializer<OperatorStrategyResult> {

    private static final String CONTEXT_NAME = "OperatorStrategyResult";

    public OperatorStrategyResultDeserializer() {
        super(OperatorStrategyResult.class);
    }

    @Override
    public OperatorStrategyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        OperatorStrategy operatorStrategy = null;
        LimitViolationsResult limitViolationsResult = null;
        NetworkResult networkResult = null;
        PostContingencyComputationStatus status = null;
        List<OperatorStrategyResult.ConditionalActionsResult> conditionalActionsResultList = null;
        String version = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        if (version == null) {  // assuming current version...
            version = SecurityAnalysisResultSerializer.VERSION;
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "operatorStrategy":
                    parser.nextToken();
                    operatorStrategy = JsonUtil.readValue(deserializationContext, parser, OperatorStrategy.class);
                    break;

                case "limitViolationsResult":
                    parser.nextToken();
                    limitViolationsResult = JsonUtil.readValue(deserializationContext, parser, LimitViolationsResult.class);
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: limitViolationsResult",
                            version, "1.5");
                    break;

                case "networkResult":
                    parser.nextToken();
                    networkResult = JsonUtil.readValue(deserializationContext, parser, NetworkResult.class);
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: networkResult",
                            version, "1.5");
                    break;

                case "status":
                    parser.nextToken();
                    status = JsonUtil.readValue(deserializationContext, parser, PostContingencyComputationStatus.class);
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: contingencyStatus",
                            version, "1.3");
                    JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: contingencyStatus",
                            version, "1.6");
                    break;

                case "conditionalActionsResults":
                    parser.nextToken();
                    conditionalActionsResultList = JsonUtil.readList(deserializationContext, parser, OperatorStrategyResult.ConditionalActionsResult.class);
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: conditionalActionsResults",
                            version, "1.6");
                    break;
                default:
                    throw new JsonMappingException(parser, "Unexpected field: " + parser.currentName());
            }
        }
        if (version.compareTo("1.3") < 0) {
            Objects.requireNonNull(limitViolationsResult);
            return new OperatorStrategyResult(operatorStrategy, limitViolationsResult.isComputationOk() ? PostContingencyComputationStatus.CONVERGED : PostContingencyComputationStatus.FAILED,
                    limitViolationsResult, networkResult);
        } else if (version.compareTo("1.6") < 0) {
            return new OperatorStrategyResult(operatorStrategy, status, limitViolationsResult, networkResult);
        } else {
            return new OperatorStrategyResult(operatorStrategy, conditionalActionsResultList);
        }
    }
}
