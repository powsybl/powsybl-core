/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.GeneratorAction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class GeneratorActionSerializer extends StdSerializer<GeneratorAction> {

    public GeneratorActionSerializer() {
        super(GeneratorAction.class);
    }

    @Override
    public void serialize(GeneratorAction action, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", action.getType());
        jsonGenerator.writeStringProperty("id", action.getId());
        jsonGenerator.writeStringProperty("generatorId", action.getGeneratorId());
        action.isActivePowerRelativeValue().ifPresent(activePowerRelativeValue ->
            jsonGenerator.writeBooleanProperty("activePowerRelativeValue", activePowerRelativeValue));
        action.getActivePowerValue().ifPresent(activePowerValue ->
            jsonGenerator.writeNumberProperty("activePowerValue", activePowerValue));
        action.isVoltageRegulatorOn().ifPresent(voltageControlOn ->
            jsonGenerator.writeBooleanProperty("voltageRegulatorOn", voltageControlOn));
        action.getTargetV().ifPresent(targetV ->
            jsonGenerator.writeNumberProperty("targetV", targetV));
        action.getTargetQ().ifPresent(targetQ ->
            jsonGenerator.writeNumberProperty("targetQ", targetQ));
        jsonGenerator.writeEndObject();
    }
}
