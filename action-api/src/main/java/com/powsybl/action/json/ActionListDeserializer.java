/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.action.Action;
import com.powsybl.action.ActionList;

import java.io.IOException;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class ActionListDeserializer extends StdDeserializer<ActionList> {

    public static final String VERSION = "version";

    public ActionListDeserializer() {
        super(ActionList.class);
    }

    private static class ParsingContext {
        String version;
        List<Action> actions;
    }

    @Override
    public ActionList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case VERSION:
                    context.version = parser.nextTextValue();
                    deserializationContext.setAttribute(VERSION, context.version);
                    return true;
                case "actions":
                    parser.nextToken();
                    context.actions = JsonUtil.readList(deserializationContext, parser, Action.class);
                    return true;
                default:
                    return false;
            }
        });
        if (context.version == null) {
            throw new JsonMappingException(parser, "version is missing");
        }
        JsonUtil.assertLessThanOrEqualToReferenceVersion("actions", "Tag: tapPosition", context.version, ActionList.VERSION);
        return new ActionList(context.actions);
    }
}
