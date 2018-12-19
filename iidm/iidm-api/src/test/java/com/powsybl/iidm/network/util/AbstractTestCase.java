package com.powsybl.iidm.network.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.PowsyblException;

public abstract class AbstractTestCase {
    String label;
    Config config = new Config();

    abstract void test();

    static class BranchTestCase extends AbstractTestCase {
        BranchEnd end1 = new BranchEnd();
        BranchEnd end2 = new BranchEnd();
        BranchData branchData;

        @Override
        void test() {
            branchData = piModelFlows();
            log();
            Flow.assertEquals(end1.expectedFlow, Flow.get(branchData, 1), config.toleranceFlow);
            Flow.assertEquals(end2.expectedFlow, Flow.get(branchData, 2), config.toleranceFlow);
        }

        BranchTestCase convertToIidm() {
            // Only convert test cases with parameters defined according to CGMES
            if (!config.paramsCgmes) {
                throw new PowsyblException("Test case does not have CGMES parameters. Can not convert to IIDM");
            }
            BranchTestCase tiidm = new BranchTestCase();
            tiidm.label = this.label + " converted to IIDM";
            // The result test case do not follow CGMES conventions for parameters
            tiidm.config = config.clone();
            tiidm.config.paramsCgmes = false;
            // The ratio between ratedU is considered at end 1
            tiidm.config.ratioRatedUatEnd = 1;
            // Same voltages and expected flows at ends
            tiidm.end1.voltage = this.end1.voltage;
            tiidm.end2.voltage = this.end2.voltage;
            tiidm.end1.expectedFlow = this.end1.expectedFlow;
            tiidm.end2.expectedFlow = this.end2.expectedFlow;
            tiidm.end1.ratedU = this.end1.ratedU;
            tiidm.end2.ratedU = this.end2.ratedU;
            // We assume parameters have been given only in end1
            // First the correction for changing the side where the ideal
            // ratio corresponding to rated voltages is interpreted
            double rho10 = this.end2.ratedU / this.end1.ratedU;
            double rho10square = rho10 * rho10;
            double r0 = this.end1.r * rho10square;
            double x0 = this.end1.x * rho10square;
            double g0 = this.end1.g / rho10square + this.end2.g;
            double b0 = this.end1.b / rho10square + this.end2.b;
            // Additional correction for the fact that we are moving
            // current end2.tap.rho to the end1
            double rho2square = this.end2.tap.rho * this.end2.tap.rho;
            double dz2 = 100 * (1 / rho2square - 1);
            double dy2 = 100 * (rho2square - 1);
            // The current tap at end1 could have also impedance corrections
            tiidm.end1.r = r0 * (1 + this.end1.tap.r / 100) * (1 + dz2 / 100);
            tiidm.end1.x = x0 * (1 + this.end1.tap.x / 100) * (1 + dz2 / 100);
            double g = g0 * (1 + this.end1.tap.g / 100) * (1 + dy2 / 100);
            double b = b0 * (1 + this.end1.tap.b / 100) * (1 + dy2 / 100);
            if (this.config.splitMagnetizingAdmittance) {
                tiidm.end1.g = g / 2;
                tiidm.end1.b = b / 2;
                tiidm.end2.g = g / 2;
                tiidm.end2.b = b / 2;
            } else {
                tiidm.end1.g = g;
                tiidm.end1.b = b;
                tiidm.end2.g = 0;
                tiidm.end2.b = 0;
            }
            // All ratio change has been moved to end1
            // Ratio from ratedU should be considered at end1
            tiidm.end1.tap.rho = this.end1.tap.rho * (1 / this.end2.tap.rho);
            tiidm.end1.tap.alpha = this.end1.tap.alpha - this.end2.tap.alpha;
            tiidm.end2.tap.rho = 1;
            tiidm.end2.tap.alpha = 0;
            return tiidm;
        }

        Complex z() {
            double dr = (1 + end1.tap.r / 100) * (1 + end2.tap.r / 100);
            double dx = (1 + end1.tap.x / 100) * (1 + end2.tap.r / 100);
            // Assume total impedance transmission is given at end 1
            return new Complex(end1.r * dr, end1.x * dx);
        }

