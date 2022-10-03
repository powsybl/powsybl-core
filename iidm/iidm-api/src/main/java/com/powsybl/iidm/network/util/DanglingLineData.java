/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import com.powsybl.iidm.network.DanglingLine;

import java.util.Objects;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class DanglingLineData {

    private final DanglingLine danglingLine;

    private final double networkFlowP;
    private final double networkFlowQ;
    private final double boundaryBusU;
    private final double boundaryBusTheta;
    private final double boundaryFlowP;
    private final double boundaryFlowQ;

    public DanglingLineData(DanglingLine danglingLine) {
        this(danglingLine, true);
    }

    public DanglingLineData(DanglingLine danglingLine, boolean splitShuntAdmittance) {
        this.danglingLine = Objects.requireNonNull(danglingLine);

        double networkU = getV(danglingLine);
        double networkTheta = getTheta(danglingLine);

        if (!isVoltageValid(networkU, networkTheta)) {
            boundaryBusU = Double.NaN;
            boundaryBusTheta = Double.NaN;
            boundaryFlowP = isZ0(danglingLine) ? -danglingLine.getTerminal().getP() : Double.NaN;
            boundaryFlowQ = isZ0(danglingLine) ? -danglingLine.getTerminal().getQ() : Double.NaN;
            networkFlowP = danglingLine.getTerminal().getP();
            networkFlowQ = danglingLine.getTerminal().getQ();
            return;
        }

        if (isZ0(danglingLine)) {
            boundaryBusU = networkU;
            boundaryBusTheta = networkTheta;
            boundaryFlowP = -danglingLine.getTerminal().getP();
            boundaryFlowQ = -danglingLine.getTerminal().getQ();
            networkFlowP = danglingLine.getTerminal().getP();
            networkFlowQ = danglingLine.getTerminal().getQ();
            return;
        }

        LinkData.BranchAdmittanceMatrix adm = calculateAddmitanceMatrix(splitShuntAdmittance);
        Complex networkV = ComplexUtils.polar2Complex(networkU, networkTheta);

        Complex boundaryV = boundaryVoltageAndAngle(networkV, adm);
        boundaryBusU = boundaryV.abs();
        boundaryBusTheta = boundaryV.getArgument();

        if (isVoltageValid(boundaryBusU, boundaryBusTheta)) {
            Complex boundaryFlow = boundaryFlow(networkV, boundaryV, adm);
            boundaryFlowP = boundaryFlow.getReal();
            boundaryFlowQ = boundaryFlow.getImaginary();

            Complex networkFlow = networkFlow(networkV, boundaryV, adm);
            networkFlowP = Double.isNaN(danglingLine.getTerminal().getP()) ? networkFlow.getReal() : danglingLine.getTerminal().getP();
            networkFlowQ = Double.isNaN(danglingLine.getTerminal().getQ()) ? networkFlow.getImaginary() : danglingLine.getTerminal().getQ();
        } else {
            boundaryFlowP = Double.NaN;
            boundaryFlowQ = Double.NaN;
            networkFlowP = danglingLine.getTerminal().getP();
            networkFlowQ = danglingLine.getTerminal().getQ();
        }
    }

    private Complex boundaryVoltageAndAngle(Complex networkV, LinkData.BranchAdmittanceMatrix adm) {
        Complex networkFlow = new Complex(danglingLine.getTerminal().getP(), danglingLine.getTerminal().getQ());
        if (isFlowValid(networkFlow)) {
            return calculateBoundaryVoltageFromVoltageAndFlowAtNetworkSide(networkV, networkFlow, adm);
        }

        boolean regulatingControlOn = getRegulatingControlOn();
        if (regulatingControlOn) {
            return calculateBoundaryVoltageFromTwoBusesLoadflowPV(networkV, adm);
        }

        Complex boundaryInjection = calculateBoundaryInjection();
        if (boundaryInjection.equals(Complex.ZERO)) {
            return calculateBoundaryVoltageFromVoltageAtNetworkSide(networkV, adm);
        }

        return calculateBoundaryVoltageFromTwoBusesLoadflowPQ(networkV, boundaryInjection, adm);
    }

    private Complex boundaryFlow(Complex networkV, Complex boundaryV, LinkData.BranchAdmittanceMatrix adm) {
        return adm.y21().multiply(networkV).add(adm.y22().multiply(boundaryV)).multiply(boundaryV.conjugate()).conjugate();
    }

    private Complex networkFlow(Complex networkV, Complex boundaryV, LinkData.BranchAdmittanceMatrix adm) {
        return adm.y11().multiply(networkV).add(adm.y12().multiply(boundaryV)).multiply(networkV.conjugate()).conjugate();
    }

    private static Complex calculateBoundaryVoltageFromVoltageAndFlowAtNetworkSide(Complex networkV,
        Complex networkFlow, LinkData.BranchAdmittanceMatrix adm) {
        return networkFlow.conjugate().divide(networkV.conjugate()).subtract(adm.y11().multiply(networkV)).divide(adm.y12());
    }

    private static Complex calculateBoundaryVoltageFromVoltageAtNetworkSide(Complex networkV, LinkData.BranchAdmittanceMatrix adm) {
        return adm.y21().multiply(networkV).negate().divide(adm.y22());
    }

    private static Complex calculateBoundaryVoltageFromTwoBusesLoadflowPQ(Complex v0, Complex injection,
        LinkData.BranchAdmittanceMatrix adm) {

        Complex s = injection.negate();

        // U = -(Y22 . V)/(Y21 . V0)
        // sigma = (Y22* . S*) / (Y21 . Y21* . V0 . V0*)
        Complex sigma = adm.y22().conjugate().multiply(s.conjugate())
            .divide(adm.y21().multiply(adm.y21().conjugate()).multiply(v0).multiply(v0.conjugate()));

        // Solution of U = 1 + sigma / U*
        double d = 0.25 + sigma.getReal() - sigma.getImaginary() * sigma.getImaginary();
        if (d >= 0) {
            return new Complex(0.5 + Math.sqrt(d), sigma.getImaginary()).multiply(v0).multiply(adm.y21().divide(adm.y22()).negate());
        }
        // d < 0 Collapsed network
        return new Complex(Double.NaN, Double.NaN);
    }

    private Complex calculateBoundaryVoltageFromTwoBusesLoadflowPV(Complex v0, LinkData.BranchAdmittanceMatrix adm) {
        double vSetpoint = danglingLine.getGeneration().getTargetV();
        double p = danglingLine.getP0() - danglingLine.getGeneration().getTargetP();
        double qMin = danglingLine.getGeneration().getReactiveLimits().getMinQ(danglingLine.getGeneration().getTargetP()) - danglingLine.getQ0();
        double qMax = danglingLine.getGeneration().getReactiveLimits().getMaxQ(danglingLine.getGeneration().getTargetP()) - danglingLine.getQ0();

        double qFlow = reactiveFlowFromUnlimitedTwoBusesLoadflowPV(v0, p, vSetpoint, adm);
        if (Double.isNaN(qFlow)) {
            Complex v = calculateBoundaryVoltageFromTwoBusesLoadflowPQ(v0, new Complex(p, -qMin), adm);
            if (v.abs() >= vSetpoint) {
                return v;
            }
            v = calculateBoundaryVoltageFromTwoBusesLoadflowPQ(v0, new Complex(p, -qMax), adm);
            if (v.abs() <= vSetpoint) {
                return v;
            }
            return new Complex(Double.NaN, Double.NaN);
        } else if (qFlow < qMin) {
            return calculateBoundaryVoltageFromTwoBusesLoadflowPQ(v0, new Complex(p, -qMin), adm);
        } else if (qFlow > qMax) {
            return calculateBoundaryVoltageFromTwoBusesLoadflowPQ(v0, new Complex(p, -qMax), adm);
        } else {
            return calculateBoundaryVoltageFromTwoBusesLoadflowPQ(v0, new Complex(p, -qFlow), adm);
        }
    }

    private static double reactiveFlowFromUnlimitedTwoBusesLoadflowPV(Complex v0, double pInjection, double vSetpoint,
        LinkData.BranchAdmittanceMatrix adm) {

        double vSetpoint2 = vSetpoint * vSetpoint;
        Complex y = adm.y22();
        Complex u0 = adm.y21().multiply(v0).divide(adm.y22()).negate();

        // pFlow = -p
        double d = y.multiply(y.conjugate()).multiply(u0).multiply(u0.conjugate()).multiply(vSetpoint2).getReal()
            - Math.pow(-pInjection - y.getReal() * vSetpoint2, 2);

        if (d >= 0.0) {
            return -y.getImaginary() * vSetpoint2 + sign(y.getImaginary()) * Math.sqrt(d);
        } else {
            return Double.NaN;
        }
    }

    private static int sign(double b) {
        return b > 0.0 ? 1 : -1;
    }

    private boolean getRegulatingControlOn() {
        return danglingLine.getGeneration() != null ? danglingLine.getGeneration().isVoltageRegulationOn() : Boolean.FALSE;
    }

    private Complex calculateBoundaryInjection() {
        double targetP = danglingLine.getGeneration() != null ? danglingLine.getGeneration().getTargetP() : 0.0;
        double targetQ = danglingLine.getGeneration() != null ? danglingLine.getGeneration().getTargetQ() : 0.0;
        return new Complex(danglingLine.getP0() - targetP, danglingLine.getQ0() - targetQ);
    }

    private LinkData.BranchAdmittanceMatrix calculateAddmitanceMatrix(boolean splitShuntAdmittance) {
        double g1 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : danglingLine.getG();
        double b1 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : danglingLine.getB();
        double g2 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : 0.0;
        double b2 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : 0.0;
        return LinkData.calculateBranchAdmittance(danglingLine.getR(), danglingLine.getX(), 1.0, 0.0, 1.0, 0.0,
            new Complex(g1, b1), new Complex(g2, b2));
    }

    public static boolean isZ0(DanglingLine dl) {
        return dl.getR() == 0.0 && dl.getX() == 0.0 && dl.getG() == 0.0 && dl.getB() == 0.0;
    }

    private static double getV(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected() ? danglingLine.getTerminal().getBusView().getBus().getV()
            : Double.NaN;
    }

    private static double getTheta(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected()
            ? Math.toRadians(danglingLine.getTerminal().getBusView().getBus().getAngle())
            : Double.NaN;
    }

    private static boolean isVoltageValid(double v, double theta) {
        return !Double.isNaN(v) && v > 0.0 && !Double.isNaN(theta);
    }

    private static boolean isFlowValid(Complex flow) {
        return !Double.isNaN(flow.getReal()) && !Double.isNaN(flow.getImaginary());
    }

    public String getId() {
        return danglingLine.getId();
    }

    public double getBoundaryBusU() {
        return boundaryBusU;
    }

    public double getBoundaryBusTheta() {
        return boundaryBusTheta;
    }

    public double getBoundaryFlowP() {
        return boundaryFlowP;
    }

    public double getBoundaryFlowQ() {
        return boundaryFlowQ;
    }

    public double getNetworkFlowP() {
        return networkFlowP;
    }

    public double getNetworkFlowQ() {
        return networkFlowQ;
    }
}
