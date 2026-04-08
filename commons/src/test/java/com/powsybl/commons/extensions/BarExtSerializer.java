/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionJsonSerializer.class)
public class BarExtSerializer implements ExtensionJsonSerializer<Foo, BarExt> {

    @Override
    public void serialize(BarExt extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBooleanField("value", extension.getValue());
        jsonGenerator.writeEndObject();
    }

    @Override
    public BarExt deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        // does nothing
        return null;
    }

    private interface SerializationSpec {

        @JsonIgnore
        String getName();

        @JsonIgnore
        Foo getExtendable();
    }

    private static ObjectMapper createMapper() {
        return JsonUtil.createObjectMapper()
                .addMixIn(BarExt.class, SerializationSpec.class);
    }

    @Override
    public BarExt deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext, BarExt parameters) throws IOException {
        ObjectMapper objectMapper = createMapper();
        ObjectReader objectReader = objectMapper.readerForUpdating(parameters);
        BarExt updatedParameters = objectReader.readValue(jsonParser, BarExt.class);
        return updatedParameters;
    }

    @Override
    public String getExtensionName() {
        return "BarExt";
    }

    @Override
    public String getCategoryName() {
        return "test";
    }

    @Override
    public Class<? super BarExt> getExtensionClass() {
        return BarExt.class;
    }
}
