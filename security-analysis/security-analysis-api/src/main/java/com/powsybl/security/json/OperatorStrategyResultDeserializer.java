/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.results.*;

import java.io.IOException;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
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
        LoadFlowResult.ComponentResult.Status status = null;
        String version = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        if (version == null) {  // assuming current version...
            version = SecurityAnalysisResultSerializer.VERSION;
        }
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "operatorStrategy":
                    parser.nextToken();
                    operatorStrategy = parser.readValueAs(OperatorStrategy.class);
                    break;

                case "limitViolationsResult":
                    parser.nextToken();
                    limitViolationsResult = parser.readValueAs(LimitViolationsResult.class);
                    break;

                case "networkResult":
                    parser.nextToken();
                    networkResult = parser.readValueAs(NetworkResult.class);
                    break;

                case "status":
                    parser.nextToken();
                    status = parser.readValueAs(LoadFlowResult.ComponentResult.Status.class);
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: contingencyStatus",
                            version, "1.3");
                    break;

                default:
                    throw new JsonMappingException(parser, "Unexpected field: " + parser.getCurrentName());
            }
        }
        if (version.compareTo("1.3") < 0) {
            if (limitViolationsResult != null) {
                return new OperatorStrategyResult(operatorStrategy, limitViolationsResult,
                        limitViolationsResult.isComputationOk() ? LoadFlowResult.ComponentResult.Status.CONVERGED : LoadFlowResult.ComponentResult.Status.FAILED,
                        networkResult);
            } else {
                return new OperatorStrategyResult(operatorStrategy, limitViolationsResult, LoadFlowResult.ComponentResult.Status.CONVERGED, networkResult);
            }
        } else {
            return new OperatorStrategyResult(operatorStrategy, limitViolationsResult, status, networkResult);
        }
    }
}
