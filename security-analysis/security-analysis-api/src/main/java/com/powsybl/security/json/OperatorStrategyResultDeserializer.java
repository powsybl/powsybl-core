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
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.operator.strategy.OperatorStrategy;
import com.powsybl.security.results.*;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class OperatorStrategyResultDeserializer  extends StdDeserializer<OperatorStrategyResult> {

    public OperatorStrategyResultDeserializer() {
        super(OperatorStrategyResult.class);
    }

    @Override
    public OperatorStrategyResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        OperatorStrategy operatorStrategy = null;
        LimitViolationsResult limitViolationsResult = null;
        NetworkResult networkResult = null;
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

                default:
                    throw new JsonMappingException(parser, "Unexpected field: " + parser.getCurrentName());
            }
        }
        return new OperatorStrategyResult(operatorStrategy, limitViolationsResult, networkResult);
    }
}
