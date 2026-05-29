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
import org.junit.jupiter.params.provider.CsvSource;
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

    private static final double INV_SQRT2 = 1.0 / Math.sqrt(2);

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

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = {"null"}, textBlock = """
             p0,   q0,   newP, minPowerFactor, expectedQ, comment
             10.,  20.,  5.,   null,           10.0,      'No minPowerFactor, positive P & Q'
             10.,  20.,  5.,   0.4,            10.0,      'MinPowerFactor too low, positive P & Q'
             10.,  20.,  5.,   0.7071,         5.0,       'MinPowerFactor applied, positive P & Q'
             -10., 20.,  5.,   null,           -10.0,     'No minPowerFactor, negative to positive P & positive Q'
             -10., 20.,  5.,   0.4,            -10.0,     'MinPowerFactor too low, negative to positive P & positive Q'
             -10., 20.,  5.,   0.7071,         -5.0,      'MinPowerFactor applied, negative to positive P & positive Q'
             -10., 20.,  -5.,  0.7071,         5.0,       'MinPowerFactor applied, negative P & positive Q'
             10.,  -20., 5.,   0.7071,         -5.0,      'MinPowerFactor applied, positive P & negative Q'
            """
            // With P0 = +/-10 & Q0 = +/-20, we have cosphi_initial = cos(atan(20 / 10)) = 0,447
            // Any minPowerFactor below this cosphi_initial will not limit Q scaling for this load => Q will be scaled proportionally (as it would with no minPowerFactor)
            // A minPowerFactor of 0.7071 is above this cosphi_initial, so it should limit Q_scaled abs value to tan(acos(0.7071)) * newP ~= 1 * newP
            // The sign of Q_scaled depends on the sign that Q would have had if scaled proportionally
    )
    void testMinPowerFactor(double p0, double q0, double newP, Double minPowerFactor, double expectedQ, String comment) {
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setScalingConvention(LOAD); // no impact on Q scaling, just on P scaling
        if (minPowerFactor != null) {
            params.setLoadMinPowerFactor(minPowerFactor);
        }
        Load load = network.getLoad("l1");
        load.setP0(p0).setQ0(q0);
        double askedDelta = newP - p0; // with load convention: asked < 0 reduces P
        l4.scale(network, askedDelta, params);
        assertEquals(newP, load.getP0(), 1e-3);
        assertEquals(expectedQ, load.getQ0(), 1e-3);
    }

    @Test
    void testMinPowerFactorOneClampedToZero() {
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(1.0);
        Load load = network.getLoad("l1");
        load.setQ0(100.0);
        ls1.scale(network, 20, params); // newP = 80
        assertEquals(80.0, load.getP0(), 1e-3);
        assertEquals(0.0, load.getQ0(), 1e-3);
        assertFalse(Double.isNaN(load.getQ0()));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = {"null"}, textBlock = """
             p0,   q0,    newP, minQRate, expectedQ, comment
             10.0, 10.0,  2.0,  0.5,     5.0,       'MinQRate applied, positive Q'
             10.0, -10.0, 2.0,  0.5,     -5.0,      'MinQRate applied, negative Q'
             10.0, 10.0,  8.0,  0.5,     8.0,       'MinQRate, positive Q already above limit'
             10.0, -10.0, 8.0,  0.5,     -8.0,      'MinQRate, negative Q already below limit'
             10.0, 10.0,  2.0,  null,    2.0,        'no minQRate, Q scales proportionally'
             10.0, -10.0, 2.0,  null,    -2.0,       'no minQRate, negative Q scales proportionally'
            """
            // With P0 = 10 & Q0 = +/-10 and minQRate = 0.5, we have minQ = q0 * 0.5 = +/-5
            // Q scales proportionally to P: newQ = newP * q0 / p0
            // minQRate sets a floor on the absolute value of Q: scaled Q cannot be closer to 0 than minQ
            // If the proportional newQ would fall between 0 and minQ, it is limited to minQ instead
            // Examples (positive Q):
            //      newP = 2: proportional Q = 2 * 10 / 10 = 2, below limit of 5 => limited to 5
            //      newP = 8: proportional Q = 8 * 10 / 10 = 8, above limit of 5 => not limited
            // Examples (negative Q):
            //      newP = 2: proportional Q = 2 * -10 / 10 = -2, above limit of -5 => limited to -5
            //      newP = 8: proportional Q = 8 * -10 / 10 = -8, below limit of -5 => not limited
    )
    void testMinQRate(double p0, double q0, double newP, Double minQRate, double expectedQ, String comment) {
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setScalingConvention(LOAD); // no impact on Q scaling, just on P scaling
        if (minQRate != null) {
            params.setLoadMinQRate(minQRate);
        }
        Load load = network.getLoad("l1");
        load.setP0(p0).setQ0(q0);
        double askedDelta = newP - p0;
        ls1.scale(network, askedDelta, params);
        assertEquals(newP, load.getP0(), 1e-3);
        assertEquals(expectedQ, load.getQ0(), 1e-3);
    }

    private static Stream<Arguments> maxQRateCases() {
        // P0=100; ceiling = q0 * rate
        return Stream.of(
                // asked=-100 -> newP=200; proportional Q=40, ceiling=30 -> clamped
                Arguments.of("positive Q clamped to ceiling", 20.0, -100, 1.5, GENERATOR, 200.0, 30.0),
                // asked=-20 -> newP=120; proportional Q=24, ceiling=30 -> no clamp
                Arguments.of("no clamp, positive Q below ceiling", 20.0, -20, 1.5, GENERATOR, 120.0, 24.0),
                // asked=-200 -> newP=300; proportional Q=-300, ceiling=-200 -> clamped
                Arguments.of("negative Q clamped to ceiling", -100.0, -200, 2.0, GENERATOR, 300.0, -200.0),
                // asked=-20 -> newP=120; proportional Q=-120, ceiling=-200 -> no clamp
                Arguments.of("no clamp, negative Q within ceiling", -100.0, -20, 2.0, GENERATOR, 120.0, -120.0),
                // LOAD convention: asked=100 increases P to 200; proportional Q=40, ceiling=30 -> clamped
                Arguments.of("positive Q clamped to ceiling (LOAD)", 20.0, 100, 1.5, LOAD, 200.0, 30.0)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("maxQRateCases")
    void testMaxQRate(String name, double q0, double asked, double rate, ScalingConvention convention,
                      double expectedP, double expectedQ) {
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setScalingConvention(convention)
                .setLoadMaxQRate(rate);
        Load load = network.getLoad("l1");
        load.setQ0(q0);
        ls1.scale(network, asked, params);
        assertEquals(expectedP, load.getP0(), 1e-3);
        assertEquals(expectedQ, load.getQ0(), 1e-3);
    }

    @Test
    void testNoQScalingWhenConstantPowerFactorFalse() {
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(false)
                .setLoadMinPowerFactor(0.99)
                .setLoadMinQRate(0.9)
                .setLoadMaxQRate(1.1);
        Load load = network.getLoad("l1");
        load.setQ0(100.0);
        ls1.scale(network, 50, params);
        assertEquals(100.0, load.getQ0(), 1e-3);
    }

    @Test
    void testNoQScalingWhenOldP0IsZero() {
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinQRate(0.5);
        Load load = network.getLoad("l1");
        load.setP0(0.0);
        load.setQ0(50.0);
        ls1.scale(network, -20, params);
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testInitialPFAboveMinimumProportionalScaling() {
        // cosphi_initial ~ 0.316 > 0.3 -> proportional scaling, no clamp
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.3);
        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);
        ls1.scale(network, -1000, params); // newP=2000
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(6000.0, load.getQ0(), 1e-3);
    }

    @Test
    void testInitialPFBelowMinimumClampedToMinPF() {
        // cosphi_initial ~ 0.316 < 0.5 -> Q clamped to newP * tan(acos(0.5))
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.5);
        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);
        ls1.scale(network, -1000, params); // newP=2000
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(2000.0 * Math.tan(Math.acos(0.5)), load.getQ0(), 1e-3);
    }

    @Test
    void testRateLimitMoreRestrictiveThanPF() {
        // P0=100, Q0=100 throughout; asked=-100 -> newP=200, proportional Q=200
        // PF(INV_SQRT2) allows maxQ=200; rate ceiling = 100 * 1.5 = 150 -> rate enforced
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(INV_SQRT2)
                .setLoadMaxQRate(1.5);
        Load load = network.getLoad("l1");
        load.setQ0(100.0);
        ls1.scale(network, -100, params);
        assertEquals(200.0, load.getP0(), 1e-3);
        assertEquals(150.0, load.getQ0(), 1e-3);
    }

    @Test
    void testPFMoreRestrictiveThanRateLimit() {
        // P0=100, Q0=100 throughout; asked=-100 -> newP=200, proportional Q=200
        // PF(0.9) caps at 200*tan(acos(0.9)) ~ 96.8; rate ceiling = 150 -> PF enforced
        double minPF = 0.9;
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(minPF)
                .setLoadMaxQRate(1.5);
        Load load = network.getLoad("l1");
        load.setQ0(100.0);
        ls1.scale(network, -100, params);
        assertEquals(200.0, load.getP0(), 1e-3);
        assertEquals(200.0 * Math.tan(Math.acos(minPF)), load.getQ0(), 1e-3);
    }

    @Test
    void testCombinedConstraintsExactlyAtRateCeiling() {
        // P0=1000, Q0=3000; PF(0.3) -> cosphi_initial ~ 0.316 > 0.3; asked=-1000 -> newP=2000, proportional Q=6000
        // Rate ceiling = 3000 * 2.0 = 6000; proportional Q = 6000 -> no clamp
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.3)
                .setLoadMaxQRate(2.0);
        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);
        ls1.scale(network, -1000, params);
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(6000.0, load.getQ0(), 1e-3);
    }

    @Test
    void testCombinedConstraintsRateCeilingClampsProportionalQ() {
        // P0=1000, Q0=3000; PF(0.3) -> cosphi_initial ~ 0.316 > 0.3; asked=-1000 -> newP=2000, proportional Q=6000
        // Rate ceiling = 3000 * 1.5 = 4500; proportional Q = 6000 -> clamped to 4500
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.3)
                .setLoadMaxQRate(1.5);
        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);
        ls1.scale(network, -1000, params);
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(4500.0, load.getQ0(), 1e-3);
    }

    @Test
    void testScaledPIsZeroQIsZeroAndNotNaN() {
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(INV_SQRT2);
        Load load = network.getLoad("l1");
        load.setQ0(100.0);
        ls1.scale(network, 100, params); // newP = 0
        assertEquals(0.0, load.getP0(), 1e-3);
        assertEquals(0.0, load.getQ0(), 1e-3);
        assertFalse(Double.isNaN(load.getQ0()));
    }

    @Test
    void testScaledPIsZeroMinQRateFloorStillApplied() {
        // newQ=0 after proportional scaling; floor = 100 * 0.5 = 50 -> Q clamped to 50
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(INV_SQRT2)
                .setLoadMinQRate(0.5);
        Load load = network.getLoad("l1");
        load.setQ0(100.0);
        ls1.scale(network, 100, params); // newP = 0
        assertEquals(0.0, load.getP0(), 1e-3);
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

    private static Stream<Arguments> signChangeScalingCases() {
        return Stream.of(
                // cosphi_initial ~ 0.316 > 0.3 -> proportional scaling, no clamp
                Arguments.of("positive P to negative P", 1000.0, 3000.0, 2000.0, 0.3, -1000.0, -3000.0),
                Arguments.of("negative P to positive P", -1000.0, -3000.0, -2000.0, 0.3, 1000.0, 3000.0),
                Arguments.of("negative P grows in magnitude", -1000.0, -3000.0, 1000.0, 0.3, -2000.0, -6000.0),

                // cosphi_initial ~ 0.316 < 0.5 -> clamp; limitedQ = tan(acos(0.5)) * |newP| * signum(newQ)
                // tan(acos(0.5)) = tan(60°) = sqrt(3) ~ 1.732
                Arguments.of("positive P to negative P, clamped", 1000.0, 3000.0, 2000.0, 0.5, -1000.0, -Math.tan(Math.acos(0.5)) * 1000),
                Arguments.of("negative P to positive P, clamped", -1000.0, -3000.0, -2000.0, 0.5, 1000.0, Math.tan(Math.acos(0.5)) * 1000),
                Arguments.of("negative P grows in magnitude, clamped", -1000.0, -3000.0, 1000.0, 0.5, -2000.0, -Math.tan(Math.acos(0.5)) * 2000),

                // Opposite-sign P and Q, P crosses zero
                // P0=+1000, Q0=-3000 -> newP=-1000; newQ(proportional)=+3000 (Q flips sign)
                Arguments.of("positive P to negative P, Q flips sign, no clamp", 1000.0, -3000.0, 2000.0, 0.3, -1000.0, 3000.0),
                Arguments.of("positive P to negative P, Q flips sign, clamped", 1000.0, -3000.0, 2000.0, 0.5, -1000.0, Math.tan(Math.acos(0.5)) * 1000),

                // P0=-1000, Q0=+3000 -> newP=+1000; newQ(proportional)=-3000 (Q flips sign)
                Arguments.of("negative P to positive P, Q flips sign, no clamp", -1000.0, 3000.0, -2000.0, 0.3, 1000.0, -3000.0),
                Arguments.of("negative P to positive P, Q flips sign, clamped", -1000.0, 3000.0, -2000.0, 0.5, 1000.0, -Math.tan(Math.acos(0.5)) * 1000)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("signChangeScalingCases")
    void testMinPowerFactorWithSignChangesInP(String caseName,
                                              double p0, double q0, double asked, double loadMinPowerFactor, double expectedP, double expectedQ) {
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(loadMinPowerFactor);
        Load load = network.getLoad("l1");
        load.setP0(p0);
        load.setQ0(q0);
        // Unconstrained bounds so negative P and negative target P are accepted
        new LoadScalable("l1", -Double.MAX_VALUE, Double.MAX_VALUE).scale(network, asked, params);
        assertEquals(expectedP, load.getP0(), 1e-3);
        assertEquals(expectedQ, load.getQ0(), 1e-3);
    }

}
