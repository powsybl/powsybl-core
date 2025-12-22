/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.HvdcAction;
import com.powsybl.action.HvdcActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.HvdcLine;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class HvdcActionBuilderDeserializer extends StdDeserializer<HvdcActionBuilder> {

    public HvdcActionBuilderDeserializer() {
        super(HvdcActionBuilder.class);
    }

    @Override
    public HvdcActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        HvdcActionBuilder hvdcActionBuilder = new HvdcActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!HvdcAction.NAME.equals(jsonParser.nextStringValue())) {
                        throw DatabindException.from(jsonParser, "Expected type " + HvdcAction.NAME);
                    }
                    return true;
                case "id":
                    hvdcActionBuilder.withId(jsonParser.nextStringValue());
                    return true;
                case "hvdcId":
                    hvdcActionBuilder.withHvdcId(jsonParser.nextStringValue());
                    return true;
                case "acEmulationEnabled":
                    jsonParser.nextToken();
                    hvdcActionBuilder.withAcEmulationEnabled(jsonParser.getValueAsBoolean());
                    return true;
                case "activePowerSetpoint":
                    jsonParser.nextToken();
                    hvdcActionBuilder.withActivePowerSetpoint(jsonParser.getValueAsDouble());
                    return true;
                case "converterMode":
                    hvdcActionBuilder.withConverterMode(HvdcLine.ConvertersMode.valueOf(jsonParser.nextStringValue()));
                    return true;
                case "droop":
                    jsonParser.nextToken();
                    hvdcActionBuilder.withDroop(jsonParser.getValueAsDouble());
                    return true;
                case "p0":
                    jsonParser.nextToken();
                    hvdcActionBuilder.withP0(jsonParser.getValueAsDouble());
                    return true;
                case "relativeValue":
                    jsonParser.nextToken();
                    hvdcActionBuilder.withRelativeValue(jsonParser.getValueAsBoolean());
                    return true;
                default:
                    return false;
            }
        });
        return hvdcActionBuilder;
    }
}
