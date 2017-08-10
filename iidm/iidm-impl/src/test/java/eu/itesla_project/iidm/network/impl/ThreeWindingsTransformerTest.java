/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

public class ThreeWindingsTransformerTest {
    @Test
    public void testSetterGetter() {
        String idThreeWindingsTransformer = "ABCD_ID";
        String nameThreeWindingsTransformer = "XYZ_NAME";

        Network network = EurostagTutorialExample1Factory.create();
        Substation p1 = network.getSubstation("P1");
        assertNotNull(p1);

        ThreeWindingsTransformerAdder transformerAdder = p1.newThreeWindingsTransformer();
        ThreeWindingsTransformer transformer = transformerAdder
                .newLeg1().setR(1.3f).setX(1.4f).setRatedU(1.1f)
                .setG(1.6f).setB(1.7f)
                .setVoltageLevel("VLGEN").setConnectableBus("NGEN").add()
                .newLeg2().setR(2.03f).setX(2.04f).setRatedU(2.05f)
                .setVoltageLevel("VLGEN").setConnectableBus("NGEN").add()
                .newLeg3().setR(3.3f).setX(3.4f).setRatedU(3.5f)
                .setVoltageLevel("VLGEN").setConnectableBus("NGEN").add()
                .setId(idThreeWindingsTransformer)
                .setName(nameThreeWindingsTransformer).add();
        assertEquals(idThreeWindingsTransformer, transformer.getId());
        assertEquals(nameThreeWindingsTransformer, transformer.getName());
        assertEquals(p1, transformer.getSubstation());
        assertEquals(ConnectableType.THREE_WINDINGS_TRANSFORMER, transformer.getType());

        // leg1 getter
        ThreeWindingsTransformer.Leg1 leg1 = transformer.getLeg1();
        assertEquals(1.3f, leg1.getR(), 0.0f);
        assertEquals(1.4f, leg1.getX(), 0.0f);
        assertEquals(1.1f, leg1.getRatedU(), 0.0f);
        assertEquals(1.6f, leg1.getG(), 0.0f);
        assertEquals(1.7f, leg1.getB(), 0.0f);
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), leg1.getTerminal());

        // leg2/3 getter
        ThreeWindingsTransformer.Leg2or3 leg2 = transformer.getLeg2();
        ThreeWindingsTransformer.Leg2or3 leg3 = transformer.getLeg3();
        assertEquals(2.03f, leg2.getR(), 0.0f);
        assertEquals(2.04f, leg2.getX(), 0.0f);
        assertEquals(2.05f, leg2.getRatedU(), 0.0f);
        assertEquals(3.3f, leg3.getR(), 0.0f);
        assertEquals(3.4f, leg3.getX(), 0.0f);
        assertEquals(3.5f, leg3.getRatedU(), 0.0f);
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), leg2.getTerminal());
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), leg3.getTerminal());

        RatioTapChanger ratioTapChangerInLeg2 = leg2.newRatioTapChanger()
                                            .setTargetV(200.0f)
                                            .setLoadTapChangingCapabilities(false)
                                            .setLowTapPosition(0)
                                            .setTapPosition(0)
                                            .setRegulating(false)
                                            .setRegulationTerminal(transformer.getTerminal(ThreeWindingsTransformer.Side.TWO))
                                            .beginStep().setR(39.78473f).setX(39.784725f).setG(0.0f).setB(0.0f).setRho(1.0f).endStep()
                                            .beginStep().setR(39.78474f).setX(39.784726f).setG(0.0f).setB(0.0f).setRho(1.0f).endStep()
                                            .beginStep().setR(39.78475f).setX(39.784727f).setG(0.0f).setB(0.0f).setRho(1.0f).endStep()
                                    .add();
        assertSame(ratioTapChangerInLeg2, leg2.getRatioTapChanger());
        CurrentLimits currentLimitsInLeg2 = leg2.newCurrentLimits()
                                        .setPermanentLimit(100)
                                        .beginTemporaryLimit()
                                        .setName("20'")
                                        .setAcceptableDuration(20 * 60)
                                        .setValue(1200)
                                        .endTemporaryLimit()
                                    .add();
        assertSame(currentLimitsInLeg2, leg2.getCurrentLimits());

        RatioTapChanger ratioTapChangerInLeg3 = leg3.newRatioTapChanger()
                                                    .setTargetV(200.0f)
                                                    .setLoadTapChangingCapabilities(false)
                                                    .setLowTapPosition(0)
                                                    .setTapPosition(0)
                                                    .setRegulating(false)
                                                    .setRegulationTerminal(transformer.getTerminal(ThreeWindingsTransformer.Side.THREE))
                                                    .beginStep().setR(39.78473f).setX(39.784725f).setG(0.0f).setB(0.0f).setRho(1.0f).endStep()
                                                    .beginStep().setR(39.78474f).setX(39.784726f).setG(0.0f).setB(0.0f).setRho(1.0f).endStep()
                                                    .beginStep().setR(39.78475f).setX(39.784727f).setG(0.0f).setB(0.0f).setRho(1.0f).endStep()
                                                .add();
        assertSame(ratioTapChangerInLeg3, leg3.getRatioTapChanger());
        CurrentLimits currentLimitsInLeg3 = leg3.newCurrentLimits()
                .setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        assertSame(currentLimitsInLeg3, leg3.getCurrentLimits());

        ThreeWindingsTransformerImpl.Leg1Impl t = new ThreeWindingsTransformerImpl.Leg1Impl(0.1f, 0.2f, 0.3f, 4f, 4f);
        t.setR(2.1f);
        assertEquals(2.1f, t.getR(), 0.0f);
        t.setX(3.1f);
        assertEquals(3.1f, t.getX(), 0.0f);
        t.setRatedU(4.1f);
        assertEquals(4.1f, t.getRatedU(),0.0f);
        t.setG(1.3f);
        assertEquals(1.3f, t.getG(), 0.0f);
        t.setB(1.4f);
        assertEquals(1.4f, t.getB(), 0.0f);
    }
}
