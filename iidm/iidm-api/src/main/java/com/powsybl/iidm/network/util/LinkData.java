/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 * @author Marcos de Miguel <demiguelm at aia.es>
 */

public final class LinkData {

    private LinkData() {
    }

    static double getFixedX(double x, double epsilonX, boolean applyReactanceCorrection) {
        return Math.abs(x) < epsilonX && applyReactanceCorrection ? epsilonX : x;
    }

    public static BranchAdmittanceMatrix calculateBranchAdmittance(double r, double x, double ratio1, double alpha1,
        double ratio2, double alpha2, Complex ysh1, Complex ysh2) {

        Complex a1 = ComplexUtils.polar2Complex(ratio1, alpha1);
        Complex a2 = ComplexUtils.polar2Complex(ratio2, alpha2);

        Complex ytr;
        if (r == 0.0 && x == 0.0) {
            ytr = Complex.ZERO;
        } else {
            ytr = new Complex(r, x).reciprocal();
        }

        BranchAdmittanceMatrix branchAdmittance = new BranchAdmittanceMatrix();

        branchAdmittance.y11 = ytr.add(ysh1).divide(a1.conjugate().multiply(a1));
        branchAdmittance.y12 = ytr.negate().divide(a1.conjugate().multiply(a2));
        branchAdmittance.y21 = ytr.negate().divide(a2.conjugate().multiply(a1));
        branchAdmittance.y22 = ytr.add(ysh2).divide(a2.conjugate().multiply(a2));

        return branchAdmittance;
    }

    static BranchAdmittanceMatrix kronChain(Complex yFirstConnected11, Complex yFirstConnected12,
        Complex yFirstConnected21, Complex yFirstConnected22, Complex ySecondConnected11,
        Complex ySecondConnected12, Complex ySecondConnected21, Complex ySecondConnected22) {
        BranchAdmittanceMatrix admittance = new BranchAdmittanceMatrix();

        admittance.y11 = yFirstConnected11.subtract(yFirstConnected21.multiply(yFirstConnected12)
            .divide(yFirstConnected22.add(ySecondConnected22)));
        admittance.y12 = ySecondConnected21.multiply(yFirstConnected12)
            .divide(yFirstConnected22.add(ySecondConnected22)).negate();
        admittance.y21 = yFirstConnected21.multiply(ySecondConnected12)
            .divide(yFirstConnected22.add(ySecondConnected22)).negate();
        admittance.y22 = ySecondConnected11.subtract(
            ySecondConnected21.multiply(ySecondConnected12).divide(yFirstConnected22.add(ySecondConnected22)));

        return admittance;
    }

    static Complex kronAntenna(Complex y11, Complex y12, Complex y21, Complex y22, boolean isOpenFrom) {
        Complex ysh = Complex.ZERO;

        if (isOpenFrom) {
            if (!y11.equals(Complex.ZERO)) {
                ysh = y22.subtract(y21.multiply(y12).divide(y11));
            }
        } else {
            if (!y22.equals(Complex.ZERO)) {
                ysh = y11.subtract(y12.multiply(y21).divide(y22));
            }
        }
        return ysh;
    }

    static Complex flowYshunt(Complex ysh, double u, double theta) {

        Complex v = ComplexUtils.polar2Complex(u, theta);

        return ysh.conjugate().multiply(v.conjugate().multiply(v));
    }

    static Flow flowBothEnds(Complex y11, Complex y12, Complex y21, Complex y22,
        double u1, double theta1, double u2, double theta2) {

        Complex v1 = ComplexUtils.polar2Complex(u1, theta1);
        Complex v2 = ComplexUtils.polar2Complex(u2, theta2);

        return flowBothEnds(y11, y12, y21, y22, v1, v2);
    }

    static Flow flowBothEnds(Complex y11, Complex y12, Complex y21, Complex y22,
        Complex v1, Complex v2) {

        Flow flow = new Flow();
        Complex ift = y12.multiply(v2).add(y11.multiply(v1));
        flow.fromTo = ift.conjugate().multiply(v1);

        Complex itf = y21.multiply(v1).add(y22.multiply(v2));
        flow.toFrom = itf.conjugate().multiply(v2);

        return flow;
    }

    static double getPhaseAngleClockDegrees(int phaseAngleClock) {
        double phaseAngleClockDegree = 0.0;
        phaseAngleClockDegree += phaseAngleClock * 30.0;
        phaseAngleClockDegree = Math.IEEEremainder(phaseAngleClockDegree, 360.0);
        if (phaseAngleClockDegree > 180.0) {
            phaseAngleClockDegree -= 360.0;
        }
        return phaseAngleClockDegree;
    }

    public static class BranchAdmittanceMatrix {
        Complex y11 = Complex.ZERO;
        Complex y12 = Complex.ZERO;
        Complex y21 = Complex.ZERO;
        Complex y22 = Complex.ZERO;

        public Complex y11() {
            return y11;
        }

        public Complex y12() {
            return y11;
        }

        public Complex y21() {
            return y11;
        }

        public Complex y22() {
            return y11;
        }
    }

    static class Flow {
        Complex fromTo = Complex.ZERO;
        Complex toFrom = Complex.ZERO;
    }
}
