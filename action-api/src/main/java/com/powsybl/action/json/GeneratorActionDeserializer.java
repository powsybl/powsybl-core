/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.GeneratorAction;
import com.powsybl.action.GeneratorActionBuilder;

import java.io.IOException;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class GeneratorActionDeserializer extends StdDeserializer<GeneratorAction> {

    public GeneratorActionDeserializer() {
        super(GeneratorAction.class);
    }

    private static class ParsingContext {
        String id;
        String generatorId;
        Boolean activePowerRelativeValue;
        Double activePowerValue;
        Boolean voltageRegulatorOn;
        Double targetV;
        Double targetQ;
    }

    @Override
    public GeneratorAction deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type":
                    if (!GeneratorAction.NAME.equals(parser.nextTextValue())) {
                        throw JsonMappingException.from(parser, "Expected type " + GeneratorAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = parser.nextTextValue();
                    return true;
                case "generatorId":
                    context.generatorId = parser.nextTextValue();
                    return true;
                case "activePowerRelativeValue":
                    parser.nextToken();
                    context.activePowerRelativeValue = parser.getValueAsBoolean();
                    return true;
                case "activePowerValue":
                    parser.nextToken();
                    context.activePowerValue = parser.getValueAsDouble();
                    return true;
                case "voltageRegulatorOn":
                    parser.nextToken();
                    context.voltageRegulatorOn = parser.getValueAsBoolean();
                    return true;
                case "targetV":
                    parser.nextToken();
                    context.targetV = parser.getValueAsDouble();
                    return true;
                case "targetQ":
                    parser.nextToken();
                    context.targetQ = parser.getValueAsDouble();
                    return true;
                default:
                    return false;
            }
        });
        GeneratorActionBuilder generatorActionBuilder = new GeneratorActionBuilder();
        generatorActionBuilder
                .withId(context.id)
                .withGeneratorId(context.generatorId);
        if (context.activePowerRelativeValue != null) {
            generatorActionBuilder.withActivePowerRelativeValue(context.activePowerRelativeValue);
        }
        if (context.activePowerValue != null) {
            generatorActionBuilder.withActivePowerValue(context.activePowerValue);
        }
        if (context.voltageRegulatorOn != null) {
            generatorActionBuilder.withVoltageRegulatorOn(context.voltageRegulatorOn);
        }
        if (context.targetV != null) {
            generatorActionBuilder.withTargetV(context.targetV);
        }
        if (context.targetQ != null) {
            generatorActionBuilder.withTargetQ(context.targetQ);
        }
        return generatorActionBuilder.build();
    }
}
