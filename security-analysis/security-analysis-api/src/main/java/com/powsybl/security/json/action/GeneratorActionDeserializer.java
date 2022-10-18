/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.security.action.GeneratorAction;
import com.powsybl.security.action.GeneratorActionBuilder;

import java.io.IOException;

/**
 * @author Anne Tilloy <anne.tilloy@rte-france.com>
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
    public GeneratorAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            switch (jsonParser.getCurrentName()) {
                case "id":
                    context.id = jsonParser.nextTextValue();
                    break;
                case "generatorId":
                    context.generatorId = jsonParser.nextTextValue();
                    break;
                case "activePowerRelativeValue":
                    jsonParser.nextToken();
                    context.activePowerRelativeValue = jsonParser.getValueAsBoolean();
                    break;
                case "activePowerValue":
                    jsonParser.nextToken();
                    context.activePowerValue = jsonParser.getValueAsDouble();
                    break;
                case "voltageRegulatorOn":
                    jsonParser.nextToken();
                    context.voltageRegulatorOn = jsonParser.getValueAsBoolean();
                    break;
                case "targetV":
                    jsonParser.nextToken();
                    context.targetV = jsonParser.getValueAsDouble();
                    break;
                case "targetQ":
                    jsonParser.nextToken();
                    context.targetQ = jsonParser.getValueAsDouble();
                    break;
                default:
                    throw new IllegalArgumentException("");
            }
        }
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
