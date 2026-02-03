/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.util.BusbarSectionFinderTraverser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */

class BusbarSectionFinderTraverserTest {

    private Network network;

    @BeforeEach
    void setUp() {
        createNetwork();
    }

    @Test
    void testWithOpenSwitch() {
        Line line12 = network.getLine("LINE_1_2");
        Line line22 = network.getLine("LINE_2_2");
        BusbarSectionFinderTraverser.BusbarSectionResult result12 = BusbarSectionFinderTraverser.getBusbarSectionResult(line12.getTerminal1());
        BusbarSectionFinderTraverser.BusbarSectionResult result22 = BusbarSectionFinderTraverser.getBusbarSectionResult(line22.getTerminal1());
        assertNotNull(result12);
        assertEquals("BBS1_2", result12.busbarSectionId());
        assertNotNull(result22);
        assertEquals("BBS1_2", result22.busbarSectionId());
        assertEquals(4, result12.depth());
        assertEquals(4, result22.depth());
        assertEquals("DISC_BBS1_2", result12.lastSwitch().id());
        assertEquals("DISC_BBS1_2", result22.lastSwitch().id());
        assertTrue(result12.lastSwitch().isOpen());
        assertTrue(result22.lastSwitch().isOpen());
        assertFalse(result12.allClosedSwitch());
        assertFalse(result22.allClosedSwitch());

    }

    @Test
    void testWithClosedLastSwitch() {
        network.getSwitch("DISC_BBS1_1").setOpen(false);
        network.getSwitch("DISC_BBS2_2").setOpen(false);
        Line line11 = network.getLine("LINE_1_1");
        Line line21 = network.getLine("LINE_2_1");
        Line line12 = network.getLine("LINE_1_2");
        BusbarSectionFinderTraverser.BusbarSectionResult result11 = BusbarSectionFinderTraverser.getBusbarSectionResult(line11.getTerminal1());
        BusbarSectionFinderTraverser.BusbarSectionResult result21 = BusbarSectionFinderTraverser.getBusbarSectionResult(line21.getTerminal1());
        BusbarSectionFinderTraverser.BusbarSectionResult result12 = BusbarSectionFinderTraverser.getBusbarSectionResult(line12.getTerminal1());
        assertNotNull(result11);
        assertEquals("BBS1_1", result11.busbarSectionId());
        assertEquals(3, result11.depth());
        assertEquals("DISC_BBS1_1", result11.lastSwitch().id());
        assertFalse(result11.lastSwitch().isOpen());
        assertFalse(result11.allClosedSwitch());
        assertNotNull(result21);
        assertEquals("BBS1_1", result21.busbarSectionId());
        assertEquals(4, result21.depth());
        assertEquals("DISC_BBS1_1", result21.lastSwitch().id());
        assertFalse(result21.lastSwitch().isOpen());
        assertFalse(result21.allClosedSwitch());
        assertNotNull(result12);
        assertEquals("BBS2_2", result12.busbarSectionId());
        assertEquals(4, result12.depth());
        assertEquals("DISC_BBS2_2", result12.lastSwitch().id());
        assertFalse(result12.lastSwitch().isOpen());
        assertFalse(result12.allClosedSwitch());
    }

    @Test
    void testWithAllClosedSwitch() {
        network.getSwitch("BRK_LINE_1_1").setOpen(false);
        network.getSwitch("DISC_LINE_1_1").setOpen(false);
        network.getSwitch("DISC_BBS1_1").setOpen(false);
        network.getSwitch("DISC_BBS2_1").setOpen(false);
        network.getSwitch("BRK_LINE_2_1").setOpen(false);
        network.getSwitch("DISC_LINE_2_1").setOpen(false);
        Line line11 = network.getLine("LINE_1_1");
        Line line21 = network.getLine("LINE_2_1");
        BusbarSectionFinderTraverser.BusbarSectionResult result11 = BusbarSectionFinderTraverser.getBusbarSectionResult(line11.getTerminal1());
        BusbarSectionFinderTraverser.BusbarSectionResult result21 = BusbarSectionFinderTraverser.getBusbarSectionResult(line21.getTerminal1());
        assertNotNull(result11);
        assertEquals("BBS1_1", result11.busbarSectionId());
        assertEquals(3, result11.depth());
        assertTrue(result11.allClosedSwitch());
        assertEquals("DISC_BBS1_1", result11.lastSwitch().id());
        assertNotNull(result21);
        assertEquals("BBS2_1", result21.busbarSectionId());
        assertEquals(3, result21.depth());
        assertEquals("DISC_BBS2_1", result21.lastSwitch().id());
        assertTrue(result21.allClosedSwitch());
    }

