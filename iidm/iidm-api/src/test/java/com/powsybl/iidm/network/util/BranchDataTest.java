/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import static org.junit.Assert.assertEquals;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Branch.Side;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class BranchDataTest {

    // Some tests for a transmission line disconnected at one end

    @Test
    public void testDanglingLine() {
        BranchTestCase t = lineEnd2Disconnected();

        // The expected values at the other end will be the same when we disconnect end
        // 1 or end 2
        // If we use the same voltage at the connected end
        double expectedU = 381.9095;
        double expectedTheta = -0.000503;

        // First obtain results when end 2 is disconnected
        BranchData b2disconnected = checkTestCase("End 2 disconnected", t);
        assertEquals(expectedU, b2disconnected.getComputedU2(), t.config.toleranceVoltage);
        assertEquals(expectedTheta, b2disconnected.getComputedTheta2(), t.config.toleranceVoltage);

        // Now change the disconnected end and check the same results are obtained
        t.bus2.u = t.bus1.u;
        t.bus2.theta = t.bus1.theta;
        t.bus1.u = Double.NaN;
        t.bus1.theta = Double.NaN;
        t.branch.end2.connected = true;
        t.branch.end1.connected = false;
        t.expectedFlow2.p = t.expectedFlow1.p;
        t.expectedFlow2.q = t.expectedFlow1.q;
        t.expectedFlow1.p = Double.NaN;
        t.expectedFlow1.q = Double.NaN;
        BranchData b1disconnected = checkTestCase("End 1 disconnected", t);
        assertEquals(expectedU, b1disconnected.getComputedU1(), t.config.toleranceVoltage);
        assertEquals(expectedTheta, b1disconnected.getComputedTheta1(), t.config.toleranceVoltage);
    }

    @Test
    public void testDanglingLineDifferentY() {
        BranchTestCase t = lineEnd2Disconnected();
        t.branch.id = "Dangling-Y1-Y2-different";
        // End 1 admittance to ground has different value of End 2
        t.branch.end1.b = t.branch.end2.b * 2;
        double expectedU;
        double expectedTheta;

        // When end 2 is disconnected, the voltage at end 2
        // should be the same that was obtained when Y1 = Y2
        // because the voltage at end 2 depends only on Ytr and Y2
        expectedU = 381.9095;
        expectedTheta = -0.000503;
        // But the flow seen at end 1 will be different
        t.expectedFlow1.p = 0.0072927;
        t.expectedFlow1.q = -43.392559;
        BranchData b2disconnected = checkTestCase("End 2 disconnected", t);
        assertEquals(expectedU, b2disconnected.getComputedU2(), t.config.toleranceVoltage);
        assertEquals(expectedTheta, b2disconnected.getComputedTheta2(), t.config.toleranceVoltage);

        // Now when we disconnect end 1 both the voltage drop and
        // the expected values for flow are different
        t.bus2.u = t.bus1.u;
        t.bus2.theta = t.bus1.theta;
        t.bus1.u = Double.NaN;
        t.bus1.theta = Double.NaN;
        t.branch.end2.connected = true;
        t.branch.end1.connected = false;
        expectedU = 383.838188;
        expectedTheta = -0.001010;
        t.expectedFlow1.p = Double.NaN;
        t.expectedFlow1.q = Double.NaN;
        t.expectedFlow2.p = 0.02946635;
        t.expectedFlow2.q = -43.611687;
        BranchData b1disconnected = checkTestCase("End 1 disconnected", t);
        assertEquals(expectedU, b1disconnected.getComputedU1(), t.config.toleranceVoltage);
        assertEquals(expectedTheta, b1disconnected.getComputedTheta1(), t.config.toleranceVoltage);
    }

    private BranchTestCase lineEnd2Disconnected() {
        BranchTestCase t = new BranchTestCase();
        t.config.convertAsTransformer = false;

        t.branch.id = "Dangling";
        t.branch.end1.ratedU = 380;
        t.branch.end2.ratedU = 380;
        t.branch.end1.r = 5;
        t.branch.end1.x = 50.0;
        t.branch.end1.g = 0;
        t.branch.end1.b = 0.0001;
        t.branch.end2.g = 0;
        t.branch.end2.b = 0.0001;

        t.branch.end1.connected = true;
        t.branch.end2.connected = false;
        t.bus1.u = 380;
        t.bus1.theta = 0;
        t.bus2.u = Double.NaN;
        t.bus2.theta = Double.NaN;

        t.expectedFlow2.p = Double.NaN;
        t.expectedFlow2.q = Double.NaN;
        t.expectedFlow1.p = 0.007293;
        t.expectedFlow1.q = -28.952559;
        return t;
    }

    // Test cases related to ENTSO-E CASv2.0 test configuration
    // ENTSOE_LoadFlowExplicit
    // There are two parallel branches between a generator and a load bus:
    // a transmission line and a phase shift transformer

    @Test
    public void testCAS2EntsoeLoadFlowExplicitLine() {
        checkTestCase("line", cas2EntsoeLoadFlowExplicitLine());
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitPhaseShiftTransformer() {
        checkTestCase("pst", cas2EntsoeLoadFlowExplicitPhaseShiftTransformer());
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitLineAndPhaseShifter() {
        BranchTestCase line = cas2EntsoeLoadFlowExplicitLine();
        BranchTestCase pst = cas2EntsoeLoadFlowExplicitPhaseShiftTransformer();
        Flow load = cas2EntsoeLoadFlowExplicitLoad();
        Flow generator = cas2EntsoeLoadFlowExplicitGenerator();

        // Obtain flows at both ends of line and phase shifter
        BranchData pline = piModelFlows(line);
        BranchData ppst = piModelFlows(pst);

        // And check bus balance at each side
        // At end1 (load) the mismatch should be zero
        // because load values are given with all significant digits
        // and voltages have been copied from Excel documentation,
        // where they have sufficient precision
        // At end2 (generator, slack bus) the mismatch should be small:
        // generator injection in P and Q are provided with low precision
        // (P with 10e-2, Q with 10e-4)
        LOG.debug("");
        LOG.debug("Balance at ends of parallel branches " + pline.getId() + ", " + ppst.getId());
        Flow line1 = flow(pline, Side.ONE);
        Flow line2 = flow(pline, Side.TWO);
        Flow pst1 = flow(ppst, Side.ONE);
        Flow pst2 = flow(ppst, Side.TWO);
        checkBusBalance("End 1", TOLERANCE_BALANCE_EXACT, TOLERANCE_BALANCE_EXACT, line1, pst1, load);
        checkBusBalance("End 2", 1e-2, 1e-4, line2, pst2, generator);
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitLineAndPhaseShifterMovedToEnd1() {
        BranchTestCase line = cas2EntsoeLoadFlowExplicitLine();
        BranchTestCase pst = cas2EntsoeLoadFlowExplicitPhaseShiftTransformer();
        Flow load = cas2EntsoeLoadFlowExplicitLoad();
        Flow generator = cas2EntsoeLoadFlowExplicitGenerator();

        // Move phase shift from end 2 to end 1
        pst.branch.end1.tap.rho = 1 / pst.branch.end2.tap.rho;
        pst.branch.end1.tap.alpha = -pst.branch.end2.tap.alpha;
        double rho2square = pst.branch.end2.tap.rho * pst.branch.end2.tap.rho;
        double dz = 100 * (1 / rho2square - 1);
        double dy = 100 * (rho2square - 1);
        pst.branch.end1.tap.r = dz;
        pst.branch.end1.tap.x = dz;
        pst.branch.end1.tap.g = dy;
        pst.branch.end1.tap.b = dy;
        pst.branch.end2.tap.rho = 1;
        pst.branch.end2.tap.alpha = 0;

        BranchData pline = piModelFlows(line);
        BranchData ppst = piModelFlows(pst);

        LOG.debug("");
        LOG.debug("Balance at ends of parallel branches " + pline.getId() + ", " + ppst.getId());
        LOG.debug("After moving phase shifter to side 1");
        Flow line1 = flow(pline, Side.ONE);
        Flow line2 = flow(pline, Side.TWO);
        Flow pst1 = flow(ppst, Side.ONE);
        Flow pst2 = flow(ppst, Side.TWO);
        checkBusBalance("End 1", TOLERANCE_BALANCE_EXACT, TOLERANCE_BALANCE_EXACT, line1, pst1, load);
        checkBusBalance("End 2", 1e-2, 1e-4, line2, pst2, generator);
    }

    private BranchTestCase cas2EntsoeLoadFlowExplicitLine() {
        BranchTestCase t = new BranchTestCase();
        t.branch.id = "FFNOD0L41__FNO";
        t.branch.end1.ratedU = 380;
        t.branch.end2.ratedU = 380;
        t.branch.end1.r = 4.1956;
        t.branch.end1.x = 12.73;
        // Voltage and angle for bus 1 have been taken from Excel documentation
        // with much more precision that the one found in SV data files
        t.bus1.u = cas2EntsoeLoadFLowExplicitU1();
        t.bus1.theta = Math.toRadians(cas2EntsoeLoadFLowExplicitTheta1());
        t.bus2.u = 397.1;
        t.bus2.theta = Math.toRadians(0);
        t.expectedFlow1.p = -534.9869;
        t.expectedFlow1.q = 153.1046;
        t.expectedFlow2.p = 543.2755;
        t.expectedFlow2.q = -127.9559;
        return t;
    }

    private BranchTestCase cas2EntsoeLoadFlowExplicitPhaseShiftTransformer() {
        BranchTestCase t = new BranchTestCase();
        t.branch.id = "FNOD041__FNOD021__1_PT";
        t.branch.end1.ratedU = 380;
        t.branch.end2.ratedU = 380;
        t.branch.end1.r = 4.1956;
        t.branch.end1.x = 12.73;
        t.bus1.u = cas2EntsoeLoadFLowExplicitU1();
        t.bus1.theta = Math.toRadians(cas2EntsoeLoadFLowExplicitTheta1());
        t.bus2.u = 397.1;
        t.bus2.theta = Math.toRadians(0);
        t.expectedFlow1.p = 202.9869;
        t.expectedFlow1.q = -75.1046;
        t.expectedFlow2.p = -201.7355;
        t.expectedFlow2.q = 78.9091;

        t.branch.end2.tap.rho = 0.997829;
        t.branch.end2.tap.alpha = Math.toRadians(-3.77605);
        return t;
    };

    // Voltage and angle for bus 1 have been taken from Excel documentation
    // with much more precision that the one found in SV data files
    private double cas2EntsoeLoadFLowExplicitU1() {
        return 395.906724888442;
    }

    private double cas2EntsoeLoadFLowExplicitTheta1() {
        return -2.717121983205;
    }

    private Flow cas2EntsoeLoadFlowExplicitLoad() {
        Flow f = new Flow();
        f.id = "Load";
        f.p = 332.0;
        f.q = -78.0;
        return f;
    }

    private Flow cas2EntsoeLoadFlowExplicitGenerator() {
        Flow f = new Flow();
        f.id = "Generator";
        f.p = -341.54;
        f.q = 49.0468;
        return f;
    }

    // Test case related to ENTSO-E CASv1.1.3 test configuration MicroGrid
    // (data version 4.0.3) using base case and variants
    // We check flows in 3-winding transformer BE-TR3_1

    enum CAS1EntsoeMicroGrid3wTxVariant {
        BC, BC_NO_TRANSFORMER_REGULATION, BC_AREA_CONTROL_ON, T1, T2, T3, T4
    }

    @Test
    public void testCAS1EntsoeMicroGrid3wTxBC() {
        testCAS1EntsoeMicroGrid3wTx(CAS1EntsoeMicroGrid3wTxVariant.BC);
    }

    @Test
    public void testCAS1EntsoeMicroGrid3wTxBCNoTxRegulation() {
        testCAS1EntsoeMicroGrid3wTx(CAS1EntsoeMicroGrid3wTxVariant.BC_NO_TRANSFORMER_REGULATION);
    }

    @Test
    public void testCAS1EntsoeMicroGrid3wTxBCAreaControlOn() {
        testCAS1EntsoeMicroGrid3wTx(CAS1EntsoeMicroGrid3wTxVariant.BC_AREA_CONTROL_ON);
    }

    @Test
    public void testCAS1EntsoeMicroGrid3wTxT1() {
        testCAS1EntsoeMicroGrid3wTx(CAS1EntsoeMicroGrid3wTxVariant.T1);
    }

    @Test
    public void testCAS1EntsoeMicroGrid3wTxT2() {
        testCAS1EntsoeMicroGrid3wTx(CAS1EntsoeMicroGrid3wTxVariant.T2);
    }

    @Test
    public void testCAS1EntsoeMicroGrid3wTxT3() {
        testCAS1EntsoeMicroGrid3wTx(CAS1EntsoeMicroGrid3wTxVariant.T3);
    }

    @Test
    public void testCAS1EntsoeMicroGrid3wTxT4() {
        testCAS1EntsoeMicroGrid3wTx(CAS1EntsoeMicroGrid3wTxVariant.T4);
    }

    private void testCAS1EntsoeMicroGrid3wTx(CAS1EntsoeMicroGrid3wTxVariant variant) {
        LOG.debug("");
        LOG.debug("CAS1 ENTSO-E MicroGrid 3-widing transformer, variant " + variant);
        BranchTestCase w380 = cas1EntsoeMicroGrid3wTxW380(variant);
        BranchTestCase w225 = cas1EntsoeMicroGrid3wTxW225(variant);
        BranchTestCase w21 = cas1EntsoeMicroGrid3wTxW21(variant);
        check3wTx(w380, w225, w21);
    }

    private BranchTestCase cas1EntsoeMicroGrid3wTxW380(CAS1EntsoeMicroGrid3wTxVariant variant) {
        BranchTestCase t = new BranchTestCase();
        t.branch.id = "380";
        t.branch.end1.ratedU = 400;
        t.branch.end2.ratedU = 1;
        t.branch.end1.r = 0.898462;
        t.branch.end1.x = 17.204128;
        t.branch.end2.b = 0.0000024375;
        t.expectedFlow2.p = Double.NaN;
        t.expectedFlow2.q = Double.NaN;
        switch (variant) {
            case BC:
                t.bus1.u = 412.989001;
                t.bus1.theta = Math.toRadians(-6.78071);
                t.expectedFlow1.p = 99.218431;
                t.expectedFlow1.q = 3.304328;
                break;
            case BC_NO_TRANSFORMER_REGULATION:
                t.bus1.u = 413.367538;
                t.bus1.theta = Math.toRadians(-6.96199);
                t.expectedFlow1.p = -38.7065;
                t.expectedFlow1.q = 13.850241;
                break;
            case BC_AREA_CONTROL_ON:
                t.bus1.u = 412.953536;
                t.bus1.theta = Math.toRadians(-6.23401);
                t.expectedFlow1.p = 61.652507;
                t.expectedFlow1.q = 5.431494;
                break;
            case T1:
                t.bus1.u = 412.989258;
                t.bus1.theta = Math.toRadians(-6.78901);
                t.expectedFlow1.p = 99.586268;
                t.expectedFlow1.q = 3.250355;
                break;
            case T2:
                t.bus1.u = 412.633073;
                t.bus1.theta = Math.toRadians(-5.82972);
                t.expectedFlow1.p = -2.463349;
                t.expectedFlow1.q = 4.837149;
                break;
            case T3:
                t.bus1.u = 413.589856;
                t.bus1.theta = Math.toRadians(-6.64052);
                t.expectedFlow1.p = 67.610584;
                t.expectedFlow1.q = -11.251975;
                break;
            case T4:
                t.bus1.u = 414.114413;
                t.bus1.theta = Math.toRadians(-21.5265);
                t.expectedFlow1.p = -37.513383;
                t.expectedFlow1.q = 28.348302;
                break;
        }
        return t;
    }

    private BranchTestCase cas1EntsoeMicroGrid3wTxW225(CAS1EntsoeMicroGrid3wTxVariant variant) {
        BranchTestCase t = new BranchTestCase();
        t.branch.id = "225";
        t.branch.end1.ratedU = 220;
        t.branch.end2.ratedU = 1;
        t.branch.end1.r = 0.323908;
        t.branch.end1.x = 5.949086;
        t.branch.end1.b = 0.0;
        // Tap changer is at step 17 that is the neutralStep
        // ratio tap changer has lowStep = 1, highStep = 33,
        // stepVoltageIncrement = 0.625, neutralU = 220, neutralStep = 17
        t.branch.end1.tap.rho = 1.0;
        t.expectedFlow2.p = Double.NaN;
        t.expectedFlow2.q = Double.NaN;
        switch (variant) {
            case BC:
                t.bus1.u = 224.315268;
                t.bus1.theta = Math.toRadians(-8.77012);
                t.expectedFlow1.p = -216.19819;
                t.expectedFlow1.q = -85.36818;
                break;
            case BC_NO_TRANSFORMER_REGULATION:
                t.bus1.u = 224.386792;
                t.bus1.theta = Math.toRadians(-7.22458);
                t.expectedFlow1.p = -78.584994;
                t.expectedFlow1.q = -97.109252;
                break;
            case BC_AREA_CONTROL_ON:
                t.bus1.u = 224.309142;
                t.bus1.theta = Math.toRadians(-7.86995);
                t.expectedFlow1.p = -195.95349;
                t.expectedFlow1.q = -86.033369;
                break;
            case T1:
                t.bus1.u = 224.315838;
                t.bus1.theta = Math.toRadians(-8.77964);
                t.expectedFlow1.p = -216.06472;
                t.expectedFlow1.q = -85.396168;
                break;
            case T2:
                t.bus1.u = 224.114164;
                t.bus1.theta = Math.toRadians(-6.54843);
                t.expectedFlow1.p = -114.74994;
                t.expectedFlow1.q = -95.746507;
                break;
            case T3:
                t.bus1.u = 226.03389;
                t.bus1.theta = Math.toRadians(-8.23977);
                t.expectedFlow1.p = -184.84088;
                t.expectedFlow1.q = -49.665543;
                break;
            case T4:
                t.bus1.u = 224.156562;
                t.bus1.theta = Math.toRadians(-21.7962);
                t.expectedFlow1.p = -79.771949;
                t.expectedFlow1.q = -108.56893;
                break;
        }
        return t;
    }

    private BranchTestCase cas1EntsoeMicroGrid3wTxW21(CAS1EntsoeMicroGrid3wTxVariant variant) {
        BranchTestCase t = new BranchTestCase();
        t.branch.id = "21";
        t.branch.end1.ratedU = 21;
        t.branch.end2.ratedU = 1;
        t.branch.end1.r = 0.013332;
        t.branch.end1.x = 0.059978;
        t.branch.end1.b = 0.0;
        t.expectedFlow2.p = Double.NaN;
        t.expectedFlow2.q = Double.NaN;
        switch (variant) {
            case BC:
                t.bus1.u = 21.987;
                t.bus1.theta = Math.toRadians(-6.6508);
                t.expectedFlow1.p = 118.0;
                t.expectedFlow1.q = 92.612077;
                break;
            case BC_NO_TRANSFORMER_REGULATION:
                t.bus1.u = 21.987;
                t.bus1.theta = Math.toRadians(-6.02499);
                t.expectedFlow1.p = 118.0;
                t.expectedFlow1.q = 88.343914;
                break;
            case BC_AREA_CONTROL_ON:
                t.bus1.u = 21.987;
                t.bus1.theta = Math.toRadians(-5.75689);
                t.expectedFlow1.p = 135.34484;
                t.expectedFlow1.q = 90.056974;
                break;
            case T1:
                t.bus1.u = 21.987;
                t.bus1.theta = Math.toRadians(-6.665);
                t.expectedFlow1.p = 117.495810;
                t.expectedFlow1.q = 92.681978;
                break;
            case T2:
                t.bus1.u = 21.987;
                t.bus1.theta = Math.toRadians(-5.11757);
                t.expectedFlow1.p = 118.0;
                t.expectedFlow1.q = 96.822572;
                break;
            case T3:
                t.bus1.u = 21.987;
                t.bus1.theta = Math.toRadians(-6.29357);
                t.expectedFlow1.p = 118.0;
                t.expectedFlow1.q = 68.339383;
                break;
            case T4:
                t.bus1.u = 21.987;
                t.bus1.theta = Math.toRadians(-20.5883);
                t.expectedFlow1.p = 118.0;
                t.expectedFlow1.q = 85.603401;
                break;
        }
        return t;
    }

    // Test tools

    private void check3wTx(BranchTestCase w1, BranchTestCase w2, BranchTestCase w3) {
        String label1 = String.format("star bus from V1, S1 (%s)", w1.branch.id);
        String label2 = String.format("star bus from V2, S2 (%s)", w2.branch.id);
        String label3 = String.format("star bus from V3, S3 (%s)", w3.branch.id);
        String label123 = "star bus from V1, V2, V3";

        Bus starBus1tx = calcStarBusFromVkSk(w1);
        Bus starBus2tx = calcStarBusFromVkSk(w2);
        Bus starBus3tx = calcStarBusFromVkSk(w3);
        Bus starBusV1V2V3Ytx = calcStarBusV1V2V3Y(w1, w2, w3);
        w1.config.convertAsTransformer = false;
        w2.config.convertAsTransformer = false;
        w3.config.convertAsTransformer = false;
        Bus starBus1 = calcStarBusFromVkSk(w1);
        Bus starBus2 = calcStarBusFromVkSk(w2);
        Bus starBus3 = calcStarBusFromVkSk(w3);
        Bus starBusV1V2V3Y = calcStarBusV1V2V3Y(w1, w2, w3);
        LOG.debug("comparing voltages computed from different alternatives");
        LOG.debug("applying conversion to IIDM transformer modeling for each end");
        logVoltage(label1, starBus1tx);
        logVoltage(label2, starBus2tx);
        logVoltage(label3, starBus3tx);
        logVoltage(label123, starBusV1V2V3Ytx);
        LOG.debug("without applying conversion to IIDM transformer modeling for each end");
        logVoltage(label1, starBus1);
        logVoltage(label2, starBus2);
        logVoltage(label3, starBus3);
        logVoltage(label123, starBusV1V2V3Y);

        // Ensure the voltage of the star bus is similar
        // when it is computed using the different alternatives
        assertEquals(starBus1.u, starBus2.u, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.u, starBus3.u, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.u, starBusV1V2V3Y.u, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.theta, starBus2.theta, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.theta, starBus3.theta, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.theta, starBusV1V2V3Y.theta, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.u, starBus2tx.u, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.u, starBus3tx.u, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.u, starBusV1V2V3Ytx.u, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.theta, starBus2tx.theta, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.theta, starBus3tx.theta, TOLERANCE_VOLTAGE);
        assertEquals(starBus1.theta, starBusV1V2V3Ytx.theta, TOLERANCE_VOLTAGE);

        // Check bus balance at star bus using the different similar voltages
        // When voltage at star bus is computed from voltage and flow at one end,
        // there will be a non-zero balance at star bus,
        // either because flows are not given with enough precision or because
        // the given solution already contained had a mismatch at star bus
        w1.config.toleranceFlow = TOLERANCE_FLOW_WHEN_3W_STAR_BUS_FROM_VS;
        w2.config.toleranceFlow = TOLERANCE_FLOW_WHEN_3W_STAR_BUS_FROM_VS;
        w3.config.toleranceFlow = TOLERANCE_FLOW_WHEN_3W_STAR_BUS_FROM_VS;
        checkBusBalance3wStarBus(w3, w2, w1, starBus1, TOLERANCE_BALANCE_3W_STAR_BUS, label1);
        checkBusBalance3wStarBus(w3, w2, w1, starBus2, TOLERANCE_BALANCE_3W_STAR_BUS, label2);
        checkBusBalance3wStarBus(w3, w2, w1, starBus3, TOLERANCE_BALANCE_3W_STAR_BUS, label3);

        // When star bus voltage is computed from the three transformer end voltages,
        // it is calculated imposing bus balance equals zero at star bus,
        // so we can check the bus balance is exactly zero,
        // but we have to increase to tolerance when comparing flows
        // with the given values
        w1.config.toleranceFlow = TOLERANCE_FLOW_WHEN_3W_STAR_BUS_FROM_V1V2V3Y;
        w2.config.toleranceFlow = TOLERANCE_FLOW_WHEN_3W_STAR_BUS_FROM_V1V2V3Y;
        w3.config.toleranceFlow = TOLERANCE_FLOW_WHEN_3W_STAR_BUS_FROM_V1V2V3Y;
        checkBusBalance3wStarBus(w3, w2, w1, starBusV1V2V3Y, TOLERANCE_BALANCE_EXACT, label123);
    }

    private BranchData checkTestCase(String title, BranchTestCase t) {
        BranchData b = piModelFlows(t);
        logTestCase(title, t, b);
        assertEquals(t.expectedFlow1.p, b.getComputedP1(), t.config.toleranceFlow);
        assertEquals(t.expectedFlow1.q, b.getComputedQ1(), t.config.toleranceFlow);
        if (!Double.isNaN(t.expectedFlow2.p)) {
            assertEquals(t.expectedFlow2.p, b.getComputedP2(), t.config.toleranceFlow);
        }
        if (!Double.isNaN(t.expectedFlow2.q)) {
            assertEquals(t.expectedFlow2.q, b.getComputedQ2(), t.config.toleranceFlow);
        }
        return b;
    }

    Bus calcStarBusV1V2V3Y(BranchTestCase w1, BranchTestCase w2, BranchTestCase w3) {
        Complex v1 = ComplexUtils.polar2Complex(w1.bus1.u, w1.bus1.theta);
        Complex v2 = ComplexUtils.polar2Complex(w2.bus1.u, w2.bus1.theta);
        Complex v3 = ComplexUtils.polar2Complex(w3.bus1.u, w3.bus1.theta);
        Complex ytr1 = new Complex(w1.branch.end1.r, w1.branch.end1.x).reciprocal();
        Complex ytr2 = new Complex(w2.branch.end1.r, w2.branch.end1.x).reciprocal();
        Complex ytr3 = new Complex(w3.branch.end1.r, w3.branch.end1.x).reciprocal();

        // FIXME consider tap.rho and tap.alpha
        Complex a01 = new Complex(w1.branch.end2.ratedU / w1.branch.end1.ratedU, 0);
        Complex a1 = new Complex(1, 0);
        Complex a02 = new Complex(w2.branch.end2.ratedU / w2.branch.end1.ratedU, 0);
        Complex a2 = new Complex(1, 0);
        Complex a03 = new Complex(w3.branch.end2.ratedU / w3.branch.end1.ratedU, 0);
        Complex a3 = new Complex(1, 0);

        Complex ysh01 = new Complex(w1.branch.end2.g, w1.branch.end2.b);
        Complex ysh02 = new Complex(w2.branch.end2.g, w2.branch.end2.b);
        Complex ysh03 = new Complex(w3.branch.end2.g, w3.branch.end2.b);
        Complex y01 = ytr1.negate().divide(a01.conjugate().multiply(a1));
        Complex y02 = ytr2.negate().divide(a02.conjugate().multiply(a2));
        Complex y03 = ytr3.negate().divide(a03.conjugate().multiply(a3));
        Complex y0101 = ytr1.add(ysh01).divide(a01.conjugate().multiply(a01));
        Complex y0202 = ytr2.add(ysh02).divide(a02.conjugate().multiply(a02));
        Complex y0303 = ytr3.add(ysh03).divide(a03.conjugate().multiply(a03));

        Complex v0 = y01.multiply(v1).add(y02.multiply(v2)).add(y03.multiply(v3)).negate()
                .divide(y0101.add(y0202).add(y0303));

        Bus starBus = new Bus();
        starBus.u = v0.abs();
        starBus.theta = v0.getArgument();
        return starBus;
    }

    Bus calcStarBusFromVkSk(BranchTestCase w) {
        double r;
        double x;
        double g1;
        double b1;
        Complex a1;
        Complex a2;

        if (w.config.convertAsTransformer) {
            double rho0 = w.branch.end2.ratedU / w.branch.end1.ratedU;
            double rho0Square = rho0 * rho0;

            double r1 = w.branch.end1.r;
            double r2 = w.branch.end2.r;
            double x1 = w.branch.end1.x;
            double x2 = w.branch.end2.x;
            g1 = w.branch.end1.g;
            b1 = w.branch.end1.b;
            double g2 = w.branch.end2.g;
            double b2 = w.branch.end2.b;
            double r0 = r1 * rho0Square + r2;
            double x0 = x1 * rho0Square + x2;
            double g0 = g1 / rho0Square + g2;
            double b0 = b1 / rho0Square + b2;
            r = r0 * (1 + w.branch.end1.tap.r / 100);
            x = x0 * (1 + w.branch.end1.tap.x / 100);
            if (w.config.specificCompatibility) {
                g1 = g0 / 2 * (1 + w.branch.end1.tap.g / 100);
                b1 = b0 / 2 * (1 + w.branch.end1.tap.b / 100);
            } else {
                g1 = g0;
                b1 = b0;
            }
            double rho1 = rho0 * w.branch.end1.tap.rho;
            a1 = new Complex(1 / rho1, -w.branch.end1.tap.alpha);
            a2 = new Complex(1.0, 0.0);
        } else {
            r = w.branch.end1.r;
            x = w.branch.end1.x;
            g1 = w.branch.end1.g;
            b1 = w.branch.end1.b;
            a1 = new Complex(1 / w.branch.end1.tap.rho, -w.branch.end1.tap.alpha);
            double a02 = w.branch.end2.ratedU / w.branch.end1.ratedU;
            a2 = new Complex(a02 * 1 / w.branch.end2.tap.rho, -w.branch.end2.tap.alpha);
        }

        Complex v1 = ComplexUtils.polar2Complex(w.bus1.u, w.bus1.theta);
        Complex s1 = new Complex(w.expectedFlow1.p, w.expectedFlow1.q);
        Complex i1 = s1.divide(v1).conjugate();

        Complex ytr = new Complex(r, x).reciprocal();
        Complex y1 = new Complex(g1, b1);

        Complex y11 = ytr.add(y1).divide(a1.conjugate().multiply(a1));
        Complex y12 = ytr.divide(a1.conjugate().multiply(a2)).negate();

        Complex v2 = i1.subtract(y11.multiply(v1)).divide(y12);

        Bus starBus = new Bus();
        starBus.u = v2.abs();
        starBus.theta = v2.getArgument();

        return starBus;
    }

    private void checkBusBalance3wStarBus(
            BranchTestCase w1, BranchTestCase w2, BranchTestCase w3,
            Bus starBus,
            double toleranceBalance,
            String label) {
        w1.bus2 = starBus;
        w2.bus2 = starBus;
        w3.bus2 = starBus;
        BranchData r1 = checkTestCase(label, w1);
        BranchData r2 = checkTestCase(label, w2);
        BranchData r3 = checkTestCase(label, w3);
        Flow f1 = flow(r1, Side.TWO);
        Flow f2 = flow(r2, Side.TWO);
        Flow f3 = flow(r3, Side.TWO);
        checkBusBalance(label, toleranceBalance, f1, f2, f3);
    }

    private BranchData piModelFlows(BranchTestCase t) {
        // Compute final PI model values
        double r;
        double x;
        double g1;
        double b1;
        double g2;
        double b2;
        double rho1;
        double alpha1;
        double rho2;
        double alpha2;

        if (t.config.convertAsTransformer) {
            double rho0 = t.branch.end2.ratedU / t.branch.end1.ratedU;
            double rho0Square = rho0 * rho0;
            double r0 = t.branch.end1.r * rho0Square + t.branch.end2.r;
            double x0 = t.branch.end1.x * rho0Square + t.branch.end2.x;
            double g0 = t.branch.end1.g / rho0Square + t.branch.end2.g;
            double b0 = t.branch.end1.b / rho0Square + t.branch.end2.b;
            r = r0 * (1 + t.branch.end1.tap.r / 100);
            x = x0 * (1 + t.branch.end1.tap.x / 100);
            if (t.config.specificCompatibility) {
                g1 = g0 / 2 * (1 + t.branch.end1.tap.g / 100);
                b1 = b0 / 2 * (1 + t.branch.end1.tap.b / 100);
                g2 = g0 / 2;
                b2 = b0 / 2;
            } else {
                g1 = g0;
                b1 = b0;
                g2 = 0;
                b2 = 0;
            }
            rho1 = rho0 * t.branch.end1.tap.rho;
            rho2 = t.branch.end2.tap.rho;
            alpha1 = t.branch.end1.tap.alpha;
            alpha2 = t.branch.end2.tap.alpha;
        } else {
            r = t.branch.end1.r;
            x = t.branch.end1.x;
            g1 = t.branch.end1.g;
            b1 = t.branch.end1.b;
            g2 = t.branch.end2.g;
            b2 = t.branch.end2.b;
            rho1 = t.branch.end1.tap.rho;
            rho2 = t.branch.end2.tap.rho * t.branch.end1.ratedU / t.branch.end2.ratedU;
            alpha1 = t.branch.end1.tap.alpha;
            alpha2 = t.branch.end2.tap.alpha;
        }

        // Build final PI model
        // (and compute flows on it)
        return new BranchData(t.branch.id,
                r, x,
                rho1, rho2,
                t.bus1.u, t.bus2.u, t.bus1.theta, t.bus2.theta,
                alpha1, alpha2,
                g1, g2, b1, b2,
                t.expectedFlow1.p, t.expectedFlow1.q, t.expectedFlow2.p, t.expectedFlow2.q,
                t.branch.end1.connected, t.branch.end2.connected,
                t.bus1.mainComponent, t.bus2.mainComponent,
                t.config.epsilonX, t.config.applyReactanceCorrection);
    }

    private void checkBusBalance(String title, double tolerance, Flow... flows) {
        checkBusBalance(title, tolerance, tolerance, flows);
    }

    private void checkBusBalance(String title, double ptol, double qtol, Flow... flows) {
        logBusBalance(title, flows);
        Flow mismatch = sum(flows);
        assertEquals(0, mismatch.p, ptol);
        assertEquals(0, mismatch.q, qtol);
    }

    private void logTestCase(String title, BranchTestCase t, BranchData b) {
        if (!LOG.isDebugEnabled()) {
            return;
        }
        LOG.debug("");
        LOG.debug("Results for " + title + " branch " + b.getId());
        LOG.debug("End1");
        LOG.debug(String.format("    V          = %14.6f  %14.6f",
                b.getComputedU1(),
                Math.toDegrees(b.getComputedTheta1())));
        LOG.debug(String.format("    S expected = %14.6f  %14.6f",
                t.expectedFlow1.p,
                t.expectedFlow1.q));
        LOG.debug(String.format("    S actual   = %14.6f  %14.6f",
                b.getComputedP1(),
                b.getComputedQ1()));
        LOG.debug(String.format("    diff       = %14.6f  %14.6f",
                Math.abs(t.expectedFlow1.p - b.getComputedP1()),
                Math.abs(t.expectedFlow1.q - b.getComputedQ1())));
        LOG.debug(String.format("    tolerance  = %14.6f  %14.6f",
                t.config.toleranceFlow,
                t.config.toleranceFlow));
        LOG.debug("End2");
        LOG.debug(String.format("    V          = %14.6f  %14.6f",
                b.getComputedU2(),
                Math.toDegrees(b.getComputedTheta2())));
        LOG.debug(String.format("    S expected = %14.6f  %14.6f",
                t.expectedFlow2.p,
                t.expectedFlow2.q));
        LOG.debug(String.format("    S actual   = %14.6f  %14.6f",
                b.getComputedP2(),
                b.getComputedQ2()));
        LOG.debug(String.format("    diff       = %14.6f  %14.6f",
                Math.abs(t.expectedFlow2.p - b.getComputedP2()),
                Math.abs(t.expectedFlow2.q - b.getComputedQ2())));
        LOG.debug(String.format("    tolerance  = %14.6f  %14.6f",
                t.config.toleranceFlow,
                t.config.toleranceFlow));
    }

    private void logBusBalance(String title, Flow... flows) {
        if (!LOG.isDebugEnabled()) {
            return;
        }
        LOG.debug(title);
        for (Flow f : flows) {
            LOG.debug(String.format("    %12.6f  %12.6f  %s", f.p, f.q, f.id));
        }
        Flow sum = sum(flows);
        LOG.debug(String.format("    %12.6f  %12.6f  %s", sum.p, sum.q, sum.id));
    }

    void logVoltage(String label, Bus bus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("%12.6f  %12.6f  %s", bus.u, Math.toDegrees(bus.theta), label));
        }
    }

    private Flow sum(Flow... flows) {
        Flow sum = new Flow();
        sum.id = "SUM";
        for (Flow f : flows) {
            sum.p += f.p;
            sum.q += f.q;
        }
        return sum;
    }

    private Flow flow(BranchData b, Side side) {
        Flow f = new Flow();
        f.id = b.getId();
        f.p = b.getComputedP(side);
        f.q = b.getComputedQ(side);
        return f;
    }

    static class BranchTestCase {
        BranchTwoEnds branch = new BranchTwoEnds();
        Bus bus1 = new Bus();
        Bus bus2 = new Bus();
        Flow expectedFlow1 = new Flow();
        Flow expectedFlow2 = new Flow();
        Config config = new Config();
    }

    static class BranchTwoEnds {
        String id;
        BranchEnd end1 = new BranchEnd();
        BranchEnd end2 = new BranchEnd();
    }

    static class BranchEnd {
        double ratedU = 1;
        double r;
        double x;
        double g = 0;
        double b = 0;
        Tap tap = new Tap();
        boolean connected = true;
    }

    static class Tap {
        double rho = 1;
        double alpha = 0;
        double r = 0;
        double x = 0;
        double g = 0;
        double b = 0;
    }

    static class Bus {
        double u;
        double theta;
        boolean mainComponent = true;
    }

    static class Flow {
        String id;
        double p;
        double q;
    }

    static class Config {
        boolean convertAsTransformer = true;
        double toleranceFlow = TOLERANCE_FLOW;
        double toleranceVoltage = TOLERANCE_VOLTAGE;
        boolean specificCompatibility = true;
        boolean applyReactanceCorrection = false;
        double epsilonX;
    }

    private static final Logger LOG = LoggerFactory.getLogger(BranchDataTest.class);

    private static final double TOLERANCE_VOLTAGE = 0.001;
    private static final double TOLERANCE_FLOW = 0.01;
    private static final double TOLERANCE_BALANCE_EXACT = 1e-10;
    private static final double TOLERANCE_BALANCE_3W_STAR_BUS = 0.8;
    private static final double TOLERANCE_FLOW_WHEN_3W_STAR_BUS_FROM_VS = 0.03;
    private static final double TOLERANCE_FLOW_WHEN_3W_STAR_BUS_FROM_V1V2V3Y = 0.3;
}
