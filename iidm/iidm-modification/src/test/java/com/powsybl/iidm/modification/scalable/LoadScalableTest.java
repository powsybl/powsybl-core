/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention.GENERATOR;
import static com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention.LOAD;
import static com.powsybl.iidm.modification.scalable.ScalableTestNetwork.createNetwork;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 */
class LoadScalableTest {

    private Network network;
    private Scalable l1;
    private LoadScalable ls1;
    private Scalable l2;
    private Scalable l3;
    private Scalable l4;
    private ScalingConvention convention;

    @BeforeEach
    void setUp() {

        network = createNetwork();
        l1 = Scalable.onLoad("l1");
        ls1 = (LoadScalable) l1;

        l2 = new LoadScalable("l1", 110);
        l3 = new LoadScalable("l1", 20, 100);
        l4 = new LoadScalable("l1", -10, 100);

    }

    @Test
    void testConstructorThrowWhenIdIsNull() {
        assertThrows(NullPointerException.class, () -> new LoadScalable(null));
    }

    @Test
    void testConstructorInvalidP() {
        assertThrows(PowsyblException.class, () -> new LoadScalable("l1", 10, 0));
    }

    @Test
    void testInitialValue() {
        assertEquals(0, l1.initialValue(network), 1e-3);
    }

    @Test
    void testMaximumlValue() {
        assertEquals(Double.MAX_VALUE, l1.maximumValue(network, LOAD), 0.);
        assertEquals(-20, l3.maximumValue(network), 0.);
        assertEquals(-20, l3.maximumValue(network, GENERATOR), 0.);
        assertEquals(100, l3.maximumValue(network, LOAD), 0.);
    }

    @Test
    void testMinimumlValue() {
        assertEquals(0, l1.minimumValue(network, LOAD), 0.);
        assertEquals(-100, l3.minimumValue(network), 0.);
        assertEquals(-100, l3.minimumValue(network, GENERATOR), 0.);
        assertEquals(20, l3.minimumValue(network, LOAD), 0.);
    }

    @Test
    void testLoadScaleGeneratorConvention() {
        //test with ScalingConvention.GENERATOR
        //test with default maxValue = Double.MAX_VALUE and minValue = 0
        Load load = network.getLoad("l1");
        assertEquals(100, load.getP0(), 1e-3);
        assertEquals(20, l1.scale(network, 20), 1e-3);
        assertEquals(80, load.getP0(), 1e-3);
        assertEquals(-40, l1.scale(network, -40), 1e-3);
        assertEquals(120, load.getP0(), 1e-3);

        //test minValue = 0
        assertEquals(120, l1.scale(network, 140), 1e-3);
        assertEquals(0, load.getP0(), 1e-3);

        //test with a maximum value
        l2.reset(network);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(-40, l2.scale(network, -40), 1e-3);
        assertEquals(40, load.getP0(), 1e-3);
        assertEquals(-70, l2.scale(network, -80), 1e-3);
        assertEquals(110, load.getP0(), 1e-3);
        assertEquals(110, l2.scale(network, 120), 1e-3);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(-50, l2.scale(network, -50), 1e-3);
        assertEquals(50, load.getP0(), 1e-3);

        //test with minValue = 20
        assertEquals(100, l3.maximumValue(network, LOAD), 1e-3);
        assertEquals(20, l3.minimumValue(network, LOAD), 1e-3);
        assertEquals(50, load.getP0(), 1e-3);

        assertEquals(30, l3.scale(network, 50), 1e-3);
        assertEquals(20, load.getP0(), 1e-3);

        l3.reset(network);
        //test with p0 outside interval
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(0, l3.scale(network, -40), 1e-3);

        //test LoadScalable with negative minValue
        l4.reset(network);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(10, l4.scale(network, 20), 1e-3);
        assertEquals(-10, load.getP0(), 1e-3);

    }

