/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractNodeBreakerTest {

    protected Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        // Busbar sections
        BusbarSection bbs11 = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS11")
            .setNode(0)
            .add();
        bbs11.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs21 = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS21")
            .setNode(1)
            .add();
        bbs21.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs12 = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS12")
            .setNode(2)
            .add();
        bbs12.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(2)
            .add();
        BusbarSection bbs22 = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS22")
            .setNode(3)
            .add();
        bbs22.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(2)
            .add();

        // Disconnectors for coupling
        vl.getNodeBreakerView().newDisconnector()
            .setId("D_BBS11_BBS12")
            .setNode1(0)
            .setNode2(2)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D_BBS21_BBS22")
            .setNode1(1)
            .setNode2(3)
            .setOpen(false)
            .add();

        // Generators and loads
        vl.newLoad()
            .setId("L1")
            .setNode(4)
            .setP0(1)
            .setQ0(1)
            .add();
        vl.newGenerator()
            .setId("G1")
            .setNode(5)
            .setMaxP(100)
            .setMinP(50)
            .setTargetP(100)
            .setTargetV(400)
            .setVoltageRegulatorOn(true)
            .add();
        vl.newGenerator()
            .setId("G2")
            .setNode(6)
            .setMaxP(100)
            .setMinP(50)
            .setTargetP(100)
            .setTargetV(400)
            .setVoltageRegulatorOn(true)
            .add();

        // Breakers
        vl.getNodeBreakerView().newBreaker()
            .setId("B_L1_1")
            .setNode1(4)
            .setNode2(7)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B_L1_2")
            .setNode1(4)
            .setNode2(7)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B_G1")
            .setNode1(5)
            .setNode2(8)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B_G2")
            .setNode1(6)
            .setNode2(9)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B0")
            .setNode1(7)
            .setNode2(17)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B1")
            .setNode1(8)
            .setNode2(11)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B2")
            .setNode1(9)
            .setNode2(12)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B3")
            .setNode1(7)
            .setNode2(8)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B4")
            .setNode1(8)
            .setNode2(9)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B5")
            .setNode1(17)
            .setNode2(10)
            .setOpen(false)
            .add();

        // Disconnectors
        vl.getNodeBreakerView().newDisconnector()
            .setId("D0")
            .setNode1(0)
            .setNode2(10)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D1")
            .setNode1(1)
            .setNode2(10)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D2")
            .setNode1(0)
            .setNode2(11)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D3")
            .setNode1(1)
            .setNode2(11)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D4")
            .setNode1(2)
            .setNode2(12)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D5")
            .setNode1(3)
            .setNode2(12)
            .setOpen(true)
            .add();
        return network;
    }

    private static Network createIsolatedLoadNetwork() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation().setId("S").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL").setNominalV(1f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel vl2 = s1.newVoltageLevel().setId("VL2").setNominalV(1f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl1.getNodeBreakerView()
                .newBusbarSection()
                .setId("B0")
                .setNode(0)
                .add();
        vl1.getNodeBreakerView()
                .newBusbarSection()
                .setId("B1")
                .setNode(1)
                .add();
        vl1.getNodeBreakerView()
                .newBusbarSection()
                .setId("B2")
                .setNode(2)
                .add();

        vl1.newLoad()
                .setId("L0")
                .setNode(6)
                .setP0(0)
                .setQ0(0)
                .add();
        vl1.newLoad()
                .setId("L1")
                .setNode(3)
                .setP0(0)
                .setQ0(0)
                .add();
        vl1.newLoad()
                .setId("L2")
                .setNode(4)
                .setP0(0)
                .setQ0(0)
                .add();
        vl1.newLoad()
                .setId("L3")
                .setNode(5)
                .setP0(0)
                .setQ0(0)
                .add();

        vl1.getNodeBreakerView().newBreaker()
                .setId("L0-node")
                .setOpen(false)
                .setNode1(0)
                .setNode2(6)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("L1-node")
                .setOpen(true)
                .setNode1(4)
                .setNode2(10)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("L0-B0")
                .setOpen(false)
                .setNode1(3)
                .setNode2(1)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("B0-node")
                .setOpen(true)
                .setNode1(1)
                .setNode2(10)
                .setRetained(true)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("node-B1")
                .setOpen(false)
                .setNode1(10)
                .setNode2(2)
                .setRetained(true)
                .add();

        vl2.newLoad()
                .setId("L4")
                .setNode(0)
                .setP0(0)
                .setQ0(0)
                .add();
        return network;
    }

    /**
     * <pre>
     *                load
     *                  |
     *               ___|___
     *               |     |
     *           fd1 x     x fd2
     * bbs1 _________|__   |
     *        |            |
     *        c            |
     * bbs2 __|____________|__
     * </pre>
     */
    private static Network createNetworkWithLoop() {
        Network network = Network.create("test", "test");
        Substation substation = network.newSubstation().setId("s").add();
        VoltageLevel vl = substation.newVoltageLevel().setId("vl").setNominalV(400).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        VoltageLevel.NodeBreakerView topology = vl.getNodeBreakerView();
        topology.newBusbarSection().setId("bbs1").setNode(0).add();
        topology.newBusbarSection().setId("bbs2").setNode(1).add();
        topology.newDisconnector().setId("fd1").setNode1(0).setNode2(2).add();
        topology.newDisconnector().setId("fd2").setNode1(1).setNode2(2).add();
        topology.newBreaker().setId("c").setNode1(0).setNode2(1).add();
        vl.newLoad().setId("load").setNode(2).setP0(10).setQ0(3).add();
        return network;
    }

    @Test
    public void connectDisconnectRemove() {
        Network network = createNetwork();

        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Load l1 = network.getLoad("L1");
        Generator g1 = network.getGenerator("G1");
        Generator g2 = network.getGenerator("G2");

        // generator 1 is disconnected, load and generator 2 are connected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertTrue(topo.getOptionalTerminal(6).isPresent());
        assertNotNull(l1.getTerminal().getBusView().getBus());
        assertNull(g1.getTerminal().getBusView().getBus());
        assertNotNull(g2.getTerminal().getBusView().getBus());
        assertTrue(l1.getTerminal().isConnected());
        assertFalse(g1.getTerminal().isConnected());
        assertTrue(g2.getTerminal().isConnected());

        // connect the generator 1
        assertTrue(g1.getTerminal().connect());

        // check generator 1 is connected
        assertTrue(topo.getOptionalTerminal(5).isPresent());
        assertNotNull(g1.getTerminal().getBusView().getBus());
        assertTrue(g1.getTerminal().isConnected());

        // disconnect the load
        l1.getTerminal().disconnect();

        // check load is disconnected
        assertTrue(topo.getOptionalTerminal(4).isPresent());
        assertNull(l1.getTerminal().getBusView().getBus());
        assertFalse(l1.getTerminal().isConnected());

        // remove load
        l1.remove();
        topo.removeSwitch("B_L1_1");
        topo.removeSwitch("B_L1_2");

        // check load is removed
        assertFalse(topo.getOptionalTerminal(4).isPresent());

        // disconnect and reconnect the generator 1
        g1.getTerminal().disconnect();
        network.getSwitch("B_G1").setFictitious(true);
        network.getSwitch("B1").setFictitious(true);
        assertFalse(g1.getTerminal().connect(SwitchPredicates.IS_NONFICTIONAL_BREAKER));
    }

    @Test
    public void errorDisconnectOnRemoved() {
        Network network = createNetwork();

        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Load l1 = network.getLoad("L1");

        // disconnect the load
        l1.getTerminal().disconnect(SwitchPredicates.IS_CLOSED_BREAKER);

        // remove load
        l1.remove();
        topo.removeSwitch("B_L1_1");
        topo.removeSwitch("B_L1_2");

        // Load terminal (used to have only one possible runtime exception)
        Terminal loadTerminal = l1.getTerminal();

        // disconnect the load again
        PowsyblException exception = assertThrows(PowsyblException.class, () -> loadTerminal.disconnect(SwitchPredicates.IS_CLOSED_BREAKER));
        assertTrue(Pattern.compile("Cannot modify removed equipment(?: L1)?$").matcher(exception.getMessage()).find());
    }

    @Test
    public void errorConnectOnRemoved() {
        Network network = createNetwork();

        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL1").getNodeBreakerView();
        Load l1 = network.getLoad("L1");

        // disconnect the load
        l1.getTerminal().disconnect(SwitchPredicates.IS_CLOSED_BREAKER);

        // remove load
        l1.remove();
        topo.removeSwitch("B_L1_1");
        topo.removeSwitch("B_L1_2");

        // Load terminal (used to have only one possible runtime exception)
        Terminal loadTerminal = l1.getTerminal();

        // disconnect the load again
        PowsyblException exception = assertThrows(PowsyblException.class, loadTerminal::connect);
        assertTrue(Pattern.compile("Cannot modify removed equipment(?: L1)?$").matcher(exception.getMessage()).find());
    }

    @Test
    public void failToDisconnectWhenAlreadyDisconnected() {
        Network network = createNetwork();
        Generator g1 = network.getGenerator("G1");

        // disconnect the generator
        assertFalse(g1.getTerminal().disconnect(SwitchPredicates.IS_CLOSED_BREAKER));
    }

    @Test
    public void failToDisconnectDueToPredicate() {
        Network network = createNetwork();
        Generator g2 = network.getGenerator("G2");

        // disconnect the generator
        assertFalse(g2.getTerminal().disconnect(SwitchPredicates.IS_NONFICTIONAL_CLOSED_BREAKER));
    }

    private static Bus getBusInBusBreakerView(Injection<?> i) {
        return i.getTerminal().getBusBreakerView().getBus();
    }

    private static Bus getBusInBusView(Injection<?> i) {
        return i.getTerminal().getBusView().getBus();
    }

    private static Bus getConnectableBusInBusBreakerView(Injection<?> i) {
        return i.getTerminal().getBusBreakerView().getConnectableBus();
    }

    private static Bus getConnectableBusInBusView(Injection<?> i) {
        return i.getTerminal().getBusView().getConnectableBus();
    }

    @Test
    public void replaceLoad() {
        Network network = NetworkTest1Factory.create();
        VoltageLevel vl = network.getVoltageLevel("voltageLevel1");

        Load l1 = network.getLoad("load1");
        int n = l1.getTerminal().getNodeBreakerView().getNode();
        LoadAdder newLoad = vl.newLoad()
                .setId("load2")
                .setP0(10)
                .setQ0(2)
                .setNode(n);

        // The new load cannot be connected since the old load is still connected to the node.
        try {
            newLoad.add();
            fail("Should have thrown a validation exception");
        } catch (ValidationException ignored) {
            // ignore
        }

        l1.remove();
        // Now the load may be attached.
        Load l2 = newLoad.add();

        assertNull(network.getLoad("load1"));
        assertNotNull(network.getLoad("load2"));
        assertEquals(n, l2.getTerminal().getNodeBreakerView().getNode());

        // Check thew load is connected to the correct bus bar.
        BusbarSection bb = vl.getNodeBreakerView().getBusbarSection("voltageLevel1BusbarSection1");
        assertEquals(getBusInBusView(bb), getBusInBusView(l2));
    }

    @Test
    public void testIsolatedLoadBusBranch() {
        Network network = createIsolatedLoadNetwork();
        assertEquals(2, network.getBusView().getBusStream().count());

        // load "L0" is connected to bus "VL_0"
        Load l0 = network.getLoad("L0");
        assertNotNull(getBusInBusBreakerView(l0));
        assertEquals("VL_0", getConnectableBusInBusBreakerView(l0).getId());
        assertNotNull(getBusInBusView(l0));
        assertEquals("VL_0", getConnectableBusInBusView(l0).getId());

        // load "L1" is connected to bus "VL_1"
        Load l1 = network.getLoad("L1");
        assertNotNull(getBusInBusBreakerView(l1));
        assertEquals("VL_1", getConnectableBusInBusBreakerView(l1).getId());
        assertNotNull(getBusInBusView(l1));
        assertEquals("VL_1", getConnectableBusInBusView(l1).getId());

        // load "L2" is not connected but is connectable to bus "VL_1"
        Load l2 = network.getLoad("L2");
        assertNotNull(getBusInBusBreakerView(l2));
        assertEquals("VL_4", getConnectableBusInBusBreakerView(l2).getId());
        assertNull(getBusInBusView(l2));
        assertEquals("VL_1", getConnectableBusInBusView(l2).getId());

        // load "L3" is not connected and has no connectable bus (the first bus is taken as connectable bus in this case)
        Load l3 = network.getLoad("L3");
        assertNotNull(getBusInBusBreakerView(l3));
        assertEquals("VL_5", getConnectableBusInBusBreakerView(l3).getId());
        assertNull(getBusInBusView(l3));
        assertEquals("VL_0", getConnectableBusInBusView(l3).getId());

        // load "L4" is not connected, has no connectable bus and is in a disconnected voltage level
        Load l4 = network.getLoad("L4");
        assertNotNull(getBusInBusBreakerView(l4));
        assertEquals("VL2_0", getConnectableBusInBusBreakerView(l4).getId());
        assertNull(getBusInBusView(l4));
        assertNull(getConnectableBusInBusView(l4));
    }

    @Test
    public void testCalculatedBus() {
        Network network = createIsolatedLoadNetwork();

        Bus busL0 = network.getLoad("L0").getTerminal().getBusBreakerView().getBus();
        assertNotNull(busL0);
        assertEquals("VL", busL0.getVoltageLevel().getId());
        assertEquals("VL_0", busL0.getId());

        assertNull(network.getBusBreakerView().getBus("unknownBus"));

        network.getVoltageLevel("VL").getNodeBreakerView().newBusbarSection().setId("VL_0").setNode(10).add();
        busL0 = network.getLoad("L0").getTerminal().getBusBreakerView().getBus();
        assertEquals("VL_0#0", busL0.getId());
    }

    @Test
    public void testRemove() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        Substation sub = network.getSubstation("S1");

        // Disconnect the substation from the network
        network.getHvdcLine("HVDC1").remove();
        network.getHvdcLine("HVDC2").remove();
        sub.getVoltageLevelStream()
                .flatMap(VoltageLevel::getVscConverterStationStream)
                .forEach(Connectable::remove);

        // Remove 1 switch
        VoltageLevel vl1 = network.getVoltageLevel("S1VL1");
        vl1.getNodeBreakerView().removeSwitch("S1VL1_LD1_BREAKER");
        assertNull(vl1.getNodeBreakerView().getSwitch("S1VL1_LD1_BREAKER"));

        // Remove substation
        sub.remove();
        assertNull(network.getSubstation("S1"));
    }

    @Test
    public void testCalculatedBusTopologyWithLoop() {
        Network n = createNetworkWithLoop();

        Bus busBbv = n.getBusBreakerView().getBus("vl_0");
        assertNotNull(busBbv);
        assertEquals(1, n.getBusBreakerView().getBusCount());
        assertEquals(3, busBbv.getConnectedTerminalCount());

        Bus busBv = n.getBusView().getBus("vl_0");
        assertNotNull(busBv);
        assertEquals(1, n.getBusView().getBusStream().count());
        assertEquals(3, busBv.getConnectedTerminalCount());
    }
}
