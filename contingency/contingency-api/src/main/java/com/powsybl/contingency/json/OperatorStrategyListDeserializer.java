/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.strategy.OperatorStrategy;
import com.powsybl.contingency.strategy.OperatorStrategyList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.List;

import static com.powsybl.contingency.json.OperatorStrategyDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategyListDeserializer extends StdDeserializer<OperatorStrategyList> {

    public OperatorStrategyListDeserializer() {
        super(OperatorStrategyList.class);
    }

    private static final class ParsingContext {
        String version;
        List<OperatorStrategy> operatorStrategies;
    }

    @Override
    public OperatorStrategyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (parser.currentName()) {
                case "version":
                    context.version = parser.nextStringValue();
                    JsonUtil.setSourceVersion(deserializationContext, context.version, SOURCE_VERSION_ATTRIBUTE);
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
            throw DatabindException.from(parser, "version is missing");
        }
        return new OperatorStrategyList(context.operatorStrategies);
    }
}
