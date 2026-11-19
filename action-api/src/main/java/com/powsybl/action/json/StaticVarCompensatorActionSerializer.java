/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.StaticVarCompensatorAction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class StaticVarCompensatorActionSerializer extends StdSerializer<StaticVarCompensatorAction> {

    public StaticVarCompensatorActionSerializer() {
        super(StaticVarCompensatorAction.class);
    }

    @Override
    public void serialize(StaticVarCompensatorAction action, JsonGenerator jsonGenerator,
                          SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", action.getType());
        jsonGenerator.writeStringProperty("id", action.getId());
        jsonGenerator.writeStringProperty("staticVarCompensatorId", action.getStaticVarCompensatorId());
        action.getRegulationMode().ifPresent(regulationMode ->
            jsonGenerator.writeStringProperty("regulationMode", String.valueOf(regulationMode)));
        action.getReactivePowerSetpoint().ifPresent(reactivePowerSetpoint ->
            jsonGenerator.writeNumberProperty("reactivePowerSetpoint", reactivePowerSetpoint));
        action.getVoltageSetpoint().ifPresent(voltageSetpoint ->
            jsonGenerator.writeNumberProperty("voltageSetpoint", voltageSetpoint));
        jsonGenerator.writeEndObject();
    }
}
