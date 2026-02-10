/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;

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
    public void serialize(FooExt extension, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBooleanProperty("value", extension.getValue());
        jsonGenerator.writeEndObject();
    }

    @Override
    public FooExt deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
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

    private static JsonMapper createMapper() {
        return JsonUtil.createJsonMapperBuilder()
            .addMixIn(FooExt.class, SerializationSpec.class)
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();
    }

    @Override
    public FooExt deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext, FooExt parameters) throws JacksonException {
        JsonMapper jsonMapper = createMapper();
        ObjectReader objectReader = jsonMapper.readerForUpdating(parameters);
        return objectReader.readValue(jsonParser);
    }
}
