/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.iidm.network.*;
import org.apache.commons.math3.complex.Complex;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class TransformerUtils {

    private TransformerUtils() {
    }

    public static void copyRatioTapChanger(RatioTapChangerAdder rtcAdder, RatioTapChanger rtc) {
        copyCommonRatioTapChanger(rtcAdder, rtc);

        rtc.getAllSteps().keySet().stream().sorted().forEach(step ->
                rtcAdder.beginStep()
                        .setR(rtc.getStep(step).getR())
                        .setX(rtc.getStep(step).getX())
                        .setG(rtc.getStep(step).getG())
                        .setB(rtc.getStep(step).getB())
                        .setRho(rtc.getStep(step).getRho())
                        .endStep());
        rtcAdder.add();
    }

    public static void copyAndMoveRatioTapChanger(RatioTapChangerAdder rtcAdder, RatioTapChanger rtc) {
        copyCommonRatioTapChanger(rtcAdder, rtc);

        rtc.getAllSteps().keySet().stream().sorted().forEach(step -> {
            double rho = rtc.getStep(step).getRho();
            double r = rtc.getStep(step).getR();
            double x = rtc.getStep(step).getX();
            double g = rtc.getStep(step).getG();
            double b = rtc.getStep(step).getB();

            Complex ratio = obtainComplexRatio(1.0 / rho, 0.0);
            Complex movedRatio = ratio.reciprocal();

            double rCorrection = 100 * (impedanceConversion(1 + r / 100, ratio) - 1);
            double xCorrection = 100 * (impedanceConversion(1 + x / 100, ratio) - 1);
            double gCorrection = 100 * (admittanceConversion(1 + g / 100, ratio) - 1);
            double bCorrection = 100 * (admittanceConversion(1 + b / 100, ratio) - 1);

            rtcAdder.beginStep()
                    .setR(rCorrection)
                    .setX(xCorrection)
                    .setG(gCorrection)
                    .setB(bCorrection)
                    .setRho(1.0 / movedRatio.abs())
                    .endStep();
        });
        rtcAdder.add();
    }

    private static void copyCommonRatioTapChanger(RatioTapChangerAdder rtcAdder, RatioTapChanger rtc) {
        rtcAdder.setTapPosition(rtc.getTapPosition())
                .setTargetV(rtc.getTargetV())
                .setLoadTapChangingCapabilities(rtc.hasLoadTapChangingCapabilities())
                .setRegulationMode(rtc.getRegulationMode())
                .setRegulationValue(rtc.getRegulationValue())
                .setLowTapPosition(rtc.getLowTapPosition())
                .setRegulating(rtc.isRegulating())
                .setRegulationTerminal(rtc.getRegulationTerminal())
                .setTargetDeadband(rtc.getTargetDeadband());
    }

    public static void copyPhaseTapChanger(PhaseTapChangerAdder ptcAdder, PhaseTapChanger ptc) {
        copyCommonPhaseTapChanger(ptcAdder, ptc);

        ptc.getAllSteps().keySet().stream().sorted().forEach(step ->
                ptcAdder.beginStep()
                        .setR(ptc.getStep(step).getR())
                        .setX(ptc.getStep(step).getX())
                        .setG(ptc.getStep(step).getG())
                        .setB(ptc.getStep(step).getB())
                        .setRho(ptc.getStep(step).getRho())
                        .setAlpha(ptc.getStep(step).getAlpha())
                        .endStep());
        ptcAdder.add();
    }

    public static void copyAndMovePhaseTapChanger(PhaseTapChangerAdder ptcAdder, PhaseTapChanger ptc) {
        copyCommonPhaseTapChanger(ptcAdder, ptc);

        ptc.getAllSteps().keySet().stream().sorted().forEach(step -> {
            double rho = ptc.getStep(step).getRho();
            double alpha = ptc.getStep(step).getAlpha();
            double r = ptc.getStep(step).getR();
            double x = ptc.getStep(step).getX();
            double g = ptc.getStep(step).getG();
            double b = ptc.getStep(step).getB();

            Complex ratio = obtainComplexRatio(1.0 / rho, alpha);
            Complex movedRatio = ratio.reciprocal();

            double rCorrection = 100 * (impedanceConversion(1 + r / 100, ratio) - 1);
            double xCorrection = 100 * (impedanceConversion(1 + x / 100, ratio) - 1);
            double gCorrection = 100 * (admittanceConversion(1 + g / 100, ratio) - 1);
            double bCorrection = 100 * (admittanceConversion(1 + b / 100, ratio) - 1);

            ptcAdder.beginStep()
                    .setR(rCorrection)
                    .setX(xCorrection)
                    .setG(gCorrection)
                    .setB(bCorrection)
                    .setRho(1.0 / movedRatio.abs())
                    .setAlpha(-Math.toDegrees(movedRatio.getArgument()))
                    .endStep();
        });
        ptcAdder.add();
    }

    private static void copyCommonPhaseTapChanger(PhaseTapChangerAdder ptcAdder, PhaseTapChanger ptc) {
        ptcAdder.setTapPosition(ptc.getTapPosition())
                .setRegulationMode(ptc.getRegulationMode())
                .setRegulationValue(ptc.getRegulationValue())
                .setLowTapPosition(ptc.getLowTapPosition())
                .setRegulating(ptc.isRegulating())
                .setRegulationTerminal(ptc.getRegulationTerminal())
                .setTargetDeadband(ptc.getTargetDeadband());
    }

    private static Complex obtainComplexRatio(double ratio, double alpha) {
        return new Complex(ratio * Math.cos(Math.toRadians(alpha)), ratio * Math.sin(Math.toRadians(alpha)));
    }

    public static double impedanceConversion(double impedance, Complex a) {
        return impedance * a.abs() * a.abs();
    }

    public static double impedanceConversion(double impedance, double a) {
        return impedance * a * a;
    }

    public static double admittanceConversion(double admittance, Complex a) {
        return admittance / (a.abs() * a.abs());
    }

    public static double admittanceConversion(double admittance, double a) {
        return admittance / (a * a);
    }

    public static void copyOperationalLimitsGroup(OperationalLimitsGroup destination, OperationalLimitsGroup source) {
        source.getActivePowerLimits().ifPresent(activePowerLimits -> copyLoadingLimits(destination.newActivePowerLimits(), activePowerLimits));
        source.getApparentPowerLimits().ifPresent(apparentPowerLimits -> copyLoadingLimits(destination.newApparentPowerLimits(), apparentPowerLimits));
        source.getCurrentLimits().ifPresent(currentLimits -> copyLoadingLimits(destination.newCurrentLimits(), currentLimits));
    }

    private static void copyLoadingLimits(LoadingLimitsAdder<?, ?> loadingLimitsAdder, LoadingLimits loadingLimits) {
        loadingLimitsAdder.setPermanentLimit(loadingLimits.getPermanentLimit());
        loadingLimits.getTemporaryLimits().forEach(temporaryLimit ->
                loadingLimitsAdder.beginTemporaryLimit()
                        .setName(temporaryLimit.getName())
                        .setValue(temporaryLimit.getValue())
                        .setAcceptableDuration(temporaryLimit.getAcceptableDuration())
                        .setFictitious(temporaryLimit.isFictitious())
                        .endTemporaryLimit());
        loadingLimitsAdder.add();
    }

    public static void copyTerminalActiveAndReactivePower(Terminal sourceTerminal, Terminal destinationTerminal) {
        destinationTerminal.setP(sourceTerminal.getP());
        destinationTerminal.setQ(sourceTerminal.getQ());
    }
}
