/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.shortcircuit.ShortCircuitParameters;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Boubakeur Brahimi
 */
public class JsonShortCircuitParametersTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        roundTripTest(parameters, JsonShortCircuitParameters::write, JsonShortCircuitParameters::read,
                "/ShortCircuitParameters.json");
    }

    private static class DummyExtension extends AbstractExtension<ShortCircuitParameters> {

        @Override
        public String getName() {
            return "dummy-extension";
        }
    }

    @AutoService(JsonShortCircuitParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonShortCircuitParameters.ExtensionSerializer<DummyExtension> {

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator,
                SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }

        @Override
        public DummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
            return new DummyExtension();
        }

        @Override
        public String getExtensionName() {
            return "dummy-extension";
        }

        @Override
        public String getCategoryName() {
            return "short-circuit-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }
}
