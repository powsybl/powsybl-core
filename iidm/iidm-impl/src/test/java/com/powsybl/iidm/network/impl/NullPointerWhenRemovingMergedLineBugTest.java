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
public class NullPointerWhenRemovingMergedLineBugTest {

    @Test
    public void test() {
        Network n1 = NetworkFactory.create("n1", "test");
        Substation s1 = n1.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newDanglingLine()
                .setId("dl1")
                .setConnectableBus("b1")
                .setBus("b1")
                .setP0(0f)
                .setQ0(0f)
                .setR(1f)
                .setX(1f)
                .setG(0f)
                .setB(0f)
                .setUcteXnodeCode("XNODE")
                .add();
        Network n2 = NetworkFactory.create("n2", "test");
        Substation s2 = n2.newSubstation()
                .setId("s2")
                .setCountry(Country.BE)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(380f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newDanglingLine()
                .setId("dl2")
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0f)
                .setQ0(0f)
                .setR(1f)
                .setX(1f)
                .setG(0f)
                .setB(0f)
                .setUcteXnodeCode("XNODE")
                .add();
        Assert.assertTrue(n1.getLineCount() == 0);
        Assert.assertTrue(n1.getDanglingLineCount() == 1);
        Assert.assertTrue(n2.getLineCount() == 0);
        Assert.assertTrue(n2.getDanglingLineCount() == 1);
        n1.merge(n2);
        Assert.assertTrue(n1.getLineCount() == 1);
        Assert.assertTrue(n1.getDanglingLineCount() == 0);
        n1.getLine("dl1 + dl2").remove();
        for (Bus b : n1.getBusBreakerView().getBuses()) {
            // throws an exception if bug already present
            b.isInMainConnectedComponent();
        }
    }
}
