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
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 367.40, -63.73);
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
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 0.0, 0.0);
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
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 367.40, -63.73);
        assertTrue(ok);
    }

    @Test
    public void testInvalidNetworkVoltage() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setInvalidNetworkVoltage();
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
    public void testZ0() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setZ0();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.62, -8.6);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, Double.NaN, Double.NaN);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, Double.NaN, Double.NaN);
        assertTrue(ok);
    }

    @Test
    public void testNetworkFlow() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setNetworkFlow();
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
    public void testGenerationControl() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setGenerationControl();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.60, -8.558715082646454);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -550.9985402563893, 178.54356710697186);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 551.10, -178.3030598246199);
        assertTrue(ok);
    }

    @Test
    public void testGenerationControlSaturatedAtQmin() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setGenerationControlSaturatedAtQmin();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.6514429130987, -8.560532069207145);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -551.0065296776671, 73.93852867914545);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 551.10, -73.73);
        assertTrue(ok);
    }

    @Test
    public void testGenerationControlSaturatedAtQmax() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setGenerationControlSaturatedAtQmax();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.7055410334431, -8.562442852961746);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -551.0077946191535, -36.06655308730602);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 551.10, 36.27);
        assertTrue(ok);
    }

    @Test
    public void testIncreaseQ0ToForceVoltageCollapse() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setP0Q0ToForceVoltageCollapse();
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
    public void testGenerationControlVoltageCollapseHighVsetpoint() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setGenerationControlVoltageCollapseHighVsetpoint();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.9021364892158, -8.569386864781194);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -550.9507830807831, -435.8385869096061);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 551.10, 436.27);
        assertTrue(ok);
    }

    @Test
    public void testGenerationControlVoltageCollapseLowVsetpoint() {
        DanglingLineTestData dlTestData = new DanglingLineTestData();
        dlTestData.setGenerationControlVoltageCollapseLowVsetpoint();
        DanglingLine danglingLine = dlTestData.getDanglingLine();
        DanglingLineData dlData = new DanglingLineData(danglingLine);

        boolean ok = dlCompareBoundaryBusVoltage(dlData, 406.4102798132545, -8.552014194201389);
        assertTrue(ok);
        ok = dlCompareNetworkActiveAndReactivePower(dlData, -550.9118870878652, 564.3171970786386);
        assertTrue(ok);
        ok = dlCompareBoundaryActiveAndReactivePower(dlData, 551.10, -563.73);
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
