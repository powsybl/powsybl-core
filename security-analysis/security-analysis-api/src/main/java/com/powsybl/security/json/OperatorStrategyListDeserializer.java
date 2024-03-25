/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;

import java.io.IOException;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyListDeserializer extends StdDeserializer<OperatorStrategyList> {

    public OperatorStrategyListDeserializer() {
        super(OperatorStrategyList.class);
    }

    private static class ParsingContext {
        String version;
        List<OperatorStrategy> operatorStrategies;
    }

    @Override
    public OperatorStrategyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (parser.getCurrentName()) {
                case "version":
                    context.version = parser.nextTextValue();
                    return true;
                case "operatorStrategies":
                    parser.nextToken(); // skip
                    context.operatorStrategies = JsonUtil.readList(deserializationContext, parser, OperatorStrategy.class);
                    return true;
                default:
                    return false;
            }
        });
        if (context.version == null) {
            throw new JsonMappingException(parser, "version is missing");
        }
        return new OperatorStrategyList(context.operatorStrategies);
    }
}
