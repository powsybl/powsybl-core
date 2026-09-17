/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter.util;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteAngleRegulation;
import com.powsybl.ucte.network.UctePhaseRegulation;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.ToDoubleFunction;

/**
 * @author Abdelsalem HEDHILI  {@literal <abdelsalem.hedhili at rte-france.com>}
 */
public final class UcteConverterHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteConverterHelper.class);

    private static final double DEVIATION_EPSILON = 1e-6;

    private UcteConverterHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * calculate the δu(%) for the phase regulation of the two windings transformer
     *
     * du = 100 * (1 / ρmin – 1/ ρmax) / (number of taps - 1).
     *
     * @param twoWindingsTransformer The TwoWindingsTransformers containing the RatioTapChanger we want to convert
     * @return the δu needed to create a {@link UctePhaseRegulation}
     */
    public static double calculatePhaseDu(TwoWindingsTransformer twoWindingsTransformer) {
        RatioTapChanger tapChanger = twoWindingsTransformer.getRatioTapChanger();
        // Given the formulas : rho(i) = 1/(1+du.i/100)
        // We have : -n.du = 100.(1/rho(-n) - 1)  and n.du = 100.(1/rho(n) - 1)
        // Which gives n.du - (-n.du) = 100.(1/rho(n) - 1/rho(-n))
        // then du = 100.(1/rho(n) - 1/rho(-n))/(nbTaps-1)
        double rhoStepMin = tapChanger.getStep(tapChanger.getLowTapPosition()).getRho();
        double rhoStepMax = tapChanger.getStep(tapChanger.getHighTapPosition()).getRho();
        double res = 100 * (1 / rhoStepMax - 1 / rhoStepMin) / (tapChanger.getStepCount() - 1);

        return BigDecimal.valueOf(res).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculate the δu(%) for the angle regulation of the two windings transformer
     * This computation only work for an angle regulation with the type symmetrical
     *
     * dU = 100 * (2*(tan(αmax/2) – tan(αmin/2))/(number of taps -1))
     *
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return the δu needed to create a {@link UcteAngleRegulation}
     */
    public static double calculateSymmAngleDu(TwoWindingsTransformer twoWindingsTransformer) {
        PhaseTapChanger tapChanger = twoWindingsTransformer.getPhaseTapChanger();
        // Given the formulas : alpha(i) = 2.Atan(i/100.du/2)
        // We have : -n.du = 200.tan(alpha(-n)/2)  and n.du = 200.tan(alpha(n)/2)
        // Which gives n.du - (-n.du) = 200.(tan(alpha(n)/2) - tan(alpha(-n)/2))
        // then du = 2.100.(tan(alpha(n)/2) - tan(alpha(-n)/2))/(nbTaps-1)
        double alphaStepMax = Math.toRadians(tapChanger.getStep(tapChanger.getHighTapPosition()).getAlpha());
        double alphaStepMin = Math.toRadians(tapChanger.getStep(tapChanger.getLowTapPosition()).getAlpha());

        // minus sign because in the UCT importer, alpha has sign inverted because in the UCT model PST is on side 2 and side1 on IIDM model
        // we apply here the same transformation back
        return -100 * (2 * (Math.tan(alphaStepMax / 2) - Math.tan(alphaStepMin / 2)) / (tapChanger.getStepCount() - 1));
    }

    /**
     * Calculate the δu(%) for the angle regulation of the two windings transformer
     * This computation only work for an angle regulation with the type asymmetrical
     *
     * dU = 100 * (distance between the 2 most distant taps) / (number of taps - 1)
     *
     * @param twoWindingsTransformer The twoWindingsTransformer containing the PhaseTapChanger we want to convert
     * @return the δu needed to create a {@link UcteAngleRegulation}
     */

    public static Complex calculateAsymmAngleDuAndAngle(TwoWindingsTransformer twoWindingsTransformer, boolean combinePhaseAngleRegulation) {
        PhaseTapChanger phaseTapChanger = twoWindingsTransformer.getPhaseTapChanger();
        int lowTapPosition = phaseTapChanger.getLowTapPosition();
        int highTapPosition = phaseTapChanger.getHighTapPosition();
        double lowPositionAlpha = Math.toRadians(-phaseTapChanger.getStep(lowTapPosition).getAlpha());
        double lowPositionRho = 1 / phaseTapChanger.getStep(lowTapPosition).getRho();
        double highPositionAlpha = Math.toRadians(-phaseTapChanger.getStep(highTapPosition).getAlpha());
        double highPositionRho = 1 / phaseTapChanger.getStep(highTapPosition).getRho();
        double xa = lowPositionRho * Math.cos(lowPositionAlpha);
        double ya = lowPositionRho * Math.sin(lowPositionAlpha);
        double xb = highPositionRho * Math.cos(highPositionAlpha);
        double yb = highPositionRho * Math.sin(highPositionAlpha);

        double theta;
        if (Math.abs(xb - xa) < 0.000000001) {
            // we suppose that theta is equal to Pi/2 in this case
            theta = Math.PI / 2.;
        } else {
            theta = Math.atan((yb - ya) / (xb - xa));
        }
        // the formula above gives actually the module of du, we need to verify the sign of du
        if ((yb * highPositionRho - ya * lowPositionRho) / Math.sin(theta) < 0.) {
            theta = theta - Math.PI;
        }

        int tapNumber = phaseTapChanger.getStepCount();
        double distance = Math.sqrt((xb - xa) * (xb - xa) + (yb - ya) * (yb - ya));
        double absDu = 100 * distance / (tapNumber - 1);
        if (combinePhaseAngleRegulation) {
            double r0Rtc = 1.0;
            if (twoWindingsTransformer.getRatioTapChanger() != null) {
                RatioTapChanger ratioTapChanger = twoWindingsTransformer.getRatioTapChanger();
                int r0TapPosition = ratioTapChanger.getTapPosition();
                r0Rtc = ratioTapChanger.getStep(r0TapPosition).getRho();
            }
            absDu = absDu / r0Rtc; // in the case of a combined RTC and PTC absDu includes rho0 of RTC
        }

        return ComplexUtils.polar2Complex(BigDecimal.valueOf(absDu).setScale(4, RoundingMode.HALF_UP).doubleValue(),
                                          theta);
    }

    /**
     * Holds the UCTE {@code n}/{@code np} pair computed for a tap changer's declared range.
     */
    public record TapPositionRange(int n, int np) { }

    /**
     * Get the position with the lowest step distance according to the provided function.
     * @param tapChanger a tap changer
     * @param f a function calculating a distance on a step
     * @return the tap position of the step that scores the lowest
     * @param <S> type of steps
     * @param <C> type of tap changer
     */
    private static <S extends TapChangerStep<S>, C extends TapChanger<C, S, ?, ?>> int findNeutralPosition(
            TapChanger<C, S, ?, ?> tapChanger, ToDoubleFunction<S> f) {
        // Try to get the "perfectly" neutral position first.
        OptionalInt realNeutral = tapChanger.getNeutralPosition();
        if (realNeutral.isPresent()) {
            return realNeutral.getAsInt();
        }

        // We compare steps using the distance function f.
        Comparator<Map.Entry<Integer, S>> stepsComparator =
                Comparator.comparingDouble(posStep -> f.applyAsDouble(posStep.getValue()));
        // In case of many equal steps on the "neutral" position, we use the distance to the middle tap position
        int midPos = (tapChanger.getLowTapPosition() + tapChanger.getHighTapPosition()) / 2;
        stepsComparator = stepsComparator.thenComparingInt(posStep -> Math.abs(midPos - posStep.getKey()));

        return tapChanger.getAllSteps().entrySet().stream()
                         .min(stepsComparator)
                         .map(Map.Entry::getKey)
                         .orElse(-1);
    }

    /**
     * Finds the tap position whose {@code rho} is closest to {@code 1.0}. Ties break to the lower tap position.
     *
     * @param tapChanger the ratio tap changer
     * @return the neutral tap position
     */
    public static int findNeutralTapPosition(RatioTapChanger tapChanger) {
        return findNeutralPosition(
                tapChanger,
                step -> Math.abs(step.getRho() - 1.0)
        );
    }

    /**
     * Finds the tap position whose {@code alpha} is closest to {@code 0.0}. Ties (including the degenerate case
     * where every step has the same {@code alpha}, e.g. an all-zero asymmetrical regulation) break to the step
     * whose {@code rho} is closest to {@code 1.0}; further ties break to the lower tap position.
     *
     * @param tapChanger the phase tap changer
     * @return the neutral tap position
     */
    public static int findNeutralTapPosition(PhaseTapChanger tapChanger) {
        return findNeutralPosition(
                tapChanger,
                step -> Math.abs(step.getAlpha())
        );
    }

    /**
     * Computes the UCTE {@code (n, np)} pair relative to the given neutral tap position, extending the declared range
     * to the longer side when the real range isn't symmetric around the neutral tap. Callers that care about this
     * extension are responsible for warning about it (see {@code UcteExporter#logIfTapPositionRangeExtended}).
     *
     * @param lowTapPosition the tap changer's low tap position
     * @param highTapPosition the tap changer's high tap position
     * @param currentTapPosition the tap changer's current tap position
     * @param neutralPosition the neutral tap position, as found by {@code findNeutralTapPosition}
     * @return the UCTE {@code (n, np)} tap position range
     */
    public static TapPositionRange computeTapPositionRange(int lowTapPosition, int highTapPosition,
                                                            int currentTapPosition, int neutralPosition) {
        int offsetLow = lowTapPosition - neutralPosition;
        int offsetHigh = highTapPosition - neutralPosition;
        int offsetCurrent = currentTapPosition - neutralPosition;
        int n = Math.max(Math.abs(offsetLow), Math.abs(offsetHigh));
        return new TapPositionRange(n, offsetCurrent);
    }

    /**
     * Predicts the {@code rho} the UCTE linear model implies at a tap position {@code du·offset/100} away from
     * neutral, per {@code rho(i) = 1/(1+du.i/100)} (see {@link #calculatePhaseDu(TwoWindingsTransformer)}).
     *
     * @param du the δu(%) of the ratio regulation
     * @param offset the number of taps away from neutral
     * @return the predicted {@code rho}
     */
    public static double predictRatioStep(double du, int offset) {
        return 1 / (1 + du * offset / 100);
    }

    /**
     * Predicts the {@code alpha} (degrees, IIDM sign convention) the UCTE symmetric-angle linear model implies at a
     * tap position {@code offset} taps from neutral. The leading minus sign mirrors the same UCT/IIDM sign inversion
     * already applied in {@link #calculateSymmAngleDu(TwoWindingsTransformer)}.
     * <br>
     * SYMM angle regulation assumes that {@code theta = 90°}
     *
     * @param du     the δu(%) of the symmetric angle regulation
     * @param offset the number of taps away from neutral
     * @return the predicted {@code alpha}, in degrees
     */
    public static double predictSymmAngleStep(double du, int offset) {
        return -2 * Math.toDegrees(Math.atan(offset * du / 200));
    }

    /**
     * Predicts the complex point the UCTE asymmetric-angle linear model implies {@code stepsFromLow} taps away from
     * the low tap position, anchored at {@code lowPositionComplex} the same way
     * {@link #calculateAsymmAngleDuAndAngle(TwoWindingsTransformer, boolean)} fits its line.
     *
     * @param lowPositionComplex the complex point at the low tap position
     * @param absDu the δu(%) (module) of the asymmetric angle regulation
     * @param theta the angle (radians) of the asymmetric angle regulation
     * @param stepsFromLow the number of taps away from the low tap position
     * @return the predicted complex point
     */
    public static Complex predictAsymmAngleStep(Complex lowPositionComplex,
                                                double absDu,
                                                double theta,
                                                int stepsFromLow) {
        Complex direction = new Complex(Math.cos(theta), Math.sin(theta));
        return lowPositionComplex.add(direction.multiply(stepsFromLow * absDu / 100));
    }

    /**
     * Logs one warning if any step of {@code tapChanger} deviates from the ratio linear model by more than
     * {@link #DEVIATION_EPSILON}.
     *
     * @param tapChanger the ratio tap changer to check
     * @param du the δu(%) of the ratio regulation
     * @param neutralPosition the neutral tap position
     * @param equipmentId the id of the two windings transformer, used in the warning message
     * @param reportNode the reportNode used for functional logs
     */
    public static void checkRatioTapChangerDeviation(RatioTapChanger tapChanger,
                                                     double du,
                                                     int neutralPosition,
                                                     String equipmentId,
                                                     ReportNode reportNode) {
        for (int p = tapChanger.getLowTapPosition(); p <= tapChanger.getHighTapPosition(); p++) {
            double predicted = predictRatioStep(du, p - neutralPosition);
            double actual = tapChanger.getStep(p).getRho();
            if (Math.abs(actual - predicted) > DEVIATION_EPSILON) {
                logDeviationWarning(equipmentId, "ratio tap changer", reportNode);
                return;
            }
        }
    }

    /**
     * Logs one warning if any step of {@code tapChanger} deviates from the symmetric-angle linear model by more than
     * {@link #DEVIATION_EPSILON}.
     *
     * @param tapChanger      the phase tap changer to check
     * @param du              the δu(%) of the symmetric angle regulation
     * @param neutralPosition the neutral tap position
     * @param equipmentId     the id of the two windings transformer, used in the warning message
     * @param reportNode      the reportNode used for functional logs
     */
    public static void checkSymmAngleTapChangerDeviation(PhaseTapChanger tapChanger,
                                                         double du,
                                                         int neutralPosition,
                                                         String equipmentId,
                                                         ReportNode reportNode) {
        for (int p = tapChanger.getLowTapPosition(); p <= tapChanger.getHighTapPosition(); p++) {
            double predicted = predictSymmAngleStep(du, p - neutralPosition);
            double actual = tapChanger.getStep(p).getAlpha();
            if (Math.abs(actual - predicted) > DEVIATION_EPSILON) {
                logDeviationWarning(equipmentId, "angle tap changer (SYMM)", reportNode);
                return;
            }
        }
    }

    /**
     * Logs one warning if any step of {@code tapChanger} deviates from the asymmetric-angle linear model by more than
     * {@link #DEVIATION_EPSILON}, comparing points in the same complex representation used by
     * {@link #calculateAsymmAngleDuAndAngle(TwoWindingsTransformer, boolean)}.
     *
     * @param tapChanger the phase tap changer to check
     * @param absDu the δu(%) (module) of the asymmetric angle regulation
     * @param theta the angle (radians) of the asymmetric angle regulation
     * @param equipmentId the id of the two windings transformer, used in the warning message
     * @param reportNode the reportNode used for functional logs
     */
    public static void checkAsymmAngleTapChangerDeviation(PhaseTapChanger tapChanger,
                                                          double absDu,
                                                          double theta,
                                                          String equipmentId,
                                                          ReportNode reportNode) {
        int lowTapPosition = tapChanger.getLowTapPosition();
        double lowPositionAlpha = Math.toRadians(-tapChanger.getStep(lowTapPosition).getAlpha());
        double lowPositionRho = 1 / tapChanger.getStep(lowTapPosition).getRho();
        Complex lowPositionComplex = ComplexUtils.polar2Complex(lowPositionRho, lowPositionAlpha);
        for (int p = lowTapPosition; p <= tapChanger.getHighTapPosition(); p++) {
            Complex predicted = predictAsymmAngleStep(lowPositionComplex, absDu, theta, p - lowTapPosition);
            double actualAlpha = Math.toRadians(-tapChanger.getStep(p).getAlpha());
            double actualRho = 1 / tapChanger.getStep(p).getRho();
            Complex actual = ComplexUtils.polar2Complex(actualRho, actualAlpha);
            if (predicted.subtract(actual).abs() > DEVIATION_EPSILON) {
                logDeviationWarning(equipmentId, "angle tap changer (ASSYM)", reportNode);
                return;
            }
        }
    }

    private static void logDeviationWarning(String equipmentId, String tapChangerType, ReportNode reportNode) {
        LOGGER.warn("Two windings transformer {}: differences found between actual tap steps and the UCTE linear model",
                equipmentId);
        UcteExporterReports.tapChangerModelDeviation(reportNode, tapChangerType, equipmentId);
    }
}
