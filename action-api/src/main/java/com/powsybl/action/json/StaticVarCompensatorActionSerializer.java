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
import com.powsybl.action.StaticVarCompensatorAction;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class StaticVarCompensatorActionSerializer extends StdSerializer<StaticVarCompensatorAction> {

    public StaticVarCompensatorActionSerializer() {
        super(StaticVarCompensatorAction.class);
    }

    @Override
    public void serialize(StaticVarCompensatorAction action, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("staticVarCompensatorId", action.getStaticVarCompensatorId());
        action.getRegulationMode().ifPresent(regulationMode -> {
            try {
                jsonGenerator.writeStringField("regulationMode", String.valueOf(regulationMode));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        action.getReactivePowerSetpoint().ifPresent(reactivePowerSetpoint -> {
            try {
                jsonGenerator.writeNumberField("reactivePowerSetpoint", reactivePowerSetpoint);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        action.getVoltageSetpoint().ifPresent(voltageSetpoint -> {
            try {
                jsonGenerator.writeNumberField("voltageSetpoint", voltageSetpoint);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        jsonGenerator.writeEndObject();
    }
}
