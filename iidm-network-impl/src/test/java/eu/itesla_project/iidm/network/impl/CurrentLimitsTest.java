/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsTest {

    private Network createNetwork() {
        Network network = NetworkFactory.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        Line l = network.newLine()
                .setId("L")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(1)
                .setX(1)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        l.newCurrentLimits1()
                .setPermanentLimit(1000)
                .beginTemporaryLimit()
                    .setAcceptableDuration(20 * 60)
                    .setLimit(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setAcceptableDuration(5 * 60)
                    .setLimit(1400)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setAcceptableDuration(60)
                    .setLimit(1600)
                .endTemporaryLimit()
            .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        Line l = network.getLine("L");
        assertFalse(l.isOverloaded());
        l.getTerminal1().getBusBreakerView().getBus().setV(390);
        l.getTerminal1().setP(100).setQ(50);
        assertTrue(!Float.isNaN(l.getTerminal1().getI()));
        assertFalse(l.isOverloaded());
        l.getTerminal1().setP(800).setQ(400);
        assertTrue(l.isOverloaded());
        assertTrue(l.getOverloadDuration() == 5 * 60);
        l.getTerminal1().setP(900).setQ(500);
        assertTrue(l.getOverloadDuration() == 60);
    }
}
