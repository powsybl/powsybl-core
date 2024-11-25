/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import com.powsybl.iidm.network.DanglingLine;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class DanglingLineData {

    private final DanglingLine danglingLine;

    private final double boundaryBusU;
    private final double boundaryBusTheta;

    public DanglingLineData(DanglingLine danglingLine) {
        this.danglingLine = Objects.requireNonNull(danglingLine);

        double u1 = getV(danglingLine);
        double theta1 = getTheta(danglingLine);

        if (!valid(u1, theta1)) {
            boundaryBusU = Double.NaN;
            boundaryBusTheta = Double.NaN;
            return;
        }

        if (zeroImpedance(danglingLine)) {
            boundaryBusU = u1;
            boundaryBusTheta = theta1;
            return;
        }

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);

        // DanglingLine model has shunt admittance on network side only, so it is not split between both sides.
        Complex vBoundaryBus = new Complex(Double.NaN, Double.NaN);
        if (danglingLine.getP0() == 0.0 && danglingLine.getQ0() == 0.0) {
            LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(
                    danglingLine.getR(), danglingLine.getX(), 1.0, 0.0, 1.0, 0.0, new Complex(danglingLine.getG(), danglingLine.getB()), new Complex(0, 0));
            vBoundaryBus = adm.y21().multiply(v1).negate().divide(adm.y22());
        } else {

            // Two buses Loadflow
            Complex sBoundary = new Complex(-danglingLine.getP0(), -danglingLine.getQ0());
            Complex zt = new Complex(danglingLine.getR(), danglingLine.getX());
            double v12 = v1.abs() * v1.abs();

            Complex sigma = zt.multiply(sBoundary.conjugate()).multiply(1.0 / v12);
            double d = 0.25 + sigma.getReal() - sigma.getImaginary() * sigma.getImaginary();
            // d < 0 Collapsed network
            if (d >= 0) {
                vBoundaryBus = new Complex(0.5 + Math.sqrt(d), sigma.getImaginary()).multiply(v1);
            }
        }

        boundaryBusU = vBoundaryBus.abs();
        boundaryBusTheta = vBoundaryBus.getArgument();
    }

    static double getV(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected() ? danglingLine.getTerminal().getBusView().getBus().getV()
            : Double.NaN;
    }

    static double getTheta(DanglingLine danglingLine) {
        return danglingLine.getTerminal().isConnected()
            ? Math.toRadians(danglingLine.getTerminal().getBusView().getBus().getAngle())
            : Double.NaN;
    }

    private static boolean valid(double v, double theta) {
        if (Double.isNaN(v) || v <= 0.0) {
            return false;
        }
        return !Double.isNaN(theta);
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

    public static boolean zeroImpedance(DanglingLine parent) {
        // Simple way to deal with zero impedance dangling line.
        return parent.getR() == 0.0 && parent.getX() == 0.0;
    }
}

