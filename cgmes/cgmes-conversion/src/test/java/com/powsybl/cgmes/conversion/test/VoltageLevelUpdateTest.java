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

        VoltageLevel voltageLevel = network.getVoltageLevel("VoltageLevel");
        assertTrue(checkEq(voltageLevel, 395.0, 425.0));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml", "voltageLevel_SSH.xml");
        assertEquals(1, network.getVoltageLevelCount());

        VoltageLevel voltageLevel = network.getVoltageLevel("VoltageLevel");
        assertTrue(checkSsh(voltageLevel, 405.0, 435.0));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml");
        assertEquals(1, network.getVoltageLevelCount());

        VoltageLevel voltageLevel = network.getVoltageLevel("VoltageLevel");
        assertTrue(checkEq(voltageLevel, 395.0, 425.0));

        readCgmesResources(network, DIR, "voltageLevel_SSH.xml");
        assertTrue(checkSsh(voltageLevel, 405.0, 435.0));

        readCgmesResources(network, DIR, "voltageLevel_SSH_1.xml");
        assertTrue(checkSsh(voltageLevel, 400.0, 430.0));
    }

    private static boolean checkEq(VoltageLevel voltageLevel, double lowVoltageLimit, double highVoltageLimit) {
        assertNotNull(voltageLevel);
        assertNotNull(voltageLevel.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.LOW_VOLTAGE_LIMIT));
        assertNotNull(voltageLevel.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.HIGH_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.OPERATIONAL_LIMIT_SET + "_" + CgmesNames.LOW_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.OPERATIONAL_LIMIT_SET + "_" + CgmesNames.HIGH_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.OPERATIONAL_LIMIT + "_") && propertyName.contains(CgmesNames.LOW_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.OPERATIONAL_LIMIT + "_") && propertyName.contains(CgmesNames.HIGH_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.NORMAL_VALUE + "_") && propertyName.contains(CgmesNames.LOW_VOLTAGE_LIMIT)));
        assertTrue(voltageLevel.getPropertyNames().stream().anyMatch(propertyName -> propertyName.contains(CgmesNames.NORMAL_VALUE + "_") && propertyName.contains(CgmesNames.HIGH_VOLTAGE_LIMIT)));

        checkSsh(voltageLevel, lowVoltageLimit, highVoltageLimit);
        return true;
    }

    private static boolean checkSsh(VoltageLevel voltageLevel, double lowVoltageLimit, double highVoltageLimit) {
        assertNotNull(voltageLevel);
        double tol = 0.0000001;
        assertEquals(lowVoltageLimit, voltageLevel.getLowVoltageLimit(), tol);
        assertEquals(highVoltageLimit, voltageLevel.getHighVoltageLimit(), tol);
        return true;
    }
}
