/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.ComparisonUtils;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class JsonDynamicSimulationParametersTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters()
                .setStartTime(0)
                .setStopTime(1);
        roundTripTest(parameters, JsonDynamicSimulationParameters::write, JsonDynamicSimulationParameters::read, "/DynamicSimulationParameters.json");
    }

    @Test
    public void writeExtension() throws IOException {
        DynamicSimulationParameters parameters = new DynamicSimulationParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonDynamicSimulationParameters::write, ComparisonUtils::compareTxt, "/DynamicSimulationParametersWithExtension.json");
    }

    @Test
    public void readExtension() {
        DynamicSimulationParameters parameters = JsonDynamicSimulationParameters.read(getClass().getResourceAsStream("/DynamicSimulationParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    public void readError() {
        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unknownParameter");
        JsonDynamicSimulationParameters.read(getClass().getResourceAsStream("/DynamicSimulationParametersError.json"));
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
