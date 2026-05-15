/**
 * Copyright (c) 2019-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tck.AbstractNodeBreakerTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NodeBreakerTest extends AbstractNodeBreakerTest {

    @Test
    void testFictitiousP0AndFictitiousQ0ForInvalidatedBus() {
        Network network = createNetwork();
        Bus bus = network.getVoltageLevel("VL1").getBusView().getBus("VL1_1");
        network.getSwitch("B_L1_1").setOpen(true);
        assertThrows(PowsyblException.class, bus::getFictitiousP0, "Bus has been invalidated");
        assertThrows(PowsyblException.class, bus::getFictitiousQ0, "Bus has been invalidated");
    }

    @Test
    void testGetConnectableBusDependOnSwitchCreationOrder() {
        // Two identical networks
        Network network1 = createIsolatedLoadNetwork(true);
        Network network2 = createIsolatedLoadNetwork(false);
        // ConnectableBus gives different value from 2 identical networks
        String connectableBus1 = network1.getLoad("Load").getTerminal().getBusView().getConnectableBus().getId();
        String connectableBus2 = network2.getLoad("Load").getTerminal().getBusView().getConnectableBus().getId();
        assertEquals("VL_1", connectableBus1);
        assertEquals("VL_4", connectableBus2);
    }


    /**
     * <pre>
     *                BBS_A      (switch_A closed)    Load_A
     * ---------------(1)---------------|--------------(2)-
     *                 |
     *                     (switch_AL open)
     *                 |
     *                (3) Load
     *                 |
     *                     (switch_BL open)
     *                 |
     * ---------------(4)---------------|---------------(5)-
     *               BBS_B      (switch_B closed)      Load_B
     * </pre>
     */
    private Network createIsolatedLoadNetwork(boolean addOpenedSwitchALFirst) {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation().setId("Substation").add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        // BBS_A (1) -- (switch_A closed) -- Load_A (2)
        vl.getNodeBreakerView().newBusbarSection().setId("BBS_A").setNode(1).add();
        vl.newLoad().setId("Load_A").setNode(2).setP0(10).setQ0(-5).add();
        vl.getNodeBreakerView().newBreaker()
                .setId("switch_A")
                .setNode1(1)
                .setNode2(2)
                .setOpen(false)
                .add();
        // BBS_B (4) -- (switch_B closed) -- Load_B (5)
        vl.getNodeBreakerView().newBusbarSection().setId("BBS_B").setNode(4).add();
        vl.newLoad().setId("Load_B").setNode(5).setP0(4).setQ0(-5).add();
        vl.getNodeBreakerView().newBreaker()
                .setId("switch_B")
                .setNode1(4)
                .setNode2(5)
                .setOpen(false)
                .add();
        // Load (node 3): isolated
        vl.newLoad().setId("Load").setNode(3).setP0(5).setQ0(-2).add();
        // Two paths from node 3
        if (addOpenedSwitchALFirst) {
            vl.getNodeBreakerView().newBreaker().setId("switch_AL").setNode1(1).setNode2(3).setOpen(true).add();
            vl.getNodeBreakerView().newBreaker().setId("switch_BL").setNode1(3).setNode2(4).setOpen(true).add();
        } else {
            vl.getNodeBreakerView().newBreaker().setId("switch_BL").setNode1(3).setNode2(4).setOpen(true).add();
            vl.getNodeBreakerView().newBreaker().setId("switch_AL").setNode1(1).setNode2(3).setOpen(true).add();
        }
        return network;
    }
}
