/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.action.AreaInterchangeTargetUpdateAction;

import java.io.IOException;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AreaInterchangeTargetUpdateActionSerializer extends StdSerializer<AreaInterchangeTargetUpdateAction> {

    AreaInterchangeTargetUpdateActionSerializer() {
        super(AreaInterchangeTargetUpdateAction.class);
    }

    @Override
    public void serialize(AreaInterchangeTargetUpdateAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("areaId", action.getAreaId());
        jsonGenerator.writeNumberField("target", action.getInterchangeTarget());
        jsonGenerator.writeEndObject();
    }
}
