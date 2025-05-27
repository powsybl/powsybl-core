/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.elements.transformers.TapChanger.Step;
import com.powsybl.cgmes.model.CgmesModelException;
import org.apache.commons.math3.complex.Complex;

import java.util.Objects;

/**
 * TapChangerTypes
 * <p>
 * TapChangers are classified into four categories: NULL, FIXED, NON_REGULATING and REGULATING <br>
 * NULL: TapChanger does not exist <br>
 * FIXED: TapChanger has only one step <br>
 * NON_REGULATING: TapChanger is not regulating. It has several steps <br>
 * REGULATING: TapChanger is regulating and has several steps
 * <p>
 * These categories are used to establish a priority ranking when two tapChangers are combined to only one.
 * The tapChanger with lower priority is fixed at the current tap position and the tapChanger with higher priority
 * is preserved. NULL is the lowest priority and REGULATING the highest one. <br>
 * If both tapChangers are regulating tapChanger at end2 is fixed.
 * <p>
 * The combineTapChanger method does a Cartesian product of two tapChangers getting as result a new tapChanger with
 * steps1 x steps2 steps. The combined tapChanger will not be useful for network analysis and it is not possible to
 * map back to the original one. <br>
 * To avoid this, one of the tapChangers must be fixed. To do that the tapChangerFixPosition method is used.
 * <p>
 * A warning message is logged when a tapChanger is fixed.
 * <p>
 * When structural ratio is moved from one side to the other a correction is applied to transmission impedance (r, x) and shunt admittance (g and b). <br>
 * When tapChanger is moved the transmission impedance and shunt admittance corrections are managed as step correction
 * expressed as percentage deviation of nominal value.
 * //TODO: how to deal with solvedtapposition???
 * <p>
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class TapChangerConversion {

    protected final Context context;

    TapChangerConversion(Context context) {
        Objects.requireNonNull(context);
        this.context = context;
    }

    private enum TapChangerType {
        NULL, FIXED, NON_REGULATING, REGULATING
    }

    protected TapChanger combineTapChangers(TapChanger tc1, TapChanger tc2) {
        switch (tapChangerType(tc1)) {
            case NULL:
                return combineTapChangerNull(tc2);
            case FIXED:
                return combineTapChangerFixed(tc1, tc2);
            case NON_REGULATING:
                return combineTapChangerNonRegulating(tc1, tc2);
            case REGULATING:
                return combineTapChangerRegulating(tc1, tc2);
        }
        return null;
    }

    private static TapChanger combineTapChangerNull(TapChanger tc) {
        switch (tapChangerType(tc)) {
            case NULL:
                return null;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return tc;
        }
        return null;
    }

    private static TapChanger combineTapChangerFixed(TapChanger fixTc, TapChanger tc2) {
        switch (tapChangerType(tc2)) {
            case NULL:
                return fixTc;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return combineTapChanger(tc2, fixTc);
        }
        return null;
    }

    private TapChanger combineTapChangerNonRegulating(TapChanger tcNonRegulating, TapChanger tc2) {
        switch (tapChangerType(tc2)) {
            case NULL:
                return tcNonRegulating;
            case FIXED:
                return combineTapChanger(tcNonRegulating, tc2);
            case NON_REGULATING:
                TapChanger ntc2 = tapChangerFixPosition(tc2);
                return combineTapChanger(tcNonRegulating, ntc2);
            case REGULATING:
                TapChanger ntcNonRegulating = tapChangerFixPosition(tcNonRegulating);
                return combineTapChanger(tc2, ntcNonRegulating);
        }
        return null;
    }

    private TapChanger combineTapChangerRegulating(TapChanger tcRegulating, TapChanger tc2) {
        switch (tapChangerType(tc2)) {
            case NULL:
                return tcRegulating;
            case FIXED:
                return combineTapChanger(tcRegulating, tc2);
            case NON_REGULATING:
            case REGULATING:
                TapChanger ntc2 = tapChangerFixPosition(tc2);
                return combineTapChanger(tcRegulating, ntc2);
        }
        return null;
    }

    private static TapChanger combineTapChanger(TapChanger tc1, TapChanger tc2) {
        TapChanger tapChanger = baseCloneTapChanger(tc1);
        combineTapChangerSteps(tapChanger, tc1, tc2);
        tapChanger.setHiddenCombinedTapChanger(tc2);
        return tapChanger;
    }

    /**
     * A new tapChanger is created with only one step, the current tap position.
    */
    private TapChanger tapChangerFixPosition(TapChanger tc) {
        if (tc.getLowTapPosition() != tc.getHighTapPosition()) {
            context.fixed("TapChanger", () -> String.format("%s fixed tap at position %d ", tc.getId(), tc.getTapPosition()));
        }
        TapChanger tapChanger = baseCloneTapChanger(tc);
        tapChanger.setLowTapPosition(tapChanger.getTapPosition());
        Step step = getTapChangerFixedStep(tc);
        double ratio = step.getRatio();
        double angle = step.getAngle();
        double r = step.getR();
        double x = step.getX();
        double g1 = step.getG1();
        double b1 = step.getB1();
        double g2 = step.getG2();
        double b2 = step.getB2();
        tapChanger.beginStep()
            .setRatio(ratio)
            .setAngle(angle)
            .setR(r)
            .setX(x)
            .setG1(g1)
            .setB1(b1)
            .setG2(g2)
            .setB2(b2)
            .endStep();
        return tapChanger;
    }

    /**
     * A new tapChanger is created as Cartesian product of tc1 and tc2
     * One of the tapChangers must be fixed (only one step)
    */
    private static void combineTapChangerSteps(TapChanger tapChanger, TapChanger tc1, TapChanger tc2) {
        TapChanger fixTc;
        TapChanger tc;
        if (tc1 != null && tc2 != null && tc1.getSteps().size() == 1
            && tc1.getLowTapPosition() == tc1.getHighTapPosition()) {
            fixTc = tc1;
            tc = tc2;
        } else if (tc1 != null && tc2 != null && tc2.getSteps().size() == 1
            && tc2.getLowTapPosition() == tc2.getHighTapPosition()) {
            fixTc = tc2;
            tc = tc1;
        } else if (tc1 != null && tc2 != null) {
            throw new CgmesModelException(
                "Unexpected number of steps in tapChangers: " + tc1.getId() + ", " + tc2.getId());
        } else {
            throw new CgmesModelException("Unexpected null tapChanger");
        }

        Step stepFixed = getTapChangerFixedStep(fixTc);
        double ratioFixed = stepFixed.getRatio();
        double angleFixed = stepFixed.getAngle();
        double rFixed = stepFixed.getR();
        double xFixed = stepFixed.getX();
        double g1Fixed = stepFixed.getG1();
        double b1Fixed = stepFixed.getB1();
        double g2Fixed = stepFixed.getG2();
        double b2Fixed = stepFixed.getB2();
        Complex aFixed = new Complex(ratioFixed * Math.cos(Math.toRadians(angleFixed)),
            ratioFixed * Math.sin(Math.toRadians(angleFixed)));
        tc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            double g1 = step.getG1();
            double b1 = step.getB1();
            double g2 = step.getG2();
            double b2 = step.getB2();
            Complex a = new Complex(ratio * Math.cos(Math.toRadians(angle)),
                ratio * Math.sin(Math.toRadians(angle)));
            Complex na = a.multiply(aFixed);
            tapChanger.beginStep()
                .setRatio(na.abs())
                .setAngle(Math.toDegrees(na.getArgument()))
                .setR(combineTapChangerCorrection(rFixed, r))
                .setX(combineTapChangerCorrection(xFixed, x))
                .setG1(combineTapChangerCorrection(g1Fixed, g1))
                .setB1(combineTapChangerCorrection(b1Fixed, b1))
                .setG2(combineTapChangerCorrection(g2Fixed, g2))
                .setB2(combineTapChangerCorrection(b2Fixed, b2))
                .endStep();
        });
        tapChanger.setLowTapPosition(tc.getLowTapPosition());
        tapChanger.setTapPosition(tc.getTapPosition());
        tapChanger.setSolvedTapPosition(tc.getSolvedTapPosition());
    }

    /**
     * To combine the percentage deviations express them as multiplying factors, then combine and
     * express them again as percentage deviations
     */
    private static double combineTapChangerCorrection(double correction1, double correction2) {
        if (correction1 != 0.0 && correction2 != 0.0) {
            return 100 * ((1 + correction1 / 100) * (1 + correction2 / 100) - 1);
        } else if (correction1 != 0.0) {
            return correction1;
        } else if (correction2 != 0.0) {
            return correction2;
        } else {
            return 0.0;
        }
    }

    // not used at the moment
    protected static TapChanger moveTapChangerFrom1To2(TapChanger tc) {
        return moveTapChangerFromOneEndToTheOther(tc);
    }

    protected static TapChanger moveTapChangerFrom2To1(TapChanger tc) {
        return moveTapChangerFromOneEndToTheOther(tc);
    }

    private static TapChanger moveTapChangerFromOneEndToTheOther(TapChanger tc) {
        switch (tapChangerType(tc)) {
            case NULL:
                return null;
            case FIXED:
            case NON_REGULATING:
            case REGULATING:
                return moveTapChanger(tc);
        }
        return null;
    }

    /**
     * A new equivalent tapChanger located in the other side is created.
     * When given in RatioTapChangerTablePoint
     * r, x, g, b of the step are already percentage deviations of nominal values
     * R = R * (1 + r / 100)
     * X = X * (1 + x / 100)
     * G = G * (1 + g / 100)
     * B = B * (1 + b / 100)
     *
     * New tapChanger
     * Will have the same base attributes, ratio will be the reciprocal and
     * an impedance/admittance deviations are required by step
     */
    private static TapChanger moveTapChanger(TapChanger tc) {
        TapChanger tapChanger = baseCloneTapChanger(tc);
        moveTapChangerSteps(tapChanger, tc);
        return tapChanger;
    }

    private static void moveTapChangerSteps(TapChanger tapChanger, TapChanger tc) {
        tc.getSteps().forEach(step -> {
            double ratio = step.getRatio();
            double angle = step.getAngle();
            double r = step.getR();
            double x = step.getX();
            double g1 = step.getG1();
            double b1 = step.getB1();
            double g2 = step.getG2();
            double b2 = step.getB2();
            TapChangerStepConversion convertedStep = calculateConversionStep(ratio, angle, r, x, g1, b1, g2, b2);
            tapChanger.beginStep()
                .setRatio(convertedStep.ratio)
                .setAngle(convertedStep.angle)
                .setR(convertedStep.r)
                .setX(convertedStep.x)
                .setG1(convertedStep.g1)
                .setB1(convertedStep.b1)
                .setG2(convertedStep.g2)
                .setB2(convertedStep.b2)
                .endStep();
        });
    }

    private static TapChangerStepConversion calculateConversionStep(double a, double angle, double r,
        double x, double g1, double b1, double g2, double b2) {
        Complex ratio = new Complex(a * Math.cos(Math.toRadians(angle)),
            a * Math.sin(Math.toRadians(angle)));
        return calculateConversionStep(ratio, r, x, g1, b1, g2, b2);
    }

    /**
     * To calculate the step correction express it as multiplying factor, then apply the correction
     * and express again as percentage deviation
     */
    private static TapChangerStepConversion calculateConversionStep(Complex a, double r,
        double x, double g1, double b1, double g2, double b2) {
        TapChangerStepConversion step = new TapChangerStepConversion();
        Complex na = a.reciprocal();
        step.ratio = na.abs();
        step.angle = Math.toDegrees(na.getArgument());
        step.r = 100 * (impedanceConversion(1 + r / 100, a) - 1);
        step.x = 100 * (impedanceConversion(1 + x / 100, a) - 1);
        step.g1 = 100 * (admittanceConversion(1 + g1 / 100, a) - 1);
        step.b1 = 100 * (admittanceConversion(1 + b1 / 100, a) - 1);
        step.g2 = 100 * (admittanceConversion(1 + g2 / 100, a) - 1);
        step.b2 = 100 * (admittanceConversion(1 + b2 / 100, a) - 1);
        return step;
    }

    protected static RatioConversion identityRatioConversion(double r, double x, double g1,
        double b1, double g2, double b2) {
        RatioConversion ratio = new RatioConversion();
        ratio.r = r;
        ratio.x = x;
        ratio.g1 = g1;
        ratio.b1 = b1;
        ratio.g2 = g2;
        ratio.b2 = b2;
        return ratio;
    }

    protected static RatioConversion moveRatioFrom2To1(double a, double angle, double r, double x, double g1,
        double b1, double g2, double b2) {
        return moveRatio(a, angle, r, x, g1, b1, g2, b2);
    }

    protected static RatioConversion moveRatioFrom1To2(double a, double angle, double r, double x, double g1,
        double b1, double g2, double b2) {
        return moveRatio(a, angle, r, x, g1, b1, g2, b2);
    }

    /**
     * Equivalent impedance / admittance after moving a complex ratio from one end to the other
     */
    private static RatioConversion moveRatio(double a, double angle, double r, double x, double g1,
        double b1, double g2, double b2) {
        Complex ratio = new Complex(a * Math.cos(Math.toRadians(angle)),
            a * Math.sin(Math.toRadians(angle)));
        return moveRatio(ratio, r, x, g1, b1, g2, b2);
    }

    private static RatioConversion moveRatio(Complex a, double r, double x, double g1,
        double b1, double g2, double b2) {
        RatioConversion ratio = new RatioConversion();
        ratio.r = impedanceConversion(r, a);
        ratio.x = impedanceConversion(x, a);
        ratio.g1 = admittanceConversion(g1, a);
        ratio.b1 = admittanceConversion(b1, a);
        ratio.g2 = admittanceConversion(g2, a);
        ratio.b2 = admittanceConversion(b2, a);
        return ratio;
    }

    private static double admittanceConversion(double correction, Complex a) {
        double a2 = a.abs() * a.abs();
        return correction / a2;
    }

    private static double impedanceConversion(double correction, Complex a) {
        double a2 = a.abs() * a.abs();
        return correction * a2;
    }

    /**
     * A new tapChanger is created with the same base attributes. Steps are not cloned.
     */
    private static TapChanger baseCloneTapChanger(TapChanger rtc) {
        TapChanger tapChanger = new TapChanger();
        String id = rtc.getId();
        boolean isLtcFlag = rtc.isLtcFlag();
        boolean isRegulating = rtc.isRegulating();
        String regulatingControlId = rtc.getRegulatingControlId();
        String tculControlMode = rtc.getTculControlMode();
        boolean isTapChangerControlEnabled = rtc.isTapChangerControlEnabled();
        int lowStep = rtc.getLowTapPosition();
        int position = rtc.getTapPosition();
        int solvedTapPosition = rtc.getSolvedTapPosition();
        String type = rtc.getType();
        TapChanger hiddenCombinedTapChanger = rtc.getHiddenCombinedTapChanger();
        tapChanger.setLowTapPosition(lowStep)
            .setTapPosition(position)
            .setSolvedTapPosition(solvedTapPosition)
            .setLtcFlag(isLtcFlag)
            .setId(id)
            .setRegulating(isRegulating)
            .setRegulatingControlId(regulatingControlId)
            .setTculControlMode(tculControlMode)
            .setTapChangerControlEnabled(isTapChangerControlEnabled)
            .setType(type)
            .setHiddenCombinedTapChanger(hiddenCombinedTapChanger);
        return tapChanger;
    }

    private static Step getTapChangerFixedStep(TapChanger tc) {
        if (isTapChangerFixed(tc)) {
            return tc.getSteps().get(0);
        }
        int position = tc.getTapPosition();
        int lowPosition = tc.getLowTapPosition();
        return tc.getSteps().get(position - lowPosition);
    }

    private static TapChangerType tapChangerType(TapChanger tc) {
        if (isTapChangerNull(tc)) {
            return TapChangerType.NULL;
        }
        if (isTapChangerFixed(tc)) {
            return TapChangerType.FIXED;
        }
        if (!isTapChangerRegulating(tc)) {
            return TapChangerType.NON_REGULATING;
        } else {
            return TapChangerType.REGULATING;
        }
    }

    private static boolean isTapChangerNull(TapChanger tc) {
        return tc == null;
    }

    private static boolean isTapChangerFixed(TapChanger tc) {
        return tc.getSteps().size() == 1;
    }

    private static boolean isTapChangerRegulating(TapChanger tc) {
        return tc.isRegulating();
    }

    static class InterpretedEnd {
        final double g;
        final double b;
        final TapChanger ratioTapChanger;
        final TapChanger phaseTapChanger;
        final double ratedU;
        final String terminal;

        InterpretedEnd(double g, double b, TapChanger ratioTapChanger, TapChanger phaseTapChanger, double ratedU, String terminal) {
            this.g = g;
            this.b = b;
            this.ratioTapChanger = ratioTapChanger;
            this.phaseTapChanger = phaseTapChanger;
            this.ratedU = ratedU;
            this.terminal = terminal;
        }
    }

    static class ConvertedEnd1 {
        final double g;
        final double b;
        final TapChanger ratioTapChanger;
        final TapChanger phaseTapChanger;
        final double ratedU;
        final String terminal;

        ConvertedEnd1(double g, double b, TapChanger ratioTapChanger, TapChanger phaseTapChanger, double ratedU, String terminal) {
            this.g = g;
            this.b = b;
            this.ratioTapChanger = ratioTapChanger;
            this.phaseTapChanger = phaseTapChanger;
            this.ratedU = ratedU;
            this.terminal = terminal;
        }
    }

    static class TapChangerStepConversion {
        double ratio;
        double angle;
        double r;
        double x;
        double g1;
        double b1;
        double g2;
        double b2;
    }

    static class RatioConversion {
        double r;
        double x;
        double g1;
        double b1;
        double g2;
        double b2;
    }

    static class AllTapChanger {
        TapChanger ratioTapChanger1;
        TapChanger phaseTapChanger1;
        TapChanger ratioTapChanger2;
        TapChanger phaseTapChanger2;
    }

    static class AllShunt {
        double g1;
        double b1;
        double g2;
        double b2;
    }
}