    @Test
    void testLoadScaleLoadConvention() {

        //test with ScalingConvention.LOAD
        convention = LOAD;
        ScalingParameters parameters = new ScalingParameters().setScalingConvention(LOAD);

        //test with default maxValue = Double.MAX_VALUE and minValue = 0
        Load load = network.getLoad("l1");
        assertEquals(100, load.getP0(), 1e-3);
        assertEquals(20, l1.scale(network, 20, parameters), 1e-3);
        assertEquals(120, load.getP0(), 1e-3);
        assertEquals(-40, l1.scale(network, -40, parameters), 1e-3);
        assertEquals(80, load.getP0(), 1e-3);

        //test minValue = 0
        assertEquals(-80, l1.scale(network, -140, parameters), 1e-3);
        assertEquals(0, load.getP0(), 1e-3);

        //test with a maximum value
        l2.reset(network);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(0, l2.scale(network, -40, parameters), 1e-3);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(110, l2.scale(network, 120, parameters), 1e-3);
        assertEquals(110, load.getP0(), 1e-3);
        assertEquals(-80, l2.scale(network, -80, parameters), 1e-3);
        assertEquals(30, load.getP0(), 1e-3);

        //test with minValue = 20
        assertEquals(-10, l3.scale(network, -30, parameters), 1e-3);
        assertEquals(20, load.getP0(), 1e-3);

        l3.reset(network);
        //test with p0 outside interval
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(0, l3.scale(network, -40, parameters), 1e-3);

        //test LoadScalable with negative minValue
        l4.reset(network);
        assertEquals(0, load.getP0(), 1e-3);
        assertEquals(-10, l4.scale(network, -20, parameters), 1e-3);
        assertEquals(-10, load.getP0(), 1e-3);
    }

    @Test
    void testConstantPowerFactor() {
        //test with ScalingConvention.GENERATOR
        ScalingParameters parameters = new ScalingParameters().setConstantPowerFactor(true);

        //test with default maxValue = Double.MAX_VALUE and minValue = 0
        Load load = network.getLoad("l1");

        ls1.scale(network, 20, parameters);
        assertEquals(0.0, load.getQ0(), 1e-3);

        load.setQ0(10.0);
        assertEquals(10.0, load.getQ0(), 1e-3);
        ls1.scale(network, 20, parameters);
        assertEquals(60, load.getP0(), 1e-3);
        assertEquals(7.5, load.getQ0(), 1e-3);

        ls1.reset(network);
        assertEquals(0.0, load.getP0(), 1e-3);
        assertEquals(7.5, load.getQ0(), 1e-3);
        ls1.scale(network, -20, parameters);
        assertEquals(20.0, load.getP0(), 1e-3);
        assertEquals(7.5, load.getQ0(), 1e-3);
    }

    @Test
    void testReconnectLoad() {
        // test with ScalingConvention.GENERATOR
        Load load = network.getLoad("l1");
        load.getTerminal().disconnect();
        assertFalse(load.getTerminal().isConnected());

        ScalingParameters parameters = new ScalingParameters().setReconnect(true);

        ls1.scale(network, 20, parameters);
        assertTrue(load.getTerminal().isConnected());
        assertEquals(80.0, load.getP0(), 1e-3);

        // test with ScalingConvention.LOAD
        convention = LOAD;
        parameters.setScalingConvention(convention);
        load.getTerminal().disconnect();
        assertFalse(load.getTerminal().isConnected());
        ls1.scale(network, 20, parameters);
        assertTrue(load.getTerminal().isConnected());
        assertEquals(100.0, load.getP0(), 1e-3);

        // test with constant power factor
        load.getTerminal().disconnect();
        assertFalse(load.getTerminal().isConnected());
        load.setQ0(10.0);
        load.setP0(80.0);
        assertEquals(10.0, load.getQ0(), 1e-3);
        assertEquals(80.0, load.getP0(), 1e-3);
        parameters.setScalingConvention(GENERATOR).setConstantPowerFactor(true);
        ls1.scale(network, 20, parameters);
        assertTrue(load.getTerminal().isConnected());
        assertEquals(60.0, load.getP0(), 1e-3);
        assertEquals(7.5, load.getQ0(), 1e-3);

        // reconnect to false
        load.getTerminal().disconnect();
        assertFalse(load.getTerminal().isConnected());
        parameters.setReconnect(false);
        double scaleResult = ls1.scale(network, 20, parameters);
        assertFalse(load.getTerminal().isConnected());
        assertEquals(60.0, load.getP0(), 1e-3);
        assertEquals(0.0, scaleResult, 1e-3);
    }

