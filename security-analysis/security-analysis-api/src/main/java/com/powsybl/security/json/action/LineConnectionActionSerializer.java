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
import com.powsybl.security.action.LineConnectionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class LineConnectionActionSerializer extends StdSerializer<LineConnectionAction> {

    public LineConnectionActionSerializer() {
        super(LineConnectionAction.class);
    }

    @Override
    public void serialize(LineConnectionAction action, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", action.getType());
        jsonGenerator.writeStringField("id", action.getId());
        jsonGenerator.writeStringField("lineId", action.getLineId());
        jsonGenerator.writeBooleanField("openSide1", action.isOpenSide1());
        jsonGenerator.writeBooleanField("openSide2", action.isOpenSide2());
        jsonGenerator.writeEndObject();
    }
}
