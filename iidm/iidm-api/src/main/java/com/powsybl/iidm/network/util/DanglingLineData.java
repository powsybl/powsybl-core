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
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class DanglingLineData {

    private final DanglingLine danglingLine;

    private final double boundaryBusU;
    private final double boundaryBusTheta;

    public DanglingLineData(DanglingLine danglingLine) {
        this(danglingLine, true);
    }

    public DanglingLineData(DanglingLine danglingLine, boolean splitShuntAdmittance) {
        this.danglingLine = Objects.requireNonNull(danglingLine);

        double g1 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : danglingLine.getG();
        double b1 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : danglingLine.getB();
        double g2 = splitShuntAdmittance ? danglingLine.getG() * 0.5 : 0.0;
        double b2 = splitShuntAdmittance ? danglingLine.getB() * 0.5 : 0.0;

        double u1 = getV(danglingLine);
        double theta1 = getTheta(danglingLine);

        if (!valid(u1, theta1)) {
            boundaryBusU = Double.NaN;
            boundaryBusTheta = Double.NaN;
            return;
        }

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);

        Complex vBoundaryBus = new Complex(Double.NaN, Double.NaN);
        if (danglingLine.getP0() == 0.0 && danglingLine.getQ0() == 0.0) {
            LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(danglingLine.getR(), danglingLine.getX(), 1.0, 0.0, 1.0, 0.0, new Complex(g1, b1), new Complex(g2, b2));
            vBoundaryBus = adm.y21().multiply(v1).negate().divide(adm.y22());
        } else {

            // Two buses Loadflow
            Complex sBoundary = new Complex(-danglingLine.getP0(), -danglingLine.getQ0());
            Complex ytr = new Complex(danglingLine.getR(), danglingLine.getX()).reciprocal();
            Complex ysh2 = new Complex(g2, b2);
            Complex zt = ytr.add(ysh2).reciprocal();
            Complex v0 = ytr.multiply(v1).divide(ytr.add(ysh2));
            double v02 = v0.abs() * v0.abs();

            Complex sigma = zt.multiply(sBoundary.conjugate()).multiply(1.0 / v02);
            double d = 0.25 + sigma.getReal() - sigma.getImaginary() * sigma.getImaginary();
            // d < 0 Collapsed network
            if (d >= 0) {
                vBoundaryBus = new Complex(0.5 + Math.sqrt(d), sigma.getImaginary()).multiply(v0);
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
}

