/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class TapChangerAttributeTest {

    @Test
    void testTapChangerAttributeName() {
        Network network = NoEquipmentNetworkFactory.create();
        Substation substation = network.getSubstation("sub");

        // Check name for two winding transformers
        TwoWindingsTransformer twt2 = createTwoWindingsTransformer(substation);
        createPhaseTapChanger(twt2);
        createRatioTapChanger(twt2);
        assertEquals("phaseTapChanger", ((AbstractTapChanger) twt2.getPhaseTapChanger()).getTapChangerAttribute());
        assertEquals("ratioTapChanger", ((AbstractTapChanger) twt2.getRatioTapChanger()).getTapChangerAttribute());

        // Check name for three winding transformers
        ThreeWindingsTransformer twt3 = createThreeWindingsTransformer(substation);
        createRatioTapChanger(twt3.getLeg2());
        createRatioTapChanger(twt3.getLeg3());
        assertEquals("ratioTapChanger2",
            ((AbstractTapChanger) twt3.getLeg2().getRatioTapChanger()).getTapChangerAttribute());
        assertEquals("ratioTapChanger3",
            ((AbstractTapChanger) twt3.getLeg3().getRatioTapChanger()).getTapChangerAttribute());
    }

    @Test
    void testTapChangerStepsReplacement() {
        Network network = NoEquipmentNetworkFactory.create();
        Substation substation = network.getSubstation("sub");

        // Create a TWT
        TwoWindingsTransformer twt2 = createTwoWindingsTransformer(substation);
        createRatioTapChanger(twt2);
        createPhaseTapChanger(twt2);

        testPhaseTapChangerStepsReplacement(twt2);

        testRatioTapChangerStepsReplacement(twt2);
    }

    private static void testPhaseTapChangerStepsReplacement(TwoWindingsTransformer twt2) {
        PhaseTapChanger ptc = twt2.getPhaseTapChanger();
        assertEquals(2, ptc.getStepCount());
        PhaseTapChangerStepsReplacer phaseStepsReplacer = ptc.stepsReplacer();
        assertEquals("2 windings transformer 'twt2': a tap changer shall have at least one step",
            assertThrows(ValidationException.class, phaseStepsReplacer::replaceSteps).getMessage());
        phaseStepsReplacer.beginStep()
            .endStep();
        assertEquals("2 windings transformer 'twt2': step alpha is not set",
            assertThrows(ValidationException.class, phaseStepsReplacer::replaceSteps).getMessage());
        phaseStepsReplacer = ptc.stepsReplacer();
        phaseStepsReplacer.beginStep()
            .setR(6.0)
            .setX(5.0)
            .setG(4.0)
            .setB(3.0)
            .setAlpha(2.0)
            .setRho(1.0)
            .endStep();
        ptc.setTapPosition(1);
        assertEquals("2 windings transformer 'twt2': incorrect tap position 1 [0, 0]",
            assertThrows(ValidationException.class, phaseStepsReplacer::replaceSteps).getMessage());
        ptc.setTapPosition(0);
        phaseStepsReplacer.replaceSteps();
        assertEquals(1, ptc.getStepCount());
        int phaseLowTapPosition = ptc.getLowTapPosition();
        assertEquals(6.0, ptc.getStep(phaseLowTapPosition).getR());
        assertEquals(5.0, ptc.getStep(phaseLowTapPosition).getX());
        assertEquals(4.0, ptc.getStep(phaseLowTapPosition).getG());
        assertEquals(3.0, ptc.getStep(phaseLowTapPosition).getB());
        assertEquals(2.0, ptc.getStep(phaseLowTapPosition).getAlpha());
        assertEquals(1.0, ptc.getStep(phaseLowTapPosition).getRho());
    }

    private static void testRatioTapChangerStepsReplacement(TwoWindingsTransformer twt2) {
        RatioTapChanger rtc = twt2.getRatioTapChanger();
        assertEquals(3, rtc.getStepCount());
        RatioTapChangerStepsReplacer ratioStepsReplacer = rtc.stepsReplacer();
        assertEquals("2 windings transformer 'twt2': a tap changer shall have at least one step",
            assertThrows(ValidationException.class, ratioStepsReplacer::replaceSteps).getMessage());
        ratioStepsReplacer.beginStep()
            .endStep();
        assertEquals("2 windings transformer 'twt2': step rho is not set",
            assertThrows(ValidationException.class, ratioStepsReplacer::replaceSteps).getMessage());
        ratioStepsReplacer = rtc.stepsReplacer();
        ratioStepsReplacer.beginStep()
            .setR(1.0)
            .setX(2.0)
            .setG(3.0)
            .setB(4.0)
            .setRho(5.0)
            .endStep()
            .beginStep()
            .setR(6.0)
            .setX(7.0)
            .setG(8.0)
            .setB(9.0)
            .setRho(10.0)
            .endStep();
        rtc.setTapPosition(2);
        assertEquals("2 windings transformer 'twt2': incorrect tap position 2 [0, 1]",
            assertThrows(ValidationException.class, ratioStepsReplacer::replaceSteps).getMessage());
        rtc.setTapPosition(0);
        ratioStepsReplacer.replaceSteps();
        assertEquals(2, rtc.getStepCount());
        int ratioLowTapPosition = rtc.getLowTapPosition();
        assertEquals(1.0, rtc.getStep(ratioLowTapPosition).getR());
        assertEquals(2.0, rtc.getStep(ratioLowTapPosition).getX());
        assertEquals(3.0, rtc.getStep(ratioLowTapPosition).getG());
        assertEquals(4.0, rtc.getStep(ratioLowTapPosition).getB());
        assertEquals(5.0, rtc.getStep(ratioLowTapPosition).getRho());
        assertEquals(6.0, rtc.getStep(ratioLowTapPosition + 1).getR());
        assertEquals(7.0, rtc.getStep(ratioLowTapPosition + 1).getX());
        assertEquals(8.0, rtc.getStep(ratioLowTapPosition + 1).getG());
        assertEquals(9.0, rtc.getStep(ratioLowTapPosition + 1).getB());
        assertEquals(10.0, rtc.getStep(ratioLowTapPosition + 1).getRho());
    }

    private ThreeWindingsTransformer createThreeWindingsTransformer(Substation substation) {
        return substation.newThreeWindingsTransformer()
            .setId("twt3")
            .setName("twt3_name")
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
    }

    private TwoWindingsTransformer createTwoWindingsTransformer(Substation substation) {
        return substation.newTwoWindingsTransformer()
            .setId("twt2")
            .setName("twt2_name")
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

    private void createPhaseTapChanger(PhaseTapChangerHolder ptch) {
        ptch.newPhaseTapChanger()
            .setTapPosition(1)
            .setLowTapPosition(0)
            .setRegulating(false)
            .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
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

    private void createRatioTapChanger(RatioTapChangerHolder rtch) {
        rtch.newRatioTapChanger()
            .setLowTapPosition(0)
            .setTapPosition(1)
            .setLoadTapChangingCapabilities(false)
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
