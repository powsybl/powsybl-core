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

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL;
import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_TERMINAL;
import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class GroundUpdateTest {

    private static final String DIR = "/update/ground/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "ground_EQ.xml");
        assertEq(network);
        assertFictSwitch(network, false);
        assertGroundConnected(network, true);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "ground_EQ.xml", "ground_SSH.xml");
        assertEq(network);
        assertFictSwitch(network, false);
        assertGroundConnected(network, true);
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "ground_EQ.xml");
        assertEq(network);

        // Import a SSH where the ground is connected. No fictitious switch needed.
        readCgmesResources(network, DIR, "ground_SSH.xml");
        assertFictSwitch(network, false);
        assertGroundConnected(network, true);

        // Import a SSH where the ground is disconnected. A fictitious switch is created.
        readCgmesResources(network, DIR, "ground_SSH_1.xml");
        assertFictSwitch(network, true);
        assertGroundConnected(network, false);

        // Check that a second import doesn't create a new fictitious switch.
        readCgmesResources(network, DIR, "ground_SSH_1.xml");
        assertFictSwitch(network, true);
        assertGroundConnected(network, false);

        // When importing a situation where the ground is reconnected,
        // the fictitious switch remains and is now closed.
        readCgmesResources(network, DIR, "ground_SSH.xml");
        assertFictSwitch(network, true);
        assertGroundConnected(network, true);
    }

    @Test
    void completeUpdateUsingDifferentVariantsTest() {
        // Basically the same test as above except the different situations are imported in different variants.
        Network network = readCgmesResources(DIR, "ground_EQ.xml", "ground_SSH.xml");

        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "update-08");
        network.getVariantManager().setWorkingVariant("update-08");
        readCgmesResources(network, DIR, "ground_SSH_1.xml");

        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "update-16");
        network.getVariantManager().setWorkingVariant("update-16");
        readCgmesResources(network, DIR, "ground_SSH_1.xml");

        network.getVariantManager().cloneVariant(network.getVariantManager().getWorkingVariantId(), "update-24");
        network.getVariantManager().setWorkingVariant("update-24");
        readCgmesResources(network, DIR, "ground_SSH.xml");

        assertEq(network);
        assertFictSwitch(network, true);

        network.getVariantManager().setWorkingVariant("InitialState");
        assertGroundConnected(network, true);

        network.getVariantManager().setWorkingVariant("update-08");
        assertGroundConnected(network, false);

        network.getVariantManager().setWorkingVariant("update-16");
        assertGroundConnected(network, false);

        network.getVariantManager().setWorkingVariant("update-24");
        assertGroundConnected(network, true);
    }

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "ground_EQ.xml", "ground_SSH.xml");
        assertEquals(1, network.getGroundCount());
        assertGroundConnected(network, true);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");
        readCgmesResources(network, properties, DIR, "../empty_SSH.xml", "../empty_SV.xml");
        assertGroundConnected(network, true);
    }

    @Test
    void removeAllPropertiesAndAliasesTest() {
        Network network = readCgmesResources(DIR, "ground_EQ.xml", "ground_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, false);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.remove-properties-and-aliases-after-import", "true");
        network = readCgmesResources(properties, DIR, "ground_EQ.xml", "ground_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, true);
    }

    private static void assertPropertiesAndAliasesEmpty(Network network, boolean expected) {
        assertEquals(expected, network.getSubstationStream().allMatch(substation -> substation.getPropertyNames().isEmpty()));
        assertTrue(network.getSubstationStream().allMatch(substation -> substation.getAliases().isEmpty()));

        assertTrue(network.getGroundStream().allMatch(ground -> ground.getPropertyNames().isEmpty()));
        assertEquals(expected, network.getGroundStream().allMatch(ground -> ground.getAliases().isEmpty()));
    }

    private static void assertEq(Network network) {
        Ground ground = network.getGround("Ground");
        assertNotNull(ground);
        assertNotNull(ground.getTerminal());
    }

    private static void assertFictSwitch(Network network, boolean fictitiousSwitch) {
        if (!fictitiousSwitch) {
            assertEquals(0, network.getSwitchCount());
        } else {
            assertEquals(1, network.getSwitchCount());
            Switch fictSwitch = network.getSwitch("Ground-T_SW_fict");
            assertEquals("true", fictSwitch.getProperty(PROPERTY_IS_CREATED_FOR_DISCONNECTED_TERMINAL));
            assertEquals("Ground-T", fictSwitch.getProperty(PROPERTY_TERMINAL));
        }
    }

    private static void assertGroundConnected(Network network, boolean connected) {
        Ground ground = network.getGround("Ground");
        assertEquals(connected, ground.getTerminal().isConnected());
    }
}
