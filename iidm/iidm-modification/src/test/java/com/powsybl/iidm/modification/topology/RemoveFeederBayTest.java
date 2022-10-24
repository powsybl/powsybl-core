/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.VLTEST;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class RemoveFeederBayTest {

    @Test
    public void test2Feeders() {
        Network network = createNetwork2Feeders();

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newSwitch().setNode1(4).setNode2(6).setKind(SwitchKind.BREAKER).setId("SW1").add();

        assertNotNull(network.getSwitch("SW1"));
        assertNotNull(network.getSwitch("load2_DISCONNECTOR_6_1"));
        assertNotNull(network.getSwitch("load2_DISCONNECTOR_6_2"));
        assertNotNull(network.getSwitch("load2_BREAKER"));
        assertNotNull(network.getLoad("load2"));

        new RemoveFeederBay("load2").apply(network);
        assertNull(network.getSwitch("SW1"));
        assertNull(network.getSwitch("load2_DISCONNECTOR_6_1"));
        assertNull(network.getSwitch("load2_DISCONNECTOR_6_2"));
        assertNull(network.getSwitch("load2_BREAKER"));
        assertNull(network.getLoad("load2"));
    }

    @Test
    public void test2FeedersInternalConnection() {
        Network network = createNetwork2Feeders();

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newInternalConnection().setNode1(4).setNode2(6).add();

        assertNotNull(network.getSwitch("load2_DISCONNECTOR_6_1"));
        assertNotNull(network.getSwitch("load2_DISCONNECTOR_6_2"));
        assertNotNull(network.getSwitch("load2_BREAKER"));
        assertNotNull(network.getLoad("load2"));

        new RemoveFeederBay("load2").apply(network);

        assertNull(network.getSwitch("load2_DISCONNECTOR_6_1"));
        assertNull(network.getSwitch("load2_DISCONNECTOR_6_2"));
        assertNull(network.getSwitch("load2_BREAKER"));
        assertNull(network.getLoad("load2"));
    }

    @Test
    public void test3Feeders() {
        Network network = createNetwork3Feeders();

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newSwitch().setNode1(4).setNode2(6).setKind(SwitchKind.BREAKER).setId("SW1").add();
        voltageLevel.getNodeBreakerView().newSwitch().setNode1(6).setNode2(8).setKind(SwitchKind.BREAKER).setId("SW2").add();

        assertNotNull(network.getSwitch("SW1"));
        assertNotNull(network.getSwitch("load2_DISCONNECTOR"));
        assertNotNull(network.getSwitch("load2_DISCONNECTOR_6_1"));
        assertNotNull(network.getSwitch("load2_DISCONNECTOR_6_2"));
        assertNotNull(network.getSwitch("load2_BREAKER"));
        assertNotNull(network.getLoad("load2"));

        new RemoveFeederBay("load2").apply(network);

        assertNotNull(network.getSwitch("SW1")); // Not removed as connect load1 with load3
        assertNull(network.getSwitch("load2_DISCONNECTOR_6"));
        assertNull(network.getSwitch("load2_DISCONNECTOR_6_1"));
        assertNull(network.getSwitch("load2_DISCONNECTOR_6_2"));
        assertNull(network.getSwitch("load2_BREAKER"));
        assertNull(network.getLoad("load2"));
    }

    @Test
    public void test3FeedersInternalConnection() {
        Network network = createNetwork3Feeders();

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newInternalConnection().setNode1(4).setNode2(6).add();
        voltageLevel.getNodeBreakerView().newInternalConnection().setNode1(6).setNode2(8).add();

        assertNotNull(network.getSwitch("load2_DISCONNECTOR"));
        assertNotNull(network.getSwitch("load2_DISCONNECTOR_6_1"));
        assertNotNull(network.getSwitch("load2_DISCONNECTOR_6_2"));
        assertNotNull(network.getSwitch("load2_BREAKER"));
        assertNotNull(network.getLoad("load2"));

        new RemoveFeederBay("load2").apply(network);

        assertNull(network.getSwitch("load2_DISCONNECTOR_6"));
        assertNull(network.getSwitch("load2_DISCONNECTOR_6_1"));
        assertNull(network.getSwitch("load2_DISCONNECTOR_6_2"));
        assertNull(network.getSwitch("load2_BREAKER"));
        assertNull(network.getLoad("load2"));
    }

    private Network createNetwork2Feeders() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology createVlTopology = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withBusbarCount(3)
                .withSectionCount(1)
                .withBusbarSectionPrefixId("BBS_TEST")
                .withSwitchPrefixId("SW_TEST")
                .withSwitchKinds()
                .build();
        createVlTopology.apply(network);

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);

        LoadAdder loadAdder = voltageLevel.newLoad()
                .setId("load1")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder)
                .withBbsId("BBS_TEST_1_1")
                .withInjectionPositionOrder(10)
                .withInjectionFeederName("L1")
                .withInjectionDirection(BOTTOM)
                .build();
        modification.apply(network);

        LoadAdder loadAdder2 = voltageLevel.newLoad()
                .setId("load2")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification2 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder2)
                .withBbsId("BBS_TEST_1_1")
                .withInjectionPositionOrder(20)
                .withInjectionFeederName("L2")
                .withInjectionDirection(BOTTOM)
                .build();
        modification2.apply(network);

        return network;
    }

    private Network createNetwork3Feeders() {
        Network network = createNetwork2Feeders();

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        LoadAdder loadAdder3 = voltageLevel.newLoad()
                .setId("load3")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(0)
                .setQ0(0);
        NetworkModification modification3 = new CreateFeederBayBuilder()
                .withInjectionAdder(loadAdder3)
                .withBbsId("BBS_TEST_1_1")
                .withInjectionPositionOrder(30)
                .withInjectionFeederName("L3")
                .withInjectionDirection(BOTTOM)
                .build();
        modification3.apply(network);

        return network;
    }
}