/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.ActionList;
import com.powsybl.action.IdentifierActionList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Map;
import java.util.stream.Collectors;

import static com.powsybl.action.ActionList.VERSION;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ActionListSerializer extends StdSerializer<ActionList> {

    public ActionListSerializer() {
        super(ActionList.class);
    }

    @Override
    public void serialize(ActionList actionList, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", VERSION);
        serializationContext.defaultSerializeProperty("actions", actionList.getActions(), jsonGenerator);
        if (actionList instanceof IdentifierActionList identifierActionList) {
            serializationContext.defaultSerializeProperty("elementIdentifiers",
                identifierActionList.getElementIdentifierMap().entrySet().stream()
                    .collect(Collectors.toMap(map -> map.getKey().getId(), Map.Entry::getValue)), jsonGenerator);
            serializationContext.defaultSerializeProperty("actionBuilders",
                identifierActionList.getElementIdentifierMap().entrySet().stream()
                    .collect(Collectors.toMap(map -> map.getKey().getId(), Map.Entry::getKey)), jsonGenerator);
        }
        jsonGenerator.writeEndObject();
    }
}
