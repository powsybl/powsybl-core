/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractNodeBreakerTest {

    private Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        vl.newLoad()
                .setId("L")
                .setNode(2)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.newGenerator()
                .setId("G")
                .setNode(3)
                .setMaxP(100)
                .setMinP(50)
                .setTargetP(100)
                .setTargetV(400)
                .setVoltageRegulatorOn(true)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B1")
                .setNode1(1)
                .setNode2(2)
                .setOpen(true)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B2")
                .setNode1(1)
                .setNode2(3)
                .setOpen(false)
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

    @Test
    public void connectDisconnectRemove() {
        Network network = createNetwork();
        VoltageLevel.NodeBreakerView topo = network.getVoltageLevel("VL").getNodeBreakerView();
        Load l = network.getLoad("L");
        Generator g = network.getGenerator("G");

        // generator is connected, load is disconnected
        assertTrue(topo.getOptionalTerminal(2).isPresent());
        assertTrue(topo.getOptionalTerminal(3).isPresent());
        assertNotNull(g.getTerminal().getBusView().getBus());
        assertNull(l.getTerminal().getBusView().getBus());
        assertTrue(g.getTerminal().isConnected());
        assertFalse(l.getTerminal().isConnected());

        // connect the load
        assertTrue(l.getTerminal().connect());

        // check load is connected
        assertTrue(topo.getOptionalTerminal(2).isPresent());
        assertNotNull(l.getTerminal().getBusView().getBus());
        assertTrue(l.getTerminal().isConnected());

        // disconnect the generator
        g.getTerminal().disconnect();

        // check generator is disconnected
        assertTrue(topo.getOptionalTerminal(3).isPresent());
        assertNull(g.getTerminal().getBusView().getBus());
        assertFalse(g.getTerminal().isConnected());

        // remove generator
        g.remove();
        topo.removeSwitch("B2");

        // check generator is removed
        assertFalse(topo.getOptionalTerminal(3).isPresent());
    }

    private static Bus getBus(Injection i) {
        return i.getTerminal().getBusView().getBus();
    }

    private static Bus getConnectableBus(Injection i) {
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
        assertEquals(getBus(bb), getBus(l2));
    }

    @Test
    public void testIsolatedLoadBusBranch() {
        Network network = createIsolatedLoadNetwork();
        assertEquals(2, network.getBusView().getBusStream().count());

        // load "L0" is connected to bus "VL_0"
        assertNotNull(getBus(network.getLoad("L0")));
        assertEquals("VL_0", getConnectableBus(network.getLoad("L0")).getId());

        // load "L1" is connected to bus "VL_1"
        assertNotNull(getBus(network.getLoad("L1")));
        assertEquals("VL_1", getConnectableBus(network.getLoad("L1")).getId());

        // load "L2" is not connected but is connectable to bus "VL_1"
        assertNull(getBus(network.getLoad("L2")));
        assertEquals("VL_1", getConnectableBus(network.getLoad("L2")).getId());

        // load "L3" is not connected and has no connectable bus (the first bus is taken as connectable bus in this case)
        assertNull(getBus(network.getLoad("L3")));
        assertEquals("VL_0", getConnectableBus(network.getLoad("L3")).getId());

        // load "L4" is not connected, has no connectable bus and is in a disconnected voltage level
        assertNull(getBus(network.getLoad("L4")));
        assertNull(getConnectableBus(network.getLoad("L4")));
    }
}
