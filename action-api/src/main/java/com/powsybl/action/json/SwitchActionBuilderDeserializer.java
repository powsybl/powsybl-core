/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.SwitchAction;
import com.powsybl.action.SwitchActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class SwitchActionBuilderDeserializer extends StdDeserializer<SwitchActionBuilder> {

    public SwitchActionBuilderDeserializer() {
        super(SwitchAction.class);
    }

    @Override
    public SwitchActionBuilder deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        SwitchActionBuilder builder = new SwitchActionBuilder();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type":
                    if (!SwitchAction.NAME.equals(parser.nextStringValue())) {
                        throw DatabindException.from(parser, "Expected type " + SwitchAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(parser.nextStringValue());
                    return true;
                case "switchId":
                    builder.withSwitchId(parser.nextStringValue());
                    return true;
                case "open":
                    parser.nextToken();
                    builder.withOpen(parser.getValueAsBoolean());
                    return true;
                default:
                    return false;
            }
        });
        return builder;
    }
}
