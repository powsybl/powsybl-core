/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static java.lang.Float.NaN;
import static org.junit.jupiter.api.Assertions.*;

public class Issue3200Test {

    @Test
    void testIssu3200WithRetainedSwitch() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .add();

        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(225.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(1)
                .add();

        vl.getNodeBreakerView().newBreaker()
                .setId("BR1")
                .setRetained(true)
                .setOpen(false)
                .setNode1(1)
                .setNode2(2)
                .add();
        /**
         * nodeView   node1 (BBS1 terminal T1) ---- [ BR1 : closed, retained ] ----  node2 (no equipment)
         * busView      [VL_1]                        |BR1 closed|                         [VL_2]
         *
         * equivalentTerminal for VL_2
         *   start node2 -- (BR1 closed => terminated) --> node1 => find T1
         *   => bus VL_2 use T1 (same as bus VL_1)
         *   => setV(VL_1)=123  => getV(VL_2)=123
         *
         * =======================================================================
         *
         * nodeView   node1 (BBS1 terminal T1) ---- [ BR1 : open, retained ]   --X-- node2 (no equipment)
         * busView      [VL_1]                        |BR1 open|                         [VL_2]
         *
         * bus VL_2 still use Terminal T1 memorized
         *   => setV(VL_1)=456 => getV(VL_2)=456 (Bug)
         */
        //        System.out.println("bus1 VL_1 connected equipement ? " + vl.getBusBreakerView().getBus("VL_1").getConnectedTerminalCount());
        //        System.out.println("bus2 VL_2 connected equipement ? " + vl.getBusBreakerView().getBus("VL_2").getConnectedTerminalCount());
        vl.getBusBreakerView().getBus("VL_1").setV(123.0);
        assertEquals(123.0, vl.getBusBreakerView().getBus("VL_1").getV(), 0.0);
        assertEquals(123.0, vl.getBusBreakerView().getBus("VL_2").getV(), 0.0);

        vl.getNodeBreakerView().getSwitch("BR1").setOpen(true);

        vl.getBusBreakerView().getBus("VL_1").setV(456.0);
        assertEquals(456.0, vl.getBusBreakerView().getBus("VL_1").getV(), 0.0);
        assertEquals(NaN, vl.getBusBreakerView().getBus("VL_2").getV(), 0.0);
    }

}