        Complex ratio(int end) {
            double rho = (end == 1) ? end1.tap.rho : end2.tap.rho;
            double alpha = (end == 1) ? end1.tap.alpha : end2.tap.alpha;
            // Correction if the ratio between ratedU is considered at this end
            if (end == config.ratioRatedUatEnd && end1.ratedU != end2.ratedU) {
                rho = rho * (end == 1 ? end2.ratedU / end1.ratedU : end1.ratedU / end2.ratedU);
            }
            return ComplexUtils.polar2Complex(rho, alpha).reciprocal();
        }

        Complex ysh(int end) {
            double dg = (1 + end1.tap.g / 100) * (1 + end2.tap.g / 100);
            double db = (1 + end1.tap.b / 100) * (1 + end2.tap.g / 100);
            if (config.paramsCgmes) {
                double g = end1.g * dg;
                double b = end1.b * db;
                if (config.splitMagnetizingAdmittance) {
                    return new Complex(g / 2, b / 2);
                } else {
                    // FIXME(Luma) according to the CGMES documentation
                    // the magnetizing admittance is modeled as a shunt admittance to
                    // ground at the left side of the impedance transmission
                    // If we assume the "primary" side in the "primary" winding is
                    // in the network (not in the star bus) we obtain worst results
                    // that if we assume the "primary" side of the "primary" winding is at the star
                    // bus ...
                    // return end == 1 ? new Complex(g, b) : Complex.ZERO;
                    return end == 1 ? Complex.ZERO : new Complex(g, b);
                }
            } else {
                double g = end == 1 ? end1.g : end2.g;
                double b = end == 1 ? end1.b : end2.b;
                return new Complex(g * dg, b * db);
            }
        }

        BranchData piModelFlows() {
            // Obtain PI model values and compute flows
            Complex z = z();
            double r = z.getReal();
            double x = z.getImaginary();
            double g1 = ysh(1).getReal();
            double b1 = ysh(1).getImaginary();
            double g2 = ysh(2).getReal();
            double b2 = ysh(2).getImaginary();
            double rho1 = ratio(1).reciprocal().abs();
            double alpha1 = ratio(1).reciprocal().getArgument();
            double rho2 = ratio(2).reciprocal().abs();
            double alpha2 = ratio(2).reciprocal().getArgument();

            // Build final PI model
            // (and compute flows on it)
            boolean mainComponent = true;
            return new BranchData(label,
                    r, x,
                    rho1, rho2,
                    end1.voltage.u, end2.voltage.u, end1.voltage.theta, end2.voltage.theta,
                    alpha1, alpha2,
                    g1, g2, b1, b2,
                    end1.expectedFlow.p, end1.expectedFlow.q, end2.expectedFlow.p, end2.expectedFlow.q,
                    end1.connected, end2.connected,
                    mainComponent, mainComponent,
                    config.epsilonX, config.applyReactanceCorrection);
        }

        @Override
        void log() {
            if (!LOG.isInfoEnabled()) {
                return;
            }
            super.log();
            log(end1, 1);
            log(end2, 2);
        }

        void log(BranchEnd end, int endNumber) {
            LOG.info("End " + endNumber);
            Voltage v = Voltage.get(branchData, endNumber);
            Flow sexpected = (endNumber == 1 ? end1 : end2).expectedFlow;
            Flow sactual = Flow.get(branchData, endNumber);
            Flow error = Flow.error(sexpected, sactual);
            v.log(
                    "    V          = ");
            sexpected.log(
                    "    S expected = ");
            sactual.log(
                    "    S actual   = ");
            error.log(
                    "    S error    = ");
            AbstractTestCase.log(
                    "    tolerance  = ", config.toleranceFlow, config.toleranceFlow);
        }
    }

    static class DanglingLineTestCase extends BranchTestCase {
        Voltage expectedVoltageAtDisconnectedEnd = new Voltage();

        @Override
        void test() {
            calcUnknowns();
            super.test();
            Voltage actualVoltageAtDisconnectedEnd = end1.connected
                    ? Voltage.get(branchData, 2)
                    : Voltage.get(branchData, 1);
            Voltage.assertEquals(
                    expectedVoltageAtDisconnectedEnd,
                    actualVoltageAtDisconnectedEnd,
                    config.toleranceVoltage);
        }

