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
import com.powsybl.action.AbstractLoadAction;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractLoadActionSerializer<T extends AbstractLoadAction> extends StdSerializer<T> {

    protected AbstractLoadActionSerializer(Class<T> vc) {
        super(vc);
    }

    protected abstract String getElementIdAttributeName();

    protected abstract String getElementId(T action);

    @Override
    public void serialize(T action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField(getElementIdAttributeName(), getElementId(action));
        jsonGenerator.writeBooleanField("relativeValue", action.isRelativeValue());
        action.getActivePowerValue().ifPresent(activePowerValue -> {
            try {
                jsonGenerator.writeNumberField("activePowerValue", activePowerValue);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        action.getReactivePowerValue().ifPresent(reactivePowerValue -> {
            try {
                jsonGenerator.writeNumberField("reactivePowerValue", reactivePowerValue);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        jsonGenerator.writeEndObject();
    }
}
