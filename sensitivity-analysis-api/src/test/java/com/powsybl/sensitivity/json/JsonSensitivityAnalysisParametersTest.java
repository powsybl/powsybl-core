/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.SensitivityAnalysisParameters;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class JsonSensitivityAnalysisParametersTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        roundTripTest(parameters, JsonSensitivityAnalysisParameters::write, JsonSensitivityAnalysisParameters::read, "/SensitivityAnalysisParameters.json");
    }

    @Test
    public void writeExtension() throws IOException {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonSensitivityAnalysisParameters::write, AbstractConverterTest::compareTxt, "/SensitivityAnalysisParametersWithExtension.json");
    }

    @Test
    public void updateLoadFlowParameters() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        parameters.getLoadFlowParameters().setTwtSplitShuntAdmittance(true);
        JsonSensitivityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/SensitivityAnalysisParametersIncomplete.json"));

        assertTrue(parameters.getLoadFlowParameters().isTwtSplitShuntAdmittance());
    }

    @Test
    public void readExtension() {
        SensitivityAnalysisParameters parameters = JsonSensitivityAnalysisParameters.read(getClass().getResourceAsStream("/SensitivityAnalysisParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    public void updateExtensions() {
        SensitivityAnalysisParameters parameters = new SensitivityAnalysisParameters();
        DummyExtension extension = new DummyExtension();
        extension.setParameterBoolean(false);
        extension.setParameterString("test");
        extension.setParameterDouble(2.8);
        DummyExtension oldExtension = new DummyExtension(extension);
        parameters.addExtension(DummyExtension.class, extension);
        JsonSensitivityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/SensitivityAnalysisParametersExtensionUpdate.json"));
        DummyExtension updatedExtension = parameters.getExtension(DummyExtension.class);
        assertEquals(oldExtension.isParameterBoolean(), updatedExtension.isParameterBoolean());
        assertEquals(oldExtension.getParameterDouble(), updatedExtension.getParameterDouble(), 0.01);
        assertNotEquals(oldExtension.getParameterString(), updatedExtension.getParameterString());
    }

    @Test
    public void readError() {
        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unexpected");
        JsonSensitivityAnalysisParameters.read(getClass().getResourceAsStream("/SensitivityAnalysisParametersInvalid.json"));
    }

    static class DummyExtension extends AbstractExtension<SensitivityAnalysisParameters> {
        public double parameterDouble;
        public boolean parameterBoolean;
        public String parameterString;

        DummyExtension() {
            super();
        }

        DummyExtension(DummyExtension another) {
            this.parameterDouble = another.parameterDouble;
            this.parameterBoolean = another.parameterBoolean;
            this.parameterString = another.parameterString;
        }

        public boolean isParameterBoolean() {
            return parameterBoolean;
        }

        public double getParameterDouble() {
            return parameterDouble;
        }

        public String getParameterString() {
            return parameterString;
        }

        public void setParameterBoolean(boolean parameterBoolean) {
            this.parameterBoolean = parameterBoolean;
        }

        public void setParameterString(String parameterString) {
            this.parameterString = parameterString;
        }

        public void setParameterDouble(double parameterDouble) {
            this.parameterDouble = parameterDouble;
        }

        @Override
        public String getName() {
            return "dummy-extension";
        }
    }

    @AutoService(JsonSensitivityAnalysisParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonSensitivityAnalysisParameters.ExtensionSerializer<DummyExtension> {
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
            DummyExtension updatedParameters = objectReader.readValue(jsonParser, DummyExtension.class);
            return updatedParameters;
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
