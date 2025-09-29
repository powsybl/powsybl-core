/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.BoundaryLine;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class BoundaryLineData {

    private final BoundaryLine boundaryLine;

    private final double boundaryBusU;
    private final double boundaryBusTheta;

    public BoundaryLineData(BoundaryLine boundaryLine) {
        this.boundaryLine = Objects.requireNonNull(boundaryLine);

        double u1 = getV(boundaryLine);
        double theta1 = getTheta(boundaryLine);

        if (!valid(u1, theta1)) {
            boundaryBusU = Double.NaN;
            boundaryBusTheta = Double.NaN;
            return;
        }

        if (zeroImpedance(boundaryLine)) {
            boundaryBusU = u1;
            boundaryBusTheta = theta1;
            return;
        }

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);

        // DanglingLine model has shunt admittance on network side only, so it is not split between both sides.
        Complex vBoundaryBus = new Complex(Double.NaN, Double.NaN);
        if (boundaryLine.getP0() == 0.0 && boundaryLine.getQ0() == 0.0) {
            LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(
                    boundaryLine.getR(), boundaryLine.getX(), 1.0, 0.0, 1.0, 0.0, new Complex(boundaryLine.getG(), boundaryLine.getB()), new Complex(0, 0));
            vBoundaryBus = adm.y21().multiply(v1).negate().divide(adm.y22());
        } else {

            // Two buses Loadflow
            Complex sBoundary = new Complex(-boundaryLine.getP0(), -boundaryLine.getQ0());
            Complex zt = new Complex(boundaryLine.getR(), boundaryLine.getX());
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

    static double getV(BoundaryLine boundaryLine) {
        return boundaryLine.getTerminal().isConnected() ? boundaryLine.getTerminal().getBusView().getBus().getV()
            : Double.NaN;
    }

    static double getTheta(BoundaryLine boundaryLine) {
        return boundaryLine.getTerminal().isConnected()
            ? Math.toRadians(boundaryLine.getTerminal().getBusView().getBus().getAngle())
            : Double.NaN;
    }

    private static boolean valid(double v, double theta) {
        if (Double.isNaN(v) || v <= 0.0) {
            return false;
        }
        return !Double.isNaN(theta);
    }

    public String getId() {
        return boundaryLine.getId();
    }

    public double getBoundaryBusU() {
        return boundaryBusU;
    }

    public double getBoundaryBusTheta() {
        return boundaryBusTheta;
    }

    public static boolean zeroImpedance(BoundaryLine parent) {
        // Simple way to deal with zero impedance dangling line.
        return parent.getR() == 0.0 && parent.getX() == 0.0;
    }
}

