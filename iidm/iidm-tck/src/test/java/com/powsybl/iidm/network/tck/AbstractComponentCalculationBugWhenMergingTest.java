/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractComponentCalculationBugWhenMergingTest {

    @Test
    public void shouldNotThrowShouldNotHappen() {
        Network n1 = Network.create("n1", "test");
        Substation s1 = n1.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newDanglingLine()
                .setId("dl1")
                .setConnectableBus("b1")
                .setBus("b1")
                .setP0(0)
                .setQ0(0)
                .setR(1)
                .setX(1)
                .setG(0)
                .setB(0)
                .setPairingKey("XNODE")
                .add();
        Network n2 = Network.create("n2", "test");
        Substation s2 = n2.newSubstation()
                .setId("s2")
                .setCountry(Country.BE)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newDanglingLine()
                .setId("dl2")
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0)
                .setQ0(0)
                .setR(1)
                .setX(1)
                .setG(0)
                .setB(0)
                .setPairingKey("XNODE")
                .add();
        // to reproduce the issue, force connected components on one of the network (n1)
        // and not the other one
        for (Bus b : n1.getBusView().getBuses()) {
            b.getConnectedComponent();
        }
        assertEquals(0, n1.getTieLineCount());
        Network merged = Network.merge(n1, n2);
        for (Bus b : merged.getBusView().getBuses()) {
            assertDoesNotThrow(b::getConnectedComponent);
            assertEquals(0, b.getConnectedComponent().getNum());
            assertEquals(2, b.getConnectedComponent().getSize());
            assertEquals(0, b.getSynchronousComponent().getNum());
            assertEquals(2, b.getConnectedComponent().getSize());
        }
        assertEquals(1, merged.getTieLineCount());
        merged.getTieLine("dl1 + dl2").remove();
        assertEquals(0, merged.getTieLineCount());
        for (Bus b : merged.getBusView().getBuses()) {
            assertEquals(1, b.getConnectedComponent().getSize());
            assertEquals(1, b.getConnectedComponent().getSize());
        }
        for (Bus b : n2.getBusView().getBuses()) {
            assertEquals(1, b.getConnectedComponent().getSize());
            assertEquals(1, b.getConnectedComponent().getSize());
        }
    }
}
