/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowParameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOError;
import java.io.IOException;

import static com.powsybl.loadflow.LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES;
import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class JsonLoadFlowParametersTest extends AbstractConverterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void roundTrip() throws IOException {
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setVoltageInitMode(PREVIOUS_VALUES)
                .setNoGeneratorReactiveLimits(true)
                .setTransformerVoltageControlOn(true);
        roundTripTest(parameters, JsonLoadFlowParameters::write, JsonLoadFlowParameters::read, "/LoadFlowParameters.json");
    }

    @Test
    public void writeExtension() throws IOException {
        LoadFlowParameters parameters = new LoadFlowParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonLoadFlowParameters::write, AbstractConverterTest::compareTxt, "/LoadFlowParametersWithExtension.json");
    }

    @Test
    public void readExtension() {
        LoadFlowParameters parameters = JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    public void readError() {
        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unknownParameter");
        JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersError.json"));
    }

    @Test
    public void readJsonVersion10() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion10.json"));
        assertTrue(parameters.isTwtSplitShuntAdmittance());
    }

    @Test
    public void readJsonVersion11() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion11.json"));
        assertTrue(parameters.isTwtSplitShuntAdmittance());
    }

    @Test
    public void readJsonVersion12() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion12.json"));
        assertTrue(parameters.isTwtSplitShuntAdmittance());
    }

    @Test
    public void readJsonVersion13() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion13.json"));
        assertTrue(parameters.isSimulShunt());
        assertTrue(parameters.isReadSlackBus());
        assertTrue(parameters.isWriteSlackBus());
    }

    @Test
    public void readJsonVersion14() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion14.json"));
        assertTrue(parameters.isDc());
        assertTrue(parameters.isDistributedSlack());
        assertEquals(LoadFlowParameters.BalanceType.PROPORTIONAL_TO_LOAD, parameters.getBalanceType());
    }

    @Test
    public void readJsonVersion15() {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion15.json"));
        assertTrue(parameters.getDcUseTransformerRatio());
        assertEquals(2, parameters.getCountriesToBalance().size());
        assertEquals("FR", parameters.getCountriesToBalance().get(0));
        assertEquals("KI", parameters.getCountriesToBalance().get(1));
        assertEquals(LoadFlowParameters.ComputedConnectedComponentType.MAIN, parameters.getComputedConnectedComponent());
    }

    @Test
    public void readJsonVersion10Exception() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("LoadFlowParameters. Tag: t2wtSplitShuntAdmittance is not valid for version 1.0. Version should be > 1.0");
        JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersVersion10Exception.json"));
    }

    @Test
    public void readJsonVersion11Exception() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("LoadFlowParameters. Tag: specificCompatibility is not valid for version 1.1. Version should be <= 1.0");
        JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersVersion11Exception.json"));
    }

    @Test
    public void readJsonVersion12Exception() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("LoadFlowParameters. Tag: t2wtSplitShuntAdmittance is not valid for version 1.2. Version should be <= 1.1");
        JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersVersion12Exception.json"));
    }

    public static class DummyExtension extends AbstractExtension<LoadFlowParameters> {
        public double parameterDouble;
        public boolean parameterBoolean;
        public String parameterString;

        DummyExtension() {
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

        public boolean isParameterBoolean() {
            return this.parameterBoolean;
        }

        public String getParameterString() {
            return this.parameterString;
        }

        public double getParameterDouble() {
            return this.parameterDouble;
        }

        public void setParameterDouble(double parameterDouble) {
            this.parameterDouble = parameterDouble;
        }

        public void setParameterBoolean(boolean parameterBoolean) {
            this.parameterBoolean = parameterBoolean;
        }

        public void setParameterString(String parameterString) {
            this.parameterString = parameterString;
        }
    }

    @Test
    public void updateExtension() throws IOError {
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

    @AutoService(JsonLoadFlowParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonLoadFlowParameters.ExtensionSerializer<DummyExtension> {

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }

        public interface SerializationSpec {

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
            DummyExtension updatedParameters = objectReader.readValue(jsonParser, DummyExtension.class);
            return updatedParameters;
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
