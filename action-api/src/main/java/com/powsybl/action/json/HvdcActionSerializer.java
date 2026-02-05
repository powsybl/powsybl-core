/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.HvdcAction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class HvdcActionSerializer extends StdSerializer<HvdcAction> {
    public HvdcActionSerializer() {
        super(HvdcAction.class);
    }

    @Override
    public void serialize(HvdcAction action, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", action.getType());
        jsonGenerator.writeStringProperty("id", action.getId());
        jsonGenerator.writeStringProperty("hvdcId", action.getHvdcId());
        JsonUtil.writeOptionalBoolean(jsonGenerator, "acEmulationEnabled", action.isAcEmulationEnabled());
        JsonUtil.writeOptionalDouble(jsonGenerator, "activePowerSetpoint", action.getActivePowerSetpoint());
        JsonUtil.writeOptionalEnum(jsonGenerator, "converterMode", action.getConverterMode());
        JsonUtil.writeOptionalDouble(jsonGenerator, "droop", action.getDroop());
        JsonUtil.writeOptionalDouble(jsonGenerator, "p0", action.getP0());
        JsonUtil.writeOptionalBoolean(jsonGenerator, "relativeValue", action.isRelativeValue());
        jsonGenerator.writeEndObject();
    }
}
