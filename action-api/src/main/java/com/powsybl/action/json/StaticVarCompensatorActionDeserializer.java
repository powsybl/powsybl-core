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

    @Override
    public StaticVarCompensatorAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        StaticVarCompensatorActionBuilder builder = new StaticVarCompensatorActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!StaticVarCompensatorAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + StaticVarCompensatorAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextTextValue());
                    return true;
                case "staticVarCompensatorId":
                    builder.withStaticVarCompensatorId(jsonParser.nextTextValue());
                    return true;
                case "regulationMode":
                    builder.withRegulationMode(StaticVarCompensator.RegulationMode.valueOf(jsonParser.nextTextValue()));
                    return true;
                case "voltageSetpoint":
                    jsonParser.nextToken();
                    builder.withVoltageSetpoint(jsonParser.getValueAsDouble());
                    return true;
                case "reactivePowerSetpoint":
                    jsonParser.nextToken();
                    builder.withReactivePowerSetpoint(jsonParser.getValueAsDouble());
                    return true;
                default:
                    return false;
            }
        });
        return builder.build();
    }
}
