/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.ReportNodeRootBuilderImpl;
import com.powsybl.iidm.modification.topology.AbstractModificationTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ConnectionAndDisconnectionsTest extends AbstractModificationTest {

    public Network createNetwork() {
        // Initialisation
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2023-12-13T10:05:55.570Z"));

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
            .setFictitious(true)
            .setOpen(true)
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
    void testPlannedDisconnection() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        PlannedDisconnection modification = new PlannedDisconnectionBuilder()
            .withConnectableId("L1")
            .build();
        modification.apply(network);
        writeXmlTest(network, "/network-planned-disconnection.xiidm");
    }

    @Test
    void testPlannedDisconnectionComplete() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        ReportNode reportNode = new ReportNodeRootBuilderImpl().withMessageTemplate("reportPlannedDisconnectionComplete", "Testing reportNode for connectable disconnection").build();
        PlannedDisconnection modification = new PlannedDisconnectionBuilder()
            .withConnectableId("L1")
            .withFictitiousSwitchesOperable(true)
            .build();
        modification.apply(network, reportNode);
        writeXmlTest(network, "/network-disconnection-with-fictitious.xiidm");
        testReportNode(reportNode, "/reportNode/connectable-disconnected-planned.txt");
    }

    @Test
    void testPlannedDisconnectionNoDisconnection() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Set a disconnector to fictitious
        network.getSwitch("D1").setFictitious(true);

        // Network modification
        ReportNode reportNode = new ReportNodeRootBuilderImpl().withMessageTemplate("reportTestConnectionNoDisconnection", "Testing reportNode for connectable disconnection").build();
        PlannedDisconnection modification = new PlannedDisconnectionBuilder()
            .withConnectableId("L1")
            .withFictitiousSwitchesOperable(false)
            .build();
        modification.apply(network, reportNode);
        writeXmlTest(network, "/network-planned-disconnection-not-disconnected.xiidm");
        testReportNode(reportNode, "/reportNode/connectable-not-disconnected-planned.txt");
    }

    @Test
    void testUnplannedDisconnection() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        ReportNode reportNode = new ReportNodeRootBuilderImpl().withMessageTemplate("reportTestConnectionDisconnection", "Testing reportNode for connectable disconnection").build();
        UnplannedDisconnection modification = new UnplannedDisconnectionBuilder()
            .withConnectableId("L1")
            .withFictitiousSwitchesOperable(true)
            .build();
        modification.apply(network, reportNode);
        writeXmlTest(network, "/network-disconnection-with-fictitious.xiidm");
        testReportNode(reportNode, "/reportNode/connectable-disconnected-unplanned.txt");
    }

    @Test
    void testUnplannedDisconnectionNoDisconnection() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        ReportNode reportNode = new ReportNodeRootBuilderImpl().withMessageTemplate("reportTestConnectionNoDisconnection", "Testing reportNode for connectable disconnection").build();
        UnplannedDisconnection modification = new UnplannedDisconnectionBuilder()
            .withConnectableId("L1")
            .withFictitiousSwitchesOperable(false)
            .build();
        modification.apply(network, reportNode);
        writeXmlTest(network, "/network-unplanned-disconnection-not-disconnected.xiidm");
        testReportNode(reportNode, "/reportNode/connectable-not-disconnected-unplanned.txt");
    }

    @Test
    void testConnection() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        ReportNode reportNode = new ReportNodeRootBuilderImpl().withMessageTemplate("reportTestConnection", "Testing reportNode for connectable connection").build();
        ConnectableConnection modification = new ConnectableConnectionBuilder()
            .withConnectableId("L2")
            .withFictitiousSwitchesOperable(true)
            .withOnlyBreakersOperable(false)
            .build();
        modification.apply(network, reportNode);
        writeXmlTest(network, "/network-connectable-connection.xiidm");
        testReportNode(reportNode, "/reportNode/connectable-connected.txt");
    }

    @Test
    void testConnectionNoConnection() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        ReportNode reportNode = new ReportNodeRootBuilderImpl().withMessageTemplate("reportTestConnectionNoConnection", "Testing reportNode for connectable connection").build();
        ConnectableConnection modification = new ConnectableConnectionBuilder()
            .withConnectableId("L2")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .build();
        modification.apply(network, reportNode);
        writeXmlTest(network, "/network-unplanned-disconnection-not-disconnected.xiidm");
        testReportNode(reportNode, "/reportNode/connectable-not-connected.txt");
    }
}
