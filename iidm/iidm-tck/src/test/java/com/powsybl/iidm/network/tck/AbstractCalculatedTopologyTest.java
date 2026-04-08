/**
 * Copyright (c) 2016-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractCalculatedTopologyTest {

    @Test
    public void testBusBreaker() {
        Network n = createBusBreaker();
        Bus bus = checkSameMergedBus(n, "B1a", "B1b");
        checkSameBusesFromMergedBus(n, bus.getId(), "B1a", "B1b");
    }

    @Test
    public void testNodeBreaker() {
        Network n = createNodeBreaker();
        Bus bus = checkSameMergedBus(n, "B1a", "B1b");
        String bus1a = n.getBusbarSection("B1a").getTerminal().getBusBreakerView().getBus().getId();
        String bus1b = n.getBusbarSection("B1b").getTerminal().getBusBreakerView().getBus().getId();
        checkSameBusesFromMergedBus(n, bus.getId(), bus1a, bus1b);
    }

    private Bus checkSameMergedBus(Network n, String ida, String idb) {
        Bus ma = n.getVoltageLevel("VL1").getBusView().getMergedBus(ida);
        Bus mb = n.getVoltageLevel("VL1").getBusView().getMergedBus(idb);
        assertNotNull(ma);
        assertNotNull(mb);
        assertEquals(ma, mb);
        assertSame(n, ma.getNetwork());
        return ma;
    }

    private void checkSameBusesFromMergedBus(Network n, String mergedBusId, String... busesIds) {
        Collection<Bus> buses = n.getVoltageLevel("VL1").getBusBreakerView().getBusesFromBusViewBusId(mergedBusId);
        assertFalse(buses.isEmpty());
        assertEquals(2, buses.size());
        for (String busId : busesIds) {
            assertTrue(buses.stream().anyMatch(b -> b.getId().equals(busId)));
        }
        Collection<Bus> buses2 = n.getVoltageLevel("VL1").getBusBreakerView().getBusStreamFromBusViewBusId(mergedBusId).collect(Collectors.toSet());
        assertTrue(buses.containsAll(buses2));
        assertTrue(buses2.containsAll(buses));
    }

    private Network createBusBreaker() {
        // For the buses to be valid they have to be connected to at least one branch
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.ES)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
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
        vl1.getBusBreakerView().newBus()
                .setId("B1a")
                .add();
        vl1.newLoad()
                .setId("L1")
                .setBus("B1a")
                .setP0(1)
                .setQ0(0)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1b")
                .add();
        vl1.newGenerator()
                .setId("G1")
                .setBus("B1b")
                .setMinP(0)
                .setMaxP(1)
                .setTargetP(1)
                .setTargetQ(0)
                .setVoltageRegulatorOn(false)
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("SW")
                .setOpen(false)
                .setBus1("B1a")
                .setBus2("B1b")
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        vl2.newLoad()
                .setId("L2")
                .setBus("B2")
                .setP0(1)
                .setQ0(0)
                .add();
        network.newLine()
                .setId("Line")
                .setVoltageLevel1("VL1")
                .setVoltageLevel2("VL2")
                .setBus1("B1a")
                .setBus2("B2")
                .setR(1)
                .setX(1)
                .setG1(0)
                .setB1(0)
                .setG2(0)
                .setB2(0)
                .add();
        return network;
    }

    private Network createNodeBreaker() {
        // For the buses to be valid they have to be connected to at least one branch
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.ES)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
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
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("B1a")
                .setNode(0)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("B1b")
                .setNode(1)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("B1a-B1b")
                .setOpen(false)
                .setNode1(0)
                .setNode2(1)
                .setRetained(true)
                .add();
        vl1.newLoad()
                .setId("L1")
                .setNode(2)
                .setP0(1)
                .setQ0(0)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("L1-B1a")
                .setOpen(false)
                .setNode1(2)
                .setNode2(0)
                .add();
        vl1.newGenerator()
                .setId("G1")
                .setNode(3)
                .setMinP(0)
                .setMaxP(1)
                .setTargetP(1)
                .setTargetQ(0)
                .setVoltageRegulatorOn(false)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("G1-B1b")
                .setOpen(false)
                .setNode1(3)
                .setNode2(1)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        vl2.newLoad()
                .setId("L2")
                .setBus("B2")
                .setP0(1)
                .setQ0(0)
                .add();
        network.newLine()
                .setId("Line")
                .setVoltageLevel1("VL1")
                .setVoltageLevel2("VL2")
                .setNode1(4)
                .setBus2("B2")
                .setR(1)
                .setX(1)
                .setG1(0)
                .setB1(0)
                .setG2(0)
                .setB2(0)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("Line-B1a")
                .setOpen(false)
                .setNode1(4)
                .setNode2(0)
                .add();
        return network;
    }
}
