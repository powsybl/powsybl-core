/**
 * Copyright (c) 2026, Artelys (https://www.artelys.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extensions of {@link DynamicSimulationParameters} which are not the specific parameters of any provider.
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DynamicSimulationParametersNonProviderExtensionTest {

    @Test
    void jsonRoundTrip() {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        parameters.addExtension(BusinessExtension.class, new BusinessExtension().setValue("foo"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JsonDynamicSimulationParameters.write(parameters, os);

        DynamicSimulationParameters read = JsonDynamicSimulationParameters.read(new ByteArrayInputStream(os.toByteArray()));
        assertEquals("foo", read.getExtension(BusinessExtension.class).getValue());
    }

    public static class BusinessExtension extends AbstractExtension<DynamicSimulationParameters> {

        private String value = "default";

        @Override
        public String getName() {
            return "business-extension";
        }

        public String getValue() {
            return value;
        }

        public BusinessExtension setValue(String value) {
            this.value = value;
            return this;
        }
    }

    @AutoService(ExtensionJsonSerializer.class)
    public static class BusinessExtensionSerializer implements ExtensionJsonSerializer<DynamicSimulationParameters, BusinessExtension> {

        @Override
        public void serialize(BusinessExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("value", extension.getValue());
            jsonGenerator.writeEndObject();
        }

        @Override
        public BusinessExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            BusinessExtension extension = new BusinessExtension();
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                if ("value".equals(jsonParser.currentName())) {
                    extension.setValue(jsonParser.nextTextValue());
                }
            }
            return extension;
        }

        @Override
        public String getExtensionName() {
            return "business-extension";
        }

        @Override
        public String getCategoryName() {
            return DynamicSimulationParameters.EXTENSIONS_CATEGORY;
        }

        @Override
        public Class<? super BusinessExtension> getExtensionClass() {
            return BusinessExtension.class;
        }
    }
}
