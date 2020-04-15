package com.powsybl.security.json;

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
import com.powsybl.security.SecurityAnalysisParameters;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class JsonSecurityAnalysisParametersTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        roundTripTest(parameters, JsonSecurityAnalysisParameters::write, JsonSecurityAnalysisParameters::read, "/SecurityAnalysisParameters.json");
    }

    @Test
    public void writeExtension() throws IOException {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonSecurityAnalysisParameters::write, AbstractConverterTest::compareTxt, "/SecurityAnalysisParametersWithExtension.json");
    }

    @Test
    public void updateLoadFlowParameters() {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        parameters.getLoadFlowParameters().setT2wtSplitShuntAdmittance(true);
        JsonSecurityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/SecurityAnalysisParametersIncomplete.json"));
        assertTrue(parameters.getLoadFlowParameters().isT2wtSplitShuntAdmittance());
    }

    @Test
    public void readExtension() throws IOException {
        SecurityAnalysisParameters parameters = JsonSecurityAnalysisParameters.read(getClass().getResourceAsStream("/SecurityAnalysisParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    public void readError() throws IOException {
        try {
            JsonSecurityAnalysisParameters.read(getClass().getResourceAsStream("/SecurityAnalysisParametersWithExtension.json"));
            Assert.fail();
        } catch (AssertionError ignored) {
        }
    }

    @Test
    public void updateExtensions() {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        DummyExtension extension = new DummyExtension();
        extension.setParameterBoolean(false);
        extension.setParameterString("test");
        extension.setParameterDouble(2.8);
        DummyExtension oldExtension = new DummyExtension(extension);
        parameters.addExtension(DummyExtension.class, extension);
        JsonSecurityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/SecurityAnalysisParametersExtensionUpdate.json"));
        DummyExtension updatedExtension = parameters.getExtension(DummyExtension.class);
        assertEquals(oldExtension.isParameterBoolean(), updatedExtension.isParameterBoolean());
        assertEquals(oldExtension.getParameterDouble(), updatedExtension.getParameterDouble(), 0.01);
        assertNotEquals(oldExtension.getParameterString(), updatedExtension.getParameterString());
    }

    static class DummyExtension extends AbstractExtension<SecurityAnalysisParameters> {
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

    @AutoService(JsonSecurityAnalysisParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonSecurityAnalysisParameters.ExtensionSerializer<DummyExtension> {
        private interface SerializationSpec {

            @JsonIgnore
            String getName();

            @JsonIgnore
            SecurityAnalysisParameters getExtendable();
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
            return "security-analysis-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

}
