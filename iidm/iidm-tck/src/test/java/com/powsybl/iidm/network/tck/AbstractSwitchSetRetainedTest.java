/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractSwitchSetRetainedTest {

    /**
     *             BBS1
     *   --------- (0) ---------
     *              |
     *             [ ] B1, retained, closed
     *              |
     *             (1)
     */
    static Network createMinimalNetworkWithRetainedSwitch() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("VL")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS1")
            .setNode(0)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B1")
            .setNode1(0)
            .setNode2(1)
            .setOpen(false)
            .setRetained(true)
            .add();
        return network;
    }

    /**
     *                                         BBS2
     *                               --------- (3) ---------
     *             BBS1                         |
     *   --------- (0) ---------               [ ] B3, retained, closed
     *              |                           |
     *              |                           | (2)
     *              |                           |
     *             [ ] B1, retained, open      [ ] B2, retained, closed
     *              |                           |
     *              ------------ (1) ------------
     */
    static Network createNetworkWithForkAndRetainedSwitches() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("VL")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS1")
            .setNode(0)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B1")
            .setNode1(0)
            .setNode2(1)
            .setOpen(true)
            .setRetained(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B2")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .setRetained(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B3")
            .setNode1(2)
            .setNode2(3)
            .setOpen(false)
            .setRetained(true)
            .add();
        vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS2")
            .setNode(3)
            .add();
        return network;
    }

    private Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(1)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B1")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .setRetained(true)
                .add();
        return network;
    }

    /**
     *             BBS1
     *   --------- (0) ------------------------------
     *              |                               |
     *             [ ] B1, retained, closed         [ ] B2, retained, open
     *              |                               |
     *             (1)                              (2)
     */
    private Network createNetworkWithRetainedSwitches() {
        Network network = createMinimalNetworkWithRetainedSwitch();
        VoltageLevel vl = network.getVoltageLevel("VL");
        vl.getNodeBreakerView().newBreaker()
            .setId("B2")
            .setNode1(0)
            .setNode2(2)
            .setOpen(true)
            .setRetained(true)
            .add();
        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        VoltageLevel vl = network.getVoltageLevel("VL");
        assertNotNull(vl);

        Switch b1 = network.getSwitch("B1");
        assertNotNull(b1);

        assertTrue(b1.isRetained());
        assertEquals(2, Iterables.size(vl.getBusBreakerView().getBuses()));

        b1.setRetained(false);
        assertFalse(b1.isRetained());
        assertEquals(1, Iterables.size(vl.getBusBreakerView().getBuses()));
    }

    @Test
    public void testCloseRetainedSwitch() {
        Network network = createNetwork();
        VoltageLevel vl = network.getVoltageLevel("VL");
        assertNotNull(vl);

        Switch b1 = network.getSwitch("B1");
        assertNotNull(b1);
        assertTrue(b1.isRetained());
        assertFalse(b1.isOpen());

        List<Bus> buses0 = vl.getBusBreakerView().getBusStream().toList();

        b1.setOpen(true);

        List<Bus> buses1 = vl.getBusBreakerView().getBusStream().toList();
        assertEquals(buses0, buses1);

        b1.setOpen(false);

        List<Bus> buses2 = vl.getBusBreakerView().getBusStream().toList();
        assertEquals(buses0, buses2);
    }

    @Test
    void testRetainedSwitchModification() {
        Network network = createMinimalNetworkWithRetainedSwitch();
        VoltageLevel vl = network.getVoltageLevel("VL");
        Bus bus0 = vl.getBusBreakerView().getBus("VL_0");
        Bus bus1 = vl.getBusBreakerView().getBus("VL_1");

        bus0.setV(123.0);
        assertEquals(123.0, bus0.getV(), 0.0);
        assertEquals(123.0, bus1.getV(), 0.0);

        vl.getNodeBreakerView().getSwitch("B1").setOpen(true);

        bus0.setV(456.0);
        assertEquals(456.0, bus0.getV(), 0.0);
        assertEquals(Double.NaN, bus1.getV(), 0.0);

        vl.getNodeBreakerView().getSwitch("B1").setOpen(false);

        bus0.setV(789.0);
        assertEquals(789.0, bus0.getV(), 0.0);
        assertEquals(789.0, bus1.getV(), 0.0);
    }

    @Test
    void testRetainedSwitchModificationOnMoreComplexNetwork() {
        Network network = createNetworkWithRetainedSwitches();
        VoltageLevel vl = network.getVoltageLevel("VL");
        Bus bus0 = vl.getBusBreakerView().getBus("VL_0");
        Bus bus1 = vl.getBusBreakerView().getBus("VL_1");
        Bus bus2 = vl.getBusBreakerView().getBus("VL_2");

        bus0.setV(123.0);
        assertEquals(123.0, bus0.getV(), 0.0);
        assertEquals(123.0, bus1.getV(), 0.0);
        assertEquals(Double.NaN, bus2.getV(), 0.0);

        vl.getNodeBreakerView().getSwitch("B1").setOpen(true);

        bus0.setV(456.0);
        assertEquals(456.0, bus0.getV(), 0.0);
        assertEquals(Double.NaN, bus1.getV(), 0.0);
        assertEquals(Double.NaN, bus2.getV(), 0.0);

        vl.getNodeBreakerView().getSwitch("B2").setOpen(false);

        bus0.setV(789.0);
        assertEquals(789.0, bus0.getV(), 0.0);
        assertEquals(Double.NaN, bus1.getV(), 0.0);
        assertEquals(789.0, bus2.getV(), 0.0);
    }

    @Test
    void testRetainedSwitchModifiedOnForkedNetwork() {
        Network network = createNetworkWithForkAndRetainedSwitches();
        VoltageLevel vl = network.getVoltageLevel("VL");
        Bus bus0 = vl.getBusBreakerView().getBus("VL_0");
        Bus bus1 = vl.getBusBreakerView().getBus("VL_1");
        Bus bus3 = vl.getBusBreakerView().getBus("VL_3");

        bus3.setV(123.0);
        assertEquals(123.0, bus3.getV(), 0.0);
        assertEquals(123.0, bus1.getV(), 0.0);
        assertEquals(Double.NaN, bus0.getV(), 0.0);

        vl.getNodeBreakerView().getSwitch("B1").setOpen(false);

        assertEquals(123.0, bus3.getV(), 0.0);
        assertEquals(123.0, bus1.getV(), 0.0);
        assertEquals(123.0, bus0.getV(), 0.0);
    }

    @Test
    void testRetainedSwitchModifiedOnForkedNetwork2() {
        Network network = createNetworkWithForkAndRetainedSwitches();
        VoltageLevel vl = network.getVoltageLevel("VL");
        Bus bus0 = vl.getBusBreakerView().getBus("VL_0");
        Bus bus1 = vl.getBusBreakerView().getBus("VL_1");
        Bus bus3 = vl.getBusBreakerView().getBus("VL_3");

        bus3.setV(123.0);
        bus0.setV(456.0);
        assertEquals(123.0, bus3.getV(), 0.0);
        assertEquals(123.0, bus1.getV(), 0.0);
        assertEquals(456.0, bus0.getV(), 0.0);

        vl.getNodeBreakerView().getSwitch("B1").setOpen(false);

        assertEquals(123.0, bus3.getV(), 0.0);
        assertEquals(123.0, bus1.getV(), 0.0);
        assertEquals(456.0, bus0.getV(), 0.0);
    }

    @Test
    void testNodeReuseAfterSwitchRemoval() {
        Network network = createMinimalNetworkWithRetainedSwitch();
        VoltageLevel vl = network.getVoltageLevel("VL");
        vl.getBusBreakerView().getBus("VL_1").setV(400.0);

        // node 1 is removed with the switch, then reused by a new load
        vl.getNodeBreakerView().removeSwitch("B1");
        Load load = vl.newLoad()
            .setId("L")
            .setNode(1)
            .setP0(10.0)
            .setQ0(0.0)
            .add();

        assertEquals(400.0, vl.getBusBreakerView().getBus("VL_0").getV(), 0.0);
        assertTrue(Double.isNaN(load.getTerminal().getBusBreakerView().getBus().getV()));
        assertTrue(Double.isNaN(load.getTerminal().getBusBreakerView().getBus().getAngle()));
    }

    @Test
    void testConnectedComponentOfBusWithoutEquipment() {
        Network network = createMinimalNetworkWithRetainedSwitch();
        VoltageLevel vl = network.getVoltageLevel("VL");
        vl.getNodeBreakerView().getSwitch("B1").setOpen(true);
        Bus bus1 = vl.getBusBreakerView().getBus("VL_1");
        assertNull(bus1.getConnectedComponent());
        assertNull(bus1.getSynchronousComponent());
        assertTrue(Double.isNaN(bus1.getV()));
    }
}