        private void calcUnknowns() {
            Complex ytr = z().reciprocal();
            Complex y1 = ysh(1);
            Complex y2 = ysh(2);
            Complex a1 = ratio(1);
            Complex a2 = ratio(2);

            Complex y11 = ytr.add(y1).divide(a1.conjugate().multiply(a1));
            Complex y12 = ytr.negate().divide(a1.conjugate().multiply(a2));
            Complex y21 = ytr.negate().divide(a2.conjugate().multiply(a1));
            Complex y22 = ytr.add(y2).divide(a2.conjugate().multiply(a2));

            // Unknowns are the power flow at connected end and voltage at disconnected end
            Complex v;
            if (!end2.connected) {
                // We know the voltage at end 1 (V1)
                // We want to know the flow at end 1 (S1)
                // when the end 2 is disconnected.
                // That is, when current at end 2 is zero
                //
                // On the equations for the currents
                // I1 = Y11 * V1 + Y12 * V2
                // I2 = Y21 * V1 + Y22 * V2
                // We impose the condition I2 = 0, we have
                // V2 = -Y21 * V1 / Y22
                // And substituting this expression in the equation for I1 we have
                // I1 = V1 * (Y11 - Y12 * Y21 / Y22)

                Complex v1 = end1.voltage.asComplex();
                Complex i1 = v1.multiply(y11.subtract(y12.multiply(y21).divide(y22)));
                Complex s1 = v1.multiply(i1.conjugate());
                v = y21.negate().multiply(v1).divide(y22);
                end1.expectedFlow = new Flow(s1);
            } else {
                assertFalse(end1.connected);
                Complex v2 = end2.voltage.asComplex();
                Complex i2 = v2.multiply(y22.subtract(y12.multiply(y21).divide(y11)));
                Complex s2 = v2.multiply(i2.conjugate());
                v = y12.negate().multiply(v2).divide(y11);
                end2.expectedFlow = new Flow(s2);
            }
            expectedVoltageAtDisconnectedEnd = new Voltage(v);
        }
    }

    static class ThreeWindingsTransformerTestCase extends AbstractTestCase {
        private static final double TOLERANCE_BALANCE_STAR = 0.8;
        private static final double TOLERANCE_FLOW_STAR_FROM_VS = 0.03;
        private static final double TOLERANCE_FLOW_STAR_FROM_V1V2V3Y = 0.3;

        private double toleranceVoltage;
        private double toleranceBalanceStar = TOLERANCE_BALANCE_STAR;
        private double toleranceFlowStarFromVS = TOLERANCE_FLOW_STAR_FROM_VS;
        private double toleranceFlowStarFromV1V2V3Y = TOLERANCE_FLOW_STAR_FROM_V1V2V3Y;

        private BranchTestCase w1;
        private BranchTestCase w2;
        private BranchTestCase w3;

        ThreeWindingsTransformerTestCase(String label, BranchTestCase w1, BranchTestCase w2, BranchTestCase w3) {
            this.label = label;
            this.w1 = w1;
            this.w2 = w2;
            this.w3 = w3;
            // By default use the same tolerance voltage of primary
            this.toleranceVoltage = w1.config.toleranceVoltage;
        }

        ThreeWindingsTransformerTestCase setToleranceVoltage(double tol) {
            this.toleranceVoltage = tol;
            return this;
        }
        
        ThreeWindingsTransformerTestCase setToleranceBalanceStar(double tol) {
            this.toleranceBalanceStar = tol;
            return this;
        }
        
        ThreeWindingsTransformerTestCase setToleranceFlowStarFromVS(double tol) {
            this.toleranceFlowStarFromVS = tol;
            return this;
        }
        
        ThreeWindingsTransformerTestCase setToleranceFlowStarFromV1V2V3Y(double tol) {
            this.toleranceFlowStarFromV1V2V3Y = tol;
            return this;
        }
        
