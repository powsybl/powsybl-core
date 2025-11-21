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

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class BusbarSectionUpdateTest {

    private static final String DIR = "/update/busbar-section/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "busbarSection_EQ.xml");
        assertEquals(1, network.getBusbarSectionCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "busbarSection_EQ.xml", "busbarSection_SSH.xml");
        // BusbarSections terminals are always considered connected, regardless of their state.
        // Therefore, fictitious switches used in other equipment to simulate disconnected terminals are not created.
        assertEq(network);
    }

    @Test
    void importEqAndSshTest() {
        Network network = readCgmesResources(DIR, "busbarSection_EQ.xml");
        assertEquals(1, network.getBusbarSectionCount());
        assertEq(network);

        readCgmesResources(network, DIR, "busbarSection_SSH.xml");
        assertEq(network);
    }

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "busbarSection_EQ.xml", "busbarSection_SSH.xml");
        assertEq(network);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");
        readCgmesResources(network, properties, DIR, "../empty_SSH.xml", "../empty_SV.xml");
        assertEq(network);
    }

    @Test
    void removeAllPropertiesAndAliasesTest() {
        Network network = readCgmesResources(DIR, "busbarSection_EQ.xml", "busbarSection_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, false);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.remove-properties-and-aliases-after-import", "true");
        network = readCgmesResources(properties, DIR, "busbarSection_EQ.xml", "busbarSection_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, true);
    }

    private static void assertPropertiesAndAliasesEmpty(Network network, boolean expected) {
        assertEquals(expected, network.getSubstationStream().allMatch(substation -> substation.getPropertyNames().isEmpty()));
        assertTrue(network.getSubstationStream().allMatch(substation -> substation.getAliases().isEmpty()));

        assertTrue(network.getBusbarSectionStream().allMatch(busbarSection -> busbarSection.getPropertyNames().isEmpty()));
        assertEquals(expected, network.getBusbarSectionStream().allMatch(busbarSection -> busbarSection.getAliases().isEmpty()));
    }

    private static void assertEq(Network network) {
        BusbarSection busbarSection = network.getBusbarSection("BusbarSection");
        assertNotNull(busbarSection);
        assertNotNull(busbarSection.getTerminal());
        assertTrue(busbarSection.getTerminal().isConnected());
    }
}
