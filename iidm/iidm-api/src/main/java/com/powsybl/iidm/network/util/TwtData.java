/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import java.util.Objects;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class TwtData {

    private static final String UNEXPECTED_SIDE = "Unexpected side";

    private final String id;

    private final double p1;
    private final double q1;
    private final double p2;
    private final double q2;
    private final double p3;
    private final double q3;

    private final double u1;
    private final double theta1;
    private final double u2;
    private final double theta2;
    private final double u3;
    private final double theta3;

    private final double g1;
    private final double b1;
    private final double g2;
    private final double b2;
    private final double g3;
    private final double b3;

    private final double r1;
    private final double x1;
    private final double ratedU1;
    private final double r2;
    private final double x2;
    private final double ratedU2;
    private final double r3;
    private final double x3;
    private final double ratedU3;

    private final boolean connected1;
    private final boolean connected2;
    private final boolean connected3;
    private final boolean mainComponent1;
    private final boolean mainComponent2;
    private final boolean mainComponent3;

    private final double computedP1;
    private final double computedQ1;
    private final double computedP2;
    private final double computedQ2;
    private final double computedP3;
    private final double computedQ3;

    private final double starU;
    private final double starTheta;

    private final int phaseAngleClock2;
    private final int phaseAngleClock3;
    private final double ratedU0;

    public TwtData(ThreeWindingsTransformer twt, double epsilonX, boolean applyReactanceCorrection) {
        this(twt, 0, 0, epsilonX, applyReactanceCorrection);
    }

    public TwtData(ThreeWindingsTransformer twt, int phaseAngleClock2, int phaseAngleClock3, double epsilonX, boolean applyReactanceCorrection) {
        Objects.requireNonNull(twt);
        id = twt.getId();

        p1 = twt.getLeg1().getTerminal().getP();
        q1 = twt.getLeg1().getTerminal().getQ();
        p2 = twt.getLeg2().getTerminal().getP();
        q2 = twt.getLeg2().getTerminal().getQ();
        p3 = twt.getLeg3().getTerminal().getP();
        q3 = twt.getLeg3().getTerminal().getQ();

        u1 = getV(twt.getLeg1());
        theta1 = getTheta(twt.getLeg1());
        u2 = getV(twt.getLeg2());
        theta2 = getTheta(twt.getLeg2());
        u3 = getV(twt.getLeg3());
        theta3 = getTheta(twt.getLeg3());

        g1 = getG(twt.getLeg1());
        b1 = getB(twt.getLeg1());
        g2 = getG(twt.getLeg2());
        b2 = getB(twt.getLeg2());
        g3 = getG(twt.getLeg3());
        b3 = getB(twt.getLeg3());

        r1 = getR(twt.getLeg1());
        x1 = getX(twt.getLeg1());
        ratedU1 = twt.getLeg1().getRatedU();
        r2 = getR(twt.getLeg2());
        x2 = getX(twt.getLeg2());
        ratedU2 = twt.getLeg2().getRatedU();
        r3 = getR(twt.getLeg3());
        x3 = getX(twt.getLeg3());
        ratedU3 = twt.getLeg3().getRatedU();

        connected1 = twt.getLeg1().getTerminal().isConnected();
        connected2 = twt.getLeg2().getTerminal().isConnected();
        connected3 = twt.getLeg3().getTerminal().isConnected();
        mainComponent1 = isMainComponent(twt.getLeg1());
        mainComponent2 = isMainComponent(twt.getLeg2());
        mainComponent3 = isMainComponent(twt.getLeg3());

        // Assume the ratedU at the star bus is equal to ratedU of Leg1
        ratedU0 = twt.getRatedU0();
        this.phaseAngleClock2 = phaseAngleClock2;
        this.phaseAngleClock3 = phaseAngleClock3;

        Complex starVoltage = calcStarVoltage(twt, ratedU0);
        starU = starVoltage.abs();
        starTheta = starVoltage.getArgument();

        BranchData leg1BranchData = legBranchData(twt.getId(), Side.ONE, twt.getLeg1(), 0, ratedU0, starVoltage,
                epsilonX, applyReactanceCorrection);
        computedP1 = leg1BranchData.getComputedP1();
        computedQ1 = leg1BranchData.getComputedQ1();
        BranchData leg2BranchData = legBranchData(twt.getId(), Side.TWO, twt.getLeg2(), phaseAngleClock2, ratedU0, starVoltage,
                epsilonX, applyReactanceCorrection);
        computedP2 = leg2BranchData.getComputedP1();
        computedQ2 = leg2BranchData.getComputedQ1();
        BranchData leg3BranchData = legBranchData(twt.getId(), Side.THREE, twt.getLeg3(), phaseAngleClock3, ratedU0, starVoltage,
                epsilonX, applyReactanceCorrection);
        computedP3 = leg3BranchData.getComputedP1();
        computedQ3 = leg3BranchData.getComputedQ1();
    }

    public static Complex calcStarVoltage(ThreeWindingsTransformer twt, double ratedU0) {
        Objects.requireNonNull(twt);
        Complex v1 = ComplexUtils.polar2Complex(getV(twt.getLeg1()), getTheta(twt.getLeg1()));
        Complex v2 = ComplexUtils.polar2Complex(getV(twt.getLeg2()), getTheta(twt.getLeg2()));
        Complex v3 = ComplexUtils.polar2Complex(getV(twt.getLeg3()), getTheta(twt.getLeg3()));
        Complex ytr1 = new Complex(getR(twt.getLeg1()), getX(twt.getLeg1())).reciprocal();
        Complex ytr2 = new Complex(getR(twt.getLeg2()), getX(twt.getLeg2())).reciprocal();
        Complex ytr3 = new Complex(getR(twt.getLeg3()), getX(twt.getLeg3())).reciprocal();

        Complex a01 = new Complex(1, 0);
        Complex a1 = new Complex(1 / rho(twt.getLeg1(), ratedU0), -alpha(twt.getLeg1()));
        Complex a02 = new Complex(1, 0);
        Complex a2 = new Complex(1 / rho(twt.getLeg2(), ratedU0), -alpha(twt.getLeg2()));
        Complex a03 = new Complex(1, 0);
        Complex a3 = new Complex(1 / rho(twt.getLeg3(), ratedU0), -alpha(twt.getLeg3()));

        // IIDM model includes admittance to ground at star bus side in Leg1
        Complex ysh01 = new Complex(getG(twt.getLeg1()), getB(twt.getLeg1()));
        Complex ysh02 = new Complex(getG(twt.getLeg2()), getB(twt.getLeg2()));
        Complex ysh03 = new Complex(getG(twt.getLeg3()), getB(twt.getLeg3()));
        Complex y01 = ytr1.negate().divide(a01.conjugate().multiply(a1));
        Complex y02 = ytr2.negate().divide(a02.conjugate().multiply(a2));
        Complex y03 = ytr3.negate().divide(a03.conjugate().multiply(a3));
        Complex y0101 = ytr1.add(ysh01).divide(a01.conjugate().multiply(a01));
        Complex y0202 = ytr2.add(ysh02).divide(a02.conjugate().multiply(a02));
        Complex y0303 = ytr3.add(ysh03).divide(a03.conjugate().multiply(a03));

        return y01.multiply(v1).add(y02.multiply(v2)).add(y03.multiply(v3)).negate()
                .divide(y0101.add(y0202).add(y0303));
    }

    private static double getV(Leg leg) {
        return leg.getTerminal().isConnected() ? leg.getTerminal().getBusView().getBus().getV() : Double.NaN;
    }

    private static double getTheta(Leg leg) {
        return leg.getTerminal().isConnected() ? Math.toRadians(leg.getTerminal().getBusView().getBus().getAngle())
                : Double.NaN;
    }

    private static double rho(Leg leg, double ratedU0) {
        double rho = ratedU0 / leg.getRatedU();
        if (leg.getRatioTapChanger() != null) {
            rho *= leg.getRatioTapChanger().getCurrentStep().getRho();
        }
        if (leg.getPhaseTapChanger() != null) {
            rho *= leg.getPhaseTapChanger().getCurrentStep().getRho();
        }
        return rho;
    }

    private static double alpha(Leg leg) {
        return leg.getPhaseTapChanger() != null ? Math.toRadians(leg.getPhaseTapChanger().getCurrentStep().getAlpha()) : 0f;
    }

    private static double getValue(double initialValue, double rtcStepValue, double ptcStepValue) {
        return initialValue * (1 + rtcStepValue / 100) * (1 + ptcStepValue / 100);
    }

    private static double getR(Leg leg) {
        return getValue(leg.getR(),
            leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getR() : 0,
            leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getR() : 0);
    }

    private static double getX(Leg leg) {
        return getValue(leg.getX(),
            leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getX() : 0,
            leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getX() : 0);
    }

    private static double getG(Leg leg) {
        return getValue(leg.getG(),
            leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getG() : 0,
            leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getG() : 0);
    }

    private static double getB(Leg leg) {
        return getValue(leg.getB(),
            leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getB() : 0,
            leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getB() : 0);
    }

    private static boolean isMainComponent(Leg leg) {
        Bus bus = leg.getTerminal().getBusView().getBus();
        Bus connectableBus = leg.getTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        return bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
    }

    private static BranchData legBranchData(String twtId, Side side, Leg leg, int phaseAngleClock, double ratedU0, Complex starVoltage,
            double epsilonX, boolean applyReactanceCorrection) {
        // All (gk, bk) are zero in the IIDM model
        return legBranchData(twtId, side, leg, getG(leg), getB(leg), phaseAngleClock, ratedU0, starVoltage, epsilonX, applyReactanceCorrection);
    }

    private static BranchData legBranchData(String twtId, Side side, Leg leg, double g, double b, int phaseAngleClock, double ratedU0,
            Complex starVoltage,
            double epsilonX, boolean applyReactanceCorrection) {
        String branchId = twtId + "_" + side;
        double r = getR(leg);
        double x = getX(leg);
        double uk = getV(leg);
        double thetak = getTheta(leg);
        double u0 = starVoltage.abs();
        double theta0 = starVoltage.getArgument();
        double gk = 0;
        double bk = 0;
        double g0 = g;
        double b0 = b;
        double rhok = rho(leg, ratedU0);
        double alphak = alpha(leg);
        double rho0 = 1;
        double alpha0 = 0;
        boolean buskMainComponent = true;
        boolean bus0MainComponent = true;
        boolean buskConnected = true;
        boolean bus0Connected = true;
        double flowPk = Double.NaN;
        double flowQk = Double.NaN;
        double flowP0 = Double.NaN;
        double flowQ0 = Double.NaN;
        return new BranchData(branchId, r, x, rhok, rho0, uk, u0, thetak, theta0, alphak, alpha0, gk, g0, bk, b0,
                flowPk, flowQk, flowP0, flowQ0, buskConnected, bus0Connected, buskMainComponent, bus0MainComponent,
                phaseAngleClock, epsilonX, applyReactanceCorrection);
    }

    public String getId() {
        return id;
    }

    public double getComputedP(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return computedP1;
            case TWO:
                return computedP2;
            case THREE:
                return computedP3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getComputedQ(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return computedQ1;
            case TWO:
                return computedQ2;
            case THREE:
                return computedQ3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getP(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return p1;
            case TWO:
                return p2;
            case THREE:
                return p3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getQ(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return q1;
            case TWO:
                return q2;
            case THREE:
                return q3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getU(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return u1;
            case TWO:
                return u2;
            case THREE:
                return u3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getStarU() {
        return starU;
    }

    public double getTheta(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return theta1;
            case TWO:
                return theta2;
            case THREE:
                return theta3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getStarTheta() {
        return starTheta;
    }

    public double getG1() {
        return g1;
    }

    public double getB1() {
        return b1;
    }

    public double getG2() {
        return g2;
    }

    public double getB2() {
        return b2;
    }

    public double getG3() {
        return g3;
    }

    public double getB3() {
        return b3;
    }

    public double getR(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return r1;
            case TWO:
                return r2;
            case THREE:
                return r3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getX(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return x1;
            case TWO:
                return x2;
            case THREE:
                return x3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getRatedU(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return ratedU1;
            case TWO:
                return ratedU2;
            case THREE:
                return ratedU3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public boolean isConnected(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return connected1;
            case TWO:
                return connected2;
            case THREE:
                return connected3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public boolean isMainComponent(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return mainComponent1;
            case TWO:
                return mainComponent2;
            case THREE:
                return mainComponent3;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public int getPhaseAngleClock2() {
        return phaseAngleClock2;
    }

    public int getPhaseAngleClock3() {
        return phaseAngleClock3;
    }

    public double getRatedU0() {
        return ratedU0;
    }

}
