/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitsreduction;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.security.limitsreduction.LimitReductionDefinition;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;

import java.io.IOException;
import java.util.List;

/**
 * @author Olivier Perrin {@literal <olivier.perrin@rte-france.com>}
 */
public class LimitReductionDefinitionDeserializer extends StdDeserializer<LimitReductionDefinition> {
    public LimitReductionDefinitionDeserializer() {
        super(LimitReductionDefinition.class);
    }

    private static class ParsingContext {
        float limitReduction;
        LimitType limitType;
        List<NetworkElementCriterion> networkElementCriteria;
        List<LimitDurationCriterion> durationCriteria;
    }

    @Override
    public LimitReductionDefinition deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "limitReduction" -> {
                    parser.nextToken();
                    context.limitReduction = parser.readValueAs(Float.class);
                    return true;
                }
                case "limitType" -> {
                    context.limitType = LimitType.valueOf(parser.nextTextValue());
                    return true;
                }
                case "contingencyContexts" -> {
                    parser.nextToken();
                    context.networkElementCriteria = JsonUtil.readList(deserializationContext, parser, ContingencyContext.class);
                    return true;
                }
                case "equipmentCriteria" -> {
                    parser.nextToken();
                    context.networkElementCriteria = JsonUtil.readList(deserializationContext, parser, NetworkElementCriterion.class);
                    return true;
                }
                case "durationCriteria" -> {
                    parser.nextToken();
                    context.durationCriteria = JsonUtil.readList(deserializationContext, parser, LimitDurationCriterion.class);
                    return true;
                }
                default -> {
                    return false;
                }
            }
        });
        LimitReductionDefinition definition = new LimitReductionDefinition(context.limitType)
                .setLimitReduction(context.limitReduction);
        if (context.networkElementCriteria != null) {
            definition.setNetworkElementCriteria(context.networkElementCriteria);
        }
        if (context.durationCriteria != null) {
            definition.setDurationCriteria(context.durationCriteria);
        }
        return definition;
    }
}
