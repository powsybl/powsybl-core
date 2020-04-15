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
import org.junit.Assert;
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
    public void readExtension() throws IOException {
        LoadFlowParameters parameters = JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    public void readError() throws IOException {
        try {
            JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersError.json"));
            Assert.fail();
        } catch (AssertionError ignored) {
        }
    }

    @Test
    public void readJsonVersion10() throws IOException {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion10.json"));
        assertEquals(true, parameters.isT2wtSplitShuntAdmittance());
    }

    @Test
    public void readJsonVersion11() throws IOException {
        LoadFlowParameters parameters = JsonLoadFlowParameters
                .read(getClass().getResourceAsStream("/LoadFlowParametersVersion11.json"));
        assertEquals(true, parameters.isT2wtSplitShuntAdmittance());
    }

    @Test
    public void readJsonVersion10Exception() throws IOException {
        exception.expect(PowsyblException.class);
        exception.expectMessage("LoadFlowParameters. Tag: t2wtSplitShuntAdmittance is not valid for version 1.0. Version should be > 1.0");
        JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersVersion10Exception.json"));
    }

    @Test
    public void readJsonVersion11Exception() throws IOException {
        exception.expect(PowsyblException.class);
        exception.expectMessage("LoadFlowParameters. Tag: specificCompatibility is not valid for version 1.1. Version should be <= 1.0");
        JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersVersion11Exception.json"));
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
        LoadFlowParameters oldParameters = parameters.copy();
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
