/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.ComparisonUtils;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.YamlModuleConfigRepository;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.json.JsonShortCircuitParameters;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class ShortCircuitParametersTest extends AbstractConverterTest {

    private static final String DUMMY_EXTENSION_NAME = "dummy-extension";

    static class DummyExtension extends AbstractExtension<ShortCircuitParameters> {
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
            return DUMMY_EXTENSION_NAME;
        }
    }

    @AutoService(JsonShortCircuitParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonShortCircuitParameters.ExtensionSerializer<DummyExtension> {
        private interface SerializationSpec {

            @JsonIgnore
            String getName();

            @JsonIgnore
            ShortCircuitParameters getExtendable();
        }

        private static ObjectMapper createMapper() {
            return JsonUtil.createObjectMapper()
                    .addMixIn(DummyExtension.class, DummySerializer.SerializationSpec.class);
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
            return DUMMY_EXTENSION_NAME;
        }

        @Override
        public String getCategoryName() {
            return "short-circuit-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

    @Test
    public void testExtensions() {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertTrue(parameters.getExtensionByName(DUMMY_EXTENSION_NAME) instanceof DummyExtension);
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    public void testNoExtensions() {
        ShortCircuitParameters parameters = new ShortCircuitParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertNull(parameters.getExtensionByName(DUMMY_EXTENSION_NAME));
        assertNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    public void testExtensionFromConfig() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load();

        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtensionByName(DUMMY_EXTENSION_NAME));
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    public void testStudyType() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load();
        assertEquals(StudyType.TRANSIENT, parameters.getStudyType());
    }

    @Test
    public void testWithFeederResult() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load();
        assertTrue(parameters.isWithFeederResult());

        parameters.setWithFeederResult(false);
        assertFalse(parameters.isWithFeederResult());
    }

    @Test
    public void testConfigLoader() throws IOException {
        Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
        Path cfgFile = cfgDir.resolve("config.yml");

        Files.copy(getClass().getResourceAsStream("/config.yml"), cfgFile);
        PlatformConfig platformConfig = new PlatformConfig(new YamlModuleConfigRepository(cfgFile), cfgDir);
        ShortCircuitParameters parameters = ShortCircuitParameters.load(platformConfig);
        assertFalse(parameters.isWithLimitViolations());
        assertFalse(parameters.isWithVoltageMap());
        assertFalse(parameters.isWithFeederResult());
        assertEquals(StudyType.SUB_TRANSIENT, parameters.getStudyType());
        assertEquals(1.2, parameters.getMinVoltageDropProportionalThreshold(), 0.0);
    }

    @AutoService(ShortCircuitParameters.ConfigLoader.class)
    public static class DummyLoader implements ShortCircuitParameters.ConfigLoader<DummyExtension> {

        @Override
        public DummyExtension load(PlatformConfig platformConfig) {
            return new DummyExtension();
        }

        @Override
        public String getExtensionName() {
            return DUMMY_EXTENSION_NAME;
        }

        @Override
        public String getCategoryName() {
            return "short-circuit-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

    @Test
    public void roundTrip() throws IOException {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        parameters.setWithVoltageMap(false);
        parameters.setWithLimitViolations(false);
        roundTripTest(parameters, JsonShortCircuitParameters::write, JsonShortCircuitParameters::read,
                "/ShortCircuitParameters.json");
    }

    @Test
    public void writeExtension() throws IOException {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonShortCircuitParameters::write,
                ComparisonUtils::compareTxt, "/ShortCircuitParametersWithExtension.json");
    }

    @Test
    public void readExtension() {
        ShortCircuitParameters parameters = JsonShortCircuitParameters.read(getClass().getResourceAsStream("/ShortCircuitParametersExtensionUpdate.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    public void updateExtensions() {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        DummyExtension extension = new DummyExtension();
        extension.setParameterBoolean(false);
        extension.setParameterString("test");
        extension.setParameterDouble(2.8);
        DummyExtension oldExtension = new DummyExtension(extension);
        parameters.addExtension(DummyExtension.class, extension);
        JsonShortCircuitParameters.update(parameters, getClass().getResourceAsStream("/ShortCircuitParametersExtensionUpdate.json"));
        DummyExtension updatedExtension = parameters.getExtension(DummyExtension.class);
        assertEquals(oldExtension.isParameterBoolean(), updatedExtension.isParameterBoolean());
        assertEquals(oldExtension.getParameterDouble(), updatedExtension.getParameterDouble(), 0.01);
        assertNotEquals(oldExtension.getParameterString(), updatedExtension.getParameterString());
    }

    @Test
    public void readError() {
        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unexpected");
        JsonShortCircuitParameters.read(getClass().getResourceAsStream("/ShortCircuitParametersInvalid.json"));
    }
}
