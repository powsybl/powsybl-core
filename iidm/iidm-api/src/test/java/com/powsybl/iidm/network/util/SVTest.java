/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerStep;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class SVTest {

    @Test
    public void testLine() {
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

        SV svA1 = new SV(p1, q1, v1, a1, 1);
        SV svA2 = svA1.otherSide(line);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(q2, svA2.getQ(), tol);
        assertEquals(v2, svA2.getU(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, 2);
        SV svB1 = svB2.otherSide(line);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(q1, svB1.getQ(), tol);
        assertEquals(v1, svB1.getU(), tol);
        assertEquals(a1, svB1.getA(), tol);
    }

    @Test
    public void testDanglingLine() {
        DanglingLine dl = new DanglingLineTestData().getDanglingLine();

        double tol = 0.0001;
        double p1 = 126.818177;
        double q1 = -77.444122;
        double v1 = 118.13329315185547;
        double a1 = 0.19568365812301636;

        double p2 = 15.098317;
        double q2 = 64.333028;
        double v2 = 138.0;
        double a2 = 0.0;

        SV svA1 = new SV(p1, q1, v1, a1, 1);
        SV svA2 = svA1.otherSide(dl);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(q2, svA2.getQ(), tol);
        assertEquals(v2, svA2.getU(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, 2);
        SV svB1 = svB2.otherSide(dl);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(q1, svB1.getQ(), tol);
        assertEquals(v1, svB1.getU(), tol);
        assertEquals(a1, svB1.getA(), tol);

        assertEquals(p2, svA1.otherSideP(dl), tol);
        assertEquals(q2, svA1.otherSideQ(dl), tol);
        assertEquals(v2, svA1.otherSideU(dl), tol);
        assertEquals(a2, svA1.otherSideA(dl), tol);

        assertEquals(p1, svB2.otherSideP(dl), tol);
        assertEquals(q1, svB2.otherSideQ(dl), tol);
        assertEquals(v1, svB2.otherSideU(dl), tol);
        assertEquals(a1, svB2.otherSideA(dl), tol);
    }

    @Test
    public void testTwoWindingsTransformer() {
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

        SV svA1 = new SV(p1, q1, v1, a1, 1);
        SV svA2 = svA1.otherSide(twt);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(q2, svA2.getQ(), tol);
        assertEquals(v2, svA2.getU(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, 2);
        SV svB1 = svB2.otherSide(twt);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(q1, svB1.getQ(), tol);
        assertEquals(v1, svB1.getU(), tol);
        assertEquals(a1, svB1.getA(), tol);
    }

    @Test
    public void testTwoWindingsTransformerWithoutRtc() {
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

        SV svA1 = new SV(p1, q1, v1, a1, 1);
        SV svA2 = svA1.otherSide(twt);
        assertEquals(p2, svA2.getP(), tol);
        assertEquals(q2, svA2.getQ(), tol);
        assertEquals(v2, svA2.getU(), tol);
        assertEquals(a2, svA2.getA(), tol);

        SV svB2 = new SV(p2, q2, v2, a2, 2);
        SV svB1 = svB2.otherSide(twt);
        assertEquals(p1, svB1.getP(), tol);
        assertEquals(q1, svB1.getQ(), tol);
        assertEquals(v1, svB1.getU(), tol);
        assertEquals(a1, svB1.getA(), tol);
    }

    @Test
    public void testHalfLine() {
        TieLine.HalfLine halfLine = new HalfLineTestData().getHalfLine();

        double tol = 0.0001;
        double p1 = 485.306701;
        double q1 = 48.537745;
        double v1 = 138.0;
        double a1 = 0.0;

        double p2 = -104.996276;
        double q2 = -123.211145;
        double v2 = 137.5232696533203;
        double a2 = -0.18332427740097046;

        SV svA1 = new SV(p1, q1, v1, a1, 1);
        SV svB2 = new SV(p2, q2, v2, a2, 2);

        assertEquals(p2, svA1.otherSideP(halfLine), tol);
        assertEquals(q2, svA1.otherSideQ(halfLine), tol);
        assertEquals(v2, svA1.otherSideU(halfLine), tol);
        assertEquals(a2, svA1.otherSideA(halfLine), tol);

        assertEquals(p1, svB2.otherSideP(halfLine), tol);
        assertEquals(q1, svB2.otherSideQ(halfLine), tol);
        assertEquals(v1, svB2.otherSideU(halfLine), tol);
        assertEquals(a1, svB2.otherSideA(halfLine), tol);
    }

    private static final class LineTestData {
        private static double R = 0.15;
        private static double X = 0.25;
        private static double G1 = 0.01;
        private static double B1 = 0.0020;
        private static double G2 = 0.01;
        private static double B2 = 0.0020;
        private Line line;

        private LineTestData() {
            line = Mockito.mock(Line.class);
            Mockito.when(line.getR()).thenReturn(R);
            Mockito.when(line.getX()).thenReturn(X);
            Mockito.when(line.getG1()).thenReturn(G1);
            Mockito.when(line.getB1()).thenReturn(B1);
            Mockito.when(line.getG2()).thenReturn(G2);
            Mockito.when(line.getB2()).thenReturn(B2);
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
        private DanglingLine danglinLine;

        private DanglingLineTestData() {
            danglinLine = Mockito.mock(DanglingLine.class);
            Mockito.when(danglinLine.getR()).thenReturn(R);
            Mockito.when(danglinLine.getX()).thenReturn(X);
            Mockito.when(danglinLine.getG()).thenReturn(G);
            Mockito.when(danglinLine.getB()).thenReturn(B);
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

        private static RatioTapChanger rtc;
        private static RatioTapChangerStep rtcStep;
        private static PhaseTapChanger ptc;
        private static PhaseTapChangerStep ptcStep;
        private static TwoWindingsTransformer twt;

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

        private static PhaseTapChanger ptc;
        private static PhaseTapChangerStep ptcStep;
        private static TwoWindingsTransformer twt;

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
        }

        private TwoWindingsTransformer getTwoWindingsTransformer() {
            return twt;
        }
    }

    private static final class HalfLineTestData {
        private static double R = 0.15;
        private static double X = 0.25;
        private static double G1 = 0.01;
        private static double B1 = 0.0020;
        private static double G2 = 0.01;
        private static double B2 = 0.0020;
        private TieLine.HalfLine halfLine;

        private HalfLineTestData() {
            halfLine = Mockito.mock(TieLine.HalfLine.class);
            Mockito.when(halfLine.getR()).thenReturn(R);
            Mockito.when(halfLine.getX()).thenReturn(X);
            Mockito.when(halfLine.getG1()).thenReturn(G1);
            Mockito.when(halfLine.getB1()).thenReturn(B1);
            Mockito.when(halfLine.getG2()).thenReturn(G2);
            Mockito.when(halfLine.getB2()).thenReturn(B2);
        }

        private TieLine.HalfLine getHalfLine() {
            return halfLine;
        }
    }
}
