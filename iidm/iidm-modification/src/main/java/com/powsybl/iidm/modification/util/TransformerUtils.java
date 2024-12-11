/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.util;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.util.LoadingLimitsUtil;
import org.apache.commons.math3.complex.Complex;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class TransformerUtils {

    private TransformerUtils() {
    }

    public static void copyAndAddRatioTapChanger(RatioTapChangerAdder rtcAdder, RatioTapChanger rtc) {
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

    public static void copyAndMoveAndAddRatioTapChanger(RatioTapChangerAdder rtcAdder, RatioTapChanger rtc) {
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

    public static void copyAndAddPhaseTapChanger(PhaseTapChangerAdder ptcAdder, PhaseTapChanger ptc) {
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

    public static void copyAndMoveAndAddPhaseTapChanger(PhaseTapChangerAdder ptcAdder, PhaseTapChanger ptc) {
        copyCommonPhaseTapChanger(ptcAdder, ptc);

        ptc.getAllSteps().keySet().stream().sorted().forEach(step -> {
            double rho = ptc.getStep(step).getRho();
            double alpha = ptc.getStep(step).getAlpha();
            double r = ptc.getStep(step).getR();
            double x = ptc.getStep(step).getX();
            double g = ptc.getStep(step).getG();
            double b = ptc.getStep(step).getB();

            Complex ratio = obtainComplexRatio(1.0 / rho, -alpha);
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
                .setRegulationTerminal(ptc.getRegulationTerminal())
                .setTargetDeadband(ptc.getTargetDeadband())
                .setRegulating(ptc.isRegulating());
    }

    private static Complex obtainComplexRatio(double ratio, double angle) {
        return new Complex(ratio * Math.cos(Math.toRadians(angle)), ratio * Math.sin(Math.toRadians(angle)));
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
        source.getActivePowerLimits().ifPresent(activePowerLimits -> LoadingLimitsUtil.initializeFromLoadingLimits(destination.newActivePowerLimits(), activePowerLimits).add());
        source.getApparentPowerLimits().ifPresent(apparentPowerLimits -> LoadingLimitsUtil.initializeFromLoadingLimits(destination.newApparentPowerLimits(), apparentPowerLimits).add());
        source.getCurrentLimits().ifPresent(currentLimits -> LoadingLimitsUtil.initializeFromLoadingLimits(destination.newCurrentLimits(), currentLimits).add());
    }

    public static void copyTerminalActiveAndReactivePower(Terminal destinationTerminal, Terminal sourceTerminal) {
        destinationTerminal.setP(sourceTerminal.getP());
        destinationTerminal.setQ(sourceTerminal.getQ());
    }

    public static void copyAndAddFortescue(TwoWindingsTransformerFortescueAdder t2wFortescueAdder, ThreeWindingsTransformerFortescue.LegFortescue legFortescue) {
        t2wFortescueAdder.withConnectionType1(legFortescue.getConnectionType())
                .withFreeFluxes(legFortescue.isFreeFluxes())
                .withGroundingR1(legFortescue.getGroundingR())
                .withGroundingX1(legFortescue.getGroundingX())
                .withRz(legFortescue.getRz())
                .withXz(legFortescue.getXz())
                .withConnectionType2(WindingConnectionType.Y)
                .withGroundingR2(.0)
                .withGroundingX2(.0);
        t2wFortescueAdder.add();
    }

    public static void copyAndAddFortescue(ThreeWindingsTransformerFortescueAdder t3wFortescue, TwoWindingsTransformerFortescue t2w1Fortescue, boolean isWellOrientedT2w1, TwoWindingsTransformerFortescue t2w2Fortescue, boolean isWellOrientedT2w2, TwoWindingsTransformerFortescue t2w3Fortescue, boolean isWellOrientedT2w3) {
        copyFortescueLeg(t3wFortescue.leg1(), t2w1Fortescue, isWellOrientedT2w1);
        copyFortescueLeg(t3wFortescue.leg2(), t2w2Fortescue, isWellOrientedT2w2);
        copyFortescueLeg(t3wFortescue.leg3(), t2w3Fortescue, isWellOrientedT2w3);
        t3wFortescue.add();
    }

    private static void copyFortescueLeg(ThreeWindingsTransformerFortescueAdder.LegFortescueAdder legFortescueAdder, TwoWindingsTransformerFortescue t2wFortescue, boolean isWellOrientedT2w) {
        if (t2wFortescue != null) {
            legFortescueAdder.withConnectionType(isWellOrientedT2w ? t2wFortescue.getConnectionType1() : t2wFortescue.getConnectionType2())
                    .withFreeFluxes(t2wFortescue.isFreeFluxes())
                    .withGroundingR(isWellOrientedT2w ? t2wFortescue.getGroundingR1() : t2wFortescue.getGroundingR2())
                    .withGroundingX(isWellOrientedT2w ? t2wFortescue.getGroundingX1() : t2wFortescue.getGroundingX2())
                    .withRz(t2wFortescue.getRz())
                    .withXz(t2wFortescue.getXz());
        }
    }

    public static void copyAndAddPhaseAngleClock(TwoWindingsTransformerPhaseAngleClockAdder phaseAngleClockAdder, int phaseAngleClock) {
        phaseAngleClockAdder.withPhaseAngleClock(phaseAngleClock);
        phaseAngleClockAdder.add();
    }

    public static void copyAndAddPhaseAngleClock(ThreeWindingsTransformerPhaseAngleClockAdder phaseAngleClockAdder, TwoWindingsTransformerPhaseAngleClock t2w2PhaseAngleClock, TwoWindingsTransformerPhaseAngleClock t2w3PhaseAngleClock) {
        if (t2w2PhaseAngleClock != null) {
            phaseAngleClockAdder.withPhaseAngleClockLeg2(t2w2PhaseAngleClock.getPhaseAngleClock());
        }
        if (t2w3PhaseAngleClock != null) {
            phaseAngleClockAdder.withPhaseAngleClockLeg3(t2w3PhaseAngleClock.getPhaseAngleClock());
        }
        phaseAngleClockAdder.add();
    }

    public static void copyAndAddToBeEstimated(TwoWindingsTransformerToBeEstimatedAdder toBeEstimatedAdder, boolean shouldEstimateRatioTapChanger, boolean shouldEstimatePhaseTapChanger) {
        toBeEstimatedAdder.withRatioTapChangerStatus(shouldEstimateRatioTapChanger)
                .withPhaseTapChangerStatus(shouldEstimatePhaseTapChanger);
        toBeEstimatedAdder.add();
    }

    public static void copyAndAddToBeEstimated(ThreeWindingsTransformerToBeEstimatedAdder toBeEstimatedAdder, TwoWindingsTransformerToBeEstimated t2w1ToBeEstimated, TwoWindingsTransformerToBeEstimated t2w2ToBeEstimated, TwoWindingsTransformerToBeEstimated t2w3ToBeEstimated) {
        if (t2w1ToBeEstimated != null) {
            toBeEstimatedAdder.withRatioTapChanger1Status(t2w1ToBeEstimated.shouldEstimateRatioTapChanger())
                    .withPhaseTapChanger1Status(t2w1ToBeEstimated.shouldEstimatePhaseTapChanger());
        }
        if (t2w2ToBeEstimated != null) {
            toBeEstimatedAdder.withRatioTapChanger2Status(t2w2ToBeEstimated.shouldEstimateRatioTapChanger())
                    .withPhaseTapChanger2Status(t2w2ToBeEstimated.shouldEstimatePhaseTapChanger());
        }
        if (t2w3ToBeEstimated != null) {
            toBeEstimatedAdder.withRatioTapChanger3Status(t2w3ToBeEstimated.shouldEstimateRatioTapChanger())
                    .withPhaseTapChanger3Status(t2w3ToBeEstimated.shouldEstimatePhaseTapChanger());
        }
        toBeEstimatedAdder.add();
    }
}
