/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.strategy.condition.*;
import com.powsybl.contingency.violations.LimitViolationType;
import com.powsybl.iidm.network.TerminalNumber;
import com.powsybl.iidm.network.ThreeSides;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ConditionDeserializer extends StdDeserializer<Condition> {

    private final transient ValueDeserializer<Object> violationIdsDeserializer;
    private final transient ValueDeserializer<Object> conditionFiltersDeserializer;

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
        this(null, null);
    }

    public ConditionDeserializer(ValueDeserializer<Object> violationIdsDeserializer, ValueDeserializer<Object> conditionFiltersDeserializer) {
        super(Condition.class);
        this.violationIdsDeserializer = violationIdsDeserializer;
        this.conditionFiltersDeserializer = conditionFiltersDeserializer;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws DatabindException {
        return new ConditionDeserializer(
            JsonUtil.buildListDeserializer(ctxt, property, String.class),
            JsonUtil.buildSetDeserializer(ctxt, property, LimitViolationType.class)
        );
    }

    @Override
    public Condition deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> parseChilNode(parser, context, fieldName, deserializationContext));
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
                throw DatabindException.from(parser, "Unexpected condition type: " + context.type);
        }
    }

    private boolean parseChilNode(JsonParser parser, ParsingContext context, String fieldName,
                                  DeserializationContext deserializationContext) throws JacksonException {
        switch (fieldName) {
            case "type":
                context.type = parser.nextStringValue();
                return true;
            case "violationIds":
                parser.nextToken();
                context.violationIds = JsonUtil.readList(violationIdsDeserializer, deserializationContext, parser, String.class);
                return true;
            case "filters":
                parser.nextToken();
                context.conditionFilters = JsonUtil.readSet(conditionFiltersDeserializer, deserializationContext, parser, LimitViolationType.class);
                return true;
            case "threshold":
                parser.nextToken();
                context.threshold = parser.getValueAsDouble();
                return true;
            case "equipmentId":
                context.equipmentId = parser.nextStringValue();
                return true;
            case "side":
                context.side = JsonUtil.readNullableEnumValue(parser.nextTextValue(), ThreeSides.class);
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
                context.comparisonType = JsonUtil.readNullableEnumValue(parser.nextTextValue(), AbstractThresholdCondition.ComparisonType.class);
                return true;
            case "variable":
                context.variable = JsonUtil.readNullableEnumValue(parser.nextTextValue(), AbstractThresholdCondition.Variable.class);
                return true;
            default:
                return false;
        }
    }
}
