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
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Olivier Perrin {@literal <olivier.perrin@rte-france.com>}
 */
public class LimitReductionDeserializer extends StdDeserializer<LimitReduction> {
    public LimitReductionDeserializer() {
        super(LimitReduction.class);
    }

    private static class ParsingContext {
        float value;
        LimitType limitType;
        boolean monitoringOnly;
        ContingencyContext contingencyContext;
        List<NetworkElementCriterion> networkElementCriteria;
        List<LimitDurationCriterion> durationCriteria;
    }

    @Override
    public LimitReduction deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "value" -> {
                    parser.nextToken();
                    context.value = parser.readValueAs(Float.class);
                    return true;
                }
                case "limitType" -> {
                    context.limitType = LimitType.valueOf(parser.nextTextValue());
                    return true;
                }
                case "monitoringOnly" -> {
                    context.monitoringOnly = parser.nextBooleanValue();
                    return true;
                }
                case "contingencyContext" -> {
                    parser.nextToken();
                    context.contingencyContext = JsonUtil.readValue(deserializationContext, parser, ContingencyContext.class);
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
        return new LimitReduction(context.limitType, context.value, context.monitoringOnly,
                context.contingencyContext,
                context.networkElementCriteria != null ? context.networkElementCriteria : Collections.emptyList(),
                context.durationCriteria != null ? context.durationCriteria : Collections.emptyList());
    }
}
