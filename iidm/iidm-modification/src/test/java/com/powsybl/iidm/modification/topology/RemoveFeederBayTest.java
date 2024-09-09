/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportConstants;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.powsybl.iidm.modification.topology.TopologyTestUtils.VLTEST;
import static com.powsybl.iidm.modification.topology.TopologyTestUtils.createNbNetwork;
import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class RemoveFeederBayTest {

    private final Set<String> removedObjects = new HashSet<>();
    private final Set<String> beforeRemovalObjects = new HashSet<>();

    @AfterEach
    void tearDown() {
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
    void testSimpleRemove() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        addListener(network);

        new RemoveFeederBayBuilder().withConnectableId("LD1").build().apply(network);

        assertEquals(Set.of("S1VL1_LD1_BREAKER", "S1VL1_BBS_LD1_DISCONNECTOR", "LD1"), beforeRemovalObjects);
        assertEquals(Set.of("S1VL1_LD1_BREAKER", "S1VL1_BBS_LD1_DISCONNECTOR", "LD1"), removedObjects);
    }

    @Test
    void testBbvRemove() {
        Network network = EurostagTutorialExample1Factory.create();
        addListener(network);

        new RemoveFeederBay("LOAD").apply(network);

        assertEquals(Set.of("LOAD"), beforeRemovalObjects);
        assertEquals(Set.of("LOAD"), removedObjects);
    }

    @Test
    void testRemoveWithForkFeeder() {
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
    void testRemoveWithShunt() {
        Network network = createNetwork2Feeders();
        addListener(network);

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newSwitch().setNode1(4).setNode2(6).setKind(SwitchKind.BREAKER).setId("SW1").add();

        new RemoveFeederBay("load2").apply(network);

        Set<String> removedIdentifiables = Set.of("SW1", "load2", "load2_DISCONNECTOR_6_0", "load2_DISCONNECTOR_6_1", "load2_DISCONNECTOR_6_2", "load2_BREAKER");
        assertEquals(removedIdentifiables, beforeRemovalObjects);
        assertEquals(removedIdentifiables, removedObjects);
    }

    @Test
    void testRemoveWithInternalConnectionShunt() {
        Network network = createNetwork2Feeders();
        addListener(network);

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newInternalConnection().setNode1(4).setNode2(6).add();

        new RemoveFeederBay("load2").apply(network);

        Set<String> removedIdentifiables = Set.of("load2", "load2_DISCONNECTOR_6_0", "load2_DISCONNECTOR_6_1", "load2_DISCONNECTOR_6_2", "load2_BREAKER");
        assertEquals(removedIdentifiables, beforeRemovalObjects);
        assertEquals(removedIdentifiables, removedObjects);
    }

    @Test
    void testRemoveWithDoubleShunt() {
        Network network = createNetwork3Feeders();
        addListener(network);

        VoltageLevel voltageLevel = network.getVoltageLevel(VLTEST);
        voltageLevel.getNodeBreakerView().newSwitch().setNode1(4).setNode2(6).setKind(SwitchKind.BREAKER).setId("SW1").add();
        voltageLevel.getNodeBreakerView().newSwitch().setNode1(6).setNode2(8).setKind(SwitchKind.BREAKER).setId("SW2").add();

        new RemoveFeederBay("load2").apply(network);

        Set<String> removedIdentifiables = Set.of("load2", "load2_DISCONNECTOR_6_0", "load2_DISCONNECTOR_6_1", "load2_DISCONNECTOR_6_2", "load2_BREAKER");
        assertEquals(removedIdentifiables, beforeRemovalObjects);
        assertEquals(removedIdentifiables, removedObjects);
        assertNotNull(network.getSwitch("SW1")); // Not removed as connect load1 with load3
    }

    @Test
    void testRemoveBbs() {
        Network network = createNetwork2Feeders();
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTestRemoveBbs", "Testing reportNode when trying to remove a busbar section").build();
        RemoveFeederBay removeBbs = new RemoveFeederBay("BBS_TEST_1_1");
        PowsyblException e = assertThrows(PowsyblException.class, () -> removeBbs.apply(network, true, reportNode));
        assertEquals("BusbarSection connectables are not allowed as RemoveFeederBay input: BBS_TEST_1_1", e.getMessage());
        assertEquals("removeBayBusbarSectionConnectable", reportNode.getChildren().get(0).getMessageKey());
        assertEquals("Cannot remove feeder bay for connectable ${connectableId}, as it is a busbarSection", reportNode.getChildren().get(0).getMessageTemplate());
        Map<String, TypedValue> values = reportNode.getChildren().get(0).getValues();
        assertEquals(2, values.size());
        assertEquals(TypedValue.ERROR_SEVERITY, values.get(ReportConstants.SEVERITY_KEY));
        assertEquals("BBS_TEST_1_1", values.get("connectableId").getValue());
        assertEquals(TypedValue.UNTYPED, values.get("connectableId").getType());
    }

    private Network createNetwork2Feeders() {
        Network network = createNbNetwork();
        CreateVoltageLevelTopology createVlTopology = new CreateVoltageLevelTopologyBuilder()
                .withVoltageLevelId(VLTEST)
                .withAlignedBusesOrBusbarCount(3)
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
                .withBusOrBusbarSectionId("BBS_TEST_1_1")
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
                .withBusOrBusbarSectionId("BBS_TEST_1_1")
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
                .withBusOrBusbarSectionId("BBS_TEST_1_1")
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

    /**
     *  Network with direct shunt in voltage level VL1 between line LINE1 and generator G1 :
     *
     *
     *             VOLTAGE LEVEL VL2
     *             =================
     *
     *                 VL2_BBS (node 0)
     *  ---------------------------------
     *     |
     *  disc. VL2_D1
     *     |
     *   node 1
     *     |
     *  break. VL2_B1
     *     |
     *  LINE1 (node 2)
     *     |
     *     |
     *     |                    VOLTAGE LEVEL VL1
     *     |                    =================
     *     |
     *  LINE1 (node 3)  ------------ disc. VL1_D3 ---------------- G1 (node 1)
     *     |                                                       |
     *  break. VL1_B2                                         break. VL1_B1
     *     |                                                       |
     *   node 4                                                 node 2
     *     |                                                       |
     *  disc. VL1_D2                                           disc. VL1_D1
     *     |                                                       |
     *     |                    VL1_BBS (node 0)                   |
     *  ---------------------------------------------------------------------
     *
     */
    private static Network createNetworkWithShuntRemoveLinePb() {
        Network network = Network.create("test", "test");

        VoltageLevel vl1 = network.newVoltageLevel().setId("VL1").setNominalV(400.0).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl1.getNodeBreakerView().newBusbarSection().setId("VL1_BBS").setNode(0).add();
        vl1.newGenerator().setId("G1").setNode(1).setTargetP(0).setVoltageRegulatorOn(true).setTargetV(400).setMinP(0).setMaxP(10).add();
        vl1.getNodeBreakerView().newBreaker().setId("VL1_B1").setNode1(1).setNode2(2).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_D1").setNode1(0).setNode2(2).add();
        vl1.getNodeBreakerView().newBreaker().setId("VL1_B2").setNode1(3).setNode2(4).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_D2").setNode1(0).setNode2(4).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_D3").setNode1(1).setNode2(3).add();

        VoltageLevel vl2 = network.newVoltageLevel().setId("VL2").setNominalV(400.0).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl2.getNodeBreakerView().newBusbarSection().setId("VL2_BBS").setNode(0).add();
        vl2.getNodeBreakerView().newBreaker().setId("VL2_B1").setNode1(1).setNode2(2).add();
        vl2.getNodeBreakerView().newDisconnector().setId("VL2_D1").setNode1(0).setNode2(1).add();

        network.newLine().setId("LINE1").setR(0.1).setX(0.1).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0)
                .setNode1(3).setVoltageLevel1("VL1")
                .setNode2(2).setVoltageLevel2("VL2")
                .add();

        return network;
    }

    @Test
    void testNetworkWithShuntRemoveLinePb() {
        Network network = createNetworkWithShuntRemoveLinePb();
        addListener(network);

        // removing line 'LINE1'
        new RemoveFeederBay("LINE1").apply(network);

        Set<String> removedIdentifiables = Set.of("VL2_B1", "VL2_D1", "LINE1", "VL1_B2", "VL1_D2", "VL1_D3");
        assertEquals(removedIdentifiables, beforeRemovalObjects);
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new RemoveFeederBay("LINE1");
        assertEquals("RemoveFeederBay", networkModification.getName());
    }
}
