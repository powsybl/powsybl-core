/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ThreeWindingsTransformerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private Substation substation;

    @Before
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        substation = network.getSubstation("sub");
    }

    @Test
    public void baseTests() {
        ThreeWindingsTransformer transformer = substation.newThreeWindingsTransformer()
                .setId("twt")
                .setName("twtName")
                .newLeg1()
                    .setR(1.3f)
                    .setX(1.4f)
                    .setG(1.6f)
                    .setB(1.7f)
                    .setRatedU(1.1f)
                    .setVoltageLevel("vl1")
                    .setConnectableBus("busA")
                    .setBus("busA")
                .add()
                .newLeg2()
                    .setR(2.03f)
                    .setX(2.04f)
                    .setRatedU(2.05f)
                    .setVoltageLevel("vl2")
                    .setConnectableBus("busB")
                .add()
                .newLeg3()
                    .setR(3.3f)
                    .setX(3.4f)
                    .setRatedU(3.5f)
                    .setVoltageLevel("vl2")
                    .setConnectableBus("busB")
                .add()
                .add();
        assertEquals("twt", transformer.getId());
        assertEquals("twtName", transformer.getName());
        assertEquals(substation, transformer.getSubstation());
        assertEquals(ConnectableType.THREE_WINDINGS_TRANSFORMER, transformer.getType());

        // leg1 adder
        ThreeWindingsTransformer.Leg1 leg1 = transformer.getLeg1();
        assertEquals(1.3f, leg1.getR(), 0.0f);
        assertEquals(1.4f, leg1.getX(), 0.0f);
        assertEquals(1.1f, leg1.getRatedU(), 0.0f);
        assertEquals(1.6f, leg1.getG(), 0.0f);
        assertEquals(1.7f, leg1.getB(), 0.0f);
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), leg1.getTerminal());
        // leg1 setter getter
        leg1.setR(2.0f);
        assertEquals(2.0f, leg1.getR(), 0.0f);
        leg1.setX(2.1f);
        assertEquals(2.1f, leg1.getX(), 0.0f);
        leg1.setG(2.2f);
        assertEquals(2.2f, leg1.getG(), 0.0f);
        leg1.setB(2.3f);
        assertEquals(2.3f, leg1.getB(), 0.0f);

        // leg2/3 adder
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
        // leg2/3 setter getter
        leg2.setR(1.0f);
        assertEquals(1.0f, leg2.getR(), 0.0f);
        leg2.setX(1.1f);
        assertEquals(1.1f, leg2.getX(), 0.0f);
        leg2.setRatedU(1.2f);
        assertEquals(1.2f, leg2.getRatedU(), 0.0f);
        leg3.setR(1.0f);
        assertEquals(1.0f, leg3.getR(), 0.0f);
        leg3.setX(1.1f);
        assertEquals(1.1f, leg3.getX(), 0.0f);
        leg3.setRatedU(1.2f);
        assertEquals(1.2f, leg3.getRatedU(), 0.0f);

        RatioTapChanger ratioTapChangerInLeg2 = leg2.newRatioTapChanger()
                                            .setTargetV(200.0f)
                                            .setLoadTapChangingCapabilities(false)
                                            .setLowTapPosition(0)
                                            .setTapPosition(0)
                                            .setRegulating(false)
                                            .setRegulationTerminal(transformer.getTerminal(ThreeWindingsTransformer.Side.TWO))
                                            .beginStep()
                                                .setR(39.78473f)
                                                .setX(39.784725f)
                                                .setG(0.0f)
                                                .setB(0.0f)
                                                .setRho(1.0f)
                                            .endStep()
                                            .beginStep()
                                                .setR(39.78474f)
                                                .setX(39.784726f)
                                                .setG(0.0f)
                                                .setB(0.0f)
                                                .setRho(1.0f)
                                            .endStep()
                                            .beginStep()
                                                .setR(39.78475f)
                                                .setX(39.784727f)
                                                .setG(0.0f)
                                                .setB(0.0f)
                                                .setRho(1.0f)
                                            .endStep()
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
                                                    .beginStep()
                                                        .setR(39.78473f)
                                                        .setX(39.784725f)
                                                        .setG(0.0f)
                                                        .setB(0.0f)
                                                        .setRho(1.0f)
                                                    .endStep()
                                                    .beginStep()
                                                        .setR(39.78474f)
                                                        .setX(39.784726f)
                                                        .setG(0.0f)
                                                        .setB(0.0f)
                                                        .setRho(1.0f)
                                                    .endStep()
                                                    .beginStep()
                                                        .setR(39.78475f)
                                                        .setX(39.784727f)
                                                        .setG(0.0f)
                                                        .setB(0.0f)
                                                        .setRho(1.0f)
                                                    .endStep()
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

        int count = network.getThreeWindingsTransformerCount();
        transformer.remove();
        assertNull(network.getThreeWindingsTransformer("twt"));
        assertNotNull(transformer);
        assertEquals(count - 1, network.getThreeWindingsTransformerCount());
    }

    @Test
    public void invalidLeg1ArgumentsR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is not set");
        createThreeWindingsTransformerWithLeg1(Float.NaN, 2.0f, 3.0f, 4.0f, 5.0f);
    }

    @Test
    public void invalidLeg1ArgumentsX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is not set");
        createThreeWindingsTransformerWithLeg1(1.0f, Float.NaN, 3.0f, 4.0f, 5.0f);
    }

    @Test
    public void invalidLeg1ArgumentsG() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g is not set");
        createThreeWindingsTransformerWithLeg1(1.0f, 2.0f, Float.NaN, 4.0f, 5.0f);
    }

    @Test
    public void invalidLeg1ArgumentsB() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b is not set");
        createThreeWindingsTransformerWithLeg1(1.0f, 2.0f, 3.0f, Float.NaN, 5.0f);
    }

    @Test
    public void invalidLeg1ArgumentsRatedU() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("rated u is not set");
        createThreeWindingsTransformerWithLeg1(1.0f, 2.0f, 3.0f, 4.0f, Float.NaN);
    }

    private void createThreeWindingsTransformerWithLeg1(float r, float x, float g, float b, float ratedU) {
        substation.newThreeWindingsTransformer()
                    .setId("twt")
                    .setName("twtName")
                    .newLeg1()
                        .setR(r)
                        .setX(x)
                        .setG(g)
                        .setB(b)
                        .setRatedU(ratedU)
                        .setVoltageLevel("vl1")
                        .setConnectableBus("busA")
                    .add()
                    .newLeg2()
                        .setR(2.03f)
                        .setX(2.04f)
                        .setRatedU(2.05f)
                        .setVoltageLevel("vl2")
                        .setConnectableBus("busB")
                    .add()
                    .newLeg3()
                        .setR(3.3f)
                        .setX(3.4f)
                        .setRatedU(3.5f)
                        .setVoltageLevel("vl2")
                        .setConnectableBus("busB")
                    .add()
                .add();
    }
}
