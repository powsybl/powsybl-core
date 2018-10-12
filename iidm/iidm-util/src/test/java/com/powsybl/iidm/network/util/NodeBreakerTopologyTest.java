/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class NodeBreakerTopologyTest {

    @Test
    public void removeIsolatedSwitches() throws Exception {

        Network network = NetworkTest1Factory.create();

        VoltageLevel vl = network.getVoltageLevel("voltageLevel1");
        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();

        assertNotNull(vl);

        assertNotNull(topo.getSwitch("load1Disconnector1"));
        assertNotNull(topo.getSwitch("load1Breaker1"));
        assertEquals(5, topo.getSwitchCount());

        // remove the load
        vl.getConnectable("load1", Load.class).remove();
        assertNull(vl.getConnectable("load1", Load.class));

        // remove the switch connected to the bus bar
        topo.removeSwitch("load1Breaker1");
        assertNull(topo.getSwitch("load1Breaker1"));

        // The connecting switch of the load is now isolated: remove it
        NodeBreakerTopology.removeIsolatedSwitches(topo);

        assertNull(topo.getSwitch("load1Disconnector1"));
        assertNull(topo.getSwitch("load1Breaker1"));
        // 2 switches have been removed
        assertEquals(3, topo.getSwitchCount());
    }


    @Test
    public void newStandardConnection() throws Exception {
        Network network = NetworkTest1Factory.create();

        VoltageLevel vl = network.getVoltageLevel("voltageLevel1");
        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();

        int initialSwitchCount = topo.getSwitchCount();

        BusbarSection bb = topo.getBusbarSection("voltageLevel1BusbarSection1");
        int connectionNode = NodeBreakerTopology.newStandardConnection(bb);

        Load l = vl.newLoad()
                .setId("load2")
                .setP0(10)
                .setQ0(0)
                .setNode(connectionNode)
                .add();

        // Check the new load is correctly connected to the bus corresponding to the bus bar.
        assertEquals(bb.getTerminal().getBusView().getBus(), l.getTerminal().getBusView().getBus());
        assertEquals(initialSwitchCount + 2, topo.getSwitchCount());
    }

}
