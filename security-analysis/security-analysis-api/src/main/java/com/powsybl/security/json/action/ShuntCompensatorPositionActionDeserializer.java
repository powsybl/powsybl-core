/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;
import com.powsybl.security.action.ShuntCompensatorPositionAction;
import com.powsybl.security.action.ShuntCompensatorPositionActionBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ShuntCompensatorPositionActionDeserializer extends StdDeserializer<ShuntCompensatorPositionAction> {

    public ShuntCompensatorPositionActionDeserializer() {
        super(ShuntCompensatorPositionAction.class);
    }

    private static class ParsingContext {
        String id;
        List<NetworkElementIdentifier> networkElementIdentifiers;
        Integer sectionCount = null;
    }

    @Override
    public ShuntCompensatorPositionAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!ShuntCompensatorPositionAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + ShuntCompensatorPositionAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = jsonParser.nextTextValue();
                    return true;
                case "shuntCompensatorId":
                    context.networkElementIdentifiers = Collections.singletonList(new IdBasedNetworkElementIdentifier(jsonParser.nextTextValue()));
                    return true;
                case "identifiers":
                    jsonParser.nextToken();
                    context.networkElementIdentifiers = JsonUtil.readList(deserializationContext, jsonParser, NetworkElementIdentifier.class);
                    return true;
                case "sectionCount":
                    jsonParser.nextToken();
                    context.sectionCount = jsonParser.getValueAsInt();
                    return true;
                default:
                    return false;
            }
        });
        return new ShuntCompensatorPositionActionBuilder()
                .withId(context.id)
                .withShuntCompensatorIdentifiers(context.networkElementIdentifiers)
                .withSectionCount(context.sectionCount)
                .build();
    }
}
