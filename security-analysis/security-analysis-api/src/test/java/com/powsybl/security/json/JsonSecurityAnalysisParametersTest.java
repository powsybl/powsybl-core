package com.powsybl.security.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.ComparisonUtils;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.SecurityAnalysisParameters;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class JsonSecurityAnalysisParametersTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        parameters.getIncreasedViolationsParameters().setFlowProportionalThreshold(0.2);
        roundTripTest(parameters, JsonSecurityAnalysisParameters::write, JsonSecurityAnalysisParameters::read, "/SecurityAnalysisParametersV1.1.json");
    }

    @Test
    public void writeExtension() throws IOException {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonSecurityAnalysisParameters::write, ComparisonUtils::compareTxt, "/SecurityAnalysisParametersWithExtension.json");
    }

    @Test
    public void updateLoadFlowParameters() {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        parameters.getLoadFlowParameters().setTwtSplitShuntAdmittance(true);
        JsonSecurityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/SecurityAnalysisParametersIncomplete.json"));
        assertTrue(parameters.getLoadFlowParameters().isTwtSplitShuntAdmittance());
    }

    @Test
    public void readExtension() throws IOException {
        SecurityAnalysisParameters parameters = JsonSecurityAnalysisParameters.read(getClass().getResourceAsStream("/SecurityAnalysisParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    public void readError() {
        InputStream inputStream = getClass().getResourceAsStream("/SecurityAnalysisParametersInvalid.json");
        assertThrows("Unexpected field: unexpected", AssertionError.class, () -> JsonSecurityAnalysisParameters.read(inputStream));
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

    @Test
    public void readJsonVersion10() {
        SecurityAnalysisParameters parameters = JsonSecurityAnalysisParameters
                .read(getClass().getResourceAsStream("/SecurityAnalysisParametersV1.json"));
        assertEquals(0.1, parameters.getIncreasedViolationsParameters().getFlowProportionalThreshold(), 0.0001);
    }

    @Test
    public void readJsonVersion11() {
        SecurityAnalysisParameters parameters = JsonSecurityAnalysisParameters
                .read(getClass().getResourceAsStream("/SecurityAnalysisParametersV1.1.json"));
        assertEquals(0.2, parameters.getIncreasedViolationsParameters().getFlowProportionalThreshold(), 0.0001);
    }

    @Test
    public void readJsonVersion10Invalid() {
        InputStream inputStream = getClass().getResourceAsStream("/SecurityAnalysisParametersV1Invalid.json");
        assertThrows("SecurityAnalysisParameters. Tag: specificCompatibility is not valid for version 1.0. Version should be > 1.0",
                PowsyblException.class, () -> JsonSecurityAnalysisParameters.read(inputStream));
    }

    public static class DummyExtension extends AbstractExtension<SecurityAnalysisParameters> {
        public double parameterDouble;
        public boolean parameterBoolean;
        public String parameterString;

        public DummyExtension() {
            super();
        }

        public DummyExtension(DummyExtension another) {
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

    public static class DummySerializer implements ExtensionJsonSerializer<SecurityAnalysisParameters, DummyExtension> {
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
