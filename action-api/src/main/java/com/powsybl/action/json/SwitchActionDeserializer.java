/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.action.SwitchActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.SwitchAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class SwitchActionDeserializer extends StdDeserializer<SwitchAction> {

    public SwitchActionDeserializer() {
        super(SwitchAction.class);
    }

    @Override
    public SwitchAction deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        SwitchActionBuilder builder = new SwitchActionBuilder();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type":
                    if (!SwitchAction.NAME.equals(parser.nextTextValue())) {
                        throw JsonMappingException.from(parser, "Expected type " + SwitchAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(parser.nextTextValue());
                    return true;
                case "switchId":
                    builder.withSwitchId(parser.nextTextValue());
                    return true;
                case "open":
                    parser.nextToken();
                    builder.withOpen(parser.getValueAsBoolean());
                    return true;
                default:
                    return false;
            }
        });
        return builder.build();
    }
}
