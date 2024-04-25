/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.YamlModuleConfigRepository;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.shortcircuit.json.JsonShortCircuitParameters;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
class ShortCircuitParametersTest extends AbstractSerDeTest {

    private static final String DUMMY_EXTENSION_NAME = "dummy-extension";

    public static class DummyExtension extends AbstractExtension<ShortCircuitParameters> {
        double parameterDouble;
        boolean parameterBoolean;
        String parameterString;

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

        public boolean isParameterBoolean() {
            return this.parameterBoolean;
        }

        public String getParameterString() {
            return this.parameterString;
        }

        double getParameterDouble() {
            return this.parameterDouble;
        }

        void setParameterDouble(double parameterDouble) {
            this.parameterDouble = parameterDouble;
        }

        void setParameterBoolean(boolean parameterBoolean) {
            this.parameterBoolean = parameterBoolean;
        }

        void setParameterString(String parameterString) {
            this.parameterString = parameterString;
        }
    }

    public static class DummySerializer implements ExtensionJsonSerializer<ShortCircuitParameters, DummyExtension> {

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }

        interface SerializationSpec {

            @JsonIgnore
            String getName();

            @JsonIgnore
            ShortCircuitParameters getExtendable();
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
            return "shortcircuit-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

    @Test
    void testExtensions() {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        DummyExtension dummyExtension = new DummyExtension();
        parameters.addExtension(DummyExtension.class, dummyExtension);

        assertEquals(1, parameters.getExtensions().size());
        assertTrue(parameters.getExtensions().contains(dummyExtension));
        assertInstanceOf(DummyExtension.class, parameters.getExtensionByName(DUMMY_EXTENSION_NAME));
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    void testNoExtensions() {
        ShortCircuitParameters parameters = new ShortCircuitParameters();

        assertEquals(0, parameters.getExtensions().size());
        assertFalse(parameters.getExtensions().contains(new DummyExtension()));
        assertNull(parameters.getExtensionByName(DUMMY_EXTENSION_NAME));
        assertNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    void testExtensionFromConfig() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load();

        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtensionByName(DUMMY_EXTENSION_NAME));
        assertNotNull(parameters.getExtension(DummyExtension.class));
    }

    @Test
    void testStudyType() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load();
        assertEquals(StudyType.TRANSIENT, parameters.getStudyType());
    }

    @Test
    void testWithFeederResult() {
        ShortCircuitParameters parameters = ShortCircuitParameters.load();
        assertTrue(parameters.isWithFeederResult());

        parameters.setWithFeederResult(false);
        assertFalse(parameters.isWithFeederResult());
    }

    @Test
    void testConfigLoader() throws IOException {
        Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
        Path cfgFile = cfgDir.resolve("config.yml");
        Path voltageDataFile = cfgDir.resolve("voltage-ranges.json");

        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")), cfgFile);
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/voltage-ranges.json")), voltageDataFile);
        PlatformConfig platformConfig = new PlatformConfig(new YamlModuleConfigRepository(cfgFile), cfgDir);
        ShortCircuitParameters parameters = ShortCircuitParameters.load(platformConfig);
        assertFalse(parameters.isWithLimitViolations());
        assertFalse(parameters.isWithVoltageResult());
        assertFalse(parameters.isWithFeederResult());
        assertTrue(parameters.isWithFortescueResult());
        assertEquals(StudyType.SUB_TRANSIENT, parameters.getStudyType());
        assertEquals(50, parameters.getMinVoltageDropProportionalThreshold(), 0.0);
        assertEquals(0.8, parameters.getSubTransientCoefficient());
        assertTrue(parameters.isWithLoads());
        assertTrue(parameters.isWithShuntCompensators());
        assertFalse(parameters.isWithVSCConverterStations());
        assertTrue(parameters.isWithNeutralPosition());
        assertEquals(InitialVoltageProfileMode.CONFIGURED, parameters.getInitialVoltageProfileMode());
        List<VoltageRange> voltageRanges = parameters.getVoltageRanges();
        assertEquals(3, voltageRanges.size());
        assertEquals(1, voltageRanges.get(0).getRangeCoefficient());
        assertEquals(Range.of(380., 420.), voltageRanges.get(0).getRange());
        assertEquals(1.2, voltageRanges.get(1).getRangeCoefficient());
        assertEquals(Range.of(215., 235.), voltageRanges.get(1).getRange());
        assertEquals(1.05, voltageRanges.get(2).getRangeCoefficient());
        assertEquals(Range.of(80., 100.), voltageRanges.get(2).getRange());
        assertFalse(parameters.isDetailedReport());
    }

