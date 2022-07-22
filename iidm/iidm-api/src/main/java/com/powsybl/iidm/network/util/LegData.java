/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import java.util.Objects;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.util.LinkData.BranchAdmittanceMatrix;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class LegData {

    private final double r;
    private final double x;

    private final double g1;
    private final double b1;
    private final double g2;
    private final double b2;

    private final double rho;
    private final double alpha;

    private final double u;
    private final double theta;
    private final double ratedU;
    private final boolean connected;
    private final boolean mainComponent;

    private final LinkData.BranchAdmittanceMatrix branchAdmittanceMatrix;

    public LegData(Leg leg, double ratedU0, int phaseAngleClock, boolean twtSplitShuntAdmittance) {
        this(leg, ratedU0, phaseAngleClock, 0.0, false, twtSplitShuntAdmittance);
    }

    public LegData(Leg leg, double ratedU0, int phaseAngleClock, double epsilonX, boolean applyReactanceCorrection,
        boolean twtSplitShuntAdmittance) {
        Objects.requireNonNull(leg);

        r = getR(leg);
        x = LinkData.getFixedX(getX(leg), epsilonX, applyReactanceCorrection);

        g1 = getG1(leg, twtSplitShuntAdmittance);
        b1 = getB1(leg, twtSplitShuntAdmittance);
        g2 = getG2(leg, twtSplitShuntAdmittance);
        b2 = getB2(leg, twtSplitShuntAdmittance);

        rho = rho(leg, ratedU0);
        alpha = alpha(leg, phaseAngleClock);
        double rhof = 1.0;
        double alphaf = 0.0;

        connected = leg.getTerminal().isConnected();
        mainComponent = isMainComponent(leg);
        branchAdmittanceMatrix = LinkData.calculateBranchAdmittance(r, x,
            1 / rho, -alpha, 1 / rhof, -alphaf, new Complex(g1, b1), new Complex(g2, b2));

        u = getV(leg);
        theta = getTheta(leg);
        ratedU = leg.getRatedU();
    }

    public static double rho(Leg leg, double ratedU0) {
        double rho = ratedU0 / leg.getRatedU();
        rho *= leg.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getRho()).orElse(1d);
        rho *= leg.getOptionalPhaseTapChanger().map(ptc -> ptc.getCurrentStep().getRho()).orElse(1d);
        return rho;
    }

    private static double alpha(Leg leg) {
        return leg.getOptionalPhaseTapChanger().map(ptc -> Math.toRadians(ptc.getCurrentStep().getAlpha())).orElse(0d);
    }

    private static double alpha(Leg leg, int phaseAngleClock) {
        return alpha(leg) + Math.toRadians(LinkData.getPhaseAngleClockDegrees(phaseAngleClock));
    }

    public static double alphaDegrees(Leg leg, int phaseAngleClock) {
        return Math.toDegrees(alpha(leg) + LinkData.getPhaseAngleClockDegrees(phaseAngleClock));
    }

    public Complex getComplexVFromZ0AtStarBus() {
        if (connected && valid(u, theta)) {
            return ComplexUtils.polar2Complex(u * rho, theta + alpha);
        }
        return new Complex(Double.NaN, Double.NaN);
    }

    public Complex getFlowAtStarBus(Complex vstar) {
        if (connected) {
            if (valid(u, theta)) {
                Complex v = ComplexUtils.polar2Complex(u, theta);
                return branchAdmittanceMatrix.y21().multiply(v)
                    .add(branchAdmittanceMatrix.y22().multiply(vstar)).multiply(vstar.conjugate())
                    .conjugate();
            }
            return new Complex(Double.NaN, Double.NaN);
        } else {
            Complex vantenna = branchAdmittanceMatrix.y12().multiply(vstar)
                .divide(branchAdmittanceMatrix.y11()).negate();
            return branchAdmittanceMatrix.y21().multiply(vantenna)
                .add(branchAdmittanceMatrix.y22().multiply(vstar)).multiply(vstar.conjugate())
                .conjugate();
        }
    }

    public Complex getFlow(Complex vstar) {
        if (connected) {
            if (valid(u, theta)) {
                Complex v = ComplexUtils.polar2Complex(u, theta);
                return branchAdmittanceMatrix.y11().multiply(v)
                    .add(branchAdmittanceMatrix.y12().multiply(vstar)).multiply(v.conjugate())
                    .conjugate();
            }
            return new Complex(Double.NaN, Double.NaN);
        } else {
            return new Complex(0.0, 0.0);
        }
    }

    private static double getV(Leg leg) {
        return leg.getTerminal().isConnected() ? leg.getTerminal().getBusView().getBus().getV() : Double.NaN;
    }

    private static double getTheta(Leg leg) {
        return leg.getTerminal().isConnected() ? Math.toRadians(leg.getTerminal().getBusView().getBus().getAngle()) : Double.NaN;
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

    // Only one leg with impedance, the two zero impedance legs are disconnected
    public Complex flowWhenIsAntennaAtTheStarBus() {
        if (connected) {
            Complex ysh = LinkData.kronAntenna(branchAdmittanceMatrix.y11(), branchAdmittanceMatrix.y12(),
                branchAdmittanceMatrix.y21(), branchAdmittanceMatrix.y22(), false);
            return LinkData.flowYshunt(ysh, u, theta);
        } else {
            return Complex.ZERO;
        }
    }

    // Two legs with impedance, the zero impedance leg is disconnected
    public LinkData.Flow flowWhenIsChainAtTheStarBus(LegData otherLeg) {
        BranchAdmittanceMatrix chainAdm = LinkData.kronChain(branchAdmittanceMatrix, Branch.Side.TWO,
            otherLeg.getBranchAdmittanceMatrix(), Branch.Side.TWO);

        if (connected && otherLeg.isConnected()) {
            return LinkData.flowBothEnds(chainAdm.y11(), chainAdm.y12(), chainAdm.y21(), chainAdm.y22(),
                u, theta, otherLeg.getU(), otherLeg.getTheta());
        } else if (connected) {
            Complex ysh = LinkData.kronAntenna(chainAdm.y11(), chainAdm.y12(), chainAdm.y21(), chainAdm.y22(), false);
            LinkData.Flow flow = new LinkData.Flow();
            flow.setFromTo(LinkData.flowYshunt(ysh, u, theta));
            return flow;
        } else if (otherLeg.isConnected()) {
            Complex ysh = LinkData.kronAntenna(chainAdm.y11(), chainAdm.y12(), chainAdm.y21(), chainAdm.y22(), true);
            LinkData.Flow flow = new LinkData.Flow();
            flow.setToFrom(LinkData.flowYshunt(ysh, otherLeg.getU(), otherLeg.getTheta()));
            return flow;
        } else {
            return new LinkData.Flow();
        }
    }

    public double getR() {
        return r;
    }

    public double getX() {
        return x;
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

    public double getRho() {
        return rho;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getU() {
        return u;
    }

    public double getTheta() {
        return theta;
    }

    public double getRatedU() {
        return ratedU;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isMainComponent() {
        return mainComponent;
    }

    public LinkData.BranchAdmittanceMatrix getBranchAdmittanceMatrix() {
        return branchAdmittanceMatrix;
    }
}
