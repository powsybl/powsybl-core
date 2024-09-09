/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class SwitchModificationsTest {

    private final Network network = NetworkTest1Factory.create();

    @Test
    void test() {
        String switchId = "generator1Breaker1";

        Switch sw = network.getSwitch(switchId);
        Generator generator = network.getGenerator("generator1");

        assertFalse(sw.isOpen());
        assertTrue(generator.getTerminal().isConnected());

        new OpenSwitch(switchId).apply(network);
        assertTrue(sw.isOpen());
        assertFalse(generator.getTerminal().isConnected());

        new CloseSwitch(switchId).apply(network);
        assertFalse(sw.isOpen());
        assertTrue(generator.getTerminal().isConnected());
    }

    @Test
    void testInvalidOpenSwitch() {
        assertThrows(RuntimeException.class, () -> new OpenSwitch("dummy").apply(network));
    }

    @Test
    void testInvalidCloseSwitch() {
        assertThrows(RuntimeException.class, () -> new CloseSwitch("dummy").apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new OpenSwitch("ID");
        assertEquals("OpenSwitch", networkModification.getName());

        networkModification = new CloseSwitch("ID");
        assertEquals("CloseSwitch", networkModification.getName());
    }
}
