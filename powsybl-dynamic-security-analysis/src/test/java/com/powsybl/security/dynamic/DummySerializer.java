/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.dynamic.json.DynamicSecurityDummyExtension;

import java.io.IOException;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
class DummySerializer implements ExtensionJsonSerializer<DynamicSecurityAnalysisParameters, DynamicSecurityDummyExtension> {
    private interface SerializationSpec {

        @JsonIgnore
        String getName();

        @JsonIgnore
        DynamicSecurityAnalysisParameters getExtendable();
    }

    private static ObjectMapper createMapper() {
        return JsonUtil.createObjectMapper()
                .addMixIn(DynamicSecurityDummyExtension.class, SerializationSpec.class);
    }

    @Override
    public void serialize(DynamicSecurityDummyExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeEndObject();
    }

    @Override
    public DynamicSecurityDummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        return new DynamicSecurityDummyExtension();
    }

    @Override
    public DynamicSecurityDummyExtension deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext, DynamicSecurityDummyExtension parameters) throws IOException {
        ObjectMapper objectMapper = createMapper();
        ObjectReader objectReader = objectMapper.readerForUpdating(parameters);
        return objectReader.readValue(jsonParser, DynamicSecurityDummyExtension.class);
    }

    @Override
    public String getExtensionName() {
        return "dummy-extension";
    }

    @Override
    public String getCategoryName() {
        return "dynamic-security-analysis-parameters";
    }

    @Override
    public Class<? super DynamicSecurityDummyExtension> getExtensionClass() {
        return DynamicSecurityDummyExtension.class;
    }
}
