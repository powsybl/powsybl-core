/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.json;

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
import com.powsybl.iidm.network.Country;
import com.powsybl.loadflow.LoadFlowParameters;
import org.junit.jupiter.api.Test;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;

import static com.powsybl.loadflow.LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class JsonLoadFlowParametersTest extends AbstractSerDeTest {

    @Test
    void roundTrip() throws IOException {
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setVoltageInitMode(PREVIOUS_VALUES)
                .setUseReactiveLimits(false)
                .setTransformerVoltageControlOn(true);
        roundTripTest(parameters, JsonLoadFlowParameters::write, JsonLoadFlowParameters::read, "/LoadFlowParameters.json");
    }

    @Test
    void writeExtension() throws IOException {
        LoadFlowParameters parameters = new LoadFlowParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonLoadFlowParameters::write, ComparisonUtils::compareTxt, "/LoadFlowParametersWithExtension.json");
    }

    @Test
    void readExtension() {
        LoadFlowParameters parameters = JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    void readError() throws IOException {
        try (var is = getClass().getResourceAsStream("/LoadFlowParametersError.json")) {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> JsonLoadFlowParameters.read(is));
            assertEquals("Unexpected field: unknownParameter", e.getMessage());
        }
    }

    @Test
    void readJsonVersion10() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion10.json"));
        assertTrue(parameters.isTwtSplitShuntAdmittance());
    }

    @Test
    void readJsonVersion11() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion11.json"));
        assertTrue(parameters.isTwtSplitShuntAdmittance());
    }

    @Test
    void readJsonVersion12() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion12.json"));
        assertTrue(parameters.isTwtSplitShuntAdmittance());
    }

    @Test
    void readJsonVersion13() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion13.json"));
        assertTrue(parameters.isShuntCompensatorVoltageControlOn());
        assertTrue(parameters.isReadSlackBus());
        assertTrue(parameters.isWriteSlackBus());
    }

    @Test
    void readJsonVersion14() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion14.json"));
        assertTrue(parameters.isDc());
        assertTrue(parameters.isDistributedSlack());
        assertEquals(LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD, parameters.getBalanceType());
    }

    @Test
    void readJsonVersion15() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion15.json"));
        assertTrue(parameters.isDcUseTransformerRatio());
        assertEquals(2, parameters.getCountriesToBalance().size());
        assertTrue(parameters.getCountriesToBalance().contains(Country.FR));
        assertTrue(parameters.getCountriesToBalance().contains(Country.KI));
        assertEquals(LoadFlowParameters.ConnectedComponentMode.MAIN, parameters.getConnectedComponentMode());
    }

    @Test
    void readJsonVersion16() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion16.json"));
        assertFalse(parameters.isShuntCompensatorVoltageControlOn());
    }

    @Test
    void readJsonVersion17() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion17.json"));
        assertFalse(parameters.isHvdcAcEmulation());
    }

    @Test
    void readJsonVersion18() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion18.json"));
        assertFalse(parameters.isUseReactiveLimits());
    }

    @Test
    void readJsonVersion19() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion19.json"));
        assertEquals(0.8d, parameters.getDcPowerFactor(), 0d);
    }

    @Test
    void readJsonVersion10Exception() {
        InputStream inputStream = getClass().getResourceAsStream("/LoadFlowParametersVersion10Exception.json");
        assertThrows(PowsyblException.class, () -> JsonLoadFlowParameters.read(inputStream), "LoadFlowParameters. Tag: t2wtSplitShuntAdmittance is not valid for version 1.0. Version should be > 1.0");
    }

    @Test
    void readJsonVersion11Exception() {
        InputStream inputStream = getClass().getResourceAsStream("/LoadFlowParametersVersion11Exception.json");
        assertThrows(PowsyblException.class, () -> JsonLoadFlowParameters.read(inputStream), "LoadFlowParameters. Tag: specificCompatibility is not valid for version 1.1. Version should be <= 1.0");
    }

    @Test
    void readJsonVersion12Exception() {
        InputStream inputStream = getClass().getResourceAsStream("/LoadFlowParametersVersion12Exception.json");
        assertThrows(PowsyblException.class, () -> JsonLoadFlowParameters.read(inputStream), "LoadFlowParameters. Tag: t2wtSplitShuntAdmittance is not valid for version 1.2. Version should be <= 1.1");
    }

    public static class DummyExtension extends AbstractExtension<LoadFlowParameters> {

        public static final double PARAMETER_DOUBLE_DEFAULT_VALUE = 0;
        public static final boolean PARAMETER_BOOLEAN_DEFAULT_VALUE = false;
        public static final String PARAMETER_STRING_DEFAULT_VALUE = null;

        private double parameterDouble = PARAMETER_DOUBLE_DEFAULT_VALUE;
        private boolean parameterBoolean = PARAMETER_BOOLEAN_DEFAULT_VALUE;
        private String parameterString = PARAMETER_STRING_DEFAULT_VALUE;

        public DummyExtension() {
            super();
        }

        public DummyExtension(DummyExtension another) {
            this.parameterDouble = another.parameterDouble;
            this.parameterBoolean = another.parameterBoolean;
            this.parameterString = another.parameterString;
        }

        /**
         * Return the name of this extension.
         */
        @Override
        public String getName() {
            return "dummy-extension";
        }

        public double getParameterDouble() {
            return this.parameterDouble;
        }

        public void setParameterDouble(double parameterDouble) {
            this.parameterDouble = parameterDouble;
        }

        public boolean isParameterBoolean() {
            return this.parameterBoolean;
        }

        public void setParameterBoolean(boolean parameterBoolean) {
            this.parameterBoolean = parameterBoolean;
        }

        public String getParameterString() {
            return this.parameterString;
        }

        public void setParameterString(String parameterString) {
            this.parameterString = parameterString;
        }
    }

    @Test
    void updateExtension() throws IOError {
        LoadFlowParameters parameters = new LoadFlowParameters();
        DummyExtension extension = new DummyExtension();
        extension.setParameterDouble(2.5);
        extension.setParameterBoolean(false);
        extension.setParameterString("Hello World !");
        DummyExtension oldExtension = new DummyExtension(extension);
        parameters.addExtension(DummyExtension.class, extension);
        JsonLoadFlowParameters.update(parameters, getClass().getResourceAsStream("/LoadFlowParametersUpdate.json"));
        extension = parameters.getExtension(DummyExtension.class);
        assertEquals(oldExtension.isParameterBoolean(), extension.isParameterBoolean());
        assertNotEquals(oldExtension.getParameterDouble(), extension.getParameterDouble());
        assertNotEquals(oldExtension.getParameterString(), extension.getParameterString());
    }

    public static class DummySerializer implements ExtensionJsonSerializer<LoadFlowParameters, DummyExtension> {

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }

        interface SerializationSpec {

            @JsonIgnore
            String getName();

            @JsonIgnore
            LoadFlowParameters getExtendable();
        }

        private static ObjectMapper createMapper() {
            return JsonUtil.createObjectMapper()
                    .addMixIn(DummyExtension.class, SerializationSpec.class);
        }

        @Override
        public DummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
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
            return "loadflow-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

}
