/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.shortcircuit.ShortCircuitAnalysisMultiResult;

import java.io.IOException;

/**
 * @author Teofil-Calin BANC <teofil-calin.banc at rte-france.com>
 */
@AutoService(ExtensionJsonSerializer.class)
public class DummyShortCircuitAnalysisResultExtensionSerializer implements ExtensionJsonSerializer<ShortCircuitAnalysisMultiResult, MultiShortCircuitAnalysisResultExportersTest.DummyShortCircuitAnalysisResultExtension> {

    @Override
    public String getExtensionName() {
        return "DummyShortCircuitAnalysisResultExtension";
    }

    @Override
    public String getCategoryName() {
        return "short-circuit-analysis";
    }

    @Override
    public Class<? super MultiShortCircuitAnalysisResultExportersTest.DummyShortCircuitAnalysisResultExtension> getExtensionClass() {
        return MultiShortCircuitAnalysisResultExportersTest.DummyShortCircuitAnalysisResultExtension.class;
    }

    @Override
    public void serialize(MultiShortCircuitAnalysisResultExportersTest.DummyShortCircuitAnalysisResultExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeEndObject();
    }

    @Override
    public MultiShortCircuitAnalysisResultExportersTest.DummyShortCircuitAnalysisResultExtension deserialize(JsonParser parser, DeserializationContext deserializationContext)  throws IOException {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            continue;
        }
        return new MultiShortCircuitAnalysisResultExportersTest.DummyShortCircuitAnalysisResultExtension();
    }
}
