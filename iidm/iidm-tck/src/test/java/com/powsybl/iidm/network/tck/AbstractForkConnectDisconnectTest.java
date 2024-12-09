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

    private Network createNetworkWithLine() {
        Network network = createNetwork();

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

        return network;
    }

    private Network createNetworkWithTieLine() {
        Network network = createNetwork();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        double r = 10.0;
        double r2 = 1.0;
        double x = 20.0;
        double x2 = 2.0;
        double hl1g1 = 0.03;
        double hl1g2 = 0.035;
        double hl1b1 = 0.04;
        double hl1b2 = 0.045;
        double hl2g1 = 0.013;
        double hl2g2 = 0.0135;
        double hl2b1 = 0.014;
        double hl2b2 = 0.0145;

        // Add the dangling lines
        DanglingLine dl1 = vl1.newDanglingLine()
            .setNode(5)
            .setId("hl1")
            .setEnsureIdUnicity(true)
            .setName("DANGLING1_NAME")
            .setP0(0.0)
            .setQ0(0.0)
            .setR(r)
            .setX(x)
            .setB(hl1b1 + hl1b2)
            .setG(hl1g1 + hl1g2)
            .setPairingKey("ucte")
            .add();
        DanglingLine dl2 = vl2.newDanglingLine()
            .setNode(5)
            .setId("hl2")
            .setEnsureIdUnicity(true)
            .setP0(0.0)
            .setQ0(0.0)
            .setR(r2)
            .setX(x2)
            .setB(hl2b1 + hl2b2)
            .setG(hl2g1 + hl2g2)
            .add();

        // Add the tie line
        network.newTieLine()
            .setId("TL")
            .setName("TL")
            .setDanglingLine1(dl1.getId())
            .setDanglingLine2(dl2.getId())
            .add();

        return network;
    }

    private Network createNetworkWithHvdcLines() {
        Network network = createNetwork();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VoltageLevel vl2 = network.getVoltageLevel("VL2");

        vl1.newLccConverterStation()
            .setId("C1")
            .setName("Converter1")
            .setNode(5)
            .setLossFactor(1.1f)
            .setPowerFactor(0.5f)
            .add();
        vl2.newLccConverterStation()
            .setId("C2")
            .setName("Converter2")
            .setNode(5)
            .setLossFactor(1.1f)
            .setPowerFactor(0.6f)
            .add();
        network.newHvdcLine()
            .setId("L")
            .setName("HVDC")
            .setConverterStationId1("C1")
            .setConverterStationId2("C2")
            .setR(1)
            .setNominalV(400)
            .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
            .setMaxP(300.0)
            .setActivePowerSetpoint(280)
            .add();

        return network;
    }

    private Network createNetworkWithTwt() {
        Network network = createNetwork();

        network.getSubstation("S1").newTwoWindingsTransformer()
            .setId("twt")
            .setName("TWT")
            .setR(1.0)
            .setX(2.0)
            .setG(3.0)
            .setB(4.0)
            .setRatedU1(5.0)
            .setRatedU2(6.0)
            .setRatedS(7.0)
            .setVoltageLevel1("VL1")
            .setVoltageLevel2("VL2")
            .setNode1(5)
            .setNode2(5)
            .add();

        return network;
    }

    private Network createNetworkWithT3T() {
        Network network = createNetwork();
        Substation s1 = network.getSubstation("S1");
        VoltageLevel vl3 = s1.newVoltageLevel()
            .setId("VL3")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl3.getBusBreakerView()
            .newBus()
            .setId("bus3")
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
            .setNode(5)
            .add()
            .newLeg2()
            .setR(2.03)
            .setX(2.04)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(2.05)
            .setRatedS(2.06)
            .setVoltageLevel("VL2")
            .setNode(5)
            .add()
            .newLeg3()
            .setR(3.3)
            .setX(3.4)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(3.5)
            .setRatedS(3.6)
            .setVoltageLevel("VL3")
            .setBus("bus3")
            .setConnectableBus("bus3")
            .add()
            .add();

        return network;
    }

    @Test
    public void fullyClosedTest() {
        Network network = createNetworkWithLine();

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
        Network network = createNetworkWithLine();

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
        assertTerminalsStatus(line2.getTerminals(), false);

        // D_L1 should be open but not D_L2
        assertTrue(disconnectorL1.isOpen());
        assertFalse(disconnectorL2.isOpen());
    }

    @Test
    public void forkDisconnectedTestWithTieLines() {
        Network network = createNetworkWithTieLine();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line1 = network.getLine("L1");
        TieLine tieLine = network.getTieLine("TL");
        Switch disconnectorL1 = network.getSwitch("D_L1");
        Switch disconnectorL2 = network.getSwitch("D_L2");
        Switch breaker = network.getSwitch("B_L1_L2");

        // In this case, B_L1_L2 is open
        breaker.setOpen(true);

        // L1 and TL are fully connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), true);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(tieLine.getDanglingLine1().getTerminals(), true);
        assertTerminalsStatus(tieLine.getDanglingLine2().getTerminals(), true);

        // Disconnect L1
        line1.disconnect(SwitchPredicates.IS_OPEN.negate());

        // check that L1 and TL are disconnected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), false);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(tieLine.getDanglingLine1().getTerminals(), false);
        assertTerminalsStatus(tieLine.getDanglingLine2().getTerminals(), false);

        // D_L1 should be open but not D_L2
        assertTrue(disconnectorL1.isOpen());
        assertFalse(disconnectorL2.isOpen());
    }

    @Test
    public void forkDisconnectedTestWithHvdcLine() {
        Network network = createNetworkWithHvdcLines();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line1 = network.getLine("L1");
        HvdcLine hvdcLine = network.getHvdcLine("L");
        Switch disconnectorL1 = network.getSwitch("D_L1");
        Switch disconnectorL2 = network.getSwitch("D_L2");
        Switch breaker = network.getSwitch("B_L1_L2");

        // In this case, B_L1_L2 is open
        breaker.setOpen(true);

        // L1 and L are fully connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), true);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(hvdcLine.getConverterStation1().getTerminals(), true);
        assertTerminalsStatus(hvdcLine.getConverterStation2().getTerminals(), true);

        // Disconnect L1
        line1.disconnect(SwitchPredicates.IS_OPEN.negate());

        // check that L1 and L are disconnected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), false);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(hvdcLine.getConverterStation1().getTerminals(), false);
        assertTerminalsStatus(hvdcLine.getConverterStation2().getTerminals(), false);

        // D_L1 should be open but not D_L2
        assertTrue(disconnectorL1.isOpen());
        assertFalse(disconnectorL2.isOpen());
    }

    @Test
    public void forkDisconnectedTestWithTwt() {
        Network network = createNetworkWithTwt();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line1 = network.getLine("L1");
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("twt");
        Switch disconnectorL1 = network.getSwitch("D_L1");
        Switch disconnectorL2 = network.getSwitch("D_L2");
        Switch breaker = network.getSwitch("B_L1_L2");

        // In this case, B_L1_L2 is open
        breaker.setOpen(true);

        // L1 and twt are fully connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), true);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(twt.getTerminals(), true);

        // Disconnect L1
        line1.disconnect(SwitchPredicates.IS_OPEN.negate());

        // check that L1 and twt are disconnected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), false);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(twt.getTerminals(), false);

        // D_L1 should be open but not D_L2
        assertTrue(disconnectorL1.isOpen());
        assertFalse(disconnectorL2.isOpen());
    }

    @Test
    public void forkDisconnectedTestWithT3T() {
        Network network = createNetworkWithT3T();

        // Useful elements
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Line line1 = network.getLine("L1");
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("twt");
        Switch disconnectorL1 = network.getSwitch("D_L1");
        Switch disconnectorL2 = network.getSwitch("D_L2");
        Switch breaker = network.getSwitch("B_L1_L2");

        // In this case, B_L1_L2 is open
        breaker.setOpen(true);

        // L1 and twt are fully connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), true);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(twt.getTerminals(), true);

        // Disconnect L1
        line1.disconnect(SwitchPredicates.IS_OPEN.negate());

        // check that L1 and twt are disconnected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTerminalsStatus(line1.getTerminals(), false);
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTerminalsStatus(twt.getTerminals(), false);

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
