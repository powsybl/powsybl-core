/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultNamingStrategyTest {
    @Test
    void testName() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        assertEquals("Default", namingStrategy.getName());
    }

    @Test
    void testDisconnector() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

//        // Prefix
//        assertEquals("test", namingStrategy.getDisconnectorIdPrefix("test"));
//
//        // Suffix
//        assertEquals("5", namingStrategy.getDisconnectorIdSuffix(5));
//        assertEquals("5_6", namingStrategy.getDisconnectorIdSuffix(5, 6));
//
//        // Full Id
//        assertEquals("test_DISCONNECTOR", namingStrategy.getDisconnectorId("test"));
//        assertEquals("test_DISCONNECTOR_5", namingStrategy.getDisconnectorId("test", 5));
        assertEquals("test_DISCONNECTOR_5_6", namingStrategy.getDisconnectorId("test", 5, 6));
    }

    @Test
    void testDisconnectorWithBbs() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        Network network = createNbNetwork();

        assertEquals("test_DISCONNECTOR_5_6", namingStrategy.getDisconnectorId(network.getBusbarSection("D"), "test", 5, 6, false, 0));
    }

    @Test
    void testBreaker() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        // Prefix
//        assertEquals("test", namingStrategy.getBreakerIdPrefix("test"));
//
//        // Suffix
//        assertEquals("5", namingStrategy.getBreakerIdSuffix(5));
//        assertEquals("5_6", namingStrategy.getBreakerIdSuffix(5, 6));
//
//        // Full Id
        assertEquals("test_BREAKER", namingStrategy.getBreakerId("test"));
//        assertEquals("test_BREAKER_5", namingStrategy.getBreakerId("test", 5));
        assertEquals("test_BREAKER_5_6", namingStrategy.getBreakerId("test", 5, 6));
    }

    @Test
    void testSwitch() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

//        // Prefix
//        assertEquals("test", namingStrategy.getSwitchIdPrefix("test"));
//
//        // Suffix
//        assertEquals("5", namingStrategy.getSwitchIdSuffix(5));
//        assertEquals("5_6", namingStrategy.getSwitchIdSuffix(5, 6));

        // Full Id
        assertEquals("test_SW", namingStrategy.getSwitchId("test"));
        assertEquals("test_SW_5", namingStrategy.getSwitchId("test", 5));
        assertEquals("test_SW_5_6", namingStrategy.getSwitchId("test", 5, 6));
    }

    @Test
    void testBusBar() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        // Prefix
//        assertEquals("test", namingStrategy.getBusbarIdPrefix("test"));
//
//        // Suffix
//        assertEquals("5", namingStrategy.getBusbarIdSuffix(5));
//        assertEquals("5_6", namingStrategy.getBusbarIdSuffix(5, 6));
//
//        // Full Id
//        assertEquals("test", namingStrategy.getBusbarId("test"));
//        assertEquals("test_5", namingStrategy.getBusbarId("test", 5));
        assertEquals("test_5_6", namingStrategy.getBusbarId("test", 5, 6));
    }

    @Test
    void testSwitchBaseId() {
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        Network network = createNbNetwork();

        // On Connectable
        assertEquals("CB", namingStrategy.getSwitchBaseId(network.getConnectable("CB"), 0));
        assertEquals("CB1", namingStrategy.getSwitchBaseId(network.getConnectable("CB"), 1));

        // On VoltageLevel abs BusbarSections
        assertEquals("C", namingStrategy.getSwitchBaseId(network.getVoltageLevel("C"), network.getBusbarSection("D"), network.getBusbarSection("O")));
    }
}
