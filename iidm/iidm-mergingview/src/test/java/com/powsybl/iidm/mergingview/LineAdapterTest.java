/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LineAdapterTest {

    private MergingView mergingView;

    @Before
    public void setUp() {
        mergingView = MergingView.create("LineAdapterTest", "iidm");
    }

    @Test
    public void testSetterGetter() {
        Network networkRef = BatteryNetworkFactory.create();
        mergingView.merge(networkRef);

        // setter / getter
        final Line lineRef = networkRef.getLine("NHV1_NHV2_1");
        final Line lineAdapted = mergingView.getLine(lineRef.getId());
        assertTrue(lineAdapted instanceof AbstractAdapter<?>);
        assertSame(lineAdapted, mergingView.getBranch(lineRef.getId()));
        assertEquals(lineRef.getId(), lineAdapted.getId());
        assertEquals(lineRef.getName(), lineAdapted.getName());
        assertEquals(lineRef.isTieLine(), lineAdapted.isTieLine());
        assertTrue(lineAdapted.getTerminal1() instanceof AbstractAdapter<?>);
        assertTrue(lineAdapted.getTerminal2() instanceof AbstractAdapter<?>);
        assertSame(lineAdapted.getTerminal1(), lineAdapted.getTerminal(Branch.Side.ONE));
        assertSame(lineAdapted.getTerminal1(), lineAdapted.getTerminal("VLGEN"));
        assertEquals(lineRef.getSide(lineRef.getTerminal1()), lineAdapted.getSide(lineAdapted.getTerminal1()));
        final CurrentLimits cL1 = lineAdapted.newCurrentLimits1().setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400)
                .endTemporaryLimit()
                .add();
        assertSame(cL1, lineAdapted.getCurrentLimits1());
        assertSame(cL1, lineAdapted.getCurrentLimits(Branch.Side.ONE));
        final CurrentLimits cL2 = lineAdapted.newCurrentLimits2().setPermanentLimit(50)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        assertSame(cL2, lineAdapted.getCurrentLimits2());
        assertSame(cL2, lineAdapted.getCurrentLimits(Branch.Side.TWO));
        assertEquals(lineRef.isOverloaded(), lineAdapted.isOverloaded());
        assertEquals(lineRef.isOverloaded(0.0f), lineAdapted.isOverloaded(0.0f));
        assertEquals(lineRef.getOverloadDuration(), lineAdapted.getOverloadDuration());
        assertEquals(lineRef.checkPermanentLimit(Branch.Side.ONE, 0.0f), lineAdapted.checkPermanentLimit(Branch.Side.ONE, 0.0f));
        assertEquals(lineRef.checkPermanentLimit(Branch.Side.TWO), lineAdapted.checkPermanentLimit(Branch.Side.TWO));
        assertEquals(lineRef.checkPermanentLimit1(0.0f), lineAdapted.checkPermanentLimit1(0.0f));
        assertEquals(lineRef.checkPermanentLimit1(), lineAdapted.checkPermanentLimit1());
        assertEquals(lineRef.checkPermanentLimit2(0.0f), lineAdapted.checkPermanentLimit2(0.0f));
        assertEquals(lineRef.checkPermanentLimit2(), lineAdapted.checkPermanentLimit2());
        assertEquals(lineRef.checkTemporaryLimits(Branch.Side.ONE, 0.0f), lineAdapted.checkTemporaryLimits(Branch.Side.ONE, 0.0f));
        assertEquals(lineRef.checkTemporaryLimits(Branch.Side.TWO), lineAdapted.checkTemporaryLimits(Branch.Side.TWO));
        assertEquals(lineRef.checkTemporaryLimits1(0.0f), lineAdapted.checkTemporaryLimits1(0.0f));
        assertEquals(lineRef.checkTemporaryLimits1(), lineAdapted.checkTemporaryLimits1());
        assertEquals(lineRef.checkTemporaryLimits2(0.0f), lineAdapted.checkTemporaryLimits2(0.0f));
        assertEquals(lineRef.checkTemporaryLimits2(), lineAdapted.checkTemporaryLimits2());
        assertEquals(lineRef.getType(), lineAdapted.getType());
        assertEquals(lineRef.getTerminals().size(), lineAdapted.getTerminals().size());

        // Not implemented yet !
        TestUtil.notImplemented(lineAdapted::remove);
        TestUtil.notImplemented(lineAdapted::getR);
        TestUtil.notImplemented(() -> lineAdapted.setR(0.0d));
        TestUtil.notImplemented(lineAdapted::getX);
        TestUtil.notImplemented(() -> lineAdapted.setX(0.0d));
        TestUtil.notImplemented(lineAdapted::getG1);
        TestUtil.notImplemented(() -> lineAdapted.setG1(0.0d));
        TestUtil.notImplemented(lineAdapted::getG2);
        TestUtil.notImplemented(() -> lineAdapted.setG2(0.0d));
        TestUtil.notImplemented(lineAdapted::getB1);
        TestUtil.notImplemented(() -> lineAdapted.setB1(0.0d));
        TestUtil.notImplemented(lineAdapted::getB2);
        TestUtil.notImplemented(() -> lineAdapted.setB2(0.0d));
    }
}
