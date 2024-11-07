/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.TwoSides;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */

public final class LinkData {

    private LinkData() {
    }

    static double getFixedX(double x, double epsilonX, boolean applyReactanceCorrection) {
        return Math.abs(x) < epsilonX && applyReactanceCorrection ? epsilonX : x;
    }

    public static BranchAdmittanceMatrix calculateBranchAdmittance(double r, double x, double ratio1, double angle1,
        double ratio2, double angle2, Complex ysh1, Complex ysh2) {

        Complex a1 = ComplexUtils.polar2Complex(ratio1, angle1);
        Complex a2 = ComplexUtils.polar2Complex(ratio2, angle2);

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

    public static BranchAdmittanceMatrix kronChain(BranchAdmittanceMatrix firstAdm, TwoSides firstChainNodeSide,
        BranchAdmittanceMatrix secondAdm, TwoSides secondChainNodeSide) {
        BranchAdmittanceMatrix admittance = new BranchAdmittanceMatrix();

        Complex yFirst11;
        Complex yFirst1C;
        Complex yFirstC1;
        Complex yFirstCC;
        if (firstChainNodeSide == TwoSides.TWO) {
            yFirst11 = firstAdm.y11();
            yFirst1C = firstAdm.y12();
            yFirstC1 = firstAdm.y21();
            yFirstCC = firstAdm.y22();
        } else {
            yFirst11 = firstAdm.y22();
            yFirst1C = firstAdm.y21();
            yFirstC1 = firstAdm.y12();
            yFirstCC = firstAdm.y11();
        }

        Complex ySecond22;
        Complex ySecond2C;
        Complex ySecondC2;
        Complex ySecondCC;
        if (secondChainNodeSide == TwoSides.TWO) {
            ySecond22 = secondAdm.y11();
            ySecond2C = secondAdm.y12();
            ySecondC2 = secondAdm.y21();
            ySecondCC = secondAdm.y22();
        } else {
            ySecond22 = secondAdm.y22();
            ySecond2C = secondAdm.y21();
            ySecondC2 = secondAdm.y12();
            ySecondCC = secondAdm.y11();
        }

        admittance.y11 = yFirst11.subtract(yFirst1C.multiply(yFirstC1).divide(yFirstCC.add(ySecondCC)));
        admittance.y12 = yFirst1C.multiply(ySecondC2).divide(yFirstCC.add(ySecondCC)).negate();
        admittance.y21 = ySecond2C.multiply(yFirstC1).divide(yFirstCC.add(ySecondCC)).negate();
        admittance.y22 = ySecond22.subtract(ySecond2C.multiply(ySecondC2).divide(yFirstCC.add(ySecondCC)));

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

    public static Flow flowBothEnds(Complex y11, Complex y12, Complex y21, Complex y22,
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
        double phaseAngleClockDegree = Math.IEEEremainder(phaseAngleClock * 30.0, 360.0);
        if (phaseAngleClockDegree > 180.0) {
            phaseAngleClockDegree -= 360.0;
        }
        return phaseAngleClockDegree;
    }

    public static class BranchAdmittanceMatrix {
        private Complex y11;
        private Complex y12;
        private Complex y21;
        private Complex y22;

        public BranchAdmittanceMatrix() {
            y11 = Complex.ZERO;
            y12 = Complex.ZERO;
            y21 = Complex.ZERO;
            y22 = Complex.ZERO;
        }

        public BranchAdmittanceMatrix(Complex y11, Complex y12, Complex y21, Complex y22) {
            this.y11 = y11;
            this.y12 = y12;
            this.y21 = y21;
            this.y22 = y22;
        }

        public Complex y11() {
            return y11;
        }

        public Complex y12() {
            return y12;
        }

        public Complex y21() {
            return y21;
        }

        public Complex y22() {
            return y22;
        }
    }

    public static class Flow {
        public Complex fromTo = Complex.ZERO;
        public Complex toFrom = Complex.ZERO;

        public Complex getFromTo() {
            return fromTo;
        }

        public Complex getToFrom() {
            return toFrom;
        }

    }
}
