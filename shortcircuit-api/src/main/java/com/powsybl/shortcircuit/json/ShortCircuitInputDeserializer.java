/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.shortcircuit.Fault;
import com.powsybl.shortcircuit.ShortCircuitInput;

import java.io.IOException;
import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShortCircuitInputDeserializer extends StdDeserializer<ShortCircuitInput> {

    public ShortCircuitInputDeserializer() {
        super(ShortCircuitInput.class);
    }

    @Override
    public ShortCircuitInput deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new ShortCircuitInput());
    }

    @Override
    public ShortCircuitInput deserialize(JsonParser parser, DeserializationContext deserializationContext, ShortCircuitInput input) throws IOException {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    break;
                case "faults":
                    parser.nextToken();
                    input.setFaults(parser.readValueAs(new TypeReference<List<Fault>>() { }));
                    break;
                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return input;
    }

}
