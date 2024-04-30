/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import java.util.Objects;

import com.powsybl.iidm.network.TwoSides;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.ThreeSides;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
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

    private final double r1;
    private final double x1;
    private final double r2;
    private final double x2;
    private final double r3;
    private final double x3;

    private final double g11;
    private final double b11;
    private final double g12;
    private final double b12;
    private final double g21;
    private final double b21;
    private final double g22;
    private final double b22;
    private final double g31;
    private final double b31;
    private final double g32;
    private final double b32;

    private final double rho1;
    private final double alpha1;
    private final double rho2;
    private final double alpha2;
    private final double rho3;
    private final double alpha3;

    private final double ratedU1;
    private final double ratedU2;
    private final double ratedU3;

    private final boolean connected1;
    private final boolean connected2;
    private final boolean connected3;
    private final boolean mainComponent1;
    private final boolean mainComponent2;
    private final boolean mainComponent3;

    private double computedP1;
    private double computedQ1;
    private double computedP2;
    private double computedQ2;
    private double computedP3;
    private double computedQ3;

    private double starU;
    private double starTheta;

    private final int phaseAngleClock2;
    private final int phaseAngleClock3;
    private final double ratedU0;

    public TwtData(ThreeWindingsTransformer twt, double epsilonX, boolean applyReactanceCorrection) {
        this(twt, 0, 0, epsilonX, applyReactanceCorrection, false);
    }

    public TwtData(ThreeWindingsTransformer twt, double epsilonX, boolean applyReactanceCorrection,
        boolean twtSplitShuntAdmittance) {
        this(twt, 0, 0, epsilonX, applyReactanceCorrection, twtSplitShuntAdmittance);
    }

    public TwtData(ThreeWindingsTransformer twt, int phaseAngleClock2, int phaseAngleClock3, double epsilonX,
        boolean applyReactanceCorrection, boolean twtSplitShuntAdmittance) {
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
        x1 = LinkData.getFixedX(getX(twt.getLeg1()), epsilonX, applyReactanceCorrection);
        r2 = getR(twt.getLeg2());
        x2 = LinkData.getFixedX(getX(twt.getLeg2()), epsilonX, applyReactanceCorrection);
        r3 = getR(twt.getLeg3());
        x3 = LinkData.getFixedX(getX(twt.getLeg3()), epsilonX, applyReactanceCorrection);

        g11 = getG1(twt.getLeg1(), twtSplitShuntAdmittance);
        b11 = getB1(twt.getLeg1(), twtSplitShuntAdmittance);
        g12 = getG2(twt.getLeg1(), twtSplitShuntAdmittance);
        b12 = getB2(twt.getLeg1(), twtSplitShuntAdmittance);
        g21 = getG1(twt.getLeg2(), twtSplitShuntAdmittance);
        b21 = getB1(twt.getLeg2(), twtSplitShuntAdmittance);
        g22 = getG2(twt.getLeg2(), twtSplitShuntAdmittance);
        b22 = getB2(twt.getLeg2(), twtSplitShuntAdmittance);
        g31 = getG1(twt.getLeg3(), twtSplitShuntAdmittance);
        b31 = getB1(twt.getLeg3(), twtSplitShuntAdmittance);
        g32 = getG2(twt.getLeg3(), twtSplitShuntAdmittance);
        b32 = getB2(twt.getLeg3(), twtSplitShuntAdmittance);

        this.ratedU0 = twt.getRatedU0();
        this.phaseAngleClock2 = phaseAngleClock2;
        this.phaseAngleClock3 = phaseAngleClock3;

        rho1 = rho(twt.getLeg1(), ratedU0);
        alpha1 = alpha(twt.getLeg1());
        rho2 = rho(twt.getLeg2(), ratedU0);
        alpha2 = alpha(twt.getLeg2());
        rho3 = rho(twt.getLeg3(), ratedU0);
        alpha3 = alpha(twt.getLeg3());

        ratedU1 = twt.getLeg1().getRatedU();
        ratedU2 = twt.getLeg2().getRatedU();
        ratedU3 = twt.getLeg3().getRatedU();

        double rhof = 1.0;
        double alphaf = 0.0;

        double angle1 = -alpha1;
        double angle2 = -alpha2 - Math.toRadians(LinkData.getPhaseAngleClockDegrees(phaseAngleClock2));
        double angle3 = -alpha3 - Math.toRadians(LinkData.getPhaseAngleClockDegrees(phaseAngleClock3));
        double anglef = -alphaf;

        LinkData.BranchAdmittanceMatrix branchAdmittanceLeg1 = LinkData.calculateBranchAdmittance(r1, x1,
            1 / rho1, angle1, 1 / rhof, anglef, new Complex(g11, b11), new Complex(g12, b12));

        LinkData.BranchAdmittanceMatrix branchAdmittanceLeg2 = LinkData.calculateBranchAdmittance(r2, x2,
            1 / rho2, angle2, 1 / rhof, anglef, new Complex(g21, b21), new Complex(g22, b22));

        LinkData.BranchAdmittanceMatrix branchAdmittanceLeg3 = LinkData.calculateBranchAdmittance(r3, x3,
            1 / rho3, angle3, 1 / rhof, anglef, new Complex(g31, b31), new Complex(g32, b32));

        connected1 = twt.getLeg1().getTerminal().isConnected();
        connected2 = twt.getLeg2().getTerminal().isConnected();
        connected3 = twt.getLeg3().getTerminal().isConnected();
        mainComponent1 = isMainComponent(twt.getLeg1());
        mainComponent2 = isMainComponent(twt.getLeg2());
        mainComponent3 = isMainComponent(twt.getLeg3());

        // Assume the ratedU at the star bus is equal to ratedU of Leg1

        if (connected1 && connected2 && connected3 && valid(u1, theta1) && valid(u2, theta2) && valid(u3, theta3)) {

            calculateThreeConnectedLegsFlowAndStarBusVoltage(u1, theta1, u2, theta2, u3, theta3, branchAdmittanceLeg1,
                branchAdmittanceLeg2, branchAdmittanceLeg3);
        } else if (connected1 && connected2 && valid(u1, theta1) && valid(u2, theta2)) {

            LinkData.Flow flow = calculateTwoConnectedLegsFlow(u1, theta1, u2, theta2,
                branchAdmittanceLeg1, branchAdmittanceLeg2, branchAdmittanceLeg3);
            computedP1 = flow.fromTo.getReal();
            computedQ1 = flow.fromTo.getImaginary();
            computedP2 = flow.toFrom.getReal();
            computedQ2 = flow.toFrom.getImaginary();
            computedP3 = 0.0;
            computedQ3 = 0.0;

            Complex v0 = calculateTwoConnectedLegsStarBusVoltage(u1, theta1, u2, theta2,
                branchAdmittanceLeg1, branchAdmittanceLeg2, branchAdmittanceLeg3);
            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (connected1 && connected3 && valid(u1, theta1) && valid(u3, theta3)) {

            LinkData.Flow flow = calculateTwoConnectedLegsFlow(u1, theta1, u3, theta3,
                branchAdmittanceLeg1, branchAdmittanceLeg3, branchAdmittanceLeg2);
            computedP1 = flow.fromTo.getReal();
            computedQ1 = flow.fromTo.getImaginary();
            computedP2 = 0.0;
            computedQ2 = 0.0;
            computedP3 = flow.toFrom.getReal();
            computedQ3 = flow.toFrom.getImaginary();

            Complex v0 = calculateTwoConnectedLegsStarBusVoltage(u1, theta1, u3, theta3,
                branchAdmittanceLeg1, branchAdmittanceLeg3, branchAdmittanceLeg2);

            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (connected2 && connected3 && valid(u2, theta2) && valid(u3, theta3)) {

            LinkData.Flow flow = calculateTwoConnectedLegsFlow(u2, theta2, u3, theta3,
                branchAdmittanceLeg2, branchAdmittanceLeg3, branchAdmittanceLeg1);
            computedP1 = 0.0;
            computedQ1 = 0.0;
            computedP2 = flow.fromTo.getReal();
            computedQ2 = flow.fromTo.getImaginary();
            computedP3 = flow.toFrom.getReal();
            computedQ3 = flow.toFrom.getImaginary();

            Complex v0 = calculateTwoConnectedLegsStarBusVoltage(u2, theta2, u3, theta3,
                branchAdmittanceLeg2, branchAdmittanceLeg3, branchAdmittanceLeg1);
            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (connected1 && valid(u1, theta1)) {

            Complex flow = calculateOneConnectedLegFlow(u1, theta1, branchAdmittanceLeg1,
                branchAdmittanceLeg2, branchAdmittanceLeg3);
            computedP1 = flow.getReal();
            computedQ1 = flow.getImaginary();
            computedP2 = 0.0;
            computedQ2 = 0.0;
            computedP3 = 0.0;
            computedQ3 = 0.0;

            Complex v0 = calculateOneConnectedLegStarBusVoltage(u1, theta1, branchAdmittanceLeg1,
                branchAdmittanceLeg2, branchAdmittanceLeg3);

            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (connected2 && valid(u2, theta2)) {

            Complex flow = calculateOneConnectedLegFlow(u2, theta2, branchAdmittanceLeg2,
                branchAdmittanceLeg1, branchAdmittanceLeg3);

            computedP1 = 0.0;
            computedQ1 = 0.0;
            computedP2 = flow.getReal();
            computedQ2 = flow.getImaginary();
            computedP3 = 0.0;
            computedQ3 = 0.0;

            Complex v0 = calculateOneConnectedLegStarBusVoltage(u2, theta2, branchAdmittanceLeg2,
                branchAdmittanceLeg1, branchAdmittanceLeg3);
            starU = v0.abs();
            starTheta = v0.getArgument();
        } else if (connected3 && valid(u3, theta3)) {

            Complex flow = calculateOneConnectedLegFlow(u3, theta3, branchAdmittanceLeg3,
                branchAdmittanceLeg1, branchAdmittanceLeg2);

            computedP1 = 0.0;
            computedQ1 = 0.0;
            computedP2 = 0.0;
            computedQ2 = 0.0;
            computedP3 = flow.getReal();
            computedQ3 = flow.getImaginary();

            Complex v0 = calculateOneConnectedLegStarBusVoltage(u3, theta3, branchAdmittanceLeg3,
                branchAdmittanceLeg1, branchAdmittanceLeg2);
            starU = v0.abs();
            starTheta = v0.getArgument();
        } else {

            computedP1 = Double.NaN;
            computedQ1 = Double.NaN;
            computedP2 = Double.NaN;
            computedQ2 = Double.NaN;
            computedP3 = Double.NaN;
            computedQ3 = Double.NaN;

            starU = Double.NaN;
            starTheta = Double.NaN;
        }
    }

    private void calculateThreeConnectedLegsFlowAndStarBusVoltage(double u1, double theta1, double u2, double theta2,
        double u3, double theta3, LinkData.BranchAdmittanceMatrix branchAdmittanceLeg1,
        LinkData.BranchAdmittanceMatrix branchAdmittanceLeg2, LinkData.BranchAdmittanceMatrix branchAdmittanceLeg3) {

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
        Complex v2 = ComplexUtils.polar2Complex(u2, theta2);
        Complex v3 = ComplexUtils.polar2Complex(u3, theta3);

        Complex v0 = branchAdmittanceLeg1.y21().multiply(v1).add(branchAdmittanceLeg2.y21().multiply(v2))
            .add(branchAdmittanceLeg3.y21().multiply(v3)).negate()
            .divide(branchAdmittanceLeg1.y22().add(branchAdmittanceLeg2.y22()).add(branchAdmittanceLeg3.y22()));

        LinkData.Flow flowLeg1 = LinkData.flowBothEnds(branchAdmittanceLeg1.y11(), branchAdmittanceLeg1.y12(),
            branchAdmittanceLeg1.y21(), branchAdmittanceLeg1.y22(), v1, v0);

        LinkData.Flow flowLeg2 = LinkData.flowBothEnds(branchAdmittanceLeg2.y11(), branchAdmittanceLeg2.y12(),
            branchAdmittanceLeg2.y21(), branchAdmittanceLeg2.y22(), v2, v0);

        LinkData.Flow flowLeg3 = LinkData.flowBothEnds(branchAdmittanceLeg3.y11(), branchAdmittanceLeg3.y12(),
            branchAdmittanceLeg3.y21(), branchAdmittanceLeg3.y22(), v3, v0);

        computedP1 = flowLeg1.fromTo.getReal();
        computedQ1 = flowLeg1.fromTo.getImaginary();
        computedP2 = flowLeg2.fromTo.getReal();
        computedQ2 = flowLeg2.fromTo.getImaginary();
        computedP3 = flowLeg3.fromTo.getReal();
        computedQ3 = flowLeg3.fromTo.getImaginary();

        starU = v0.abs();
        starTheta = v0.getArgument();
    }

    private LinkData.Flow calculateTwoConnectedLegsFlow(double u1, double theta1, double u2, double theta2,
        LinkData.BranchAdmittanceMatrix admittanceMatrixLeg1, LinkData.BranchAdmittanceMatrix admittanceMatrixLeg2,
        LinkData.BranchAdmittanceMatrix admittanceMatrixOpenLeg) {

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
        Complex v2 = ComplexUtils.polar2Complex(u2, theta2);

        LinkData.BranchAdmittanceMatrix admittance = calculateTwoConnectedLegsAdmittance(admittanceMatrixLeg1,
            admittanceMatrixLeg2, admittanceMatrixOpenLeg);

        return LinkData.flowBothEnds(admittance.y11(), admittance.y12(), admittance.y21(), admittance.y22(), v1, v2);
    }

    private Complex calculateTwoConnectedLegsStarBusVoltage(double u1, double theta1, double u2, double theta2,
        LinkData.BranchAdmittanceMatrix admittanceMatrixLeg1, LinkData.BranchAdmittanceMatrix admittanceMatrixLeg2,
        LinkData.BranchAdmittanceMatrix admittanceMatrixOpenLeg) {

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
        Complex v2 = ComplexUtils.polar2Complex(u2, theta2);

        Complex yshO = LinkData.kronAntenna(admittanceMatrixOpenLeg.y11(), admittanceMatrixOpenLeg.y12(), admittanceMatrixOpenLeg.y21(), admittanceMatrixOpenLeg.y22(), true);
        return (admittanceMatrixLeg1.y21().multiply(v1).add(admittanceMatrixLeg2.y21().multiply(v2))).negate()
                .divide(admittanceMatrixLeg1.y22().add(admittanceMatrixLeg2.y22()).add(yshO));
    }

    private Complex calculateOneConnectedLegFlow(double u, double theta, LinkData.BranchAdmittanceMatrix admittanceMatrixLeg,
        LinkData.BranchAdmittanceMatrix admittanceMatrixFirstOpenLeg,
        LinkData.BranchAdmittanceMatrix admittanceMatrixSecondOpenLeg) {

        Complex ysh = calculateOneConnectedLegShunt(admittanceMatrixLeg,
            admittanceMatrixFirstOpenLeg, admittanceMatrixSecondOpenLeg);

        return LinkData.flowYshunt(ysh, u, theta);
    }

    private Complex calculateOneConnectedLegStarBusVoltage(double u, double theta,
        LinkData.BranchAdmittanceMatrix admittanceMatrixLeg, LinkData.BranchAdmittanceMatrix admittanceMatrixFirstOpenLeg,
        LinkData.BranchAdmittanceMatrix admittanceMatrixSecondOpenLeg) {

        Complex v = ComplexUtils.polar2Complex(u, theta);

        Complex ysh1O = LinkData.kronAntenna(admittanceMatrixFirstOpenLeg.y11(), admittanceMatrixFirstOpenLeg.y12(),
            admittanceMatrixFirstOpenLeg.y21(), admittanceMatrixFirstOpenLeg.y22(), true);
        Complex ysh2O = LinkData.kronAntenna(admittanceMatrixSecondOpenLeg.y11(), admittanceMatrixSecondOpenLeg.y12(),
            admittanceMatrixSecondOpenLeg.y21(), admittanceMatrixSecondOpenLeg.y22(), true);

        return admittanceMatrixLeg.y21().multiply(v).negate().divide(admittanceMatrixLeg.y22().add(ysh1O).add(ysh2O));
    }

    private LinkData.BranchAdmittanceMatrix calculateTwoConnectedLegsAdmittance(
        LinkData.BranchAdmittanceMatrix firstCloseLeg,
        LinkData.BranchAdmittanceMatrix secondCloseLeg, LinkData.BranchAdmittanceMatrix openLeg) {

        Complex ysh = LinkData.kronAntenna(openLeg.y11(), openLeg.y12(), openLeg.y21(), openLeg.y22(), true);
        LinkData.BranchAdmittanceMatrix secondCloseLegMod = new LinkData.BranchAdmittanceMatrix(secondCloseLeg.y11(),
            secondCloseLeg.y12(), secondCloseLeg.y21(), secondCloseLeg.y22().add(ysh));
        return LinkData.kronChain(firstCloseLeg, TwoSides.TWO, secondCloseLegMod, TwoSides.TWO);
    }

    private Complex calculateOneConnectedLegShunt(LinkData.BranchAdmittanceMatrix closeLeg,
        LinkData.BranchAdmittanceMatrix firstOpenLeg, LinkData.BranchAdmittanceMatrix secondOpenLeg) {
        Complex ysh1 = LinkData.kronAntenna(firstOpenLeg.y11(), firstOpenLeg.y12(), firstOpenLeg.y21(), firstOpenLeg.y22(),
            true);
        Complex ysh2 = LinkData.kronAntenna(secondOpenLeg.y11(), secondOpenLeg.y12(), secondOpenLeg.y21(),
            secondOpenLeg.y22(), true);
        Complex y22 = closeLeg.y22().add(ysh1).add(ysh2);

        return LinkData.kronAntenna(closeLeg.y11(), closeLeg.y12(), closeLeg.y21(), y22, false);
    }

    private static double getV(Leg leg) {
        return leg.getTerminal().isConnected() ? leg.getTerminal().getBusView().getBus().getV() : Double.NaN;
    }

    private static double getTheta(Leg leg) {
        return leg.getTerminal().isConnected() ? Math.toRadians(leg.getTerminal().getBusView().getBus().getAngle()) : Double.NaN;
    }

    private static double rho(Leg leg, double ratedU0) {
        double rho = ratedU0 / leg.getRatedU();
        rho *= leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getRho()).orElse(1d);
        rho *= leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getRho()).orElse(1d);
        return rho;
    }

    private static double alpha(Leg leg) {
        return leg.getOptionalPhaseTapChanger().map(ptc -> Math.toRadians(ptc.getCurrentStep().getAlpha())).orElse(0d);
    }

    private static double getValue(double initialValue, double rtcStepValue, double ptcStepValue) {
        return initialValue * (1 + rtcStepValue / 100) * (1 + ptcStepValue / 100);
    }

    private static double getR(Leg leg) {
        return getValue(leg.getR(),
            leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getR()).orElse(0d),
            leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getR()).orElse(0d));
    }

    private static double getX(Leg leg) {
        return getValue(leg.getX(),
            leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getX()).orElse(0d),
            leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getX()).orElse(0d));
    }

    private static double getG1(Leg leg, boolean twtSplitShuntAdmittance) {
        return getValue(twtSplitShuntAdmittance ? leg.getG() / 2 : leg.getG(),
            leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getG()).orElse(0d),
            leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getG()).orElse(0d));
    }

    private static double getB1(Leg leg, boolean twtSplitShuntAdmittance) {
        return getValue(twtSplitShuntAdmittance ? leg.getB() / 2 : leg.getB(),
            leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getB()).orElse(0d),
            leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getB()).orElse(0d));
    }

    private static double getG2(Leg leg, boolean twtSplitShuntAdmittance) {
        return getValue(twtSplitShuntAdmittance ? leg.getG() / 2 : 0.0,
            leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getG()).orElse(0d),
            leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getG()).orElse(0d));
    }

    private static double getB2(Leg leg, boolean twtSplitShuntAdmittance) {
        return getValue(twtSplitShuntAdmittance ? leg.getB() / 2 : 0.0,
            leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getB()).orElse(0d),
            leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getB()).orElse(0d));
    }

    private static boolean isMainComponent(Leg leg) {
        Bus bus = leg.getTerminal().getBusView().getBus();
        Bus connectableBus = leg.getTerminal().getBusView().getConnectableBus();
        boolean connectableMainComponent = connectableBus != null && connectableBus.isInMainConnectedComponent();
        return bus != null ? bus.isInMainConnectedComponent() : connectableMainComponent;
    }

    private static boolean valid(double voltage, double theta) {
        if (Double.isNaN(voltage) || voltage <= 0.0) {
            return false;
        }
        return !Double.isNaN(theta);
    }

    public String getId() {
        return id;
    }

    public double getComputedP(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return computedP1;
            case TWO:
                return computedP2;
            case THREE:
                return computedP3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getComputedQ(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return computedQ1;
            case TWO:
                return computedQ2;
            case THREE:
                return computedQ3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getP(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return p1;
            case TWO:
                return p2;
            case THREE:
                return p3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getQ(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return q1;
            case TWO:
                return q2;
            case THREE:
                return q3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getU(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return u1;
            case TWO:
                return u2;
            case THREE:
                return u3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getStarU() {
        return starU;
    }

    public double getTheta(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return theta1;
            case TWO:
                return theta2;
            case THREE:
                return theta3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getStarTheta() {
        return starTheta;
    }

    public double getR(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return r1;
            case TWO:
                return r2;
            case THREE:
                return r3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getX(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return x1;
            case TWO:
                return x2;
            case THREE:
                return x3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getG1(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return g11;
            case TWO:
                return g21;
            case THREE:
                return g31;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getB1(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return b11;
            case TWO:
                return b21;
            case THREE:
                return b31;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getG2(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return g12;
            case TWO:
                return g22;
            case THREE:
                return g32;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getB2(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return b12;
            case TWO:
                return b22;
            case THREE:
                return b32;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public double getRatedU(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return ratedU1;
            case TWO:
                return ratedU2;
            case THREE:
                return ratedU3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public boolean isConnected(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return connected1;
            case TWO:
                return connected2;
            case THREE:
                return connected3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
        }
    }

    public boolean isMainComponent(ThreeSides side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return mainComponent1;
            case TWO:
                return mainComponent2;
            case THREE:
                return mainComponent3;
            default:
                throw new IllegalStateException(UNEXPECTED_SIDE + ": " + side);
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