    @Test
    void roundTrip() throws IOException {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        parameters.setWithVoltageResult(false);
        parameters.setWithLimitViolations(false);
        parameters.setStudyType(StudyType.SUB_TRANSIENT);
        parameters.setInitialVoltageProfileMode(InitialVoltageProfileMode.CONFIGURED);
        List<VoltageRange> voltageRanges = new ArrayList<>();
        voltageRanges.add(new VoltageRange(380, 410, 1.05));
        voltageRanges.add(new VoltageRange(0, 225, 1.1));
        voltageRanges.add(new VoltageRange(230, 375, 1.09));
        parameters.setVoltageRanges(voltageRanges);
        roundTripTest(parameters, JsonShortCircuitParameters::write, JsonShortCircuitParameters::read,
                "/ShortCircuitParameters.json");
    }

    @Test
    void writeExtension() throws IOException {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonShortCircuitParameters::write,
                ComparisonUtils::compareTxt, "/ShortCircuitParametersWithExtension.json");
    }

    @Test
    void readVersion10() {
        ShortCircuitParameters parameters = JsonShortCircuitParameters
                .read(getClass().getResourceAsStream("/ShortCircuitParametersVersion10.json"));
        assertNotNull(parameters);
        assertFalse(parameters.isWithLimitViolations());
        assertFalse(parameters.isWithVoltageResult());
        assertTrue(parameters.isWithFeederResult());
        assertEquals(StudyType.TRANSIENT, parameters.getStudyType());
        assertEquals(0, parameters.getMinVoltageDropProportionalThreshold(), 0);
        assertEquals(0.7, parameters.getSubTransientCoefficient(), 0);
    }

    @Test
    void readVersion11() {
        ShortCircuitParameters parameters = JsonShortCircuitParameters
                .read(getClass().getResourceAsStream("/ShortCircuitParametersVersion11.json"));
        assertNotNull(parameters);
        assertFalse(parameters.isWithLimitViolations());
        assertFalse(parameters.isWithVoltageResult());
        assertTrue(parameters.isWithFeederResult());
        assertEquals(StudyType.TRANSIENT, parameters.getStudyType());
        assertEquals(0, parameters.getMinVoltageDropProportionalThreshold(), 0);
        assertEquals(0.7, parameters.getSubTransientCoefficient(), 0);
    }

    @Test
    void readVersion12() {
        ShortCircuitParameters parameters = JsonShortCircuitParameters
                .read(getClass().getResourceAsStream("/ShortCircuitParametersVersion12.json"));
        assertNotNull(parameters);
        assertFalse(parameters.isWithLimitViolations());
        assertFalse(parameters.isWithVoltageResult());
        assertTrue(parameters.isWithFeederResult());
        assertEquals(StudyType.SUB_TRANSIENT, parameters.getStudyType());
        assertEquals(0, parameters.getMinVoltageDropProportionalThreshold(), 0);
        assertEquals(0.7, parameters.getSubTransientCoefficient(), 0);
        assertFalse(parameters.isWithLoads());
        assertFalse(parameters.isWithShuntCompensators());
        assertFalse(parameters.isWithVSCConverterStations());
        assertTrue(parameters.isWithNeutralPosition());
        assertEquals(InitialVoltageProfileMode.CONFIGURED, parameters.getInitialVoltageProfileMode());
        List<VoltageRange> voltageRanges = parameters.getVoltageRanges();
        assertEquals(3, voltageRanges.size());
        assertEquals(1.05, voltageRanges.get(0).getRangeCoefficient());
        assertEquals(Range.of(380., 410.), voltageRanges.get(0).getRange());
        assertEquals(1.1, voltageRanges.get(1).getRangeCoefficient());
        assertEquals(Range.of(0., 225.), voltageRanges.get(1).getRange());
        assertEquals(1.09, voltageRanges.get(2).getRangeCoefficient());
        assertEquals(Range.of(230., 375.), voltageRanges.get(2).getRange());
    }

    @Test
    void testInvalidVersion12VoltageNotSupportedInVoltageRange() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/ShortCircuitParametersVersion12Invalid.json")) {
            UncheckedIOException e = assertThrows(UncheckedIOException.class, () -> JsonShortCircuitParameters.read(is));
            assertTrue(e.getMessage().contains("VoltageRange. Tag: voltage is not valid for version 1.2. Version should be >= 1.3"));
        }
    }

    @Test
    void readVersion13() {
        ShortCircuitParameters parameters = JsonShortCircuitParameters
                .read(getClass().getResourceAsStream("/ShortCircuitParametersVersion13.json"));
        assertNotNull(parameters);
        assertFalse(parameters.isWithLimitViolations());
        assertFalse(parameters.isWithVoltageResult());
        assertTrue(parameters.isWithFeederResult());
        assertEquals(StudyType.SUB_TRANSIENT, parameters.getStudyType());
        assertEquals(0, parameters.getMinVoltageDropProportionalThreshold(), 0);
        assertEquals(0.7, parameters.getSubTransientCoefficient(), 0);
        assertFalse(parameters.isDetailedReport());
        assertEquals(InitialVoltageProfileMode.CONFIGURED, parameters.getInitialVoltageProfileMode());
        assertEquals(1, parameters.getVoltageRanges().size());
        VoltageRange voltageRange = parameters.getVoltageRanges().get(0);
        assertEquals(Range.of(380., 410.), voltageRange.getRange());
        assertEquals(1.05, voltageRange.getRangeCoefficient());
        assertEquals(380., voltageRange.getVoltage());
    }

    @Test
    void readExtension() {
        ShortCircuitParameters parameters = JsonShortCircuitParameters.read(getClass().getResourceAsStream("/ShortCircuitParametersExtensionUpdate.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    void updateExtensions() {
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
    void readError() throws IOException {
        try (var is = getClass().getResourceAsStream("/ShortCircuitParametersInvalid.json")) {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> JsonShortCircuitParameters.read(is));
            assertEquals("Unexpected field: unexpected", e.getMessage());
        }
    }

    @Test
    void testConfiguredInitialVoltageProfileMode() {
        VoltageRange coeff = new VoltageRange(380, 410, 1.1);
        assertEquals(380, coeff.getMinimumNominalVoltage());
    }

    @Test
    void testLoadFromConfigButVoltageRangeMissing() throws IOException {
        Path cfgDir = Files.createDirectory(fileSystem.getPath("config"));
        Path cfgFile = cfgDir.resolve("wrongConfig.yml");

        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/wrongConfig.yml")), cfgFile);
        PlatformConfig platformConfig = new PlatformConfig(new YamlModuleConfigRepository(cfgFile), cfgDir);
        assertThrows(PowsyblException.class, () -> ShortCircuitParameters.load(platformConfig));
    }

    private static Stream<Arguments> provideArguments() {
        return Stream.of(
            Arguments.of(
                "/ShortCircuitParametersConfiguredWithoutVoltageRanges.json",
                "Configured initial voltage profile but nominal voltage ranges with associated coefficients are missing."),
            Arguments.of(
                "/ShortCircuitParametersConfiguredWithEmptyVoltageRanges.json",
                "Configured initial voltage profile but nominal voltage ranges with associated coefficients are missing."),
            Arguments.of(
                "/ShortCircuitParametersWithUnsortedOverlappingVoltageRanges.json",
                "Voltage ranges for configured initial voltage profile are overlapping")
            );
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    void testReadButVoltageRangeExceptions(String jsonFileName, String exceptionMessage) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(jsonFileName)) {
            PowsyblException e0 = assertThrows(PowsyblException.class, () -> JsonShortCircuitParameters
                .read(stream));
            assertEquals(exceptionMessage, e0.getMessage());
        }
    }

    @Test
    void testWithOverlappingVoltageRanges() {
        ShortCircuitParameters shortCircuitParameters = new ShortCircuitParameters();
        shortCircuitParameters.setInitialVoltageProfileMode(InitialVoltageProfileMode.CONFIGURED);
        List<VoltageRange> voltageRanges = new ArrayList<>();
        voltageRanges.add(new VoltageRange(100, 400, 1));
        voltageRanges.add(new VoltageRange(200, 300, 1.1));
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> shortCircuitParameters.setVoltageRanges(voltageRanges));
        assertEquals("Voltage ranges for configured initial voltage profile are overlapping", e0.getMessage());
    }

    @Test
    void testWithInvalidCoefficient() {
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> new VoltageRange(100, 199, 10));
        assertEquals("rangeCoefficient 10.0 is out of bounds, should be between 0.8 and 1.2.", e0.getMessage());
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> new VoltageRange(100, 199, 0.2));
        assertEquals("rangeCoefficient 0.2 is out of bounds, should be between 0.8 and 1.2.", e1.getMessage());
    }

    @Test
    void testInvalidSubtransientCoefficient() {
        ShortCircuitParameters parameters = new ShortCircuitParameters();
        PowsyblException e0 = assertThrows(PowsyblException.class, () -> parameters.setSubTransientCoefficient(2.));
        assertEquals("subTransientCoefficient > 1", e0.getMessage());

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> new FaultParameters("id", true, true, true, StudyType.SUB_TRANSIENT,
                0, true, 1.2, true, true, true, true,
                InitialVoltageProfileMode.NOMINAL, null));
        assertEquals("subTransientCoefficient > 1", e1.getMessage());
    }

    @Test
    void testVoltageRangeWithSpecificVoltage() {
        VoltageRange range = new VoltageRange(100, 200, 0.9, 150);
        assertEquals(100, range.getMinimumNominalVoltage());
        assertEquals(200, range.getMaximumNominalVoltage());
        assertEquals(0.9, range.getRangeCoefficient());
        assertEquals(150, range.getVoltage());
    }

    @Test
    void testVoltageRangeWithSpecificVoltageOutOfBounds() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> new VoltageRange(100, 150, 0.9, 200));
        assertEquals("Range voltage should be in voltageRange [100.0..150.0] but it is 200.0", e.getMessage());
    }

    @Test
    void roundTripWithVoltageInVoltageRange() throws IOException {
        List<VoltageRange> voltageRanges = new ArrayList<>();
        voltageRanges.add(new VoltageRange(0, 100, 0.95, 100));
        voltageRanges.add(new VoltageRange(101, 150, 1.1, 140));
        ShortCircuitParameters parameters = new ShortCircuitParameters()
                .setInitialVoltageProfileMode(InitialVoltageProfileMode.CONFIGURED)
                .setVoltageRanges(voltageRanges);
        roundTripTest(parameters, JsonShortCircuitParameters::write, JsonShortCircuitParameters::read,
                "/ShortCircuitParametersWithRangeVoltage.json");
    }

    @Test
    void testVoltageRange() {
        VoltageRange voltageRange0 = new VoltageRange(350.0, 400.0, 1.05, 380.0);
        VoltageRange voltageRange1 = new VoltageRange(350.0, 400.0, 1.05, 380.0);
        assertEquals(voltageRange1, voltageRange0);
        assertNotNull(voltageRange0);
        assertNotEquals(new VoltageRange(350.0, 400.0, 1.05), voltageRange0);
        assertEquals(voltageRange0.hashCode(), voltageRange1.hashCode());
    }
}
