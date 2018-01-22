/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.extensions;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.security.LimitViolation;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(ExtensionJsonSerializer.class)
public class PreContingencyJson implements ExtensionJsonSerializer<LimitViolation, PreContingency> {

    @Override
    public String getExtensionName() {
        return "PreContingency";
    }

    @Override
    public String getCategoryName() {
        return "security-analysis";
    }

    @Override
    public Class<? super PreContingency> getExtensionClass() {
        return PreContingency.class;
    }

    @Override
    public void serialize(PreContingency extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("value", extension.getPreContingencyValue());
        jsonGenerator.writeEndObject();
    }

    @Override
    public PreContingency deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        float value = Float.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentName().equals("value")) {
                parser.nextToken();
                value = parser.readValueAs(Float.class);
            } else {
                throw new PowsyblException("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new PreContingency(value);
    }
}
