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
                    .setR(1.3)
                    .setX(1.4)
                    .setG(1.6)
                    .setB(1.7)
                    .setRatedU(1.1)
                    .setVoltageLevel("vl1")
                    .setConnectableBus("busA")
                    .setBus("busA")
                .add()
                .newLeg2()
                    .setR(2.03)
                    .setX(2.04)
                    .setRatedU(2.05)
                    .setVoltageLevel("vl2")
                    .setConnectableBus("busB")
                .add()
                .newLeg3()
                    .setR(3.3)
                    .setX(3.4)
                    .setRatedU(3.5)
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
        assertEquals(1.3, leg1.getR(), 0.0);
        assertEquals(1.4, leg1.getX(), 0.0);
        assertEquals(1.1, leg1.getRatedU(), 0.0);
        assertEquals(1.6, leg1.getG(), 0.0);
        assertEquals(1.7, leg1.getB(), 0.0);
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), leg1.getTerminal());
        // leg1 setter getter
        leg1.setR(2.0);
        assertEquals(2.0, leg1.getR(), 0.0);
        leg1.setX(2.1);
        assertEquals(2.1, leg1.getX(), 0.0);
        leg1.setG(2.2);
        assertEquals(2.2, leg1.getG(), 0.0);
        leg1.setB(2.3);
        assertEquals(2.3, leg1.getB(), 0.0);

        // leg2/3 adder
        ThreeWindingsTransformer.Leg2or3 leg2 = transformer.getLeg2();
        ThreeWindingsTransformer.Leg2or3 leg3 = transformer.getLeg3();
        assertEquals(2.03, leg2.getR(), 0.0);
        assertEquals(2.04, leg2.getX(), 0.0);
        assertEquals(2.05, leg2.getRatedU(), 0.0);
        assertEquals(3.3, leg3.getR(), 0.0);
        assertEquals(3.4, leg3.getX(), 0.0);
        assertEquals(3.5, leg3.getRatedU(), 0.0);
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), leg2.getTerminal());
        assertSame(transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), leg3.getTerminal());
        // leg2/3 setter getter
        leg2.setR(1.0);
        assertEquals(1.0, leg2.getR(), 0.0);
        leg2.setX(1.1);
        assertEquals(1.1, leg2.getX(), 0.0);
        leg2.setRatedU(1.2);
        assertEquals(1.2, leg2.getRatedU(), 0.0);
        leg3.setR(1.0);
        assertEquals(1.0, leg3.getR(), 0.0);
        leg3.setX(1.1);
        assertEquals(1.1, leg3.getX(), 0.0);
        leg3.setRatedU(1.2);
        assertEquals(1.2, leg3.getRatedU(), 0.0);

        RatioTapChanger ratioTapChangerInLeg2 = leg2.newRatioTapChanger()
                                            .setTargetV(200.0)
                                            .setLoadTapChangingCapabilities(false)
                                            .setLowTapPosition(0)
                                            .setTapPosition(0)
                                            .setRegulating(false)
                                            .setRegulationTerminal(transformer.getTerminal(ThreeWindingsTransformer.Side.TWO))
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
                                                    .setTargetV(200.0)
                                                    .setLoadTapChangingCapabilities(false)
                                                    .setLowTapPosition(0)
                                                    .setTapPosition(0)
                                                    .setRegulating(false)
                                                    .setRegulationTerminal(transformer.getTerminal(ThreeWindingsTransformer.Side.THREE))
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
        createThreeWindingsTransformerWithLeg1(Double.NaN, 2.0, 3.0, 4.0, 5.0);
    }

    @Test
    public void invalidLeg1ArgumentsX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is not set");
        createThreeWindingsTransformerWithLeg1(1.0, Double.NaN, 3.0, 4.0, 5.0);
    }

    @Test
    public void invalidLeg1ArgumentsG() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g is not set");
        createThreeWindingsTransformerWithLeg1(1.0, 2.0, Double.NaN, 4.0, 5.0);
    }

    @Test
    public void invalidLeg1ArgumentsB() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b is not set");
        createThreeWindingsTransformerWithLeg1(1.0, 2.0, 3.0, Double.NaN, 5.0);
    }

    @Test
    public void invalidLeg1ArgumentsRatedU() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("rated u is not set");
        createThreeWindingsTransformerWithLeg1(1.0, 2.0, 3.0, 4.0, Double.NaN);
    }

    private void createThreeWindingsTransformerWithLeg1(double r, double x, double g, double b, double ratedU) {
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
                        .setR(2.03)
                        .setX(2.04)
                        .setRatedU(2.05)
                        .setVoltageLevel("vl2")
                        .setConnectableBus("busB")
                    .add()
                    .newLeg3()
                        .setR(3.3)
                        .setX(3.4)
                        .setRatedU(3.5)
                        .setVoltageLevel("vl2")
                        .setConnectableBus("busB")
                    .add()
                .add();
    }
}
