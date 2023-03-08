/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.shortcircuit.StudyType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class JsonFaultParametersTest extends AbstractConverterTest {

    @Test
    void roundTrip() throws IOException {
        List<FaultParameters> parameters = new ArrayList<>();
        parameters.add(new FaultParameters("f00", false, false, true, StudyType.STEADY_STATE, 1.0, true));
        parameters.add(new FaultParameters("f01", false, true, false, null, Double.NaN, true));
        parameters.add(new FaultParameters("f10", true, false, false, null, Double.NaN, false));
        parameters.add(new FaultParameters("f11", true, true, false, null, Double.NaN, false));
        roundTripTest(parameters, FaultParameters::write, FaultParameters::read, "/FaultParametersFile.json");

        assertNotNull(parameters.get(0));
        assertNotEquals(parameters.get(0), parameters.get(1));
        assertNotEquals(parameters.get(0).hashCode(), parameters.get(2).hashCode());
        assertEquals(parameters.get(0), parameters.get(0));
    }

    @Test
    void readVersion10() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultParametersFileVersion10.json"), fileSystem.getPath("/FaultParametersFileVersion10.json"));
        List<FaultParameters> parameters = FaultParameters.read(fileSystem.getPath("/FaultParametersFileVersion10.json"));
        assertEquals(4, parameters.size());

        FaultParameters firstParam = parameters.get(0);
        assertEquals("f00", firstParam.getId());
        assertFalse(firstParam.isWithLimitViolations());
        assertEquals(StudyType.STEADY_STATE, firstParam.getStudyType());
        assertTrue(firstParam.isWithFeederResult());
        assertFalse(firstParam.isWithVoltageResult());
        assertEquals(1.0, firstParam.getMinVoltageDropProportionalThreshold(), 0);

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
        Files.copy(getClass().getResourceAsStream("/FaultParametersFileVersion11.json"), fileSystem.getPath("/FaultParametersFileVersion11.json"));
        List<FaultParameters> parameters = FaultParameters.read(fileSystem.getPath("/FaultParametersFileVersion11.json"));
        assertEquals(1, parameters.size());

        FaultParameters firstParam = parameters.get(0);
        assertEquals("f00", firstParam.getId());
        assertFalse(firstParam.isWithLimitViolations());
        assertEquals(StudyType.STEADY_STATE, firstParam.getStudyType());
        assertTrue(firstParam.isWithFeederResult());
        assertFalse(firstParam.isWithVoltageResult());
        assertEquals(1.0, firstParam.getMinVoltageDropProportionalThreshold(), 0);
    }

    @Test
    void readUnexpectedField() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultParametersFileInvalid.json"), fileSystem.getPath("/FaultParametersFileInvalid.json"));

        Path path = fileSystem.getPath("/FaultParametersFileInvalid.json");
        AssertionError e = assertThrows(AssertionError.class, () -> FaultParameters.read(path));
        assertEquals("Unexpected field: unexpected", e.getMessage());
    }
}
