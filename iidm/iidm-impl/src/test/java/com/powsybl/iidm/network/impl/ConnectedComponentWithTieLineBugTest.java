/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ConnectedComponentWithTieLineBugTest {

    @Test
    public void test() {
        Network n = NetworkFactory.create("n", "test");
        Substation s1 = n.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b1 = vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newGenerator()
                .setId("g1")
                .setBus("b1")
                .setConnectableBus("b1")
                .setTargetP(100f)
                .setTargetV(400f)
                .setVoltageRegulatorOn(true)
                .setMinP(50f)
                .setMaxP(150f)
                .add();
        Substation s2 = n.newSubstation()
                .setId("s2")
                .setCountry(Country.BE)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(380f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b2 = vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newLoad()
                .setId("ld1")
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0)
                .setQ0(0)
                .add();
        n.newTieLine()
                .setId("l1 + l2")
                .setVoltageLevel1("vl1")
                .setConnectableBus1("b1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setConnectableBus2("b2")
                .setBus2("b2")
                .line1()
                .setId("l1")
                .setR(1f)
                .setX(1f)
                .setG1(0f)
                .setG2(0f)
                .setB1(0f)
                .setB2(0f)
                .setXnodeP(0)
                .setXnodeQ(0)
                .line2()
                .setId("l2")
                .setR(1f)
                .setX(1f)
                .setG1(0f)
                .setG2(0f)
                .setB1(0f)
                .setB2(0f)
                .setXnodeP(0)
                .setXnodeQ(0)
                .setUcteXnodeCode("XNODE")
                .add();
        Assert.assertTrue(b1.getConnectedComponent().getNum() == 0);
        Assert.assertTrue(b2.getConnectedComponent().getNum() == 0);
    }
}
