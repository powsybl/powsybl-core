/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.validation;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.validation.Validation;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class FinalValidation implements Validation {

    // Check equipments

    // Injections

    @Override
    public <B extends Battery & Validable> void checkBattery(B battery) {
        ValidationUtil.checkP0(battery, battery.getP0());
        ValidationUtil.checkQ0(battery, battery.getQ0());
        ValidationUtil.checkMinP(battery, battery.getMinP());
        ValidationUtil.checkMaxP(battery, battery.getMaxP());
        ValidationUtil.checkActivePowerLimits(battery, battery.getMinP(), battery.getMaxP());
    }

    @Override
    public <D extends DanglingLine & Validable> void checkDanglingLine(D danglingLine) {
        // Check Dangling line
        ValidationUtil.checkP0(danglingLine, danglingLine.getP0());
        ValidationUtil.checkQ0(danglingLine, danglingLine.getQ0());
        ValidationUtil.checkR(danglingLine, danglingLine.getR());
        ValidationUtil.checkX(danglingLine, danglingLine.getX());
        ValidationUtil.checkG(danglingLine, danglingLine.getG());
        ValidationUtil.checkB(danglingLine, danglingLine.getB());
        // Check generation
        DanglingLine.Generation g = danglingLine.getGeneration();
        ValidationUtil.checkActivePowerLimits(danglingLine, g.getMinP(), g.getMaxP());
        ValidationUtil.checkActivePowerSetpoint(danglingLine, g.getTargetP());
        ValidationUtil.checkVoltageControl(danglingLine, g.isVoltageRegulationOn(), g.getTargetV(), g.getTargetQ());
    }

    @Override
    public <G extends Generator & Validable> void checkGenerator(G generator) {
        ValidationUtil.checkEnergySource(generator, generator.getEnergySource());
        ValidationUtil.checkMinP(generator, generator.getMinP());
        ValidationUtil.checkMaxP(generator, generator.getMaxP());
        ValidationUtil.checkRegulatingTerminal(generator, generator.getRegulatingTerminal(), generator.getNetwork());
        ValidationUtil.checkActivePowerSetpoint(generator, generator.getTargetP());
        ValidationUtil.checkVoltageControl(generator, generator.isVoltageRegulatorOn(), generator.getTargetV(), generator.getTargetQ());
        ValidationUtil.checkActivePowerLimits(generator, generator.getMinP(), generator.getMaxP());
        ValidationUtil.checkRatedS(generator, generator.getRatedS());
    }

    @Override
    public <L extends Load & Validable> void checkLoad(L load) {
        ValidationUtil.checkLoadType(load, load.getLoadType());
        ValidationUtil.checkP0(load, load.getP0());
        ValidationUtil.checkQ0(load, load.getQ0());
    }

    @Override
    public <S extends ShuntCompensator & Validable> void checkShuntCompensator(S shuntCompensator) {
        ValidationUtil.checkRegulatingTerminal(shuntCompensator, shuntCompensator.getTerminal(), shuntCompensator.getNetwork());
        ValidationUtil.checkVoltageControl(shuntCompensator, shuntCompensator.isVoltageRegulatorOn(), shuntCompensator.getTargetV());
        ValidationUtil.checkTargetDeadband(shuntCompensator, "shunt compensator", shuntCompensator.isVoltageRegulatorOn(), shuntCompensator.getTargetDeadband());
        ValidationUtil.checkMaximumSectionCount(shuntCompensator, shuntCompensator.getMaximumSectionCount());

        if (shuntCompensator.getModelType() == ShuntCompensatorModelType.LINEAR) {
            ShuntCompensatorLinearModel model = shuntCompensator.getModel(ShuntCompensatorLinearModel.class);
            ValidationUtil.checkBPerSection(shuntCompensator, model.getBPerSection());
        }
        if (shuntCompensator.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            ShuntCompensatorNonLinearModel model = shuntCompensator.getModel(ShuntCompensatorNonLinearModel.class);
            model.getAllSections().forEach(s -> ValidationUtil.checkBPerSection(shuntCompensator, s.getB()));
        }
    }

    @Override
    public <S extends StaticVarCompensator & Validable> void checkStaticVarCompensator(S staticVarCompensator) {
        ValidationUtil.checkBmin(staticVarCompensator, staticVarCompensator.getBmin());
        ValidationUtil.checkBmax(staticVarCompensator, staticVarCompensator.getBmax());
        ValidationUtil.checkSvcRegulator(staticVarCompensator, staticVarCompensator.getVoltageSetpoint(), staticVarCompensator.getReactivePowerSetpoint(), staticVarCompensator.getRegulationMode());
        ValidationUtil.checkRegulatingTerminal(staticVarCompensator, staticVarCompensator.getRegulatingTerminal(), staticVarCompensator.getNetwork());
    }

    @Override
    public <S extends Switch & Validable> void checkSwitch(S sswitch) {
        if (sswitch.getKind() == null) {
            throw new ValidationException(sswitch, "kind is not set");
        }
    }

    // Branches & Transformers

    @Override
    public <L extends Line & Validable> void checkLine(L line) {
        ValidationUtil.checkR(line, line.getR());
        ValidationUtil.checkX(line, line.getX());
        ValidationUtil.checkG1(line, line.getG1());
        ValidationUtil.checkG2(line, line.getG2());
        ValidationUtil.checkB1(line, line.getB1());
        ValidationUtil.checkB2(line, line.getB2());
    }

    @Override
    public <T extends ThreeWindingsTransformer & Validable> void checkThreeWindingsTransformer(T twt) {
        int[] i = new int[1];
        i[0] = 1;
        twt.getLegs().forEach(leg -> {
            ValidationUtil.checkR(twt, leg.getR());
            ValidationUtil.checkX(twt, leg.getX());
            ValidationUtil.checkG(twt, leg.getG());
            ValidationUtil.checkB(twt, leg.getB());
            ValidationUtil.checkRatedU(twt, leg.getRatedU(), String.valueOf(i[0]));
            ValidationUtil.checkRatedS(twt, leg.getRatedS());

            RatioTapChanger rtc = leg.getRatioTapChanger();
            if (rtc != null) {
                checkRatioTapChanger(rtc, twt, twt.getNetwork(), String.valueOf(i[0]));
            }

            PhaseTapChanger ptc = leg.getPhaseTapChanger();
            if (ptc != null) {
                checkPhaseTapChanger(ptc, twt, twt.getNetwork(), String.valueOf(i[0]));
            }

            i[0]++;
        });
    }

    @Override
    public <T extends TwoWindingsTransformer & Validable> void checkTwoWindingsTransformer(T twt) {
        ValidationUtil.checkR(twt, twt.getR());
        ValidationUtil.checkX(twt, twt.getX());
        ValidationUtil.checkG(twt, twt.getG());
        ValidationUtil.checkB(twt, twt.getB());
        ValidationUtil.checkRatedU(twt, twt.getRatedU1(), "1");
        ValidationUtil.checkRatedU(twt, twt.getRatedU2(), "2");
        ValidationUtil.checkRatedS(twt, twt.getRatedS());

        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            checkRatioTapChanger(rtc, twt, twt.getNetwork(), "");
        }

        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            checkPhaseTapChanger(ptc, twt, twt.getNetwork(), "");
        }
    }

    private static void checkRatioTapChanger(RatioTapChanger rtc, Validable twt, Network network, String index) {
        if (rtc.getAllSteps().isEmpty()) {
            throw new ValidationException(twt, "a tap changer should have at least one step");
        }
        if (rtc.getTapPosition() == -1) {
            throw new ValidationException(twt, "tap position is not set");
        }
        int highTapPosition = rtc.getLowTapPosition() + rtc.getAllSteps().size() - 1;
        if (rtc.getTapPosition() < rtc.getLowTapPosition() || rtc.getTapPosition() > highTapPosition) {
            throw new ValidationException(twt, "incorrect tap position "
                    + rtc.getTapPosition() + " [" + rtc.getLowTapPosition() + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkRatioTapChangerRegulation(twt, rtc.isRegulating(), rtc.hasLoadTapChangingCapabilities(), rtc.getRegulationTerminal(), rtc.getTargetV(), network);
        ValidationUtil.checkTargetDeadband(twt, "ratio tap changer" + index, rtc.isRegulating(), rtc.getTargetDeadband());
        rtc.getAllSteps().values().forEach(step -> {
            checkStep(step, twt);
        });
    }

    private static void checkPhaseTapChanger(PhaseTapChanger ptc, Validable twt, Network network, String index) {
        if (ptc.getAllSteps().isEmpty()) {
            throw new ValidationException(twt, "a tap changer should have at least one step");
        }
        if (ptc.getTapPosition() == -1) {
            throw new ValidationException(twt, "tap position is not set");
        }
        int highTapPosition = ptc.getLowTapPosition() + ptc.getAllSteps().size() - 1;
        if (ptc.getTapPosition() < ptc.getLowTapPosition() || ptc.getTapPosition() > highTapPosition) {
            throw new ValidationException(twt, "incorrect tap position "
                    + ptc.getTapPosition() + " [" + ptc.getLowTapPosition() + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkPhaseTapChangerRegulation(twt, ptc.getRegulationMode(), ptc.getRegulationValue(), ptc.isRegulating(), ptc.getRegulationTerminal(), network);
        ValidationUtil.checkTargetDeadband(twt, "phase tap changer" + index, ptc.isRegulating(), ptc.getTargetDeadband());
        ptc.getAllSteps().values().forEach(step -> {
            if (Double.isNaN(step.getAlpha())) {
                throw new ValidationException(twt, "step alpha is not set");
            }
            checkStep(step, twt);
        });
    }

    private static void checkStep(TapChangerStep<?> step, Validable twt) {
        if (Double.isNaN(step.getRho())) {
            throw new ValidationException(twt, "step rho is not set");
        }
        if (Double.isNaN(step.getR())) {
            throw new ValidationException(twt, "step r is not set");
        }
        if (Double.isNaN(step.getX())) {
            throw new ValidationException(twt, "step x is not set");
        }
        if (Double.isNaN(step.getG())) {
            throw new ValidationException(twt, "step g is not set");
        }
        if (Double.isNaN(step.getB())) {
            throw new ValidationException(twt, "step b is not set");
        }
    }

    // Voltage levels

    @Override
    public <V extends VoltageLevel & Validable> void checkVoltageLevel(V voltageLevel) {
        ValidationUtil.checkNominalV(voltageLevel, voltageLevel.getNominalV());
        ValidationUtil.checkVoltageLimits(voltageLevel, voltageLevel.getLowVoltageLimit(), voltageLevel.getHighVoltageLimit());
        ValidationUtil.checkTopologyKind(voltageLevel, voltageLevel.getTopologyKind());
    }

    // DC components

    @Override
    public <H extends HvdcLine & Validable> void checkHvdcLine(H hvdcLine) {
        ValidationUtil.checkR(hvdcLine, hvdcLine.getR());
        ValidationUtil.checkConvertersMode(hvdcLine, hvdcLine.getConvertersMode());
        ValidationUtil.checkNominalV(hvdcLine, hvdcLine.getNominalV());
        ValidationUtil.checkHvdcActivePowerSetpoint(hvdcLine, hvdcLine.getActivePowerSetpoint());
        ValidationUtil.checkHvdcMaxP(hvdcLine, hvdcLine.getMaxP());
    }

    @Override
    public <L extends LccConverterStation & Validable> void checkLccConverterStation(L lccConverterStation) {
        ValidationUtil.checkLossFactor(lccConverterStation, lccConverterStation.getLossFactor());
        ValidationUtil.checkPowerFactor(lccConverterStation, lccConverterStation.getPowerFactor());
    }

    @Override
    public <V extends VscConverterStation & Validable> void checkVscConverterStation(V vscConverterStation) {
        ValidationUtil.checkLossFactor(vscConverterStation, vscConverterStation.getLossFactor());
        ValidationUtil.checkVoltageControl(vscConverterStation, vscConverterStation.isVoltageRegulatorOn(), vscConverterStation.getVoltageSetpoint(), vscConverterStation.getReactivePowerSetpoint());
    }

    // Check attributes

    @Override
    public void checkActivePowerLimits(Validable validable, double minP, double maxP) {
        // does nothing
    }

    @Override
    public void checkActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        // does nothing
    }

    @Override
    public void checkB(Validable validable, double b) {
        // does nothing
    }

    @Override
    public void checkB1(Validable validable, double b1) {
        // does nothing
    }

    @Override
    public void checkB2(Validable validable, double b2) {
        // does nothing
    }

    @Override
    public void checkBmax(Validable validable, double bMax) {
        // does nothing
    }

    @Override
    public void checkBmin(Validable validable, double bMin) {
        // does nothing
    }

    @Override
    public void checkBPerSection(Validable validable, double bPerSection) {
        // does nothing
    }

    @Override
    public void checkCaseDate(Validable validable, DateTime caseDate) {
        // does nothing
    }

    @Override
    public void checkConvertersMode(Validable validable, HvdcLine.ConvertersMode convertersMode) {
        // does nothing
    }

    @Override
    public void checkEnergySource(Validable validable, EnergySource energySource) {
        // does nothing
    }

    @Override
    public void checkForecastDistance(Validable validable, int forecastDistance) {
        // does nothing
    }

    @Override
    public void checkG(Validable validable, double g) {
        // does nothing
    }

    @Override
    public void checkG1(Validable validable, double g1) {
        // does nothing
    }

    @Override
    public void checkG2(Validable validable, double g2) {
        // does nothing
    }

    @Override
    public void checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        // does nothing
    }

    @Override
    public void checkHvdcMaxP(Validable validable, double maxP) {
        // does nothing
    }

    @Override
    public void checkLoadType(Validable validable, LoadType loadType) {
        // does nothing
    }

    @Override
    public void checkLossFactor(Validable validable, float lossFactor) {
        // does nothing
    }

    @Override
    public void checkMaxP(Validable validable, double maxP) {
        // does nothing
    }

    @Override
    public void checkMaximumSectionCount(Validable validable, int maximumSectionCount) {
        // does nothing
    }

    @Override
    public void checkMinP(Validable validable, double minP) {
        // does nothing
    }

    @Override
    public void checkNominalV(Validable validable, double nominalV) {
        // does nothing
    }

    @Override
    public void checkOnlyOneTapChangerRegulatingEnabled(Validable validable, Set<TapChanger> tapChangersNotIncludingTheModified, boolean regulating) {
        // does nothing
    }

    @Override
    public void checkP0(Validable validable, double p0) {
        // does nothing
    }

    @Override
    public void checkPermanentLimit(Validable validable, double permanentLimit) {
        // does nothing
    }

    @Override
    public void checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode, double regulationValue, boolean regulating, Terminal regulationTerminal, Network network) {
        // does nothing
    }

    @Override
    public void checkPowerFactor(Validable validable, double powerFactor) {
        // does nothing
    }

    @Override
    public void checkQ0(Validable validable, double q0) {
        // does nothing
    }

    @Override
    public void checkR(Validable validable, double r) {
        // does nothing
    }

    @Override
    public void checkRatedS(Validable validable, double ratedS) {
        // does nothing
    }

    @Override
    public void checkRatedU(Validable validable, double ratedU, String num) {
        // does nothing
    }

    @Override
    public void checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities, Terminal regulationTerminal, double targetV, Network network) {
        // does nothing
    }

    @Override
    public void checkRegulatingTerminal(Validable validable, Terminal terminal, Network network) {
        // does nothing
    }

    @Override
    public void checkSections(Validable validable, int sectionCount, int maxSectionCount) {
        // does nothing
    }

    @Override
    public void checkStep(Validable validable, double rho, double r, double x, double g, double b) {
        // does nothing
    }

    @Override
    public void checkStep(Validable validable, double alpha, double rho, double r, double x, double g, double b) {
        // does nothing
    }

    @Override
    public void checkSteps(Validable validable, Collection<? extends TapChangerStep<?>> steps) {
        // does nothing
    }

    @Override
    public void checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint, StaticVarCompensator.RegulationMode regulationMode) {
        // does nothing
    }

    @Override
    public void checkSwitchKind(Validable validable, SwitchKind kind) {
        // does nothing
    }

    @Override
    public void checkTapPosition(Validable validable, int lowTapPosition, Integer tapPosition, int stepsSize) {
        // does nothing
    }

    @Override
    public void checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband) {
        // does nothing
    }

    @Override
    public void checkTemporaryLimit(Validable validable, double value, Integer acceptableDuration) {
        // does nothing
    }

    @Override
    public void checkTemporaryLimitName(Validable validable, String name) {
        // does nothing
    }

    @Override
    public void checkTemporaryLimits(Validable validable, double permanentLimit, Map<Integer, LoadingLimits.TemporaryLimit> temporaryLimits) {
        // does nothing
    }

    @Override
    public void checkTopologyKind(Validable validable, TopologyKind topologyKind) {
        // does nothing
    }

    @Override
    public void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint) {
        // does nothing
    }

    @Override
    public void checkVoltageControl(Validable validable, boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint) {
        // does nothing
    }

    @Override
    public void checkVoltageLimits(Validable validable, double lowVoltageLimit, double highVoltageLimit) {
        // does nothing
    }

    @Override
    public void checkX(Validable validable, double x) {
        // does nothing
    }
}
