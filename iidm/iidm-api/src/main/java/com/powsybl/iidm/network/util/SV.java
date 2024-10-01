/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

/**
 * Utility class to compute the state variables on one side of a branch, knowing
 * the state variables on the other side.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class SV {

    /**
     * In this class, lines, two windings transformers and dangling lines can be considered as equivalent branches.
     * <p><div>
     * <object data="doc-files/SV.svg" type="image/svg+xml">
     * </object> </div>
     * For dangling lines, side ONE is always on network's side and side TWO is always on boundary's side. <br>
     * @param p active power flow on the side of the branch we consider.
     * @param q reactive power flow on the side of the branch we consider
     * @param u voltage on the side of the branch we consider.
     * @param a phase on the side of the branch we consider.
     * @param side the side of the branch we consider.
     */
    public SV(double p, double q, double u, double a, TwoSides side) {
        this.p = p;
        this.q = q;
        this.u = u;
        this.a = a;
        this.side = side;
    }

    private final double p;

    private final double q;

    private final double u;

    private final double a;

    private final TwoSides side;

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

    public TwoSides getSide() {
        return side;
    }

    public SV otherSide(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        return otherSide(r, x, g1, b1, g2, b2, rho, alpha, Double.NaN);
    }

    public SV otherSide(Line l) {
        double zb = l.getTerminal1().getVoltageLevel().getNominalV() * l.getTerminal2().getVoltageLevel().getNominalV();
        return otherSide(l.getR(), l.getX(), l.getG1(), l.getB1(), l.getG2(), l.getB2(), 1.0, 0.0, zb);
    }

    public SV otherSide(TieLine l) {
        double zb = l.getDanglingLine1().getTerminal().getVoltageLevel().getNominalV() * l.getDanglingLine2().getTerminal().getVoltageLevel().getNominalV();
        return otherSide(l.getR(), l.getX(), l.getG1(), l.getB1(), l.getG2(), l.getB2(), 1.0, 0.0, zb);
    }

    public SV otherSide(TwoWindingsTransformer twt) {
        double zbase = twt.getTerminal2().getVoltageLevel().getNominalV() * twt.getTerminal2().getVoltageLevel().getNominalV();
        return otherSide(getR(twt), getX(twt), getG(twt), getB(twt), 0.0, 0.0, getRho(twt), getAlpha(twt), zbase);
    }

    public SV otherSide(TwoWindingsTransformer twt, boolean splitShuntAdmittance) {
        if (splitShuntAdmittance) {
            double zb = twt.getTerminal2().getVoltageLevel().getNominalV() * twt.getTerminal2().getVoltageLevel().getNominalV();
            return otherSide(getR(twt), getX(twt), getG(twt) * 0.5, getB(twt) * 0.5, getG(twt) * 0.5, getB(twt) * 0.5, getRho(twt), getAlpha(twt), zb);
        } else {
            return otherSide(twt);
        }
    }

    public SV otherSide(DanglingLine dl) {
        double zb = dl.getTerminal().getVoltageLevel().getNominalV() * dl.getTerminal().getVoltageLevel().getNominalV();
        if (dl.hasShuntAdmittanceLineEquivalentModel()) {
            return otherSide(dl.getR(), dl.getX(), dl.getG() * 0.5, dl.getB() * 0.5, dl.getG() * 0.5, dl.getB() * 0.5, 1.0, 0.0, zb);
        } else {
            return otherSide(dl.getR(), dl.getX(), dl.getG(), dl.getB(), 0.0, 0.0, 1.0, 0.0, zb);
        }
    }

    public double otherSideP(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        return otherSide(r, x, g1, b1, g2, b2, rho, alpha, Double.NaN).getP();
    }

    public double otherSideP(DanglingLine dl) {
        return otherSide(dl).getP();
    }

    public double otherSideQ(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        return otherSide(r, x, g1, b1, g2, b2, rho, alpha, Double.NaN).getQ();
    }

    public double otherSideQ(DanglingLine dl) {
        return otherSide(dl).getQ();
    }

    public double otherSideU(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        return otherSide(r, x, g1, b1, g2, b2, rho, alpha, Double.NaN).getU();
    }

    public double otherSideU(DanglingLine dl) {
        return otherSide(dl).getU();
    }

    public double otherSideA(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        return otherSide(r, x, g1, b1, g2, b2, rho, alpha, Double.NaN).getA();
    }

    public double otherSideA(DanglingLine dl) {
        return otherSide(dl).getA();
    }

    private static double getRho(TwoWindingsTransformer twt) {
        double rho = twt.getRatedU2() / twt.getRatedU1();
        if (twt.getRatioTapChanger() != null) {
            rho = rho * twt.getRatioTapChanger().getCurrentStep().getRho();
        }
        if (twt.getPhaseTapChanger() != null) {
            rho = rho * twt.getPhaseTapChanger().getCurrentStep().getRho();
        }
        return rho;
    }

    private static double getAlpha(TwoWindingsTransformer twt) {
        double alpha = 0.0;
        if (twt.getPhaseTapChanger() != null) {
            alpha = twt.getPhaseTapChanger().getCurrentStep().getAlpha();
        }
        return Math.toRadians(alpha);
    }

    private static double getR(TwoWindingsTransformer twt) {
        double r = twt.getR();
        if (twt.getRatioTapChanger() != null) {
            r = r * (1 + twt.getRatioTapChanger().getCurrentStep().getR() / 100);
        }
        if (twt.getPhaseTapChanger() != null) {
            r = r * (1 + twt.getPhaseTapChanger().getCurrentStep().getR() / 100);
        }
        return r;
    }

    private static double getX(TwoWindingsTransformer twt) {
        double x = twt.getX();
        if (twt.getRatioTapChanger() != null) {
            x = x * (1 + twt.getRatioTapChanger().getCurrentStep().getX() / 100);
        }
        if (twt.getPhaseTapChanger() != null) {
            x = x * (1 + twt.getPhaseTapChanger().getCurrentStep().getX() / 100);
        }
        return x;
    }

    private static double getG(TwoWindingsTransformer twt) {
        double g = twt.getG();
        if (twt.getRatioTapChanger() != null) {
            g = g * (1 + twt.getRatioTapChanger().getCurrentStep().getG() / 100);
        }
        if (twt.getPhaseTapChanger() != null) {
            g = g * (1 + twt.getPhaseTapChanger().getCurrentStep().getG() / 100);
        }
        return g;
    }

    private static double getB(TwoWindingsTransformer twt) {
        double b = twt.getG();
        if (twt.getRatioTapChanger() != null) {
            b = b * (1 + twt.getRatioTapChanger().getCurrentStep().getB() / 100);
        }
        if (twt.getPhaseTapChanger() != null) {
            b = b * (1 + twt.getPhaseTapChanger().getCurrentStep().getB() / 100);
        }
        return b;
    }

    @Override
    public String toString() {
        return "p=" + p + ", q=" + q + ", u=" + u + ", a=" + a + ", end=" + side;
    }

    private boolean isAllDataForCalculatingOtherSide() {
        return !(Double.isNaN(p) || Double.isNaN(q) || Double.isNaN(u) || Double.isNaN(a));
    }

    private boolean isAllDataForCalculatingOtherSideDcApproximation(double zbase) {
        return !(Double.isNaN(p) || Double.isNaN(a) || Double.isNaN(zbase));
    }

    private SV otherSide(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha, double zb) {
        if (isAllDataForCalculatingOtherSide()) {
            LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1 / rho, -alpha, 1.0, 0.0,
                new Complex(g1, b1), new Complex(g2, b2));
            return otherSide(adm);
        } else if (isAllDataForCalculatingOtherSideDcApproximation(zb)) {
            return otherSideDcApproximation(x, 1 / rho, -alpha, zb, true); // we always consider useRatio true
        } else {
            TwoSides otherSide = (side == TwoSides.ONE) ? TwoSides.TWO : TwoSides.ONE;
            return new SV(Double.NaN, Double.NaN, Double.NaN, Double.NaN, otherSide);
        }
    }

    private SV otherSide(LinkData.BranchAdmittanceMatrix adm) {
        Complex v;
        Complex s;
        TwoSides otherSide;
        if (side == TwoSides.ONE) {
            Complex v1 = ComplexUtils.polar2Complex(u, Math.toRadians(a));
            Complex s1 = new Complex(p, q);
            v = voltageAtEnd2(adm, v1, s1);
            s = flowAtEnd2(adm, v1, v);
            otherSide = TwoSides.TWO;
        } else {
            Complex v2 = ComplexUtils.polar2Complex(u, Math.toRadians(a));
            Complex s2 = new Complex(p, q);
            v = voltageAtEnd1(adm, v2, s2);
            s = flowAtEnd1(adm, v, v2);
            otherSide = TwoSides.ONE;
        }
        return new SV(s.getReal(), s.getImaginary(), v.abs(), Math.toDegrees(v.getArgument()), otherSide);
    }

    private SV otherSideDcApproximation(double x, double ratio, double angle, double zb, boolean useRatio) {
        double pOtherSide = -p;
        double xpu = x / zb;
        double b = useRatio ? 1 / (xpu * ratio) : 1 / xpu;
        double aOtherSide;
        TwoSides otherSide;
        if (side == TwoSides.ONE) {
            aOtherSide = Math.toDegrees(Math.toRadians(a) - angle - p / b);
            otherSide = TwoSides.TWO;
        } else {
            aOtherSide = Math.toDegrees(Math.toRadians(a) + angle - p / b);
            otherSide = TwoSides.ONE;
        }
        return new SV(pOtherSide, Double.NaN, Double.NaN, aOtherSide, otherSide);
    }

    // Get V2 from Y11.V1 + Y12.V2 = S1* / V1*
    private static Complex voltageAtEnd2(LinkData.BranchAdmittanceMatrix adm, Complex vEnd1, Complex sEnd1) {
        return sEnd1.conjugate().divide(vEnd1.conjugate()).subtract(adm.y11().multiply(vEnd1)).divide(adm.y12());
    }

    // Get V1 from Y21.V1 + Y22.V2 = S2* / V2*
    private static Complex voltageAtEnd1(LinkData.BranchAdmittanceMatrix adm, Complex vEnd2, Complex sEnd2) {
        return sEnd2.conjugate().divide(vEnd2.conjugate()).subtract(adm.y22().multiply(vEnd2)).divide(adm.y21());
    }

    // Get S1 from Y11.V1 + Y12.V2 = S1* / V1*
    private static Complex flowAtEnd1(LinkData.BranchAdmittanceMatrix adm, Complex vEnd1, Complex vEnd2) {
        return adm.y11().multiply(vEnd1).add(adm.y12().multiply(vEnd2)).multiply(vEnd1.conjugate()).conjugate();
    }

    // Get S2 from Y21.V1 + Y22.V2 = S2* / V2*
    private static Complex flowAtEnd2(LinkData.BranchAdmittanceMatrix adm, Complex vEnd1, Complex vEnd2) {
        return adm.y21().multiply(vEnd1).add(adm.y22().multiply(vEnd2)).multiply(vEnd2.conjugate()).conjugate();
    }
}
