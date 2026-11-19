/*
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.AreaInterchangeTargetAction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AreaInterchangeTargetActionSerializer extends StdSerializer<AreaInterchangeTargetAction> {

    AreaInterchangeTargetActionSerializer() {
        super(AreaInterchangeTargetAction.class);
    }

    @Override
    public void serialize(AreaInterchangeTargetAction action, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", action.getType());
        jsonGenerator.writeStringProperty("id", action.getId());
        jsonGenerator.writeStringProperty("areaId", action.getAreaId());
        if (!Double.isNaN(action.getInterchangeTarget())) {
            jsonGenerator.writeNumberProperty("interchangeTarget", action.getInterchangeTarget());
        }
        jsonGenerator.writeEndObject();
    }
}
