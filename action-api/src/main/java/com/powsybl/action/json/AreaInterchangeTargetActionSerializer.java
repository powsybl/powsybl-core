/*
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.action.AreaInterchangeTargetAction;

import java.io.IOException;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AreaInterchangeTargetActionSerializer extends StdSerializer<AreaInterchangeTargetAction> {

    AreaInterchangeTargetActionSerializer() {
        super(AreaInterchangeTargetAction.class);
    }

    @Override
    public void serialize(AreaInterchangeTargetAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("areaId", action.getAreaId());
        if (!Double.isNaN(action.getInterchangeTarget())) {
            jsonGenerator.writeNumberField("interchangeTarget", action.getInterchangeTarget());
        }
        jsonGenerator.writeEndObject();
    }
}
