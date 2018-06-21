/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

/**
 * Utility class to compute the state variables on one side of a branch, knowing
 * the state variables on the other side.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SV {

    public static double getRatio(TwoWindingsTransformer twt) {
        double rho = twt.getRatedU2() / twt.getRatedU1();
        if (twt.getRatioTapChanger() != null) {
            rho *= twt.getRatioTapChanger().getCurrentStep().getRho();
        }
        return rho;
    }

    public static double getR(TwoWindingsTransformer twt) {
        double r = twt.getR();
        if (twt.getRatioTapChanger() != null) {
            r *= 1 + twt.getRatioTapChanger().getCurrentStep().getR() / 100;
        }
        return r;
    }

    public static double getX(TwoWindingsTransformer twt) {
        double x = twt.getX();
        if (twt.getRatioTapChanger() != null) {
            x *= 1 + twt.getRatioTapChanger().getCurrentStep().getX() / 100;
        }
        return x;
    }

    public static double getG(TwoWindingsTransformer twt) {
        double g = twt.getG();
        if (twt.getRatioTapChanger() != null) {
            g *= 1 + twt.getRatioTapChanger().getCurrentStep().getG() / 100;
        }
        return g;
    }

    public static double getB(TwoWindingsTransformer twt) {
        double b = twt.getB();
        if (twt.getRatioTapChanger() != null) {
            b *= 1 + twt.getRatioTapChanger().getCurrentStep().getB() / 100;
        }
        return b;
    }

    double p;

    double q;

    double u;

    double a;

    public SV(double p, double q, double u, double a) {
        this.p = p;
        this.q = q;
        this.u = u;
        this.a = a;
    }

    public double getP() {
        return p;
    }

    public double getQ() {
        return q;
    }

    public double getU() {
        return u;
    }

    public double getA() {
        return a;
    }

    public SV otherSide(double r, double x, double g, double b, double ratio) {
        Complex z = new Complex(r, x); // z=r+jx
        Complex y = new Complex(g, b); // y=g+jb
        Complex s1 = new Complex(p, q); // s1=p1+jq1
        Complex u1 = ComplexUtils.polar2Complex(u, Math.toRadians(a));
        Complex v1 = u1.divide(Math.sqrt(3f)); // v1=u1/sqrt(3)

        Complex v1p = v1.multiply(ratio); // v1p=v1*rho
        Complex i1 = s1.divide(v1.multiply(3)).conjugate(); // i1=conj(s1/(3*v1))
        Complex i1p = i1.divide(ratio); // i1p=i1/rho
        Complex i2 = i1p.subtract(y.multiply(v1p)); // i2=i1p-y*v1p
        Complex v2 = v1p.subtract(z.multiply(i2)); // v2=v1p-z*i2
        Complex s2 = v2.multiply(3).multiply(i2.conjugate()); // s2=3*v2*conj(i2)

        Complex u2 = v2.multiply(Math.sqrt(3f));
        return new SV(-s2.getReal(), -s2.getImaginary(), u2.abs(), Math.toDegrees(u2.getArgument()));
    }

    public SV otherSide(double r, double x, double g1, double b1, double g2, double b2, double ratio) {
        Complex z = new Complex(r, x); // z=r+jx
        Complex y1 = new Complex(g1, b1); // y1=g1+jb1
        Complex y2 = new Complex(g2, b2); // y2=g2+jb2
        Complex s1 = new Complex(p, q); // s1=p1+jq1
        Complex u1 = ComplexUtils.polar2Complex(u, Math.toRadians(a));
        Complex v1 = u1.divide(Math.sqrt(3f)); // v1=u1/sqrt(3)

        Complex v1p = v1.multiply(ratio); // v1p=v1*rho
        Complex i1 = s1.divide(v1.multiply(3)).conjugate(); // i1=conj(s1/(3*v1))
        Complex i1p = i1.divide(ratio); // i1p=i1/rho
        Complex i2p = i1p.subtract(y1.multiply(v1p)); // i2p=i1p-y1*v1p
        Complex v2 = v1p.subtract(z.multiply(i2p)); // v2p=v1p-z*i2
        Complex i2 = i2p.subtract(y2.multiply(v2)); // i2=i2p-y2*v2
        Complex s2 = v2.multiply(3).multiply(i2.conjugate()); // s2=3*v2*conj(i2)

        Complex u2 = v2.multiply(Math.sqrt(3f));
        return new SV(-s2.getReal(), -s2.getImaginary(), u2.abs(), Math.toDegrees(u2.getArgument()));
    }

    public SV otherSide(TwoWindingsTransformer twt) {
        return otherSide(getR(twt), getX(twt), getG(twt), getB(twt), getRatio(twt));
    }

    public SV otherSide(Line l) {
        return otherSide(l.getR(), l.getX(), l.getG1() + l.getG2(), l.getB1() + l.getB2(), 1);
    }

    public SV otherSideY1Y2(Line l) {
        return otherSide(l.getR(), l.getX(), l.getG1(), l.getB1(), l.getG2(), l.getB2(), 1);
    }

    public SV otherSide(DanglingLine dl) {
        return otherSide(dl.getR(), dl.getX(), dl.getG(), dl.getB(), 1);
    }

    @Override
    public String toString() {
        return "p=" + p + ", q=" + q + ", u=" + u + ", a=" + a;
    }

}
