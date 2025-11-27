/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.TerminalNumber;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.condition.*;
import com.powsybl.contingency.strategy.condition.*;
import com.powsybl.contingency.violations.LimitViolationType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ConditionDeserializer extends StdDeserializer<Condition> {

    private static final class ParsingContext {
        String type;
        List<String> violationIds;
        Set<LimitViolationType> conditionFilters = Collections.emptySet();
        double threshold;
        String equipmentId;
        ThreeSides side;
        boolean isAcSide;
        TerminalNumber terminalNumber;
        AbstractThresholdCondition.ComparisonType comparisonType;
        AbstractThresholdCondition.Variable variable;
    }

    public ConditionDeserializer() {
        super(Condition.class);
    }

    @Override
    public Condition deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "type":
                    context.type = parser.nextTextValue();
                    return true;
                case "violationIds":
                    parser.nextToken();
                    context.violationIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    return true;
                case "filters":
                    parser.nextToken();
                    context.conditionFilters = JsonUtil.readSet(deserializationContext, parser, LimitViolationType.class);
                    return true;
                case "threshold":
                    parser.nextToken();
                    context.threshold = parser.getValueAsDouble();
                    return true;
                case "equipmentId":
                    context.equipmentId = parser.nextTextValue();
                    return true;
                case "side":
                    parser.nextToken();
                    context.side = JsonUtil.readValue(deserializationContext, parser, ThreeSides.class);
                    return true;
                case "acSide":
                    parser.nextToken();
                    context.isAcSide = parser.getBooleanValue();
                    return true;
                case "terminalNumber":
                    parser.nextToken();
                    context.terminalNumber = TerminalNumber.valueOf(parser.getValueAsInt());
                    return true;
                case "comparisonType":
                    parser.nextToken();
                    context.comparisonType = JsonUtil.readValue(deserializationContext, parser, AbstractThresholdCondition.ComparisonType.class);
                    return true;
                case "variable":
                    parser.nextToken();
                    context.variable = JsonUtil.readValue(deserializationContext, parser, AbstractThresholdCondition.Variable.class);
                    return true;
                default:
                    return false;
            }
        });
        switch (context.type) {
            case TrueCondition.NAME:
                return new TrueCondition();
            case AnyViolationCondition.NAME:
                return new AnyViolationCondition(context.conditionFilters);
            case AtLeastOneViolationCondition.NAME:
                return new AtLeastOneViolationCondition(context.violationIds, context.conditionFilters);
            case AllViolationCondition.NAME:
                return new AllViolationCondition(context.violationIds, context.conditionFilters);
            case InjectionThresholdCondition.NAME:
                return new InjectionThresholdCondition(context.equipmentId, context.variable, context.comparisonType, context.threshold);
            case BranchThresholdCondition.NAME:
                return new BranchThresholdCondition(context.equipmentId, context.variable, context.comparisonType, context.threshold, context.side.toTwoSides());
            case ThreeWindingsTransformerThresholdCondition.NAME:
                return new ThreeWindingsTransformerThresholdCondition(context.equipmentId, context.variable, context.comparisonType, context.threshold, context.side);
            case AcDcConverterThresholdCondition.NAME:
                return new AcDcConverterThresholdCondition(context.equipmentId, context.variable, context.comparisonType, context.threshold, context.isAcSide, context.terminalNumber);
            default:
                throw new JsonMappingException(parser, "Unexpected condition type: " + context.type);
        }
    }
}
