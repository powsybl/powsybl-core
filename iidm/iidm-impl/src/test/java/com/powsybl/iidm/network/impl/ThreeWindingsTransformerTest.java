/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.PhaseTapChanger.RegulationMode;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
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
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();

        assertEquals("twt", transformer.getId());
        assertEquals("twtName", transformer.getName());
        assertEquals(substation, transformer.getSubstation());
        assertEquals(ConnectableType.THREE_WINDINGS_TRANSFORMER, transformer.getType());

        assertEquals(substation.getThreeWindingsTransformerStream().count(), substation.getThreeWindingsTransformerCount());

        // leg1 adder
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
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
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
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

        RatioTapChanger ratioTapChangerInLeg1 = createRatioTapChanger(leg1,
            transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));

        assertSame(ratioTapChangerInLeg1, leg1.getRatioTapChanger());
        CurrentLimits currentLimitsInLeg1 = leg1.newCurrentLimits()
            .setPermanentLimit(100)
            .beginTemporaryLimit()
            .setName("20'")
            .setAcceptableDuration(20 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .add();
        assertSame(currentLimitsInLeg1, leg1.getCurrentLimits());

        RatioTapChanger ratioTapChangerInLeg2 = createRatioTapChanger(leg2,
            transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));

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

        RatioTapChanger ratioTapChangerInLeg3 = createRatioTapChanger(leg3,
            transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));

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

        PhaseTapChanger phaseTapChangerInLeg1 = createPhaseTapChanger(leg1,
            transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));
        assertSame(phaseTapChangerInLeg1, leg1.getPhaseTapChanger());

        PhaseTapChanger phaseTapChangerInLeg2 = createPhaseTapChanger(leg2,
            transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));
        assertSame(phaseTapChangerInLeg2, leg2.getPhaseTapChanger());

        PhaseTapChanger phaseTapChangerInLeg3 = createPhaseTapChanger(leg3,
            transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));
        assertSame(phaseTapChangerInLeg3, leg3.getPhaseTapChanger());

        int count = network.getThreeWindingsTransformerCount();
        transformer.remove();
        assertNull(network.getThreeWindingsTransformer("twt"));
        assertNotNull(transformer);
        assertEquals(count - 1, network.getThreeWindingsTransformerCount());
    }

    @Test
    public void leg1SetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));
        createPhaseTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg1 'twt': Only one regulating control enabled is allowed");
        leg1.getRatioTapChanger().setRegulating(true);
        leg1.getPhaseTapChanger().setRegulating(true);
    }

    @Test
    public void leg2SetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        createRatioTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));
        createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg2 'twt': Only one regulating control enabled is allowed");
        leg2.getRatioTapChanger().setRegulating(true);
        leg2.getPhaseTapChanger().setRegulating(true);
    }

    @Test
    public void leg3SetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        // First create phase
        createPhaseTapChanger(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));
        createRatioTapChanger(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg3 'twt': Only one regulating control enabled is allowed");
        leg3.getRatioTapChanger().setRegulating(true);
        leg3.getPhaseTapChanger().setRegulating(true);
    }

    @Test
    public void ratioSetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        createRatioTapChanger(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE));

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg3 'twt': Only one regulating control enabled is allowed");
        leg1.getRatioTapChanger().setRegulating(true);
        leg3.getRatioTapChanger().setRegulating(true);
    }

    @Test
    public void phaseSetTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createPhaseTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg2 'twt': Only one regulating control enabled is allowed");
        leg1.getPhaseTapChanger().setRegulating(true);
        leg2.getPhaseTapChanger().setRegulating(true);
    }

    @Test
    public void ratioAddTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), true);

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg3 'twt': Only one regulating control enabled is allowed");

        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();
        createRatioTapChanger(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), true);
    }

    @Test
    public void phaseAddTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createPhaseTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), true);

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg2 'twt': Only one regulating control enabled is allowed");

        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), true);
    }

    @Test
    public void phaseRatioAddTwoRegulatingControlsEnabled() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), true);

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg1 'twt': Only one regulating control enabled is allowed");
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE), true);
    }

    @Test
    public void ratioIncorrectTapPosition() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        RatioTapChanger rtc = createRatioTapChanger(leg1, transformer.getTerminal(ThreeWindingsTransformer.Side.ONE));

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg1 'twt': incorrect tap position 1000 [0, 2]");
        rtc.setTapPosition(1000);
    }

    @Test
    public void phaseIncorrectTapPosition() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        PhaseTapChanger ptc = createPhaseTapChanger(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO));

        thrown.expect(ValidationException.class);
        thrown.expectMessage("3 windings transformer leg2 'twt': incorrect tap position 100 [0, 2]");
        ptc.setTapPosition(100);
    }

    @Test
    public void invalidRatioStepArgumentRho() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("rho is not set");
        createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), Double.NaN, 0.0, 0.0, 0.0, 0.0);
    }

    @Test
    public void invalidRatioStepArgumentR() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is not set");
        createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), 0.0, Double.NaN, 0.0, 0.0, 0.0);
    }

    @Test
    public void invalidRatioStepArgumentX() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is not set");
        createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), 0.0, 0.0, Double.NaN, 0.0, 0.0);
    }

    @Test
    public void invalidRatioStepArgumentG() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("g is not set");
        createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), 0.0, 0.0, 0.0, Double.NaN, 0.0);
    }

    @Test
    public void invalidRatioStepArgumentB() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("b is not set");
        createRatioTapChangerOneStep(leg2, transformer.getTerminal(ThreeWindingsTransformer.Side.TWO), 0.0, 0.0, 0.0, 0.0, Double.NaN);
    }

    @Test
    public void invalidPhaseStepArgumentRho() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("rho is not set");
        createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), Double.NaN, 0.0, 0.0, 0.0, 0.0, 0.0);
    }

    @Test
    public void invalidPhaseStepArgumentAlpha() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("alpha is not set");
        createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, Double.NaN, 0.0, 0.0, 0.0, 0.0);
    }

    @Test
    public void invalidPhaseStepArgumentR() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is not set");
        createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, 0.0, Double.NaN, 0.0, 0.0, 0.0);
    }

    @Test
    public void invalidPhaseStepArgumentX() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is not set");
        createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, 0.0, 0.0,
            Double.NaN, 0.0, 0.0);
    }

    @Test
    public void invalidPhaseStepArgumentG() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("g is not set");
        createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, 0.0, 0.0,
            0.0, Double.NaN, 0.0);
    }

    @Test
    public void invalidPhaseStepArgumentB() {
        ThreeWindingsTransformer transformer = createThreeWindingsTransformer();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        thrown.expect(ValidationException.class);
        thrown.expectMessage("b is not set");
        createPhaseTapChangerOneStep(leg3, transformer.getTerminal(ThreeWindingsTransformer.Side.THREE), 0.0, 0.0, 0.0,
            0.0, 0.0, Double.NaN);
    }

    private ThreeWindingsTransformer createThreeWindingsTransformer() {
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
            .setG(0.0)
            .setB(0.0)
            .setRatedU(2.05)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .newLeg3()
            .setR(3.3)
            .setX(3.4)
            .setG(0.0)
            .setB(0.0)
            .setRatedU(3.5)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .add();

        return transformer;
    }

    private RatioTapChanger createRatioTapChanger(Leg leg, Terminal terminal) {
        return createRatioTapChanger(leg, terminal, false);
    }

    private RatioTapChanger createRatioTapChanger(Leg leg, Terminal terminal, boolean regulating) {
        RatioTapChanger ratioTapChangerInLeg = leg.newRatioTapChanger()
            .setTargetV(200.0)
            .setLoadTapChangingCapabilities(false)
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(regulating)
            .setRegulationTerminal(terminal)
            .setTargetDeadband(0.5)
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

        return ratioTapChangerInLeg;
    }

    private RatioTapChanger createRatioTapChangerOneStep(Leg leg, Terminal terminal, double rho, double r, double x, double g, double b) {
        RatioTapChanger ratioTapChangerInLeg = leg.newRatioTapChanger()
            .setTargetV(200.0)
            .setLoadTapChangingCapabilities(false)
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(false)
            .setRegulationTerminal(terminal)
            .setTargetDeadband(0.5)
            .beginStep()
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRho(rho)
            .endStep()
            .add();

        return ratioTapChangerInLeg;
    }

    private PhaseTapChanger createPhaseTapChanger(Leg leg, Terminal terminal) {
        return createPhaseTapChanger(leg, terminal, false);
    }

    private PhaseTapChanger createPhaseTapChanger(Leg leg, Terminal terminal, boolean regulating) {
        PhaseTapChanger phaseTapChangerInLeg = leg.newPhaseTapChanger()
            .setRegulationValue(200.0)
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(regulating)
            .setRegulationTerminal(terminal)
            .setRegulationMode(RegulationMode.ACTIVE_POWER_CONTROL)
            .setTargetDeadband(0.5)
            .beginStep()
                .setR(39.78473)
                .setX(39.784725)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .setAlpha(-10.0)
            .endStep()
            .beginStep()
                .setR(39.78474)
                .setX(39.784726)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .setAlpha(0.0)
            .endStep()
            .beginStep()
                .setR(39.78475)
                .setX(39.784727)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .setAlpha(10.0)
                .endStep()
            .add();

        return phaseTapChangerInLeg;
    }

    private PhaseTapChanger createPhaseTapChangerOneStep(Leg leg, Terminal terminal, double rho, double alpha, double r, double x, double g, double b) {
        PhaseTapChanger phaseTapChangerInLeg = leg.newPhaseTapChanger()
            .setRegulationValue(200.0)
            .setLowTapPosition(0)
            .setTapPosition(0)
            .setRegulating(false)
            .setRegulationTerminal(terminal)
            .setRegulationMode(RegulationMode.ACTIVE_POWER_CONTROL)
            .setTargetDeadband(0.5)
            .beginStep()
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setRho(rho)
                .setAlpha(alpha)
            .endStep()
            .add();

        return phaseTapChangerInLeg;
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
        thrown.expectMessage("rated U is invalid");
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

    @Test
    public void invalidLeg2ArgumentsR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is not set");
        createThreeWindingsTransformerWithLeg2(Double.NaN, 2.2, 3.2, 4.2, 5.2);
    }

    @Test
    public void invalidLeg2ArgumentsX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is not set");
        createThreeWindingsTransformerWithLeg2(1.2, Double.NaN, 3.2, 4.2, 5.2);
    }

    @Test
    public void invalidLeg2ArgumentsG() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g is not set");
        createThreeWindingsTransformerWithLeg2(1.2, 2.2, Double.NaN, 4.2, 5.2);
    }

    @Test
    public void invalidLeg2ArgumentsB() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b is not set");
        createThreeWindingsTransformerWithLeg2(1.2, 2.2, 3.2, Double.NaN, 5.2);
    }

    @Test
    public void invalidLeg2ArgumentsRatedU() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("rated U is invalid");
        createThreeWindingsTransformerWithLeg2(1.2, 2.2, 3.2, 4.2, Double.NaN);
    }

    private void createThreeWindingsTransformerWithLeg2(double r, double x, double g, double b, double ratedU) {
        substation.newThreeWindingsTransformer()
            .setId("twt")
            .setName("twtName")
            .newLeg1()
            .setR(2.03)
            .setX(2.04)
            .setG(0.05)
            .setB(0.03)
            .setRatedU(2.05)
            .setVoltageLevel("vl1")
            .setConnectableBus("busA")
            .add()
            .newLeg2()
            .setR(r)
            .setX(x)
            .setG(g)
            .setB(b)
            .setRatedU(ratedU)
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

    @Test
    public void invalidLeg3ArgumentsR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is not set");
        createThreeWindingsTransformerWithLeg3(Double.NaN, 2.3, 3.3, 4.3, 5.3);
    }

    @Test
    public void invalidLeg3ArgumentsX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is not set");
        createThreeWindingsTransformerWithLeg3(1.3, Double.NaN, 3.3, 4.3, 5.3);
    }

    @Test
    public void invalidLeg3ArgumentsG() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g is not set");
        createThreeWindingsTransformerWithLeg3(1.3, 2.3, Double.NaN, 4.3, 5.3);
    }

    @Test
    public void invalidLeg3ArgumentsB() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b is not set");
        createThreeWindingsTransformerWithLeg3(1.3, 2.3, 3.3, Double.NaN, 5.3);
    }

    @Test
    public void invalidLeg3ArgumentsRatedU() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("rated U is invalid");
        createThreeWindingsTransformerWithLeg3(1.3, 2.3, 3.3, 4.3, Double.NaN);
    }

    private void createThreeWindingsTransformerWithLeg3(double r, double x, double g, double b, double ratedU) {
        substation.newThreeWindingsTransformer()
            .setId("twt")
            .setName("twtName")
            .newLeg1()
            .setR(2.03)
            .setX(2.04)
            .setG(0.05)
            .setB(0.03)
            .setRatedU(2.05)
            .setVoltageLevel("vl1")
            .setConnectableBus("busA")
            .add()
            .newLeg2()
            .setR(3.3)
            .setX(3.4)
            .setG(0.025)
            .setB(0.015)
            .setRatedU(3.5)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .newLeg3()
            .setR(r)
            .setX(x)
            .setG(g)
            .setB(b)
            .setRatedU(ratedU)
            .setVoltageLevel("vl2")
            .setConnectableBus("busB")
            .add()
            .add();
    }
}
