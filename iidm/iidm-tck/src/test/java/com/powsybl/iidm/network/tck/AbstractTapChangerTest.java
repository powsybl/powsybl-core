/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

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
import static org.mockito.Mockito.*;

public abstract class AbstractTapChangerTest {
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
                                                .setTargetDeadband(1.0)
                                                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                                                .setRegulationValue(10.0)
                                                .setRegulationTerminal(terminal)
                                                .beginStep()
                                                    .setR(1.0)
                                                    .setX(2.0)
                                                    .setG(3.0)
                                                    .setB(4.0)
                                                    .setAlpha(0.0)
                                                    .setRho(1.0)
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
        assertEquals(2, phaseTapChanger.getAllSteps().size());
        assertEquals(0, phaseTapChanger.getLowTapPosition());
        assertEquals(1, phaseTapChanger.getHighTapPosition());
        assertTrue(phaseTapChanger.isRegulating().orElse(false));
        assertEquals(1.0, phaseTapChanger.getTargetDeadband(), 0.0);
        assertEquals(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, phaseTapChanger.getRegulationMode());
        assertEquals(terminal, phaseTapChanger.getRegulationTerminal());
        assertEquals(10.0, phaseTapChanger.getRegulationValue(), 0.0);

        // setter getter
        assertEquals(0, phaseTapChanger.getNeutralPosition().orElseThrow(AssertionError::new));
        PhaseTapChangerStep neutralStep = phaseTapChanger.getNeutralStep().orElseThrow(AssertionError::new);
        assertEquals(1.0, neutralStep.getR(), 0.0);
        assertEquals(2.0, neutralStep.getX(), 0.0);
        assertEquals(3.0, neutralStep.getG(), 0.0);
        assertEquals(4.0, neutralStep.getB(), 0.0);
        assertEquals(0.0, neutralStep.getAlpha(), 0.0);
        assertEquals(1.0, neutralStep.getRho(), 0.0);
        phaseTapChanger.setTapPosition(0);
        assertEquals(0, phaseTapChanger.getTapPosition().orElse(-1));
        assertSame(phaseTapChanger.getCurrentStep(), phaseTapChanger.getStep(0));
        phaseTapChanger.setRegulationValue(5.0);
        assertEquals(5.0, phaseTapChanger.getRegulationValue(), 0.0);
        phaseTapChanger.setTargetDeadband(0.5);
        assertEquals(0.5, phaseTapChanger.getTargetDeadband(), 0.0);
        phaseTapChanger.setRegulating(false);
        assertFalse(phaseTapChanger.isRegulating().orElse(true));
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, phaseTapChanger.getRegulationMode());
        Terminal terminal2 = twt.getTerminal2();
        phaseTapChanger.setRegulationTerminal(terminal2);
        assertSame(terminal2, phaseTapChanger.getRegulationTerminal());
        int lowTapPosition = 2;
        phaseTapChanger.setLowTapPosition(lowTapPosition);
        assertEquals(lowTapPosition, phaseTapChanger.getLowTapPosition());
        assertEquals(2, phaseTapChanger.getNeutralPosition().orElseThrow(AssertionError::new));

        try {
            phaseTapChanger.setTapPosition(5);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            phaseTapChanger.getStep(5);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            phaseTapChanger.setTargetDeadband(-1);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            phaseTapChanger.setTargetDeadband(Double.NaN);
            phaseTapChanger.setRegulating(true);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

        // Changes listener
        NetworkListener mockedListener = mock(DefaultNetworkListener.class);
        // Add observer changes to current network
        network.addListener(mockedListener);
        // Changes will raise notifications
        PhaseTapChangerStep currentStep = phaseTapChanger.getCurrentStep();
        currentStep.setR(2.0);
        currentStep.setX(3.0);
        currentStep.setG(4.0);
        currentStep.setB(5.0);
        currentStep.setAlpha(6.0);
        currentStep.setRho(7.0);
        verify(mockedListener, times(6)).onUpdate(any(Identifiable.class), anyString(), any(), any());
        // Remove observer
        network.removeListener(mockedListener);
        // Cancel modification
        currentStep.setR(1.0);
        currentStep.setX(2.0);
        currentStep.setG(3.0);
        currentStep.setB(4.0);
        currentStep.setAlpha(5.0);
        currentStep.setRho(6.0);
        // Check no notification
        verifyNoMoreInteractions(mockedListener);
        // remove
        phaseTapChanger.remove();
        assertNull(twt.getPhaseTapChanger());
    }

    @Test
    public void invalidTapPositionPhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("incorrect tap position");
        createPhaseTapChangerWith2Steps(3, 0, false,
                PhaseTapChanger.RegulationMode.FIXED_TAP, 1.0, 1.0, terminal);
    }

    @Test
    public void invalidNullModePhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("phase regulation mode is not set");
        createPhaseTapChangerWith2Steps(1, 0, true,
                null, 1.0, 1.0, terminal);
    }

    @Test
    public void invalidRegulatingValuePhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("phase regulation is on and threshold/setpoint value is not set");
        createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, Double.NaN, 1.0, terminal);
    }

    @Test
    public void invalidNullRegulatingTerminalPhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("phase regulation is on and regulated terminal is not set");
        createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, 1.0, 1.0, null);
    }

    @Test
    public void invalidModePhase() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("phase regulation cannot be on if mode is FIXED");
        createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.FIXED_TAP, 1.0, 1.0, terminal);
    }

    @Test
    public void invalidTargetDeadbandPtc() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("2 windings transformer 'twt': Unexpected value for target deadband of phase tap changer: -1.0");
        createPhaseTapChangerWith2Steps(1, 0, false,
                PhaseTapChanger.RegulationMode.FIXED_TAP, 1.0, -1.0, terminal);
    }

    @Test
    public void testTapChangerSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createPhaseTapChangerWith2Steps(1, 0, false,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, 1.0, 1.0, terminal);
        createRatioTapChangerWith3Steps(0, 1, true, true, 10.0, 1.0, terminal);
        createThreeWindingTransformer();
        ThreeWindingsTransformer threeWindingsTransformer = network.getThreeWindingsTransformer("twt2");
        ThreeWindingsTransformer.Leg leg2 = threeWindingsTransformer.getLeg2();
        ThreeWindingsTransformer.Leg leg3 = threeWindingsTransformer.getLeg3();
        PhaseTapChanger phaseTapChanger = twt.getPhaseTapChanger();
        RatioTapChanger ratioTapChanger = twt.getRatioTapChanger();
        RatioTapChanger ratioTapChangerInLeg2 = leg2.getRatioTapChanger();
        RatioTapChanger ratioTapChangerInLeg3 = leg3.getRatioTapChanger();

        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertKnownState(phaseTapChanger, ratioTapChanger, ratioTapChangerInLeg2, ratioTapChangerInLeg3);

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
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(0, phaseTapChanger.getTapPosition().orElse(-1));
        assertFalse(phaseTapChanger.isRegulating().orElse(true));
        assertEquals(9.9, phaseTapChanger.getRegulationValue(), 0.0);
        assertEquals(0, ratioTapChanger.getTapPosition().orElse(-1));
        assertFalse(ratioTapChanger.isRegulating().orElse(true));
        assertEquals(3.5, ratioTapChanger.getTargetV(), 0.0);
        assertEquals(2, ratioTapChangerInLeg2.getTapPosition().orElse(-1));
        assertFalse(ratioTapChangerInLeg2.isRegulating().orElse(true));
        assertEquals(31.5, ratioTapChangerInLeg2.getTargetV(), 0.0);
        assertEquals(4, ratioTapChangerInLeg3.getTapPosition().orElse(-1));
        assertFalse(ratioTapChangerInLeg3.isRegulating().orElse(true));
        assertEquals(13.5, ratioTapChangerInLeg3.getTargetV(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertKnownState(phaseTapChanger, ratioTapChanger, ratioTapChangerInLeg2, ratioTapChangerInLeg3);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        getTapPositionThrowsException(phaseTapChanger);
        getTapPositionThrowsException(ratioTapChanger);
        getTapPositionThrowsException(ratioTapChangerInLeg2);
        getTapPositionThrowsException(ratioTapChangerInLeg3);
    }

    private void assertKnownState(PhaseTapChanger phaseTapChanger, RatioTapChanger ratioTapChanger,
            RatioTapChanger ratioTapChangerInLeg2, RatioTapChanger ratioTapChangerInLeg3) {
        assertEquals(1, phaseTapChanger.getTapPosition().orElse(-1));
        assertFalse(phaseTapChanger.isRegulating().orElse(true));
        assertEquals(1.0, phaseTapChanger.getRegulationValue(), 0.0);
        assertEquals(1, ratioTapChanger.getTapPosition().orElse(-1));
        assertTrue(ratioTapChanger.isRegulating().orElse(false));
        assertEquals(10.0, ratioTapChanger.getTargetV(), 0.0);
        assertEquals(1, ratioTapChangerInLeg2.getTapPosition().orElse(-1));
        assertTrue(ratioTapChangerInLeg2.isRegulating().orElse(false));
        assertEquals(10.0, ratioTapChangerInLeg2.getTargetV(), 0.0);
        assertEquals(3, ratioTapChangerInLeg3.getTapPosition().orElse(-1));
        assertFalse(ratioTapChangerInLeg3.isRegulating().orElse(true));
        assertEquals(11.0, ratioTapChangerInLeg3.getTargetV(), 0.0);
    }

    private void getTapPositionThrowsException(TapChanger<?, ?> tapChanger) {
        try {
            tapChanger.getTapPosition();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    private void createPhaseTapChangerWith2Steps(int tapPosition, int lowTap, boolean isRegulating,
                                                PhaseTapChanger.RegulationMode mode, double value, double deadband,
                                                 Terminal terminal) {
        twt.newPhaseTapChanger()
                .setTapPosition(tapPosition)
                .setLowTapPosition(lowTap)
                .setRegulating(isRegulating)
                .setRegulationMode(mode)
                .setRegulationValue(value)
                .setTargetDeadband(deadband)
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
                                                .setTargetDeadband(1.0)
                                                .setTargetV(220.0)
                                                .setRegulationTerminal(twt.getTerminal1())
                                                .beginStep()
                                                    .setR(39.78473)
                                                    .setX(39.784725)
                                                    .setG(0.0)
                                                    .setB(0.0)
                                                    .setRho(0.9)
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
                                                    .setRho(1.1)
                                                .endStep()
                                            .add();
        assertEquals(1, ratioTapChanger.getTapPosition().orElse(-1));
        assertEquals(3, ratioTapChanger.getAllSteps().size());
        assertFalse(ratioTapChanger.hasLoadTapChangingCapabilities());
        assertTrue(ratioTapChanger.isRegulating().orElse(false));
        assertEquals(1.0, ratioTapChanger.getTargetDeadband(), 0.0);
        assertEquals(220.0, ratioTapChanger.getTargetV(), 0.0);
        assertSame(twt.getTerminal1(), ratioTapChanger.getRegulationTerminal());
        assertEquals(3, ratioTapChanger.getStepCount());

        // setter getter
        assertEquals(1, ratioTapChanger.getNeutralPosition().orElseThrow(AssertionError::new));
        RatioTapChangerStep neutralStep = ratioTapChanger.getNeutralStep().orElseThrow(AssertionError::new);
        assertEquals(39.78474, neutralStep.getR(), 0.0);
        assertEquals(39.784726, neutralStep.getX(), 0.0);
        assertEquals(0.0, neutralStep.getG(), 0.0);
        assertEquals(0.0, neutralStep.getB(), 0.0);
        assertEquals(1.0, neutralStep.getRho(), 0.0);
        ratioTapChanger.setTapPosition(2);
        assertEquals(2, ratioTapChanger.getTapPosition().orElse(-1));
        ratioTapChanger.setTargetV(110.0);
        assertEquals(110.0, ratioTapChanger.getTargetV(), 0.0);
        ratioTapChanger.setRegulating(false);
        assertFalse(ratioTapChanger.isRegulating().orElse(true));
        ratioTapChanger.setTargetDeadband(0.5);
        assertEquals(0.5, ratioTapChanger.getTargetDeadband(), 0.0);
        ratioTapChanger.setRegulationTerminal(twt.getTerminal2());
        assertSame(twt.getTerminal2(), ratioTapChanger.getRegulationTerminal());
        ratioTapChanger.setLoadTapChangingCapabilities(true);
        assertTrue(ratioTapChanger.hasLoadTapChangingCapabilities());

        try {
            ratioTapChanger.setTargetDeadband(-1);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }
        try {
            ratioTapChanger.setTargetDeadband(Double.NaN);
            ratioTapChanger.setRegulating(true);
            fail();
        } catch (ValidationException ignored) {
            // ignore
        }

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
        createRatioTapChangerWith3Steps(0, 4, true, false, 10.0, 1.0, terminal);
    }

    @Test
    public void undefinedTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("a target voltage has to be set for a regulating ratio tap changer");
        createRatioTapChangerWith3Steps(0, 1, true, true, Double.NaN, 1.0, terminal);
    }

    @Test
    public void undefinedTargetVOnlyWarning() {
        createRatioTapChangerWith3Steps(0, 1, false, true, Double.NaN, 1.0, terminal);
        RatioTapChanger rtc = twt.getRatioTapChanger();
        assertNotNull(rtc);
        assertFalse(rtc.hasLoadTapChangingCapabilities());
        assertTrue(rtc.isRegulating().orElse(false));
        assertTrue(Double.isNaN(rtc.getTargetV()));
    }

    @Test
    public void negativeTargetV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("bad target voltage");
        createRatioTapChangerWith3Steps(0, 1, true, true, -10.0, 1.0, terminal);
    }

    @Test
    public void invalidTargetDeadbandRtc() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("2 windings transformer 'twt': Unexpected value for target deadband of ratio tap changer: -1.0");
        createRatioTapChangerWith3Steps(0, 1, true, true, 10.0, -1.0, terminal);
    }

    @Test
    public void nullRegulatingTerminal() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("a regulation terminal has to be set for a regulating ratio tap changer");
        createRatioTapChangerWith3Steps(0, 1, true, true, 10.0, 1.0, null);
    }

    @Test
    public void nullRegulatingTerminalOnlyWarning() {
        createRatioTapChangerWith3Steps(0, 1, false, true, 10.0, 1.0, null);
        RatioTapChanger rtc = twt.getRatioTapChanger();
        assertNotNull(rtc);
        assertFalse(rtc.hasLoadTapChangingCapabilities());
        assertTrue(rtc.isRegulating().orElse(false));
        assertNull(rtc.getRegulationTerminal());
    }

    private void createRatioTapChangerWith3Steps(int low, int tap, boolean load, boolean regulating,
                                                 double targetV, double deadband, Terminal terminal) {
        twt.newRatioTapChanger()
                .setLowTapPosition(low)
                .setTapPosition(tap)
                .setLoadTapChangingCapabilities(load)
                .setRegulating(regulating)
                .setTargetV(targetV)
                .setTargetDeadband(deadband)
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
        ThreeWindingsTransformer threeWindingsTransformer = network.getThreeWindingsTransformer("twt2");
        ThreeWindingsTransformer.Leg leg2 = threeWindingsTransformer.getLeg2();
        ThreeWindingsTransformer.Leg leg3 = threeWindingsTransformer.getLeg3();
        leg2.newRatioTapChanger()
                .setTargetV(10.0)
                .setTargetDeadband(0)
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
                .setRegulating(false)
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
