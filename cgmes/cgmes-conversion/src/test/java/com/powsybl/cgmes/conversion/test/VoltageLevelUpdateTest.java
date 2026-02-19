/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.Conversion.*;
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

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml", "voltageLevel_SSH.xml");
        assertEquals(1, network.getVoltageLevelCount());
        assertSsh(network.getVoltageLevel("VoltageLevel"), 405.0, 435.0);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");
        readCgmesResources(network, properties, DIR, "../empty_SSH.xml", "../empty_SV.xml");
        assertSsh(network.getVoltageLevel("VoltageLevel"), 405.0, 435.0);
    }

    @Test
    void removeAllPropertiesAndAliasesTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml", "voltageLevel_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, false);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.remove-properties-and-aliases-after-import", "true");
        network = readCgmesResources(properties, DIR, "voltageLevel_EQ.xml", "voltageLevel_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, true);
    }

    private static void assertPropertiesAndAliasesEmpty(Network network, boolean expected) {
        assertEquals(expected, network.getSubstationStream().allMatch(substation -> substation.getPropertyNames().isEmpty()));
        assertTrue(network.getSubstationStream().allMatch(substation -> substation.getAliases().isEmpty()));

        assertEquals(expected, network.getVoltageLevelStream().allMatch(voltageLevel -> voltageLevel.getPropertyNames().isEmpty()));
        assertTrue(network.getVoltageLevelStream().allMatch(voltageLevel -> voltageLevel.getAliases().isEmpty()));
    }

    private static void assertEq(VoltageLevel voltageLevel) {
        assertNotNull(voltageLevel);

        assertTrue(voltageLevel.hasProperty(PROPERTY_HIGH_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(PROPERTY_LOW_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(PROPERTY_OPERATIONAL_LIMIT_HIGH_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(PROPERTY_OPERATIONAL_LIMIT_LOW_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(PROPERTY_NORMAL_VALUE_HIGH_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(PROPERTY_NORMAL_VALUE_LOW_VOLTAGE_LIMIT));

        assertSsh(voltageLevel, 396.0, 425.0);
    }

    private static void assertSsh(VoltageLevel voltageLevel, double lowVoltageLimit, double highVoltageLimit) {
        assertNotNull(voltageLevel);
        double tol = 0.0000001;
        assertEquals(lowVoltageLimit, voltageLevel.getLowVoltageLimit(), tol);
        assertEquals(highVoltageLimit, voltageLevel.getHighVoltageLimit(), tol);
    }
}
