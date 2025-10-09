/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.action.ActionBuilder;
import com.powsybl.action.IdentifierActionList;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.Action;
import com.powsybl.action.ActionList;
import com.powsybl.iidm.network.identifiers.NetworkElementIdentifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class ActionListDeserializer extends StdDeserializer<ActionList> {

    public static final String VERSION = "version";

    public ActionListDeserializer() {
        super(ActionList.class);
    }

    public static class ParsingContext {
        String version;
        List<Action> actions;
        Map<String, NetworkElementIdentifier> elementIdentifierMap = new HashMap<>();
        Map<String, ActionBuilder> actionBuilderMap = new HashMap<>();
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
                    List<ActionBuilder> actionBuilders = JsonUtil.readList(deserializationContext, parser, ActionBuilder.class);
                    context.actions = actionBuilders.stream().map(ActionBuilder::build).toList();
                    return true;
                case "elementIdentifiers":
                    parser.nextToken();
                    context.elementIdentifierMap = parser.readValueAs(new TypeReference<HashMap<String, NetworkElementIdentifier>>() {
                    });
                    return true;
                case "actionBuilders":
                    parser.nextToken();
                    context.actionBuilderMap = parser.readValueAs(new TypeReference<HashMap<String, ActionBuilder>>() {
                    });
                    return true;
                default:
                    return false;
            }
        });
        if (context.version == null) {
            throw new JsonMappingException(parser, "version is missing");
        }
        JsonUtil.assertSupportedVersion("actions", context.version, ActionList.VERSION);
        if (!context.actionBuilderMap.isEmpty()) {
            if (context.elementIdentifierMap.size() != context.actionBuilderMap.size()) {
                throw new IOException("map elementIdentifiers and actionBuilders must have the same size");
            }
            if (!context.actionBuilderMap.keySet().containsAll(context.elementIdentifierMap.keySet())) {
                throw new IOException("keys in elementIdentifiers are different from actionBuilders");
            }
            Map<ActionBuilder, NetworkElementIdentifier> actionBuilderNetworkElementIdentifierMap = new HashMap<>();
            context.elementIdentifierMap.forEach((actionId, identifier) ->
                actionBuilderNetworkElementIdentifierMap.put(context.actionBuilderMap.get(actionId), identifier));
            return new IdentifierActionList(context.actions, actionBuilderNetworkElementIdentifierMap);
        }
        return new ActionList(context.actions);
    }
}
