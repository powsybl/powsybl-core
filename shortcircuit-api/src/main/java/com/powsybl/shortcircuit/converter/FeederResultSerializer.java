/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.shortcircuit.FeederResult;

import java.io.IOException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FeederResultSerializer extends StdSerializer<FeederResult> {

    public FeederResultSerializer() {
        super(FeederResult.class);
    }

    @Override
    public void serialize(FeederResult result, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("connectableId", result.getConnectableId());
        if (result.getCurrent() != null) {
            jsonGenerator.writeObjectField("current", result.getCurrent());
        }
        jsonGenerator.writeEndObject();
    }
}
