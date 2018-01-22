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
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.security.LimitViolation;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(ExtensionJsonSerializer.class)
public class ExtraValuesJson implements ExtensionJsonSerializer<LimitViolation, ExtraValues> {

    @Override
    public String getExtensionName() {
        return "ExtraValues";
    }

    @Override
    public String getCategoryName() {
        return "security-analysis";
    }

    @Override
    public Class<? super ExtraValues> getExtensionClass() {
        return ExtraValues.class;
    }

    @Override
    public void serialize(ExtraValues extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("extraValue1", extension.getExtraValue1());
        jsonGenerator.writeNumberField("extraValue2", extension.getExtraValue2());
        jsonGenerator.writeEndObject();
    }

    @Override
    public ExtraValues deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        float extraValue1 = Float.NaN;
        float extraValue2 = Float.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "extraValue1":
                    parser.nextToken();
                    extraValue1 = parser.readValueAs(Float.class);
                    break;

                case "extraValue2":
                    parser.nextToken();
                    extraValue2 = parser.readValueAs(Float.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new ExtraValues(extraValue1, extraValue2);
    }
}
