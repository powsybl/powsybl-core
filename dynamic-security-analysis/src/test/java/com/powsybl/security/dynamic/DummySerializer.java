/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.dynamic.json.DynamicSecurityDummyExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;

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

    private static JsonMapper createMapper() {
        return JsonUtil.createJsonMapperBuilder()
            .addMixIn(DynamicSecurityDummyExtension.class, SerializationSpec.class)
            .build();
    }

    @Override
    public void serialize(DynamicSecurityDummyExtension extension, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeEndObject();
    }

    @Override
    public DynamicSecurityDummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        return new DynamicSecurityDummyExtension();
    }

    @Override
    public DynamicSecurityDummyExtension deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext, DynamicSecurityDummyExtension parameters) throws JacksonException {
        JsonMapper jsonMapper = createMapper();
        ObjectReader objectReader = jsonMapper.readerForUpdating(parameters);
        return objectReader.readValue(jsonParser);
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
