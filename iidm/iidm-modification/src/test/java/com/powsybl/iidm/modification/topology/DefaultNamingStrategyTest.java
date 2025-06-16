/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DefaultNamingStrategyTest {

    private NamingStrategy namingStrategy;

    @BeforeEach
    void setUp() {
        namingStrategy = new DefaultNamingStrategy();
    }

    @Test
    void testName() {
        assertEquals("Default", namingStrategy.getName());
    }

    @Test
    void testDisconnector() {
        assertEquals("test_DISCONNECTOR_5_6", namingStrategy.getDisconnectorId("test", 5, 6));
    }

    @Test
    void testDisconnectorWithBbs() {
        Network network = createNbNetwork();

        assertEquals("test_DISCONNECTOR_5_6", namingStrategy.getDisconnectorId(network.getBusbarSection("D"), "test", 5, 6, 0));
    }

    @Test
    void testBreaker() {
//        // Full Id
        assertEquals("test_BREAKER", namingStrategy.getBreakerId("test"));
//        assertEquals("test_BREAKER_5", namingStrategy.getBreakerId("test", 5));
        assertEquals("test_BREAKER_5_6", namingStrategy.getBreakerId("test", 5, 6));
    }

    @Test
    void testSwitch() {
        // Full Id
        assertEquals("test_SW", namingStrategy.getSwitchId("test"));
        assertEquals("test_SW_5", namingStrategy.getSwitchId("test", 5));
        assertEquals("test_SW_5_6", namingStrategy.getSwitchId("test", 5, 6));
    }

    @Test
    void testBusBar() {
        assertEquals("test_5_6", namingStrategy.getBusbarId("test", 5, 6));
    }

    @Test
    void testSwitchBaseId() {
        Network network = createNbNetwork();

        // On Connectable
        assertEquals("CB", namingStrategy.getSwitchBaseId(network.getConnectable("CB"), 0));
        assertEquals("CB1", namingStrategy.getSwitchBaseId(network.getConnectable("CB"), 1));

        // On VoltageLevel abs BusbarSections
        assertEquals("C", namingStrategy.getSwitchBaseId(network.getVoltageLevel("C"), network.getBusbarSection("D"), network.getBusbarSection("O")));
    }
}
