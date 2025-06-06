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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SwitchUpdateTest {

    private static final String DIR = "/update/switch/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml");
        assertEquals(2, network.getSwitchCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertEquals(5, network.getSwitchCount());

        assertEqSsh(network);
    }

    @Test
    void importEqAndSshTogetherAndSsh1LaterTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertEquals(5, network.getSwitchCount());

        assertEqSsh(network);

        readCgmesResources(network, DIR, "switch_SSH_1.xml");
        assertEqSshSsh1(network);
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml");
        assertEquals(2, network.getSwitchCount());

        assertEq(network);

        readCgmesResources(network, DIR, "switch_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "switch_SSH_1.xml");
        assertSecondSsh(network);
    }

    private static void assertEq(Network network) {
        assertEq(network.getSwitch("SeriesCompensator"));
        assertEq(network.getSwitch("Breaker"));
    }

    private static void assertEqSsh(Network network) {
        assertSsh(network.getSwitch("SeriesCompensator"), true);
        assertSsh(network.getSwitch("Breaker"), false);
        assertSsh(network.getSwitch("SeriesCompensator-T1_SW_fict"), true);
        assertSsh(network.getSwitch("Breaker-T2_SW_fict"), true);
        assertSsh(network.getSwitch("EnergyConsumer-T_SW_fict"), true);
    }

    private static void assertEqSshSsh1(Network network) {
        assertSsh(network.getSwitch("SeriesCompensator"), false);
        assertSsh(network.getSwitch("Breaker"), true);
        assertSsh(network.getSwitch("SeriesCompensator-T1_SW_fict"), false);
        assertSsh(network.getSwitch("Breaker-T2_SW_fict"), false);
        assertSsh(network.getSwitch("EnergyConsumer-T_SW_fict"), false);
    }

    private static void assertFirstSsh(Network network) {
        assertSsh(network.getSwitch("SeriesCompensator"), true);
        assertSsh(network.getSwitch("Breaker"), false);
    }

    private static void assertSecondSsh(Network network) {
        assertSsh(network.getSwitch("SeriesCompensator"), false);
        assertSsh(network.getSwitch("Breaker"), true);
    }

    private static void assertEq(Switch sw) {
        assertNotNull(sw);
        assertNotNull(sw.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        assertNotNull(sw.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_OPEN));
    }

    private static void assertSsh(Switch sw, boolean isOpen) {
        assertNotNull(sw);
        assertEquals(isOpen, sw.isOpen());
    }
}
