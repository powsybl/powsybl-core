/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class RatioTapChangerAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("RatioTapChangerAdapterTest", "iidm");
        mergingView.merge(NoEquipmentNetworkFactory.create());
        createTwt("twt");
    }

    @Test
    public void testSetterGetter() {
        final TwoWindingsTransformer twt = mergingView.getTwoWindingsTransformer("twt");

        // adder
        final RatioTapChanger ratioTapChanger = createRtc(twt);
        assertTrue(twt instanceof TwoWindingsTransformerAdapter);
        assertTrue(ratioTapChanger instanceof RatioTapChangerAdapter);

        assertEquals(0, ratioTapChanger.getLowTapPosition());
        assertEquals(1, ratioTapChanger.getTapPosition());
        assertEquals(2, ratioTapChanger.getHighTapPosition());
        assertFalse(ratioTapChanger.hasLoadTapChangingCapabilities());
        assertTrue(ratioTapChanger.isRegulating());
        assertEquals(1.0, ratioTapChanger.getTargetDeadband(), 0.0);
        assertEquals(220.0, ratioTapChanger.getTargetV(), 0.0);
        assertSame(twt.getTerminal1(), ratioTapChanger.getRegulationTerminal());
        assertEquals(3, ratioTapChanger.getStepCount());

        // setter getter
        ratioTapChanger.setTapPosition(2);
        assertEquals(2, ratioTapChanger.getTapPosition());
        assertSame(ratioTapChanger.getCurrentStep(), ratioTapChanger.getStep(2));
        ratioTapChanger.setTargetV(110.0);
        assertEquals(110.0, ratioTapChanger.getTargetV(), 0.0);
        ratioTapChanger.setRegulating(false);
        assertFalse(ratioTapChanger.isRegulating());
        ratioTapChanger.setTargetDeadband(0.5);
        assertEquals(0.5, ratioTapChanger.getTargetDeadband(), 0.0);
        ratioTapChanger.setRegulationTerminal(twt.getTerminal2());
        assertSame(twt.getTerminal2(), ratioTapChanger.getRegulationTerminal());
        ratioTapChanger.setLoadTapChangingCapabilities(true);
        assertTrue(ratioTapChanger.hasLoadTapChangingCapabilities());
        final int lowTapPosition = 1;
        ratioTapChanger.setLowTapPosition(lowTapPosition);
        assertEquals(lowTapPosition, ratioTapChanger.getLowTapPosition());

        // ratio tap changer step setter/getter
        final RatioTapChangerStep step = ratioTapChanger.getStep(1);
        final double stepR = 10.0;
        final double stepX = 20.0;
        final double stepG = 30.0;
        final double stepB = 40.0;
        final double stepRho = 50.0;
        step.setR(stepR);
        assertEquals(stepR, step.getR(), 0.0);
        step.setX(stepX);
        assertEquals(stepX, step.getX(), 0.0);
        step.setG(stepG);
        assertEquals(stepG, step.getG(), 0.0);
        step.setB(stepB);
        assertEquals(stepB, step.getB(), 0.0);
        step.setRho(stepRho);
        assertEquals(stepRho, step.getRho(), 0.0);

        ratioTapChanger.remove();
        assertNull(twt.getRatioTapChanger());
    }

    @Test
    public void testAdderFromExisting() {
        createTwt("twt2");
        createRtc(mergingView.getTwoWindingsTransformer("twt"));
        mergingView.getTwoWindingsTransformer("twt2")
                .newRatioTapChanger(mergingView.getTwoWindingsTransformer("twt").getRatioTapChanger())
                .add();
        RatioTapChanger rtc = mergingView.getTwoWindingsTransformer("twt2").getRatioTapChanger();
        assertNotNull(rtc);
        assertEquals(0, rtc.getLowTapPosition());
        assertEquals(1, rtc.getTapPosition());
        assertFalse(rtc.hasLoadTapChangingCapabilities());
        assertTrue(rtc.isRegulating());
        assertEquals(1.0, rtc.getTargetDeadband(), 0.0);
        assertEquals(220.0, rtc.getTargetV(), 0.0);
        assertSame(mergingView.getTwoWindingsTransformer("twt").getTerminal1(), rtc.getRegulationTerminal());
        assertEquals(3, rtc.getStepCount());
    }

    private void createTwt(String id) {
        mergingView.getSubstation("sub").newTwoWindingsTransformer()
                .setId(id)
                .setName("twt_name")
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRatedU1(5.0)
                .setRatedU2(6.0)
                .setVoltageLevel1("vl1")
                .setVoltageLevel2("vl2")
                .setConnectableBus1("busA")
                .setConnectableBus2("busB")
                .add();
    }

    private RatioTapChanger createRtc(TwoWindingsTransformer twt) {
        return twt.newRatioTapChanger()
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
    }
}
