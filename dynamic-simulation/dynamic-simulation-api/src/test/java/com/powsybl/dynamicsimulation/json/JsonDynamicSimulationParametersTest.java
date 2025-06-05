/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class JsonDynamicSimulationParametersTest extends AbstractSerDeTest {

    @Test
    void roundTrip() throws IOException {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters()
                .setStartTime(0)
                .setStopTime(1)
                .setDebugDir("/tmp/debugDir");
        roundTripTest(parameters, JsonDynamicSimulationParameters::write, JsonDynamicSimulationParameters::read, "/DynamicSimulationParametersV11.json");
    }

    @Test
    void readVersion10() {
        DynamicSimulationParameters parameters = JsonDynamicSimulationParameters
                .read(getClass().getResourceAsStream("/DynamicSimulationParametersV10.json"));
        assertNotNull(parameters);
        assertEquals(0.0, parameters.getStartTime());
        assertEquals(1.0, parameters.getStopTime());
    }

    @Test
    void writeExtension() throws IOException {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonDynamicSimulationParameters::write, ComparisonUtils::assertTxtEquals, "/DynamicSimulationParametersWithExtension.json");
    }

    @Test
    void readExtension() {
        DynamicSimulationParameters parameters = JsonDynamicSimulationParameters.read(getClass().getResourceAsStream("/DynamicSimulationParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    void readError() throws IOException {
        try (var is = getClass().getResourceAsStream("/DynamicSimulationParametersError.json")) {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> JsonDynamicSimulationParameters.read(is));
            assertEquals("Unexpected field: unknownParameter", e.getMessage());
        }
    }

    static class DummyExtension extends AbstractExtension<DynamicSimulationParameters> {

        DummyExtension() {
            super();
        }

        /**
         * Return the name of this extension.
         */
        @Override
        public String getName() {
            return "dummy-extension";
        }
    }

    @AutoService(JsonDynamicSimulationParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonDynamicSimulationParameters.ExtensionSerializer<DummyExtension> {

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }

        @Override
        public DummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return new DummyExtension();
        }

        @Override
        public String getExtensionName() {
            return "dummy-extension";
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
}
