/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Clement Philipot {@literal <clement.philipot at rte-france.com>}
 */

class TapChangerConformityConversionTest {

    private static final String RESOURCE_DIR = "/issues/tap-changer/";
    private static Network network;

    @BeforeAll
    static void setUp() {
        network = readCgmesResources(RESOURCE_DIR, "tap_changers_EQ.xml", "tap_changers_SSH.xml");
    }

    @Test
    void tabularTapChangerSteps() {
        RatioTapChanger rtc = network.getTwoWindingsTransformer("PT1").getRatioTapChanger();
        assertEquals(6, rtc.getStepCount());
        assertEquals(1.0, rtc.getStep(3).getRho(), 0.0);
        assertEquals(1.0, rtc.getStep(4).getRho(), 0.0);
        assertEquals(0.0, rtc.getStep(5).getR(), 1e-12);
        assertEquals(0.0, rtc.getStep(6).getX(), 1e-12);

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("PT4").getPhaseTapChanger();
        assertEquals(4, ptc.getStepCount());
        assertEquals(0.0, ptc.getStep(1).getR(), 1e-12);
        assertEquals(0.0, ptc.getStep(1).getX(), 1e-12);
        for (int step = 1; step <= ptc.getStepCount(); step++) {
            assertEquals(1.0, ptc.getStep(step).getRho(), 1e-12);
        }
    }

    @Test
    void linearPhaseTapChangerSteps() {
        PhaseTapChanger ptc = network.getTwoWindingsTransformer("PT7").getPhaseTapChanger();

        assertEquals(25, ptc.getStepCount());
        for (int step = 1; step <= ptc.getStepCount(); step++) {
            assertEquals(1.0, ptc.getStep(step).getRho(), 0.0);
            assertEquals(0.0, ptc.getStep(step).getR(), 0.0);
            assertEquals(-87.517240, ptc.getStep(step).getX(), 0.000001);
            assertEquals(0.0, ptc.getStep(step).getG(), 0.0);
            assertEquals(0.0, ptc.getStep(step).getB(), 0.0);
        }

        assertEquals(14.4, ptc.getStep(1).getAlpha(), 0.001);
        assertEquals(8.4, ptc.getStep(6).getAlpha(), 0.001);
        assertEquals(2.4, ptc.getStep(11).getAlpha(), 0.001);
        assertEquals(0.0, ptc.getStep(13).getAlpha(), 0.0);
        assertEquals(-1.2, ptc.getStep(14).getAlpha(), 0.001);
        assertEquals(-7.2, ptc.getStep(19).getAlpha(), 0.001);
        assertEquals(-14.4, ptc.getStep(25).getAlpha(), 0.001);
    }

    @Test
    void faultyTabularTapChangerStepsFallBackToLinear() {
        RatioTapChanger rtc = network.getTwoWindingsTransformer("PT2").getRatioTapChanger();
        assertEquals(6, rtc.getStepCount());
        int neutralStep = 4;
        double stepVoltageIncrement = 1.25;
        for (int step = 1; step <= rtc.getStepCount(); step++) {
            assertEquals(0.0, rtc.getStep(step).getR(), 0.0);
            assertEquals(0.0, rtc.getStep(step).getX(), 0.0);
            assertEquals(0.0, rtc.getStep(step).getG(), 0.0);
            assertEquals(0.0, rtc.getStep(step).getB(), 0.0);
            assertEquals(1 / (1.0 + (step - neutralStep) * (stepVoltageIncrement / 100.0)), rtc.getStep(step).getRho(), 0.0);
        }

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("PT3").getPhaseTapChanger();
        assertEquals(5, ptc.getStepCount());
        for (int step = 1; step <= ptc.getStepCount(); step++) {
            assertEquals(0.0, ptc.getStep(step).getR(), 0.0);
            assertEquals(0.0, ptc.getStep(step).getX(), 0.0);
            assertEquals(0.0, ptc.getStep(step).getG(), 0.0);
            assertEquals(0.0, ptc.getStep(step).getB(), 0.0);
            assertEquals(1.0, ptc.getStep(step).getRho(), 0.0);
            assertEquals(0.0, ptc.getStep(step).getAlpha(), 0.0);
        }
    }

    @Test
    void phaseTapChangerRegulationIsOnSecondTransformerSide() {
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("PT4");

        assertSame(transformer.getTerminal2(), transformer.getPhaseTapChanger().getRegulationTerminal());
    }

    @Test
    void tapChangerControlDisabledInSsh() {
        assertFalse(network.getTwoWindingsTransformer("PT5").getRatioTapChanger().isRegulating());
        assertFalse(network.getTwoWindingsTransformer("PT6").getPhaseTapChanger().isRegulating());
    }

    @Test
    void phaseTapChangerCurrentLimiterControl() {
        PhaseTapChanger ptc = network.getTwoWindingsTransformer("PT4").getPhaseTapChanger();

        assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, ptc.getRegulationMode());
        assertEquals(65.0, ptc.getRegulationValue(), 0.0);
    }
}
