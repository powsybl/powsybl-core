/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import java.util.Objects;

import org.apache.commons.math3.complex.Complex;

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class BranchData {

    private final String id;

    private final double r;
    private final double x;
    // If we use complex numbers for the calculation of branch flows
    // we do not need to compute z, y, ksi
    // these attributes could be removed
    private final double z;
    private final double y;
    private final double ksi;
    private final double rho1;
    private final double rho2;
    private final double u1;
    private final double u2;
    private final double theta1;
    private final double theta2;
    private final double alpha1;
    private final double alpha2;
    private final double g1;
    private final double g2;
    private final double b1;
    private final double b2;
    private final double p1;
    private final double q1;
    private final double p2;
    private final double q2;

    int phaseAngleClock;

    private final boolean connected1;
    private final boolean connected2;
    private final boolean mainComponent1;
    private final boolean mainComponent2;

    private double computedP1;
    private double computedQ1;
    private double computedP2;
    private double computedQ2;

    public BranchData(String id,
        double r, double x,
        double rho1, double rho2,
        double u1, double u2, double theta1, double theta2,
        double alpha1, double alpha2,
        double g1, double g2, double b1, double b2,
        double p1, double q1, double p2, double q2,
        boolean connected1, boolean connected2,
        boolean mainComponent1, boolean mainComponent2,
        double epsilonX, boolean applyReactanceCorrection) {
        this(id, r, x, rho1, rho2, u1, u2, theta1, theta2, alpha1, alpha2, g1, g2, b1, b2, p1, q1, p2, q2, connected1,
            connected2, mainComponent1, mainComponent2, 0, epsilonX, applyReactanceCorrection);
    }

    public BranchData(String id,
            double r, double x,
            double rho1, double rho2,
            double u1, double u2, double theta1, double theta2,
            double alpha1, double alpha2,
            double g1, double g2, double b1, double b2,
            double p1, double q1, double p2, double q2,
            boolean connected1, boolean connected2,
            boolean mainComponent1, boolean mainComponent2,
            int phaseAngleClock,
            double epsilonX, boolean applyReactanceCorrection) {
        this.id = id;
        this.r = r;
        this.x = x;
        double fixedX = LinkData.getFixedX(x, epsilonX, applyReactanceCorrection);
        z = Math.hypot(r, fixedX);
        y = 1 / z;
        ksi = Math.atan2(r, fixedX);
        this.rho1 = rho1;
        this.rho2 = rho2;
        this.u1 = u1;
        this.u2 = u2;
        this.theta1 = theta1;
        this.theta2 = theta2;
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
        this.g1 = g1;
        this.g2 = g2;
        this.b1 = b1;
        this.b2 = b2;
        this.p1 = p1;
        this.q1 = q1;
        this.p2 = p2;
        this.q2 = q2;
        this.phaseAngleClock = phaseAngleClock;
        this.connected1 = connected1;
        this.connected2 = connected2;
        this.mainComponent1 = mainComponent1;
        this.mainComponent2 = mainComponent2;
        computeValues();
    }

    public BranchData(Line line, double epsilonX, boolean applyReactanceCorrection) {
        Objects.requireNonNull(line);

        id = line.getId();

        Bus bus1 = line.getTerminal1().getBusView().getBus();
        Bus bus2 = line.getTerminal2().getBusView().getBus();
        Bus connectableBus1 = line.getTerminal1().getBusView().getConnectableBus();
        Bus connectableBus2 = line.getTerminal2().getBusView().getConnectableBus();

        r = line.getR();
        x = line.getX();
        double fixedX = LinkData.getFixedX(x, epsilonX, applyReactanceCorrection);
        z = Math.hypot(r, fixedX);
        y = 1 / z;
        ksi = Math.atan2(r, fixedX);
        u1 = bus1 != null ? bus1.getV() : Double.NaN;
        u2 = bus2 != null ? bus2.getV() : Double.NaN;
        theta1 = bus1 != null ? Math.toRadians(bus1.getAngle()) : Double.NaN;
        theta2 = bus2 != null ? Math.toRadians(bus2.getAngle()) : Double.NaN;
        g1 = line.getG1();
        g2 = line.getG2();
        b1 = line.getB1();
        b2 = line.getB2();
        p1 = line.getTerminal1().getP();
        q1 = line.getTerminal1().getQ();
        p2 = line.getTerminal2().getP();
        q2 = line.getTerminal2().getQ();

        phaseAngleClock = 0;

        connected1 = bus1 != null;
        connected2 = bus2 != null;
        boolean connectableMainComponent1 = connectableBus1 != null && connectableBus1.isInMainConnectedComponent();
        boolean connectableMainComponent2 = connectableBus2 != null && connectableBus2.isInMainConnectedComponent();
        mainComponent1 = bus1 != null ? bus1.isInMainConnectedComponent() : connectableMainComponent1;
        mainComponent2 = bus2 != null ? bus2.isInMainConnectedComponent() : connectableMainComponent2;

        rho2 = 1f;
        alpha1 = 0f;
        alpha2 = 0f;
        if (isStructuralRatioLineNeeded(line)) {
            rho1 = 1.0 / structuralRatioEnd1(line);
        } else {
            rho1 = 1f;
        }

        computeValues();
    }

    public BranchData(TwoWindingsTransformer twt, double epsilonX, boolean applyReactanceCorrection, boolean t2wtSplitShuntAdmittance) {
        this(twt, 0, epsilonX, applyReactanceCorrection, t2wtSplitShuntAdmittance);
    }

    public BranchData(TwoWindingsTransformer twt, int phaseAngleClock, double epsilonX, boolean applyReactanceCorrection, boolean t2wtSplitShuntAdmittance) {
        Objects.requireNonNull(twt);

        id = twt.getId();

        Bus bus1 = twt.getTerminal1().getBusView().getBus();
        Bus bus2 = twt.getTerminal2().getBusView().getBus();
        Bus connectableBus1 = twt.getTerminal1().getBusView().getConnectableBus();
        Bus connectableBus2 = twt.getTerminal2().getBusView().getConnectableBus();

        r = getR(twt);
        x = getX(twt);
        double fixedX = LinkData.getFixedX(x, epsilonX, applyReactanceCorrection);
        z = Math.hypot(r, fixedX);
        y = 1 / z;
        ksi = Math.atan2(r, fixedX);
        rho1 = getRho1(twt);
        rho2 = 1f;
        u1 = bus1 != null ? bus1.getV() : Double.NaN;
        u2 = bus2 != null ? bus2.getV() : Double.NaN;
        theta1 = bus1 != null ? Math.toRadians(bus1.getAngle()) : Double.NaN;
        theta2 = bus2 != null ? Math.toRadians(bus2.getAngle()) : Double.NaN;
        alpha1 = twt.getPhaseTapChanger() != null ? Math.toRadians(twt.getPhaseTapChanger().getCurrentStep().getAlpha()) : 0f;
        alpha2 = 0f;
        g1 = getG1(twt, t2wtSplitShuntAdmittance);
        g2 = getG2(twt, t2wtSplitShuntAdmittance);
        b1 = getB1(twt, t2wtSplitShuntAdmittance);
        b2 = getB2(twt, t2wtSplitShuntAdmittance);
        p1 = twt.getTerminal1().getP();
        q1 = twt.getTerminal1().getQ();
        p2 = twt.getTerminal2().getP();
        q2 = twt.getTerminal2().getQ();

        this.phaseAngleClock = phaseAngleClock;

        connected1 = bus1 != null;
        connected2 = bus2 != null;
        boolean connectableMainComponent1 = connectableBus1 != null && connectableBus1.isInMainConnectedComponent();
        boolean connectableMainComponent2 = connectableBus2 != null && connectableBus2.isInMainConnectedComponent();
        mainComponent1 = bus1 != null ? bus1.isInMainConnectedComponent() : connectableMainComponent1;
        mainComponent2 = bus2 != null ? bus2.isInMainConnectedComponent() : connectableMainComponent2;

        computeValues();
    }

    private boolean isStructuralRatioLineNeeded(Line line) {
        if (line.getTerminal1() == null || line.getTerminal1().getVoltageLevel() == null ||
            line.getTerminal2() == null || line.getTerminal2().getVoltageLevel() == null) {
            return false;
        }

        double nominalV1 = line.getTerminal1().getVoltageLevel().getNominalV();
        double nominalV2 = line.getTerminal2().getVoltageLevel().getNominalV();

        if (nominalV1 == 0.0 || Double.isNaN(nominalV1) ||
            nominalV2 == 0.0 || Double.isNaN(nominalV2)) {
            return false;
        }
        return nominalV1 != nominalV2;
    }

    private double structuralRatioEnd1(Line line) {
        double nominalV1 = line.getTerminal1().getVoltageLevel().getNominalV();
        double nominalV2 = line.getTerminal2().getVoltageLevel().getNominalV();
        return nominalV1 / nominalV2;
    }

    private double getValue(double initialValue, double rtcStepValue, double ptcStepValue) {
        return initialValue * (1 + rtcStepValue / 100) * (1 + ptcStepValue / 100);
    }

    private double getR(TwoWindingsTransformer twt) {
        return getValue(twt.getR(),
                        twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getR() : 0,
                        twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getR() : 0);
    }

    private double getX(TwoWindingsTransformer twt) {
        return getValue(twt.getX(),
                        twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getX() : 0,
                        twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getX() : 0);
    }

    private double getG1(TwoWindingsTransformer twt, boolean t2wtSplitShuntAdmittance) {
        return getValue(t2wtSplitShuntAdmittance ? twt.getG() / 2 : twt.getG(),
                        twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getG() : 0,
                        twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getG() : 0);
    }

    private double getB1(TwoWindingsTransformer twt, boolean t2wtSplitShuntAdmittance) {
        return getValue(t2wtSplitShuntAdmittance ? twt.getB() / 2 : twt.getB(),
                        twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getB() : 0,
                        twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getB() : 0);
    }

    private double getG2(TwoWindingsTransformer twt, boolean t2wtSplitShuntAdmittance) {
        return getValue(t2wtSplitShuntAdmittance ? twt.getG() / 2 : 0,
                        twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getG() : 0,
                        twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getG() : 0);
    }

    private double getB2(TwoWindingsTransformer twt, boolean t2wtSplitShuntAdmittance) {
        return getValue(t2wtSplitShuntAdmittance ? twt.getB() / 2 : 0,
                        twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getB() : 0,
                        twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getB() : 0);
    }

    private double getRho1(TwoWindingsTransformer twt) {
        double rho = twt.getRatedU2() / twt.getRatedU1();
        if (twt.getRatioTapChanger() != null) {
            rho *= twt.getRatioTapChanger().getCurrentStep().getRho();
        }
        if (twt.getPhaseTapChanger() != null) {
            rho *= twt.getPhaseTapChanger().getCurrentStep().getRho();
        }
        return rho;
    }

    private void computeValues() {
        if (!connected1 && !connected2) {
            computedP1 = Double.NaN;
            computedQ1 = Double.NaN;
            computedP2 = Double.NaN;
            computedQ2 = Double.NaN;
        } else {
            double angle1 = -alpha1;
            double angle2 = -alpha2 - Math.toRadians(LinkData.getPhaseAngleClockDegrees(phaseAngleClock));

            LinkData.BranchAdmittanceMatrix branchAdmittance = LinkData.calculateBranchAdmittance(r, x,
                1 / rho1, angle1, 1 / rho2, angle2, new Complex(g1, b1), new Complex(g2, b2));

            if (connected1 && connected2) {
                LinkData.Flow flow = LinkData.flowBothEnds(branchAdmittance.y11, branchAdmittance.y12,
                    branchAdmittance.y21, branchAdmittance.y22, u1, theta1, u2, theta2);

                computedP1 = flow.fromTo.getReal();
                computedQ1 = flow.fromTo.getImaginary();
                computedP2 = flow.toFrom.getReal();
                computedQ2 = flow.toFrom.getImaginary();
            } else if (connected1) {

                Complex ysh = LinkData.kronAntenna(branchAdmittance.y11, branchAdmittance.y12,
                    branchAdmittance.y21, branchAdmittance.y22, false);
                Complex sFrom = LinkData.flowYshunt(ysh, u1, theta1);

                computedP1 = sFrom.getReal();
                computedQ1 = sFrom.getImaginary();
                computedP2 = 0.0;
                computedQ2 = 0.0;
            } else {
                Complex ysh = LinkData.kronAntenna(branchAdmittance.y11, branchAdmittance.y12,
                    branchAdmittance.y21, branchAdmittance.y22, true);
                Complex sTo = LinkData.flowYshunt(ysh, u2, theta2);

                computedP1 = 0.0;
                computedQ1 = 0.0;
                computedP2 = sTo.getReal();
                computedQ2 = sTo.getImaginary();
            }
        }
    }

    public String getId() {
        return id;
    }

    public double getR() {
        return r;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    public double getY() {
        return y;
    }

    public double getKsi() {
        return ksi;
    }

    public double getRho1() {
        return rho1;
    }

    public double getRho2() {
        return rho2;
    }

    public double getU1() {
        return u1;
    }

    public double getU2() {
        return u2;
    }

    public double getTheta1() {
        return theta1;
    }

    public double getTheta2() {
        return theta2;
    }

    public double getAlpha1() {
        return alpha1;
    }

    public double getAlpha2() {
        return alpha2;
    }

    public double getG1() {
        return g1;
    }

    public double getG2() {
        return g2;
    }

    public double getB1() {
        return b1;
    }

    public double getB2() {
        return b2;
    }

    public boolean isConnected1() {
        return connected1;
    }

    public boolean isConnected2() {
        return connected2;
    }

    public boolean isMainComponent1() {
        return mainComponent1;
    }

    public boolean isMainComponent2() {
        return mainComponent2;
    }

    public double getP1() {
        return p1;
    }

    public double getQ1() {
        return q1;
    }

    public double getP2() {
        return p2;
    }

    public double getQ2() {
        return q2;
    }

    public double getComputedP1() {
        return computedP1;
    }

    public double getComputedQ1() {
        return computedQ1;
    }

    public double getComputedP2() {
        return computedP2;
    }

    public double getComputedQ2() {
        return computedQ2;
    }

    public double getComputedP(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return computedP1;
            case TWO:
                return computedP2;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    public double getComputedQ(Side side) {
        Objects.requireNonNull(side);
        switch (side) {
            case ONE:
                return computedQ1;
            case TWO:
                return computedQ2;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    public int getPhaseAngleClock() {
        return phaseAngleClock;
    }
}
