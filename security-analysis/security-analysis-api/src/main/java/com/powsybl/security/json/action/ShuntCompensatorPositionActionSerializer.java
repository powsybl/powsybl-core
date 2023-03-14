/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.action.ShuntCompensatorPositionAction;

import java.io.IOException;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class ShuntCompensatorPositionActionSerializer extends StdSerializer<ShuntCompensatorPositionAction> {

    public ShuntCompensatorPositionActionSerializer() {
        super(ShuntCompensatorPositionAction.class);
    }

    @Override
    public void serialize(ShuntCompensatorPositionAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("shuntCompensatorId", action.getShuntCompensatorId());
        jsonGenerator.writeNumberField("sectionCount", action.getSectionCount());
        jsonGenerator.writeEndObject();
    }
}
