/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.extensions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.service.AutoService;
import com.powsybl.commons.json.JsonUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
@AutoService(ExtensionJsonSerializer.class)
public class BarExtSerializer implements ExtensionJsonSerializer<Foo, BarExt> {

    @Override
    public void serialize(BarExt extension, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeBooleanProperty("value", extension.getValue());
        jsonGenerator.writeEndObject();
    }

    @Override
    public BarExt deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        // does nothing
        return null;
    }

    private interface SerializationSpec {

        @JsonIgnore
        String getName();

        @JsonIgnore
        Foo getExtendable();
    }

    private static JsonMapper createMapper() {
        return JsonUtil.createJsonMapperBuilder()
            .addMixIn(BarExt.class, SerializationSpec.class)
            .build();
    }

    @Override
    public BarExt deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext, BarExt parameters) throws JacksonException {
        JsonMapper jsonMapper = createMapper();
        ObjectReader objectReader = jsonMapper.readerForUpdating(parameters);
        BarExt updatedParameters = objectReader.readValue(jsonParser);
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