        @Override
        void test() {
            super.log();

            ThreeWindingsTransformerTestCase iidm = convertToIiidm();
            String label1 = String.format("star voltage from V1, S1 (%3s) ", w1.label);
            String label2 = String.format("star voltage from V2, S2 (%3s) ", w2.label);
            String label3 = String.format("star voltage from V3, S3 (%3s) ", w3.label);
            String label123 = "star voltage from V1, V2, V3, Y";

            // Determine the voltage at the star bus, v0, in different ways
            Voltage v01 = calcStarVFromVkSk(w1);
            Voltage v02 = calcStarVFromVkSk(w2);
            Voltage v03 = calcStarVFromVkSk(w3);
            Voltage v0V1V2V3Y = calcStarVFromV1V2V3Y();
            Voltage v01iidm = calcStarVFromVkSk(iidm.w1);
            Voltage v02iidm = calcStarVFromVkSk(iidm.w2);
            Voltage v03iidm = calcStarVFromVkSk(iidm.w3);
            Voltage v0V1V2V3Yiidm = iidm.calcStarVFromV1V2V3Y();

            LOG.info("");
            LOG.info("Using original CGMES model");
            v01.log(label1);
            v02.log(label2);
            v03.log(label3);
            v0V1V2V3Y.log(label123);
            LOG.info("");
            LOG.info("Using converted IIDM model");
            v01iidm.log(label1);
            v02iidm.log(label2);
            v03iidm.log(label3);
            v0V1V2V3Yiidm.log(label123);

            // Ensure the voltage of the star bus is similar
            // when it is computed using the different alternatives
            Voltage.assertEquals(v01, v02, toleranceVoltage);
            Voltage.assertEquals(v01, v03, toleranceVoltage);
            Voltage.assertEquals(v01, v0V1V2V3Y, toleranceVoltage);
            Voltage.assertEquals(v01, v02iidm, toleranceVoltage);
            Voltage.assertEquals(v01, v03iidm, toleranceVoltage);
            Voltage.assertEquals(v01, v0V1V2V3Yiidm, toleranceVoltage);

            // Check bus balance at star bus using the different (similar) voltages
            test(v01, 1, label1);
            test(v02, 2, label2);
            test(v03, 3, label3);
            test(v0V1V2V3Y, 0, label123);
        }

        private ThreeWindingsTransformerTestCase convertToIiidm() {
            BranchTestCase w1iidm = w1.convertToIidm();
            BranchTestCase w2iidm = w2.convertToIidm();
            BranchTestCase w3iidm = w3.convertToIidm();
            String label = this.label + " converted to IIDM";
            return new ThreeWindingsTransformerTestCase(label, w1iidm, w2iidm, w3iidm);
        }

        private void test(Voltage starVoltage, int vksk, String label) {
            LOG.info("");
            LOG.info(label);
            w1.end2.voltage = starVoltage;
            w2.end2.voltage = starVoltage;
            w3.end2.voltage = starVoltage;

            // Test each winding
            w1.config.toleranceFlow = toleranceFlow(1, vksk);
            w2.config.toleranceFlow = toleranceFlow(2, vksk);
            w3.config.toleranceFlow = toleranceFlow(3, vksk);
            w1.test();
            w2.test();
            w3.test();
            Flow f1 = Flow.get(w1.branchData, 2);
            Flow f2 = Flow.get(w2.branchData, 2);
            Flow f3 = Flow.get(w3.branchData, 2);

            // When voltage at star bus is computed from voltage and flow at one end,
            // there will be a non-zero balance at star bus,
            // either because flows are not given with enough precision or because
            // the given solution already contained had a mismatch at star bus.
            // The balance at star bus must be exact if V0 is calculated from V1, V2, V3, Y
            double toleranceBalance = vksk == 0 ? BusBalance.EXACT : toleranceBalanceStar;
            new BusBalance("Balance at star bus, " + label, toleranceBalance, f1, f2, f3).test();
        }

        private double toleranceFlow(int endNumber, int starVoltageFromEndNumber) {
            if (endNumber > 0 && endNumber == starVoltageFromEndNumber) {
                // If we have used (Vk, Sk) to compute the star bus voltage,
                // then the winding k the flow must be exact
                return Flow.EXACT;
            } else if (starVoltageFromEndNumber > 0) {
                return toleranceFlowStarFromVS;
            } else if (starVoltageFromEndNumber == 0) {
                return toleranceFlowStarFromV1V2V3Y;
            }
            throw new PowsyblException("Bad endNumber");
        }

