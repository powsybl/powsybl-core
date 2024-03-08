/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.action.HvdcAction;
import com.powsybl.action.HvdcActionBuilder;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class HvdcActionDeserializer extends StdDeserializer<HvdcAction> {

    public HvdcActionDeserializer() {
        super(HvdcAction.class);
    }

    private static class ParsingContext {
        String id;
        String hvdcId;
        Boolean acEmulationEnabled;
        Double activePowerSetpoint;
        HvdcLine.ConvertersMode converterMode;
        Double droop;
        Double p0;
        Boolean relativeValue;
    }

    @Override
    public HvdcAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!HvdcAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + HvdcAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = jsonParser.nextTextValue();
                    return true;
                case "hvdcId":
                    context.hvdcId = jsonParser.nextTextValue();
                    return true;
                case "acEmulationEnabled":
                    jsonParser.nextToken();
                    context.acEmulationEnabled = jsonParser.getValueAsBoolean();
                    return true;
                case "activePowerSetpoint":
                    jsonParser.nextToken();
                    context.activePowerSetpoint = jsonParser.getValueAsDouble();
                    return true;
                case "converterMode":
                    context.converterMode = HvdcLine.ConvertersMode.valueOf(jsonParser.nextTextValue());
                    return true;
                case "droop":
                    jsonParser.nextToken();
                    context.droop = jsonParser.getValueAsDouble();
                    return true;
                case "p0":
                    jsonParser.nextToken();
                    context.p0 = jsonParser.getValueAsDouble();
                    return true;
                case "relativeValue":
                    jsonParser.nextToken();
                    context.relativeValue = jsonParser.getValueAsBoolean();
                    return true;
                default:
                    return false;
            }
        });
        return new HvdcActionBuilder()
                .withId(context.id)
                .withHvdcId(context.hvdcId)
                .withAcEmulationEnabled(context.acEmulationEnabled)
                .withActivePowerSetpoint(context.activePowerSetpoint)
                .withConverterMode(context.converterMode)
                .withDroop(context.droop)
                .withP0(context.p0)
                .withRelativeValue(context.relativeValue)
                .build();
    }
}
