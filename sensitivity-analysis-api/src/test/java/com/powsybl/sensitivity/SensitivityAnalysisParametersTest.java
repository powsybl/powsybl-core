/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.sensitivity.json.JsonSensitivityAnalysisParameters;
import com.powsybl.sensitivity.json.SensitivityJsonModule;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class SensitivityAnalysisParametersTest extends AbstractSerDeTest {

    private static final String DUMMY_EXTENSION_NAME = "dummy-extension";

    private final ObjectMapper objectMapper = JsonSensitivityAnalysisParameters.createObjectMapper();

    static class DummyExtension extends AbstractExtension<SensitivityAnalysisParameters> {
        double parameterDouble;
        boolean parameterBoolean;
        String parameterString;

        DummyExtension() {
            super();
        }

        DummyExtension(DummyExtension another) {
            this.parameterDouble = another.parameterDouble;
            this.parameterBoolean = another.parameterBoolean;
            this.parameterString = another.parameterString;
        }

        boolean isParameterBoolean() {
            return parameterBoolean;
        }

        double getParameterDouble() {
            return parameterDouble;
        }

        String getParameterString() {
            return parameterString;
        }

        void setParameterBoolean(boolean parameterBoolean) {
            this.parameterBoolean = parameterBoolean;
        }

        void setParameterString(String parameterString) {
            this.parameterString = parameterString;
        }

        void setParameterDouble(double parameterDouble) {
            this.parameterDouble = parameterDouble;
        }

        @Override
        public String getName() {
            return DUMMY_EXTENSION_NAME;
        }
    }

    static class DummySerializer implements ExtensionJsonSerializer<SensitivityAnalysisParameters, DummyExtension> {
        private interface SerializationSpec {

            @JsonIgnore
            String getName();

            @JsonIgnore
            SensitivityAnalysisParameters getExtendable();
        }

        private static ObjectMapper createMapper() {
            return JsonUtil.createObjectMapper()
                    .addMixIn(DummyExtension.class, SerializationSpec.class);
        }

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
        public DummyExtension deserializeAndUpdate(JsonParser jsonParser, DeserializationContext deserializationContext, DummyExtension parameters) throws IOException {
            ObjectMapper objectMapper = createMapper();
            ObjectReader objectReader = objectMapper.readerForUpdating(parameters);
            return objectReader.readValue(jsonParser, DummyExtension.class);
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

    @Test
    void testExtensions() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertTrue(parameters.getExtensionByName(DUMMY_EXTENSION_NAME) instanceof DummyExtension);
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    void testNoExtensions() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertFalse(parameters.getExtensionByName(DUMMY_EXTENSION_NAME) instanceof DummyExtension);
        assertNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    void testExtensionFromConfig() {
        SensitivityAnalysisParameters parameters = SensitivityAnalysisParameters.load();
        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensionByName(DUMMY_EXTENSION_NAME) instanceof DummyExtension);
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    void roundTrip() throws IOException {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        roundTripTest(parameters, (parameters1, jsonFile) -> JsonUtil.writeJson(jsonFile, parameters1, objectMapper),
            jsonFile -> JsonUtil.readJsonAndUpdate(jsonFile, new SensitivityAnalysisParameters(), objectMapper),
            "/SensitivityAnalysisParameters.json");
    }

    @Test
    void writeExtension() throws IOException {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, (parameters1, path) -> JsonUtil.writeJson(path, parameters1, objectMapper),
                ComparisonUtils::assertTxtEquals, "/SensitivityAnalysisParametersWithExtension.json");
    }

    @Test
    void updateLoadFlowParameters() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        parameters.getLoadFlowParameters().setTwtSplitShuntAdmittance(true);
        JsonUtil.readJsonAndUpdate(getClass().getResourceAsStream("/SensitivityAnalysisParametersIncomplete.json"), parameters, objectMapper);

        assertTrue(parameters.getLoadFlowParameters().isTwtSplitShuntAdmittance());
    }

    @Test
    void readExtension() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        JsonUtil.readJsonAndUpdate(getClass().getResourceAsStream("/SensitivityAnalysisParametersWithExtension.json"), parameters, objectMapper);
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    void updateExtensions() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        DummyExtension extension = new DummyExtension();
        extension.setParameterBoolean(false);
        extension.setParameterString("test");
        extension.setParameterDouble(2.8);
        DummyExtension oldExtension = new DummyExtension(extension);
        parameters.addExtension(DummyExtension.class, extension);
        JsonUtil.readJsonAndUpdate(getClass().getResourceAsStream("/SensitivityAnalysisParametersExtensionUpdate.json"), parameters, objectMapper);
        DummyExtension updatedExtension = parameters.getExtension(DummyExtension.class);
        assertEquals(oldExtension.isParameterBoolean(), updatedExtension.isParameterBoolean());
        assertEquals(oldExtension.getParameterDouble(), updatedExtension.getParameterDouble(), 0.01);
        assertNotEquals(oldExtension.getParameterString(), updatedExtension.getParameterString());
    }

    @Test
    void readError() throws IOException {
        try (var is = getClass().getResourceAsStream("/SensitivityAnalysisParametersInvalid.json")) {
            SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> JsonUtil.readJsonAndUpdate(is, parameters, objectMapper));
            assertEquals("Unexpected field: unexpected", e.getMessage());
        }
    }

    @Test
    void testSensitivityAnalysisResultContingencyStatusSerializer() throws IOException {
        SensitivityAnalysisResult.SensitivityContingencyStatus value = new SensitivityAnalysisResult.SensitivityContingencyStatus("C1", SensitivityAnalysisResult.Status.SUCCESS);
        ObjectMapper objectMapper = JsonUtil.createObjectMapper().registerModule(new SensitivityJsonModule());
        roundTripTest(value, (value2, jsonFile) -> JsonUtil.writeJson(jsonFile, value, objectMapper),
            jsonFile -> JsonUtil.readJson(jsonFile, SensitivityAnalysisResult.SensitivityContingencyStatus.class, objectMapper), "/contingencyStatusRef.json");
    }

    @Test
    void updateThresholdParameters() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();

        assertEquals(0.0, parameters.getFlowFlowSensitivityValueThreshold(), 1e-3);
        assertEquals(0.0, parameters.getAngleFlowSensitivityValueThreshold(), 1e-3);
        assertEquals(0.0, parameters.getFlowVoltageSensitivityValueThreshold(), 1e-3);
        assertEquals(0.0, parameters.getVoltageVoltageSensitivityValueThreshold(), 1e-3);

        parameters.setFlowFlowSensitivityValueThreshold(0.1)
                .setAngleFlowSensitivityValueThreshold(0.2)
                .setFlowVoltageSensitivityValueThreshold(0.3)
                .setVoltageVoltageSensitivityValueThreshold(0.4);

        assertEquals(0.1, parameters.getFlowFlowSensitivityValueThreshold(), 1e-3);
        assertEquals(0.2, parameters.getAngleFlowSensitivityValueThreshold(), 1e-3);
        assertEquals(0.3, parameters.getFlowVoltageSensitivityValueThreshold(), 1e-3);
        assertEquals(0.4, parameters.getVoltageVoltageSensitivityValueThreshold(), 1e-3);
    }

    @Test
    void readJsonVersion10() {
        SensitivityAnalysisParameters parameters = JsonSensitivityAnalysisParameters
                .read(getClass().getResourceAsStream("/SensitivityAnalysisParametersV1.0.json"));
        assertEquals(0.0, parameters.getFlowFlowSensitivityValueThreshold(), 0.0001);
    }

    @Test
    void readJsonVersion10Invalid() {
        assertThrows(PowsyblException.class, () -> JsonSensitivityAnalysisParameters
                        .read(getClass().getResourceAsStream("/SensitivityAnalysisParametersV1.0Invalid.json")),
                "SensitivityAnalysisParameters. flow-flow-sensitivity-value-threshold is not valid for version 1.0. Version should be >= 1.1");
    }
}
