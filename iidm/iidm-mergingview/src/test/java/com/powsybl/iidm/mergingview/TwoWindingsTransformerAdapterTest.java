/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TwoWindingsTransformerAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("TwoWindingsTransformerAdapterTest", "iidm");
        mergingView.merge(NoEquipmentNetworkFactory.create());
    }

    @Test
    public void testSetterGetter() {
        final Substation substation = mergingView.getSubstation("sub");

        // adder
        final TwoWindingsTransformer twt = substation.newTwoWindingsTransformer()
                    .setId("twt")
                    .setName("twt_name")
                    .setR(1.0)
                    .setX(2.0)
                    .setG(3.0)
                    .setB(4.0)
                    .setRatedU1(5.0)
                    .setRatedU2(6.0)
                    .setRatedS(7.0)
                    .setVoltageLevel1("vl1")
                    .setVoltageLevel2("vl2")
                    .setConnectableBus1("busA")
                    .setConnectableBus2("busB")
                .add();
        assertNotNull(twt);
        assertSame(mergingView.getTwoWindingsTransformer("twt"), mergingView.getBranch("twt"));
        assertTrue(twt instanceof TwoWindingsTransformerAdapter);
        assertSame(mergingView, twt.getNetwork());

        assertEquals(ConnectableType.TWO_WINDINGS_TRANSFORMER, twt.getType());
        assertSame(substation, twt.getSubstation());
        assertEquals(7.0, twt.getRatedS(), 0.0);

        final RatioTapChanger ratioTapChanger = twt.newRatioTapChanger()
                    .setLowTapPosition(0)
                    .setTapPosition(1)
                    .setLoadTapChangingCapabilities(false)
                    .setRegulating(true)
                    .setTargetDeadband(1.0)
                    .setTargetV(220.0)
                    .setRegulationTerminal(twt.getTerminal1())
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
        assertTrue(ratioTapChanger instanceof RatioTapChangerAdapter);
        assertSame(ratioTapChanger, twt.getRatioTapChanger());

        final PhaseTapChanger phaseTapChanger = twt.newPhaseTapChanger()
                    .setTapPosition(1)
                    .setLowTapPosition(0)
                    .setRegulating(false)
                    .setTargetDeadband(1.0)
                    .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                    .setRegulationValue(10.0)
                    .setRegulationTerminal(twt.getTerminal1())
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
        assertTrue(phaseTapChanger instanceof PhaseTapChangerAdapter);
        assertSame(phaseTapChanger, twt.getPhaseTapChanger());

        // setter getter
        final double r = 0.5;
        assertTrue(twt.setR(r) instanceof AbstractAdapter<?>);
        assertEquals(r, twt.getR(), 0.0);
        final double b = 1.0;
        assertTrue(twt.setB(b) instanceof AbstractAdapter<?>);
        assertEquals(b, twt.getB(), 0.0);
        final double g = 2.0;
        assertTrue(twt.setG(g) instanceof AbstractAdapter<?>);
        assertEquals(g, twt.getG(), 0.0);
        final double x = 4.0;
        assertTrue(twt.setX(x) instanceof AbstractAdapter<?>);
        assertEquals(x, twt.getX(), 0.0);
        final double ratedU1 = 8.0;
        assertTrue(twt.setRatedU1(ratedU1) instanceof AbstractAdapter<?>);
        assertEquals(ratedU1, twt.getRatedU1(), 0.0);
        final double ratedU2 = 16.0;
        assertTrue(twt.setRatedU2(ratedU2) instanceof AbstractAdapter<?>);
        assertEquals(ratedU2, twt.getRatedU2(), 0.0);

        assertEquals(2, twt.getTerminals().size());
        assertEquals(twt.getTerminal1(), twt.getTerminal(Side.ONE));
        assertEquals(twt.getTerminal2(), twt.getTerminal(Side.TWO));
        assertEquals(Side.ONE, twt.getSide(twt.getTerminal1()));
        assertEquals(Side.TWO, twt.getSide(twt.getTerminal2()));
        assertEquals(twt.getTerminal1(), twt.getTerminal("vl1"));
        assertEquals(twt.getTerminal2(), twt.getTerminal("vl2"));
        final CurrentLimits currentLimits1 = twt.newCurrentLimits1()
                .setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400)
                .endTemporaryLimit()
                .add();
        final CurrentLimits currentLimits2 = twt.newCurrentLimits2()
                .setPermanentLimit(50)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        assertFalse(currentLimits1 instanceof AbstractAdapter<?>);
        assertFalse(currentLimits2 instanceof AbstractAdapter<?>);
        assertEquals(twt.getCurrentLimits(Side.ONE), twt.getCurrentLimits1());
        assertEquals(twt.getCurrentLimits(Side.TWO), twt.getCurrentLimits2());
        assertFalse(twt.isOverloaded());
        assertFalse(twt.isOverloaded(0.0f));
        assertEquals(Integer.MAX_VALUE, twt.getOverloadDuration());

        assertFalse(twt.checkPermanentLimit(Branch.Side.ONE));
        assertFalse(twt.checkPermanentLimit(Branch.Side.ONE, 0.9f));
        assertFalse(twt.checkPermanentLimit1());
        assertFalse(twt.checkPermanentLimit1(0.9f));
        assertNull(twt.checkTemporaryLimits(Branch.Side.ONE, 0.9f));
        assertNull(twt.checkTemporaryLimits(Branch.Side.ONE));
        assertNull(twt.checkTemporaryLimits1());
        assertNull(twt.checkTemporaryLimits1(0.9f));

        assertFalse(twt.checkPermanentLimit(Branch.Side.TWO, 0.9f));
        assertFalse(twt.checkPermanentLimit(Branch.Side.TWO));
        assertFalse(twt.checkPermanentLimit2());
        assertFalse(twt.checkPermanentLimit2(0.9f));
        assertNull(twt.checkTemporaryLimits(Branch.Side.TWO, 0.9f));
        assertNull(twt.checkTemporaryLimits(Branch.Side.TWO));
        assertNull(twt.checkTemporaryLimits2());
        assertNull(twt.checkTemporaryLimits2(0.9f));

        // Topology
        TopologyVisitor visitor = mock(TopologyVisitor.class);
        mergingView.getVoltageLevel("vl1").visitEquipments(visitor);
        verify(visitor, times(1)).visitTwoWindingsTransformer(any(TwoWindingsTransformer.class), any(Branch.Side.class));

        // Not implemented yet !
        TestUtil.notImplemented(twt::remove);
    }
}
