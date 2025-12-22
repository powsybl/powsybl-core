/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.PercentChangeLoadAction;
import com.powsybl.action.PercentChangeLoadActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Benoît Chiquet {@literal <benoit.chiquet@rte-france.com>}
 */
public class PercentChangeLoadActionBuilderDeserializer extends StdDeserializer<PercentChangeLoadActionBuilder> {
    public PercentChangeLoadActionBuilderDeserializer() {
        super(PercentChangeLoadActionBuilder.class);
    }

    @Override
    public PercentChangeLoadActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        PercentChangeLoadActionBuilder builder = new PercentChangeLoadActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!PercentChangeLoadAction.NAME.equals(jsonParser.nextStringValue())) {
                        throw DatabindException.from(jsonParser, "Expected type " + PercentChangeLoadAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextStringValue());
                    return true;
                case "loadId":
                    builder.withLoadId(jsonParser.nextStringValue());
                    return true;
                case "p0PercentChange":
                    jsonParser.nextToken();
                    builder.withP0PercentChange(jsonParser.getValueAsDouble());
                    return true;
                case "qModificationStrategy":
                    builder.withQModificationStrategy(PercentChangeLoadAction.QModificationStrategy.valueOf(jsonParser.nextStringValue()));
                    return true;
                default:
                    return false;
            }
        });
        return builder;
    }
}
