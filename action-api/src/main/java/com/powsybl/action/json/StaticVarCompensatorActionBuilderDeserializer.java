/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.StaticVarCompensatorAction;
import com.powsybl.action.StaticVarCompensatorActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.StaticVarCompensator;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class StaticVarCompensatorActionBuilderDeserializer extends StdDeserializer<StaticVarCompensatorActionBuilder> {

    public StaticVarCompensatorActionBuilderDeserializer() {
        super(StaticVarCompensatorAction.class);
    }

    @Override
    public StaticVarCompensatorActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        StaticVarCompensatorActionBuilder builder = new StaticVarCompensatorActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!StaticVarCompensatorAction.NAME.equals(jsonParser.nextStringValue())) {
                        throw DatabindException.from(jsonParser, "Expected type " + StaticVarCompensatorAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextStringValue());
                    return true;
                case "staticVarCompensatorId":
                    builder.withStaticVarCompensatorId(jsonParser.nextStringValue());
                    return true;
                case "regulationMode":
                    builder.withRegulationMode(StaticVarCompensator.RegulationMode.valueOf(jsonParser.nextStringValue()));
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
        return builder;
    }
}
