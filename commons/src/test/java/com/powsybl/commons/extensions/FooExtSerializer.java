/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
@AutoService(ExtensionJsonSerializer.class)
public class FooExtSerializer implements ExtensionJsonSerializer<Foo, FooExt> {

    @Override
    public String getExtensionName() {
        return "FooExt";
    }

    @Override
    public String getCategoryName() {
        return "test";
    }

    @Override
    public Class<? super FooExt> getExtensionClass() {
        return FooExt.class;
    }

    @Override
    public void serialize(FooExt extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBooleanField("value", extension.getValue());
        jsonGenerator.writeEndObject();
    }

    @Override
    public FooExt deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        Boolean value = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentName().equals("value")) {
                parser.nextToken();
                value = parser.readValueAs(Boolean.class);
            } else {
                throw new PowsyblException("Unexpected field: " + parser.currentName());
            }
        }

        if (value == null) {
            throw new PowsyblException("Value has not been read");
        }

        return new FooExt(value);
    }

    private interface SerializationSpec {

        @JsonIgnore
        String getName();

        @JsonIgnore
        Foo getExtendable();
    }

    private static ObjectMapper createMapper() {
        return JsonUtil.createObjectMapper()
                .addMixIn(FooExt.class, SerializationSpec.class);
    }

    @Override
    public FooExt deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext, FooExt parameters) throws IOException {
        ObjectMapper objectMapper = createMapper();
        ObjectReader objectReader = objectMapper.readerForUpdating(parameters);
        FooExt updatedParameters = objectReader.readValue(jsonParser, FooExt.class);
        return updatedParameters;
    }
}
