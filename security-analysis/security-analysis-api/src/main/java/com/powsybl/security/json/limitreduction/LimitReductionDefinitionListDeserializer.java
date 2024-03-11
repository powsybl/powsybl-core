/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitreduction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.limitreduction.LimitReductionDefinition;
import com.powsybl.security.limitreduction.LimitReductionDefinitionList;

import java.io.IOException;
import java.util.List;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LimitReductionDefinitionListDeserializer extends StdDeserializer<LimitReductionDefinitionList> {
    public LimitReductionDefinitionListDeserializer() {
        super(LimitReductionDefinitionList.class);
    }

    private static class ParsingContext {
        String version;
        List<LimitReductionDefinition> limitReductionDefinitions;
    }

    @Override
    public LimitReductionDefinitionList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (parser.getCurrentName()) {
                case "version":
                    context.version = parser.nextTextValue();
                    return true;
                case "limitReductionDefinitions":
                    parser.nextToken(); // skip
                    context.limitReductionDefinitions = JsonUtil.readList(deserializationContext, parser, LimitReductionDefinition.class);
                    return true;
                default:
                    return false;
            }
        });
        if (context.version == null) {
            throw new JsonMappingException(parser, "version is missing");
        }
        return new LimitReductionDefinitionList(context.limitReductionDefinitions);
    }
}
