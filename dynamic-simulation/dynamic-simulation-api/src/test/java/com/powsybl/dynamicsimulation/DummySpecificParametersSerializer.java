/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import com.google.auto.service.AutoService;
import com.powsybl.dynamicsimulation.json.JsonDynamicSimulationParameters;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
@AutoService(JsonDynamicSimulationParameters.ExtensionSerializer.class)
class DummySpecificParametersSerializer implements JsonDynamicSimulationParameters.ExtensionSerializer<DummyExtension> {

    @Override
    public void serialize(DummyExtension extension, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeEndObject();
    }

    @Override
    public DummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        return new DummyExtension();
    }

    @Override
    public String getExtensionName() {
        return "dynamic-simulation-dummy-extension";
    }

    @Override
    public String getCategoryName() {
        return "dynamic-simulation-parameters";
    }

    @Override
    public Class<? super DummyExtension> getExtensionClass() {
        return DummyExtension.class;
    }
}
