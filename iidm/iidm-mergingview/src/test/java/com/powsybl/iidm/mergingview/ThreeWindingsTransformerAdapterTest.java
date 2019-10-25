/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("ThreeWindingsTransformerAdapterTest", "iidm");
        mergingView.merge(ThreeWindingsTransformerNetworkFactory.create());
    }

    @Test
    public void testSetterGetter() {
        final ThreeWindingsTransformer twt = mergingView.getThreeWindingsTransformer("3WT");
        assertNotNull(twt);
        assertTrue(twt instanceof ThreeWindingsTransformerAdapter);
        assertSame(mergingView, twt.getNetwork());

        assertNotNull(twt.getSubstation());
        assertEquals(ConnectableType.THREE_WINDINGS_TRANSFORMER, twt.getType());

        assertEquals(twt.getLeg1().getTerminal(), twt.getTerminal(Side.ONE));
        assertEquals(Side.ONE, twt.getSide(twt.getLeg1().getTerminal()));
        assertFalse(twt.getTerminals().isEmpty());

        // Leg1
        final ThreeWindingsTransformer.Leg1 leg1 = twt.getLeg1();
        assertNotNull(leg1);
        assertTrue(leg1 instanceof Leg1Adapter);
        assertTrue(leg1.setR(2.0) instanceof Leg1Adapter);
        assertEquals(2.0, leg1.getR(), 0.0);
        assertTrue(leg1.setX(2.1) instanceof Leg1Adapter);
        assertEquals(2.1, leg1.getX(), 0.0);
        assertTrue(leg1.setG(2.2) instanceof Leg1Adapter);
        assertEquals(2.2, leg1.getG(), 0.0);
        assertTrue(leg1.setB(2.3) instanceof Leg1Adapter);
        assertEquals(2.3, leg1.getB(), 0.0);
        assertTrue(leg1.setRatedU(1.1) instanceof Leg1Adapter);
        assertEquals(1.1, leg1.getRatedU(), 0.0);
        assertSame(twt.getTerminal(ThreeWindingsTransformer.Side.ONE), leg1.getTerminal());

        final CurrentLimits currentLimitsInLeg1 = leg1.newCurrentLimits()
                .setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        assertSame(currentLimitsInLeg1, leg1.getCurrentLimits());

        // Leg 2 or 3
        final ThreeWindingsTransformer.Leg2or3 leg2 = twt.getLeg2();
        final ThreeWindingsTransformer.Leg2or3 leg3 = twt.getLeg3();
        assertNotNull(leg2);
        assertTrue(leg2 instanceof Leg2or3Adapter);
        assertNotNull(leg3);
        assertTrue(leg3 instanceof Leg2or3Adapter);
        assertSame(twt.getTerminal(ThreeWindingsTransformer.Side.TWO), leg2.getTerminal());
        assertSame(twt.getTerminal(ThreeWindingsTransformer.Side.THREE), leg3.getTerminal());
        assertTrue(leg2.setR(1.0) instanceof Leg2or3Adapter);
        assertEquals(1.0, leg2.getR(), 0.0);
        assertTrue(leg2.setX(1.1) instanceof Leg2or3Adapter);
        assertEquals(1.1, leg2.getX(), 0.0);
        assertTrue(leg2.setRatedU(1.2) instanceof Leg2or3Adapter);
        assertEquals(1.2, leg2.getRatedU(), 0.0);
        assertTrue(leg3.setR(1.0) instanceof Leg2or3Adapter);
        assertEquals(1.0, leg3.getR(), 0.0);
        assertTrue(leg3.setX(1.1) instanceof Leg2or3Adapter);
        assertEquals(1.1, leg3.getX(), 0.0);
        assertTrue(leg3.setRatedU(1.2) instanceof Leg2or3Adapter);
        assertEquals(1.2, leg3.getRatedU(), 0.0);

        final RatioTapChanger ratioTapChangerInLeg2 = leg2.newRatioTapChanger()
                .setTargetV(200.0)
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setRegulating(false)
                .setRegulationTerminal(twt.getTerminal(ThreeWindingsTransformer.Side.TWO))
                .beginStep()
                .setR(39.78473)
                .setX(39.784725)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78474)
                .setX(39.784726)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78475)
                .setX(39.784727)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .add();
        assertSame(ratioTapChangerInLeg2, leg2.getRatioTapChanger());
        final CurrentLimits currentLimitsInLeg2 = leg2.newCurrentLimits()
                .setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        assertSame(currentLimitsInLeg2, leg2.getCurrentLimits());

        // Not implemented yet !
        TestUtil.notImplemented(twt::remove);
    }
}
