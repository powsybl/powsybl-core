/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.TieLine;
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

    public static double getRho(TwoWindingsTransformer twt) {
        return twt.getRatedU2() / twt.getRatedU1() * twt.getOptionalRatioTapChanger().map(rtc -> rtc.getCurrentStep().getRho()).orElse(1d);
    }

    public static double getR(TwoWindingsTransformer twt) {
        return twt.getR() * twt.getOptionalRatioTapChanger().map(rtc -> 1 + rtc.getCurrentStep().getR() / 100).orElse(1d);
    }

    public static double getX(TwoWindingsTransformer twt) {
        return twt.getX() * twt.getOptionalRatioTapChanger().map(rtc -> 1 + rtc.getCurrentStep().getX() / 100).orElse(1d);
    }

    public static double getG(TwoWindingsTransformer twt) {
        return twt.getG() * twt.getOptionalRatioTapChanger().map(rtc -> 1 + rtc.getCurrentStep().getG() / 100).orElse(1d);
    }

    public static double getB(TwoWindingsTransformer twt) {
        return twt.getB() * twt.getOptionalRatioTapChanger().map(rtc -> 1 + rtc.getCurrentStep().getB() / 100).orElse(1d);
    }

    double p;

    double q;

    double u;

    double a;

    private Complex s1;

    private Complex u1;

    private Complex v1;

    private Complex i1;

    public SV(double p, double q, double u, double a) {
        this.p = p;
        this.q = q;
        this.u = u;
        this.a = a;
        s1 = new Complex(p, q); // s=p+jq
        u1 = ComplexUtils.polar2Complex(u, Math.toRadians(a));
        v1 = u1.divide(Math.sqrt(3f)); // v1=u1/sqrt(3)
        i1 = s1.divide(v1.multiply(3)).conjugate(); // i1=conj(s1/(3*v1))
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

    private Complex computeU2(Complex y1, Complex y2, Complex z, double rho) {
        Complex v1p = v1.multiply(rho); // v1p=v1*rho
        Complex i1p = i1.divide(rho); // i1p=i1/rho

        Complex i2p = i1p.subtract(y1.multiply(v1p)); // i2p=i1p-y1*v1p
        Complex v2 = v1p.subtract(z.multiply(i2p)); // v2p=v1p-z*i2
        Complex i2 = i2p.subtract(y2.multiply(v2)); // i2=i2p-y2*v2
        Complex s2 = v2.multiply(3).multiply(i2.conjugate()); // s2=3*v2*conj(i2)
        return v2.multiply(Math.sqrt(3f));
    }

    private Complex computeU2(Complex y, Complex z, double rho) {
        return computeU2(y, new Complex(0, 0), z, rho);
    }

    private Complex computeS2(Complex y1, Complex y2, Complex z, double rho) {
        Complex v1p = v1.multiply(rho); // v1p=v1*rho
        Complex i1p = i1.divide(rho); // i1p=i1/rho

        Complex i2p = i1p.subtract(y1.multiply(v1p)); // i2p=i1p-y1*v1p
        Complex v2 = v1p.subtract(z.multiply(i2p)); // v2p=v1p-z*i2
        Complex i2 = i2p.subtract(y2.multiply(v2)); // i2=i2p-y2*v2
        return v2.multiply(3).multiply(i2.conjugate()); // s2=3*v2*conj(i2)
    }

    private Complex computeS2(Complex y, Complex z, double rho) {
        return computeS2(y, new Complex(0, 0), z, rho);
    }

    public SV otherSide(double r, double x, double g, double b, double rho) {
        Complex z = new Complex(r, x); // z=r+jx
        Complex y = new Complex(g, b); // y=g+jb
        Complex u2 = computeU2(y, z, rho);
        Complex s2 = computeS2(y, z, rho);
        return new SV(-s2.getReal(), -s2.getImaginary(), u2.abs(), Math.toDegrees(u2.getArgument()));
    }

    public SV otherSide(double r, double x, double g1, double b1, double g2, double b2, double rho) {
        Complex z = new Complex(r, x); // z=r+jx
        Complex y1 = new Complex(g1, b1); // y1=g1+jb1
        Complex y2 = new Complex(g2, b2); // y2=g2+jb2
        Complex u2 = computeU2(y1, y2, z, rho);
        Complex s2 = computeS2(y1, y2, z, rho);
        return new SV(-s2.getReal(), -s2.getImaginary(), u2.abs(), Math.toDegrees(u2.getArgument()));
    }

    public SV otherSide(TwoWindingsTransformer twt) {
        return otherSide(getR(twt), getX(twt), getG(twt), getB(twt), getRho(twt));
    }

    public SV otherSide(Line l) {
        return otherSide(l.getR(), l.getX(), l.getG1() + l.getG2(), l.getB1() + l.getB2(), 1);
    }

    public SV otherSideY1Y2(Line l) {
        return otherSide(l.getR(), l.getX(), l.getG1(), l.getB1(), l.getG2(), l.getB2(), 1);
    }

    public SV otherSide(DanglingLine dl) {
        return otherSide(dl.getR(), dl.getX(), dl.getG() / 2.0, dl.getB() / 2.0, dl.getG() / 2.0, dl.getB() / 2.0, 1);
    }

    public double otherSideP(double r, double x, double g1, double b1, double g2, double b2, double rho) {
        Complex z = new Complex(r, x); // z=r+jx
        Complex y1 = new Complex(g1, b1); // y1=g1+jb1
        Complex y2 = new Complex(g2, b2); // y2=g2+jb2
        Complex s2 = computeS2(y1, y2, z, rho);
        return -s2.getReal();
    }

    public double otherSideP(DanglingLine dl) {
        return otherSideP(dl.getR(), dl.getX(), dl.getG() / 2.0, dl.getB() / 2.0, dl.getG() / 2.0, dl.getB() / 2.0, 1);
    }

    public double otherSideP(TieLine.HalfLine hl) {
        return otherSideP(hl.getR(), hl.getX(), hl.getG1(), hl.getB1(), hl.getG2(), hl.getB2(), 1.0);
    }

    public double otherSideQ(double r, double x, double g1, double b1, double g2, double b2, double rho) {
        Complex z = new Complex(r, x); // z=r+jx
        Complex y1 = new Complex(g1, b1); // y1=g1+jb1
        Complex y2 = new Complex(g2, b2); // y2=g2+jb2
        Complex s2 = computeS2(y1, y2, z, rho);
        return -s2.getImaginary();
    }

    public double otherSideQ(TieLine.HalfLine hl) {
        return otherSideQ(hl.getR(), hl.getX(), hl.getG1(), hl.getB1(), hl.getG2(), hl.getB2(), 1.0);
    }

    public double otherSideQ(DanglingLine dl) {
        return otherSideQ(dl.getR(), dl.getX(), dl.getG() / 2.0, dl.getB() / 2.0, dl.getG() / 2.0, dl.getB() / 2.0, 1);
    }

    public double otherSideU(double r, double x, double g1, double b1, double rho) {
        Complex z = new Complex(r, x); // z=r+jx
        Complex y1 = new Complex(g1, b1); // y1=g1+jb1
        Complex u2 = computeU2(y1, new Complex(0, 0), z, rho);
        return u2.abs();
    }

    public double otherSideU(TieLine.HalfLine hl) {
        return otherSideU(hl.getR(), hl.getX(), hl.getG1(), hl.getB1(), 1.0);
    }

    public double otherSideU(DanglingLine dl) {
        return otherSideU(dl.getR(), dl.getX(), dl.getG() / 2.0, dl.getB() / 2.0, 1);
    }

    public double otherSideA(double r, double x, double g1, double b1, double rho) {
        Complex z = new Complex(r, x); // z=r+jx
        Complex y1 = new Complex(g1, b1); // y1=g1+jb1
        Complex u2 = computeU2(y1, new Complex(0, 0), z, rho);
        return Math.toDegrees(u2.getArgument());
    }

    public double otherSideA(TieLine.HalfLine hl) {
        return otherSideA(hl.getR(), hl.getX(), hl.getG1(), hl.getB1(), 1.0);
    }

    public double otherSideA(DanglingLine dl) {
        return otherSideA(dl.getR(), dl.getX(), dl.getG() / 2.0, dl.getB() / 2.0, 1.0);
    }

    @Override
    public String toString() {
        return "p=" + p + ", q=" + q + ", u=" + u + ", a=" + a;
    }
}
