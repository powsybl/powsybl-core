/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractForkConnectDisconnectTest {

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
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        // Busbar sections
        BusbarSection bbs1 = vl1.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS11")
            .setNode(0)
            .add();
        bbs1.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs2A = vl2.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS2A")
            .setNode(0)
            .add();
        bbs2A.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs2B = vl2.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS2B")
            .setNode(1)
            .add();
        bbs2B.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(1)
            .add();

        // Lines
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
            .setNode2(3)
            .add();

        // Add a second line
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
            .setVoltageLevel2("VL2")
            .setNode1(5)
            .setNode2(5)
            .add();

        // VL1 - Breakers and disconnectors for L1 and L2
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_L1")
            .setNode1(4)
            .setNode2(6)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D_L2")
            .setNode1(5)
            .setNode2(6)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("B_L1_L2")
            .setNode1(6)
            .setNode2(7)
            .setOpen(false)
            .setFictitious(false)
            .add();
        vl1.getNodeBreakerView().newDisconnector()
            .setId("D0")
            .setNode1(7)
            .setNode2(0)
            .setOpen(false)
            .add();

        // VL2 - Breakers and disconnectors for L1 and L2
        vl2.getNodeBreakerView().newBreaker()
            .setId("B_L1_VL2")
            .setNode1(3)
            .setNode2(2)
            .setOpen(false)
            .setFictitious(false)
            .add();
        vl2.getNodeBreakerView().newDisconnector()
            .setId("D_L1_BBS2A")
            .setNode1(2)
            .setNode2(0)
            .setOpen(false)
            .add();
        vl2.getNodeBreakerView().newDisconnector()
            .setId("D_L1_BBS2B")
            .setNode1(2)
            .setNode2(1)
            .setOpen(true)
            .add();
        vl2.getNodeBreakerView().newBreaker()
            .setId("B_L2_VL2")
            .setNode1(5)
            .setNode2(4)
            .setOpen(false)
            .setFictitious(false)
            .add();
        vl2.getNodeBreakerView().newDisconnector()
            .setId("D_L2_BBS2A")
            .setNode1(4)
            .setNode2(0)
            .setOpen(true)
            .add();
        vl2.getNodeBreakerView().newDisconnector()
            .setId("D_L2_BBS2B")
            .setNode1(4)
            .setNode2(1)
            .setOpen(false)
            .add();
        return network;
    }

    @Test
    public void fullyClosedTest() {
        Network network = createNetwork();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line1 = network.getLine("L1");
        Line line2 = network.getLine("L2");
        Switch disconnector = network.getSwitch("D_L1");

        // L1 and L2 are fully connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), true);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(line2.getTerminals(), true);

        // Disconnect L1
        line1.disconnect(SwitchPredicates.IS_OPEN.negate());

        // check that L1 is disconnected while L2 is still connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), false);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(line2.getTerminals(), true);

        // D_L1 should be open
        assertTrue(disconnector.isOpen());
    }

    @Test
    public void forkDisconnectedTestWithLines() {
        Network network = createNetwork();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line1 = network.getLine("L1");
        Line line2 = network.getLine("L2");
        Switch disconnectorL1 = network.getSwitch("D_L1");
        Switch disconnectorL2 = network.getSwitch("D_L2");
        Switch breaker = network.getSwitch("B_L1_L2");

        // In this case, B_L1_L2 is open
        breaker.setOpen(true);

        // L1 and L2 are fully connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), true);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(line2.getTerminals(), true);

        // Disconnect L1
        line1.disconnect(SwitchPredicates.IS_OPEN.negate());

        // check that L1 and L2 are disconnected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), false);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(List.of(line2.getTerminal1()), false);
        assertTerminalsStatus(List.of(line2.getTerminal2()), true);

        // D_L1 should be open but not D_L2
        assertTrue(disconnectorL1.isOpen());
        assertFalse(disconnectorL2.isOpen());
    }

    private void assertTerminalsStatus(List<? extends Terminal> terminals, boolean terminalsShouldBeConnected) {
        if (terminalsShouldBeConnected) {
            terminals.forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
            terminals.forEach(terminal -> assertTrue(terminal.isConnected()));
        } else {
            terminals.forEach(terminal -> assertNull(terminal.getBusView().getBus()));
            terminals.forEach(terminal -> assertFalse(terminal.isConnected()));
        }
    }
}
