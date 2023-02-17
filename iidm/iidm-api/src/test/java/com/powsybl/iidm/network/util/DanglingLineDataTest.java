/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.DanglingLine;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class DanglingLineDataTest {

    @Test
    void test() {
        DanglingLine danglingLine = new DanglingLineTestData().getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.63382758266334, -8.573434828294932);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -367.35795801563415, 63.73282249057797);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 367.40, -63.73);
        assertTrue(ok);
    }

    @Test
    void testNetworkFlow() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.63382758266334, -8.573434828294932);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -367.35795801563415, 63.73282249057797);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 367.40, -63.73);
        assertTrue(ok);
    }

    @Test
    void testInvalidNetworkVoltage() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setInvalidNetworkVoltage();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, Double.NaN, Double.NaN);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -367.35795801563415, 63.73282249057797);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, Double.NaN, Double.NaN);
        assertTrue(ok);
    }

    @Test
    void testInvalidNetworkFlow() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setInvalidNetworkFlow();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, Double.NaN, Double.NaN);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, Double.NaN, Double.NaN);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, Double.NaN, Double.NaN);
        assertTrue(ok);
    }

    @Test
    void testZ0() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setZ0();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.62, -8.6);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -367.35795801563415, 63.73282249057797);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 367.35795801563415, -63.73282249057797);
        assertTrue(ok);
    }

    @Test
    void testZ0WithNetworkFlow() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setZ0();
        dlTestData.setInvalidNetworkFlow();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.62, -8.6);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, Double.NaN, Double.NaN);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, Double.NaN, Double.NaN);
        assertTrue(ok);
    }

    private static boolean dlCompareBoundaryBusVoltage(DanglingLineData dlData, double boundaryBusU, double boundaryBusAngle) {
        double tol = 0.00001;
        if (!compareNaN(boundaryBusU, dlData.getBoundaryBusU())
            || !compareNaN(boundaryBusAngle, dlData.getBoundaryBusTheta())
            || Math.abs(dlData.getBoundaryBusU() - boundaryBusU) > tol
            || Math.abs(Math.toDegrees(dlData.getBoundaryBusTheta()) - boundaryBusAngle) > tol) {
            LOG.info("DanglingLine {} Expected {} {} Actual {} {}", dlData.getId(),
                boundaryBusU, boundaryBusAngle, dlData.getBoundaryBusU(), Math.toDegrees(dlData.getBoundaryBusTheta()));
            return false;
        }
        return true;
    }

    private static boolean dlCompareNetworkActiveAndReactivePower(DanglingLineData dlData, double activePower, double reactivePower) {
        double tol = 0.00001;
        if (!compareNaN(activePower, dlData.getNetworkFlowP())
            || !compareNaN(reactivePower, dlData.getNetworkFlowQ())
            || Math.abs(dlData.getNetworkFlowP() - activePower) > tol
            || Math.abs(dlData.getNetworkFlowQ() - reactivePower) > tol) {
            LOG.info("DanglingLine {} Expected {} {} Actual {} {}", dlData.getId(),
                activePower, reactivePower, dlData.getNetworkFlowP(), dlData.getNetworkFlowQ());
            return false;
        }
        return true;
    }

    private static boolean dlCompareBoundaryActiveAndReactivePower(DanglingLineData dlData, double activePower, double reactivePower) {
        double tol = 0.00001;
        if (!compareNaN(activePower, dlData.getBoundaryFlowP())
            || !compareNaN(reactivePower, dlData.getBoundaryFlowQ())
            || Math.abs(dlData.getBoundaryFlowP() - activePower) > tol
            || Math.abs(dlData.getBoundaryFlowQ() - reactivePower) > tol) {
            LOG.info("DanglingLine {} Expected {} {} Actual {} {}", dlData.getId(),
                activePower, reactivePower, dlData.getBoundaryFlowP(), dlData.getBoundaryFlowQ());
            return false;
        }
        return true;
    }

    private static boolean compareNaN(double expected, double actual) {
        return !(Double.isNaN(expected) && !Double.isNaN(actual) || !Double.isNaN(expected) && Double.isNaN(actual));
    }

    private static final Logger LOG = LoggerFactory.getLogger(DanglingLineDataTest.class);
}
