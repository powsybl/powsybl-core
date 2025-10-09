/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.shortcircuit.InitialVoltageProfileMode;
import com.powsybl.shortcircuit.StudyType;
import com.powsybl.shortcircuit.VoltageRange;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class JsonFaultParametersTest extends AbstractSerDeTest {

    @Test
    void roundTrip() throws IOException {
        List<FaultParameters> parameters = new ArrayList<>();
        parameters.add(new FaultParameters("f00", false, false, true, StudyType.STEADY_STATE, 20, true, Double.NaN, true, true, true, true, InitialVoltageProfileMode.NOMINAL, null));
        List<VoltageRange> voltageRanges = List.of(new VoltageRange(0, 230, 1), new VoltageRange(231, 250, 1.05, 240));
        parameters.add(new FaultParameters("f01", false, true, false, null, Double.NaN, true, Double.NaN, true, true, false, false, InitialVoltageProfileMode.CONFIGURED, voltageRanges));
        parameters.add(new FaultParameters("f10", true, false, false, null, Double.NaN, false, Double.NaN, false, true, false, false, InitialVoltageProfileMode.NOMINAL, null));
        parameters.add(new FaultParameters("f11", true, true, false, null, Double.NaN, false, Double.NaN, false, false, false, false, null, null));
        parameters.add(new FaultParameters("f12", true, false, false, StudyType.SUB_TRANSIENT, Double.NaN, false, 0.8, true, false, false, false, InitialVoltageProfileMode.PREVIOUS_VALUE, null));
        roundTripTest(parameters, FaultParameters::write, FaultParameters::read, "/FaultParametersFile.json");

        assertNotNull(parameters.get(0));
        assertNotEquals(parameters.get(0), parameters.get(1));
        assertNotEquals(parameters.get(0).hashCode(), parameters.get(2).hashCode());
    }

    //
    @Test
    void readVersion10() throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/FaultParametersFileVersion10.json")), fileSystem.getPath("/FaultParametersFileVersion10.json"));
        List<FaultParameters> parameters = FaultParameters.read(fileSystem.getPath("/FaultParametersFileVersion10.json"));
        assertEquals(4, parameters.size());

        FaultParameters firstParam = parameters.get(0);
        assertEquals("f00", firstParam.getId());
        assertFalse(firstParam.isWithLimitViolations());
        assertEquals(StudyType.STEADY_STATE, firstParam.getStudyType());
        assertTrue(firstParam.isWithFeederResult());
        assertFalse(firstParam.isWithVoltageResult());
        assertEquals(20, firstParam.getMinVoltageDropProportionalThreshold(), 0);

        FaultParameters secondParam = parameters.get(1);
        assertEquals("f01", secondParam.getId());
        assertFalse(secondParam.isWithLimitViolations());
        assertNull(secondParam.getStudyType());
        assertFalse(secondParam.isWithFeederResult());
        assertTrue(secondParam.isWithVoltageResult());
        assertEquals(Double.NaN, secondParam.getMinVoltageDropProportionalThreshold(), 0);

        FaultParameters thirdParam = parameters.get(2);
        assertEquals("f10", thirdParam.getId());
        assertTrue(thirdParam.isWithLimitViolations());
        assertNull(thirdParam.getStudyType());
        assertFalse(thirdParam.isWithFeederResult());
        assertFalse(thirdParam.isWithVoltageResult());
        assertEquals(Double.NaN, thirdParam.getMinVoltageDropProportionalThreshold(), 0);

        FaultParameters fourthParam = parameters.get(3);
        assertEquals("f11", fourthParam.getId());
        assertTrue(fourthParam.isWithLimitViolations());
        assertNull(fourthParam.getStudyType());
        assertFalse(fourthParam.isWithFeederResult());
        assertTrue(fourthParam.isWithVoltageResult());
        assertEquals(Double.NaN, fourthParam.getMinVoltageDropProportionalThreshold(), 0);
    }

    @Test
    void readVersion11() throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/FaultParametersFileVersion11.json")), fileSystem.getPath("/FaultParametersFileVersion11.json"));
        List<FaultParameters> parameters = FaultParameters.read(fileSystem.getPath("/FaultParametersFileVersion11.json"));
        assertEquals(1, parameters.size());

        FaultParameters firstParam = parameters.get(0);
        assertEquals("f00", firstParam.getId());
        assertFalse(firstParam.isWithLimitViolations());
        assertEquals(StudyType.STEADY_STATE, firstParam.getStudyType());
        assertTrue(firstParam.isWithFeederResult());
        assertFalse(firstParam.isWithVoltageResult());
        assertEquals(20, firstParam.getMinVoltageDropProportionalThreshold(), 0);
    }

    @Test
    void readVersion12() throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/FaultParametersFileVersion12.json")), fileSystem.getPath("/FaultParametersFileVersion12.json"));
        List<FaultParameters> parameters = FaultParameters.read(fileSystem.getPath("/FaultParametersFileVersion12.json"));
        assertEquals(1, parameters.size());

        FaultParameters firstParam = parameters.get(0);
        assertEquals("f00", firstParam.getId());
        assertFalse(firstParam.isWithLimitViolations());
        assertEquals(StudyType.SUB_TRANSIENT, firstParam.getStudyType());
        assertTrue(firstParam.isWithFeederResult());
        assertFalse(firstParam.isWithVoltageResult());
        assertEquals(20, firstParam.getMinVoltageDropProportionalThreshold(), 0);
        assertEquals(0.8, firstParam.getSubTransientCoefficient());
    }

    @Test
    void readVersion13() throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/FaultParametersFileVersion13.json")), fileSystem.getPath("/FaultParametersFileVersion13.json"));
        List<FaultParameters> parameters = FaultParameters.read(fileSystem.getPath("/FaultParametersFileVersion13.json"));
        assertEquals(1, parameters.size());

        FaultParameters param = parameters.get(0);
        assertEquals("f00", param.getId());
        assertFalse(param.isWithLimitViolations());
        assertEquals(StudyType.SUB_TRANSIENT, param.getStudyType());
        assertTrue(param.isWithFeederResult());
        assertFalse(param.isWithVoltageResult());
        assertEquals(20, param.getMinVoltageDropProportionalThreshold(), 0);
        assertEquals(0.8, param.getSubTransientCoefficient());
        assertEquals(InitialVoltageProfileMode.CONFIGURED, param.getInitialVoltageProfileMode());
        assertEquals(2, param.getVoltageRanges().size());
        VoltageRange voltageRange1 = param.getVoltageRanges().get(0);
        assertEquals(Range.of(0., 230.), voltageRange1.getRange());
        assertEquals(1.0, voltageRange1.getRangeCoefficient());
        assertTrue(Double.isNaN(voltageRange1.getVoltage()));
        VoltageRange voltageRange2 = param.getVoltageRanges().get(1);
        assertEquals(Range.of(231., 250.), voltageRange2.getRange());
        assertEquals(1.05, voltageRange2.getRangeCoefficient());
        assertEquals(240., voltageRange2.getVoltage());
    }

    @Test
    void readUnexpectedField() throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/FaultParametersFileInvalid.json")), fileSystem.getPath("/FaultParametersFileInvalid.json"));

        Path path = fileSystem.getPath("/FaultParametersFileInvalid.json");
        UncheckedIOException e = assertThrows(UncheckedIOException.class, () -> FaultParameters.read(path));
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: Unexpected field: unexpected (through reference chain: java.util.ArrayList[0])", e.getMessage());
    }

    @Test
    void readParameters() throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/FaultParametersFile.json")), fileSystem.getPath("/FaultParametersFile.json"));
        List<FaultParameters> parameters = FaultParameters.read(fileSystem.getPath("/FaultParametersFile.json"));
        assertEquals(5, parameters.size());

        FaultParameters param = new FaultParameters("f00", false, false, true, StudyType.STEADY_STATE, 20, true, Double.NaN, true, true, true, true, InitialVoltageProfileMode.NOMINAL, null);
        assertEquals(parameters.get(0), param);

        FaultParameters param2 = new FaultParameters("f01", false, false, true, StudyType.STEADY_STATE, 20, true, Double.NaN, true, true, true, true, InitialVoltageProfileMode.NOMINAL, null);
        assertNotEquals(parameters.get(0), param2);
    }

    @Test
    void readParametersMissingVoltageRanges() throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/FaultParametersFileWithoutVoltageRanges.json")), fileSystem.getPath("/FaultParametersFileWithoutVoltageRanges.json"));
        Path path = fileSystem.getPath("/FaultParametersFileWithoutVoltageRanges.json");
        UncheckedIOException e0 = assertThrows(UncheckedIOException.class, () -> FaultParameters.read(path));
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: Configured initial voltage profile but nominal voltage ranges with associated coefficients are missing. (through reference chain: java.util.ArrayList[0])", e0.getMessage());
    }

    @Test
    void readParametersEmptyVoltageRange() throws IOException {
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/FaultParametersFileEmptyVoltageRanges.json")), fileSystem.getPath("/FaultParametersFileEmptyVoltageRanges.json"));
        Path path = fileSystem.getPath("/FaultParametersFileEmptyVoltageRanges.json");
        UncheckedIOException e0 = assertThrows(UncheckedIOException.class, () -> FaultParameters.read(path));
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: Configured initial voltage profile but nominal voltage ranges with associated coefficients are missing. (through reference chain: java.util.ArrayList[0])", e0.getMessage());
    }
}
