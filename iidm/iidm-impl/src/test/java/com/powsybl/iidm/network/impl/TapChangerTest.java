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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TapChangerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private Substation substation;
    private TwoWindingsTransformer twt;
    private Terminal terminal;

    @Before
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        substation = network.getSubstation("sub");
        twt = substation.newTwoWindingsTransformer()
                                .setId("twt")
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
        terminal = twt.getTerminal(Branch.Side.ONE);
    }

    @Test
    public void baseTestsPhaseTapChanger() {
        // adder
        PhaseTapChanger phaseTapChanger = twt.newPhaseTapChanger()
                                                .setTapPosition(1)
                                                .setLowTapPosition(0)
                                                .setRegulating(true)
                                                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                                                .setRegulationValue(10.0)
                                                .setRegulationTerminal(terminal)
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
        assertEquals(2, phaseTapChanger.getStepCount());
        assertEquals(0, phaseTapChanger.getLowTapPosition());
        assertEquals(1, phaseTapChanger.getHighTapPosition());
        assertTrue(phaseTapChanger.isRegulating());
        assertEquals(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, phaseTapChanger.getRegulationMode());
        assertEquals(terminal, phaseTapChanger.getRegulationTerminal());
        assertEquals(10.0, phaseTapChanger.getRegulationValue(), 0.0);

        // setter getter
        phaseTapChanger.setTapPosition(0);
        assertEquals(0, phaseTapChanger.getTapPosition());
        assertSame(phaseTapChanger.getCurrentStep(), phaseTapChanger.getStep(0));
        phaseTapChanger.setRegulationValue(5.0);
        assertEquals(5.0, phaseTapChanger.getRegulationValue(), 0.0);
        phaseTapChanger.setRegulating(false);
        assertFalse(phaseTapChanger.isRegulating());
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, phaseTapChanger.getRegulationMode());
        Terminal terminal2 = twt.getTerminal2();
        phaseTapChanger.setRegulationTerminal(terminal2);
        assertSame(terminal2, phaseTapChanger.getRegulationTerminal());

        try {
            phaseTapChanger.setTapPosition(5);
            fail();
        } catch (Exception ignored) {
        }
        try {
            phaseTapChanger.getStep(5);
            fail();
        } catch (Exception ignored) {
        }

        // remove
        phaseTapChanger.remove();
        assertNull(twt.getPhaseTapChanger());
    }

    @Test
    public void invalidTapPositionPhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("incorrect tap position");
        createPhaseTapChangerWith2Steps(3, 0, false,
                PhaseTapChanger.RegulationMode.FIXED_TAP, 1.0, terminal);
    }

    @Test
    public void invalidNullModePhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("phase regulation mode is not set");
        createPhaseTapChangerWith2Steps(1, 0, true,
                null, 1.0, terminal);
    }

    @Test
    public void invalidRegulatingValuePhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("phase regulation is on and threshold/setpoint value is not set");
        createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, Double.NaN, terminal);
    }

    @Test
    public void invalidNullRegulatingTerminalPhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("phase regulation is on and regulated terminal is not set");
        createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, 1.0, null);
    }

    @Test
    public void invalidModePhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("phase regulation cannot be on if mode is FIXED");
        createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.FIXED_TAP, 1.0, terminal);
    }

    @Test
    public void testTapChangerSetterGetterInMultiStates() {
        StateManager stateManager = network.getStateManager();
        createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, 1.0, terminal);
        createRatioTapChangerWith3Steps(0, 1, true, true, 10.0, terminal);
        createThreeWindingTransformer();
        ThreeWindingsTransformer threeWindingsTransformer = network.getThreeWindingsTransformer("twt2");
        ThreeWindingsTransformer.Leg2or3 leg2 = threeWindingsTransformer.getLeg2();
        ThreeWindingsTransformer.Leg2or3 leg3 = threeWindingsTransformer.getLeg3();
        PhaseTapChanger phaseTapChanger = twt.getPhaseTapChanger();
        RatioTapChanger ratioTapChanger = twt.getRatioTapChanger();
        RatioTapChanger ratioTapChangerInLeg2 = leg2.getRatioTapChanger();
        RatioTapChanger ratioTapChangerInLeg3 = leg3.getRatioTapChanger();

        List<String> statesToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        stateManager.cloneState(StateManagerConstants.INITIAL_STATE_ID, statesToAdd);

        stateManager.setWorkingState("s4");
        // check values cloned by extend
        assertEquals(1, phaseTapChanger.getTapPosition());
        assertTrue(phaseTapChanger.isRegulating());
        assertEquals(1.0, phaseTapChanger.getRegulationValue(), 0.0);
        assertEquals(1, ratioTapChanger.getTapPosition());
        assertTrue(ratioTapChanger.isRegulating());
        assertEquals(10.0, ratioTapChanger.getTargetV(), 0.0);
        assertEquals(1, ratioTapChangerInLeg2.getTapPosition());
        assertTrue(ratioTapChangerInLeg2.isRegulating());
        assertEquals(10.0, ratioTapChangerInLeg2.getTargetV(), 0.0);
        assertEquals(3, ratioTapChangerInLeg3.getTapPosition());
        assertTrue(ratioTapChangerInLeg3.isRegulating());
        assertEquals(11.0, ratioTapChangerInLeg3.getTargetV(), 0.0);

        // change values in s4
        phaseTapChanger.setTapPosition(0);
        phaseTapChanger.setRegulating(false);
        phaseTapChanger.setRegulationValue(9.9);
        ratioTapChanger.setTapPosition(0);
        ratioTapChanger.setRegulating(false);
        ratioTapChanger.setTargetV(3.5);
        ratioTapChangerInLeg2.setTapPosition(2);
        ratioTapChangerInLeg2.setRegulating(false);
        ratioTapChangerInLeg2.setTargetV(31.5);
        ratioTapChangerInLeg3.setTapPosition(4);
        ratioTapChangerInLeg3.setRegulating(false);
        ratioTapChangerInLeg3.setTargetV(13.5);

        // remove s2
        stateManager.removeState("s2");

        stateManager.cloneState("s4", "s2b");
        stateManager.setWorkingState("s2b");
        // check values cloned by allocate
        assertEquals(0, phaseTapChanger.getTapPosition());
        assertFalse(phaseTapChanger.isRegulating());
        assertEquals(9.9, phaseTapChanger.getRegulationValue(), 0.0);
        assertEquals(0, ratioTapChanger.getTapPosition());
        assertFalse(ratioTapChanger.isRegulating());
        assertEquals(3.5, ratioTapChanger.getTargetV(), 0.0);
        assertEquals(2, ratioTapChangerInLeg2.getTapPosition());
        assertFalse(ratioTapChangerInLeg2.isRegulating());
        assertEquals(31.5, ratioTapChangerInLeg2.getTargetV(), 0.0);
        assertEquals(4, ratioTapChangerInLeg3.getTapPosition());
        assertFalse(ratioTapChangerInLeg3.isRegulating());
        assertEquals(13.5, ratioTapChangerInLeg3.getTargetV(), 0.0);

        // recheck initial state value
        stateManager.setWorkingState(StateManagerConstants.INITIAL_STATE_ID);
        assertEquals(1, phaseTapChanger.getTapPosition());
        assertTrue(phaseTapChanger.isRegulating());
        assertEquals(1.0, phaseTapChanger.getRegulationValue(), 0.0);
        assertEquals(1, ratioTapChanger.getTapPosition());
        assertTrue(ratioTapChanger.isRegulating());
        assertEquals(10.0, ratioTapChanger.getTargetV(), 0.0);
        assertEquals(1, ratioTapChangerInLeg2.getTapPosition());
        assertTrue(ratioTapChangerInLeg2.isRegulating());
        assertEquals(10.0, ratioTapChangerInLeg2.getTargetV(), 0.0);
        assertEquals(3, ratioTapChangerInLeg3.getTapPosition());
        assertTrue(ratioTapChangerInLeg3.isRegulating());
        assertEquals(11.0, ratioTapChangerInLeg3.getTargetV(), 0.0);

        // remove working state s4
        stateManager.setWorkingState("s4");
        stateManager.removeState("s4");
        getTapPositionThrowsException(phaseTapChanger);
        getTapPositionThrowsException(ratioTapChanger);
        getTapPositionThrowsException(ratioTapChangerInLeg2);
        getTapPositionThrowsException(ratioTapChangerInLeg3);
    }

    private void getTapPositionThrowsException(TapChanger tapChanger) {
        try {
            tapChanger.getTapPosition();
            fail();
        } catch (Exception ignored) {
        }
    }

    private void createPhaseTapChangerWith2Steps(int tapPosition, int lowTap, boolean isRegulating,
                                                PhaseTapChanger.RegulationMode mode, double value, Terminal terminal) {
        twt.newPhaseTapChanger()
                .setTapPosition(tapPosition)
                .setLowTapPosition(lowTap)
                .setRegulating(isRegulating)
                .setRegulationMode(mode)
                .setRegulationValue(value)
                .setRegulationTerminal(terminal)
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
    }

    @Test
    public void invalidPhaseTapChangerWithoutSteps() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("phase tap changer shall have at least one step");
        twt.newPhaseTapChanger()
                .setTapPosition(1)
                .setLowTapPosition(0)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(10.0)
                .setRegulationTerminal(terminal)
            .add();
    }

    @Test
    public void baseTestsRatioTapChanger() {
        // adder
        RatioTapChanger ratioTapChanger = twt.newRatioTapChanger()
                                                .setLowTapPosition(0)
                                                .setTapPosition(1)
                                                .setLoadTapChangingCapabilities(false)
                                                .setRegulating(true)
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
        assertEquals(0, ratioTapChanger.getLowTapPosition());
        assertEquals(1, ratioTapChanger.getTapPosition());
        assertFalse(ratioTapChanger.hasLoadTapChangingCapabilities());
        ratioTapChanger.setRegulating(true);
        assertEquals(220.0, ratioTapChanger.getTargetV(), 0.0);
        assertSame(twt.getTerminal1(), ratioTapChanger.getRegulationTerminal());
        assertEquals(3, ratioTapChanger.getStepCount());

        // setter getter
        ratioTapChanger.setTapPosition(2);
        assertEquals(2, ratioTapChanger.getTapPosition());
        ratioTapChanger.setTargetV(110.0);
        assertEquals(110.0, ratioTapChanger.getTargetV(), 0.0);
        ratioTapChanger.setRegulating(false);
        assertFalse(ratioTapChanger.isRegulating());
        ratioTapChanger.setRegulationTerminal(twt.getTerminal2());
        assertSame(twt.getTerminal2(), ratioTapChanger.getRegulationTerminal());

        // ratio tap changer step setter/getter
        RatioTapChangerStep step = ratioTapChanger.getStep(0);
        double stepR = 10.0;
        double stepX = 20.0;
        double stepG = 30.0;
        double stepB = 40.0;
        double stepRho = 50.0;
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

        // remove
        ratioTapChanger.remove();
        assertNull(twt.getRatioTapChanger());
    }

    @Test
    public void invalidRatioTapChangerWithoutSteps() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("ratio tap changer should have at least one step");
        twt.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(false)
                .setRegulating(true)
                .setTargetV(220.0)
                .setRegulationTerminal(twt.getTerminal1())
            .add();
    }

    @Test
    public void invalidTapPosition() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("incorrect tap position");
        createRatioTapChangerWith3Steps(0, 4, true, false, 10.0, terminal);
    }

    @Test
    public void invalidTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("a target voltage has to be set for a regulating ratio tap changer");
        createRatioTapChangerWith3Steps(0, 1, true, true, Double.NaN, terminal);
    }

    @Test
    public void negativeTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("bad target voltage");
        createRatioTapChangerWith3Steps(0, 1, true, true, -10.0, terminal);
    }

    @Test
    public void nullRegulatingTerminal() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("a regulation terminal has to be set for a regulating ratio tap changer");
        createRatioTapChangerWith3Steps(0, 1, true, true, 10.0, null);
    }

    private void createRatioTapChangerWith3Steps(int low, int tap, boolean load, boolean regulating,
                                                 double targetV, Terminal terminal) {
        twt.newRatioTapChanger()
                .setLowTapPosition(low)
                .setTapPosition(tap)
                .setLoadTapChangingCapabilities(load)
                .setRegulating(regulating)
                .setTargetV(targetV)
                .setRegulationTerminal(terminal)
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

    private void createThreeWindingTransformer() {
        substation.newThreeWindingsTransformer()
                .setId("twt2")
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
        ThreeWindingsTransformer threeWindingsTransformer = network.getThreeWindingsTransformer("twt2");
        ThreeWindingsTransformer.Leg2or3 leg2 = threeWindingsTransformer.getLeg2();
        ThreeWindingsTransformer.Leg2or3 leg3 = threeWindingsTransformer.getLeg3();
        leg2.newRatioTapChanger()
                .setTargetV(10.0)
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setRegulating(true)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO))
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
        leg3.newRatioTapChanger()
                .setTargetV(11.0)
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(2)
                .setTapPosition(3)
                .setRegulating(true)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeWindingsTransformer.Side.TWO))
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
