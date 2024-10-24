/**
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
import com.powsybl.action.PctLoadAction;

import java.io.IOException;

/**
 * @author Benoît Chiquet {@literal <benoit.chiquet@rte-france.com>}
 */
public class PctLoadActionSerializer extends StdSerializer<PctLoadAction> {

    public PctLoadActionSerializer() {
        super(PctLoadAction.class);
    }

    @Override
    public void serialize(PctLoadAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("loadId", action.getLoadId());
        jsonGenerator.writeNumberField("pctPChange", action.getPctPChange());
        jsonGenerator.writeStringField("qStrategy", action.getQStrategy().toString());
        jsonGenerator.writeEndObject();
    }
}
