/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImport;
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
        assertEquals(3, network.getVoltageLevelCount());

        assertProperties(network, "VL_1", true, true);
        assertProperties(network, "VL_2", true, false);
        assertProperties(network, "VL_3", false, false);

        assertLimits(network, "VL_1", 400.0, 410.0);
        assertLimits(network, "VL_2", 380.0, 420.0);
        assertLimits(network, "VL_3", Double.NaN, Double.NaN);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml", "voltageLevel_SSH.xml");
        assertLimits(network, "VL_1", 385.0, 395.0);
        assertLimits(network, "VL_2", 390.0, 410.0);
        assertLimits(network, "VL_3", Double.NaN, 410.0);
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml");
        assertLimits(network, "VL_1", 400.0, 410.0);
        assertLimits(network, "VL_2", 380.0, 420.0);
        assertLimits(network, "VL_3", Double.NaN, Double.NaN);

        readCgmesResources(network, DIR, "voltageLevel_SSH.xml");
        assertLimits(network, "VL_1", 385.0, 395.0);
        assertLimits(network, "VL_2", 390.0, 410.0);
        assertLimits(network, "VL_3", Double.NaN, 410.0);

        readCgmesResources(network, DIR, "voltageLevel_SSH_1.xml");
        assertLimits(network, "VL_1", 405.0, 415.0);
        assertLimits(network, "VL_2", 380.0, 420.0);
        assertLimits(network, "VL_3", 390.0, Double.NaN);
    }

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml", "voltageLevel_SSH.xml");
        assertLimits(network, "VL_1", 385.0, 395.0);
        assertLimits(network, "VL_2", 390.0, 410.0);
        assertLimits(network, "VL_3", Double.NaN, 410.0);

        Properties properties = new Properties();
        properties.put(CgmesImport.USE_PREVIOUS_VALUES_DURING_UPDATE, "true");
        readCgmesResources(network, properties, DIR, "voltageLevel_SSH_1.xml");
        assertLimits(network, "VL_1", 405.0, 415.0);
        assertLimits(network, "VL_2", 390.0, 410.0);
        assertLimits(network, "VL_3", 390.0, 410.0);
    }

    @Test
    void removeAllPropertiesAndAliasesTest() {
        Network network = readCgmesResources(DIR, "voltageLevel_EQ.xml", "voltageLevel_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, false);

        Properties properties = new Properties();
        properties.put(CgmesImport.REMOVE_PROPERTIES_AND_ALIASES_AFTER_IMPORT, "true");
        network = readCgmesResources(properties, DIR, "voltageLevel_EQ.xml", "voltageLevel_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, true);
    }

    private static void assertPropertiesAndAliasesEmpty(Network network, boolean expected) {
        assertEquals(expected, network.getSubstationStream().allMatch(substation -> substation.getPropertyNames().isEmpty()));
        assertTrue(network.getSubstationStream().allMatch(substation -> substation.getAliases().isEmpty()));

        assertEquals(expected, network.getVoltageLevelStream().allMatch(voltageLevel -> voltageLevel.getPropertyNames().isEmpty()));
        assertTrue(network.getVoltageLevelStream().allMatch(voltageLevel -> voltageLevel.getAliases().isEmpty()));
    }

    private static void assertProperties(Network network, String voltageLevelId,
                                         boolean hasHighLowProperties, boolean hasNormalProperties) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        assertNotNull(voltageLevel);

        assertEquals(hasHighLowProperties, voltageLevel.hasProperty(PROPERTY_HIGH_VOLTAGE_LIMIT));
        assertEquals(hasHighLowProperties, voltageLevel.hasProperty(PROPERTY_LOW_VOLTAGE_LIMIT));
        assertEquals(hasNormalProperties, voltageLevel.hasProperty(PROPERTY_NORMAL_VALUE_HIGH_VOLTAGE_LIMIT));
        assertEquals(hasNormalProperties, voltageLevel.hasProperty(PROPERTY_NORMAL_VALUE_LOW_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(PROPERTY_OPERATIONAL_LIMIT_HIGH_VOLTAGE_LIMIT));
        assertTrue(voltageLevel.hasProperty(PROPERTY_OPERATIONAL_LIMIT_LOW_VOLTAGE_LIMIT));
    }

    private static void assertLimits(Network network, String voltageLevelId, double lowVoltageLimit, double highVoltageLimit) {
        VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
        assertNotNull(voltageLevel);
        double tol = 0.0000001;
        assertEquals(lowVoltageLimit, voltageLevel.getLowVoltageLimit(), tol);
        assertEquals(highVoltageLimit, voltageLevel.getHighVoltageLimit(), tol);
    }
}
