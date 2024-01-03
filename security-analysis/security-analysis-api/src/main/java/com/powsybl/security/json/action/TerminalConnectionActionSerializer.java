/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.security.action.TerminalConnectionAction;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class TerminalConnectionActionSerializer extends StdSerializer<TerminalConnectionAction> {

    public TerminalConnectionActionSerializer() {
        super(TerminalConnectionAction.class);
    }

    @Override
    public void serialize(TerminalConnectionAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("elementId", action.getElementId());
        Optional<ThreeSides> side = action.getSide();
        if (side.isPresent()) {
            JsonUtil.writeOptionalStringField(jsonGenerator, "side", String.valueOf(side.get()));
        }
        jsonGenerator.writeBooleanField("open", action.isOpen());
        jsonGenerator.writeEndObject();
    }
}