    @Test
    void testLoadMinPowerFactor() {
        // loadMinPowerFactor clamps Q so the resulting power factor never drops below the minimum.
        // Using minPowerFactor = 0.7071 gives a maxQ = newP * 1
        // (because cos(tan(0.7071) = 1)
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.7071);

        Load load = network.getLoad("l1");
        load.setQ0(200.0);
        assertEquals(100.0, load.getP0(), 1e-3);
        assertEquals(200.0, load.getQ0(), 1e-3);

        // Scale by 50: newP = 100 - 50 = 50
        // Proportional newQ = 50 * 200/100 = ~ 100
        // maxQ = cos(tan(0.7071) * 50 = ~ 50
        // 100 > 50 -> Q clamped to 50
        ls1.scale(network, 50, parameters);
        assertEquals(50.0, load.getP0(), 1e-3);
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinPowerFactorNegativeQ() {
        // Same as above but with a capacitive load (negative Q).
        // The sign of Q must be preserved when clamping.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(1.0 / Math.sqrt(2));

        Load load = network.getLoad("l1");
        load.setQ0(-200.0);

        // Scale by 50: newP=50, proportional newQ=-100, clamped to -50 (sign preserved)
        ls1.scale(network, 50, parameters);
        assertEquals(50.0, load.getP0(), 1e-3);
        assertEquals(-50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinPowerFactorNoClamp() {
        // When the proportional Q already respects the power factor minimum, no clamping occurs.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.7071);

        Load load = network.getLoad("l1");
        load.setQ0(10.0);

        // Scale by 20: newP=80, proportional newQ = 80 * 10/100 = 8
        // maxQ = 80 * 1 = 80; 8 < 80 -> no clamping, proportional value kept
        ls1.scale(network, 20, parameters);
        assertEquals(80.0, load.getP0(), 1e-3);
        assertEquals(8.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinQRate() {
        // Q_scaled >= Q_initial * loadMinQRate
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinQRate(0.5);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        // Scale by 80: newP = 100 - 80 = 20
        // Proportional newQ = 20 * 100/100 = 20
        // Floor = 100 * 0.5 = 50; 20 < 50 -> Q clamped to 50
        ls1.scale(network, 80, parameters);
        assertEquals(20.0, load.getP0(), 1e-3);
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinQRateNegativeQ() {
        // For negative oldQ0, the minQRate bound keeps Q from becoming *less* negative
        // (from drifting toward zero) beyond the allowed rate.
        // minBound = oldQ0 * minQRate = -100 * 0.5 = -50
        // oldQ0 < 0 -> Math.min(newQ, minBound) ensures newQ <= -50
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinQRate(0.5);

        Load load = network.getLoad("l1");
        load.setQ0(-100.0);

        // Scale by 80: newP=20, proportional newQ = 20*(-100)/100 = -20
        // -20 > -50 -> Math.min(-20, -50) = -50 -> clamped
        ls1.scale(network, 80, parameters);
        assertEquals(20.0, load.getP0(), 1e-3);
        assertEquals(-50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinQRateNegativeQNoClampNeeded() {
        // When the proportional Q is already at or beyond the minQRate bound, no clamping occurs.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinQRate(0.5);

        Load load = network.getLoad("l1");
        load.setQ0(-100.0);

        // Scale by 20: newP=80, proportional newQ = 80*(-100)/100 = -80
        // minBound = -50; -80 < -50 -> Math.min(-80, -50) = -80 -> no clamping
        ls1.scale(network, 20, parameters);
        assertEquals(80.0, load.getP0(), 1e-3);
        assertEquals(-80.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinQRateNoClamp() {
        // When the proportional Q is already above the floor, no clamping occurs.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinQRate(0.5);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        // Scale by 20: newP = 80, proportional newQ = 80
        // Floor = 100 * 0.5 = 50; 80 >= 50 -> no clamping
        ls1.scale(network, 20, parameters);
        assertEquals(80.0, load.getP0(), 1e-3);
        assertEquals(80.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMaxQRate() {
        // Q_scaled <= Q_initial * loadMaxQRate
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMaxQRate(1.5);

        Load load = network.getLoad("l1");
        load.setQ0(20.0);

        // Scale by -100: asked < 0 means P increases -> newP = 100 + 100 = 200
        // Proportional newQ = 200 * 20/100 = 40
        // Ceiling = 20 * 1.5 = 30; 40 > 30 -> Q clamped to 30
        ls1.scale(network, -100, parameters);
        assertEquals(200.0, load.getP0(), 1e-3);
        assertEquals(30.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMaxQRateNoClamp() {
        // When the proportional Q is already below the ceiling, no clamping occurs.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMaxQRate(1.5);

        Load load = network.getLoad("l1");
        load.setQ0(20.0);

        // Scale by -20: newP = 120, proportional newQ = 24
        // Ceiling = 20 * 1.5 = 30; 24 <= 30 -> no clamping
        ls1.scale(network, -20, parameters);
        assertEquals(120.0, load.getP0(), 1e-3);
        assertEquals(24.0, load.getQ0(), 1e-3);
    }

    @Test
    void testNoQScalingWhenConstantPowerFactorFalse() {
        // When constantPowerFactor=false, Q is never touched regardless of Q parameters.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(false)
                .setLoadMinPowerFactor(0.99)
                .setLoadMinQRate(0.9)
                .setLoadMaxQRate(1.1);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        ls1.scale(network, 50, parameters);
        // Q must be completely untouched
        assertEquals(100.0, load.getQ0(), 1e-3);
    }

    @Test
    void testNoQScalingWhenOldP0IsZero() {
        // When oldP0=0, the constantPowerFactor guard (oldP0 != 0) prevents Q scaling.
        // None of the new Q parameters should apply either.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinQRate(0.5);

        Load load = network.getLoad("l1");
        load.setP0(0.0);
        load.setQ0(50.0);

        // asked < 0 -> P increases from 0
        ls1.scale(network, -20, parameters);
        // Q must be untouched because oldP0 was 0
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinPowerFactorInitialPFAboveMinimum() {
        // P_initial=1000, Q_initial=3000, P_scaled=2000, minPowerFactor=0.3
        // cosphi_initial = cos(atan(3000 / 1000)) ~ 0.316 > 0.3 -> initial PF is already above minimum
        // proportional scaling applies, no clamping
        // newQ = 2000 * 3000/1000 = 6000 MVAr
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.3);

        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);

        // asked < 0 increases P, asked=−1000 -> newP=2000
        ls1.scale(network, -1000, parameters);
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(6000.0, load.getQ0(), 1e-3); // purely proportional, no clamping
    }

    @Test
    void testLoadMinPowerFactorInitialPFBelowMinimum() {
        // Same setup, but minPowerFactor=0.5, which is above cosphi_initial (0.316)
        // -> Q must be clamped to the minPowerFactor limit
        // maxQ = 2000 * tan(acos(0.316)) ~ 3464.1 MVAr
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.5);

        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);

        ls1.scale(network, -1000, parameters);
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(2000 * Math.tan(Math.acos(0.5)), load.getQ0(), 1e-3); // ~ 3464.1 MVAr
    }

    @Test
    void testLoadMinPowerFactorLoadMaxQRateNoClamp() {
        // P_initial=1000, Q_initial=3000, P_scaled=2000
        // minPowerFactor=0.3 -> proportional Q=6000
        // loadMaxQRate=2.0 -> ceiling = 3000*2 = 6000; 6000 <= 6000 -> no clamping
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.3)
                .setLoadMaxQRate(2.0);

        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);

        ls1.scale(network, -1000, parameters);
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(6000.0, load.getQ0(), 1e-3); // exactly at ceiling, not clamped
    }

    @Test
    void testLoadMaxQRateWithClamp() {
        // loadMaxQRate=1.5 -> ceiling = 3000*1.5 = 4500; 6000 > 4500 -> clamped
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.3)
                .setLoadMaxQRate(1.5);

        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);

        ls1.scale(network, -1000, parameters);
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(4500.0, load.getQ0(), 1e-3); // 3000 * 1.5
    }

    private static Stream<Arguments> signChangeScalingCases() {
        return Stream.of(
                // Case 1: positive P -> negative P after scaling
                // P0=1000, asked=2000 -> newP = 1000 - 2000 = -1000
                // Q proportional: 3000 * (-1000 / 1000) = -3000
                // cosphi_initial = cos(atan(3000 / 1000)) ~ 0.316 > 0.3 -> no clamping
                Arguments.of("positivePToNegativeP",
                        1000.0, 3000.0,   // P0, Q0
                        2000.0,           // asked
                        -1000.0, -3000.0  // expectedP, expectedQ
                ),

                // Case 2: negative P -> positive P after scaling
                // P0=-1000, asked=-2000 -> newP = -1000 - (-2000) = 1000
                // Q proportional: -3000 * (1000 / -1000) = 3000
                // cosphi_initial = cos(atan(3000 / 1000)) ~ 0.316 > 0.3 -> no clamping
                Arguments.of("negativePToPositiveP",
                        -1000.0, -3000.0,
                        -2000.0,
                        1000.0, 3000.0
                ),

                // Case 3: negative P -> negative P after scaling (magnitude grows)
                // P0=-1000, asked=1000 -> newP = -1000 - 1000 = -2000
                // Q proportional: -3000 * (-2000 / -1000) = -6000
                // cosphi_initial = 2000 / sqrt(2000² + 6000²) ~ 0.316 > 0.3 -> no clamping
                Arguments.of("negativePStaysNegativeP",
                        -1000.0, -3000.0,
                        1000.0,
                        -2000.0, -6000.0
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("signChangeScalingCases")
    void testLoadMinPowerFactorSignChangesInP(
            String caseName,
            double p0, double q0,
            double asked,
            double expectedP, double expectedQ) {
        // minPowerFactor=0.3; initial cosphi_initial ~ 0.316 > 0.3 -> proportional scaling, no clamping.
        // Covers sign-change edge cases where P crosses zero or stays negative.
        // Use unconstrained bounds so negative P0 and negative target P are both accepted.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.3);

        Load load = network.getLoad("l1");
        load.setP0(p0);
        load.setQ0(q0);

        LoadScalable unconstrainedScalable = new LoadScalable("l1", -Double.MAX_VALUE, Double.MAX_VALUE);
        unconstrainedScalable.scale(network, asked, parameters);

        assertEquals(expectedP, load.getP0(), 1e-3);
        assertEquals(expectedQ, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMaxQRateNegativeQ() {
        // For negative oldQ0, the maxQRate bound keeps Q from becoming *more* negative
        // (from growing in magnitude) beyond the allowed rate.
        // maxBound = oldQ0 * maxQRate = -100 * 2.0 = -200
        // oldQ0 < 0 -> Math.max(newQ, maxBound) ensures newQ >= -200
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMaxQRate(2.0);

        Load load = network.getLoad("l1");
        load.setQ0(-100.0);

        // Scale by -200: newP=300, proportional newQ = 300*(-100)/100 = -300
        // maxBound = -200; Math.max(-300, -200) = -200 -> clamped
        ls1.scale(network, -200, parameters);
        assertEquals(300.0, load.getP0(), 1e-3);
        assertEquals(-200.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMaxQRateNegativeQNoClampNeeded() {
        // When the proportional Q is already within the maxQRate bound, no clamping occurs.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMaxQRate(2.0);

        Load load = network.getLoad("l1");
        load.setQ0(-100.0);

        // Scale by -20: newP=120, proportional newQ = 120*(-100)/100 = -120
        // maxBound = -200; Math.max(-120, -200) = -120 -> no clamping
        ls1.scale(network, -20, parameters);
        assertEquals(120.0, load.getP0(), 1e-3);
        assertEquals(-120.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinPowerFactorLoadConvention() {
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setScalingConvention(LOAD)
                .setLoadMinPowerFactor(0.7071);

        Load load = network.getLoad("l1");
        load.setQ0(200.0);

        // LOAD convention, asked=-50: newP = 100 - 50 = 50
        // proportional newQ = 50 * 200/100 = 100
        // maxQ = 50 * 1 = 50; 100 > 50 -> clamped to 50
        ls1.scale(network, -50, parameters);
        assertEquals(50.0, load.getP0(), 1e-3);
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinQRateLoadConvention() {
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setScalingConvention(LOAD)
                .setLoadMinQRate(0.5);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        // LOAD convention, asked=-80: newP = 100 - 80 = 20
        // proportional newQ = 20 * 100/100 = 20
        // floor = 100 * 0.5 = 50; 20 < 50 -> clamped to 50
        ls1.scale(network, -80, parameters);
        assertEquals(20.0, load.getP0(), 1e-3);
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMaxQRateLoadConvention() {
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setScalingConvention(LOAD)
                .setLoadMaxQRate(1.5);

        Load load = network.getLoad("l1");
        load.setQ0(20.0);

        // LOAD convention, asked=100: newP = 100 + 100 = 200
        // proportional newQ = 200 * 20/100 = 40
        // ceiling = 20 * 1.5 = 30; 40 > 30 -> clamped to 30
        ls1.scale(network, 100, parameters);
        assertEquals(200.0, load.getP0(), 1e-3);
        assertEquals(30.0, load.getQ0(), 1e-3);
    }

    @Test
    void testAllScalingParametersQConstraintsRateMoreRestrictive() {
        // loadMinPowerFactor caps newQ at newP * tan(acos(pf)).
        // loadMaxQRate caps newQ at oldQ0 * rate.
        // When the rate limit is more restrictive, is enforced.
        //
        // oldP0=100, oldQ0=100, scale by -100 (GENERATOR): newP=200
        // proportional newQ = 200
        // Limit1: minPF=cos(atan(2)) -> maxQ = 200 * 1 = 200; 200 <= 200 -> no PF clamping
        // Limit2: maxBound = 100 * 1.5 = 150; 200 > 150 -> clamped to 150 (rate is enforced)
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.7071)
                .setLoadMaxQRate(1.5);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        ls1.scale(network, -100, parameters);
        assertEquals(200.0, load.getP0(), 1e-3);
        assertEquals(150.0, load.getQ0(), 1e-3);
    }

    @Test
    void testAllScalingParametersQConstraintsPFMoreRestrictive() {
        // When the PF constraint is more restrictive than the rate limit, PF is enforced.
        //
        // oldP0=100, oldQ0=100, scale by -100: newP=200
        // proportional newQ = 200
        // Limit1: minPF=0.9 -> maxQ = 200 * tan(acos(0,9))  ~ 96.8; clamped to ~96.8
        // Limit2: ceiling = 100 * 1.5 = 150; 96.8 <= 150 -> no further clamping (PF is enforced)
        double minPF = 0.9;
        double expectedMaxQ = 200.0 * Math.tan(Math.acos(minPF)) ;
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(minPF)
                .setLoadMaxQRate(1.5);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        ls1.scale(network, -100, parameters);
        assertEquals(200.0, load.getP0(), 1e-3);
        assertEquals(expectedMaxQ, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinPowerFactorWhenScaledPIsZero() {
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.7071);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        // Scale by 100: newP = 100 - 100 = 0
        // proportional newQ = 0 * 100/100 = 0
        ls1.scale(network, 100, parameters);
        assertEquals(0.0, load.getP0(), 1e-3);
        assertEquals(0.0, load.getQ0(), 1e-3);
        assertFalse(Double.isNaN(load.getQ0()));
    }

    @Test
    void testLoadMinPowerFactorWhenScaledPIsZeroWithRateLimits() {
        // Same scenario but also with rate limits active.
        // newQ after proportional scaling is 0; Limit1 is skipped; Limit2 rate floor would be
        // oldQ0 * minQRate = 100 * 0.5 = 50, but newQ=0 is already below the floor
        // so it should be clamped up to 50. Verifies rate limits chaining.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.7071)
                .setLoadMinQRate(0.5);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        ls1.scale(network, 100, parameters);
        assertEquals(0.0, load.getP0(), 1e-3);
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

}
