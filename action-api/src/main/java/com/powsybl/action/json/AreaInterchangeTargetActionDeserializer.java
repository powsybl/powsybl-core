/*
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.AreaInterchangeTargetAction;
import com.powsybl.action.AreaInterchangeTargetActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AreaInterchangeTargetActionDeserializer extends StdDeserializer<AreaInterchangeTargetActionBuilder> {

    protected AreaInterchangeTargetActionDeserializer() {
        super(AreaInterchangeTargetActionBuilder.class);
    }

    @Override
    public AreaInterchangeTargetActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        AreaInterchangeTargetActionBuilder builder = new AreaInterchangeTargetActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!AreaInterchangeTargetAction.NAME.equals(jsonParser.nextStringValue())) {
                        throw DatabindException.from(jsonParser, "Expected type " + AreaInterchangeTargetAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextStringValue());
                    return true;
                case "areaId":
                    builder.withAreaId(jsonParser.nextStringValue());
                    return true;
                case "interchangeTarget":
                    jsonParser.nextToken();
                    builder.withTarget(jsonParser.getValueAsDouble());
                    return true;
                default:
                    return false;
            }
        });
        return builder;
    }
}
