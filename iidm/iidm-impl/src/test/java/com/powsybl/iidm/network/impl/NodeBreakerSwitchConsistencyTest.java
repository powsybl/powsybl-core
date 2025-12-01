/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify consistency between getSwitches(), getSwitchStream(), and
 * getSwitchCount() methods in node-breaker topology. These methods should all exclude
 * InternalConnections and only return real voltage level switches.
 *
 * @author Giovanni Ferrari {@literal <giovanni.ferrari at soft.it>}
 */
class NodeBreakerSwitchConsistencyTest {
    private Network network;

    @BeforeEach
    void setUp() {
        network = Network.create("testSwitchConsistency", "test");
        Substation s = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL1")
                .setNominalV(10.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        // Create busbar sections
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(2)
                .add();

        // Create 2 switches
        vl.getNodeBreakerView().newBreaker()
                .setId("BREAKER1")
                .setNode1(0)
                .setNode2(1)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("DISCONNECTOR1")
                .setNode1(1)
                .setNode2(2)
                .add();
    }

    @Test
    void testSwitchConsistencyWithoutInternalConnections() {
        VoltageLevel vl = network.getVoltageLevel("VL1");
        vl.getNodeBreakerView().newBreaker()
                .setId("BREAKER2")
                .setNode1(2)
                .setNode2(3)
                .add();

        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();

        // Verify all three methods return/count the same number of switches
        int switchCount = topo.getSwitchCount();

        Iterable<Switch> switchIterable = topo.getSwitches();
        long switchesSize = StreamSupport.stream(switchIterable.spliterator(), false).count();
        long switchStreamCount = topo.getSwitchStream().count();

        assertEquals(3, switchCount, "getSwitchCount() should return 3 switches");
        assertEquals(3, switchesSize, "getSwitches() should return 3 switches");
        assertEquals(3, switchStreamCount, "getSwitchStream() should return 3 switches");

        // Verify consistency between the three methods
        assertEquals(switchCount, switchesSize, "getSwitchCount() and getSwitches() should be consistent");
        assertEquals(switchCount, switchStreamCount,
                "getSwitchCount() and getSwitchStream() should be consistent");
    }

    @Test
    void testSwitchConsistencyWithInternalConnections() {
        VoltageLevel vl = network.getVoltageLevel("VL1");

        // Add 3 internal connections
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(3)
                .setNode2(4)
                .add();
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(5)
                .setNode2(6)
                .add();
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(7)
                .setNode2(8)
                .add();

        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();

        // Verify that we have 2 switches and 3 internal connections
        assertEquals(3, topo.getInternalConnectionCount(), "Should have 3 internal connections");

        // Verify all three methods return/count only the switches, excluding internal
        // connections
        int switchCount = topo.getSwitchCount();
        Iterable<Switch> switchIterable = topo.getSwitches();
        long switchesSize = StreamSupport.stream(switchIterable.spliterator(), false).count();
        long switchStreamCount = topo.getSwitchStream().count();

        assertEquals(2, switchCount,
                "getSwitchCount() should return 2 switches (excluding internal connections)");
        assertEquals(2, switchesSize,
                "getSwitches() should return 2 switches (excluding internal connections)");
        assertEquals(2, switchStreamCount,
                "getSwitchStream() should return 2 switches (excluding internal connections)");

        // Verify consistency between the three methods
        assertEquals(switchCount, switchesSize, "getSwitchCount() and getSwitches() should be consistent");
        assertEquals(switchCount, switchStreamCount,
                "getSwitchCount() and getSwitchStream() should be consistent");
    }

}
