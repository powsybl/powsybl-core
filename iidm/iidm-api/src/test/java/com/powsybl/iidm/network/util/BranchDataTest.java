package com.powsybl.iidm.network.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.TwoTerminalsConnectable.Side;

public class BranchDataTest {

    private BranchTestCase cas2EntsoeLoadFlowExplicitLine;
    private BranchTestCase cas2EntsoeLoadFlowExplicitPhaseShiftTransformer;
    private Flow           cas2EntsoeLoadFlowExplicitLoad;
    private Flow           cas2EntsoeLoadFlowExplicitGenerator;

    @Before
    public void setUp() {
        BranchTestCase t;

        // Input data from ENTSO-E CASv2.0 test configuration ENTSOE_LoadFlowExplicit

        t = new BranchTestCase();
        t.branch.id = "FFNOD0L41__FNO";
        t.branch.end1.ratedU = 380;
        t.branch.end2.ratedU = 380;
        t.branch.end1.r = 4.1956;
        t.branch.end1.x = 12.73;
        t.bus1.u = 395.906724888442;
        t.bus2.u = 397.1;
        t.bus1.theta = Math.toRadians(-2.717121983205);
        t.bus2.theta = Math.toRadians(0);
        t.expectedFlow1.p = -534.9869;
        t.expectedFlow1.q = 153.1046;
        t.expectedFlow2.p = 543.2755;
        t.expectedFlow2.q = -127.9559;
        cas2EntsoeLoadFlowExplicitLine = t;

        t = new BranchTestCase();
        t.branch.id = "FNOD041__FNOD021__1_PT";
        t.branch.end1.ratedU = 380;
        t.branch.end2.ratedU = 380;
        t.branch.end1.r = 4.1956;
        t.branch.end1.x = 12.73;
        t.bus1.u = 395.906724888442;
        t.bus2.u = 397.1;
        t.bus1.theta = Math.toRadians(-2.717121983205);
        t.bus2.theta = Math.toRadians(0);
        t.expectedFlow1.p = 202.9869;
        t.expectedFlow1.q = -75.1046;
        t.expectedFlow2.p = -201.7355;
        t.expectedFlow2.q = 78.9091;

        t.branch.end2.tap.rho = 0.997829;
        t.branch.end2.tap.alpha = Math.toRadians(-3.77605);
        cas2EntsoeLoadFlowExplicitPhaseShiftTransformer = t;

        cas2EntsoeLoadFlowExplicitLoad = new Flow();
        cas2EntsoeLoadFlowExplicitLoad.id = "Load";
        cas2EntsoeLoadFlowExplicitLoad.p = 332.0;
        cas2EntsoeLoadFlowExplicitLoad.q = -78.0;

        cas2EntsoeLoadFlowExplicitGenerator = new Flow();
        cas2EntsoeLoadFlowExplicitGenerator.id = "Generator";
        cas2EntsoeLoadFlowExplicitGenerator.p = -341.54;
        cas2EntsoeLoadFlowExplicitGenerator.q = 49.0468;
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitLine() {
        checkTestCase(cas2EntsoeLoadFlowExplicitLine);
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitPhaseShiftTransformer() {
        checkTestCase(cas2EntsoeLoadFlowExplicitPhaseShiftTransformer);
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitLineAndPhaseShifter() {
        BranchTestCase line = cas2EntsoeLoadFlowExplicitLine;
        BranchTestCase pst = cas2EntsoeLoadFlowExplicitPhaseShiftTransformer;
        Flow load = cas2EntsoeLoadFlowExplicitLoad;
        Flow generator = cas2EntsoeLoadFlowExplicitGenerator;

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
        LOG.info("");
        LOG.info("Balance at ends of parallel branches " + pline.getId() + ", " + ppst.getId());
        Flow line1 = flow(pline, Side.ONE);
        Flow line2 = flow(pline, Side.TWO);
        Flow pst1 = flow(ppst, Side.ONE);
        Flow pst2 = flow(ppst, Side.TWO);
        checkBusBalance("End 1", 1e-10, 1e-10, line1, pst1, load);
        checkBusBalance("End 2", 1e-2, 1e-4, line2, pst2, generator);
    }

    @Test
    public void testCAS2EntsoeLoadFlowExplicitLineAndPhaseShifterMovedToEnd1() {
        BranchTestCase line = cas2EntsoeLoadFlowExplicitLine;
        BranchTestCase pst = cas2EntsoeLoadFlowExplicitPhaseShiftTransformer;
        Flow load = cas2EntsoeLoadFlowExplicitLoad;
        Flow generator = cas2EntsoeLoadFlowExplicitGenerator;

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

        LOG.info("");
        LOG.info("Balance at ends of parallel branches " + pline.getId() + ", " + ppst.getId());
        LOG.info("After moving phase shifter to side 1");
        Flow line1 = flow(pline, Side.ONE);
        Flow line2 = flow(pline, Side.TWO);
        Flow pst1 = flow(ppst, Side.ONE);
        Flow pst2 = flow(ppst, Side.TWO);
        checkBusBalance("End 1", 1e-10, 1e-10, line1, pst1, load);
        checkBusBalance("End 2", 1e-2, 1e-4, line2, pst2, generator);
    }

    private void checkTestCase(BranchTestCase t) {
        BranchData b = piModelFlows(t);
        LOG.info("");
        LOG.info("Results for " + b.getId());
        LOG.info(String.format("   end1 computed = %14.6f  %14.6f",
                b.getComputedP1(),
                b.getComputedQ1()));
        LOG.info(String.format("   end1 expected = %14.6f  %14.6f",
                t.expectedFlow1.p,
                t.expectedFlow1.q));
        LOG.info(String.format("   end1 diff     = %14.6f  %14.6f",
                Math.abs(t.expectedFlow1.p - b.getComputedP1()),
                Math.abs(t.expectedFlow1.q - b.getComputedQ1())));
        assertEquals(t.expectedFlow1.p, b.getComputedP1(), t.config.tolerance);
        assertEquals(t.expectedFlow2.p, b.getComputedP2(), t.config.tolerance);
        assertEquals(t.expectedFlow1.q, b.getComputedQ1(), t.config.tolerance);
        assertEquals(t.expectedFlow2.q, b.getComputedQ2(), t.config.tolerance);
    }

    private BranchData piModelFlows(BranchTestCase t) {
        // Compute final PI model values
        double rho0 = t.branch.end2.ratedU / t.branch.end1.ratedU;
        double rho0Square = rho0 * rho0;
        double r0 = t.branch.end1.r * rho0Square + t.branch.end2.r;
        double x0 = t.branch.end1.x * rho0Square + t.branch.end2.x;
        double g0 = t.branch.end1.g / rho0Square + t.branch.end2.g;
        double b0 = t.branch.end1.b / rho0Square + t.branch.end2.b;
        double r = r0 * (1 + t.branch.end1.tap.r / 100);
        double x = x0 * (1 + t.branch.end1.tap.x / 100);
        double g1;
        double b1;
        double g2;
        double b2;
        if (t.config.specificCompatibility) {
            g1 = g0 / 2 * (1 + t.branch.end1.tap.g / 100);
            b1 = b0 / 2 * (1 + t.branch.end1.tap.b / 100);
            g2 = b0 / 2;
            b2 = b0 / 2;
        } else {
            g1 = g0;
            b1 = b0;
            g2 = 0;
            b2 = 0;
        }
        double rho1 = rho0 * t.branch.end1.tap.rho;
        double rho2 = t.branch.end2.tap.rho;
        double alpha1 = t.branch.end1.tap.alpha;
        double alpha2 = t.branch.end2.tap.alpha;

        // Build final PI model
        // (and compute flows on it)
        return new BranchData(t.branch.id,
                r, x,
                rho1, rho2,
                t.bus1.u, t.bus2.u, t.bus1.theta, t.bus2.theta,
                alpha1, alpha2,
                g1, g2, b1, b2,
                t.expectedFlow1.p, t.expectedFlow1.q, t.expectedFlow2.p, t.expectedFlow2.q,
                t.bus1.connected, t.bus2.connected,
                t.bus1.mainComponent, t.bus2.mainComponent,
                t.config.epsilonX, t.config.applyReactanceCorrection);
    }

    private void checkBusBalance(String title, double ptol, double qtol, Flow... flows) {
        logBusBalance(title, flows);
        Flow mismatch = sum(flows);
        assertEquals(0, mismatch.p, ptol);
        assertEquals(0, mismatch.q, qtol);
    }

    private void logBusBalance(String title, Flow... flows) {
        LOG.info(title);
        for (Flow f : flows) {
            LOG.info(String.format("    %12.6f  %12.6f  %s", f.p, f.q, f.id));
        }
        Flow sum = sum(flows);
        LOG.info(String.format("    %12.6f  %12.6f  %s", sum.p, sum.q, sum.id));
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
        double ratedU = 1;
        double r;
        double x;
        double g      = 0;
        double b      = 0;
        Tap    tap    = new Tap();
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
        boolean connected     = true;
        boolean mainComponent = true;
    }

    static class Flow {
        String id;
        double p;
        double q;
    }

    static class Config {
        double  tolerance                = 0.01;
        boolean specificCompatibility    = true;
        boolean applyReactanceCorrection = false;
        double  epsilonX;
    }

    private static final Logger LOG = LoggerFactory.getLogger(BranchDataTest.class);
}
