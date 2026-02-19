/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class SVTest {

    @Test
    void testLine() {
        Line line = new LineTestData().getLine();

        double tol = 0.0001;
        double p1 = 485.306701;
        double q1 = 48.537745;
        double v1 = 138.0;
        double a1 = 0.0;

        double p2 = -104.996276;
        double q2 = -123.211145;
        double v2 = 137.5232696533203;
        double a2 = -0.18332427740097046;

        SV svA1 = new SV(p1, q1, v1, a1, TwoSides.ONE);
        SV svA2 = svA1.otherSide(line);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(q2, svA2.getQ(), tol);
        assertEquals(v2, svA2.getU(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, TwoSides.TWO);
        SV svB1 = svB2.otherSide(line);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(q1, svB1.getQ(), tol);
        assertEquals(v1, svB1.getU(), tol);
        assertEquals(a1, svB1.getA(), tol);
    }

    @Test
    void testDanglingLine() {
        DanglingLine dl = new DanglingLineTestData().getDanglingLine();

        double tol = 0.0001;
        double p1 = 126.818177;
        double q1 = -77.444122;
        double v1 = 118.13329315185547;
        double a1 = 0.19568365812301636;
        double i1 = 726.224579;

        double p2 = 15.098317;
        double q2 = 64.333028;
        double v2 = 138.0;
        double a2 = 0.0;
        double i2 = 276.462893;

        SV svA1 = new SV(p1, q1, v1, a1, TwoSides.ONE);
        SV svA2 = svA1.otherSide(dl);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(q2, svA2.getQ(), tol);
        assertEquals(v2, svA2.getU(), tol);
        assertEquals(a2, svA2.getA(), tol);
        assertEquals(i2, svA2.getI(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, TwoSides.TWO);
        SV svB1 = svB2.otherSide(dl);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(q1, svB1.getQ(), tol);
        assertEquals(v1, svB1.getU(), tol);
        assertEquals(a1, svB1.getA(), tol);
        assertEquals(i1, svB1.getI(), tol);

        assertEquals(p2, svA1.otherSideP(dl), tol);
        assertEquals(q2, svA1.otherSideQ(dl), tol);
        assertEquals(v2, svA1.otherSideU(dl), tol);
        assertEquals(a2, svA1.otherSideA(dl), tol);
        assertEquals(i2, svA1.otherSideI(dl), tol);

        assertEquals(p1, svB2.otherSideP(dl), tol);
        assertEquals(q1, svB2.otherSideQ(dl), tol);
        assertEquals(v1, svB2.otherSideU(dl), tol);
        assertEquals(a1, svB2.otherSideA(dl), tol);
        assertEquals(i1, svB2.otherSideI(dl), tol);

        assertEquals(p2, svA1.otherSideP(dl, false), tol);
        assertEquals(q2, svA1.otherSideQ(dl, false), tol);
        assertEquals(v2, svA1.otherSideU(dl, false), tol);
        assertEquals(a2, svA1.otherSideA(dl, false), tol);
        assertEquals(i2, svA1.otherSideI(dl, false), tol);

        assertEquals(p1, svB2.otherSideP(dl, false), tol);
        assertEquals(q1, svB2.otherSideQ(dl, false), tol);
        assertEquals(v1, svB2.otherSideU(dl, false), tol);
        assertEquals(a1, svB2.otherSideA(dl, false), tol);
        assertEquals(i1, svB2.otherSideI(dl, false), tol);
    }

    @Test
    void testTwoWindingsTransformer() {
        TwoWindingsTransformer twt = new TwoWindingsTransformerTestData().getTwoWindingsTransformer();

        double tol = 0.0001;
        double p1 = 220.644832;
        double q1 = 8.699260;
        double v1 = 197.66468811035156;
        double a1 = 19.98349380493164;

        double p2 = -219.092739;
        double q2 = 48.692085;
        double v2 = 118.13329315185547;
        double a2 = 0.19568365812301636;

        SV svA1 = new SV(p1, q1, v1, a1, TwoSides.ONE);
        SV svA2 = svA1.otherSide(twt);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(q2, svA2.getQ(), tol);
        assertEquals(v2, svA2.getU(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, TwoSides.TWO);
        SV svB1 = svB2.otherSide(twt);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(q1, svB1.getQ(), tol);
        assertEquals(v1, svB1.getU(), tol);
        assertEquals(a1, svB1.getA(), tol);

        SV svA2split = svA1.otherSide(twt, true);
        assertEquals(p2, svA2split.getP(), tol);
        assertEquals(q2, svA2split.getQ(), tol);
        assertEquals(v2, svA2split.getU(), tol);
        assertEquals(a2, svA2split.getA(), tol);

        SV svB1split = svB2.otherSide(twt, true);
        assertEquals(p1, svB1split.getP(), tol);
        assertEquals(q1, svB1split.getQ(), tol);
        assertEquals(v1, svB1split.getU(), tol);
        assertEquals(a1, svB1split.getA(), tol);
    }

    @Test
    void testTwoWindingsTransformerWithoutRtc() {
        TwoWindingsTransformer twt = new TwoWindingsTransformerWithoutRtcTestData().getTwoWindingsTransformer();

        double tol = 0.0001;
        double p1 = 220.644832;
        double q1 = 8.699260;
        double v1 = 197.66468811035156;
        double a1 = 19.98349380493164;

        double p2 = -219.092739;
        double q2 = 48.692085;
        double v2 = 118.13329315185547;
        double a2 = 0.19568365812301636;

        SV svA1 = new SV(p1, q1, v1, a1, TwoSides.ONE);
        SV svA2 = svA1.otherSide(twt);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(q2, svA2.getQ(), tol);
        assertEquals(v2, svA2.getU(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, TwoSides.TWO);
        SV svB1 = svB2.otherSide(twt);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(q1, svB1.getQ(), tol);
        assertEquals(v1, svB1.getU(), tol);
        assertEquals(a1, svB1.getA(), tol);
    }

    @Test
    void testTwoWindingsTransformerWithoutPtc() {
        TwoWindingsTransformer twt = new TwoWindingsTransformerWithoutPtcTestData().getTwoWindingsTransformer();

        double tol = 0.0001;
        double p1 = 220.644832;
        double q1 = 8.699260;
        double v1 = 197.66468811035156;
        double a1 = 19.98349380493164;

        double p2 = -219.092739124819760;
        double q2 = 48.692081198528110;
        double v2 = 118.133298648525750;
        double a2 = 5.195684102383955;

        SV svA1 = new SV(p1, q1, v1, a1, TwoSides.ONE);
        SV svA2 = svA1.otherSide(twt);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(q2, svA2.getQ(), tol);
        assertEquals(v2, svA2.getU(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, TwoSides.TWO);
        SV svB1 = svB2.otherSide(twt);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(q1, svB1.getQ(), tol);
        assertEquals(v1, svB1.getU(), tol);
        assertEquals(a1, svB1.getA(), tol);
    }

    private static final class LineTestData {
        private static double R = 0.15;
        private static double X = 0.25;
        private static double G1 = 0.01;
        private static double B1 = 0.0020;
        private static double G2 = 0.01;
        private static double B2 = 0.0020;
        private static double VN = 138.0;
        private static Line line;
        private static Terminal t1;
        private static Terminal t2;
        private static VoltageLevel vl1;
        private static VoltageLevel vl2;

        private LineTestData() {
            line = Mockito.mock(Line.class);
            Mockito.when(line.getR()).thenReturn(R);
            Mockito.when(line.getX()).thenReturn(X);
            Mockito.when(line.getG1()).thenReturn(G1);
            Mockito.when(line.getB1()).thenReturn(B1);
            Mockito.when(line.getG2()).thenReturn(G2);
            Mockito.when(line.getB2()).thenReturn(B2);

            t1 = Mockito.mock(Terminal.class);
            t2 = Mockito.mock(Terminal.class);
            vl1 = Mockito.mock(VoltageLevel.class);
            vl2 = Mockito.mock(VoltageLevel.class);

            Mockito.when(line.getTerminal1()).thenReturn(t1);
            Mockito.when(line.getTerminal2()).thenReturn(t2);
            Mockito.when(t1.getVoltageLevel()).thenReturn(vl1);
            Mockito.when(t2.getVoltageLevel()).thenReturn(vl2);
            Mockito.when(vl1.getNominalV()).thenReturn(VN);
            Mockito.when(vl2.getNominalV()).thenReturn(VN);
        }

        private Line getLine() {
            return line;
        }
    }

    private static final class DanglingLineTestData {
        private static double R = 10.30;
        private static double X = 40.20;
        private static double G = 0.01;
        private static double B = 0.0016;
        private static double VN = 138.0;
        private static DanglingLine danglinLine;

        private static Terminal t;
        private static VoltageLevel vl;

        private DanglingLineTestData() {
            danglinLine = Mockito.mock(DanglingLine.class);
            Mockito.when(danglinLine.getR()).thenReturn(R);
            Mockito.when(danglinLine.getX()).thenReturn(X);
            Mockito.when(danglinLine.getG()).thenReturn(G);
            Mockito.when(danglinLine.getB()).thenReturn(B);

            t = Mockito.mock(Terminal.class);
            vl = Mockito.mock(VoltageLevel.class);

            Mockito.when(danglinLine.getTerminal()).thenReturn(t);
            Mockito.when(t.getVoltageLevel()).thenReturn(vl);
            Mockito.when(vl.getNominalV()).thenReturn(VN);
        }

        private DanglingLine getDanglingLine() {
            return danglinLine;
        }
    }

    private static final class TwoWindingsTransformerTestData {
        private static double R = 0.43;
        private static double X = 15.90;
        private static double G = 0.0;
        private static double B = 0.0;
        private static double RTC_RHO = 1.0;
        private static double RTC_R = 0.0;
        private static double RTC_X = 0.0;
        private static double RTC_G = 0.0;
        private static double RTC_B = 0.0;
        private static double PTC_RHO = 0.98;
        private static double PTC_ALPHA = -5.0;
        private static double PTC_R = 0.0;
        private static double PTC_X = 0.0;
        private static double PTC_G = 0.0;
        private static double PTC_B = 0.0;
        private static double RATEDU1 = 230.0;
        private static double RATEDU2 = 138.0;
        private static double VN = 138.0;

        private static RatioTapChanger rtc;
        private static RatioTapChangerStep rtcStep;
        private static PhaseTapChanger ptc;
        private static PhaseTapChangerStep ptcStep;
        private static TwoWindingsTransformer twt;

        private static Terminal t2;
        private static VoltageLevel vl2;

        private TwoWindingsTransformerTestData() {
            twt = Mockito.mock(TwoWindingsTransformer.class);
            Mockito.when(twt.getR()).thenReturn(R);
            Mockito.when(twt.getX()).thenReturn(X);
            Mockito.when(twt.getG()).thenReturn(G);
            Mockito.when(twt.getB()).thenReturn(B);

            rtc = Mockito.mock(RatioTapChanger.class);
            rtcStep = Mockito.mock(RatioTapChangerStep.class);
            Mockito.when(twt.getRatioTapChanger()).thenReturn(rtc);
            Mockito.when(rtc.getCurrentStep()).thenReturn(rtcStep);
            Mockito.when(rtcStep.getRho()).thenReturn(RTC_RHO);
            Mockito.when(rtcStep.getR()).thenReturn(RTC_R);
            Mockito.when(rtcStep.getX()).thenReturn(RTC_X);
            Mockito.when(rtcStep.getG()).thenReturn(RTC_G);
            Mockito.when(rtcStep.getB()).thenReturn(RTC_B);

            ptc = Mockito.mock(PhaseTapChanger.class);
            ptcStep = Mockito.mock(PhaseTapChangerStep.class);
            Mockito.when(twt.getPhaseTapChanger()).thenReturn(ptc);
            Mockito.when(ptc.getCurrentStep()).thenReturn(ptcStep);
            Mockito.when(ptcStep.getRho()).thenReturn(PTC_RHO);
            Mockito.when(ptcStep.getAlpha()).thenReturn(PTC_ALPHA);
            Mockito.when(ptcStep.getR()).thenReturn(PTC_R);
            Mockito.when(ptcStep.getX()).thenReturn(PTC_X);
            Mockito.when(ptcStep.getG()).thenReturn(PTC_G);
            Mockito.when(ptcStep.getB()).thenReturn(PTC_B);

            Mockito.when(twt.getRatedU1()).thenReturn(RATEDU1);
            Mockito.when(twt.getRatedU2()).thenReturn(RATEDU2);

            t2 = Mockito.mock(Terminal.class);
            vl2 = Mockito.mock(VoltageLevel.class);

            Mockito.when(twt.getTerminal2()).thenReturn(t2);
            Mockito.when(t2.getVoltageLevel()).thenReturn(vl2);
            Mockito.when(vl2.getNominalV()).thenReturn(VN);
        }

        private TwoWindingsTransformer getTwoWindingsTransformer() {
            return twt;
        }
    }

    private static final class TwoWindingsTransformerWithoutRtcTestData {
        private static double R = 0.43;
        private static double X = 15.90;
        private static double G = 0.0;
        private static double B = 0.0;
        private static double PTC_RHO = 0.98;
        private static double PTC_ALPHA = -5.0;
        private static double PTC_R = 0.0;
        private static double PTC_X = 0.0;
        private static double PTC_G = 0.0;
        private static double PTC_B = 0.0;
        private static double RATEDU1 = 230.0;
        private static double RATEDU2 = 138.0;
        private static double VN = 138.0;

        private static PhaseTapChanger ptc;
        private static PhaseTapChangerStep ptcStep;
        private static TwoWindingsTransformer twt;

        private static Terminal t2;
        private static VoltageLevel vl2;

        private TwoWindingsTransformerWithoutRtcTestData() {
            twt = Mockito.mock(TwoWindingsTransformer.class);
            Mockito.when(twt.getR()).thenReturn(R);
            Mockito.when(twt.getX()).thenReturn(X);
            Mockito.when(twt.getG()).thenReturn(G);
            Mockito.when(twt.getB()).thenReturn(B);

            Mockito.when(twt.getRatioTapChanger()).thenReturn(null);

            ptc = Mockito.mock(PhaseTapChanger.class);
            ptcStep = Mockito.mock(PhaseTapChangerStep.class);
            Mockito.when(twt.getPhaseTapChanger()).thenReturn(ptc);
            Mockito.when(ptc.getCurrentStep()).thenReturn(ptcStep);
            Mockito.when(ptcStep.getRho()).thenReturn(PTC_RHO);
            Mockito.when(ptcStep.getAlpha()).thenReturn(PTC_ALPHA);
            Mockito.when(ptcStep.getR()).thenReturn(PTC_R);
            Mockito.when(ptcStep.getX()).thenReturn(PTC_X);
            Mockito.when(ptcStep.getG()).thenReturn(PTC_G);
            Mockito.when(ptcStep.getB()).thenReturn(PTC_B);

            Mockito.when(twt.getRatedU1()).thenReturn(RATEDU1);
            Mockito.when(twt.getRatedU2()).thenReturn(RATEDU2);

            t2 = Mockito.mock(Terminal.class);
            vl2 = Mockito.mock(VoltageLevel.class);

            Mockito.when(twt.getTerminal2()).thenReturn(t2);
            Mockito.when(t2.getVoltageLevel()).thenReturn(vl2);
            Mockito.when(vl2.getNominalV()).thenReturn(VN);
        }

        private TwoWindingsTransformer getTwoWindingsTransformer() {
            return twt;
        }
    }

    private static final class TwoWindingsTransformerWithoutPtcTestData {
        private static double R = 0.43;
        private static double X = 15.90;
        private static double G = 0.0;
        private static double B = 0.0;
        private static double RTC_RHO = 0.98;
        private static double RTC_R = 0.0;
        private static double RTC_X = 0.0;
        private static double RTC_G = 0.0;
        private static double RTC_B = 0.0;
        private static double RATEDU1 = 230.0;
        private static double RATEDU2 = 138.0;
        private static double VN = 138.0;

        private static RatioTapChanger rtc;
        private static RatioTapChangerStep rtcStep;
        private static TwoWindingsTransformer twt;

        private static Terminal t2;
        private static VoltageLevel vl2;

        private TwoWindingsTransformerWithoutPtcTestData() {
            twt = Mockito.mock(TwoWindingsTransformer.class);
            Mockito.when(twt.getR()).thenReturn(R);
            Mockito.when(twt.getX()).thenReturn(X);
            Mockito.when(twt.getG()).thenReturn(G);
            Mockito.when(twt.getB()).thenReturn(B);

            rtc = Mockito.mock(RatioTapChanger.class);
            rtcStep = Mockito.mock(RatioTapChangerStep.class);
            Mockito.when(twt.getRatioTapChanger()).thenReturn(rtc);
            Mockito.when(rtc.getCurrentStep()).thenReturn(rtcStep);
            Mockito.when(rtcStep.getRho()).thenReturn(RTC_RHO);
            Mockito.when(rtcStep.getR()).thenReturn(RTC_R);
            Mockito.when(rtcStep.getX()).thenReturn(RTC_X);
            Mockito.when(rtcStep.getG()).thenReturn(RTC_G);
            Mockito.when(rtcStep.getB()).thenReturn(RTC_B);

            Mockito.when(twt.getPhaseTapChanger()).thenReturn(null);

            Mockito.when(twt.getRatedU1()).thenReturn(RATEDU1);
            Mockito.when(twt.getRatedU2()).thenReturn(RATEDU2);

            t2 = Mockito.mock(Terminal.class);
            vl2 = Mockito.mock(VoltageLevel.class);

            Mockito.when(twt.getTerminal2()).thenReturn(t2);
            Mockito.when(t2.getVoltageLevel()).thenReturn(vl2);
            Mockito.when(vl2.getNominalV()).thenReturn(VN);
        }

        private TwoWindingsTransformer getTwoWindingsTransformer() {
            return twt;
        }
    }

    @Test
    void testDcLine() {
        Line line = new LineDcTestData().getLine();

        double tol = 0.0001;
        double p1 = 148.70937259543686;
        double q1 = Double.NaN;
        double v1 = Double.NaN;
        double a1 = 0.0;

        double p2 = -148.70937259543686;
        double q2 = Double.NaN;
        double v2 = Double.NaN;
        double a2 = -5.041532173036991;

        SV svA1 = new SV(p1, q1, v1, a1, TwoSides.ONE);
        SV svA2 = svA1.otherSide(line);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(a2, svA2.getA(), tol);
        assertEquals(p2, svA1.otherSide(line).getP(), tol);
        assertEquals(a2, svA1.otherSide(line).getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, TwoSides.TWO);
        SV svB1 = svB2.otherSide(line);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(a1, svB1.getA(), tol);
        assertEquals(p1, svB2.otherSide(line).getP(), tol);
        assertEquals(a1, svB2.otherSide(line).getA(), tol);
    }

    @Test
    void testDcTwoWindingsTransformer() {
        TwoWindingsTransformer twt = new TwoWindingsTransformerDcTestData().getTwoWindingsTransformer();

        double tol = 0.0001;
        double p1 = 1.4792780985886924;
        double q1 = Double.NaN;
        double v1 = Double.NaN;
        double a1 = -10.76932587556957;

        double p2 = -1.4792780985886924;
        double q2 = Double.NaN;
        double v2 = Double.NaN;
        double a2 = -11.226110634252219;

        SV svA1 = new SV(p1, q1, v1, a1, TwoSides.ONE);
        SV svA2 = svA1.otherSide(twt);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, TwoSides.TWO);
        SV svB1 = svB2.otherSide(twt);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(a1, svB1.getA(), tol);
    }

    @Test
    void testDcPhaseShifter() {
        TwoWindingsTransformer twt = new PhaseShifterDcTestData().getTwoWindingsTransformer();

        double tol = 0.0001;
        double p1 = 58.02489256054598;
        double q1 = Double.NaN;
        double v1 = Double.NaN;
        double a1 = -10.76932587556957;

        double p2 = -58.02489256054598;
        double q2 = Double.NaN;
        double v2 = Double.NaN;
        double a2 = -7.56873858064591;

        SV svA1 = new SV(p1, q1, v1, a1, TwoSides.ONE);
        SV svA2 = svA1.otherSide(twt);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, TwoSides.TWO);
        SV svB1 = svB2.otherSide(twt);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(a1, svB1.getA(), tol);
    }

    private static final class LineDcTestData {
        private static double R = 0.0;
        private static double X = 5.917E-4;
        private static double G1 = 0.01;
        private static double B1 = 0.0020;
        private static double G2 = 0.01;
        private static double B2 = 0.0020;
        private static double VN = 1.0;
        private static Line line;
        private static Terminal t1;
        private static Terminal t2;
        private static VoltageLevel vl1;
        private static VoltageLevel vl2;

        private LineDcTestData() {
            line = Mockito.mock(Line.class);
            Mockito.when(line.getR()).thenReturn(R);
            Mockito.when(line.getX()).thenReturn(X);
            Mockito.when(line.getG1()).thenReturn(G1);
            Mockito.when(line.getB1()).thenReturn(B1);
            Mockito.when(line.getG2()).thenReturn(G2);
            Mockito.when(line.getB2()).thenReturn(B2);

            t1 = Mockito.mock(Terminal.class);
            t2 = Mockito.mock(Terminal.class);
            vl1 = Mockito.mock(VoltageLevel.class);
            vl2 = Mockito.mock(VoltageLevel.class);

            Mockito.when(line.getTerminal1()).thenReturn(t1);
            Mockito.when(line.getTerminal2()).thenReturn(t2);
            Mockito.when(t1.getVoltageLevel()).thenReturn(vl1);
            Mockito.when(t2.getVoltageLevel()).thenReturn(vl2);
            Mockito.when(vl1.getNominalV()).thenReturn(VN);
            Mockito.when(vl2.getNominalV()).thenReturn(VN);
        }

        private Line getLine() {
            return line;
        }
    }

    private static final class TwoWindingsTransformerDcTestData {
        private static double R = 0.0043;
        private static double X = 0.0055618;
        private static double G = 0.0;
        private static double B = 0.0;
        private static double RTC_RHO = 1.0;
        private static double RATEDU1 = 0.969;
        private static double RATEDU2 = 1.0;
        private static double VN = 1.0;

        private static RatioTapChanger rtc;
        private static RatioTapChangerStep rtcStep;
        private static TwoWindingsTransformer twt;

        private static Terminal t2;
        private static VoltageLevel vl2;

        private TwoWindingsTransformerDcTestData() {
            twt = Mockito.mock(TwoWindingsTransformer.class);
            Mockito.when(twt.getR()).thenReturn(R);
            Mockito.when(twt.getX()).thenReturn(X);
            Mockito.when(twt.getG()).thenReturn(G);
            Mockito.when(twt.getB()).thenReturn(B);

            rtc = Mockito.mock(RatioTapChanger.class);
            rtcStep = Mockito.mock(RatioTapChangerStep.class);
            Mockito.when(twt.getRatioTapChanger()).thenReturn(rtc);
            Mockito.when(rtc.getCurrentStep()).thenReturn(rtcStep);
            Mockito.when(rtcStep.getRho()).thenReturn(RTC_RHO);

            Mockito.when(twt.getRatedU1()).thenReturn(RATEDU1);
            Mockito.when(twt.getRatedU2()).thenReturn(RATEDU2);

            t2 = Mockito.mock(Terminal.class);
            vl2 = Mockito.mock(VoltageLevel.class);

            Mockito.when(twt.getTerminal2()).thenReturn(t2);
            Mockito.when(t2.getVoltageLevel()).thenReturn(vl2);
            Mockito.when(vl2.getNominalV()).thenReturn(VN);
        }

        private TwoWindingsTransformer getTwoWindingsTransformer() {
            return twt;
        }
    }

    private static final class PhaseShifterDcTestData {
        private static double R = 0.00043;
        private static double X = 0.0020912;
        private static double G = 0.0;
        private static double B = 0.0;
        private static double PTC_RHO = 1.0;
        private static double PTC_ALPHA = 10.0;
        private static double RATEDU1 = 0.978;
        private static double RATEDU2 = 1.0;
        private static double VN = 1.0;

        private static PhaseTapChanger ptc;
        private static PhaseTapChangerStep ptcStep;
        private static TwoWindingsTransformer twt;

        private static Terminal t2;
        private static VoltageLevel vl2;

        private PhaseShifterDcTestData() {
            twt = Mockito.mock(TwoWindingsTransformer.class);
            Mockito.when(twt.getR()).thenReturn(R);
            Mockito.when(twt.getX()).thenReturn(X);
            Mockito.when(twt.getG()).thenReturn(G);
            Mockito.when(twt.getB()).thenReturn(B);

            ptc = Mockito.mock(PhaseTapChanger.class);
            ptcStep = Mockito.mock(PhaseTapChangerStep.class);
            Mockito.when(twt.getPhaseTapChanger()).thenReturn(ptc);
            Mockito.when(ptc.getCurrentStep()).thenReturn(ptcStep);
            Mockito.when(ptcStep.getRho()).thenReturn(PTC_RHO);
            Mockito.when(ptcStep.getAlpha()).thenReturn(PTC_ALPHA);

            Mockito.when(twt.getRatedU1()).thenReturn(RATEDU1);
            Mockito.when(twt.getRatedU2()).thenReturn(RATEDU2);

            t2 = Mockito.mock(Terminal.class);
            vl2 = Mockito.mock(VoltageLevel.class);

            Mockito.when(twt.getTerminal2()).thenReturn(t2);
            Mockito.when(t2.getVoltageLevel()).thenReturn(vl2);
            Mockito.when(vl2.getNominalV()).thenReturn(VN);
        }

        private TwoWindingsTransformer getTwoWindingsTransformer() {
            return twt;
        }
    }
}
