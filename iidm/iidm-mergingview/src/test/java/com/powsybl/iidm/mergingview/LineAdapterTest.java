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
    private Network networkRef;

    @Before
    public void setUp() {
        mergingView = MergingView.create("LineAdapterTest", "iidm");
        networkRef = BatteryNetworkFactory.create();
        mergingView.merge(networkRef);
    }

    @Test
    public void testSetterGetter() {
        final Line lineRef = networkRef.getLine("NHV1_NHV2_1");
        final Line lineAdapted = mergingView.getLine(lineRef.getId());
        // setter / getter
        assertTrue(lineAdapted instanceof LineAdapter);
        assertSame(lineAdapted, mergingView.getBranch(lineRef.getId()));
        assertEquals(lineRef.getId(), lineAdapted.getId());
        assertEquals(lineRef.getName(), lineAdapted.getName());
        assertEquals(lineRef.isTieLine(), lineAdapted.isTieLine());
        assertTrue(lineAdapted.getTerminal1() instanceof TerminalAdapter);
        assertTrue(lineAdapted.getTerminal2() instanceof TerminalAdapter);
        assertSame(lineAdapted.getTerminal1(), lineAdapted.getTerminal(Branch.Side.ONE));
        assertSame(lineAdapted.getTerminal1(), lineAdapted.getTerminal("VLGEN"));
        assertEquals(lineRef.getSide(lineRef.getTerminal1()), lineAdapted.getSide(lineAdapted.getTerminal1()));

        double r = lineAdapted.getR();
        double x = lineAdapted.getX();
        double g1 = lineAdapted.getG1();
        double g2 = lineAdapted.getG2();
        double b1 = lineAdapted.getB1();
        double b2 = lineAdapted.getB2();
        assertEquals(r, lineAdapted.getR(), 0.0);
        lineAdapted.setR(++r);
        assertEquals(r, lineAdapted.getR(), 0.0);
        assertEquals(x, lineAdapted.getX(), 0.0);
        lineAdapted.setX(++x);
        assertEquals(x, lineAdapted.getX(), 0.0);
        assertEquals(g1, lineAdapted.getG1(), 0.0);
        lineAdapted.setG1(++g1);
        assertEquals(g1, lineAdapted.getG1(), 0.0);
        assertEquals(g2, lineAdapted.getG2(), 0.0);
        lineAdapted.setG2(++g2);
        assertEquals(g2, lineAdapted.getG2(), 0.0);
        assertEquals(b1, lineAdapted.getB1(), 0.0);
        lineAdapted.setB1(++b1);
        assertEquals(b1, lineAdapted.getB1(), 0.0);
        assertEquals(b2, lineAdapted.getB2(), 0.0);
        lineAdapted.setB2(++b2);
        assertEquals(b2, lineAdapted.getB2(), 0.0);
        assertSame(lineRef.getCurrentLimits1(), lineAdapted.getCurrentLimits1());
        assertSame(lineRef.getCurrentLimits2(), lineAdapted.getCurrentLimits2());
        assertSame(lineRef.getCurrentLimits(Branch.Side.ONE), lineAdapted.getCurrentLimits(Branch.Side.ONE));
        assertSame(lineRef.getCurrentLimits(Branch.Side.TWO), lineAdapted.getCurrentLimits(Branch.Side.TWO));

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
    }
}
