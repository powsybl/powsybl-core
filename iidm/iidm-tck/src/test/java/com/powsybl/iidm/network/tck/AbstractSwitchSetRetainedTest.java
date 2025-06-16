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
}
