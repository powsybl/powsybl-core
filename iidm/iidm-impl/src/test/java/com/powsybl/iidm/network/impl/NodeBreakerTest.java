/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerTest {

    private Network createNetwork() {
        Network network = NetworkFactory.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().setNodeCount(10);
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

    @Test
    public void connectDisconnect() {
        Network network = createNetwork();
        Load l = network.getLoad("L");
        Generator g = network.getGenerator("G");

        // generator is connected, load is disconnected
        assertNotNull(g.getTerminal().getBusView().getBus());
        assertNull(l.getTerminal().getBusView().getBus());
        assertTrue(g.getTerminal().isConnected());
        assertFalse(l.getTerminal().isConnected());

        // connect the load
        assertTrue(l.getTerminal().connect());

        // check load is connected
        assertNotNull(l.getTerminal().getBusView().getBus());
        assertTrue(l.getTerminal().isConnected());

        // disconnect the generator
        g.getTerminal().disconnect();

        // check generator is disconnected
        assertNull(g.getTerminal().getBusView().getBus());
        assertFalse(g.getTerminal().isConnected());
    }

    @Test
    public void cleanTopology() {
        Network network = NetworkTest1Factory.create();

        VoltageLevelExt vl = (VoltageLevelExt) network.getVoltageLevel("voltageLevel1");
        assertNotNull(vl);

        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        assertEquals(10, topo.getNodeCount());
        vl.clean();

        // Check useless nodes have been removed from the topology
        assertEquals(6, topo.getNodeCount());
    }

    @Test
    public void removeSwitch() {
        Network network = NetworkTest1Factory.create();

        VoltageLevelExt vl = (VoltageLevelExt) network.getVoltageLevel("voltageLevel1");
        assertNotNull(vl);

        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        vl.clean();

        assertEquals(6, topo.getNodeCount());
        assertNotNull(topo.getSwitch("load1Disconnector1"));
        assertNotNull(topo.getSwitch("load1Breaker1"));
        topo.removeSwitch("load1Disconnector1");
        topo.removeSwitch("load1Breaker1");

        // Check the 2 switches and the intermediate node have been removed from the topology.
        assertNull(topo.getSwitch("load1Disconnector1"));
        assertNull(topo.getSwitch("load1Breaker1"));
        assertEquals(5, topo.getNodeCount());

        assertNull(network.getSwitch("load1Disconnector1"));
        assertNull(network.getSwitch("load1Breaker1"));
        assertNull(network.getIdentifiable("load1Disconnector1"));
        assertNull(network.getIdentifiable("load1Breaker1"));
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
}
