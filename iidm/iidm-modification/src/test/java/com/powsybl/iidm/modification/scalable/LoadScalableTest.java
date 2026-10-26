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
import org.junit.jupiter.params.provider.CsvSource;

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

    // With P0 = +/-10 & Q0 = +/-20, we have cosphi_initial = cos(atan(20 / 10)) = 0,447
    // Any minPowerFactor below this cosphi_initial will not limit Q scaling for this load => Q will be scaled proportionally (as it would with no minPowerFactor)
    // A minPowerFactor of 0.7071 is above this cosphi_initial, so it should limit Q_scaled abs value to tan(acos(0.7071)) * newP ~= 1 * newP
    // The sign of Q_scaled depends on the sign that Q would have had if scaled proportionally
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
             10.,  20.,  0.,   0.7071,         0.0,       'MinPowerFactor applied, newP=0 => newQ=0'
             10.,  10.,  8.,   1.0,            0.0,       'minPowerFactor 1.0 forces Q to zero'
            """
    )
    void testMinPowerFactor(double p0, double q0, double newP, Double minPowerFactor, double expectedQ, String comment) {
        testScalingReactivePower(p0, q0, newP, minPowerFactor, null, null, expectedQ);
    }

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
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = {"null"}, textBlock = """
             p0,   q0,    newP, minQRate, expectedQ, comment
             10.,  10.,   2.,   null,     2.0,       'no minQRate, Q scales proportionally'
             10.,  -10.,  2.,   null,     -2.0,      'no minQRate, negative Q scales proportionally'
             10.,  10.,   2.,   0.5,      5.0,       'MinQRate applied, positive Q'
             10.,  -10.,  2.,   0.5,      -5.0,      'MinQRate applied, negative Q'
             10.,  10.,   8.,   0.5,      8.0,       'MinQRate, positive Q already above limit'
             10.,  -10.,  8.,   0.5,      -8.0,      'MinQRate, negative Q already below limit'
             -10., 10.,   8.,   0.5,      5.0,       'MinQRate applied, blocks Q sign change (5 instead of -8)'
            """
    )
    void testMinQRate(double p0, double q0, double newP, Double minQRate, double expectedQ, String comment) {
        testScalingReactivePower(p0, q0, newP, null, minQRate, null, expectedQ);
    }

    // With P0 = 10 & Q0 = +/-10 and maxQRate = 1.5, we have maxQ = q0 * 1.5 = +/-15
    // Q scales proportionally to P: newQ = newP * q0 / p0
    // maxQRate sets a ceiling on the absolute value of Q: scaled Q cannot be further from 0 than maxQ
    // If the proportional newQ would exceed maxQ, it is limited to maxQ instead
    // Examples (positive Q):
    //      newP = 20: proportional Q = 20 * 10 / 10 = 20, above limit of 15 => limited to 15
    //      newP = 12: proportional Q = 12 * 10 / 10 = 12, below limit of 15 => not limited
    // Examples (negative Q):
    //      newP = 20: proportional Q = 20 * -10 / 10 = -20, below limit of -15 => limited to -15
    //      newP = 12: proportional Q = 12 * -10 / 10 = -12, above limit of -15 => not limited
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = {"null"}, textBlock = """
         q0,    newP,  maxQRate, expectedQ, comment
         10.,   20.,   null,     20.0,      'no maxQRate, Q scales proportionally'
         -10.,  20.,   null,     -20.0,     'no maxQRate, negative Q scales proportionally'
         10.,   20.,   1.5,      15.0,      'MaxQRate applied, positive Q'
         -10.,  20.,   1.5,      -15.0,     'MaxQRate applied, negative Q'
         10.,   12.,   1.5,      12.0,      'MaxQRate, positive Q already below limit'
         -10.,  12.,   1.5,      -12.0,     'MaxQRate, negative Q already above limit'
        """
    )
    void testMaxQRate(double q0, double newP, Double maxQRate, double expectedQ, String comment) {
        testScalingReactivePower(10.0, q0, newP, null, null, maxQRate, expectedQ);
    }

    // With P0 = 10, Q0 = +/-10, we have cosphi_initial = cos(atan(10 / 10)) = 0,7071
    // Any minPowerFactor below this cosphi_initial will not limit Q scaling for this load => Q will be scaled proportionally (as it would with no minPowerFactor)
    // A minPowerFactor of 0.8 is above this cosphi_initial, so it should limit Q_scaled abs value to tan(acos(8)) * newP ~= 0.75 * newP
    // Rate limits then apply relative to oldQ, regardless of the power factor limits output.
    // Examples (positive Q, q0 = 10):
    //      newP = 2: powerFactor-limited Q = 0.75 * 2 = 1.5, below minQRate floor of 5 => limited to 5
    //      newP = 24: powerFactor-limited Q = 0.75 * 24 = 18, above maxQRate ceiling of 15 => limited to 15
    // Examples (negative Q, q0 = -10): symmetric, signs flipped
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, nullValues = {"null"}, textBlock = """
         q0,    newP,   minQRate, maxQRate, expectedQ, comment
         10.,   2.,     0.5,      null,     5.0,       'MinPowerFactor and minQRate applied, minQRate wins'
         10.,   24.,    null,     1.5,      15.0,      'MinPowerFactor and maxQRate, maxQRate wins'
         -10.,  2.,     0.5,      null,     -5.0,      'Negative Q: MinPowerFactor and minQRate applied, minQRate wins'
         -10.,  24.,    null,     1.5,      -15.0,     'Negative Q: MinPowerFactor and maxQRate applied, maxQRate wins'
        """
    )
    void testMinPowerFactorWithQRates(double q0, double newP, Double minQRate, Double maxQRate,
                                      double expectedQ, String comment) {
        testScalingReactivePower(10.0, q0, newP, 0.8, minQRate, maxQRate, expectedQ);
    }

    private void testScalingReactivePower(double p0, double q0, double newP,
                                          Double minPowerFactor, Double minQRate, Double maxQRate, double expectedQ) {
        ScalingParameters params = new ScalingParameters()
                .setConstantPowerFactor(true)
                .setScalingConvention(LOAD); // no impact on Q scaling, just on P scaling
        if (minPowerFactor != null) {
            params.setLoadMinPowerFactor(minPowerFactor);
        }
        params.setLoadMinQRate(minQRate);
        params.setLoadMaxQRate(maxQRate);
        Load load = network.getLoad("l1");
        load.setP0(p0).setQ0(q0);
        double askedDelta = newP - p0; // with load convention: asked < 0 reduces P
        l4.scale(network, askedDelta, params);
        assertEquals(newP, load.getP0(), 1e-3);
        assertEquals(expectedQ, load.getQ0(), 1e-3);
    }
}
