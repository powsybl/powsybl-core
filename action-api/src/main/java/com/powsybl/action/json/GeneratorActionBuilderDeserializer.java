/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
public class GeneratorActionBuilderDeserializer extends StdDeserializer<GeneratorActionBuilder> {

    public GeneratorActionBuilderDeserializer() {
        super(GeneratorActionBuilder.class);
    }

    @Override
    public GeneratorActionBuilder deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        GeneratorActionBuilder generatorActionBuilder = new GeneratorActionBuilder();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type":
                    if (!GeneratorAction.NAME.equals(parser.nextTextValue())) {
                        throw JsonMappingException.from(parser, "Expected type " + GeneratorAction.NAME);
                    }
                    return true;
                case "id":
                    generatorActionBuilder.withId(parser.nextTextValue());
                    return true;
                case "generatorId":
                    generatorActionBuilder.withGeneratorId(parser.nextTextValue());
                    return true;
                case "activePowerRelativeValue":
                    parser.nextToken();
                    generatorActionBuilder.withActivePowerRelativeValue(parser.getValueAsBoolean());
                    return true;
                case "activePowerValue":
                    parser.nextToken();
                    generatorActionBuilder.withActivePowerValue(parser.getValueAsDouble());
                    return true;
                case "voltageRegulatorOn":
                    parser.nextToken();
                    generatorActionBuilder.withVoltageRegulatorOn(parser.getValueAsBoolean());
                    return true;
                case "targetV":
                    parser.nextToken();
                    generatorActionBuilder.withTargetV(parser.getValueAsDouble());
                    return true;
                case "targetQ":
                    parser.nextToken();
                    generatorActionBuilder.withTargetQ(parser.getValueAsDouble());
                    return true;
                default:
                    return false;
            }
        });
        return generatorActionBuilder;
    }
}
