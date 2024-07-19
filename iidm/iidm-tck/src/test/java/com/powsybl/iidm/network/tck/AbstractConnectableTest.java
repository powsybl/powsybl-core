/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractConnectableTest {

    public Network createNetwork() {
        Network network = Network.create("test", "test");
        // Substations
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        VoltageLevel vl2 = s1.newVoltageLevel()
            .setId("VL2")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        VoltageLevel vl3 = s1.newVoltageLevel()
            .setId("VL3")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        // Busbar sections
        BusbarSection bbs11 = vl1.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS11")
            .setNode(0)
            .add();
        bbs11.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs21 = vl1.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS21")
            .setNode(1)
            .add();
        bbs21.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs12 = vl1.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS12")
            .setNode(2)
            .add();
        bbs12.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(2)
            .add();
        BusbarSection bbs22 = vl1.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS22")
            .setNode(3)
            .add();
        bbs22.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(2)
            .add();
        vl2.getBusBreakerView()
            .newBus()
            .setId("bus2A")
            .add();
        vl2.getBusBreakerView()
            .newBus()
            .setId("bus2B")
            .add();
        vl3.getBusBreakerView()
            .newBus()
            .setId("bus3A")
            .add();
        vl3.getBusBreakerView()
            .newBus()
            .setId("bus3B")
            .add();

        // Disconnectors for coupling
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_BBS11_BBS12")
            .setNode1(0)
            .setNode2(2)
            .setOpen(true)
            .add();
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_BBS21_BBS22")
            .setNode1(1)
            .setNode2(3)
            .setOpen(false)
            .add();

        // Line and transformer
        network.newLine()
            .setId("L1")
            .setName("LINE1")
            .setR(1.0)
            .setX(2.0)
            .setG1(3.0)
            .setG2(3.5)
            .setB1(4.0)
            .setB2(4.5)
            .setVoltageLevel1("VL1")
            .setVoltageLevel2("VL2")
            .setNode1(4)
            .setBus2("bus2A")
            .setConnectableBus2("bus2A")
            .add();
        network.newLine()
            .setId("L2")
            .setName("LINE2")
            .setR(1.0)
            .setX(2.0)
            .setG1(3.0)
            .setG2(3.5)
            .setB1(4.0)
            .setB2(4.5)
            .setVoltageLevel1("VL1")
            .setVoltageLevel2("VL3")
            .setNode1(5)
            .setBus2("bus3A")
            .setConnectableBus2("bus3A")
            .add();
        s1.newThreeWindingsTransformer()
            .setId("twt")
            .setName("TWT_NAME")
            .newLeg1()
            .setR(1.3)
            .setX(1.4)
            .setG(1.6)
            .setB(1.7)
            .setRatedU(1.1)
            .setRatedS(1.2)
            .setVoltageLevel("VL1")
            .setNode(6)
            .add()
            .newLeg2()
            .setR(2.03)
            .setX(2.04)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(2.05)
            .setRatedS(2.06)
            .setVoltageLevel("VL2")
            .setBus("bus2B")
            .setConnectableBus("bus2B")
            .add()
            .newLeg3()
            .setR(3.3)
            .setX(3.4)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(3.5)
            .setRatedS(3.6)
            .setVoltageLevel("VL3")
            .setBus("bus3B")
            .setConnectableBus("bus3B")
            .add()
            .add();

        // Breakers
        vl1.getNodeBreakerView().newBreaker()
            .setId("B_L1_1")
            .setNode1(4)
            .setNode2(7)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B_L1_2")
            .setNode1(4)
            .setNode2(7)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B_L2")
            .setNode1(5)
            .setNode2(8)
            .setOpen(true)
            .setFictitious(true)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B_TWT")
            .setNode1(6)
            .setNode2(9)
            .setOpen(true)
            .setFictitious(true)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B0")
            .setNode1(7)
            .setNode2(17)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B1")
            .setNode1(8)
            .setNode2(11)
            .setOpen(true)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B2")
            .setNode1(9)
            .setNode2(12)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B3")
            .setNode1(7)
            .setNode2(8)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B4")
            .setNode1(8)
            .setNode2(9)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B5")
            .setNode1(17)
            .setNode2(10)
            .setOpen(false)
            .setFictitious(true)
            .add();

        // Disconnectors
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D0")
            .setNode1(0)
            .setNode2(10)
            .setOpen(true)
            .add();
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D1")
            .setNode1(1)
            .setNode2(10)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D2")
            .setNode1(0)
            .setNode2(11)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D3")
            .setNode1(1)
            .setNode2(11)
            .setOpen(true)
            .add();
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D4")
            .setNode1(2)
            .setNode2(12)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D5")
            .setNode1(3)
            .setNode2(12)
            .setOpen(true)
            .add();
        return network;
    }

    @Test
    public void nominallyConnectedTest() {
        // Network creation
        Network network = createNetwork();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line1 = network.getLine("L1");

        // Line1 is fully connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        line1.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        line1.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));

        // Failing disconnection
        assertFalse(line1.disconnect(SwitchPredicates.IS_NONFICTIONAL_CLOSED_BREAKER));

        // Check that line1 is still fully connected
        line1.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));

        // disconnect the line 1
        assertTrue(line1.disconnect());

        // check line 1 is disconnected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        line1.getTerminals().forEach(terminal -> assertNull(terminal.getBusView().getBus()));
        line1.getTerminals().forEach(terminal -> assertFalse(terminal.isConnected()));

        // disconnect the already fully disconnected line 1
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        network.getReportNodeContext().pushReportNode(reportNode);
        assertFalse(line1.disconnect());
        network.getReportNodeContext().popReportNode();
        assertEquals("alreadyDisconnectedTerminal", reportNode.getChildren().get(0).getMessageKey());

        // Failing reconnection of the line 1
        assertFalse(line1.connect(SwitchPredicates.IS_NONFICTIONAL_CLOSED_BREAKER));

        // Check that line1 is still fully disconnected
        line1.getTerminals().forEach(terminal -> assertFalse(terminal.isConnected()));

        // Reconnect the line 1
        assertTrue(line1.connect());

        // check line 1 is connected
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        line1.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        line1.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));
    }

    @Test
    public void partiallyConnectedTest() {
        // Network creation
        Network network = createNetwork();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line2 = network.getLine("L2");
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("twt");

        // Line1 and twt are fully connected
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTrue(topo.getOptionalTerminal(6).isPresent());
        assertNotNull(line2.getTerminals().get(1).getBusView().getBus());
        assertNull(twt.getTerminals().get(0).getBusView().getBus());
        assertNotNull(twt.getTerminals().get(1).getBusView().getBus());
        assertNotNull(twt.getTerminals().get(2).getBusView().getBus());
        assertFalse(line2.getTerminals().get(0).isConnected());
        assertTrue(line2.getTerminals().get(1).isConnected());
        assertFalse(twt.getTerminals().get(0).isConnected());
        assertTrue(twt.getTerminals().get(1).isConnected());
        assertTrue(twt.getTerminals().get(2).isConnected());

        // Failing connection
        assertFalse(line2.connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER));

        // connect the line 2
        assertTrue(line2.connect(SwitchPredicates.IS_BREAKER_OR_DISCONNECTOR));

        // check line 2 is connected
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        line2.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        line2.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));

        // connect the already fully connected line 2
        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("reportTest", "Testing reportNode").build();
        network.getReportNodeContext().pushReportNode(reportNode);
        assertFalse(line2.connect());
        network.getReportNodeContext().popReportNode();
        assertEquals("alreadyConnectedTerminal", reportNode.getChildren().get(0).getMessageKey());

        // Disconnect the twt
        assertTrue(twt.disconnect(SwitchPredicates.IS_CLOSED_BREAKER));

        // check twt is disconnected
        assertTrue(topo.getOptionalTerminal(6).isPresent());
        twt.getTerminals().forEach(terminal -> assertNull(terminal.getBusView().getBus()));
        twt.getTerminals().forEach(terminal -> assertFalse(terminal.isConnected()));
    }

    @Test
    public void oneTerminalConnectedTest() {
        // Network creation
        Network network = createNetwork();

        // Useful elements
        Line line2 = network.getLine("L2");
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("twt");

        // Line1 and twt are fully connected
        assertFalse(line2.getTerminal1().isConnected());
        assertTrue(line2.getTerminal2().isConnected());
        assertFalse(twt.getTerminal(ThreeSides.ONE).isConnected());
        assertTrue(twt.getTerminal(ThreeSides.TWO).isConnected());
        assertTrue(twt.getTerminal(ThreeSides.THREE).isConnected());

        // Connection of side one only
        assertTrue(line2.connect(SwitchPredicates.IS_BREAKER, ThreeSides.ONE));
        assertTrue(line2.getTerminal1().isConnected());

        // disconnect the twt on side 3
        assertTrue(twt.disconnect(SwitchPredicates.IS_BREAKER_OR_DISCONNECTOR, ThreeSides.THREE));
        assertFalse(twt.getTerminal(ThreeSides.THREE).isConnected());
    }
}
