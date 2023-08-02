/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Laurent Issertial <laurent.issertial at rte-france.com>
 */
public class JsonDynamicSecurityAnalysisParametersTest extends AbstractConverterTest {

    @Test
    void roundTrip() throws IOException {
        DynamicSecurityAnalysisParameters parameters = new DynamicSecurityAnalysisParameters();
        parameters.getIncreasedViolationsParameters().setFlowProportionalThreshold(0.2);
        roundTripTest(parameters, JsonDynamicSecurityAnalysisParameters::write, JsonDynamicSecurityAnalysisParameters::read, "/DynamicSecurityAnalysisParametersV1.json");
    }

    @Test
    void writeExtension() throws IOException {
        DynamicSecurityAnalysisParameters parameters = new DynamicSecurityAnalysisParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonDynamicSecurityAnalysisParameters::write, ComparisonUtils::compareTxt, "/DynamicSecurityAnalysisParametersWithExtension.json");
    }

    @Test
    void updateDynamicSimulationParameters() {
        DynamicSecurityAnalysisParameters parameters = new DynamicSecurityAnalysisParameters();
        parameters.getDynamicSimulationParameters().setStopTime(3);
        JsonDynamicSecurityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/DynamicSecurityAnalysisParametersIncomplete.json"));
        assertEquals(3, parameters.getDynamicSimulationParameters().getStopTime());
    }

    @Test
    void readExtension() throws IOException {
        DynamicSecurityAnalysisParameters parameters = JsonDynamicSecurityAnalysisParameters.read(getClass().getResourceAsStream("/DynamicSecurityAnalysisParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    void readError() {
        InputStream inputStream = getClass().getResourceAsStream("/DynamicSecurityAnalysisParametersInvalid.json");
        assertThrows(IllegalStateException.class, () -> JsonDynamicSecurityAnalysisParameters.read(inputStream), "Unexpected field: unexpected");
    }

    @Test
    void updateExtensions() {
        DynamicSecurityAnalysisParameters parameters = new DynamicSecurityAnalysisParameters();
        DummyExtension extension = new DummyExtension();
        extension.setParameterBoolean(false);
        extension.setParameterString("test");
        extension.setParameterDouble(2.8);
        DummyExtension oldExtension = new DummyExtension(extension);
        parameters.addExtension(DummyExtension.class, extension);
        JsonDynamicSecurityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/DynamicSecurityAnalysisParametersExtensionUpdate.json"));
        DummyExtension updatedExtension = parameters.getExtension(DummyExtension.class);
        assertEquals(oldExtension.isParameterBoolean(), updatedExtension.isParameterBoolean());
        assertEquals(oldExtension.getParameterDouble(), updatedExtension.getParameterDouble(), 0.01);
        assertNotEquals(oldExtension.getParameterString(), updatedExtension.getParameterString());
    }

    public static class DummyExtension extends AbstractExtension<DynamicSecurityAnalysisParameters> {
        double parameterDouble;
        boolean parameterBoolean;
        String parameterString;

        public DummyExtension() {
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
            return "dummy-extension";
        }
    }

    public static class DummySerializer implements ExtensionJsonSerializer<DynamicSecurityAnalysisParameters, DummyExtension> {
        private interface SerializationSpec {

            @JsonIgnore
            String getName();

            @JsonIgnore
            DynamicSecurityAnalysisParameters getExtendable();
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
        public DummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
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
            return "dynamic-security-analysis-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

}
