/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.action.TerminalsConnectionAction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Optional;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class TerminalsConnectionActionSerializer extends StdSerializer<TerminalsConnectionAction> {

    public TerminalsConnectionActionSerializer() {
        super(TerminalsConnectionAction.class);
    }

    @Override
    public void serialize(TerminalsConnectionAction action, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", action.getType());
        jsonGenerator.writeStringProperty("id", action.getId());
        jsonGenerator.writeStringProperty("elementId", action.getElementId());
        Optional<ThreeSides> side = action.getSide();
        if (side.isPresent()) {
            JsonUtil.writeOptionalStringProperty(jsonGenerator, "side", String.valueOf(side.get()));
        }
        jsonGenerator.writeBooleanProperty("open", action.isOpen());
        jsonGenerator.writeEndObject();
    }
}
