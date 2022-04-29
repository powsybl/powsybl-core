/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.action.ActionList;

import java.io.IOException;

import static com.powsybl.security.action.ActionList.VERSION;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ActionListSerializer extends StdSerializer<ActionList> {

    public ActionListSerializer() {
        super(ActionList.class);
    }

    @Override
    public void serialize(ActionList actionList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", VERSION);
        jsonGenerator.writeObjectField("actions", actionList.getActions());
        jsonGenerator.writeEndObject();
    }
}
