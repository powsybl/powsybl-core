/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.PowsyblCoreTestReportResourceBundles;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.topology.AbstractModificationTest;
import com.powsybl.iidm.modification.topology.CreateFeederBayBuilder;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.network.extensions.ConnectablePosition.Direction.BOTTOM;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
            .withIdentifiableId("L1")
            .build();
        modification.apply(network);
        writeXmlTest(network, "/network-planned-disconnection.xiidm");
    }

    @Test
    void testPlannedDisconnectionComplete() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreTestReportResourceBundles.MESSAGE_TEMPLATE_PROVIDER_TEST)
                .withMessageTemplate("reportPlannedDisconnectionComplete")
                .build();
        PlannedDisconnection modification = new PlannedDisconnectionBuilder()
            .withIdentifiableId("L1")
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
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreTestReportResourceBundles.MESSAGE_TEMPLATE_PROVIDER_TEST)
                .withMessageTemplate("reportTestConnectionNoDisconnection")
                .build();
        PlannedDisconnection modification = new PlannedDisconnectionBuilder()
            .withIdentifiableId("L1")
            .withFictitiousSwitchesOperable(false)
            .build();
        modification.apply(network, reportNode);
        writeXmlTest(network, "/network-planned-disconnection-not-disconnected.xiidm");

        // Network modification
        PlannedDisconnection modificationSide1 = new PlannedDisconnectionBuilder()
            .withIdentifiableId("L1")
            .withFictitiousSwitchesOperable(false)
            .withSide(ThreeSides.ONE)
            .build();
        modificationSide1.apply(network, reportNode);
        writeXmlTest(network, "/network-planned-disconnection-not-disconnected.xiidm");
        testReportNode(reportNode, "/reportNode/connectable-not-disconnected-planned.txt");
    }

    @Test
    void testUnplannedDisconnection() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreTestReportResourceBundles.MESSAGE_TEMPLATE_PROVIDER_TEST)
                .withMessageTemplate("reportTestConnectionDisconnection")
                .build();
        UnplannedDisconnection modification = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("L1")
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
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreTestReportResourceBundles.MESSAGE_TEMPLATE_PROVIDER_TEST)
                .withMessageTemplate("reportTestConnectionNoDisconnection")
                .build();
        UnplannedDisconnection modification = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("L1")
            .withFictitiousSwitchesOperable(false)
            .build();
        modification.apply(network, reportNode);
        writeXmlTest(network, "/network-unplanned-disconnection-not-disconnected.xiidm");

        // Network modification
        UnplannedDisconnection modificationSide1 = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("L1")
            .withFictitiousSwitchesOperable(false)
            .withSide(ThreeSides.ONE)
            .build();
        modificationSide1.apply(network, reportNode);
        writeXmlTest(network, "/network-unplanned-disconnection-not-disconnected.xiidm");
        testReportNode(reportNode, "/reportNode/connectable-not-disconnected-unplanned.txt");
    }

    @Test
    void testConnection() throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreTestReportResourceBundles.MESSAGE_TEMPLATE_PROVIDER_TEST)
                .withMessageTemplate("reportTestConnection")
                .build();
        ConnectableConnection modification = new ConnectableConnectionBuilder()
            .withIdentifiableId("L2")
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
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreTestReportResourceBundles.MESSAGE_TEMPLATE_PROVIDER_TEST)
                .withMessageTemplate("reportTestConnectionNoConnection")
                .build();
        ConnectableConnection modification = new ConnectableConnectionBuilder()
            .withIdentifiableId("L2")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .build();
        modification.apply(network, reportNode);
        writeXmlTest(network, "/network-unplanned-disconnection-not-disconnected.xiidm");

        // Connection on one side
        ConnectableConnection modificationSide1 = new ConnectableConnectionBuilder()
            .withIdentifiableId("L2")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .withSide(ThreeSides.ONE)
            .build();
        modificationSide1.apply(network, reportNode);
        writeXmlTest(network, "/network-unplanned-disconnection-not-disconnected.xiidm");
        testReportNode(reportNode, "/reportNode/connectable-not-connected.txt");
    }

    @Test
    void testTieLine() {
        Network network = createNetwork();

        // Add tie line
        DanglingLine nhv1xnode1 = network.getVoltageLevel("VL2").newDanglingLine()
            .setId("NHV1_XNODE1")
            .setP0(0.0)
            .setQ0(0.0)
            .setR(1.5)
            .setX(20.0)
            .setG(1E-6)
            .setB(386E-6 / 2)
            .setBus("bus2A")
            .setPairingKey("XNODE1")
            .add();
        DanglingLine xnode1nhv2 = network.getVoltageLevel("VL3").newDanglingLine()
            .setId("XNODE1_NHV2")
            .setP0(0.0)
            .setQ0(0.0)
            .setR(1.5)
            .setX(13.0)
            .setG(2E-6)
            .setB(386E-6 / 2)
            .setBus("bus3A")
            .setPairingKey("XNODE1")
            .add();
        TieLine tieLine = network.newTieLine()
            .setId("NHV1_NHV2_1")
            .setDanglingLine1(nhv1xnode1.getId())
            .setDanglingLine2(xnode1nhv2.getId())
            .add();

        // Disconnection
        assertTieLineConnection(tieLine, true, true);
        UnplannedDisconnection disconnection = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(false)
            .build();
        disconnection.apply(network);
        assertTieLineConnection(tieLine, false, false);

        // Connection
        ConnectableConnection connection = new ConnectableConnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .build();
        connection.apply(network);
        assertTieLineConnection(tieLine, true, true);

        // Disconnection on one side
        UnplannedDisconnection disconnectionSide1 = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(false)
            .withSide(ThreeSides.ONE)
            .build();
        disconnectionSide1.apply(network);
        assertTieLineConnection(tieLine, false, true);

        // Connection on the same side
        ConnectableConnection connectionSide1 = new ConnectableConnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .withSide(ThreeSides.ONE)
            .build();
        connectionSide1.apply(network);
        assertTieLineConnection(tieLine, true, true);
    }

    @Test
    void testHvdcLine() {
        Network network = HvdcTestNetwork.createLcc();
        HvdcLine hvdcLine = network.getHvdcLine("L");

        // Disconnection
        assertHvdcLineConnection(hvdcLine, true, true);
        UnplannedDisconnection disconnection = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .build();
        disconnection.apply(network);
        assertHvdcLineConnection(hvdcLine, false, false);

        // Connection
        ConnectableConnection connection = new ConnectableConnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .build();
        connection.apply(network);
        assertHvdcLineConnection(hvdcLine, true, true);

        // Disconnection on one side
        UnplannedDisconnection disconnectionSide2 = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .withSide(ThreeSides.TWO)
            .build();
        disconnectionSide2.apply(network);
        assertHvdcLineConnection(hvdcLine, true, false);

        // Connection on the same side
        ConnectableConnection connectionSide2 = new ConnectableConnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .withSide(ThreeSides.TWO)
            .build();
        connectionSide2.apply(network);
        assertHvdcLineConnection(hvdcLine, true, true);
    }

    private void assertTieLineConnection(TieLine tieLine, boolean expectedConnectionOnSide1, boolean expectedConnectionOnSide2) {
        assertEquals(expectedConnectionOnSide1, tieLine.getDanglingLine1().getTerminal().isConnected());
        assertEquals(expectedConnectionOnSide2, tieLine.getDanglingLine2().getTerminal().isConnected());
    }

    private void assertHvdcLineConnection(HvdcLine hvdcLine, boolean expectedConnectionOnSide1, boolean expectedConnectionOnSide2) {
        assertEquals(expectedConnectionOnSide1, hvdcLine.getConverterStation1().getTerminal().isConnected());
        assertEquals(expectedConnectionOnSide2, hvdcLine.getConverterStation2().getTerminal().isConnected());
    }

    @Test
    void testIdentifiableNotFoundException() {
        Network network = createNetwork();
        UnplannedDisconnection disconnection = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("ELEMENT_NOT_PRESENT")
            .withFictitiousSwitchesOperable(false)
            .build();

        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        ComputationManager computationManager = LocalComputationManager.getDefault();
        assertDoesNotThrow(() -> disconnection.apply(network, namingStrategy, false, computationManager, ReportNode.NO_OP));
        PowsyblException disconnectionException = assertThrows(PowsyblException.class, () -> disconnection.apply(network, true, ReportNode.NO_OP));
        assertEquals("Identifiable 'ELEMENT_NOT_PRESENT' not found", disconnectionException.getMessage());

        ConnectableConnection connection = new ConnectableConnectionBuilder()
            .withIdentifiableId("ELEMENT_NOT_PRESENT")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .build();
        assertDoesNotThrow(() -> connection.apply(network, namingStrategy, false, computationManager, ReportNode.NO_OP));
        PowsyblException connectionException = assertThrows(PowsyblException.class, () -> connection.apply(network, true, ReportNode.NO_OP));
        assertEquals("Identifiable 'ELEMENT_NOT_PRESENT' not found", connectionException.getMessage());
    }

    @Test
    void testMethodNotImplemented() {
        Network network = createNetwork();
        UnplannedDisconnection disconnection = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("S1")
            .withFictitiousSwitchesOperable(false)
            .build();

        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        ComputationManager computationManager = LocalComputationManager.getDefault();
        assertDoesNotThrow(() -> disconnection.apply(network, namingStrategy, false, computationManager, ReportNode.NO_OP));
        PowsyblException disconnectionException = assertThrows(PowsyblException.class, () -> disconnection.apply(network, true, ReportNode.NO_OP));
        assertEquals("Disconnection not implemented for identifiable 'S1'", disconnectionException.getMessage());

        ConnectableConnection connection = new ConnectableConnectionBuilder()
            .withIdentifiableId("S1")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .build();

        assertDoesNotThrow(() -> connection.apply(network, namingStrategy, false, computationManager, ReportNode.NO_OP));
        PowsyblException connectionException = assertThrows(PowsyblException.class, () -> connection.apply(network, true, ReportNode.NO_OP));
        assertEquals("Connection not implemented for identifiable 'S1'", connectionException.getMessage());
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new ConnectableConnection("ID", false, false, ThreeSides.ONE);
        assertEquals("ConnectableConnection", networkModification.getName());

        networkModification = new UnplannedDisconnection("ID", false, ThreeSides.ONE);
        assertEquals("UnplannedDisconnection", networkModification.getName());

        networkModification = new PlannedDisconnection("ID", false, ThreeSides.ONE);
        assertEquals("PlannedDisconnection", networkModification.getName());
    }

    @Test
    void testHasImpactCannotBeApplied() {
        Network network = createNetwork();

        // Unknown element
        UnplannedDisconnection disconnection = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("ELEMENT_NOT_PRESENT")
            .withFictitiousSwitchesOperable(false)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, disconnection.hasImpactOnNetwork(network));
        ConnectableConnection connectionUnknownElement = new ConnectableConnectionBuilder()
            .withIdentifiableId("ELEMENT_NOT_PRESENT")
            .withFictitiousSwitchesOperable(false)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, connectionUnknownElement.hasImpactOnNetwork(network));

        // Not a connectable, nor a TieLine nor a HvdcLine
        UnplannedDisconnection voltageLevelDisconnection = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("VL1")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, voltageLevelDisconnection.hasImpactOnNetwork(network));
        ConnectableConnection connectionVoltageLevel = new ConnectableConnectionBuilder()
            .withIdentifiableId("VL1")
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, connectionVoltageLevel.hasImpactOnNetwork(network));

        // Wrong ThreeSide on Connectable with 2 sides
        PlannedDisconnection disconnectionConnectable = new PlannedDisconnectionBuilder()
            .withIdentifiableId("L1")
            .withSide(ThreeSides.THREE)
            .build();
        ConnectableConnection connectionConnectable = new ConnectableConnectionBuilder()
            .withIdentifiableId("L1")
            .withSide(ThreeSides.THREE)
            .build();
        assertImpactOfConnectDisconnect(network,
            connectionConnectable, NetworkModificationImpact.CANNOT_BE_APPLIED,
            disconnectionConnectable, NetworkModificationImpact.CANNOT_BE_APPLIED);

        // Add a load on the network
        LoadAdder loadAdder = network.getVoltageLevel("VL1").newLoad()
            .setId("LD1")
            .setLoadType(LoadType.UNDEFINED)
            .setP0(80)
            .setQ0(10);
        NetworkModification modification = new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("BBS11")
            .withInjectionPositionOrder(15)
            .withInjectionFeederName("newLoadFeeder")
            .withInjectionDirection(BOTTOM)
            .build();
        modification.apply(network);

        // Wrong ThreeSide on Connectable with 1 side
        disconnectionConnectable = new PlannedDisconnectionBuilder()
            .withIdentifiableId("LD1")
            .withSide(ThreeSides.TWO)
            .build();
        connectionConnectable = new ConnectableConnectionBuilder()
            .withIdentifiableId("LD1")
            .withSide(ThreeSides.TWO)
            .build();
        assertImpactOfConnectDisconnect(network,
            connectionConnectable, NetworkModificationImpact.CANNOT_BE_APPLIED,
            disconnectionConnectable, NetworkModificationImpact.CANNOT_BE_APPLIED);

        // Add tie line
        addTieLine(network);

        // Wrong ThreeSide on Tie Line
        disconnectionConnectable = new PlannedDisconnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withSide(ThreeSides.THREE)
            .build();
        connectionConnectable = new ConnectableConnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withSide(ThreeSides.THREE)
            .build();
        assertImpactOfConnectDisconnect(network,
            connectionConnectable, NetworkModificationImpact.CANNOT_BE_APPLIED,
            disconnectionConnectable, NetworkModificationImpact.CANNOT_BE_APPLIED);

        // Wrong ThreeSide on Hvdc Line
        Network networkHvdc = HvdcTestNetwork.createLcc();
        disconnectionConnectable = new PlannedDisconnectionBuilder()
            .withIdentifiableId("L")
            .withSide(ThreeSides.THREE)
            .build();
        connectionConnectable = new ConnectableConnectionBuilder()
            .withIdentifiableId("L")
            .withSide(ThreeSides.THREE)
            .build();
        assertImpactOfConnectDisconnect(networkHvdc,
            connectionConnectable, NetworkModificationImpact.CANNOT_BE_APPLIED,
            disconnectionConnectable, NetworkModificationImpact.CANNOT_BE_APPLIED);
    }

    @Test
    void testHasImpactConnectable() {
        Network network = createNetwork();
        Line line = network.getLine("L1");

        // Connection and disconnection on both sides
        PlannedDisconnection disconnectionConnectable = new PlannedDisconnectionBuilder()
            .withIdentifiableId("L1")
            .build();
        ConnectableConnection connectionConnectable = new ConnectableConnectionBuilder()
            .withIdentifiableId("L1")
            .build();
        // Both sides are connected
        assertImpactOfConnectDisconnect(network,
            connectionConnectable, NetworkModificationImpact.NO_IMPACT_ON_NETWORK,
            disconnectionConnectable, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);
        // Only one side connected
        line.getTerminal1().disconnect();
        assertImpactOfConnectDisconnect(network,
            connectionConnectable, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionConnectable, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);
        // Both sides disconnected
        line.getTerminal2().disconnect();
        assertImpactOfConnectDisconnect(network,
            connectionConnectable, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionConnectable, NetworkModificationImpact.NO_IMPACT_ON_NETWORK);

        // Connection and disconnection on side 1
        PlannedDisconnection disconnectionConnectableSide1 = new PlannedDisconnectionBuilder()
            .withIdentifiableId("L1")
            .withSide(ThreeSides.ONE)
            .build();
        ConnectableConnection connectionConnectableSide1 = new ConnectableConnectionBuilder()
            .withIdentifiableId("L1")
            .withSide(ThreeSides.ONE)
            .build();
        // Side 1 is disconnected
        assertImpactOfConnectDisconnect(network,
            connectionConnectableSide1, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionConnectableSide1, NetworkModificationImpact.NO_IMPACT_ON_NETWORK);

        // Connection and disconnection on side 2
        PlannedDisconnection disconnectionConnectableSide2 = new PlannedDisconnectionBuilder()
            .withIdentifiableId("L1")
            .withSide(ThreeSides.TWO)
            .build();
        ConnectableConnection connectionConnectableSide2 = new ConnectableConnectionBuilder()
            .withIdentifiableId("L1")
            .withSide(ThreeSides.TWO)
            .build();
        // Side 2 is disconnected
        assertImpactOfConnectDisconnect(network,
            connectionConnectableSide2, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionConnectableSide2, NetworkModificationImpact.NO_IMPACT_ON_NETWORK);

        // Add a load on the network
        LoadAdder loadAdder = network.getVoltageLevel("VL1").newLoad()
            .setId("LD1")
            .setLoadType(LoadType.UNDEFINED)
            .setP0(80)
            .setQ0(10);
        NetworkModification modification = new CreateFeederBayBuilder()
            .withInjectionAdder(loadAdder)
            .withBusOrBusbarSectionId("BBS11")
            .withInjectionPositionOrder(15)
            .withInjectionFeederName("newLoadFeeder")
            .withInjectionDirection(BOTTOM)
            .build();
        modification.apply(network);

        // Connection/Disconnection on Connectable with 1 side
        disconnectionConnectable = new PlannedDisconnectionBuilder()
            .withIdentifiableId("LD1")
            .withSide(ThreeSides.ONE)
            .build();
        connectionConnectable = new ConnectableConnectionBuilder()
            .withIdentifiableId("LD1")
            .withSide(ThreeSides.ONE)
            .build();
        assertImpactOfConnectDisconnect(network,
            connectionConnectable, NetworkModificationImpact.NO_IMPACT_ON_NETWORK,
            disconnectionConnectable, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);

        // Connection/Disconnection on Connectable with 3 sides
        disconnectionConnectable = new PlannedDisconnectionBuilder()
            .withIdentifiableId("twt")
            .withSide(ThreeSides.THREE)
            .build();
        connectionConnectable = new ConnectableConnectionBuilder()
            .withIdentifiableId("twt")
            .withSide(ThreeSides.THREE)
            .build();
        assertImpactOfConnectDisconnect(network,
            connectionConnectable, NetworkModificationImpact.NO_IMPACT_ON_NETWORK,
            disconnectionConnectable, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);
    }

    @Test
    void testHasImpactTieLine() {
        Network network = createNetwork();
        addTieLine(network);
        TieLine tieLine = network.getTieLine("NHV1_NHV2_1");

        // Connection/Disconnection on both sides
        ConnectableConnection connectionTieLine = new ConnectableConnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .build();
        UnplannedDisconnection disconnectionTieLine = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(true)
            .build();
        // Both sides are connected
        assertImpactOfConnectDisconnect(network,
            connectionTieLine, NetworkModificationImpact.NO_IMPACT_ON_NETWORK,
            disconnectionTieLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);
        // One side is disconnected
        tieLine.getTerminal2().disconnect();
        assertImpactOfConnectDisconnect(network,
            connectionTieLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionTieLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);
        // Both sides are disconnected
        tieLine.getTerminal1().disconnect();
        assertImpactOfConnectDisconnect(network,
            connectionTieLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionTieLine, NetworkModificationImpact.NO_IMPACT_ON_NETWORK);
        // Only the other side is disconnected
        tieLine.getTerminal2().connect();
        assertImpactOfConnectDisconnect(network,
            connectionTieLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionTieLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);

        // Connection/Disconnection on side 1
        connectionTieLine = new ConnectableConnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .withSide(ThreeSides.ONE)
            .build();
        disconnectionTieLine = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(true)
            .withSide(ThreeSides.ONE)
            .build();
        // Both sides are connected
        assertImpactOfConnectDisconnect(network,
            connectionTieLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionTieLine, NetworkModificationImpact.NO_IMPACT_ON_NETWORK);

        // Connection/Disconnection on side 2
        connectionTieLine = new ConnectableConnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(true)
            .withOnlyBreakersOperable(false)
            .withSide(ThreeSides.TWO)
            .build();
        disconnectionTieLine = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("NHV1_NHV2_1")
            .withFictitiousSwitchesOperable(true)
            .withSide(ThreeSides.TWO)
            .build();
        // Both sides are connected
        assertImpactOfConnectDisconnect(network,
            connectionTieLine, NetworkModificationImpact.NO_IMPACT_ON_NETWORK,
            disconnectionTieLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);
    }

    @Test
    void testHasImpactHvdcLine() {
        Network network = HvdcTestNetwork.createLcc();

        // Connection/Disconnection on both sides
        UnplannedDisconnection disconnectionHvdcLine = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .build();
        ConnectableConnection connectionHvdcLine = new ConnectableConnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .build();
        // Both sides are connected
        assertImpactOfConnectDisconnect(network,
            connectionHvdcLine, NetworkModificationImpact.NO_IMPACT_ON_NETWORK,
            disconnectionHvdcLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);
        // One side is disconnected
        network.getHvdcLine("L").getConverterStation2().getTerminal().disconnect();
        assertImpactOfConnectDisconnect(network,
            connectionHvdcLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionHvdcLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);
        // Both sides are disconnected
        network.getHvdcLine("L").getConverterStation1().getTerminal().disconnect();
        assertImpactOfConnectDisconnect(network,
            connectionHvdcLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionHvdcLine, NetworkModificationImpact.NO_IMPACT_ON_NETWORK);
        // Only the other side is disconnected
        network.getHvdcLine("L").getConverterStation2().getTerminal().connect();
        assertImpactOfConnectDisconnect(network,
            connectionHvdcLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionHvdcLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);

        // Connection/Disconnection on side 1
        disconnectionHvdcLine = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .withSide(ThreeSides.ONE)
            .build();
        connectionHvdcLine = new ConnectableConnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .withSide(ThreeSides.ONE)
            .build();
        // Both sides are connected
        assertImpactOfConnectDisconnect(network,
            connectionHvdcLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
            disconnectionHvdcLine, NetworkModificationImpact.NO_IMPACT_ON_NETWORK);

        // Connection/Disconnection on side 2
        disconnectionHvdcLine = new UnplannedDisconnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .withSide(ThreeSides.TWO)
            .build();
        connectionHvdcLine = new ConnectableConnectionBuilder()
            .withIdentifiableId("L")
            .withFictitiousSwitchesOperable(false)
            .withOnlyBreakersOperable(true)
            .withSide(ThreeSides.TWO)
            .build();
        // Both sides are connected
        assertImpactOfConnectDisconnect(network,
            connectionHvdcLine, NetworkModificationImpact.NO_IMPACT_ON_NETWORK,
            disconnectionHvdcLine, NetworkModificationImpact.HAS_IMPACT_ON_NETWORK);
    }

    private void assertImpactOfConnectDisconnect(Network network,
                                                 ConnectableConnection connection, NetworkModificationImpact connectionImpact,
                                                 AbstractDisconnection disconnection, NetworkModificationImpact disconnectionImpact) {
        assertEquals(connectionImpact, connection.hasImpactOnNetwork(network));
        assertEquals(disconnectionImpact, disconnection.hasImpactOnNetwork(network));
    }

    private void addTieLine(Network network) {
        // Add tie line
        DanglingLine nhv1xnode1 = network.getVoltageLevel("VL2").newDanglingLine()
            .setId("NHV1_XNODE1")
            .setP0(0.0)
            .setQ0(0.0)
            .setR(1.5)
            .setX(20.0)
            .setG(1E-6)
            .setB(386E-6 / 2)
            .setBus("bus2A")
            .setPairingKey("XNODE1")
            .add();
        DanglingLine xnode1nhv2 = network.getVoltageLevel("VL3").newDanglingLine()
            .setId("XNODE1_NHV2")
            .setP0(0.0)
            .setQ0(0.0)
            .setR(1.5)
            .setX(13.0)
            .setG(2E-6)
            .setB(386E-6 / 2)
            .setBus("bus3A")
            .setPairingKey("XNODE1")
            .add();
        network.newTieLine()
            .setId("NHV1_NHV2_1")
            .setDanglingLine1(nhv1xnode1.getId())
            .setDanglingLine2(xnode1nhv2.getId())
            .add();
    }
}
