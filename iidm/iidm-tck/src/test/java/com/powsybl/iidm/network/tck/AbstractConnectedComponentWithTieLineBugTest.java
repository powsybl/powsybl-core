/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractConnectedComponentWithTieLineBugTest {

    @Test
    public void test() {
        Network n = Network.create("n", "test");
        Substation s1 = n.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b1 = vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newGenerator()
                .setId("g1")
                .setBus("b1")
                .setConnectableBus("b1")
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .setMinP(50.0)
                .setMaxP(150.0)
                .add();
        Substation s2 = n.newSubstation()
                .setId("s2")
                .setCountry(Country.BE)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b2 = vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newLoad()
                .setId("ld1")
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0.0)
                .setQ0(0.0)
                .add();
        DanglingLine dl1 = vl1.newDanglingLine()
                .setBus("b1")
                .setId("l1")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setPairingKey("XNODE")
                .add();
        DanglingLine dl2 = vl2.newDanglingLine()
                .setBus("b2")
                .setId("l2")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setPairingKey("XNODE")
                .add();
        n.newTieLine()
                .setId("l1 + l2")
                .setDanglingLine1(dl1.getId())
                .setDanglingLine2(dl2.getId())
                .add();
        assertEquals(0, b1.getConnectedComponent().getNum());
        assertEquals(0, b2.getConnectedComponent().getNum());
    }
}
