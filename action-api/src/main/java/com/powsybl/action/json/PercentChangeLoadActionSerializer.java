/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.PercentChangeLoadAction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Benoît Chiquet {@literal <benoit.chiquet@rte-france.com>}
 */
public class PercentChangeLoadActionSerializer extends StdSerializer<PercentChangeLoadAction> {

    public PercentChangeLoadActionSerializer() {
        super(PercentChangeLoadAction.class);
    }

    @Override
    public void serialize(PercentChangeLoadAction action, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", action.getType());
        jsonGenerator.writeStringProperty("id", action.getId());
        jsonGenerator.writeStringProperty("loadId", action.getLoadId());
        jsonGenerator.writeNumberProperty("p0PercentChange", action.getP0PercentChange());
        jsonGenerator.writeStringProperty("qModificationStrategy", action.getQModificationStrategy().toString());
        jsonGenerator.writeEndObject();
    }
}
