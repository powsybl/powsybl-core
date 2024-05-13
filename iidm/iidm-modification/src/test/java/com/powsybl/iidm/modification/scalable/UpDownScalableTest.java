/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class UpDownScalableTest {
    private static final double EPSILON = 1e-3;

    @Test
    void checkWorksAsExpectedWhenGoingUp() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1");
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);

        assertEquals(0., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(100., testNetwork.getLoad("l1").getP0(), EPSILON);
        upDownScalable.scale(testNetwork, 10.);
        assertEquals(10., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(100., testNetwork.getLoad("l1").getP0(), EPSILON);
    }

    @Test
    void checkWorksAsExpectedWhenGoingDown() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1");
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);

        assertEquals(0., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(100., testNetwork.getLoad("l1").getP0(), EPSILON);
        upDownScalable.scale(testNetwork, -10.);
        assertEquals(0., testNetwork.getGenerator("g2").getTargetP(), EPSILON);
        assertEquals(110., testNetwork.getLoad("l1").getP0(), EPSILON);
    }

    @Test
    void checkComputesCorrectlyMinMaxAndInitialValues() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        testNetwork.getLoad("l1").getTerminal().setP(-100.);
        Scalable upScalable = Scalable.onGenerator("g2");
        Scalable downScalable = Scalable.onLoad("l1", 50, 200);
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);

        assertEquals(-100., upDownScalable.initialValue(testNetwork), EPSILON);
        assertEquals(-200., upDownScalable.minimumValue(testNetwork), EPSILON);
        assertEquals(0., upDownScalable.maximumValue(testNetwork), EPSILON);
        assertEquals(0., upDownScalable.minimumValue(testNetwork, Scalable.ScalingConvention.LOAD), EPSILON);
        assertEquals(200., upDownScalable.maximumValue(testNetwork, Scalable.ScalingConvention.LOAD), EPSILON);
    }

    @Test
    void checkInjectionFilteringWorksAsExpected() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.proportional(50, Scalable.onGenerator("g2"), 50, Scalable.onGenerator("unknown generator"));
        Scalable downScalable = Scalable.onLoad("l1", 50, 200);
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);

        List<Injection> foundInjections = new ArrayList<>();
        List<String> notFoundIds = new ArrayList<>();
        upDownScalable.filterInjections(testNetwork, foundInjections, notFoundIds);

        assertEquals(2, foundInjections.size());
        assertTrue(foundInjections.contains(testNetwork.getLoad("l1")));
        assertTrue(foundInjections.contains(testNetwork.getGenerator("g2")));
        assertEquals(1, notFoundIds.size());
        assertTrue(notFoundIds.contains("unknown generator"));
    }

    @Test
    void checkGetCurrentPowerInBothDirections() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.proportional(50, Scalable.onGenerator("g2"), 50, Scalable.onGenerator("unknown generator"));
        Scalable downScalable = Scalable.onLoad("l1", 50, 200);
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable);

        testNetwork.getGenerator("g2").setTargetP(32);
        double asked = 1;
        assertEquals(-32, upDownScalable.getSteadyStatePower(testNetwork, asked, Scalable.ScalingConvention.LOAD));
        assertEquals(32, upDownScalable.getSteadyStatePower(testNetwork, asked, Scalable.ScalingConvention.GENERATOR));

        testNetwork.getLoad("l1").setP0(42);
        asked = -1;
        assertEquals(42, upDownScalable.getSteadyStatePower(testNetwork, asked, Scalable.ScalingConvention.LOAD));
        assertEquals(-42, upDownScalable.getSteadyStatePower(testNetwork, asked, Scalable.ScalingConvention.GENERATOR));
    }

    @Test
    void testMaxValueBoundsScalingUpGenConvention() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.proportional(100, Scalable.onGenerator("g2"));
        Scalable downScalable = Scalable.onLoad("l1");

        double initialValueUp = testNetwork.getGenerator("g2").getTargetP();
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable, -Double.MAX_VALUE, initialValueUp + 35);

        ScalingParameters parameters = new ScalingParameters();
        parameters.setScalingConvention(Scalable.ScalingConvention.GENERATOR);

        double asked = 100;
        assertEquals(35, upDownScalable.scale(testNetwork, asked, parameters));
    }

    @Test
    void testMaxValueBoundsScalingDownLoadConvention() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.proportional(100, Scalable.onGenerator("g2"));
        Scalable downScalable = Scalable.onLoad("l1");

        double initialValueDown = -testNetwork.getLoad("l1").getP0();
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable, -Double.MAX_VALUE, initialValueDown + 35);

        ScalingParameters parameters = new ScalingParameters();
        parameters.setScalingConvention(Scalable.ScalingConvention.LOAD);

        double asked = -100;
        assertEquals(-35, upDownScalable.scale(testNetwork, asked, parameters));
    }

    @Test
    void testMinValueBoundsScalingDownGenConvention() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        Scalable upScalable = Scalable.proportional(100, Scalable.onGenerator("g2"));
        Scalable downScalable = Scalable.onLoad("l1");

        double initialValueDown = -testNetwork.getLoad("l1").getP0();
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable, initialValueDown - 35, Double.MAX_VALUE);

        ScalingParameters parameters = new ScalingParameters();
        parameters.setScalingConvention(Scalable.ScalingConvention.GENERATOR);

        double asked = -100;
        assertEquals(-35, upDownScalable.scale(testNetwork, asked, parameters));
    }

    @Test
    void testMinValueBoundsScalingUpLoadConvention() {
        Network testNetwork = ScalableTestNetwork.createNetwork();
        testNetwork.getGenerator("g2").setTargetP(50);

        Scalable upScalable = Scalable.proportional(100, Scalable.onGenerator("g2"));
        Scalable downScalable = Scalable.onLoad("l1");

        double initialValueUp = testNetwork.getGenerator("g2").getTargetP();
        Scalable upDownScalable = Scalable.upDown(upScalable, downScalable, initialValueUp - 35, Double.MAX_VALUE);

        ScalingParameters parameters = new ScalingParameters();
        parameters.setScalingConvention(Scalable.ScalingConvention.LOAD);

        double asked = 100;
        assertEquals(35, upDownScalable.scale(testNetwork, asked, parameters));
    }
}
