/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.After;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.VLTEST;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static org.junit.Assert.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class RemoveFeederBayTest {

    private final Set<String> removedObjects = new HashSet<>();
    private final Set<String> beforeRemovalObjects = new HashSet<>();

    @After
    public void tearDown() {
        removedObjects.clear();
    }

    private void addListener(Network network) {
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void beforeRemoval(Identifiable id) {
                beforeRemovalObjects.add(id.getId());
            }

            @Override
            public void afterRemoval(String id) {
                removedObjects.add(id);
            }
        });
    }

    @Test
    public void testSimpleRemove() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        addListener(network);

        new RemoveFeederBay("LD1").apply(network);

        assertEquals(Set.of("S1VL1_LD1_BREAKER", "S1VL1_BBS_LD1_DISCONNECTOR", "LD1"), beforeRemovalObjects);
        assertEquals(Set.of("S1VL1_LD1_BREAKER", "S1VL1_BBS_LD1_DISCONNECTOR", "LD1"), removedObjects);
    }

    @Test
    public void testBbvRemove() {
        Network network = EurostagTutorialExample1Factory.create();
        addListener(network);

        new RemoveFeederBay("LOAD").apply(network);

        assertEquals(Set.of("LOAD"), beforeRemovalObjects);
        assertEquals(Set.of("LOAD"), removedObjects);
    }

    @Test
    public void testRemoveWithForkFeeder() {
        Network network = createNetworkWithForkFeeder();
        addListener(network);

        new RemoveFeederBay("LD").apply(network);

        assertEquals(Set.of("B1", "LD"), beforeRemovalObjects);
        assertEquals(Set.of("B1", "LD"), removedObjects);
        assertNull(network.getLoad("LD"));
        assertNull(network.getSwitch("B1"));
        assertNotNull(network.getGenerator("G"));
        assertNotNull(network.getBusbarSection("BBS"));
        assertNotNull(network.getSwitch("B2"));
        assertNotNull(network.getSwitch("D"));
    }

    @Test
    public void testRemoveWithShunt() {
        Network network = createNetwork2Feeders();
        addListener(network);

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newSwitch().setNode1(4).setNode2(6).setKind(SwitchKind.BREAKER).setId("SW1").add();

        new RemoveFeederBay("load2").apply(network);

        Set<String> removedIdentifiables = Set.of("SW1", "load2", "load2_DISCONNECTOR", "load2_DISCONNECTOR_6_1", "load2_DISCONNECTOR_6_2", "load2_BREAKER");
        assertEquals(removedIdentifiables, beforeRemovalObjects);
        assertEquals(removedIdentifiables, removedObjects);
    }

    @Test
    public void testRemoveWithInternalConnectionShunt() {
        Network network = createNetwork2Feeders();
        addListener(network);

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newInternalConnection().setNode1(4).setNode2(6).add();

        new RemoveFeederBay("load2").apply(network);

        Set<String> removedIdentifiables = Set.of("load2", "load2_DISCONNECTOR", "load2_DISCONNECTOR_6_1", "load2_DISCONNECTOR_6_2", "load2_BREAKER");
        assertEquals(removedIdentifiables, beforeRemovalObjects);
        assertEquals(removedIdentifiables, removedObjects);
    }

    @Test
    public void testRemoveWithDoubleShunt() {
        Network network = createNetwork3Feeders();
        addListener(network);

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newSwitch().setNode1(4).setNode2(6).setKind(SwitchKind.BREAKER).setId("SW1").add();
        voltageLevel.getNodeBreakerView().newSwitch().setNode1(6).setNode2(8).setKind(SwitchKind.BREAKER).setId("SW2").add();

        new RemoveFeederBay("load2").apply(network);

        Set<String> removedIdentifiables = Set.of("load2", "load2_DISCONNECTOR", "load2_DISCONNECTOR_6_1", "load2_DISCONNECTOR_6_2", "load2_BREAKER");
        assertEquals(removedIdentifiables, beforeRemovalObjects);
        assertEquals(removedIdentifiables, removedObjects);
        assertNotNull(network.getSwitch("SW1")); // Not removed as connect load1 with load3
    }

    @Test
    public void testRemoveBbs() {
        Network network = createNetwork2Feeders();
        RemoveFeederBay removeBbs = new RemoveFeederBay("BBS_TEST_1_1");
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeBbs.apply(network, true, Reporter.NO_OP));
        assertEquals("BusbarSection connectables are not allowed as RemoveFeederBay input: BBS_TEST_1_1", e.getMessage());
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

    /**
     *   L(1)   G(2)
     *   |       |
     *   B1      B2
     *   |       |
     *   ----3----
     *       |
     *       D
     *       |
     *       BBS(0)
     */
    private static Network createNetworkWithForkFeeder() {
        Network network = Network.create("test", "test");
        VoltageLevel vl = network.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        vl.newLoad()
                .setId("LD")
                .setNode(1)
                .setP0(0)
                .setQ0(0)
                .add();
        vl.newGenerator()
                .setId("G")
                .setNode(2)
                .setTargetP(0)
                .setVoltageRegulatorOn(true)
                .setTargetV(400)
                .setMinP(0)
                .setMaxP(10)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D")
                .setNode1(0)
                .setNode2(3)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B1")
                .setNode1(1)
                .setNode2(3)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B2")
                .setNode1(2)
                .setNode2(3)
                .add();
        return network;
    }
}
