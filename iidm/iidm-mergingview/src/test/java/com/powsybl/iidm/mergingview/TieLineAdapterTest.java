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

import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapterTest {

    private MergingView network;

    @Before
    public void setUp() {
        network = MergingView.create("TieLineAdapterTest", "iidm");
        network.merge(createNetwork());
    }

    @Test
    public void testSetterGetter() {
        final TieLine tieLine = (TieLineAdapter) network.getLine("l1 + l2");

        assertTrue(tieLine.isTieLine());

        // Not implemented yet !
        TestUtil.notImplemented(tieLine::getTerminal1);
        TestUtil.notImplemented(tieLine::getTerminal2);
        TestUtil.notImplemented(() -> tieLine.getTerminal((Branch.Side) null));
        TestUtil.notImplemented(() -> tieLine.getTerminal(""));
        TestUtil.notImplemented(() -> tieLine.getSide((Terminal) null));
        TestUtil.notImplemented(() -> tieLine.getCurrentLimits((Branch.Side) null));
        TestUtil.notImplemented(tieLine::getCurrentLimits1);
        TestUtil.notImplemented(tieLine::newCurrentLimits1);
        TestUtil.notImplemented(tieLine::getCurrentLimits2);
        TestUtil.notImplemented(tieLine::newCurrentLimits2);
        TestUtil.notImplemented(tieLine::isOverloaded);
        TestUtil.notImplemented(() -> tieLine.isOverloaded(0.0f));
        TestUtil.notImplemented(tieLine::getOverloadDuration);
        TestUtil.notImplemented(() -> tieLine.checkPermanentLimit((Branch.Side) null, 0.0f));
        TestUtil.notImplemented(() -> tieLine.checkPermanentLimit((Branch.Side) null));
        TestUtil.notImplemented(() -> tieLine.checkPermanentLimit1(0.0f));
        TestUtil.notImplemented(tieLine::checkPermanentLimit1);
        TestUtil.notImplemented(() -> tieLine.checkPermanentLimit2(0.0f));
        TestUtil.notImplemented(tieLine::checkPermanentLimit2);
        TestUtil.notImplemented(() -> tieLine.checkTemporaryLimits((Branch.Side) null, 0.0f));
        TestUtil.notImplemented(() -> tieLine.checkTemporaryLimits((Branch.Side) null));
        TestUtil.notImplemented(() -> tieLine.checkTemporaryLimits1(0.0f));
        TestUtil.notImplemented(tieLine::checkTemporaryLimits1);
        TestUtil.notImplemented(() -> tieLine.checkTemporaryLimits2(0.0f));
        TestUtil.notImplemented(tieLine::checkTemporaryLimits2);
        TestUtil.notImplemented(tieLine::getType);
        TestUtil.notImplemented(tieLine::getTerminals);
        TestUtil.notImplemented(tieLine::remove);
        TestUtil.notImplemented(tieLine::getR);
        TestUtil.notImplemented(() -> tieLine.setR(0.0d));
        TestUtil.notImplemented(tieLine::getX);
        TestUtil.notImplemented(() -> tieLine.setX(0.0d));
        TestUtil.notImplemented(tieLine::getG1);
        TestUtil.notImplemented(() -> tieLine.setG1(0.0d));
        TestUtil.notImplemented(tieLine::getG2);
        TestUtil.notImplemented(() -> tieLine.setG2(0.0d));
        TestUtil.notImplemented(tieLine::getB1);
        TestUtil.notImplemented(() -> tieLine.setB1(0.0d));
        TestUtil.notImplemented(tieLine::getB2);
        TestUtil.notImplemented(() -> tieLine.setB2(0.0d));
        TestUtil.notImplemented(tieLine::getUcteXnodeCode);
        TestUtil.notImplemented(tieLine::getHalf1);
        TestUtil.notImplemented(tieLine::getHalf2);
    }

    private Network createNetwork() {
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
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .setXnodeP(0.0)
                .setXnodeQ(0.0)
                .line2()
                .setId("l2")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .setXnodeP(0.0)
                .setXnodeQ(0.0)
                .setUcteXnodeCode("XNODE")
                .add();
        return n;
    }
}
