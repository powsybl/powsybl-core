/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.AbstractLoadAction;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

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
    public void serialize(T action, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("type", action.getType());
        jsonGenerator.writeStringProperty("id", action.getId());
        jsonGenerator.writeStringProperty(getElementIdAttributeName(), getElementId(action));
        jsonGenerator.writeBooleanProperty("relativeValue", action.isRelativeValue());
        action.getActivePowerValue().ifPresent(activePowerValue -> {
            jsonGenerator.writeNumberProperty("activePowerValue", activePowerValue);
        });
        action.getReactivePowerValue().ifPresent(reactivePowerValue -> {
            jsonGenerator.writeNumberProperty("reactivePowerValue", reactivePowerValue);
        });
        jsonGenerator.writeEndObject();
    }
}
