/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractConnectionDisconnectionPropagationTest {

    @Test
    void disconnectionOnTeePointTestWithNoPropagation() {
        // Network
        Network network = createNetworkWithTeePoint();

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        Line line3 = network.getLine("testLine");
        List<Line> lines = List.of(line1, line2, line3);

        // Check that the 3 lines are connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));

        // Try a disconnection without propagation - it should fail because there are no switches in the opposite voltage
        // level, only internal connections
        assertFalse(line1.disconnect(SwitchPredicates.IS_NONFICTIONAL, false));

        // Check that the 3 lines are still connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));
    }

    @Test
    void disconnectionOnTeePointTestWithPropagationButSomeFictitiousSwitches() {
        // Network
        Network network = createNetworkWithTeePoint();

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        Line line3 = network.getLine("testLine");
        List<Line> lines = List.of(line1, line2, line3);

        // Check that the 3 lines are connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));

        // Set the switches in VL2 as fictitious
        network.getSwitch("D_L1_BBS2").setFictitious(true);
        network.getSwitch("B_L1_VL2").setFictitious(true);

        // Try a disconnection with propagation - it should fail because of the fictitious switches on the opposite
        // voltage level
        assertFalse(line1.disconnect(SwitchPredicates.IS_NONFICTIONAL, true));

        // Check that the 3 lines are still connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));
    }

    @Test
    void disconnectionOnTeePointTestWithPropagation() {
        // Network
        Network network = createNetworkWithTeePoint();

        // Switch that will be operated
        List<String> switchList = List.of("B_L1_VL1", "B_L1_VL2", "L1_BREAKER");

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        Line line3 = network.getLine("testLine");
        List<Line> lines = List.of(line1, line2, line3);

        // Check that the 3 lines are connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));

        // Check the switches that will be opened are close for now
        switchList.forEach(s -> assertFalse(network.getSwitch(s).isOpen()));

        // Disconnect the line works with the propagation
        assertTrue(line1.disconnect(SwitchPredicates.IS_NONFICTIONAL, true));

        // Check that the 3 lines are disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);
        assertLineConnection(line3, true, false);

        // Check the switches that were opened
        switchList.forEach(s -> assertTrue(network.getSwitch(s).isOpen()));
    }

    @Test
    void connectionOnTeePointTestWithoutPropagation() {
        // Network
        Network network = createNetworkWithTeePoint();

        // Switch that will be operated
        List<String> switchList = List.of("B_L1_VL1", "B_L1_VL2", "L1_BREAKER");

        // Open the switches to simulate the disconnection
        switchList.forEach(sw -> network.getSwitch(sw).setOpen(true));

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        Line line3 = network.getLine("testLine");

        // Check that the 3 lines are disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);
        assertLineConnection(line3, true, false);

        // Try to connect the line without propagation - it should work, but only the first line is now connected
        assertTrue(line1.connect(SwitchPredicates.IS_NONFICTIONAL, false));

        // Check that only line1 is now fully connected
        assertLineConnection(line1, true, true);
        assertLineConnection(line2, true, false);
        assertLineConnection(line3, true, false);
    }

    @Test
    void connectionOnTeePointTestWithPropagationButFictitiousSwitches() {
        // Network
        Network network = createNetworkWithTeePoint();

        // Switch that will be operated
        List<String> switchList = List.of("B_L1_VL1", "B_L1_VL2", "L1_BREAKER");

        // Open the switches to simulate the disconnection
        switchList.forEach(sw -> network.getSwitch(sw).setOpen(true));

        // Set the switches in VL2 as fictitious
        network.getSwitch("D_L1_BBS2").setFictitious(true);
        network.getSwitch("B_L1_VL2").setFictitious(true);

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        Line line3 = network.getLine("testLine");

        // Check that the 3 lines are disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);
        assertLineConnection(line3, true, false);

        // Connect the line with propagation - it should fail because of the fictitious switches
        assertFalse(line1.connect(SwitchPredicates.IS_NONFICTIONAL, true));

        // Check that the 3 lines are still disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);
        assertLineConnection(line3, true, false);
    }

    @Test
    void connectionOnTeePointTestWithPropagation() {
        // Network
        Network network = createNetworkWithTeePoint();

        // Switch that will be operated
        List<String> switchList = List.of("B_L1_VL1", "B_L1_VL2", "L1_BREAKER");

        // Open the switches to simulate the disconnection
        switchList.forEach(sw -> network.getSwitch(sw).setOpen(true));

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        Line line3 = network.getLine("testLine");
        List<Line> lines = List.of(line1, line2, line3);

        // Check that the 3 lines are disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);
        assertLineConnection(line3, true, false);

        // Connect the line with propagation
        assertTrue(line1.connect(SwitchPredicates.IS_NONFICTIONAL, true));

        // Check that the 3 lines are connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));

        // Check the switches that were closed
        switchList.forEach(s -> assertFalse(network.getSwitch(s).isOpen()));
    }

    @Test
    void disconnectionThroughVoltageLevelTestWithNoPropagation() {
        // Network
        Network network = createNetworkWithFictitiousVoltageLevel();

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        List<Line> lines = List.of(line1, line2);

        // Check that the 2 lines are connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));

        // Try a disconnection without propagation - it should fail
        assertFalse(line1.disconnect(SwitchPredicates.IS_NONFICTIONAL, false));

        // Check that the 3 lines are still connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));
    }

    @Test
    void disconnectionThroughVoltageLevelTestWithPropagationButSomeFictitiousSwitches() {
        // Network
        Network network = createNetworkWithFictitiousVoltageLevel();

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        List<Line> lines = List.of(line1, line2);

        // Check that the 2 lines are connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));

        // Set the switches in VL2 as fictitious
        network.getSwitch("D_L1_BBS2").setFictitious(true);
        network.getSwitch("B_L1_VL2").setFictitious(true);

        // Try a disconnection with propagation - it should fail because of the fictitious switches on the opposite
        // voltage level
        assertFalse(line1.disconnect(SwitchPredicates.IS_NONFICTIONAL, true));

        // Check that the 2 lines are still connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));
    }

    @Test
    void disconnectionThroughVoltageLevelTestWithPropagation() {
        // Network
        Network network = createNetworkWithFictitiousVoltageLevel();

        // Switch that will be operated
        List<String> switchList = List.of("B_L1_VL1", "B_L1_VL2");

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        List<Line> lines = List.of(line1, line2);

        // Check that the 2 lines are connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));

        // Check the switches that will be opened are close for now
        switchList.forEach(s -> assertFalse(network.getSwitch(s).isOpen()));

        // Disconnect the line works with the propagation
        assertTrue(line1.disconnect(SwitchPredicates.IS_NONFICTIONAL, true));

        // Check that the 3 lines are disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);

        // Check the switches that were opened
        switchList.forEach(s -> assertTrue(network.getSwitch(s).isOpen()));
    }

    @Test
    void connectionThroughVoltageLevelTestWithoutPropagation() {
        // Network
        Network network = createNetworkWithFictitiousVoltageLevel();

        // Switch that will be operated
        List<String> switchList = List.of("B_L1_VL1", "B_L1_VL2");

        // Open the switches to simulate the disconnection
        switchList.forEach(sw -> network.getSwitch(sw).setOpen(true));

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");

        // Check that the 2 lines are disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);

        // Try to connect the line without propagation - it should fail due to the fictitious switch
        assertFalse(line1.connect(SwitchPredicates.IS_NONFICTIONAL, false));

        // Check that the 2 lines are disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);
    }

    @Test
    void connectionThroughVoltageLevelTestWithPropagationButFictitiousSwitches() {
        // Network
        Network network = createNetworkWithFictitiousVoltageLevel();

        // Switch that will be operated
        List<String> switchList = List.of("B_L1_VL1", "B_L1_VL2");

        // Open the switches to simulate the disconnection
        switchList.forEach(sw -> network.getSwitch(sw).setOpen(true));

        // Set the switches in VL2 as fictitious
        network.getSwitch("D_L1_BBS2").setFictitious(true);
        network.getSwitch("B_L1_VL2").setFictitious(true);

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");

        // Check that the 2 lines are disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);

        // Connect the line with propagation - it should fail because of the fictitious switches
        assertFalse(line1.connect(SwitchPredicates.IS_NONFICTIONAL, true));

        // Check that the 2 lines are still disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);
    }

    @Test
    void connectionThroughVoltageLevelTestWithPropagation() {
        // Network
        Network network = createNetworkWithFictitiousVoltageLevel();

        // Switch that will be operated
        List<String> switchList = List.of("B_L1_VL1", "B_L1_VL2");

        // Open the switches to simulate the disconnection
        switchList.forEach(sw -> network.getSwitch(sw).setOpen(true));

        // Lines
        Line line1 = network.getLine("L1_1");
        Line line2 = network.getLine("L1_2");
        List<Line> lines = List.of(line1, line2);

        // Check that the 2 lines are disconnected on one side only
        assertLineConnection(line1, false, true);
        assertLineConnection(line2, true, false);

        // Connect the line with propagation
        assertTrue(line1.connect(SwitchPredicates.IS_NONFICTIONAL, true));

        // Check that the 2 lines are connected on both sides
        lines.forEach(line -> line.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected())));

        // Check the switches that were closed
        switchList.forEach(s -> assertFalse(network.getSwitch(s).isOpen()));
    }

    private Network createBaseNetwork() {
        Network network = Network.create("test", "test");
        // Substations
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
            .add();
        Substation s2 = network.newSubstation()
            .setId("S2")
            .setCountry(Country.FR)
            .add();
        Substation s3 = network.newSubstation()
            .setId("S3")
            .setCountry(Country.FR)
            .add();

        // Voltage levels
        VoltageLevel vl1 = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
            .setId("VL2")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        VoltageLevel vl3 = s3.newVoltageLevel()
            .setId("VL3")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        // Busbar sections
        BusbarSection bbs1 = vl1.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS1")
            .setNode(0)
            .add();
        bbs1.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs2 = vl2.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS2")
            .setNode(0)
            .add();
        bbs2.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs3 = vl3.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS3")
            .setNode(0)
            .add();
        bbs3.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();

        // VL1 - Breakers and disconnectors
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_L1_BBS1")
            .setNode1(0)
            .setNode2(1)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B_L1_VL1")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .setFictitious(false)
            .add();

        // VL2 - Breakers and disconnectors
        vl2.getNodeBreakerView().newDisconnector()
            .setId("D_L1_BBS2")
            .setNode1(0)
            .setNode2(1)
            .setOpen(false)
            .add();
        vl2.getNodeBreakerView().newBreaker()
            .setId("B_L1_VL2")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .add();

        return network;
    }

    private Network createNetworkWithTeePoint() {
        // Base network
        Network network = createBaseNetwork();

        // Fictitious voltage level
        VoltageLevel fictitiousVl = network.newVoltageLevel()
            .setId("L1_VL")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .setFictitious(true)
            .add();

        // Fictitious topology
        fictitiousVl.getNodeBreakerView()
            .newInternalConnection()
            .setNode1(0)
            .setNode2(1)
            .add();
        fictitiousVl.getNodeBreakerView()
            .newInternalConnection()
            .setNode1(1)
            .setNode2(2)
            .add();
        fictitiousVl.getNodeBreakerView()
            .newInternalConnection()
            .setNode1(1)
            .setNode2(3)
            .add();

        // VL3 - Breakers and disconnectors
        VoltageLevel vl3 = network.getVoltageLevel("VL3");
        vl3.getNodeBreakerView().newBreaker()
            .setId("L1_BREAKER")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .setRetained(true)
            .setFictitious(false)
            .add();
        vl3.getNodeBreakerView().newDisconnector()
            .setId("L1_DISCONNECTOR_2_0")
            .setNode1(2)
            .setNode2(0)
            .setOpen(false)
            .add();

        // Lines
        network.newLine()
            .setId("L1_1")
            .setR(0.5)
            .setX(1.0)
            .setG1(1.5)
            .setG2(1.75)
            .setB1(2.0)
            .setB2(2.25)
            .setVoltageLevel1("VL1")
            .setVoltageLevel2("L1_VL")
            .setNode1(2)
            .setNode2(0)
            .add();
        network.newLine()
            .setId("L1_2")
            .setR(0.5)
            .setX(1.0)
            .setG1(1.5)
            .setG2(1.75)
            .setB1(2.0)
            .setB2(2.25)
            .setVoltageLevel1("L1_VL")
            .setVoltageLevel2("VL2")
            .setNode1(2)
            .setNode2(2)
            .add();
        network.newLine()
            .setId("testLine")
            .setR(1.0)
            .setX(2.0)
            .setG1(3.0)
            .setG2(3.5)
            .setB1(4.0)
            .setB2(4.5)
            .setVoltageLevel1("L1_VL")
            .setVoltageLevel2("VL3")
            .setNode1(3)
            .setNode2(1)
            .add();

        return network;
    }

    private Network createNetworkWithFictitiousVoltageLevel() {
        Network network = createBaseNetwork();

        // VL3 - Breakers and disconnectors
        VoltageLevel vl3 = network.getVoltageLevel("VL3");
        vl3.getNodeBreakerView().newBreaker()
            .setId("L1_1_BREAKER")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .setRetained(true)
            .setFictitious(true)
            .add();
        vl3.getNodeBreakerView().newDisconnector()
            .setId("L1_1_DISCONNECTOR_2_0")
            .setNode1(2)
            .setNode2(0)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl3.getNodeBreakerView().newBreaker()
            .setId("L1_2_BREAKER")
            .setNode1(4)
            .setNode2(3)
            .setOpen(false)
            .setRetained(true)
            .setFictitious(true)
            .add();
        vl3.getNodeBreakerView().newDisconnector()
            .setId("L1_2_DISCONNECTOR_3_0")
            .setNode1(3)
            .setNode2(0)
            .setOpen(false)
            .setFictitious(true)
            .add();

        // Lines
        network.newLine()
            .setId("L1_1")
            .setR(0.5)
            .setX(1.0)
            .setG1(1.5)
            .setG2(1.75)
            .setB1(2.0)
            .setB2(2.25)
            .setVoltageLevel1("VL1")
            .setVoltageLevel2("VL3")
            .setNode1(2)
            .setNode2(1)
            .add();
        network.newLine()
            .setId("L1_2")
            .setR(0.5)
            .setX(1.0)
            .setG1(1.5)
            .setG2(1.75)
            .setB1(2.0)
            .setB2(2.25)
            .setVoltageLevel1("VL3")
            .setVoltageLevel2("VL2")
            .setNode1(4)
            .setNode2(2)
            .add();

        // Set VL3 fictitious
        vl3.setFictitious(true);

        return network;
    }

    private void assertLineConnection(Line line, boolean connectedOnSide1, boolean connectedOnSide2) {
        assertEquals(connectedOnSide1, line.getTerminal1().isConnected());
        assertEquals(connectedOnSide2, line.getTerminal2().isConnected());
    }

}
