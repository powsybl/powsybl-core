/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.HvdcAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class HvdcActionSerializer extends StdSerializer<HvdcAction> {
    public HvdcActionSerializer() {
        super(HvdcAction.class);
    }

    @Override
    public void serialize(HvdcAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("hvdcId", action.getHvdcId());
        JsonUtil.writeOptionalBoolean(jsonGenerator, "acEmulationEnabled", action.isAcEmulationEnabled());
        JsonUtil.writeOptionalDouble(jsonGenerator, "activePowerSetpoint", action.getActivePowerSetpoint());
        JsonUtil.writeOptionalEnum(jsonGenerator, "converterMode", action.getConverterMode());
        JsonUtil.writeOptionalDouble(jsonGenerator, "droop", action.getDroop());
        JsonUtil.writeOptionalDouble(jsonGenerator, "p0", action.getP0());
        JsonUtil.writeOptionalBoolean(jsonGenerator, "relativeValue", action.isRelativeValue());
        jsonGenerator.writeEndObject();
    }
}
