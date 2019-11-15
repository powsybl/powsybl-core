/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLineAdapterTest {

    private MergingView network;

    @Before
    public void setUp() {
        network = MergingView.create("DanglingLineAdapterTest", "iidm");
        network.merge(createNetwork());
    }

    private static Network createNetwork() {
        final Network n1 = Network.create("n1", "test");
        final Substation s1 = n1.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        final VoltageLevel vl1 = s1.newVoltageLevel()
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
                .setUcteXnodeCode("XNODE")
                .add();
        return n1;
    }

    @Test
    public void testSetterGetter() {
        final DanglingLine danglingLine = network.getDanglingLine("dl1");

        // Not implemented yet !
        TestUtil.notImplemented(danglingLine::getP0);
        TestUtil.notImplemented(() -> danglingLine.setP0(0.0d));
        TestUtil.notImplemented(danglingLine::getQ0);
        TestUtil.notImplemented(() -> danglingLine.setQ0(0.0d));
        TestUtil.notImplemented(danglingLine::getR);
        TestUtil.notImplemented(() -> danglingLine.setR(0.0d));
        TestUtil.notImplemented(danglingLine::getX);
        TestUtil.notImplemented(() -> danglingLine.setX(0.0d));
        TestUtil.notImplemented(danglingLine::getG);
        TestUtil.notImplemented(() -> danglingLine.setG(0.0d));
        TestUtil.notImplemented(danglingLine::getB);
        TestUtil.notImplemented(() -> danglingLine.setB(0.0d));
        TestUtil.notImplemented(danglingLine::getUcteXnodeCode);
        TestUtil.notImplemented(danglingLine::getCurrentLimits);
        TestUtil.notImplemented(danglingLine::newCurrentLimits);
        TestUtil.notImplemented(danglingLine::getTerminal);
        TestUtil.notImplemented(danglingLine::getType);
        TestUtil.notImplemented(danglingLine::getTerminals);
        TestUtil.notImplemented(danglingLine::remove);
    }
}
