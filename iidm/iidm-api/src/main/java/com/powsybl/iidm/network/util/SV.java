/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
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
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class SV {

    /**
     * In this class, lines, two windings transformers, half line and dangling lines can be considered as equivalent branches.
     * <p><div>
     * <object data="doc-files/SV.svg" type="image/svg+xml">
     * </object> </div>
     * For dangling lines, side ONE is always on network's side and side TWO is always on boundary's side. <br>
     * For half lines, if the half line is on the side ONE of its tie line, side ONE is on network's side and side TWO is on boundary's side; <br>
     * if the half line is on the side TWO of its tie line, side ONE is on boundary's side and side TWO is on network's side.
     * @param p active power flow on the side of the branch we consider.
     * @param q reactive power flow on the side of the branch we consider
     * @param u voltage on the side of the branch we consider.
     * @param a phase on the side of the branch we consider.
     * @param side the side of the branch we consider.
     */
    public SV(double p, double q, double u, double a, Branch.Side side) {
        this.p = p;
        this.q = q;
        this.u = u;
        this.a = a;
        this.side = side;
    }

    /**
     * @deprecated Not used anymore. The end associated to the voltage and flow must be defined. Use {@link #SV(double, double, double, double, Branch.Side)} instead.
     */
    @Deprecated(since = "4.3.0")
    public SV(double p, double q, double u, double a) {
        throw new PowsyblException("Deprecated. Not used anymore");
    }

    private final double p;

    private final double q;

    private final double u;

    private final double a;

    private final Branch.Side side;

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

    public Branch.Side getSide() {
        return side;
    }

    /**
     * @deprecated Not used anymore. This version with a simplified view of the parameters
     * of a generic branch has been deprecated to avoid misuse. Use the version that includes rho AND alpha.
     * {@link SV#otherSide(double, double, double, double, double, double, double, double)}
     */
    @Deprecated(since = "4.3.0")
    public SV otherSide(double r, double x, double g, double b, double rho) {
        return otherSide(r, x, g / 2, b / 2, g / 2, b / 2, rho, 0.0);
    }

    /**
     * @deprecated Not used anymore. This version with a simplified view of the parameters
     * of a generic branch has been deprecated to avoid misuse. Use the version that includes rho AND alpha.
     * {@link SV#otherSide(double, double, double, double, double, double, double, double)}
     */
    @Deprecated(since = "4.3.0")
    public SV otherSide(double r, double x, double g1, double b1, double g2, double b2, double rho) {
        return otherSide(r, x, g1, b1, g2, b2, rho, 0.0);
    }

    public SV otherSide(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1 / rho, -alpha, 1.0, 0.0,
            new Complex(g1, b1), new Complex(g2, b2));
        return otherSide(adm);
    }

    public SV otherSide(Line l) {
        return otherSide(l.getR(), l.getX(), l.getG1(), l.getB1(), l.getG2(), l.getB2(), 1.0, 0.0);
    }

    /**
     * @deprecated Should not be used anymore. Use {@link SV#otherSide(Line)} instead.
     */
    @Deprecated(since = "4.3.0")
    public SV otherSideY1Y2(Line l) {
        return otherSide(l.getR(), l.getX(), l.getG1(), l.getB1(), l.getG2(), l.getB2(), 1, 0.0);
    }

    public SV otherSide(TwoWindingsTransformer twt) {
        return otherSide(getR(twt), getX(twt), getG(twt), getB(twt), 0.0, 0.0, getRho(twt), getAlpha(twt));
    }

    public SV otherSide(TwoWindingsTransformer twt, boolean splitShuntAdmittance) {
        if (splitShuntAdmittance) {
            return otherSide(getR(twt), getX(twt), getG(twt) * 0.5, getB(twt) * 0.5, getG(twt) * 0.5, getB(twt) * 0.5, getRho(twt), getAlpha(twt));
        } else {
            return otherSide(twt);
        }
    }

    public SV otherSide(DanglingLine dl) {
        return otherSide(dl.getR(), dl.getX(), dl.getG(), dl.getB(), 0.0, 0.0, 1.0, 0.0);
    }

    public SV otherSide(DanglingLine dl, boolean splitShuntAdmittance) {
        if (splitShuntAdmittance) {
            return otherSide(dl.getR(), dl.getX(), dl.getG() * 0.5, dl.getB() * 0.5, dl.getG() * 0.5, dl.getB() * 0.5, 1.0, 0.0);
        } else {
            return otherSide(dl);
        }
    }

    /**
     * @deprecated Not used anymore. This version with a simplified view of the parameters
     * of a generic branch has been deprecated to avoid misuse. Use the version that includes rho AND alpha.
     * {@link SV#otherSideP(double, double, double, double, double, double, double, double)}
     */
    @Deprecated(since = "4.3.0")
    public double otherSideP(double r, double x, double g1, double b1, double g2, double b2, double rho) {
        return otherSideP(r, x, g1, b1, g2, b2, rho, 0.0);
    }

    public double otherSideP(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1 / rho, -alpha, 1.0, 0.0,
            new Complex(g1, b1), new Complex(g2, b2));
        return otherSideP(adm);
    }

    public double otherSideP(DanglingLine dl) {
        return otherSideP(dl.getR(), dl.getX(), dl.getG(), dl.getB(), 0.0, 0.0, 1.0, 0.0);
    }

    public double otherSideP(DanglingLine dl, boolean splitShuntAdmittance) {
        if (splitShuntAdmittance) {
            return otherSideP(dl.getR(), dl.getX(), dl.getG() * 0.5, dl.getB() * 0.5, dl.getG() * 0.5, dl.getB() * 0.5, 1.0, 0.0);
        } else {
            return otherSideP(dl);
        }
    }

    public double otherSideP(TieLine.HalfLine hl) {
        return otherSideP(hl.getR(), hl.getX(), hl.getG1(), hl.getB1(), hl.getG2(), hl.getB2(), 1.0, 0.0);
    }

    /**
     * @deprecated Not used anymore. This version with a simplified view of the parameters
     * of a generic branch has been deprecated to avoid misuse. Use the version that includes rho AND alpha.
     * {@link SV#otherSideQ(double, double, double, double, double, double, double, double)}
     */
    @Deprecated(since = "4.3.0")
    public double otherSideQ(double r, double x, double g1, double b1, double g2, double b2, double rho) {
        return otherSideQ(r, x, g1, b1, g2, b2, rho, 0.0);
    }

    public double otherSideQ(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1 / rho, -alpha, 1.0, 0.0,
            new Complex(g1, b1), new Complex(g2, b2));
        return otherSideQ(adm);
    }

    public double otherSideQ(DanglingLine dl) {
        return otherSideQ(dl.getR(), dl.getX(), dl.getG(), dl.getB(), 0.0, 0.0, 1.0, 0.0);
    }

    public double otherSideQ(DanglingLine dl, boolean splitShuntAdmittance) {
        if (splitShuntAdmittance) {
            return otherSideQ(dl.getR(), dl.getX(), dl.getG() * 0.5, dl.getB() * 0.5, dl.getG() * 0.5, dl.getB() * 0.5, 1.0, 0.0);
        } else {
            return otherSideQ(dl);
        }
    }

    public double otherSideQ(TieLine.HalfLine hl) {
        return otherSideQ(hl.getR(), hl.getX(), hl.getG1(), hl.getB1(), hl.getG2(), hl.getB2(), 1.0, 0.0);
    }

    /**
     * @deprecated Not used anymore. This version with a simplified view of the parameters
     * of a generic branch has been deprecated to avoid misuse. Use the version that includes rho AND alpha.
     * {@link SV#otherSideU(double, double, double, double, double, double, double, double)}
     */
    @Deprecated(since = "4.3.0")
    public double otherSideU(double r, double x, double g1, double b1, double rho) {
        return otherSideU(r, x, g1, b1, 0.0, 0.0, rho, 0.0);
    }

    public double otherSideU(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1 / rho, -alpha, 1.0, 0.0,
            new Complex(g1, b1), new Complex(g2, b2));
        return otherSideU(adm);
    }

    public double otherSideU(DanglingLine dl) {
        return otherSideU(dl.getR(), dl.getX(), dl.getG(), dl.getB(), 0.0, 0.0, 1.0, 0.0);
    }

    public double otherSideU(DanglingLine dl, boolean splitShuntAdmittance) {
        if (splitShuntAdmittance) {
            return otherSideU(dl.getR(), dl.getX(), dl.getG() * 0.5, dl.getB() * 0.5, dl.getG() * 0.5, dl.getB() * 0.5, 1.0, 0.0);
        } else {
            return otherSideU(dl);
        }
    }

    public double otherSideU(TieLine.HalfLine hl) {
        return otherSideU(hl.getR(), hl.getX(), hl.getG1(), hl.getB1(), hl.getG2(), hl.getB2(), 1.0, 0.0);
    }

    /**
     * @deprecated Not used anymore. This version with a simplified view of the parameters
     * of a generic branch has been deprecated to avoid misuse. Use the version that includes rho AND alpha.
     * {@link SV#otherSideA(double, double, double, double, double, double, double, double)}
     */
    @Deprecated(since = "4.3.0")
    public double otherSideA(double r, double x, double g1, double b1, double rho) {
        return otherSideA(r, x, g1, b1, 0.0, 0.0, rho, 0.0);
    }

    public double otherSideA(double r, double x, double g1, double b1, double g2, double b2, double rho, double alpha) {
        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1 / rho, -alpha, 1.0, 0.0,
            new Complex(g1, b1), new Complex(g2, b2));
        return otherSideA(adm);
    }

    public double otherSideA(DanglingLine dl) {
        return otherSideA(dl.getR(), dl.getX(), dl.getG(), dl.getB(), 0.0, 0.0, 1.0, 0.0);
    }

    public double otherSideA(DanglingLine dl, boolean splitShuntAdmittance) {
        if (splitShuntAdmittance) {
            return otherSideA(dl.getR(), dl.getX(), dl.getG() * 0.5, dl.getB() * 0.5, dl.getG() * 0.5, dl.getB() * 0.5, 1.0, 0.0);
        } else {
            return otherSideA(dl);
        }
    }

    public double otherSideA(TieLine.HalfLine hl) {
        return otherSideA(hl.getR(), hl.getX(), hl.getG1(), hl.getB1(), hl.getG2(), hl.getB2(), 1.0, 0.0);
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

    private SV otherSide(LinkData.BranchAdmittanceMatrix adm) {
        Complex v;
        Complex s;
        Branch.Side otherSide;
        if (side == Branch.Side.ONE) {
            Complex v1 = ComplexUtils.polar2Complex(u, Math.toRadians(a));
            Complex s1 = new Complex(p, q);
            v = voltageAtEnd2(adm, v1, s1);
            s = flowAtEnd2(adm, v1, v);
            otherSide = Branch.Side.TWO;
        } else {
            Complex v2 = ComplexUtils.polar2Complex(u, Math.toRadians(a));
            Complex s2 = new Complex(p, q);
            v = voltageAtEnd1(adm, v2, s2);
            s = flowAtEnd1(adm, v, v2);
            otherSide = Branch.Side.ONE;
        }
        return new SV(s.getReal(), s.getImaginary(), v.abs(), Math.toDegrees(v.getArgument()), otherSide);
    }

    private double otherSideP(LinkData.BranchAdmittanceMatrix adm) {
        return otherSide(adm).getP();
    }

    private double otherSideQ(LinkData.BranchAdmittanceMatrix adm) {
        return otherSide(adm).getQ();
    }

    private Complex otherSideV(LinkData.BranchAdmittanceMatrix adm) {
        Complex v;
        if (side == Branch.Side.ONE) {
            Complex v1 = ComplexUtils.polar2Complex(u, Math.toRadians(a));
            Complex s1 = new Complex(p, q);
            v = voltageAtEnd2(adm, v1, s1);
        } else {
            Complex v2 = ComplexUtils.polar2Complex(u, Math.toRadians(a));
            Complex s2 = new Complex(p, q);
            v = voltageAtEnd1(adm, v2, s2);
        }
        return v;
    }

    private double otherSideU(LinkData.BranchAdmittanceMatrix adm) {
        return otherSideV(adm).abs();
    }

    private double otherSideA(LinkData.BranchAdmittanceMatrix adm) {
        return Math.toDegrees(otherSideV(adm).getArgument());
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
