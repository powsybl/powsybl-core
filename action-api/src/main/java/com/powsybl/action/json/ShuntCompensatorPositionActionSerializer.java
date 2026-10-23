/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.ShuntCompensatorPositionAction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ShuntCompensatorPositionActionSerializer extends StdSerializer<ShuntCompensatorPositionAction> {

    public ShuntCompensatorPositionActionSerializer() {
        super(ShuntCompensatorPositionAction.class);
    }

    @Override
    public void serialize(ShuntCompensatorPositionAction action, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", action.getType());
        jsonGenerator.writeStringProperty("id", action.getId());
        jsonGenerator.writeStringProperty("shuntCompensatorId", action.getShuntCompensatorId());
        jsonGenerator.writeNumberProperty("sectionCount", action.getSectionCount());
        jsonGenerator.writeEndObject();
    }
}
