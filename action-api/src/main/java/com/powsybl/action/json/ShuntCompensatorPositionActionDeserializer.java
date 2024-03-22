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
import com.powsybl.action.ShuntCompensatorPositionAction;
import com.powsybl.action.ShuntCompensatorPositionActionBuilder;

import java.io.IOException;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ShuntCompensatorPositionActionDeserializer extends StdDeserializer<ShuntCompensatorPositionAction> {

    public ShuntCompensatorPositionActionDeserializer() {
        super(ShuntCompensatorPositionAction.class);
    }

    @Override
    public ShuntCompensatorPositionAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ShuntCompensatorPositionActionBuilder builder = new ShuntCompensatorPositionActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!ShuntCompensatorPositionAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + ShuntCompensatorPositionAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextTextValue());
                    return true;
                case "shuntCompensatorId":
                    builder.withShuntCompensatorId(jsonParser.nextTextValue());
                    return true;
                case "sectionCount":
                    jsonParser.nextToken();
                    builder.withSectionCount(jsonParser.getValueAsInt());
                    return true;
                default:
                    return false;
            }
        });
        return builder.build();
    }
}
