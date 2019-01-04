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
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg1;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg2or3;
import com.powsybl.iidm.network.ThreeWindingsTransformer.LegBase;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
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

    private final double g;
    private final double b;

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

    public TwtData(ThreeWindingsTransformer twt, double epsilonX, boolean applyReactanceCorrection) {
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

        g = twt.getLeg1().getG();
        b = twt.getLeg1().getB();

        r1 = twt.getLeg1().getR();
        x1 = twt.getLeg1().getX();
        ratedU1 = twt.getLeg1().getRatedU();
        r2 = adjustedR(twt.getLeg2());
        x2 = adjustedX(twt.getLeg2());
        ratedU2 = twt.getLeg2().getRatedU();
        r3 = adjustedR(twt.getLeg3());
        x3 = adjustedX(twt.getLeg3());
        ratedU3 = twt.getLeg3().getRatedU();

        connected1 = twt.getLeg1().getTerminal().isConnected();
        connected2 = twt.getLeg2().getTerminal().isConnected();
        connected3 = twt.getLeg3().getTerminal().isConnected();
        mainComponent1 = isMainComponent(twt.getLeg1());
        mainComponent2 = isMainComponent(twt.getLeg2());
        mainComponent3 = isMainComponent(twt.getLeg3());

        // Assume the ratedU at the star bus is equal to ratedU of Leg1
        double ratedU0 = twt.getLeg1().getRatedU();

        Complex starVoltage = calcStarVoltage(twt, ratedU0);
        starU = starVoltage.abs();
        starTheta = starVoltage.getArgument();

        BranchData leg1BranchData = legBranchData(twt.getId(), twt.getLeg1(), starVoltage,
                epsilonX, applyReactanceCorrection);
        computedP1 = leg1BranchData.getComputedP1();
        computedQ1 = leg1BranchData.getComputedQ1();
        BranchData leg2BranchData = legBranchData(twt.getId(), Side.TWO, twt.getLeg2(), ratedU0, starVoltage,
                epsilonX, applyReactanceCorrection);
        computedP2 = leg2BranchData.getComputedP1();
        computedQ2 = leg2BranchData.getComputedQ1();
        BranchData leg3BranchData = legBranchData(twt.getId(), Side.THREE, twt.getLeg3(), ratedU0, starVoltage,
                epsilonX, applyReactanceCorrection);
        computedP3 = leg3BranchData.getComputedP1();
        computedQ3 = leg3BranchData.getComputedQ1();
    }

    public static Complex calcStarVoltage(ThreeWindingsTransformer twt, double ratedU0) {
        Objects.requireNonNull(twt);
        Complex v1 = ComplexUtils.polar2Complex(getV(twt.getLeg1()), getTheta(twt.getLeg1()));
        Complex v2 = ComplexUtils.polar2Complex(getV(twt.getLeg2()), getTheta(twt.getLeg2()));
        Complex v3 = ComplexUtils.polar2Complex(getV(twt.getLeg3()), getTheta(twt.getLeg3()));
        Complex ytr1 = new Complex(twt.getLeg1().getR(), twt.getLeg1().getX()).reciprocal();
        Complex ytr2 = new Complex(adjustedR(twt.getLeg2()), adjustedX(twt.getLeg2())).reciprocal();
        Complex ytr3 = new Complex(adjustedR(twt.getLeg3()), adjustedX(twt.getLeg3())).reciprocal();

        Complex a01 = new Complex(1, 0);
        Complex a1 = new Complex(twt.getLeg1().getRatedU() / ratedU0, 0);
        Complex a02 = new Complex(1, 0);
        Complex a2 = new Complex(1 / rho(twt.getLeg2(), ratedU0), 0);
        Complex a03 = new Complex(1, 0);
        Complex a3 = new Complex(1 / rho(twt.getLeg3(), ratedU0), 0);

        // IIDM model includes admittance to ground at star bus side in Leg1
        Complex ysh01 = new Complex(twt.getLeg1().getG(), twt.getLeg1().getB());
        Complex ysh02 = new Complex(0, 0);
        Complex ysh03 = new Complex(0, 0);
        Complex y01 = ytr1.negate().divide(a01.conjugate().multiply(a1));
        Complex y02 = ytr2.negate().divide(a02.conjugate().multiply(a2));
        Complex y03 = ytr3.negate().divide(a03.conjugate().multiply(a3));
        Complex y0101 = ytr1.add(ysh01).divide(a01.conjugate().multiply(a01));
        Complex y0202 = ytr2.add(ysh02).divide(a02.conjugate().multiply(a02));
        Complex y0303 = ytr3.add(ysh03).divide(a03.conjugate().multiply(a03));

        return y01.multiply(v1).add(y02.multiply(v2)).add(y03.multiply(v3)).negate()
                .divide(y0101.add(y0202).add(y0303));
    }

    private static double getV(LegBase<?> leg) {
        return leg.getTerminal().isConnected() ? leg.getTerminal().getBusView().getBus().getV() : Double.NaN;
    }

    private static double getTheta(LegBase<?> leg) {
        return leg.getTerminal().isConnected() ? Math.toRadians(leg.getTerminal().getBusView().getBus().getAngle())
                : Double.NaN;
    }

    private static double rho(Leg2or3 leg, double ratedU0) {
        double rho = ratedU0 / leg.getRatedU();
        if (leg.getRatioTapChanger() != null) {
            RatioTapChangerStep step = leg.getRatioTapChanger().getCurrentStep();
            if (step != null) {
                rho *= step.getRho();
            }
        }
        return rho;
    }

    private static double adjustedR(Leg2or3 leg) {
        double r = leg.getR();
        if (leg.getRatioTapChanger() != null) {
            RatioTapChangerStep step = leg.getRatioTapChanger().getCurrentStep();
            if (step != null) {
                r *= 1 + step.getR() / 100;
            }
        }
        return r;
    }

    private static double adjustedX(Leg2or3 leg) {
        double x = leg.getX();
        if (leg.getRatioTapChanger() != null) {
            RatioTapChangerStep step = leg.getRatioTapChanger().getCurrentStep();
            if (step != null) {
                x *= 1 + step.getX() / 100;
            }
        }
        return x;
    }

    private static boolean isMainComponent(LegBase<?> leg) {
        Bus bus = leg.getTerminal().getBusView().getBus();
        Bus connectableBus = leg.getTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        return bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
    }

    private static BranchData legBranchData(String twtId, Leg1 leg, Complex starVoltage, double epsilonX,
            boolean applyReactanceCorrection) {
        // In IIDM only the Leg1 has admittance to ground
        // And it is modeled at end corresponding to star bus
        return legBranchData(twtId, Side.ONE, leg, leg.getG(), leg.getB(), leg.getRatedU(), starVoltage, epsilonX,
                applyReactanceCorrection);
    }

    private static BranchData legBranchData(String twtId, Side side, Leg2or3 leg, double ratedU0, Complex starVoltage,
            double epsilonX, boolean applyReactanceCorrection) {
        // All (gk, bk) are zero in the IIDM model
        return legBranchData(twtId, side, leg, 0, 0, ratedU0, starVoltage, epsilonX, applyReactanceCorrection);
    }

    private static BranchData legBranchData(String twtId, Side side, LegBase<?> leg, double g, double b, double ratedU0,
            Complex starVoltage,
            double epsilonX, boolean applyReactanceCorrection) {
        String branchId = twtId + "_" + side;
        double r = side == Side.ONE ? leg.getR() : adjustedR((Leg2or3) leg);
        double x = side == Side.ONE ? leg.getX() : adjustedX((Leg2or3) leg);
        double uk = getV(leg);
        double thetak = getTheta(leg);
        double u0 = starVoltage.abs();
        double theta0 = starVoltage.getArgument();
        double gk = 0;
        double bk = 0;
        double g0 = g;
        double b0 = b;
        double rhok = side == Side.ONE ? 1.0 : rho((Leg2or3) leg, ratedU0);
        double alphak = 0;
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
                epsilonX, applyReactanceCorrection);
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

    public double getG() {
        return g;
    }

    public double getB() {
        return b;
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
}
