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
import com.powsybl.iidm.network.ThreeWindingsTransformer.LegBase;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Side;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class TwtData {

    private static final String UNEXPECTED_SIDE = "Unexpected side";

    private final String        id;

    private final double        p1;
    private final double        q1;
    private final double        p2;
    private final double        q2;
    private final double        p3;
    private final double        q3;

    private final double        u1;
    private final double        theta1;
    private final double        u2;
    private final double        theta2;
    private final double        u3;
    private final double        theta3;

    private final double        r1;
    private final double        x1;
    private final double        g11;
    private final double        b11;
    private final double        g12;
    private final double        b12;
    private final double        ratedU1;
    private final double        r2;
    private final double        x2;
    private final double        g21;
    private final double        b21;
    private final double        g22;
    private final double        b22;
    private final double        ratedU2;
    private final double        r3;
    private final double        x3;
    private final double        g31;
    private final double        b31;
    private final double        g32;
    private final double        b32;
    private final double        ratedU3;

    private final boolean       connected1;
    private final boolean       connected2;
    private final boolean       connected3;
    private final boolean       mainComponent1;
    private final boolean       mainComponent2;
    private final boolean       mainComponent3;

    private final double        computedP1;
    private final double        computedQ1;
    private final double        computedP2;
    private final double        computedQ2;
    private final double        computedP3;
    private final double        computedQ3;

    private final double        starU;
    private final double        starTheta;

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

        r1 = getR(twt.getLeg1());
        x1 = getX(twt.getLeg1());
        g11 = getG1(twt.getLeg1());
        b11 = getB1(twt.getLeg1());
        g12 = getG2(twt.getLeg1());
        b12 = getB2(twt.getLeg1());
        ratedU1 = twt.getLeg1().getRatedU();
        r2 = getR(twt.getLeg2());
        x2 = getX(twt.getLeg2());
        g21 = getG1(twt.getLeg2());
        b21 = getB1(twt.getLeg2());
        g22 = getG2(twt.getLeg2());
        b22 = getB2(twt.getLeg2());
        ratedU2 = twt.getLeg2().getRatedU();
        r3 = getR(twt.getLeg3());
        x3 = getX(twt.getLeg3());
        g31 = getG1(twt.getLeg3());
        b31 = getB1(twt.getLeg3());
        g32 = getG2(twt.getLeg3());
        b32 = getB2(twt.getLeg3());
        ratedU3 = twt.getLeg3().getRatedU();

        connected1 = twt.getLeg1().getTerminal().isConnected();
        connected2 = twt.getLeg2().getTerminal().isConnected();
        connected3 = twt.getLeg3().getTerminal().isConnected();
        mainComponent1 = isMainComponent(twt.getLeg1());
        mainComponent2 = isMainComponent(twt.getLeg2());
        mainComponent3 = isMainComponent(twt.getLeg3());

        // Assume the ratedU at the star bus is equal to ratedU of LegBase
        double ratedU0 = twt.getLeg1().getRatedU();

        Complex starVoltage = calcStarVoltage(twt, ratedU0);
        starU = starVoltage.abs();
        starTheta = starVoltage.getArgument();

        BranchData leg1BranchData = legBranchData(twt.getId(), Side.ONE, twt.getLeg1(), ratedU0, starVoltage,
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
        Complex ytr1 = new Complex(getR(twt.getLeg1()), getX(twt.getLeg1())).reciprocal();
        Complex ytr2 = new Complex(getR(twt.getLeg2()), getX(twt.getLeg2())).reciprocal();
        Complex ytr3 = new Complex(getR(twt.getLeg3()), getX(twt.getLeg3())).reciprocal();

        Complex a01 = new Complex(1, 0);
        // JAMTODO Complex a1 = new Complex(twt.getLeg1().getRatedU() / ratedU0, 0);
        Complex a1 = ComplexUtils.polar2Complex(1 / rho(twt.getLeg1(), ratedU0), -alpha(twt.getLeg1()));
        Complex a02 = new Complex(1, 0);
        Complex a2 = ComplexUtils.polar2Complex(1 / rho(twt.getLeg2(), ratedU0), -alpha(twt.getLeg2()));
        Complex a03 = new Complex(1, 0);
        Complex a3 = ComplexUtils.polar2Complex(1 / rho(twt.getLeg3(), ratedU0), -alpha(twt.getLeg3()));

        // IIDM model includes admittance to ground at star bus side in LegBase
        Complex ysh01 = new Complex(getG2(twt.getLeg1()), getB2(twt.getLeg1()));
        Complex ysh02 = new Complex(getG2(twt.getLeg2()), getB2(twt.getLeg2()));
        Complex ysh03 = new Complex(getG2(twt.getLeg3()), getB2(twt.getLeg3()));
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
        if (leg.getTerminal().getBusBreakerView() != null) {
            return leg.getTerminal().isConnected() ? leg.getTerminal().getBusBreakerView().getBus().getV() : Double.NaN;
        } else {
            return leg.getTerminal().isConnected() ? leg.getTerminal().getBusView().getBus().getV() : Double.NaN;
        }
    }

    private static double getTheta(LegBase<?> leg) {
        if (leg.getTerminal().getBusBreakerView() != null) {
            return leg.getTerminal().isConnected() ? Math.toRadians(leg.getTerminal().getBusBreakerView().getBus().getAngle())
                    : Double.NaN;
        } else {
            return leg.getTerminal().isConnected() ? Math.toRadians(leg.getTerminal().getBusView().getBus().getAngle())
                    : Double.NaN;
        }
    }

    private static double rho(LegBase<?> leg, double ratedU0) {
        double rho = ratedU0 / leg.getRatedU();
        if (leg.getRatioTapChanger() != null) {
            rho *= leg.getRatioTapChanger().getCurrentStep().getRho();
        }
        if (leg.getPhaseTapChanger() != null) {
            rho *= leg.getPhaseTapChanger().getCurrentStep().getRho();
        }
        return rho;
    }

    private static double alpha(LegBase<?> leg) {
        return leg.getPhaseTapChanger() != null ? Math.toRadians(leg.getPhaseTapChanger().getCurrentStep().getAlpha()) : 0f;
    }

    private static double getValue(double initialValue, double rtcStepValue, double ptcStepValue) {
        return initialValue * (1 + rtcStepValue / 100) * (1 + ptcStepValue / 100);
    }

    private static double getR(LegBase<?> leg) {
        return getValue(leg.getR(),
                leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getR() : 0,
                leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getR() : 0);
    }

    private static double getX(LegBase<?> leg) {
        return getValue(leg.getX(),
                leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getX() : 0,
                leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getX() : 0);
    }

    private static double getG1(LegBase<?> leg) {
        return getValue(leg.getG1(),
                leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getG1() : 0,
                leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getG1() : 0);
    }

    private static double getB1(LegBase<?> leg) {
        return getValue(leg.getB1(),
                leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getB1() : 0,
                leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getB1() : 0);
    }

    private static double getG2(LegBase<?> leg) {
        return getValue(leg.getG2(),
                leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getG2() : 0,
                leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getG2() : 0);
    }

    private static double getB2(LegBase<?> leg) {
        return getValue(leg.getB2(),
                leg.getRatioTapChanger() != null ? leg.getRatioTapChanger().getCurrentStep().getB2() : 0,
                leg.getPhaseTapChanger() != null ? leg.getPhaseTapChanger().getCurrentStep().getB2() : 0);
    }

    private static boolean isMainComponent(LegBase<?> leg) {
        Bus bus = leg.getTerminal().getBusView().getBus();
        Bus connectableBus = leg.getTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        return bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
    }

    private static BranchData legBranchData(String twtId, Side side, LegBase<?> leg, double ratedU0,
            Complex starVoltage, double epsilonX, boolean applyReactanceCorrection) {
        String branchId = twtId + "_" + side;
        double r = getR(leg);
        double x = getX(leg);
        double uk = getV(leg);
        double thetak = getTheta(leg);
        double u0 = starVoltage.abs();
        double theta0 = starVoltage.getArgument();
        double g1 = getG1(leg);
        double b1 = getB1(leg);
        double g2 = getG2(leg);
        double b2 = getB2(leg);
        double rhok = rho(leg, ratedU0);
        double alphak = alpha(leg);
        double rho0 = 1;
        double alpha0 = 0f;
        boolean buskMainComponent = true;
        boolean bus0MainComponent = true;
        boolean buskConnected = true;
        boolean bus0Connected = true;
        double flowPk = Double.NaN;
        double flowQk = Double.NaN;
        double flowP0 = Double.NaN;
        double flowQ0 = Double.NaN;
        return new BranchData(branchId, r, x, rhok, rho0, uk, u0, thetak, theta0, alphak, alpha0, g1, g2, b1, b2,
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

    public double getG1(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return g11;
            case TWO:
                return g21;
            case THREE:
                return g31;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getB1(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return b11;
            case TWO:
                return b21;
            case THREE:
                return b31;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getG2(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return g12;
            case TWO:
                return g22;
            case THREE:
                return g32;
            default:
                throw new AssertionError(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getB2(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return b12;
            case TWO:
                return b22;
            case THREE:
                return b32;
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
