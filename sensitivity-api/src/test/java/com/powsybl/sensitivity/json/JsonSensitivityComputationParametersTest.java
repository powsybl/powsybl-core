/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.sensitivity.SensitivityComputationParameters;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Sebastien Murgey <sebastien.murgey@rte-france.com>
 */
public class JsonSensitivityComputationParametersTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        SensitivityComputationParameters parameters = new SensitivityComputationParameters();
        roundTripTest(parameters, JsonSensitivityComputationParameters::write, JsonSensitivityComputationParameters::read, "/SensitivityComputationParameters.json");
    }

    @Test
    public void writeExtension() throws IOException {
        SensitivityComputationParameters parameters = new SensitivityComputationParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonSensitivityComputationParameters::write, AbstractConverterTest::compareTxt, "/SensitivityComputationParametersWithExtension.json");
    }

    @Test
    public void updateLoadFlowParameters() {
        SensitivityComputationParameters parameters = new SensitivityComputationParameters();
        parameters.getLoadFlowParameters().setSpecificCompatibility(true);
        JsonSensitivityComputationParameters.update(parameters, getClass().getResourceAsStream("/SensitivityComputationParametersIncomplete.json"));

        assertTrue(parameters.getLoadFlowParameters().isSpecificCompatibility());
    }

    @Test
    public void readExtension() {
        SensitivityComputationParameters parameters = JsonSensitivityComputationParameters.read(getClass().getResourceAsStream("/SensitivityComputationParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    public void readError() {
        try {
            JsonSensitivityComputationParameters.read(getClass().getResourceAsStream("/SensitivityComputationParametersWithExtension.json"));
            Assert.fail();
        } catch (AssertionError ignored) {
        }
    }

    static class DummyExtension extends AbstractExtension<SensitivityComputationParameters> {

        @Override
        public String getName() {
            return "dummy-extension";
        }
    }

    @AutoService(JsonSensitivityComputationParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonSensitivityComputationParameters.ExtensionSerializer<DummyExtension> {

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
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
            return "sensitivity-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

}
