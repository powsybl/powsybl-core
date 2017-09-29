/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NodeBreakerConnectDisconnectTest {

    private Network createNetwork() {
        Network network = NetworkFactory.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400f)
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
    public void test() {
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
}
