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
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.action.StaticVarCompensatorAction;
import com.powsybl.action.StaticVarCompensatorActionBuilder;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class StaticVarCompensatorActionDeserializer extends StdDeserializer<StaticVarCompensatorAction> {

    public StaticVarCompensatorActionDeserializer() {
        super(StaticVarCompensatorAction.class);
    }

    private static class ParsingContext {
        String id;
        String staticVarCompensatorId;
        String regulationMode;
        Double voltageSetpoint;
        Double reactivePowerSetpoint;
    }

    @Override
    public StaticVarCompensatorAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!StaticVarCompensatorAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + StaticVarCompensatorAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = jsonParser.nextTextValue();
                    return true;
                case "staticVarCompensatorId":
                    context.staticVarCompensatorId = jsonParser.nextTextValue();
                    return true;
                case "regulationMode":
                    context.regulationMode = jsonParser.nextTextValue();
                    return true;
                case "voltageSetpoint":
                    jsonParser.nextToken();
                    context.voltageSetpoint = jsonParser.getValueAsDouble();
                    return true;
                case "reactivePowerSetpoint":
                    jsonParser.nextToken();
                    context.reactivePowerSetpoint = jsonParser.getValueAsDouble();
                    return true;
                default:
                    return false;
            }
        });
        StaticVarCompensatorActionBuilder builder = new StaticVarCompensatorActionBuilder();
        builder.withId(context.id)
                .withStaticVarCompensatorId(context.staticVarCompensatorId);
        if (context.regulationMode != null) {
            builder.withRegulationMode(StaticVarCompensator.RegulationMode.valueOf(context.regulationMode));
        }
        if (context.voltageSetpoint != null) {
            builder.withVoltageSetpoint(context.voltageSetpoint);
        }
        if (context.reactivePowerSetpoint != null) {
            builder.withReactivePowerSetpoint(context.reactivePowerSetpoint);
        }
        return builder.build();
    }
}
