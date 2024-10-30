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
import com.powsybl.action.PercentChangeLoadAction;
import com.powsybl.action.PercentChangeLoadActionBuilder;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;

/**
 * @author Benoît Chiquet {@literal <benoit.chiquet@rte-france.com>}
 */
public class PercentChangeLoadActionBuilderDeserializer extends StdDeserializer<PercentChangeLoadActionBuilder> {
    public PercentChangeLoadActionBuilderDeserializer() {
        super(PercentChangeLoadActionBuilder.class);
    }

    @Override
    public PercentChangeLoadActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        PercentChangeLoadActionBuilder builder = new PercentChangeLoadActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!PercentChangeLoadAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + HvdcAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextTextValue());
                    return true;
                case "loadId":
                    builder.withLoadId(jsonParser.nextTextValue());
                    return true;
                case "p0PercentChange":
                    jsonParser.nextToken();
                    builder.withPercentP0Change(jsonParser.getValueAsDouble());
                    return true;
                case "qModificationStrategy":
                    builder.withQModificationStrategy(PercentChangeLoadAction.QModificationStrategy.valueOf(jsonParser.nextTextValue()));
                    return true;
                default:
                    return false;
            }
        });
        return builder;
    }
}