    @Test
    void testWithInternalConnection() {
        TwoWindingsTransformer td1 = network.getTwoWindingsTransformer("TD1");
        BusbarSectionFinderTraverser.BusbarSectionResult result1 = BusbarSectionFinderTraverser.getBusbarSectionResult(td1.getTerminal1());
        BusbarSectionFinderTraverser.BusbarSectionResult result2 = BusbarSectionFinderTraverser.getBusbarSectionResult(td1.getTerminal2());
        assertNotNull(result1);
        assertEquals("BBS1_2", result1.busbarSectionId());
        assertEquals(1, result1.depth());
        assertTrue(result1.allClosedSwitch());
        assertEquals("DISC_BUS1_2_TD1", result1.lastSwitch().id());
        assertNotNull(result2);
        assertEquals("BBS2_2", result2.busbarSectionId());
        assertEquals(1, result2.depth());
        assertEquals("DISC_BUS2_2_TD1", result2.lastSwitch().id());
        assertTrue(result2.allClosedSwitch());
    }

    private void createNetwork() {
        network = NetworkFactory.findDefault().createNetwork("sim1", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setName("BBS1")
                .setNode(0)
                .add();

        VoltageLevel vl2 = s1.newVoltageLevel()
                .setId("VL2")
                .setNominalV(24.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS1_1")
                .setName("BBS1_1")
                .setNode(0)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS1_2")
                .setName("BBS1_2")
                .setNode(1)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS2_1")
                .setName("BBS2_1")
                .setNode(3)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS2_2")
                .setName("BBS2_2")
                .setNode(4)
                .add();

        vl2.getNodeBreakerView()
                .getBusbarSection("BBS1_1")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(1)
                .add();
        vl2.getNodeBreakerView()
                .getBusbarSection("BBS1_2")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(1)
                .withSectionIndex(2)
                .add();
        vl2.getNodeBreakerView()
                .getBusbarSection("BBS2_1")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(2)
                .withSectionIndex(1)
                .add();
        vl2.getNodeBreakerView()
                .getBusbarSection("BBS2_2")
                .newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(2)
                .withSectionIndex(2)
                .add();

        //Fork topology
        createSwitch(vl2, "DISC_BBS1.1_BBS1.2", "DISC_BBS1.1_BBS1.2", SwitchKind.DISCONNECTOR, false, false, false, 0, 1);
        createSwitch(vl2, "DISC_BBS2.1_BBS2.2", "DISC_BBS2.1_BBS2.2", SwitchKind.DISCONNECTOR, false, true, false, 3, 4);
        createSwitch(vl2, "DISC_BBS1_2", "DISC_BBS1_2", SwitchKind.DISCONNECTOR, false, true, false, 1, 6);
        createSwitch(vl2, "DISC_BBS2_2", "DISC_BBS2_2", SwitchKind.DISCONNECTOR, false, true, false, 4, 6);
        createSwitch(vl2, "BRK_FORK", "BRK_FORK", SwitchKind.BREAKER, false, true, false, 6, 7);
        createSwitch(vl2, "DISC_LINE_1_2", "DISC_LINE_1_2", SwitchKind.DISCONNECTOR, false, false, false, 7, 8);
        createSwitch(vl2, "DISC_LINE_2_2", "DISC_LINE_2_2", SwitchKind.DISCONNECTOR, false, false, false, 7, 9);
        createSwitch(vl2, "BRK_LINE_1_2", "BRK_LINE_1_2", SwitchKind.BREAKER, false, false, false, 8, 10);
        createSwitch(vl2, "BRK_LINE_2_2", "BRK_LINE_2_2", SwitchKind.BREAKER, false, false, false, 9, 11);

        // Internal Connection
        createSwitch(vl2, "DISC_BUS1_2_TD1", "DISC_BUS1_2_TD1", SwitchKind.DISCONNECTOR, false, false, false, 1, 12);
        createSwitch(vl2, "DISC_BUS2_2_TD1", "DISC_BUS2_2_TD1", SwitchKind.DISCONNECTOR, false, false, false, 4, 13);

        // BYPASS topology
        createSwitch(vl2, "DISC_BBS1_1", "DISC_BBS1_1", SwitchKind.DISCONNECTOR, false, true, false, 0, 19);
        createSwitch(vl2, "DISC_BBS2_1", "DISC_BBS2_1", SwitchKind.DISCONNECTOR, false, true, false, 3, 22);
        createSwitch(vl2, "DISC_BYPASS", "DISC_BYPASS", SwitchKind.DISCONNECTOR, false, true, false, 19, 22);
        createSwitch(vl2, "DISC_LINE_1_1", "DISC_LINE_1_1", SwitchKind.DISCONNECTOR, false, true, false, 19, 16);
        createSwitch(vl2, "DISC_LINE_2_1", "DISC_LINE_2_1", SwitchKind.DISCONNECTOR, false, true, false, 22, 25);
        createSwitch(vl2, "BRK_LINE_1_1", "BRK_LINE_1_1", SwitchKind.BREAKER, false, true, false, 16, 17);
        createSwitch(vl2, "BRK_LINE_2_1", "BRK_LINE_2_1", SwitchKind.BREAKER, false, true, false, 25, 26);

        network.newLine()
                .setId("LINE_1_2")
                .setName("LINE_1_2")
                .setVoltageLevel1("VL2")
                .setNode1(10)
                .setVoltageLevel2("VL1")
                .setNode2(10)
                .setR(2.0)
                .setX(25.0)
                .setG1(0.0)
                .setB1(300E-6 / 2)
                .setG2(0.0)
                .setB2(300E-6 / 2)
                .add();

        network.newLine()
                .setId("LINE_2_2")
                .setName("LINE_2_2")
                .setVoltageLevel1("VL2")
                .setNode1(11)
                .setVoltageLevel2("VL1")
                .setNode2(19)
                .setR(2.0)
                .setX(25.0)
                .setG1(0.0)
                .setB1(300E-6 / 2)
                .setG2(0.0)
                .setB2(300E-6 / 2)
                .add();
        network.newLine()
                .setId("LINE_1_1")
                .setName("LINE_1_1")
                .setVoltageLevel1("VL2")
                .setNode1(17)
                .setVoltageLevel2("VL1")
                .setNode2(12)
                .setR(2.0)
                .setX(25.0)
                .setG1(0.0)
                .setB1(300E-6 / 2)
                .setG2(0.0)
                .setB2(300E-6 / 2)
                .add();

        network.newLine()
                .setId("LINE_2_1")
                .setName("LINE_2_1")
                .setVoltageLevel1("VL2")
                .setNode1(26)
                .setVoltageLevel2("VL1")
                .setNode2(13)
                .setR(2.0)
                .setX(25.0)
                .setG1(0.0)
                .setB1(300E-6 / 2)
                .setG2(0.0)
                .setB2(300E-6 / 2)
                .add();

        s1.newTwoWindingsTransformer()
                .setId("TD1")
                .setName("TD1")
                .setVoltageLevel1("VL2")
                .setNode1(12)
                .setVoltageLevel2("VL2")
                .setNode2(13)
                .setR(0.24)
                .setX(2.4)
                .setG(0.0)
                .setB(0.0)
                .add();
        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
    }

    private static void createSwitch(VoltageLevel vl, String id, String name, SwitchKind kind, boolean retained, boolean open, boolean fictitious, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(name)
                .setKind(kind)
                .setRetained(retained)
                .setOpen(open)
                .setFictitious(fictitious)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }
}


