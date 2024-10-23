/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
import com.powsybl.action.HvdcAction;
import com.powsybl.action.PctLoadAction;
import com.powsybl.action.PctLoadActionBuilder;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;

/**
 * @author Benoît Chiquet {@literal <benoit.chiquet@rte-france.com>}
 */
public class PctLoadActionBuilderDeserializer extends StdDeserializer<PctLoadActionBuilder> {
    public PctLoadActionBuilderDeserializer() {
        super(PctLoadActionBuilder.class);
    }

    @Override
    public PctLoadActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        PctLoadActionBuilder builder = new PctLoadActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!PctLoadAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + HvdcAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextTextValue());
                    return true;
                case "loadId":
                    builder.withLoadId(jsonParser.nextTextValue());
                    return true;
                case "pctPChange":
                    jsonParser.nextToken();
                    builder.withPctPChange(jsonParser.getValueAsDouble());
                    return true;
                case "qStrategy":
                    builder.withQStrategy(PctLoadAction.QModificationStrategy.valueOf(jsonParser.nextTextValue()));
                    return true;
                default:
                    return false;
            }
        });
        return builder;
    }
}
