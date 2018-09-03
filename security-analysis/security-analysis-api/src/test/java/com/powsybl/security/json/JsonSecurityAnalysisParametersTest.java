package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.security.SecurityAnalysisParameters;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        parameters.getLoadFlowParameters().setSpecificCompatibility(true);
        JsonSecurityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/SecurityAnalysisParametersIncomplete.json"));

        assertTrue(parameters.getLoadFlowParameters().isSpecificCompatibility());
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

    static class DummyExtension extends AbstractExtension<SecurityAnalysisParameters> {

        DummyExtension() {
            super();
        }

        @Override
        public String getName() {
            return "dummy-extension";
        }
    }

    @AutoService(JsonSecurityAnalysisParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonSecurityAnalysisParameters.ExtensionSerializer<DummyExtension> {

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
            return "security-analysis-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

}
