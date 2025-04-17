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

        Switch seriesCompensator = network.getSwitch("SeriesCompensator");
        assertTrue(checkEq(seriesCompensator));

        Switch breaker = network.getSwitch("Breaker");
        assertTrue(checkEq(breaker));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertEquals(5, network.getSwitchCount());

        Switch seriesCompensator = network.getSwitch("SeriesCompensator");
        assertTrue(checkSsh(seriesCompensator, true));

        Switch breaker = network.getSwitch("Breaker");
        assertTrue(checkSsh(breaker, false));

        Switch seriesCompensatorFict = network.getSwitch("SeriesCompensator-T1_SW_fict");
        assertTrue(checkSsh(seriesCompensatorFict, true));

        Switch breakerFict = network.getSwitch("Breaker-T2_SW_fict");
        assertTrue(checkSsh(breakerFict, true));

        Switch energyConsumerFict = network.getSwitch("EnergyConsumer-T_SW_fict");
        assertTrue(checkSsh(energyConsumerFict, true));
    }

    @Test
    void importEqAndSshTogetherAndSsh1LaterTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml", "switch_SSH.xml");
        assertEquals(5, network.getSwitchCount());

        Switch seriesCompensator = network.getSwitch("SeriesCompensator");
        assertTrue(checkSsh(seriesCompensator, true));

        Switch breaker = network.getSwitch("Breaker");
        assertTrue(checkSsh(breaker, false));

        Switch seriesCompensatorFict = network.getSwitch("SeriesCompensator-T1_SW_fict");
        assertTrue(checkSsh(seriesCompensatorFict, true));

        Switch breakerFict = network.getSwitch("Breaker-T2_SW_fict");
        assertTrue(checkSsh(breakerFict, true));

        Switch energyConsumerFict = network.getSwitch("EnergyConsumer-T_SW_fict");
        assertTrue(checkSsh(energyConsumerFict, true));

        readCgmesResources(network, DIR, "switch_SSH_1.xml");

        assertTrue(checkSsh(seriesCompensator, false));
        assertTrue(checkSsh(breaker, true));
        assertTrue(checkSsh(seriesCompensatorFict, false));
        assertTrue(checkSsh(breakerFict, false));
        assertTrue(checkSsh(energyConsumerFict, false));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "switch_EQ.xml");
        assertEquals(2, network.getSwitchCount());

        Switch seriesCompensator = network.getSwitch("SeriesCompensator");
        assertTrue(checkEq(seriesCompensator));
        Switch breaker = network.getSwitch("Breaker");
        assertTrue(checkEq(breaker));

        readCgmesResources(network, DIR, "switch_SSH.xml");

        assertTrue(checkSsh(seriesCompensator, true));
        assertTrue(checkSsh(breaker, false));

        readCgmesResources(network, DIR, "switch_SSH_1.xml");

        assertTrue(checkSsh(seriesCompensator, false));
        assertTrue(checkSsh(breaker, true));
    }

    private static boolean checkEq(Switch sw) {
        assertNotNull(sw);
        assertNotNull(sw.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        assertNotNull(sw.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_OPEN));
        return true;
    }

    private static boolean checkSsh(Switch sw, boolean isOpen) {
        assertNotNull(sw);
        assertEquals(isOpen, sw.isOpen());
        return true;
    }
}
