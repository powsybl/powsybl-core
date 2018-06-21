/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SwitchOpenCloseNodeBreakerTopoTest {

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
                .setNode(3)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D1")
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
        vl.getNodeBreakerView().newDisconnector()
                .setId("2")
                .setNode1(2)
                .setNode2(3)
                .setOpen(false)
                .add();
        vl.newLoad()
                .setId("L1")
                .setNode(4)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.newLoad()
                .setId("L2")
                .setNode(5)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B2")
                .setNode1(0)
                .setNode2(4)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B3")
                .setNode1(3)
                .setNode2(5)
                .setOpen(false)
                .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        VoltageLevel vl = network.getVoltageLevel("VL");
        assertNotNull(vl);
        assertEquals(2, Iterables.size(vl.getBusView().getBuses()));
        Switch s = vl.getNodeBreakerView().getSwitch("B1");
        s.setOpen(false);
        assertEquals(1, Iterables.size(vl.getBusView().getBuses()));
        s.setOpen(true);
        assertEquals(2, Iterables.size(vl.getBusView().getBuses()));
    }
}
