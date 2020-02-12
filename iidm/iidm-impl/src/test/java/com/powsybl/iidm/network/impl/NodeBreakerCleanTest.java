/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeBreakerCleanTest {

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

        // Check useless nodes have been removed from the topology
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

}
