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

        assertEq(network.getVoltageLevel("VoltageLevel"));
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

        assertEq(network.getVoltageLevel("VoltageLevel"));

        readCgmesResources(network, DIR, "voltageLevel_SSH.xml");
        assertSsh(network.getVoltageLevel("VoltageLevel"), 405.0, 435.0);

        readCgmesResources(network, DIR, "voltageLevel_SSH_1.xml");
        assertSsh(network.getVoltageLevel("VoltageLevel"), 399.0, 430.0);
    }

    private static void assertEq(VoltageLevel voltageLevel) {
        assertNotNull(voltageLevel);

        assertTrue(voltageLevel.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.LOW_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.HIGH_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.OPERATIONAL_LIMIT + "_" + CgmesNames.LOW_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.OPERATIONAL_LIMIT + "_" + CgmesNames.HIGH_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_VALUE + "_" + CgmesNames.LOW_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_VALUE + "_" + CgmesNames.HIGH_VOLTAGE_LIMIT));

        assertSsh(voltageLevel, 396.0, 425.0);
    }

    private static void assertSsh(VoltageLevel voltageLevel, double lowVoltageLimit, double highVoltageLimit) {
        assertNotNull(voltageLevel);
        double tol = 0.0000001;
        assertEquals(lowVoltageLimit, voltageLevel.getLowVoltageLimit(), tol);
        assertEquals(highVoltageLimit, voltageLevel.getHighVoltageLimit(), tol);
    }
}
