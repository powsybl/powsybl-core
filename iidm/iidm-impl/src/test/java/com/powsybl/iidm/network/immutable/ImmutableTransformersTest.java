/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.powsybl.iidm.network.immutable.ImmutableTestHelper.RXGB_SETTERS;
import static com.powsybl.iidm.network.immutable.ImmutableTestHelper.testInvalidMethods;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableTransformersTest {

    @Test
    public void testTwoWindingsTransfo() {
        Network network = ImmutableNetwork.of(EurostagTutorialExample1Factory.create());

        TwoWindingsTransformer trans2 = network.getTwoWindingsTransformer("NHV2_NLOAD");
        assertTrue(trans2.getSubstation() instanceof ImmutableSubstation);
        Set<String> invalidMethods2wt = new HashSet<>();
        invalidMethods2wt.add("setR");
        invalidMethods2wt.add("setX");
        invalidMethods2wt.add("setG");
        invalidMethods2wt.add("setB");
        invalidMethods2wt.add("setRatedU1");
        invalidMethods2wt.add("setRatedU2");
        invalidMethods2wt.add("newRatioTapChanger");
        invalidMethods2wt.add("newPhaseTapChanger");
        invalidMethods2wt.add("newCurrentLimits1");
        invalidMethods2wt.add("newCurrentLimits2");
        invalidMethods2wt.add("remove");
        testInvalidMethods(trans2, invalidMethods2wt);
    }

    @Test
    public void testRatioTapChanger() {
        Network network = ImmutableNetwork.of(EurostagTutorialExample1Factory.create());
        TwoWindingsTransformer trans2 = network.getTwoWindingsTransformer("NHV2_NLOAD");
        RatioTapChanger rtc = trans2.getRatioTapChanger();
        assertTrue(rtc instanceof ImmutableRatioTapChanger);
        assertEquals(3, rtc.getStepCount());
        Set<String> expectedRtcInvalidMethods = new HashSet<>();
        // TapChanger
        expectedRtcInvalidMethods.add("setTapPosition");
        expectedRtcInvalidMethods.add("setRegulating");
        expectedRtcInvalidMethods.add("setRegulationTerminal");
        expectedRtcInvalidMethods.add("remove");
        // RatioTapChanger
        expectedRtcInvalidMethods.add("setTargetV");
        testInvalidMethods(rtc, expectedRtcInvalidMethods);

        RatioTapChangerStep tap = rtc.getCurrentStep();
        assertTrue(tap instanceof ImmutableRatioTapChanger.ImmutableRatioTapChangerStep);
        Set<String> expectedTapInvalidMethods = expectedTapInvalidMethods();
        testInvalidMethods(tap, expectedTapInvalidMethods);
    }

    private static Set<String> expectedTapInvalidMethods() {
        Set<String> expectedTapInvalidMethods = new HashSet<>(RXGB_SETTERS);
        expectedTapInvalidMethods.add("setRho");
        return expectedTapInvalidMethods;
    }

    @Test
    public void testPhaseTapChanger() {
        Network network = ImmutableNetwork.of(PhaseShifterTestCaseFactory.create());
        PhaseTapChanger ptc = network.getTwoWindingsTransformer("PS1").getPhaseTapChanger();
        assertTrue(ptc instanceof ImmutablePhaseTapChanger);
        Set<String> expectedPtcInvalidMethods = new HashSet<>();
        // TapChanger
        expectedPtcInvalidMethods.add("setTapPosition");
        expectedPtcInvalidMethods.add("setRegulating");
        expectedPtcInvalidMethods.add("setRegulationTerminal");
        expectedPtcInvalidMethods.add("remove");
        // PhaseTapChanger
        expectedPtcInvalidMethods.add("setRegulationMode");
        expectedPtcInvalidMethods.add("setRegulationValue");
        testInvalidMethods(ptc, expectedPtcInvalidMethods);

        PhaseTapChangerStep tap = ptc.getCurrentStep();
        assertTrue(tap instanceof ImmutablePhaseTapChanger.ImmutablePhaseTapChangerStep);
        Set<String> expectedTapInvalidMethods = new HashSet<>(expectedTapInvalidMethods());
        expectedTapInvalidMethods.add("setAlpha");
        testInvalidMethods(tap, expectedTapInvalidMethods);
    }

    @Test
    public void testThreeWindingsTransfo() {
        Network network = ImmutableNetwork.of(ThreeWindingsTransformerNetworkFactory.createWithCurrentLimits());
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        assertTrue(twt instanceof ImmutableThreeWindingsTransformer);

        Set<String> expectedTwtInvalidsMethods = new HashSet<>();
        expectedTwtInvalidsMethods.add("remove");
        testInvalidMethods(twt, expectedTwtInvalidsMethods);

        // leg1
        ThreeWindingsTransformer.Leg1 leg1 = twt.getLeg1();
        assertNotNull(leg1);
        Set<String> expectedLeg1InvaldMethods = new HashSet<>(RXGB_SETTERS);
        expectedLeg1InvaldMethods.add("setRatedU");
        expectedLeg1InvaldMethods.add("newCurrentLimits");
        testInvalidMethods(leg1, expectedLeg1InvaldMethods);

        // leg2or3
        ThreeWindingsTransformer.Leg2or3 leg2 = twt.getLeg2();
        Set<String> expectedLeg23InvalidMethods = new HashSet<>();
        expectedLeg23InvalidMethods.add("setR");
        expectedLeg23InvalidMethods.add("setX");
        expectedLeg23InvalidMethods.add("setRatedU");
        expectedLeg23InvalidMethods.add("newCurrentLimits");
        expectedLeg23InvalidMethods.add("newRatioTapChanger");
        testInvalidMethods(leg2, expectedLeg23InvalidMethods);
    }

}
