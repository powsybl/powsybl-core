/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo@rte-france.com>}
 */
class InvalidatedViewTest {

    private static Network createNodeBreakerNetwork() {
        Network network = Network.create("testNB", "test");

        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(1.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView()
                .newBusbarSection()
                .setId("BBS11")
                .setNode(0)
                .add();
        vl.newLoad()
                .setId("L1")
                .setNode(2)
                .setP0(1)
                .setQ0(1)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B_L1")
                .setNode1(2)
                .setNode2(1)
                .setOpen(false)
                .add();
        vl.getNodeBreakerView().newDisconnector()
                .setId("D_L1")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .add();

        return network;
    }

    private static Network createBusBreakerNetwork() {
        Network network = Network.create("testBB", "test");

        Substation s = network.newSubstation()
                .setId("S1")
                .add();
        VoltageLevel vl1 = s.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("BR1")
                .setBus1("B1")
                .setBus2("B2")
                .setOpen(false)
                .add();
        vl1.newGenerator()
                .setId("G")
                .setBus("B1")
                .setMaxP(100.0)
                .setMinP(50.0)
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .add();

        return network;
    }

    @Test
    void testFictitiousP0AndFictitiousQ0ForInvalidatedBusInNodeBreaker() {
        Network network = createNodeBreakerNetwork();
        Bus bus = network.getVoltageLevel("VL1").getBusView().getBus("VL1_0");
        network.getSwitch("B_L1").setOpen(true);
        assertThrows(PowsyblException.class, bus::getFictitiousP0, "Bus has been invalidated");
        assertThrows(PowsyblException.class, bus::getFictitiousQ0, "Bus has been invalidated");
    }

    @Test
    void testFictitiousP0AndFictitiousQ0ForInvalidatedBusInBusBreaker() {
        Network network = createBusBreakerNetwork();
        Bus bus = network.getVoltageLevel("VL1").getBusView().getBus("VL1_0");
        network.getSwitch("BR1").setOpen(true);
        assertThrows(PowsyblException.class, bus::getFictitiousP0, "Bus has been invalidated");
        assertThrows(PowsyblException.class, bus::getFictitiousQ0, "Bus has been invalidated");
    }
}