        private Voltage calcStarVFromV1V2V3Y() {
            Complex v1 = w1.end1.voltage.asComplex();
            Complex v2 = w2.end1.voltage.asComplex();
            Complex v3 = w3.end1.voltage.asComplex();
            Complex ytr1 = w1.z().reciprocal();
            Complex ytr2 = w2.z().reciprocal();
            Complex ytr3 = w3.z().reciprocal();
            Complex a1 = w1.ratio(1);
            Complex a2 = w2.ratio(1);
            Complex a3 = w3.ratio(1);
            Complex a01 = w1.ratio(2);
            Complex a02 = w2.ratio(2);
            Complex a03 = w3.ratio(2);
            Complex ysh01 = w1.ysh(2);
            Complex ysh02 = w2.ysh(2);
            Complex ysh03 = w3.ysh(2);

            Complex y01 = ytr1.negate().divide(a01.conjugate().multiply(a1));
            Complex y02 = ytr2.negate().divide(a02.conjugate().multiply(a2));
            Complex y03 = ytr3.negate().divide(a03.conjugate().multiply(a3));
            Complex y0101 = ytr1.add(ysh01).divide(a01.conjugate().multiply(a01));
            Complex y0202 = ytr2.add(ysh02).divide(a02.conjugate().multiply(a02));
            Complex y0303 = ytr3.add(ysh03).divide(a03.conjugate().multiply(a03));

            Complex v0 = y01.multiply(v1).add(y02.multiply(v2)).add(y03.multiply(v3)).negate()
                    .divide(y0101.add(y0202).add(y0303));

            return new Voltage(v0);
        }

        private static Voltage calcStarVFromVkSk(BranchTestCase w) {
            Complex v1 = w.end1.voltage.asComplex();
            Complex s1 = new Complex(w.end1.expectedFlow.p, w.end1.expectedFlow.q);

            Complex ytr = w.z().reciprocal();
            Complex y1 = w.ysh(1);
            Complex a1 = w.ratio(1);
            Complex a2 = w.ratio(2);
            Complex i1 = s1.divide(v1).conjugate();
            Complex y11 = ytr.add(y1).divide(a1.conjugate().multiply(a1));
            Complex y12 = ytr.divide(a1.conjugate().multiply(a2)).negate();

            Complex v2 = i1.subtract(y11.multiply(v1)).divide(y12);

            return new Voltage(v2);
        }
    }

    static class BranchEnd {
        double ratedU = 1;
        double r;
        double x;
        double g = 0;
        double b = 0;
        Tap tap = new Tap();
        boolean connected = true;
        Voltage voltage = new Voltage();
        Flow expectedFlow = new Flow();
    }

    static class Tap {
        double rho = 1;
        double alpha = 0;
        double r = 0;
        double x = 0;
        double g = 0;
        double b = 0;

        void forStep(int step, int neutralStep, double stepVoltageIncrement) {
            int n = step - neutralStep;
            double du = stepVoltageIncrement / 100;
            rho = 1 / (1 + n * du);
        }
    }

    static class Voltage {
        double u;
        double theta;
        static final Voltage UNKNOWN = new Voltage();
        static {
            UNKNOWN.u = Double.NaN;
            UNKNOWN.theta = Double.NaN;
        }

        public Voltage() {
        }

        public Voltage(Complex v) {
            u = v.abs();
            theta = v.getArgument();
        }

        public Complex asComplex() {
            return ComplexUtils.polar2Complex(u, theta);
        }

        public static Voltage get(BranchData branchData, int endNumber) {
            Voltage v = new Voltage();
            switch (endNumber) {
                case 1:
                    v.u = branchData.getComputedU1();
                    v.theta = branchData.getComputedTheta1();
                    break;
                case 2:
                    v.u = branchData.getComputedU2();
                    v.theta = branchData.getComputedTheta2();
                    break;
                default:
                    throw new PowsyblException("Bad endNumber");
            }
            return v;
        }

