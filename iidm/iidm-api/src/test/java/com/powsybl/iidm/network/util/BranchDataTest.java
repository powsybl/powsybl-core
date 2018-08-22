/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import static org.junit.Assert.assertEquals;

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

        // The expected values at the other end will be the same when we disconnect end 1 or end 2
        // If we use the same voltage at the connected end
        double expectedU = 381.9095;
        double expectedTheta = -0.000503;

        // First obtain results when end 2 is disconnected
        BranchData b2disconnected = checkTestCase("End 2 disconnected", t);
        assertEquals(expectedU, b2disconnected.getComputedU2(), t.config.tolerance);
        assertEquals(expectedTheta, b2disconnected.getComputedTheta2(), t.config.tolerance);

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
        assertEquals(expectedU, b1disconnected.getComputedU1(), t.config.tolerance);
        assertEquals(expectedTheta, b1disconnected.getComputedTheta1(), t.config.tolerance);
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
        assertEquals(expectedU, b2disconnected.getComputedU2(), t.config.tolerance);
        assertEquals(expectedTheta, b2disconnected.getComputedTheta2(), t.config.tolerance);

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
        assertEquals(expectedU, b1disconnected.getComputedU1(), t.config.tolerance);
        assertEquals(expectedTheta, b1disconnected.getComputedTheta1(), t.config.tolerance);
    }

    private BranchTestCase lineEnd2Disconnected() {
        BranchTestCase t = new BranchTestCase();
        t.config.asTransformer = false;

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

    // Test cases related to ENTSO-E CASv2.0 test configuration ENTSOE_LoadFlowExplicit
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
        // and voltages have been copied from Excel documentation with sufficient precision
        // At end2 (generator, slack bus) the mismatch should be small
        // generator injection in P and Q are provided with low precision
        // (P with 10e-2, Q with 10e-4)
        LOG.debug("");
        LOG.debug("Balance at ends of parallel branches " + pline.getId() + ", " + ppst.getId());
        Flow line1 = flow(pline, Side.ONE);
        Flow line2 = flow(pline, Side.TWO);
        Flow pst1 = flow(ppst, Side.ONE);
        Flow pst2 = flow(ppst, Side.TWO);
        checkBusBalance("End 1", 1e-10, 1e-10, line1, pst1, load);
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
        checkBusBalance("End 1", 1e-10, 1e-10, line1, pst1, load);
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

    private BranchData checkTestCase(String title, BranchTestCase t) {
        BranchData b = piModelFlows(t);
        logTestCase(title, t, b);
        assertEquals(t.expectedFlow1.p, b.getComputedP1(), t.config.tolerance);
        assertEquals(t.expectedFlow2.p, b.getComputedP2(), t.config.tolerance);
        assertEquals(t.expectedFlow1.q, b.getComputedQ1(), t.config.tolerance);
        assertEquals(t.expectedFlow2.q, b.getComputedQ2(), t.config.tolerance);
        return b;
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

        if (t.config.asTransformer) {
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
            rho2 = t.branch.end2.tap.rho;
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

    private void checkBusBalance(String title, double ptol, double qtol, Flow... flows) {
        logBusBalance(title, flows);
        Flow mismatch = sum(flows);
        assertEquals(0, mismatch.p, ptol);
        assertEquals(0, mismatch.q, qtol);
    }

    private void logTestCase(String title, BranchTestCase t, BranchData b) {
        LOG.debug("");
        LOG.debug("Results for " + title + " branch " + b.getId());
        LOG.debug("End1");
        LOG.debug(String.format("    V          = %14.6f  %14.6f",
                b.getComputedU1(),
                b.getComputedTheta1()));
        LOG.debug(String.format("    S expected = %14.6f  %14.6f",
                t.expectedFlow1.p,
                t.expectedFlow1.q));
        LOG.debug(String.format("    S actual   = %14.6f  %14.6f",
                b.getComputedP1(),
                b.getComputedQ1()));
        LOG.debug(String.format("    diff       = %14.6f  %14.6f",
                Math.abs(t.expectedFlow1.p - b.getComputedP1()),
                Math.abs(t.expectedFlow1.q - b.getComputedQ1())));
        LOG.debug("End2");
        LOG.debug(String.format("    V          = %14.6f  %14.6f",
                b.getComputedU2(),
                b.getComputedTheta2()));
        LOG.debug(String.format("    S expected = %14.6f  %14.6f",
                t.expectedFlow2.p,
                t.expectedFlow2.q));
        LOG.debug(String.format("    S actual   = %14.6f  %14.6f",
                b.getComputedP2(),
                b.getComputedQ2()));
        LOG.debug(String.format("    diff       = %14.6f  %14.6f",
                Math.abs(t.expectedFlow2.p - b.getComputedP2()),
                Math.abs(t.expectedFlow2.q - b.getComputedQ2())));
    }

    private void logBusBalance(String title, Flow... flows) {
        LOG.debug(title);
        for (Flow f : flows) {
            LOG.debug(String.format("    %12.6f  %12.6f  %s", f.p, f.q, f.id));
        }
        Flow sum = sum(flows);
        LOG.debug(String.format("    %12.6f  %12.6f  %s", sum.p, sum.q, sum.id));
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
        BranchTwoEnds branch        = new BranchTwoEnds();
        Bus           bus1          = new Bus();
        Bus           bus2          = new Bus();
        Flow          expectedFlow1 = new Flow();
        Flow          expectedFlow2 = new Flow();
        Config        config        = new Config();
    }

    static class BranchTwoEnds {
        String    id;
        BranchEnd end1 = new BranchEnd();
        BranchEnd end2 = new BranchEnd();
    }

    static class BranchEnd {
        double  ratedU    = 1;
        double  r;
        double  x;
        double  g         = 0;
        double  b         = 0;
        Tap     tap       = new Tap();
        boolean connected = true;
    }

    static class Tap {
        double rho   = 1;
        double alpha = 0;
        double r     = 0;
        double x     = 0;
        double g     = 0;
        double b     = 0;
    }

    static class Bus {
        double  u;
        double  theta;
        boolean mainComponent = true;
    }

    static class Flow {
        String id;
        double p;
        double q;
    }

    static class Config {
        boolean asTransformer            = true;
        double  tolerance                = 0.01;
        boolean specificCompatibility    = true;
        boolean applyReactanceCorrection = false;
        double  epsilonX;
    }

    private static final Logger LOG = LoggerFactory.getLogger(BranchDataTest.class);
}
