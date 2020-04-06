/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
        assertEquals(132.0d, twt.getRatedU0(), 0.0d);

        // Leg1
        final ThreeWindingsTransformer.Leg leg1 = twt.getLeg1();
        assertNotNull(leg1);
        assertTrue(leg1 instanceof AbstractAdapter);
        assertNotNull(leg1.setR(2.0));
        assertEquals(2.0, leg1.getR(), 0.0);
        assertNotNull(leg1.setX(2.1));
        assertEquals(2.1, leg1.getX(), 0.0);
        assertNotNull(leg1.setG(2.2));
        assertEquals(2.2, leg1.getG(), 0.0);
        assertNotNull(leg1.setB(2.3));
        assertEquals(2.3, leg1.getB(), 0.0);
        assertNotNull(leg1.setRatedU(1.1));
        assertEquals(1.1, leg1.getRatedU(), 0.0);
        assertSame(twt.getTerminal(ThreeWindingsTransformer.Side.ONE), leg1.getTerminal());
        assertNotNull(leg1.setRatedS(1.0));
        assertEquals(1.0, leg1.getRatedS(), 0.0);

        final CurrentLimits currentLimitsInLeg1 = leg1.newCurrentLimits()
                .setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        assertSame(currentLimitsInLeg1, leg1.getCurrentLimits());
        // --> RatioTapChanger
        final RatioTapChanger ratioTapChangerInLeg1 = leg1.newRatioTapChanger()
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
        assertTrue(ratioTapChangerInLeg1 instanceof RatioTapChangerAdapter);
        assertSame(ratioTapChangerInLeg1, leg1.getRatioTapChanger());

        // Leg 2
        final ThreeWindingsTransformer.Leg leg2 = twt.getLeg2();
        assertNotNull(leg2);
        assertTrue(leg2 instanceof AbstractAdapter);
        // --> PhaseTapChanger
        PhaseTapChanger ptc = leg2.newPhaseTapChanger()
                    .setTapPosition(1)
                    .setLowTapPosition(0)
                    .setRegulating(false)
                    .beginStep()
                        .setR(1.0)
                        .setX(2.0)
                        .setG(3.0)
                        .setB(4.0)
                        .setAlpha(5.0)
                        .setRho(6.0)
                    .endStep()
                    .beginStep()
                        .setR(1.0)
                        .setX(2.0)
                        .setG(3.0)
                        .setB(4.0)
                        .setAlpha(5.0)
                        .setRho(6.0)
                    .endStep()
                .add();
        assertSame(ptc, leg2.getPhaseTapChanger());

        // Leg 3
        final ThreeWindingsTransformer.Leg leg3 = twt.getLeg3();
        assertNotNull(leg3);
        assertTrue(leg3 instanceof AbstractAdapter);

        // Topology
        TopologyVisitor visitor = mock(TopologyVisitor.class);
        mergingView.getVoltageLevel("VL_132").visitEquipments(visitor);
        verify(visitor, times(1)).visitThreeWindingsTransformer(any(ThreeWindingsTransformer.class), any(ThreeWindingsTransformer.Side.class));

        // Not implemented yet !
        TestUtil.notImplemented(twt::remove);
    }
}
