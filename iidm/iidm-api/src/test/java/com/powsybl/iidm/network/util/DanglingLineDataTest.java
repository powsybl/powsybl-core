/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.DanglingLine;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class DanglingLineDataTest {

    @Test
    public void test() {
        DanglingLine danglingLine = new DanglingLineTestData().getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.63382758266334, -8.573434828294932);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -367.35795801563415, 63.73282249057797);
        assertTrue(ok);
    }

    @Test
    public void testP0Q0zero() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setP0Zero();
        dlTestData.setQ0Zero();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.6200406620039, -8.60000143239463);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, 0.000000002048243, -0.1653398325668502);
        assertTrue(ok);
    }

    @Test
    public void testGenerationHalfP0Q0() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setTargetPHalfP0();
        dlTestData.setTargetQHalfQ0();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.63382758266334, -8.573434828294932);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -367.35795801563415, 63.73282249057797);
        assertTrue(ok);
    }

    @Test
    public void testZ0() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setZ0();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.62, -8.60);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -367.40, 63.73);
        assertTrue(ok);
    }

    private static boolean dlCompareBoundaryBusVoltage(DanglingLineData dlData, double boundaryBusU, double boundaryBusAngle) {
        double tol = 0.00001;
        if (Math.abs(dlData.getBoundaryBusU() - boundaryBusU) > tol || Math.abs(Math.toDegrees(dlData.getBoundaryBusTheta()) - boundaryBusAngle) > tol) {
            LOG.info("DanglingLine {} Expected {} {} Actual {} {}", dlData.getId(),
                boundaryBusU, boundaryBusAngle, dlData.getBoundaryBusU(), Math.toDegrees(dlData.getBoundaryBusTheta()));
            return false;
        }
        return true;
    }

    private static boolean dlCompareNetworkActiveAndReactivePower(DanglingLineData dlData, double activePower, double reactivePower) {
        double tol = 0.00001;
        if (Math.abs(dlData.getP() - activePower) > tol || Math.abs(dlData.getQ() - reactivePower) > tol) {
            LOG.info("DanglingLine {} Expected {} {} Actual {} {}", dlData.getId(),
                activePower, reactivePower, dlData.getP(), dlData.getQ());
            return false;
        }
        return true;
    }

    private static final Logger LOG = LoggerFactory.getLogger(DanglingLineDataTest.class);
}
