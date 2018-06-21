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
public class NodeBreakerDisconnectionDoublePathBugTest {

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
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(1)
                .add();
        vl.newLoad()
                .setId("L")
                .setNode(2)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR0")
                .setNode1(2)
                .setNode2(3)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR1")
                .setNode1(3)
                .setNode2(0)
                .setOpen(true)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("BR2")
                .setNode1(3)
                .setNode2(1)
                .setOpen(false)
                .add();
        return network;
    }

    @Test
    public void testOnePathAlreadyOpen() {
        Network network = createNetwork();
        Load l = network.getLoad("L");
        assertTrue(l.getTerminal().isConnected());
        l.getTerminal().disconnect();
        assertFalse(l.getTerminal().isConnected());
    }

    @Test
    public void testBothPathClosed() {
        Network network = createNetwork();
        Load l = network.getLoad("L");
        network.getSwitch("BR1").setOpen(false);
        assertTrue(l.getTerminal().isConnected());
        l.getTerminal().disconnect();
        assertFalse(l.getTerminal().isConnected());
    }
}
