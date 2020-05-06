/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TwtDataTest {

    @Test
    public void test() {
        TwtData twtData = new TwtData(new TwtTestData().get3WTransformer(), 0, false, false);

        assertEquals(TwtTestData.P1, twtData.getComputedP(Side.ONE), .3);
        assertEquals(TwtTestData.Q1, twtData.getComputedQ(Side.ONE), .3);
        assertEquals(TwtTestData.P2, twtData.getComputedP(Side.TWO), .3);
        assertEquals(TwtTestData.Q2, twtData.getComputedQ(Side.TWO), .3);
        assertEquals(TwtTestData.P3, twtData.getComputedP(Side.THREE), .3);
        assertEquals(TwtTestData.Q3, twtData.getComputedQ(Side.THREE), .3);

        assertEquals(TwtTestData.STAR_U, twtData.getStarU(), .0001);
        assertEquals(TwtTestData.STAR_ANGLE, Math.toDegrees(twtData.getStarTheta()), .0001);
    }

    @Test
    public void testSplitShuntAdmittance() {
        ThreeWindingsTransformer twt = new TwtTestData().get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false, true);
        boolean ok = t3xCompareFlow(twtData, 99.231950, 2.876479, -216.194348, -85.558437, 117.981856, 92.439531);
        assertTrue(ok);
    }

    @Test
    public void testEnd1End2End3Connected() {
        ThreeWindingsTransformer twt = new TwtTestData().get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, 99.227288294050, 2.747147185208, -216.195866533486, -85.490493190353, 117.988318295633, 92.500849015581);
        assertTrue(ok);
    }

    @Test
    public void testEnd2End3Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg1Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, 0.0, 0.0, -164.099476216398, -81.835885442800, 165.291731946141, 89.787051339157);
        assertTrue(ok);
    }

    @Test
    public void testEnd1End3Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg2Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, -18.723067158829, -59.239225729782, 0.0, 0.0, 18.851212571411, 59.694062940578);
        assertTrue(ok);
    }

    @Test
    public void testEnd1End2Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg3Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData, 161.351352526949, 51.327798049323, -161.019856627996, -45.536840365345, 0.0, 0.0);
        assertTrue(ok);
    }

    @Test
    public void testEnd1Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg2Disconnected();
        twtTestData.setLeg3Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData,  0.0, -0.415739792683, 0.0, 0.0, 0.0, 0.0);
        assertTrue(ok);
    }

    @Test
    public void testEnd2Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg1Disconnected();
        twtTestData.setLeg3Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData,  0.0, 0.0, 0.000001946510, -0.405486077928, 0.0, 0.0);
        assertTrue(ok);
    }

    @Test
    public void testEnd3Connected() {
        TwtTestData twtTestData = new TwtTestData();
        twtTestData.setLeg1Disconnected();
        twtTestData.setLeg2Disconnected();

        ThreeWindingsTransformer twt = twtTestData.get3WTransformer();
        TwtData twtData = new TwtData(twt, 0, false);
        boolean ok = t3xCompareFlow(twtData,  0.0, 0.0, 0.0, 0.0, 0.000005977974, -0.427562118410);
        assertTrue(ok);
    }

    private boolean t3xCompareFlow(TwtData twtData, double p1, double q1, double p2, double q2, double p3, double q3) {
        T3xFlow actual = new T3xFlow();
        actual.p1 = twtData.getComputedP(Side.ONE);
        actual.q1 = twtData.getComputedQ(Side.ONE);
        actual.p2 = twtData.getComputedP(Side.TWO);
        actual.q2 = twtData.getComputedQ(Side.TWO);
        actual.p3 = twtData.getComputedP(Side.THREE);
        actual.q3 = twtData.getComputedQ(Side.THREE);

        T3xFlow expected = new T3xFlow();
        expected.p1 = p1;
        expected.q1 = q1;
        expected.p2 = p2;
        expected.q2 = q2;
        expected.p3 = p3;
        expected.q3 = q3;

        return sameFlow(expected, actual);
    }

    private boolean sameFlow(T3xFlow expected, T3xFlow actual) {
        double tol = 0.00001;
        if (Math.abs(expected.p1 - actual.p1) > tol ||
            Math.abs(expected.q1 - actual.q1) > tol ||
            Math.abs(expected.p2 - actual.p2) > tol ||
            Math.abs(expected.q2 - actual.q2) > tol ||
            Math.abs(expected.p3 - actual.p3) > tol ||
            Math.abs(expected.q3 - actual.q3) > tol) {
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
}
