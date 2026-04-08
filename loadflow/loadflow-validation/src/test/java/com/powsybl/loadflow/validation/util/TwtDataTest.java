/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation.util;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.util.TwtData;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class TwtDataTest {

    @Test
    void test() {
        TwtData twtData = new TwtData(new TwtTestData().get3WTransformer(), 0, false, false);

        assertEquals(TwtTestData.P1, twtData.getComputedP(ThreeSides.ONE), .3);
        assertEquals(TwtTestData.Q1, twtData.getComputedQ(ThreeSides.ONE), .3);
        assertEquals(TwtTestData.P2, twtData.getComputedP(ThreeSides.TWO), .3);
        assertEquals(TwtTestData.Q2, twtData.getComputedQ(ThreeSides.TWO), .3);
        assertEquals(TwtTestData.P3, twtData.getComputedP(ThreeSides.THREE), .3);
        assertEquals(TwtTestData.Q3, twtData.getComputedQ(ThreeSides.THREE), .3);

        assertEquals(TwtTestData.STAR_U, twtData.getStarU(), .0001);
        assertEquals(TwtTestData.STAR_ANGLE, Math.toDegrees(twtData.getStarTheta()), .0001);
    }

    @Test
    void testSplitShuntAdmittance() {
        ThreeWindingsTransformer twt = new TwtTestData().get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false, true);
        boolean ok = t3xCompareFlow(twtData, 99.231950, 2.876479, -216.194348, -85.558437, 117.981856, 92.439531);
        assertTrue(ok);
    }

    @Test
    void testEnd1End2End3Connected() {
        ThreeWindingsTransformer twt = new TwtTestData().get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, 99.227288294050, 2.747147185208, -216.195866533486, -85.490493190353, 117.988318295633, 92.500849015581);
        assertTrue(ok);

        ok = t3xCompareStarBusVoltage(twtData, 412.66200701692287, -7.353686938578365);
        assertTrue(ok);
    }

    @Test
    void testEnd2End3Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg1Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, 0.0, 0.0, -164.099476216398, -81.835885442800, 165.291731946141, 89.787051339157);
        assertTrue(ok);

        ok = t3xCompareStarBusVoltage(twtData, 412.29478568401856, -7.700275244269859);
        assertTrue(ok);
    }

    @Test
    void testEnd1End3Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg2Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, -18.723067158829, -59.239225729782, 0.0, 0.0, 18.851212571411, 59.694062940578);
        assertTrue(ok);

        ok = t3xCompareStarBusVoltage(twtData, 415.4806896701992, -6.690799426080698);
        assertTrue(ok);
    }

    @Test
    void testEnd1End2Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg3Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, 161.351352526949, 51.327798049323, -161.019856627996, -45.536840365345, 0.0, 0.0);
        assertTrue(ok);

        ok = t3xCompareStarBusVoltage(twtData, 410.53566804098494, -7.703116461849692);
        assertTrue(ok);
    }

    @Test
    void testEnd1Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg2Disconnected();
        twtTestData.setLeg3Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, 0.0, -0.415739792683, 0.0, 0.0, 0.0, 0.0);
        assertTrue(ok);

        ok = t3xCompareStarBusVoltage(twtData, 412.9890009999999, -6.78071000000000);
        assertTrue(ok);
    }

    @Test
    void testEnd2Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg1Disconnected();
        twtTestData.setLeg3Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, 0.0, 0.0, 0.000001946510, -0.405486077928, 0.0, 0.0);
        assertTrue(ok);

        ok = t3xCompareStarBusVoltage(twtData, 407.8654944214268, -8.77026956158324);
        assertTrue(ok);
    }

    @Test
    void testEnd3Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg1Disconnected();
        twtTestData.setLeg2Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, 0.0, 0.0, 0.0, 0.0, 0.000005977974, -0.427562118410);
        assertTrue(ok);

        ok = t3xCompareStarBusVoltage(twtData, 418.82221596280823, -6.65147559975559);
        assertTrue(ok);
    }

    @Test
    void testEnd1End2End3Disconnected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg1Disconnected();
        twtTestData.setLeg2Disconnected();
        twtTestData.setLeg3Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        assertTrue(ok);

        ok = t3xCompareStarBusVoltage(twtData, Double.NaN, Double.NaN);
        assertTrue(ok);
    }

    private static boolean t3xCompareStarBusVoltage(TwtData twtData, double starU, double starAngle) {
        double tol = 0.00001;
        if (Double.isNaN(twtData.getStarU()) && !Double.isNaN(starU)
                || Double.isNaN(twtData.getStarTheta()) && !Double.isNaN(starAngle)
                || Math.abs(twtData.getStarU() - starU) > tol
                || Math.abs(Math.toDegrees(twtData.getStarTheta()) - starAngle) > tol) {
            LOG.info("ThreeWindingsTransformer {} Expected {} {} Actual {} {}", twtData.getId(),
                starU, starAngle, twtData.getStarU(), Math.toDegrees(twtData.getStarTheta()));
            return false;
        }
        return true;
    }

    private static boolean t3xCompareFlow(TwtData twtData, double p1, double q1, double p2, double q2, double p3, double q3) {
        T3xFlow actual = new T3xFlow();
        actual.p1 = twtData.getComputedP(ThreeSides.ONE);
        actual.q1 = twtData.getComputedQ(ThreeSides.ONE);
        actual.p2 = twtData.getComputedP(ThreeSides.TWO);
        actual.q2 = twtData.getComputedQ(ThreeSides.TWO);
        actual.p3 = twtData.getComputedP(ThreeSides.THREE);
        actual.q3 = twtData.getComputedQ(ThreeSides.THREE);

        T3xFlow expected = new T3xFlow();
        expected.p1 = p1;
        expected.q1 = q1;
        expected.p2 = p2;
        expected.q2 = q2;
        expected.p3 = p3;
        expected.q3 = q3;

        return sameFlow(expected, actual);
    }

    private static boolean sameFlow(T3xFlow expected, T3xFlow actual) {
        double tol = 0.00001;
        if (!Double.isNaN(expected.p1) && Double.isNaN(actual.p1)
                || !Double.isNaN(expected.q1) && Double.isNaN(actual.q1)
                || !Double.isNaN(expected.p2) && Double.isNaN(actual.p2)
                || !Double.isNaN(expected.q2) && Double.isNaN(actual.q2)
                || !Double.isNaN(expected.p3) && Double.isNaN(actual.p3)
                || !Double.isNaN(expected.q3) && Double.isNaN(actual.q3)
                || Math.abs(expected.p1 - actual.p1) > tol
                || Math.abs(expected.q1 - actual.q1) > tol
                || Math.abs(expected.p2 - actual.p2) > tol
                || Math.abs(expected.q2 - actual.q2) > tol
                || Math.abs(expected.p3 - actual.p3) > tol
                || Math.abs(expected.q3 - actual.q3) > tol) {
            return false;
        }
        return true;
    }

    static class T3xFlow {
        double p1 = Double.NaN;
        double q1 = Double.NaN;
        double p2 = Double.NaN;
        double q2 = Double.NaN;
        double p3 = Double.NaN;
        double q3 = Double.NaN;
    }

    private static final Logger LOG = LoggerFactory.getLogger(TwtDataTest.class);
}
