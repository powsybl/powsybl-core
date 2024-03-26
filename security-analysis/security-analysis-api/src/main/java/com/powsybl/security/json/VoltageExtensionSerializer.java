/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
import com.powsybl.security.extensions.VoltageExtension;

import java.io.IOException;

/**
 * @author Olivier Bretteville {@literal <olivier.bretteville at rte-france.com>}
 */
@AutoService(ExtensionJsonSerializer.class)
public class VoltageExtensionSerializer implements ExtensionJsonSerializer<LimitViolation, VoltageExtension> {

    @Override
    public String getExtensionName() {
        return "Voltage";
    }

    @Override
    public String getCategoryName() {
        return "security-analysis";
    }

    @Override
    public Class<? super VoltageExtension> getExtensionClass() {
        return VoltageExtension.class;
    }

    @Override
    public void serialize(VoltageExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("preContingencyValue", extension.getPreContingencyValue());
        jsonGenerator.writeEndObject();
    }

    @Override
    public VoltageExtension deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        double value = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentName().equals("preContingencyValue")) {
                parser.nextToken();
                value = parser.readValueAs(Float.class);
            } else {
                throw new PowsyblException("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new VoltageExtension(value);
    }
}
