/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.OperatorStrategyResult;

import java.io.IOException;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class ConditionalActionsResultDeserializer extends StdDeserializer<OperatorStrategyResult.ConditionalActionsResult> {

    public ConditionalActionsResultDeserializer() {
        super(OperatorStrategyResult.ConditionalActionsResult.class);
    }

    @Override
    public OperatorStrategyResult.ConditionalActionsResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String conditionalActionsId = null;
        LimitViolationsResult limitViolationsResult = null;
        NetworkResult networkResult = null;
        PostContingencyComputationStatus status = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "conditionalActionsId" -> {
                    parser.nextToken();
                    conditionalActionsId = JsonUtil.readValue(deserializationContext, parser, String.class);
                }
                case "limitViolationsResult" -> {
                    parser.nextToken();
                    limitViolationsResult = JsonUtil.readValue(deserializationContext, parser, LimitViolationsResult.class);
                }
                case "networkResult" -> {
                    parser.nextToken();
                    networkResult = JsonUtil.readValue(deserializationContext, parser, NetworkResult.class);
                }
                case "status" -> {
                    parser.nextToken();
                    status = JsonUtil.readValue(deserializationContext, parser, PostContingencyComputationStatus.class);
                }
                default -> throw new JsonMappingException(parser, "Unexpected field: " + parser.getCurrentName());
            }
        }
        return new OperatorStrategyResult.ConditionalActionsResult(conditionalActionsId, status, limitViolationsResult, networkResult);
    }
}
