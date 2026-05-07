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
        // Using minPowerFactor = 1/sqrt(2) ~ 0.707 gives a clean maxAbsQ = |newP| * 1
        // (because sqrt(1/0.5 - 1) = sqrt(1) = 1)
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(1.0 / Math.sqrt(2));

        Load load = network.getLoad("l1");
        load.setQ0(200.0);
        assertEquals(100.0, load.getP0(), 1e-3);
        assertEquals(200.0, load.getQ0(), 1e-3);

        // Scale by 50 (GENERATOR convention): newP = 100 - 50 = 50
        // Proportional newQ = 50 * 200/100 = 100
        // maxAbsQ = 50 * sqrt(1/(1/sqrt(2))^2 - 1) = 50 * 1 = 50
        // |100| > 50 -> Q clamped to 50
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
    void testLoadMinPowerFactorNoClampNeeded() {
        // When the proportional Q already respects the power factor minimum, no clamping occurs.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(1.0 / Math.sqrt(2));

        Load load = network.getLoad("l1");
        load.setQ0(10.0);

        // Scale by 20: newP=80, proportional newQ = 80*10/100 = 8
        // maxAbsQ = 80 * 1 = 80; |8| < 80 -> no clamping, proportional value kept
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

        // Scale by 80 (GENERATOR): newP = 100 - 80 = 20
        // Proportional newQ = 20 * 100/100 = 20
        // Floor = 100 * 0.5 = 50; 20 < 50 -> Q clamped to 50
        ls1.scale(network, 80, parameters);
        assertEquals(20.0, load.getP0(), 1e-3);
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinQRateNoClampNeeded() {
        // When the proportional Q is already above the floor, no clamping occurs.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinQRate(0.5);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        // Scale by 20 (GENERATOR): newP = 80, proportional newQ = 80
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

        // Scale by -100 (GENERATOR): asked < 0 means P increases -> newP = 100 + 100 = 200
        // Proportional newQ = 200 * 20/100 = 40
        // Ceiling = 20 * 1.5 = 30; 40 > 30 -> Q clamped to 30
        ls1.scale(network, -100, parameters);
        assertEquals(200.0, load.getP0(), 1e-3);
        assertEquals(30.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMaxQRateNoClampNeeded() {
        // When the proportional Q is already below the ceiling, no clamping occurs.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMaxQRate(1.5);

        Load load = network.getLoad("l1");
        load.setQ0(20.0);

        // Scale by -20 (GENERATOR): newP = 120, proportional newQ = 24
        // Ceiling = 20 * 1.5 = 30; 24 <= 30 -> no clamping
        ls1.scale(network, -20, parameters);
        assertEquals(120.0, load.getP0(), 1e-3);
        assertEquals(24.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadScalableMinQ() {
        // Per-load absolute MVAr floor set directly on LoadScalable
        ScalingParameters parameters = new ScalingParameters().setConstantPowerFactor(true);

        Load load = network.getLoad("l1");
        load.setQ0(100.0);

        // Scale by 80: newP=20, proportional newQ=20
        // minQ=30 -> 20 < 30 -> Q clamped to 30
        ls1.setMinQ(30.0);
        ls1.scale(network, 80, parameters);
        assertEquals(20.0, load.getP0(), 1e-3);
        assertEquals(30.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadScalableMaxQ() {
        // Per-load absolute MVAr ceiling set directly on LoadScalable
        ScalingParameters parameters = new ScalingParameters().setConstantPowerFactor(true);

        Load load = network.getLoad("l1");
        load.setQ0(20.0);

        // Scale by -100: newP=200, proportional newQ=40
        // maxQ=35 -> 40 > 35 -> Q clamped to 35
        ls1.setMaxQ(35.0);
        ls1.scale(network, -100, parameters);
        assertEquals(200.0, load.getP0(), 1e-3);
        assertEquals(35.0, load.getQ0(), 1e-3);
    }

    @Test
    void testAllQConstraintsInteraction() {
        // When multiple constraints are active, the most restrictive one wins.
        // Here loadMinQRate gives a floor of 50, and minQ gives a harder floor of 60.
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinQRate(0.5); // floor = 100 * 0.5 = 50

        Load load = network.getLoad("l1");
        load.setQ0(100.0);
        ls1.setMinQ(60.0); // harder floor wins

        // Scale by 80: newP=20, proportional newQ=20 -> rate floor=50, absolute floor=60
        ls1.scale(network, 80, parameters);
        assertEquals(20.0, load.getP0(), 1e-3);
        assertEquals(60.0, load.getQ0(), 1e-3);
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

        // GENERATOR convention, asked < 0 -> P increases from 0
        ls1.scale(network, -20, parameters);
        // Q must be untouched because oldP0 was 0
        assertEquals(50.0, load.getQ0(), 1e-3);
    }

    @Test
    void testLoadMinPowerFactorInitialPFAboveMinimum() {
        // P_initial=1000, Q_initial=3000, P_scaled=2000, minPowerFactor=0.3
        // cosphi_initial = 1/sqrt(10) ~ 0.316 > 0.3 -> initial PF is already above minimum
        // -> proportional scaling applies, no clamping
        // newQ = 2000 * 3000/1000 = 6000 MVAr
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.3);

        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);

        // GENERATOR convention: asked < 0 increases P, asked=−1000 -> newP=2000
        ls1.scale(network, -1000, parameters);
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(6000.0, load.getQ0(), 1e-3); // purely proportional, no clamping
    }

    @Test
    void testLoadMinPowerFactorInitialPFBelowMinimum() {
        // Same setup, but minPowerFactor=0.5, which is above cosphi_initial (0.316)
        // -> Q must be clamped to the minPowerFactor limit
        // maxAbsQ = 2000 * sqrt(1/0.5² - 1) = 2000 * sqrt(3) ~ 3464.1 MVAr
        ScalingParameters parameters = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setLoadMinPowerFactor(0.5);

        Load load = network.getLoad("l1");
        load.setP0(1000.0);
        load.setQ0(3000.0);

        ls1.scale(network, -1000, parameters);
        assertEquals(2000.0, load.getP0(), 1e-3);
        assertEquals(2000 * Math.sqrt(3), load.getQ0(), 1e-3); // ~ 3464.1 MVAr
    }

    @Test
    void testLoadMaxQRateNoClamp() {
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
    void testLoadMaxQRateExampleWithClamp() {
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

}
