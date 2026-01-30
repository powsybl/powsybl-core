/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.security.condition.*;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ConditionSerializer extends StdSerializer<Condition> {

    public ConditionSerializer() {
        super(Condition.class);
    }

    @Override
    public void serialize(Condition condition, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", condition.getType());
        switch (condition.getType()) {
            case AllViolationCondition.NAME:
                serializeFilters((AbstractFilteredCondition) condition, jsonGenerator, serializationContext);
                serializationContext.defaultSerializeProperty("violationIds", ((AllViolationCondition) condition).getViolationIds(), jsonGenerator);
                break;
            case AtLeastOneViolationCondition.NAME:
                serializeFilters((AbstractFilteredCondition) condition, jsonGenerator, serializationContext);
                serializationContext.defaultSerializeProperty("violationIds", ((AtLeastOneViolationCondition) condition).getViolationIds(), jsonGenerator);
                break;
            case TrueCondition.NAME:
                break;
            case AnyViolationCondition.NAME:
                serializeFilters((AbstractFilteredCondition) condition, jsonGenerator, serializationContext);
                break;
            case InjectionThresholdCondition.NAME:
                serializeThresholdCondition((AbstractThresholdCondition) condition, jsonGenerator);
                break;
            case BranchThresholdCondition.NAME:
                BranchThresholdCondition branchCondition = (BranchThresholdCondition) condition;
                serializeThresholdCondition(branchCondition, jsonGenerator);
                jsonGenerator.writeStringProperty("side", branchCondition.getSide().name());
                break;
            case ThreeWindingsTransformerThresholdCondition.NAME:
                ThreeWindingsTransformerThresholdCondition threeWtCondition = (ThreeWindingsTransformerThresholdCondition) condition;
                serializeThresholdCondition(threeWtCondition, jsonGenerator);
                jsonGenerator.writeStringProperty("side", threeWtCondition.getSide().name());
                break;
            case AcDcConverterThresholdCondition.NAME:
                AcDcConverterThresholdCondition acDcConverterCondition = (AcDcConverterThresholdCondition) condition;
                serializeThresholdCondition(acDcConverterCondition, jsonGenerator);
                jsonGenerator.writeBooleanProperty("acSide", acDcConverterCondition.isAcSide());
                jsonGenerator.writeNumberProperty("terminalNumber", acDcConverterCondition.getTerminalNumber().getNum());
                break;
            default:
                throw new IllegalArgumentException("condition type \'" + condition.getType() + "\' does not exist");
        }
        jsonGenerator.writeEndObject();
    }

    public void serializeThresholdCondition(AbstractThresholdCondition condition, JsonGenerator jsonGenerator) throws JacksonException {
        jsonGenerator.writeStringProperty("equipmentId", condition.getEquipmentId());
        jsonGenerator.writeStringProperty("variable", condition.getVariable().name());
        jsonGenerator.writeStringProperty("comparisonType", condition.getComparisonType().name());
        jsonGenerator.writeNumberProperty("threshold", condition.getThreshold());
    }

    public void serializeFilters(AbstractFilteredCondition filteredCondition, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        if (!filteredCondition.getFilters().isEmpty()) {
            serializationContext.defaultSerializeProperty("filters", filteredCondition.getFilters(), jsonGenerator);
        }
    }
}
