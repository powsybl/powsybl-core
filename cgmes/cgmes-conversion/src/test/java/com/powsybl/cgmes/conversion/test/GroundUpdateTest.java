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
        assertEquals(1, network.getGroundCount());

        assertEq(network.getGround("Ground"));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "ground_EQ.xml", "ground_SSH.xml");
        assertEquals(1, network.getGroundCount());

        assertSsh(network.getGround("Ground"));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "ground_EQ.xml");
        assertEquals(1, network.getGroundCount());

        assertEq(network.getGround("Ground"));

        readCgmesResources(network, DIR, "ground_SSH.xml");
        assertSsh(network.getGround("Ground"));

        readCgmesResources(network, DIR, "ground_SSH_1.xml");
        // For grounds, only the terminals are updated.
        // We are using a nodeBreaker model, and the configuration attribute
        // UPDATE_TERMINAL_CONNECTION_IN_NODE_BREAKER_VOLTAGE_LEVEL is set to false.
        // Then, changing the terminal status to disconnected will not actually disconnect the ground.
        assertSsh(network.getGround("Ground"));
    }

    private static void assertEq(Ground ground) {
        assertNotNull(ground);
        assertNotNull(ground.getTerminal());
        assertTrue(ground.getTerminal().isConnected());
    }

    private static void assertSsh(Ground ground) {
        assertNotNull(ground);
        assertTrue(ground.getTerminal().isConnected());
    }
}
