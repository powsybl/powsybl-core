/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.action.GeneratorAction;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Anne Tilloy <anne.tilloy@rte-france.com>
 */
public class GeneratorActionSerializer extends StdSerializer<GeneratorAction> {

    public GeneratorActionSerializer() {
        super(GeneratorAction.class);
    }

    @Override
    public void serialize(GeneratorAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("generatorId", ((GeneratorAction) action).getGeneratorId());
        action.isActivePowerRelativeValue().ifPresent(activePowerRelativeValue -> {
            try {
                jsonGenerator.writeStringField("activePowerRelativeValue", activePowerRelativeValue.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        action.getActivePowerValue().ifPresent(activePowerValue -> {
            try {
                jsonGenerator.writeStringField("activePowerValue", activePowerValue.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        action.isVoltageRegulatorOn().ifPresent(voltageControlOn -> {
            try {
                jsonGenerator.writeStringField("voltageRegulatorOn", voltageControlOn.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        action.getTargetV().ifPresent(targetV -> {
            try {
                jsonGenerator.writeStringField("targetV", targetV.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        action.getTargetQ().ifPresent(targetQ -> {
            try {
                jsonGenerator.writeStringField("targetQ", targetQ.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        jsonGenerator.writeEndObject();
    }
}
