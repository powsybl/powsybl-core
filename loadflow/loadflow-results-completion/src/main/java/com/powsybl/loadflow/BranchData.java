/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import java.util.Objects;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoTerminalsConnectable.Side;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BranchData {

    private final String id;

    private final double r;
    private final double x;
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
    private final float p1;
    private final float q1;
    private final float p2;
    private final float q2;

    private final boolean connected1;
    private final boolean connected2;
    private final boolean mainComponent1;
    private final boolean mainComponent2;

    private double computedU1;
    private double computedU2;
    private double computedP1;
    private double computedQ1;
    private double computedP2;
    private double computedQ2;

    public BranchData(Line line, float epsilonX, boolean applyReactanceCorrection) {
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

    public BranchData(TwoWindingsTransformer twt, float epsilonX, boolean applyReactanceCorrection, boolean specificCompatibility) {
        Objects.requireNonNull(twt);

        id = twt.getId();

        Bus bus1 = twt.getTerminal1().getBusView().getBus();
        Bus bus2 = twt.getTerminal2().getBusView().getBus();
        Bus connectableBus1 = twt.getTerminal1().getBusView().getConnectableBus();
        Bus connectableBus2 = twt.getTerminal2().getBusView().getConnectableBus();

        r = (float) getR(twt);
        x = (float) getX(twt);
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

    private double getFixedX(double x, float epsilonX, boolean applyReactanceCorrection) {
        return Math.abs(x) < epsilonX && applyReactanceCorrection ? epsilonX : x;
    }

    private double getValue(float initialValue, float rtcStepValue, float ptcStepValue) {
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
        computedU1 = connected1 || !connected2 ? u1 : computeU(u2, rho1, rho2, g1, b1);
        computedU2 = connected2 || !connected1 ? u2 : computeU(u1, rho2, rho1, g2, b2);
        double computedTheta1 = connected1 || !connected2 ? theta1 : computeTheta(theta2, alpha1, alpha2, computedU1, computedU2, rho1, rho2, g1, b1);
        double computedTheta2 = connected2 || !connected1 ? theta2 : computeTheta(theta1, alpha2, alpha1, computedU2, computedU1, rho2, rho1, g2, b2);
        computedP1 = connected1 ? computeP(computedTheta1, computedTheta2, alpha1, alpha2, rho1, computedU1, g1) : Float.NaN;
        computedQ1 = connected1 ? computeQ(computedTheta1, computedTheta2, alpha1, alpha2, rho1, computedU1, b1) : Float.NaN;
        computedP2 = connected2 ? computeP(computedTheta2, computedTheta1, alpha2, alpha1, rho2, computedU2, g2) : Float.NaN;
        computedQ2 = connected2 ? computeQ(computedTheta2, computedTheta1, alpha2, alpha1, rho2, computedU2, b2) : Float.NaN;
    }

    private double computeU(double otherU, double rho, double otherRho, double g, double b) {
        double z1 = y * Math.sin(ksi) + g;
        double z2 = y * Math.cos(ksi) - b;
        return 1 / Math.sqrt(z1 * z1 + z2 * z2) * (otherRho / rho) * y * otherU;
    }

    private double computeTheta(double otherTheta, double alpha, double otherAlpha, double u, double otherU,
                                double rho, double otherRho, double g, double b) {
        double z1 = y * Math.sin(ksi) + g;
        double z2 = y * Math.cos(ksi) - b;
        double phi = -(-otherTheta - ksi + alpha - otherAlpha);
        double cosThetaMinusPhi = rho / otherRho * u / otherU * (Math.cos(ksi) - b / y);
        return Math.atan(-z1 / z2) + phi + (cosThetaMinusPhi < 0 ? Math.PI : 0);
    }

    private double computeP(double theta, double otherTheta, double alpha, double otherAlpha, double rho, double u, double g) {
        return rho1 * rho2 * computedU1 * computedU2 * y * Math.sin(theta - otherTheta - ksi + alpha - otherAlpha) + rho * rho * u * u * (y * Math.sin(ksi) + g);
    }

    private double computeQ(double theta, double otherTheta, double alpha, double otherAlpha, double rho, double u, double b) {
        return -rho1 * rho2 * computedU1 * computedU2 * y * Math.cos(theta - otherTheta - ksi + alpha - otherAlpha) + rho * rho * u * u * (y * Math.cos(ksi) - b);
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

    public float getP1() {
        return p1;
    }

    public float getQ1() {
        return q1;
    }

    public float getP2() {
        return p2;
    }

    public float getQ2() {
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
}
