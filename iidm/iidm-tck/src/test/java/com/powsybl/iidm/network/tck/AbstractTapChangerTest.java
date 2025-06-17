/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public abstract class AbstractTapChangerTest {

    private Network network;
    private Substation substation;
    private TwoWindingsTransformer twt;
    private Terminal terminal;

    @BeforeEach
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
        terminal = twt.getTerminal(TwoSides.ONE);
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
        assertTrue(phaseTapChanger.isRegulating());
        assertEquals(1.0, phaseTapChanger.getTargetDeadband(), 0.0);
        assertEquals(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, phaseTapChanger.getRegulationMode());
        assertEquals(terminal, phaseTapChanger.getRegulationTerminal());
        assertEquals(10.0, phaseTapChanger.getRegulationValue(), 0.0);

        // setter getter
        assertEquals(0, phaseTapChanger.getNeutralPosition().orElseThrow(IllegalStateException::new));
        PhaseTapChangerStep neutralStep = phaseTapChanger.getNeutralStep().orElseThrow(IllegalStateException::new);
        assertEquals(1.0, neutralStep.getR(), 0.0);
        assertEquals(2.0, neutralStep.getX(), 0.0);
        assertEquals(3.0, neutralStep.getG(), 0.0);
        assertEquals(4.0, neutralStep.getB(), 0.0);
        assertEquals(0.0, neutralStep.getAlpha(), 0.0);
        assertEquals(1.0, neutralStep.getRho(), 0.0);
        phaseTapChanger.setTapPosition(0);
        assertEquals(0, phaseTapChanger.getTapPosition());
        assertEquals(phaseTapChanger.getCurrentStep().getR(), phaseTapChanger.getStep(0).getR(), 0.0);
        assertEquals(phaseTapChanger.getCurrentStep().getX(), phaseTapChanger.getStep(0).getX(), 0.0);
        assertEquals(phaseTapChanger.getCurrentStep().getG(), phaseTapChanger.getStep(0).getG(), 0.0);
        assertEquals(phaseTapChanger.getCurrentStep().getB(), phaseTapChanger.getStep(0).getB(), 0.0);
        assertEquals(phaseTapChanger.getCurrentStep().getAlpha(), phaseTapChanger.getStep(0).getAlpha(), 0.0);
        assertEquals(phaseTapChanger.getCurrentStep().getRho(), phaseTapChanger.getStep(0).getRho(), 0.0);
        phaseTapChanger.setRegulationValue(5.0);
        assertEquals(5.0, phaseTapChanger.getRegulationValue(), 0.0);
        phaseTapChanger.setTargetDeadband(0.5);
        assertEquals(0.5, phaseTapChanger.getTargetDeadband(), 0.0);
        phaseTapChanger.setRegulating(false);
        assertFalse(phaseTapChanger.isRegulating());
        phaseTapChanger.setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
        assertEquals(PhaseTapChanger.RegulationMode.FIXED_TAP, phaseTapChanger.getRegulationMode());
        Terminal terminal2 = twt.getTerminal2();
        phaseTapChanger.setRegulationTerminal(terminal2);
        assertSame(terminal2, phaseTapChanger.getRegulationTerminal());
        Terminal loadTerminal = twt.getTerminal1().getVoltageLevel().newLoad().setId("L").setP0(1.0).setQ0(1.0).setBus("busA").add().getTerminal();
        phaseTapChanger.setRegulationTerminal(loadTerminal).setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER).setRegulating(true);
        assertSame(loadTerminal, phaseTapChanger.getRegulationTerminal());
        network.getLoad("L").remove();
        assertNull(phaseTapChanger.getRegulationTerminal());
        assertFalse(phaseTapChanger.isRegulating());
        phaseTapChanger.setRegulationTerminal(terminal).setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP);
        int lowTapPosition = 2;
        phaseTapChanger.setLowTapPosition(lowTapPosition);
        assertEquals(lowTapPosition, phaseTapChanger.getLowTapPosition());
        assertEquals(2, phaseTapChanger.getNeutralPosition().orElseThrow(IllegalStateException::new));

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
        verify(mockedListener, times(6)).onUpdate(any(Identifiable.class), anyString(), nullable(String.class), any(), any());
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
    public void testDefaultPhaseTapChangerStep() {
        PhaseTapChanger phaseTapChanger = twt.newPhaseTapChanger()
                .setTapPosition(0)
                .setLowTapPosition(0)
                .setRegulating(true)
                .setTargetDeadband(1.0)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(10.0)
                .setRegulationTerminal(terminal)
                .beginStep()
                    .setAlpha(0.0)
                .endStep()
                .add();

        PhaseTapChangerStep step = phaseTapChanger.getStep(0);
        assertEquals(0.0, step.getR(), 0.0);
        assertEquals(0.0, step.getX(), 0.0);
        assertEquals(0.0, step.getG(), 0.0);
        assertEquals(0.0, step.getB(), 0.0);
        assertEquals(1.0, step.getRho(), 0.0);
    }

    @Test
    public void testPhaseTapChangerStepsReplacer() {
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
        assertEquals(0, phaseTapChanger.getNeutralPosition().orElseThrow());

        //check neutral step attributes
        PhaseTapChangerStep neutralStep = phaseTapChanger.getNeutralStep().orElseThrow();
        assertEquals(0, neutralStep.getAlpha(), 0.0);
        assertEquals(1, neutralStep.getRho(), 0.0);
        assertEquals(1, neutralStep.getR(), 0.0);
        assertEquals(2, neutralStep.getX(), 0.0);
        assertEquals(3, neutralStep.getG(), 0.0);
        assertEquals(4, neutralStep.getB(), 0.0);

        //replace steps
        phaseTapChanger.stepsReplacer()
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setAlpha(5.0)
                .setRho(6.0)
                .endStep()
                .beginStep()
                .setR(5.0)
                .setX(6.0)
                .setG(7.0)
                .setB(8.0)
                .setAlpha(6.0)
                .setRho(7.0)
                .endStep()
                .beginStep()
                .setR(9.0)
                .setX(10.0)
                .setG(11.0)
                .setB(12.0)
                .setAlpha(0.0)
                .setRho(1.0)
                .endStep()
                .replaceSteps();

        assertEquals(3, phaseTapChanger.getStepCount());
        assertEquals(3, phaseTapChanger.getAllSteps().size());
        assertEquals(0, phaseTapChanger.getLowTapPosition());
        assertEquals(2, phaseTapChanger.getHighTapPosition());
        assertEquals(2, phaseTapChanger.getNeutralPosition().orElseThrow());

        //check neutral step attributes
        neutralStep = phaseTapChanger.getNeutralStep().orElseThrow();
        assertEquals(0, neutralStep.getAlpha(), 0.0);
        assertEquals(1, neutralStep.getRho(), 0.0);
        assertEquals(9, neutralStep.getR(), 0.0);
        assertEquals(10, neutralStep.getX(), 0.0);
        assertEquals(11, neutralStep.getG(), 0.0);
        assertEquals(12, neutralStep.getB(), 0.0);
    }

    @Test
    public void invalidTapPositionPhase() {
        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerWith2Steps(3, 0, false,
                PhaseTapChanger.RegulationMode.FIXED_TAP, 1.0, 1.0, terminal));
        assertTrue(e.getMessage().contains("incorrect tap position"));
    }

    @Test
    public void invalidNullModePhase() {
        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerWith2Steps(1, 0, true,
                null, 1.0, 1.0, terminal));
        assertTrue(e.getMessage().contains("phase regulation mode is not set"));
    }

    @Test
    public void invalidRegulatingValuePhase() {
        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, Double.NaN, 1.0, terminal));
        assertTrue(e.getMessage().contains("phase regulation is on and threshold/setpoint value is not set"));
    }

    @Test
    public void invalidNullRegulatingTerminalPhase() {
        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, 1.0, 1.0, null));
        assertTrue(e.getMessage().contains("phase regulation is on and regulated terminal is not set"));
    }

    @Test
    public void invalidModePhase() {
        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerWith2Steps(1, 0, true,
                PhaseTapChanger.RegulationMode.FIXED_TAP, 1.0, 1.0, terminal));
        assertTrue(e.getMessage().contains("phase regulation cannot be on if mode is FIXED"));
    }

    @Test
    public void invalidTargetDeadbandPtc() {
        ValidationException e = assertThrows(ValidationException.class, () -> createPhaseTapChangerWith2Steps(1, 0, false,
                PhaseTapChanger.RegulationMode.FIXED_TAP, 1.0, -1.0, terminal));
        assertTrue(e.getMessage().contains("2 windings transformer 'twt': Unexpected value for target deadband of phase tap changer: -1.0"));
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
        ratioTapChanger.setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE);
        ratioTapChanger.setRegulationValue(3.5);
        ratioTapChangerInLeg2.setTapPosition(2);
        ratioTapChangerInLeg2.setRegulating(false);
        ratioTapChangerInLeg2.setRegulationMode(RatioTapChanger.RegulationMode.REACTIVE_POWER);
        ratioTapChangerInLeg2.setRegulationValue(31.5);
        ratioTapChangerInLeg3.setTapPosition(4);
        ratioTapChangerInLeg3.setRegulating(false);
        ratioTapChangerInLeg3.setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE);
        ratioTapChangerInLeg3.setRegulationValue(13.5);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(0, phaseTapChanger.getTapPosition());
        assertFalse(phaseTapChanger.isRegulating());
        assertEquals(9.9, phaseTapChanger.getRegulationValue(), 0.0);
        assertEquals(0, ratioTapChanger.getTapPosition());
        assertFalse(ratioTapChanger.isRegulating());
        assertEquals(RatioTapChanger.RegulationMode.VOLTAGE, ratioTapChanger.getRegulationMode());
        assertEquals(3.5, ratioTapChanger.getRegulationValue(), 0.0);
        assertEquals(2, ratioTapChangerInLeg2.getTapPosition());
        assertFalse(ratioTapChangerInLeg2.isRegulating());
        assertEquals(RatioTapChanger.RegulationMode.REACTIVE_POWER, ratioTapChangerInLeg2.getRegulationMode());
        assertEquals(31.5, ratioTapChangerInLeg2.getRegulationValue(), 0.0);
        assertEquals(4, ratioTapChangerInLeg3.getTapPosition());
        assertFalse(ratioTapChangerInLeg3.isRegulating());
        assertEquals(RatioTapChanger.RegulationMode.VOLTAGE, ratioTapChangerInLeg3.getRegulationMode());
        assertEquals(13.5, ratioTapChangerInLeg3.getRegulationValue(), 0.0);

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

        // check we delete a single variant's values
        variantManager.setWorkingVariant("s3");
        assertEquals(1, phaseTapChanger.getTapPosition());
    }

    private void assertKnownState(PhaseTapChanger phaseTapChanger, RatioTapChanger ratioTapChanger,
            RatioTapChanger ratioTapChangerInLeg2, RatioTapChanger ratioTapChangerInLeg3) {
        assertEquals(1, phaseTapChanger.getTapPosition());
        assertFalse(phaseTapChanger.isRegulating());
        assertEquals(1.0, phaseTapChanger.getRegulationValue(), 0.0);
        assertEquals(1, ratioTapChanger.getTapPosition());
        assertTrue(ratioTapChanger.isRegulating());
        assertEquals(10.0, ratioTapChanger.getRegulationValue(), 0.0);
        assertEquals(1, ratioTapChangerInLeg2.getTapPosition());
        assertTrue(ratioTapChangerInLeg2.isRegulating());
        assertEquals(10.0, ratioTapChangerInLeg2.getRegulationValue(), 0.0);
        assertEquals(3, ratioTapChangerInLeg3.getTapPosition());
        assertFalse(ratioTapChangerInLeg3.isRegulating());
        assertEquals(11.0, ratioTapChangerInLeg3.getRegulationValue(), 0.0);
    }

    private void getTapPositionThrowsException(TapChanger<?, ?, ?, ?> tapChanger) {
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
        PhaseTapChangerAdder phaseTapChangerAdder = twt.newPhaseTapChanger()
                .setTapPosition(1)
                .setLowTapPosition(0)
                .setRegulating(true)
                .setRegulationMode(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL)
                .setRegulationValue(10.0)
                .setRegulationTerminal(terminal);
        ValidationException e = assertThrows(ValidationException.class, phaseTapChangerAdder::add);
        assertEquals("2 windings transformer 'twt': phase tap changer should have at least one step", e.getMessage());
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
                                                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                                                .setRegulationValue(220.0)
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
        assertEquals(1, ratioTapChanger.getTapPosition());
        assertEquals(3, ratioTapChanger.getAllSteps().size());
        assertFalse(ratioTapChanger.hasLoadTapChangingCapabilities());
        assertTrue(ratioTapChanger.isRegulating());
        assertEquals(1.0, ratioTapChanger.getTargetDeadband(), 0.0);
        assertEquals(RatioTapChanger.RegulationMode.VOLTAGE, ratioTapChanger.getRegulationMode());
        assertEquals(220.0, ratioTapChanger.getRegulationValue(), 0.0);
        assertSame(twt.getTerminal1(), ratioTapChanger.getRegulationTerminal());
        assertEquals(3, ratioTapChanger.getStepCount());

        // setter getter
        assertEquals(1, ratioTapChanger.getNeutralPosition().orElseThrow(IllegalStateException::new));
        RatioTapChangerStep neutralStep = ratioTapChanger.getNeutralStep().orElseThrow(IllegalStateException::new);
        assertEquals(39.78474, neutralStep.getR(), 0.0);
        assertEquals(39.784726, neutralStep.getX(), 0.0);
        assertEquals(0.0, neutralStep.getG(), 0.0);
        assertEquals(0.0, neutralStep.getB(), 0.0);
        assertEquals(1.0, neutralStep.getRho(), 0.0);
        ratioTapChanger.setTapPosition(2);
        assertEquals(2, ratioTapChanger.getTapPosition());
        ratioTapChanger.setRegulationValue(110.0);
        assertEquals(110.0, ratioTapChanger.getRegulationValue(), 0.0);
        ratioTapChanger.setRegulationMode(RatioTapChanger.RegulationMode.REACTIVE_POWER);
        assertEquals(RatioTapChanger.RegulationMode.REACTIVE_POWER, ratioTapChanger.getRegulationMode());
        ratioTapChanger.setRegulationValue(-50.0);
        assertEquals(-50.0, ratioTapChanger.getRegulationValue(), 0.0);
        ratioTapChanger.setRegulating(false);
        assertFalse(ratioTapChanger.isRegulating());
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
    public void testDefaultRatioTapChangerStep() {
        RatioTapChanger ratioTapChanger = twt.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .setLoadTapChangingCapabilities(false)
                .setRegulating(true)
                .setTargetDeadband(1.0)
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(220.0)
                .setRegulationTerminal(twt.getTerminal1())
                .beginStep()
                    .setRho(0.9)
                .endStep()
                .add();

        RatioTapChangerStep step = ratioTapChanger.getStep(0);
        assertEquals(0.0, step.getR(), 0.0);
        assertEquals(0.0, step.getX(), 0.0);
        assertEquals(0.0, step.getG(), 0.0);
        assertEquals(0.0, step.getB(), 0.0);
    }

    @Test
    public void testRatioTapChangerStepsReplacer() {
        RatioTapChanger ratioTapChanger = twt.newRatioTapChanger()
                .setTapPosition(1)
                .setLowTapPosition(0)
                .setRegulating(true)
                .setTargetDeadband(1.0)
                .setRegulationMode(RatioTapChanger.RegulationMode.REACTIVE_POWER)
                .setRegulationValue(10.0)
                .setRegulationTerminal(terminal)
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRho(6.0)
                .endStep()
                .add();
        assertEquals(2, ratioTapChanger.getStepCount());
        assertEquals(2, ratioTapChanger.getAllSteps().size());
        assertEquals(0, ratioTapChanger.getLowTapPosition());
        assertEquals(1, ratioTapChanger.getHighTapPosition());
        assertEquals(0, ratioTapChanger.getNeutralPosition().orElseThrow());

        //check neutral step attributes
        RatioTapChangerStep neutralStep = ratioTapChanger.getNeutralStep().orElseThrow();
        assertEquals(1, neutralStep.getRho());
        assertEquals(1, neutralStep.getR());
        assertEquals(2, neutralStep.getX());
        assertEquals(3, neutralStep.getG());
        assertEquals(4, neutralStep.getB());

        //replace steps
        ratioTapChanger.stepsReplacer()
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRho(6.0)
                .endStep()
                .beginStep()
                .setR(5.0)
                .setX(6.0)
                .setG(7.0)
                .setB(8.0)
                .setRho(7.0)
                .endStep()
                .beginStep()
                .setR(9.0)
                .setX(10.0)
                .setG(11.0)
                .setB(12.0)
                .setRho(1.0)
                .endStep()
                .replaceSteps();

        assertEquals(3, ratioTapChanger.getStepCount());
        assertEquals(3, ratioTapChanger.getAllSteps().size());
        assertEquals(0, ratioTapChanger.getLowTapPosition());
        assertEquals(2, ratioTapChanger.getHighTapPosition());
        assertEquals(2, ratioTapChanger.getNeutralPosition().orElseThrow());

        //check neutral step attributes
        neutralStep = ratioTapChanger.getNeutralStep().orElseThrow();
        assertEquals(1, neutralStep.getRho());
        assertEquals(9, neutralStep.getR());
        assertEquals(10, neutralStep.getX());
        assertEquals(11, neutralStep.getG());
        assertEquals(12, neutralStep.getB());
    }

    @Test
    public void invalidRatioTapChangerWithoutSteps() {
        ValidationException e = assertThrows(ValidationException.class, () -> twt.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(false)
                .setRegulating(true)
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(220.0)
                .setRegulationTerminal(twt.getTerminal1())
            .add());
        assertTrue(e.getMessage().contains("ratio tap changer should have at least one step"));
    }

    @Test
    public void invalidTapPosition() {
        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerWith3Steps(0, 4, true, false, 10.0, 1.0, terminal));
        assertTrue(e.getMessage().contains("incorrect tap position"));
    }

    @Test
    public void undefinedRegulationValue() {
        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerWith3Steps(0, 1, true, true, Double.NaN, 1.0, terminal));
        assertTrue(e.getMessage().contains("a regulation value has to be set for a regulating ratio tap changer"));

        ValidationException e2 = assertThrows(ValidationException.class, () -> createRatioTapChangerWith3Steps(0, 1, true, true, RatioTapChanger.RegulationMode.REACTIVE_POWER, Double.NaN, 1.0, terminal));
        assertTrue(e2.getMessage().contains("a regulation value has to be set for a regulating ratio tap changer"));
    }

    @Test
    public void undefinedRegulationValueOnlyWarning() {
        createRatioTapChangerWith3Steps(0, 1, false, true, Double.NaN, 1.0, terminal);
        RatioTapChanger rtc = twt.getRatioTapChanger();
        assertNotNull(rtc);
        assertFalse(rtc.hasLoadTapChangingCapabilities());
        assertTrue(rtc.isRegulating());
        assertEquals(RatioTapChanger.RegulationMode.VOLTAGE, rtc.getRegulationMode());
        assertTrue(Double.isNaN(rtc.getRegulationValue()));

        createRatioTapChangerWith3Steps(0, 1, false, true, RatioTapChanger.RegulationMode.REACTIVE_POWER, Double.NaN, 1.0, terminal);
        rtc = twt.getRatioTapChanger();
        assertNotNull(rtc);
        assertFalse(rtc.hasLoadTapChangingCapabilities());
        assertTrue(rtc.isRegulating());
        assertEquals(RatioTapChanger.RegulationMode.REACTIVE_POWER, rtc.getRegulationMode());
        assertTrue(Double.isNaN(rtc.getRegulationValue()));
    }

    @Test
    public void invalidNullModeRatio() {
        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerWith3Steps(0, 1, true, true, null, 10.0, 1.0, terminal));
        assertTrue(e.getMessage().contains("regulation mode of regulating ratio tap changer must be given"));
    }

    @Test
    public void negativeTargetV() {
        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerWith3Steps(0, 1, true, true, -10.0, 1.0, terminal));
        assertTrue(e.getMessage().contains("bad target voltage"));
    }

    @Test
    public void targetVGettingInReactivePowerMode() {
        createRatioTapChangerWith3Steps(0, 1, false, true, RatioTapChanger.RegulationMode.REACTIVE_POWER, -50, 1.0, terminal);
        RatioTapChanger rtc = twt.getRatioTapChanger();
        // getTargetV NaN because RTC is in reactive power control mode
        assertTrue(Double.isNaN(rtc.getTargetV()));
    }

    @Test
    public void targetVSetting() {
        createRatioTapChangerWith3Steps(0, 1, false, true, RatioTapChanger.RegulationMode.REACTIVE_POWER, -50, 1.0, terminal);
        RatioTapChanger rtc = twt.getRatioTapChanger();
        assertDoesNotThrow(() -> rtc.setTargetV(130));
        // setTargetV switched RTC to voltage control mode
        assertEquals(RatioTapChanger.RegulationMode.VOLTAGE, rtc.getRegulationMode());
        assertEquals(130, rtc.getTargetV(), 0.0);
    }

    @Test
    public void invalidTargetDeadbandRtc() {
        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerWith3Steps(0, 1, true, true, 10.0, -1.0, terminal));
        assertTrue(e.getMessage().contains("2 windings transformer 'twt': Unexpected value for target deadband of ratio tap changer: -1.0"));
    }

    @Test
    public void nullRegulatingTerminal() {
        ValidationException e = assertThrows(ValidationException.class, () -> createRatioTapChangerWith3Steps(0, 1, true, true, 10.0, 1.0, null));
        assertTrue(e.getMessage().contains("a regulation terminal has to be set for a regulating ratio tap changer"));
    }

    @Test
    public void nullRegulatingTerminalOnlyWarning() {
        createRatioTapChangerWith3Steps(0, 1, false, true, 10.0, 1.0, null);
        RatioTapChanger rtc = twt.getRatioTapChanger();
        assertNotNull(rtc);
        assertFalse(rtc.hasLoadTapChangingCapabilities());
        assertTrue(rtc.isRegulating());
        assertNull(rtc.getRegulationTerminal());
    }

    @Test
    public void createInvalidRatioTapChangerEquipmentLevel() {
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        createRatioTapChangerWith3Steps(0, 1, true, false, 10.0, 1.0, terminal);
        RatioTapChanger rtc = network.getTwoWindingsTransformer("twt").getRatioTapChanger();
        rtc.setTapPosition(4);
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
        assertEquals(4, rtc.getTapPosition());
    }

    @Test
    public void createInvalidPhaseTapChangerEquipmentLevel() {
        createPhaseTapChangerWith2Steps(1, 0, false,
            PhaseTapChanger.RegulationMode.FIXED_TAP, 1.0, 1.0, terminal);
        PhaseTapChanger ptc = network.getTwoWindingsTransformer("twt").getPhaseTapChanger();
        assertThrows(ValidationException.class, () -> ptc.setTapPosition(4));
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
        ptc.setTapPosition(4);
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());
        assertEquals(4, network.getTwoWindingsTransformer("twt").getPhaseTapChanger().getTapPosition());
    }

    private void createRatioTapChangerWith3Steps(int low, int tap, boolean load, boolean regulating,
                                                 double targetV, double deadband, Terminal terminal) {
        createRatioTapChangerWith3Steps(low, tap, load, regulating, RatioTapChanger.RegulationMode.VOLTAGE, targetV, deadband, terminal);
    }

    private void createRatioTapChangerWith3Steps(int low, int tap, boolean load, boolean regulating,
                                                 RatioTapChanger.RegulationMode regulationMode,
                                                 double regulationValue, double deadband, Terminal terminal) {
        twt.newRatioTapChanger()
                .setLowTapPosition(low)
                .setTapPosition(tap)
                .setLoadTapChangingCapabilities(load)
                .setRegulating(regulating)
                .setRegulationMode(regulationMode)
                .setRegulationValue(regulationValue)
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
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(10.0)
                .setTargetDeadband(0)
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setRegulating(true)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeSides.TWO))
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
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setRegulationValue(11.0)
                .setLoadTapChangingCapabilities(false)
                .setLowTapPosition(2)
                .setTapPosition(3)
                .setRegulating(false)
                .setRegulationTerminal(threeWindingsTransformer.getTerminal(ThreeSides.TWO))
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
