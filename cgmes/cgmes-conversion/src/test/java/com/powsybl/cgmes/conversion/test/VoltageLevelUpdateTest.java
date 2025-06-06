/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class VoltageLevelUpdateTest {

    private static final String DIR = "/update/voltage-level/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml");
        assertEquals(1, network.getVoltageLevelCount());

        assertEq(network.getVoltageLevel("VoltageLevel"), 395.0, 425.0);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml", "voltageLevel_SSH.xml");
        assertEquals(1, network.getVoltageLevelCount());

        assertSsh(network.getVoltageLevel("VoltageLevel"), 405.0, 435.0);
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml");
        assertEquals(1, network.getVoltageLevelCount());

        assertEq(network.getVoltageLevel("VoltageLevel"), 395.0, 425.0);

        readCgmesResources(network, DIR, "voltageLevel_SSH.xml");
        assertSsh(network.getVoltageLevel("VoltageLevel"), 405.0, 435.0);

        readCgmesResources(network, DIR, "voltageLevel_SSH_1.xml");
        assertSsh(network.getVoltageLevel("VoltageLevel"), 400.0, 430.0);
    }

    private static void assertEq(VoltageLevel voltageLevel, double lowVoltageLimit, double highVoltageLimit) {
        assertNotNull(voltageLevel);
        assertNotNull(voltageLevel.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.LOW_VOLTAGE_LIMIT));
        assertNotNull(voltageLevel.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.HIGH_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.OPERATIONAL_LIMIT_SET + "_" + CgmesNames.LOW_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.OPERATIONAL_LIMIT_SET + "_" + CgmesNames.HIGH_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.OPERATIONAL_LIMIT + "_") && propertyName.contains(CgmesNames.LOW_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.OPERATIONAL_LIMIT + "_") && propertyName.contains(CgmesNames.HIGH_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.NORMAL_VALUE + "_") && propertyName.contains(CgmesNames.LOW_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.NORMAL_VALUE + "_") && propertyName.contains(CgmesNames.HIGH_VOLTAGE_LIMIT)));

        assertSsh(voltageLevel, lowVoltageLimit, highVoltageLimit);
    }

    private static void assertSsh(VoltageLevel voltageLevel, double lowVoltageLimit, double highVoltageLimit) {
        assertNotNull(voltageLevel);
        double tol = 0.0000001;
        assertEquals(lowVoltageLimit, voltageLevel.getLowVoltageLimit(), tol);
        assertEquals(highVoltageLimit, voltageLevel.getHighVoltageLimit(), tol);
    }
}
