/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.shortcircuit.ShortCircuitInput;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShortCircuitInputSerializer extends StdSerializer<ShortCircuitInput> {

    public ShortCircuitInputSerializer() {
        super(ShortCircuitInput.class);
    }

    @Override
    public void serialize(ShortCircuitInput input, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", ShortCircuitInput.VERSION);
        writeOptionalListField(jsonGenerator, "faults", input.getFaults());
        jsonGenerator.writeEndObject();
    }

    public static <T> void writeOptionalListField(JsonGenerator jsonGenerator, String fieldName, List<T> values) throws IOException {
        Objects.requireNonNull(jsonGenerator);
        Objects.requireNonNull(fieldName);

        if (!values.isEmpty()) {
            jsonGenerator.writeArrayFieldStart(fieldName);
            for (T value : values) {
                jsonGenerator.writeObject(value);
            }
            jsonGenerator.writeEndArray();
        }
    }
}
