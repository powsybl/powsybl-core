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
import com.powsybl.action.TerminalsConnectionActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.action.TerminalsConnectionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class TerminalsConnectionActionBuilderDeserializer extends StdDeserializer<TerminalsConnectionActionBuilder> {

    public TerminalsConnectionActionBuilderDeserializer() {
        super(TerminalsConnectionAction.class);
    }

    @Override
    public TerminalsConnectionActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        TerminalsConnectionActionBuilder builder = new TerminalsConnectionActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!TerminalsConnectionAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + TerminalsConnectionAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextTextValue());
                    return true;
                case "elementId":
                    builder.withNetworkElementId(jsonParser.nextTextValue());
                    return true;
                case "side":
                    jsonParser.nextToken();
                    builder.withSide(ThreeSides.valueOf(jsonParser.getValueAsString()));
                    return true;
                case "open":
                    jsonParser.nextToken();
                    builder.withOpen(jsonParser.getValueAsBoolean());
                    return true;
                default:
                    return false;
            }
        });
        return builder;
    }
}
