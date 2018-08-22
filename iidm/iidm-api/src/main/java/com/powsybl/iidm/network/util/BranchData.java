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

import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
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

    private final boolean connected1;
    private final boolean connected2;
    private final boolean mainComponent1;
    private final boolean mainComponent2;

    private double computedU1;
    private double computedU2;
    private double computedTheta1;
    private double computedTheta2;
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
        this.id = id;
        this.r = r;
        this.x = x;
        double fixedX = getFixedX(x, epsilonX, applyReactanceCorrection);
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
        double fixedX = getFixedX(x, epsilonX, applyReactanceCorrection);
        z = Math.hypot(r, fixedX);
        y = 1 / z;
        ksi = Math.atan2(r, fixedX);
        rho1 = 1f;
        rho2 = 1f;
        u1 = bus1 != null ? bus1.getV() : Double.NaN;
        u2 = bus2 != null ? bus2.getV() : Double.NaN;
        theta1 = bus1 != null ? Math.toRadians(bus1.getAngle()) : Double.NaN;
        theta2 = bus2 != null ? Math.toRadians(bus2.getAngle()) : Double.NaN;
        alpha1 = 0f;
        alpha2 = 0f;
        g1 = line.getG1();
        g2 = line.getG2();
        b1 = line.getB1();
        b2 = line.getB2();
        p1 = line.getTerminal1().getP();
        q1 = line.getTerminal1().getQ();
        p2 = line.getTerminal2().getP();
        q2 = line.getTerminal2().getQ();

        connected1 = bus1 != null;
        connected2 = bus2 != null;
        boolean connectableMainComponent1 = connectableBus1 != null && connectableBus1.isInMainConnectedComponent();
        boolean connectableMainComponent2 = connectableBus2 != null && connectableBus2.isInMainConnectedComponent();
        mainComponent1 = bus1 != null ? bus1.isInMainConnectedComponent() : connectableMainComponent1;
        mainComponent2 = bus2 != null ? bus2.isInMainConnectedComponent() : connectableMainComponent2;

        computeValues();
    }

    public BranchData(TwoWindingsTransformer twt, double epsilonX, boolean applyReactanceCorrection, boolean specificCompatibility) {
        Objects.requireNonNull(twt);

        id = twt.getId();

        Bus bus1 = twt.getTerminal1().getBusView().getBus();
        Bus bus2 = twt.getTerminal2().getBusView().getBus();
        Bus connectableBus1 = twt.getTerminal1().getBusView().getConnectableBus();
        Bus connectableBus2 = twt.getTerminal2().getBusView().getConnectableBus();

        r = getR(twt);
        x = getX(twt);
        double fixedX = getFixedX(x, epsilonX, applyReactanceCorrection);
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
        g1 = getG1(twt, specificCompatibility);
        g2 = specificCompatibility ? twt.getG() / 2 : 0f;
        b1 = getB1(twt, specificCompatibility);
        b2 = specificCompatibility ? twt.getB() / 2 : 0f;
        p1 = twt.getTerminal1().getP();
        q1 = twt.getTerminal1().getQ();
        p2 = twt.getTerminal2().getP();
        q2 = twt.getTerminal2().getQ();

        connected1 = bus1 != null;
        connected2 = bus2 != null;
        boolean connectableMainComponent1 = connectableBus1 != null && connectableBus1.isInMainConnectedComponent();
        boolean connectableMainComponent2 = connectableBus2 != null && connectableBus2.isInMainConnectedComponent();
        mainComponent1 = bus1 != null ? bus1.isInMainConnectedComponent() : connectableMainComponent1;
        mainComponent2 = bus2 != null ? bus2.isInMainConnectedComponent() : connectableMainComponent2;

        computeValues();
    }

    private double getFixedX(double x, double epsilonX, boolean applyReactanceCorrection) {
        return Math.abs(x) < epsilonX && applyReactanceCorrection ? epsilonX : x;
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

    private double getG1(TwoWindingsTransformer twt, boolean specificCompatibility) {
        return getValue(specificCompatibility ? twt.getG() / 2 : twt.getG(),
                        twt.getRatioTapChanger() != null ? twt.getRatioTapChanger().getCurrentStep().getG() : 0,
                        twt.getPhaseTapChanger() != null ? twt.getPhaseTapChanger().getCurrentStep().getG() : 0);
    }

    private double getB1(TwoWindingsTransformer twt, boolean specificCompatibility) {
        return getValue(specificCompatibility ? twt.getB() / 2 : twt.getB(),
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
        Complex ytr = new Complex(r, x).reciprocal();
        Complex y1 = new Complex(g1, b1);
        Complex y2 = new Complex(g2, b2);
        Complex a1 = ComplexUtils.polar2Complex(1 / rho1, -alpha1);
        Complex a2 = ComplexUtils.polar2Complex(1 / rho2, -alpha2);
        Complex a1cc = a1.conjugate();
        Complex a2cc = a2.conjugate();

        // If one of the ends is disconnected,
        // compute the voltage at the disconnected end
        if (connected1 || !connected2) {
            computedU1 = u1;
            computedTheta1 = theta1;
        } else {
            Complex v2 = ComplexUtils.polar2Complex(u2, theta2);
            Complex v1 = v2.multiply(ytr.divide(a1cc.multiply(a2)).multiply(a1cc.multiply(a1).divide(ytr.add(y1))));
            computedU1 = v1.abs();
            computedTheta1 = v1.getArgument();
        }
        if (connected2 || !connected1) {
            computedU2 = u2;
            computedTheta2 = theta2;
        } else {
            Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
            Complex v2 = v1.multiply(ytr.divide(a2cc.multiply(a1)).multiply(a2cc.multiply(a2).divide(ytr.add(y2))));
            computedU2 = v2.abs();
            computedTheta2 = v2.getArgument();
        }

        Complex v1 = ComplexUtils.polar2Complex(computedU1, computedTheta1);
        Complex v2 = ComplexUtils.polar2Complex(computedU2, computedTheta2);

        // Optimization: .divide(a1.multiply(a1.conjugate())) == .multiply(rho1 * rho1)
        Complex y11 = ytr.add(y1).multiply(rho1 * rho1);
        Complex y12 = ytr.negate().divide(a1cc.multiply(a2));
        // Optimization: .divide(a2.multiply(a2.conjugate())) == .multiply(rho2 * rho2)
        Complex y22 = ytr.add(y2).multiply(rho2 * rho2);
        Complex y21 = ytr.negate().divide(a1.multiply(a2cc));

        Complex i12 = y11.multiply(v1).add(y12.multiply(v2));
        Complex i21 = y22.multiply(v2).add(y21.multiply(v1));

        Complex s1 = i12.conjugate().multiply(v1);
        Complex s2 = i21.conjugate().multiply(v2);

        computedP1 = connected1 ? s1.getReal() : Double.NaN;
        computedQ1 = connected1 ? s1.getImaginary() : Double.NaN;
        computedP2 = connected2 ? s2.getReal() : Double.NaN;
        computedQ2 = connected2 ? s2.getImaginary() : Double.NaN;
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

    public double getComputedU1() {
        return computedU1;
    }

    public double getComputedTheta1() {
        return computedTheta1;
    }

    public double getComputedU2() {
        return computedU2;
    }

    public double getComputedTheta2() {
        return computedTheta2;
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
}
