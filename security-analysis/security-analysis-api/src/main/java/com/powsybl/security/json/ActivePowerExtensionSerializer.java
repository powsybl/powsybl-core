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
import com.powsybl.security.extensions.ActivePowerExtension;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
@AutoService(ExtensionJsonSerializer.class)
public class ActivePowerExtensionSerializer implements ExtensionJsonSerializer<LimitViolation, ActivePowerExtension> {

    @Override
    public String getExtensionName() {
        return "ActivePower";
    }

    @Override
    public String getCategoryName() {
        return "security-analysis";
    }

    @Override
    public Class<? super ActivePowerExtension> getExtensionClass() {
        return ActivePowerExtension.class;
    }

    @Override
    public void serialize(ActivePowerExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        if (!Double.isNaN(extension.getPostContingencyValue())) {
            jsonGenerator.writeNumberField("preContingencyValue", extension.getPreContingencyValue());
            jsonGenerator.writeNumberField("postContingencyValue", extension.getPostContingencyValue());
        } else {
            jsonGenerator.writeNumberField("value", extension.getPreContingencyValue());
        }
        jsonGenerator.writeEndObject();
    }

    @Override
    public ActivePowerExtension deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        double value = Double.NaN;
        double preContingencyValue = Double.NaN;
        double postContingencyValue = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentName().equals("value")) {
                parser.nextToken();
                value = parser.readValueAs(Double.class);
            } else if (parser.getCurrentName().equals("preContingencyValue")) {
                parser.nextToken();
                preContingencyValue = parser.readValueAs(Double.class);
            } else if (parser.getCurrentName().equals("postContingencyValue")) {
                parser.nextToken();
                postContingencyValue = parser.readValueAs(Double.class);
            } else {
                throw new PowsyblException("Unexpected field: " + parser.getCurrentName());
            }
        }

        if (!Double.isNaN(value)) {
            return new ActivePowerExtension(value);
        } else {
            return new ActivePowerExtension(preContingencyValue, postContingencyValue);
        }
    }
}
