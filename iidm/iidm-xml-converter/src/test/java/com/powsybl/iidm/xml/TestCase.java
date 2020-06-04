/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class TestCase {

    @Test
    public void test1() {
        Network network = Network.create("test", "test");

        Substation s = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL1")
                .setNominalV(100)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("BUS1")
                .add();
        vl.newGenerator()
                .setId("G1")
                .setBus("BUS1")
                .setMinP(0)
                .setMaxP(100)
                .setTargetP(0)
                .setTargetQ(0)
                .setTargetV(0)
                .setVoltageRegulatorOn(false)
                .add();

        vl = s.newVoltageLevel()
                .setId("VL2")
                .setNominalV(100)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BK_BBS1_LINE1")
                .setNode1(0)
                .setNode2(1)
                .setRetained(true)
                .setOpen(true)
                .add();
        Line line = network.newLine()
                .setId("LINE1")
                .setVoltageLevel1("VL1")
                .setBus1("BUS1")
                .setVoltageLevel2("VL2")
                .setNode2(1)
                .setR(0)
                .setX(0)
                .setG1(0)
                .setB1(0)
                .setG2(0)
                .setB2(0)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BK")
                .setNode1(0)
                .setNode2(2)
                .setOpen(true)
                .setRetained(true)
                .add();
        Load load = vl.newLoad()
                .setId("LOAD")
                .setNode(2)
                .setP0(0)
                .setQ0(0)
                .add();

        assertEquals(3, vl.getBusBreakerView().getBusStream().count());
        // FIXME(mathbagu): 1 bus is expected
        // assertEquals(1, vl.getBusView().getBusStream().count());
        assertTrue(line.getTerminal1().isConnected());
        // FIXME(mathbagu): terminal 2 is expected to be connected
        // assertTrue(line.getTerminal2().isConnected());
        assertFalse(load.getTerminal().isConnected());
        assertNull(load.getTerminal().getBusView().getBus());
        assertNull(load.getTerminal().getBusView().getConnectableBus());

        // Clone the network in Bus/Breaker topology
        ExportOptions options = new ExportOptions().setTopologyLevel(TopologyLevel.BUS_BREAKER);
        Network clone = NetworkXml.copy(network, options);
        VoltageLevel cloneVL = clone.getVoltageLevel("VL2");
        Line cloneLine = clone.getLine("LINE1");
        Load cloneLoad = clone.getLoad("LOAD");

        assertEquals(3, cloneVL.getBusBreakerView().getBusStream().count());
        assertEquals(1, cloneVL.getBusView().getBusStream().count());
        assertTrue(cloneLine.getTerminal1().isConnected());
        assertTrue(cloneLine.getTerminal2().isConnected());
        // FIXME(mathbagu): The load is expected to be disconnected
        // assertFalse(cloneLoad.getTerminal().isConnected());
        assertNull(cloneLoad.getTerminal().getBusView().getBus());
        // FIXME(mathbagu): The load is expected to be reconnectable
        // assertNotNull(cloneLoad.getTerminal().getBusView().getConnectableBus());
    }

    private static Network createBase() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .add();
        VoltageLevel vl1 = s.newVoltageLevel()
                .setId("VL1")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0);
        vl1.getNodeBreakerView().newBreaker()
                .setId("BK1")
                .setRetained(true)
                .setOpen(false)
                .setNode1(0)
                .setNode2(1)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("BK2")
                .setRetained(true)
                .setOpen(false)
                .setNode1(0)
                .setNode2(2)
                .add();
        vl1.newGenerator()
                .setId("G1")
                .setNode(1)
                .setMinP(0)
                .setMaxP(100)
                .setTargetP(0)
                .setTargetQ(0)
                .setTargetV(0)
                .setVoltageRegulatorOn(false)
                .add();

        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("VL2")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(225)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(0)
                .add();
        vl2.getNodeBreakerView().newBreaker()
                .setId("BK3")
                .setNode1(0)
                .setNode2(1)
                .add();

        return network;
    }

    @Test
    public void test2() {
        Network network = createBase();

        Line line = network.newLine()
                .setId("LINE")
                .setVoltageLevel1("VL1")
                .setNode1(2)
                .setVoltageLevel2("VL2")
                .setNode2(1)
                .setR(0)
                .setX(0)
                .setG1(0)
                .setB1(0)
                .setG2(0)
                .setB2(0)
                .add();

        assertTrue(line.getTerminal1().isConnected());
        assertTrue(line.getTerminal2().isConnected());
        assertNotNull(line.getTerminal2().getBusBreakerView().getBus());
        assertNotNull(line.getTerminal2().getBusView().getBus());

        // Clone the network in Bus/Breaker topology
        ExportOptions options = new ExportOptions().setTopologyLevel(TopologyLevel.BUS_BREAKER);
        Network clone = NetworkXml.copy(network, options);

        Line cloneLine = clone.getLine("LINE");
        assertTrue(cloneLine.getTerminal1().isConnected());
        assertTrue(cloneLine.getTerminal2().isConnected());
        assertNotNull(cloneLine.getTerminal2().getBusBreakerView().getBus());
        assertNotNull(cloneLine.getTerminal2().getBusView().getBus());
    }

    @Test
    public void test3() {
        Network network = createBase();

        network.getBusbarSection("BBS2").remove();
        Line line = network.newLine()
                .setId("LINE")
                .setVoltageLevel1("VL1")
                .setNode1(2)
                .setVoltageLevel2("VL2")
                .setNode2(1)
                .setR(0)
                .setX(0)
                .setG1(0)
                .setB1(0)
                .setG2(0)
                .setB2(0)
                .add();

        // FIXME(mathbagu)
        /*
        assertTrue(line.getTerminal1().isConnected());
        assertTrue(line.getTerminal2().isConnected());
        assertNotNull(line.getTerminal2().getBusBreakerView().getBus());
        assertNotNull(line.getTerminal2().getBusView().getBus());

        // Clone the network in Bus/Breaker topology
        ExportOptions options = new ExportOptions().setTopologyLevel(TopologyLevel.BUS_BREAKER);
        Network clone = NetworkXml.copy(network, options);

        Line cloneLine = clone.getLine("LINE");
        assertTrue(cloneLine.getTerminal1().isConnected());
        assertTrue(cloneLine.getTerminal2().isConnected());
        assertNotNull(cloneLine.getTerminal2().getBusBreakerView().getBus());
        assertNotNull(cloneLine.getTerminal2().getBusView().getBus());
         */
    }

    private static Network createBaseWithDanglingLines() {
        Network network = createBase();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        vl1.newDanglingLine()
                .setId("DL1")
                .setNode(2)
                .setR(0)
                .setX(0)
                .setG(0)
                .setB(0)
                .setP0(0)
                .setQ0(0)
                .setUcteXnodeCode("XNODE")
                .add();

        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        DanglingLine dl = vl2.newDanglingLine()
                .setId("DL2")
                .setNode(1)
                .setR(0)
                .setX(0)
                .setG(0)
                .setB(0)
                .setP0(0)
                .setQ0(0)
                .setUcteXnodeCode("XNODE")
                .add();
        return network;
    }

    @Test
    public void test4() {
        Network network = createBaseWithDanglingLines();

        DanglingLine dl = network.getDanglingLine("DL2");

        assertTrue(dl.getTerminal().isConnected());
        assertTrue(dl.getTerminal().isConnected());
        assertNotNull(dl.getTerminal().getBusBreakerView().getBus());
        assertNotNull(dl.getTerminal().getBusView().getBus());
    }

    @Test
    public void test5() {
        Network network = createBaseWithDanglingLines();

        network.getBusbarSection("BBS2").remove();
        DanglingLine dl = network.getDanglingLine("DL2");

        // FIXME(mathbagu)
        /*
        assertTrue(dl.getTerminal().isConnected());
        assertTrue(dl.getTerminal().isConnected());
        assertNotNull(dl.getTerminal().getBusBreakerView().getBus());
        assertNotNull(dl.getTerminal().getBusView().getBus());
         */
    }

    private static Network createNetworkWithHVDC() {
        Network network = createBase();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        vl1.newLccConverterStation()
                .setId("LCC_CS1")
                .setNode(2)
                .setPowerFactor(0.6f)
                .setLossFactor(1.1f)
                .add();

        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        vl2.newLccConverterStation()
                .setId("LCC_CS2")
                .setNode(1)
                .setPowerFactor(0.6f)
                .setLossFactor(1.1f)
                .add();

        network.newHvdcLine()
                .setId("HVDC")
                .setR(0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .setConverterStationId1("LCC_CS1")
                .setConverterStationId2("LCC_CS2")
                .setNominalV(400)
                .setActivePowerSetpoint(0)
                .setMaxP(100)
                .add();

        return network;
    }

    @Test
    public void test6() {
        Network network = createNetworkWithHVDC();

        LccConverterStation cs = network.getLccConverterStation("LCC_CS2");

        assertTrue(cs.getTerminal().isConnected());
        assertTrue(cs.getTerminal().isConnected());
        assertNotNull(cs.getTerminal().getBusBreakerView().getBus());
        assertNotNull(cs.getTerminal().getBusView().getBus());
    }

    @Test
    public void test7() {
        Network network = createNetworkWithHVDC();

        network.getBusbarSection("BBS2").remove();
        LccConverterStation cs = network.getLccConverterStation("LCC_CS2");

        // FIXME(mathbagu)
        /*
        assertTrue(cs.getTerminal().isConnected());
        assertTrue(cs.getTerminal().isConnected());
        assertNotNull(cs.getTerminal().getBusBreakerView().getBus());
        assertNotNull(cs.getTerminal().getBusView().getBus());
         */
    }

    @Test
    public void test8() {
        Network network = createNetworkWithHVDC();

        network.getHvdcLine("HVDC").remove();
        LccConverterStation cs = network.getLccConverterStation("LCC_CS2");

        assertTrue(cs.getTerminal().isConnected());
        assertTrue(cs.getTerminal().isConnected());
        assertNotNull(cs.getTerminal().getBusBreakerView().getBus());
        assertNotNull(cs.getTerminal().getBusView().getBus());
    }
}
