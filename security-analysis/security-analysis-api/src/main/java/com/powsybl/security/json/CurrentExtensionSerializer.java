/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.extensions.CurrentExtension;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(ExtensionJsonSerializer.class)
public class CurrentExtensionSerializer implements ExtensionJsonSerializer<LimitViolation, CurrentExtension> {

    @Override
    public String getExtensionName() {
        return "Current";
    }

    @Override
    public String getCategoryName() {
        return "security-analysis";
    }

    @Override
    public Class<? super CurrentExtension> getExtensionClass() {
        return CurrentExtension.class;
    }

    @Override
    public void serialize(CurrentExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("preContingencyValue", extension.getPreContingencyValue());
        jsonGenerator.writeEndObject();
    }

    @Override
    public CurrentExtension deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        double value = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentName().equals("preContingencyValue")) {
                parser.nextToken();
                value = parser.readValueAs(Double.class);
            } else {
                throw new PowsyblException("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new CurrentExtension(value);
    }
}
