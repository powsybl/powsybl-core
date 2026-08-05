/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NodeBreakerCleanTest {

    @Test
    void removeSwitchAndCleanTopology() {
        Network network = NetworkTest1Factory.create();

        VoltageLevel vl = network.getVoltageLevel("voltageLevel1");
        assertNotNull(vl);

        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        assertEquals(6, topo.getMaximumNodeIndex());

        assertNotNull(topo.getSwitch("load1Disconnector1"));
        assertNotNull(topo.getSwitch("load1Breaker1"));

        // Remove the switches linked to an intermediate node (3)
        topo.removeSwitch("load1Disconnector1");
        topo.removeSwitch("load1Breaker1");

        // Check the 2 switches and that the node capacity and maximum node index have not changed
        assertNull(topo.getSwitch("load1Disconnector1"));
        assertNull(topo.getSwitch("load1Breaker1"));
        assertEquals(6, topo.getMaximumNodeIndex());

        assertNull(network.getSwitch("load1Disconnector1"));
        assertNull(network.getSwitch("load1Breaker1"));
        assertNull(network.getIdentifiable("load1Disconnector1"));
        assertNull(network.getIdentifiable("load1Breaker1"));

        // Remove the switches linked to the maximum node (6)
        topo.removeSwitch("generator1Disconnector1");
        topo.removeSwitch("generator1Breaker1");

        // Check the 2 switches and that the node capacity and maximum node index have changed
        assertNull(topo.getSwitch("generator1Disconnector1"));
        assertNull(topo.getSwitch("generator1Breaker1"));
        assertEquals(5, topo.getMaximumNodeIndex());
    }

    @Test
    void removeSwitchInducingSeveralBusesInTopology() {
        Network network = NetworkTest1Factory.create();
        VoltageLevel vl = network.newVoltageLevel().setId("VL1").setNominalV(400).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newSwitch().setNode1(0).setNode2(1).setId("SW1").setKind(SwitchKind.BREAKER).setOpen(false).add();
        vl.getNodeBreakerView().newBusbarSection().setNode(0).setId("BBS1").setName("BBS1").add();
        vl.getNodeBreakerView().newBusbarSection().setNode(1).setId("BBS2").setName("BBS2").add();
        vl.newLoad().setNode(3).setId("LD1").setP0(100).setQ0(100).add();
        vl.newLoad().setNode(4).setId("LD2").setP0(100).setQ0(100).add();
        vl.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(3).add();
        vl.getNodeBreakerView().newInternalConnection().setNode1(1).setNode2(4).add();
        assertTrue(vl.getBusView().getBusStream().toList().stream().map(Bus::getId).toList().containsAll(List.of("VL1_0")));
        vl.getNodeBreakerView().removeSwitch("SW1");
        assertTrue(vl.getBusView().getBusStream().toList().stream().map(Bus::getId).toList().containsAll(List.of("VL1_0", "VL1_1")));
    }
}