        static void assertEquals(Voltage expected, Voltage actual, double tolerance) {
            Assert.assertEquals(expected.u, actual.u, tolerance);
            Assert.assertEquals(expected.theta, actual.theta, tolerance);
        }

        void log(String label) {
            AbstractTestCase.log(label, u, Math.toDegrees(theta));
        }
    }

    static class Flow {
        static final Flow UNKNOWN = new Flow();
        static {
            UNKNOWN.p = Double.NaN;
            UNKNOWN.q = Double.NaN;
        }
        static final double EXACT = 1e-10;

        String label;
        double p;
        double q;

        public Flow() {
        }

        public Flow(Complex s) {
            p = s.getReal();
            q = s.getImaginary();
        }

        static Flow error(Flow f1, Flow f2) {
            Flow e = new Flow();
            e.p = Math.abs(f1.p - f2.p);
            e.q = Math.abs(f1.q - f2.q);
            return e;
        }

        static Flow sum(Flow... flows) {
            Flow sum = new Flow();
            sum.label = "SUM";
            for (Flow f : flows) {
                sum.p += f.p;
                sum.q += f.q;
            }
            return sum;
        }

        static Flow get(BranchData b, int endNumber) {
            Flow f = new Flow();
            f.label = b.getId();
            switch (endNumber) {
                case 1:
                    f.p = b.getComputedP1();
                    f.q = b.getComputedQ1();
                    break;
                case 2:
                    f.p = b.getComputedP2();
                    f.q = b.getComputedQ2();
                    break;
                default:
                    throw new PowsyblException("Bad endNumber");
            }
            return f;
        }

        static void assertEquals(Flow expected, Flow actual, double tolerance) {
            if (!Double.isNaN(expected.p)) {
                Assert.assertEquals(expected.p, actual.p, tolerance);
            }
            if (!Double.isNaN(expected.q)) {
                Assert.assertEquals(expected.q, actual.q, tolerance);
            }
        }

        void log(String label) {
            AbstractTestCase.log(label, p, q);
        }
    }

    static class Config implements Cloneable {
        static final double TOLERANCE_VOLTAGE = 0.01;
        static final double TOLERANCE_FLOW = 0.01;

        boolean paramsCgmes = true;
        int ratioRatedUatEnd = 2;
        boolean splitMagnetizingAdmittance = false;
        double toleranceFlow = TOLERANCE_FLOW;
        double toleranceVoltage = TOLERANCE_VOLTAGE;
        boolean applyReactanceCorrection = false;
        double epsilonX = 0;

        public Config clone() {
            try {
                return (Config) super.clone();
            } catch (CloneNotSupportedException x) {
                throw new PowsyblException("clone config", x);
            }
        }
    }

    static class BusBalance extends AbstractTestCase {
        static final double EXACT = 1e-10;

        double tolerancep;
        double toleranceq;
        Flow[] flows;

        BusBalance(String label, double tolerance, Flow... flows) {
            this(label, tolerance, tolerance, flows);
        }

        BusBalance(String label, double tolerancep, double toleranceq, Flow... flows) {
            this.label = label;
            this.tolerancep = tolerancep;
            this.toleranceq = toleranceq;
            this.flows = flows;
        }

        @Override
        void test() {
            log();
            Flow mismatch = Flow.sum(flows);
            assertEquals(0, mismatch.p, tolerancep);
            assertEquals(0, mismatch.q, toleranceq);
        }

        void log() {
            super.log();
            for (Flow f : flows) {
                AbstractTestCase.log("    ", f.p, f.q, f.label);
            }
            Flow sum = Flow.sum(flows);
            AbstractTestCase.log("    ", sum.p, sum.q, sum.label);
        }
    }

    void log() {
        if (!LOG.isInfoEnabled()) {
            return;
        }
        LOG.info("");
        LOG.info("{}", label);
    }

    private static void log(String label, double d1, double d2) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("%s%12.6f  %12.6f", label, d1, d2));
        }
    }

    private static void log(String indent, double d1, double d2, String label) {
        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("%s%12.6f  %12.6f  %s", indent, d1, d2, label));
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(BranchDataTest.class);
}
