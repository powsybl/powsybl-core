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
        vl2.getBusBreakerView()
            .newBus()
            .setId("bus2A")
            .add();
        vl2.getBusBreakerView()
            .newBus()
            .setId("bus2B")
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
            .setVoltageLevel2("VL2")
            .setNode1(5)
            .setBus2("bus2B")
            .setConnectableBus2("bus2B")
            .add();

        // Breakers and disconnectors for L1 and L2
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
        line1.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        line1.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        line2.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        line2.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));

        // Disconnect L1
        line1.disconnect(SwitchPredicates.IS_OPEN.negate());

        // check that L1 is disconnected while L2 is still connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        line1.getTerminals().forEach(terminal -> assertNull(terminal.getBusView().getBus()));
        line1.getTerminals().forEach(terminal -> assertFalse(terminal.isConnected()));
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        line2.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        line2.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));

        // D_L1 should be open
        assertTrue(disconnector.isOpen());
    }

    @Test
    public void forkDisconnectedTest() {
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
        line1.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        line1.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        line2.getTerminals().forEach(terminal -> assertNotNull(terminal.getBusView().getBus()));
        line2.getTerminals().forEach(terminal -> assertTrue(terminal.isConnected()));

        // Disconnect L1
        line1.disconnect(SwitchPredicates.IS_OPEN.negate());

        // check that L1 and L2 are disconnected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        line1.getTerminals().forEach(terminal -> assertNull(terminal.getBusView().getBus()));
        line1.getTerminals().forEach(terminal -> assertFalse(terminal.isConnected()));
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        line2.getTerminals().forEach(terminal -> assertNull(terminal.getBusView().getBus()));
        line2.getTerminals().forEach(terminal -> assertFalse(terminal.isConnected()));
        // TODO: Warning - L2 is half connected!

        // D_L1 should be open but not D_L2
        assertTrue(disconnectorL1.isOpen());
        assertFalse(disconnectorL2.isOpen());
    }
}
