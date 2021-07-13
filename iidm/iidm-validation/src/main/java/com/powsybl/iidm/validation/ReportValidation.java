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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class ReportValidation implements Validation {

    private static final Logger LOG = LoggerFactory.getLogger(ReportValidation.class);

    // Check equipments

    // Injections

    @Override
    public <B extends Battery & Validable> void checkBattery(B battery) {
        checkP0(battery, battery.getP0());
        checkQ0(battery, battery.getQ0());
        checkMinP(battery, battery.getMinP());
        checkMaxP(battery, battery.getMaxP());
        checkActivePowerLimits(battery, battery.getMinP(), battery.getMaxP());
    }

    @Override
    public <D extends DanglingLine & Validable> void checkDanglingLine(D danglingLine) {
        // Check Dangling line
        checkP0(danglingLine, danglingLine.getP0());
        checkQ0(danglingLine, danglingLine.getQ0());
        checkR(danglingLine, danglingLine.getR());
        checkX(danglingLine, danglingLine.getX());
        checkG(danglingLine, danglingLine.getG());
        checkB(danglingLine, danglingLine.getB());
        // Check generation
        DanglingLine.Generation g = danglingLine.getGeneration();
        checkActivePowerLimits(danglingLine, g.getMinP(), g.getMaxP());
        checkActivePowerSetpoint(danglingLine, g.getTargetP());
        checkVoltageControl(danglingLine, g.isVoltageRegulationOn(), g.getTargetV(), g.getTargetQ());
    }

    @Override
    public <G extends Generator & Validable> void checkGenerator(G generator) {
        checkEnergySource(generator, generator.getEnergySource());
        checkMinP(generator, generator.getMinP());
        checkMaxP(generator, generator.getMaxP());
        checkRegulatingTerminal(generator, generator.getRegulatingTerminal(), generator.getNetwork());
        checkActivePowerSetpoint(generator, generator.getTargetP());
        checkVoltageControl(generator, generator.isVoltageRegulatorOn(), generator.getTargetV(), generator.getTargetQ());
        checkActivePowerLimits(generator, generator.getMinP(), generator.getMaxP());
        checkRatedS(generator, generator.getRatedS());
    }

    @Override
    public <L extends Load & Validable> void checkLoad(L load) {
        checkLoadType(load, load.getLoadType());
        checkP0(load, load.getP0());
        checkQ0(load, load.getQ0());
    }

    @Override
    public <S extends ShuntCompensator & Validable> void checkShuntCompensator(S shuntCompensator) {
        checkRegulatingTerminal(shuntCompensator, shuntCompensator.getTerminal(), shuntCompensator.getNetwork());
        checkVoltageControl(shuntCompensator, shuntCompensator.isVoltageRegulatorOn(), shuntCompensator.getTargetV());
        checkTargetDeadband(shuntCompensator, "shunt compensator", shuntCompensator.isVoltageRegulatorOn(), shuntCompensator.getTargetDeadband());
        checkMaximumSectionCount(shuntCompensator, shuntCompensator.getMaximumSectionCount());

        if (shuntCompensator.getModelType() == ShuntCompensatorModelType.LINEAR) {
            ShuntCompensatorLinearModel model = shuntCompensator.getModel(ShuntCompensatorLinearModel.class);
            checkBPerSection(shuntCompensator, model.getBPerSection());
        }
        if (shuntCompensator.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            ShuntCompensatorNonLinearModel model = shuntCompensator.getModel(ShuntCompensatorNonLinearModel.class);
            model.getAllSections().forEach(s -> checkBPerSection(shuntCompensator, s.getB()));
        }
    }

    @Override
    public <S extends StaticVarCompensator & Validable> void checkStaticVarCompensator(S staticVarCompensator) {
        checkBmin(staticVarCompensator, staticVarCompensator.getBmin());
        checkBmax(staticVarCompensator, staticVarCompensator.getBmax());
        checkSvcRegulator(staticVarCompensator, staticVarCompensator.getVoltageSetpoint(), staticVarCompensator.getReactivePowerSetpoint(), staticVarCompensator.getRegulationMode());
        checkRegulatingTerminal(staticVarCompensator, staticVarCompensator.getRegulatingTerminal(), staticVarCompensator.getNetwork());
    }

    @Override
    public <S extends Switch & Validable> void checkSwitch(S sswitch) {
        checkSwitchKind(sswitch, sswitch.getKind());
    }

    // Branches & Transformers

    @Override
    public <L extends Line & Validable> void checkLine(L line) {
        checkR(line, line.getR());
        checkX(line, line.getX());
        checkG1(line, line.getG1());
        checkG2(line, line.getG2());
        checkB1(line, line.getB1());
        checkB2(line, line.getB2());
    }

    @Override
    public <T extends ThreeWindingsTransformer & Validable> void checkThreeWindingsTransformer(T twt) {
        int[] i = new int[1];
        i[0] = 1;
        twt.getLegs().forEach(leg -> {
            checkR(twt, leg.getR());
            checkX(twt, leg.getX());
            checkG(twt, leg.getG());
            checkB(twt, leg.getB());
            checkRatedU(twt, leg.getRatedU(), String.valueOf(i[0]));
            checkRatedS(twt, leg.getRatedS());

            RatioTapChanger rtc = leg.getRatioTapChanger();
            if (rtc != null) {
                checkSteps(twt, rtc.getAllSteps().values());
                checkTapPosition(twt, rtc.getLowTapPosition(), rtc.getTapPosition(), rtc.getAllSteps().size() - 1);
                checkRatioTapChangerRegulation(twt, rtc.isRegulating(), rtc.hasLoadTapChangingCapabilities(), rtc.getRegulationTerminal(), rtc.getTargetV(), twt.getNetwork());
                checkTargetDeadband(twt, "ratio tap changer" + i[0], rtc.isRegulating(), rtc.getTargetDeadband());
                rtc.getAllSteps().values().forEach(step -> checkStep(twt, step.getRho(), step.getR(), step.getX(), step.getG(), step.getB()));
            }

            PhaseTapChanger ptc = leg.getPhaseTapChanger();
            if (ptc != null) {
                checkSteps(twt, ptc.getAllSteps().values());
                checkTapPosition(twt, ptc.getLowTapPosition(), ptc.getTapPosition(), ptc.getAllSteps().size() - 1);
                checkPhaseTapChangerRegulation(twt, ptc.getRegulationMode(), ptc.getRegulationValue(), ptc.isRegulating(), ptc.getRegulationTerminal(), twt.getNetwork());
                checkTargetDeadband(twt, "phase tap changer" + i[0], ptc.isRegulating(), ptc.getTargetDeadband());
                ptc.getAllSteps().values().forEach(step -> checkStep(twt, step.getAlpha(), step.getRho(), step.getR(), step.getX(), step.getG(), step.getB()));
            }

            i[0]++;
        });
    }

    @Override
    public <T extends TwoWindingsTransformer & Validable> void checkTwoWindingsTransformer(T twt) {
        checkR(twt, twt.getR());
        checkX(twt, twt.getX());
        checkG(twt, twt.getG());
        checkB(twt, twt.getB());
        checkRatedU(twt, twt.getRatedU1(), "1");
        checkRatedU(twt, twt.getRatedU2(), "2");
        checkRatedS(twt, twt.getRatedS());

        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            checkSteps(twt, rtc.getAllSteps().values());
            checkTapPosition(twt, rtc.getLowTapPosition(), rtc.getTapPosition(), rtc.getAllSteps().size() - 1);
            checkRatioTapChangerRegulation(twt, rtc.isRegulating(), rtc.hasLoadTapChangingCapabilities(), rtc.getRegulationTerminal(), rtc.getTargetV(), twt.getNetwork());
            checkTargetDeadband(twt, "ratio tap changer", rtc.isRegulating(), rtc.getTargetDeadband());
            rtc.getAllSteps().values().forEach(step -> checkStep(twt, step.getRho(), step.getR(), step.getX(), step.getG(), step.getB()));
        }

        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            checkSteps(twt, ptc.getAllSteps().values());
            checkTapPosition(twt, ptc.getLowTapPosition(), ptc.getTapPosition(), ptc.getAllSteps().size() - 1);
            checkPhaseTapChangerRegulation(twt, ptc.getRegulationMode(), ptc.getRegulationValue(), ptc.isRegulating(), ptc.getRegulationTerminal(), twt.getNetwork());
            checkTargetDeadband(twt, "phase tap changer", ptc.isRegulating(), ptc.getTargetDeadband());
            ptc.getAllSteps().values().forEach(step -> checkStep(twt, step.getAlpha(), step.getRho(), step.getR(), step.getX(), step.getG(), step.getB()));
        }
    }

    // Voltage Levels

    @Override
    public <V extends VoltageLevel & Validable> void checkVoltageLevel(V voltageLevel) {
        checkNominalV(voltageLevel, voltageLevel.getNominalV());
        checkVoltageLimits(voltageLevel, voltageLevel.getLowVoltageLimit(), voltageLevel.getHighVoltageLimit());
        checkTopologyKind(voltageLevel, voltageLevel.getTopologyKind());
    }

    // DC Components

    @Override
    public <H extends HvdcLine & Validable> void checkHvdcLine(H hvdcLine) {
        checkR(hvdcLine, hvdcLine.getR());
        checkConvertersMode(hvdcLine, hvdcLine.getConvertersMode());
        checkNominalV(hvdcLine, hvdcLine.getNominalV());
        checkHvdcActivePowerSetpoint(hvdcLine, hvdcLine.getActivePowerSetpoint());
        checkHvdcMaxP(hvdcLine, hvdcLine.getMaxP());
    }

    @Override
    public <L extends LccConverterStation & Validable> void checkLccConverterStation(L lccConverterStation) {
        checkLossFactor(lccConverterStation, lccConverterStation.getLossFactor());
        checkPowerFactor(lccConverterStation, lccConverterStation.getPowerFactor());
    }

    @Override
    public <V extends VscConverterStation & Validable> void checkVscConverterStation(V vscConverterStation) {
        checkLossFactor(vscConverterStation, vscConverterStation.getLossFactor());
        checkVoltageControl(vscConverterStation, vscConverterStation.isVoltageRegulatorOn(), vscConverterStation.getVoltageSetpoint(), vscConverterStation.getReactivePowerSetpoint());
    }

    // Check attributes

    @Override
    public void checkActivePowerLimits(Validable validable, double minP, double maxP) {
        if (minP > maxP) {
            LOG.error("{}invalid active limits [{}, {}]", validable.getMessageHeader(), minP, maxP);
        }
    }

    @Override
    public void checkActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        if (Double.isNaN(activePowerSetpoint)) {
            createInvalidValueLog(validable, activePowerSetpoint, "active power setpoint");
        }
    }

    @Override
    public void checkB(Validable validable, double b) {
        if (Double.isNaN(b)) {
            LOG.error("{}b is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkB1(Validable validable, double b1) {
        if (Double.isNaN(b1)) {
            LOG.error("{}b1 is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkB2(Validable validable, double b2) {
        if (Double.isNaN(b2)) {
            LOG.error("{}b2 is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkBmax(Validable validable, double bMax) {
        if (Double.isNaN(bMax)) {
            LOG.error("{}bMax is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkBmin(Validable validable, double bMin) {
        if (Double.isNaN(bMin)) {
            LOG.error("{}bMin is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkBPerSection(Validable validable, double bPerSection) {
        if (Double.isNaN(bPerSection)) {
            LOG.error("{}section susceptance is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkCaseDate(Validable validable, DateTime caseDate) {
        if (caseDate == null) {
            LOG.error("{}case date is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkConvertersMode(Validable validable, HvdcLine.ConvertersMode convertersMode) {
        if (convertersMode == null) {
            LOG.error("{}converter mode is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkEnergySource(Validable validable, EnergySource energySource) {
        if (energySource == null) {
            LOG.error("{}energy source is not set", validable.getMessageHeader());
        }
    }

    @Override
    public void checkForecastDistance(Validable validable, int forecastDistance) {
        if (forecastDistance < 0) {
            LOG.error("{}forecast distance < 0", validable.getMessageHeader());
        }
    }

    @Override
    public void checkG(Validable validable, double g) {
        if (Double.isNaN(g)) {
            LOG.error("{}g is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkG1(Validable validable, double g1) {
        if (Double.isNaN(g1)) {
            LOG.error("{}g1 is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkG2(Validable validable, double g2) {
        if (Double.isNaN(g2)) {
            LOG.error("{}g2 is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        if (Double.isNaN(activePowerSetpoint)) {
            createInvalidValueLog(validable, activePowerSetpoint, "active power setpoint");
        } else if (activePowerSetpoint < 0) {
            createInvalidValueLog(validable, activePowerSetpoint, "active power setpoint should not be negative");
        }
    }

    @Override
    public void checkHvdcMaxP(Validable validable, double maxP) {
        if (Double.isNaN(maxP)) {
            createInvalidValueLog(validable, maxP, "maximum P");
        } else if (maxP < 0) {
            createInvalidValueLog(validable, maxP, "maximum P");
        }
    }

    @Override
    public void checkLoadType(Validable validable, LoadType loadType) {
        if (loadType == null) {
            LOG.error("{}load type is null", validable.getMessageHeader());
        }
    }

    @Override
    public void checkLossFactor(Validable validable, float lossFactor) {
        if (Double.isNaN(lossFactor)) {
            LOG.error("{}loss factor is invalid", validable.getMessageHeader());
        } else if (lossFactor < 0 || lossFactor > 100) {
            LOG.error("{}loss factor must be >= 0 and <= 100", validable.getMessageHeader());
        }
    }

    @Override
    public void checkMaxP(Validable validable, double maxP) {
        if (Double.isNaN(maxP)) {
            createInvalidValueLog(validable, maxP, "maximum P");
        }
    }

    @Override
    public void checkMaximumSectionCount(Validable validable, int maximumSectionCount) {
        if (maximumSectionCount <= 0) {
            LOG.error("{}the maximum number of section ({}) should be greater than 0", validable.getMessageHeader(), maximumSectionCount);
        }
    }

    @Override
    public void checkMinP(Validable validable, double minP) {
        if (Double.isNaN(minP)) {
            createInvalidValueLog(validable, minP, "minimum P");
        }
    }

    @Override
    public void checkNominalV(Validable validable, double nominalV) {
        if (Double.isNaN(nominalV) || nominalV <= 0) {
            LOG.error("{}nominal voltage is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkOnlyOneTapChangerRegulatingEnabled(Validable validable, Set<TapChanger> tapChangersNotIncludingTheModified, boolean regulating) {
        if (regulating && tapChangersNotIncludingTheModified.stream().anyMatch(TapChanger::isRegulating)) {
            LOG.error("{}Only one regulating control enabled is allowed", validable.getMessageHeader());
        }
    }

    @Override
    public void checkP0(Validable validable, double p0) {
        if (Double.isNaN(p0)) {
            LOG.error("{}p0 is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkPermanentLimit(Validable validable, double permanentLimit) {
        // TODO: if (Double.isNaN(permanentLimit) || permanentLimit <= 0) {
        if (permanentLimit <= 0) {
            LOG.error("{}permanent limit must be defined and be > 0", validable.getMessageHeader());
        }
    }

    @Override
    public void checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode, double regulationValue, boolean regulating, Terminal regulationTerminal, Network network) {
        if (regulationMode == null) {
            LOG.error("{}phase regulation mode is not set", validable.getMessageHeader());
        }
        if (regulating) {
            if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && Double.isNaN(regulationValue)) {
                LOG.error("{}phase regulation is on and threshold/setpoint is not set", validable.getMessageHeader());
            }
            if (regulationMode != PhaseTapChanger.RegulationMode.FIXED_TAP && regulationTerminal == null) {
                LOG.error("{}phase regulation is on and regulated terminal is not set", validable.getMessageHeader());
            }
            if (regulationMode == PhaseTapChanger.RegulationMode.FIXED_TAP) {
                LOG.error("{}phase regulation cannot be on if mode is FIXED", validable.getMessageHeader());
            }
        }
        if (regulationTerminal != null && regulationTerminal.getVoltageLevel().getNetwork() != network) {
            LOG.error("{}phase regulation terminal is not part of the network", validable.getMessageHeader());
        }
    }

    @Override
    public void checkPowerFactor(Validable validable, double powerFactor) {
        if (Double.isNaN(powerFactor)) {
            LOG.error("{}power factor is invalid", validable.getMessageHeader());
        } else if (Math.abs(powerFactor) > 1) {
            LOG.error("{}power factor is invalid, it should be between -1 and 1", validable.getMessageHeader());
        }
    }

    @Override
    public void checkQ0(Validable validable, double q0) {
        if (Double.isNaN(q0)) {
            LOG.error("{}q0 is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkR(Validable validable, double r) {
        if (Double.isNaN(r)) {
            LOG.error("{}r is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkRatedS(Validable validable, double ratedS) {
        if (!Double.isNaN(ratedS) && ratedS <= 0) {
            LOG.error("{}Invalid value of rated S {}", validable.getMessageHeader(), ratedS);
        }
    }

    @Override
    public void checkRatedU(Validable validable, double ratedU, String num) {
        if (Double.isNaN(ratedU)) {
            LOG.error("{}rated U{} is invalid", validable.getMessageHeader(), num);
        }
    }

    @Override
    public void checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities, Terminal regulationTerminal, double targetV, Network network) {
        if (regulating) {
            if (Double.isNaN(targetV)) {
                logErrorOrWarningForRtc(validable, loadTapChangingCapabilities, "a target voltage has to be set for a regulating ratio tap changer");
            }
            if (targetV <= 0) {
                logErrorOrWarningForRtc(validable, loadTapChangingCapabilities, "bad target voltage " + targetV);
            }
            if (regulationTerminal == null) {
                logErrorOrWarningForRtc(validable, loadTapChangingCapabilities, "a regulation terminal has to be set for a regulating ratio tap changer");
            }
            if (regulationTerminal != null && regulationTerminal.getVoltageLevel().getNetwork() != network) {
                logErrorOrWarningForRtc(validable, loadTapChangingCapabilities, "regulation terminal is not part of the network");
            }
        }
    }

    private static void logErrorOrWarningForRtc(Validable validable, boolean loadTapChangingCapabilities, String msg) {
        if (loadTapChangingCapabilities) {
            LOG.error("{}{}", validable.getMessageHeader(), msg);
        } else {
            LOG.warn("{}{}", validable.getMessageHeader(), msg);
        }
    }

    @Override
    public void checkRegulatingTerminal(Validable validable, Terminal terminal, Network network) {
        if (terminal != null && terminal.getVoltageLevel().getNetwork() != network) {
            LOG.error("{}regulating terminal is not part of the network", validable.getMessageHeader());
        }
    }

    @Override
    public void checkSections(Validable validable, int sectionCount, int maxSectionCount) {
        if (sectionCount < 0) {
            LOG.error("{}the current number of section ({}) should be greater than or equal to 0", validable.getMessageHeader(), sectionCount);
        }
        checkMaximumSectionCount(validable, maxSectionCount);
        if (sectionCount > maxSectionCount) {
            LOG.error("{}the current number ({}) of section should be lesser than the maximum number of section ({})", validable.getMessageHeader(), sectionCount, maxSectionCount);
        }
    }

    @Override
    public void checkStep(Validable validable, double rho, double r, double x, double g, double b) {
        if (Double.isNaN(rho)) {
            LOG.error("{}step rho is not set", validable.getMessageHeader());
        }
        if (Double.isNaN(r)) {
            LOG.error("{}step r is not set", validable.getMessageHeader());
        }
        if (Double.isNaN(x)) {
            LOG.error("{}step x is not set", validable.getMessageHeader());
        }
        if (Double.isNaN(g)) {
            LOG.error("{}step g is not set", validable.getMessageHeader());
        }
        if (Double.isNaN(b)) {
            LOG.error("{}step b is not set", validable.getMessageHeader());
        }
    }

    @Override
    public void checkStep(Validable validable, double alpha, double rho, double r, double x, double g, double b) {
        if (Double.isNaN(alpha)) {
            LOG.error("{}step alpha is not set", validable.getMessageHeader());
        }
        checkStep(validable, rho, r, x, g, b);
    }

    @Override
    public void checkSteps(Validable validable, Collection<? extends TapChangerStep<?>> steps) {
        if (steps.isEmpty()) {
            LOG.error("{}a tap changer should have at least one step", validable.getMessageHeader());
        }
    }

    @Override
    public void checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint, StaticVarCompensator.RegulationMode regulationMode) {
        if (regulationMode == null) {
            LOG.error("{}Regulation mode is invalid", validable.getMessageHeader());
            return;
        }
        switch (regulationMode) {
            case VOLTAGE:
                if (Double.isNaN(voltageSetpoint)) {
                    createInvalidValueLog(validable, voltageSetpoint, "voltage setpoint");
                }
                break;

            case REACTIVE_POWER:
                if (Double.isNaN(reactivePowerSetpoint)) {
                    createInvalidValueLog(validable, reactivePowerSetpoint, "reactive power setpoint");
                }
                break;

            case OFF:
                // nothing to check
                break;

            default:
                throw new AssertionError();
        }
    }

    @Override
    public void checkSwitchKind(Validable validable, SwitchKind kind) {
        if (kind == null) {
            LOG.error("{}kind is not set", validable.getMessageHeader());
        }
    }

    @Override
    public void checkTapPosition(Validable validable, int lowTapPosition, Integer tapPosition, int stepsSize) {
        if (tapPosition == null) {
            LOG.error("{}tap position is not set", validable.getMessageHeader());
            return;
        }
        int highTapPosition = lowTapPosition + stepsSize;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            LOG.error("{}incorrect tap position {} [{}, {}]", validable.getMessageHeader(), tapPosition, lowTapPosition, highTapPosition);
        }
    }

    @Override
    public void checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband) {
        if (regulating && Double.isNaN(targetDeadband)) {
            LOG.error("{}Undefined value for target deadband of regulating {}", validable.getMessageHeader(), validableType);
        }
        if (targetDeadband < 0) {
            LOG.error("{}Unexpected value for target deadband of {}: {} < 0", validable.getMessageHeader(), validableType, targetDeadband);
        }
    }

    @Override
    public void checkTemporaryLimit(Validable validable, double value, Integer acceptableDuration) {
        if (Double.isNaN(value)) {
            LOG.error("{}temporary limit value is not set", validable.getMessageHeader());
        }
        if (value <= 0) {
            LOG.error("{}temporary limit value must be > 0", validable.getMessageHeader());
        }
        if (acceptableDuration == null) {
            LOG.error("{}acceptable duration is not set", validable.getMessageHeader());
            return;
        }
        if (acceptableDuration < 0) {
            LOG.error("{}acceptable duration must be >=0", validable.getMessageHeader());
        }
    }

    @Override
    public void checkTemporaryLimitName(Validable validable, String name) {
        if (name == null) {
            LOG.error("{}name is not set", validable.getMessageHeader());
        }
    }

    @Override
    public void checkTemporaryLimits(Validable validable, double permanentLimit, Map<Integer, LoadingLimits.TemporaryLimit> temporaryLimits) {
        // check temporary limits are consistents with permanent
        double previousLimit = Double.NaN;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits.values()) { // iterate in ascending order
            if (tl.getValue() <= permanentLimit) {
                LOG.debug("{}, temporary limit should be greater than permanent limit", validable.getMessageHeader());
            }
            if (Double.isNaN(previousLimit)) {
                previousLimit = tl.getValue();
            } else if (tl.getValue() <= previousLimit) {
                LOG.debug("{} : temporary limits should be in ascending value order", validable.getMessageHeader());
            }
        }
        // check name unicity
        temporaryLimits.values().stream()
                .collect(Collectors.groupingBy(LoadingLimits.TemporaryLimit::getName))
                .forEach((name, temporaryLimits1) -> {
                    if (temporaryLimits1.size() > 1) {
                        LOG.error("{}{}temporary limits have the same name {}", validable.getMessageHeader(), temporaryLimits1.size(), name);
                    }
                });
    }

    @Override
    public void checkTopologyKind(Validable validable, TopologyKind topologyKind) {
        if (topologyKind == null) {
            LOG.error("{}topology is invalid", validable.getMessageHeader());
        }
    }

    @Override
    public void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint) {
        checkVoltageControlBool(validable, voltageRegulatorOn, voltageSetpoint);
    }

    @Override
    public void checkVoltageControl(Validable validable, boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint) {
        if (checkVoltageControlBool(validable, voltageRegulatorOn, voltageSetpoint) && Double.isNaN(reactivePowerSetpoint)) {
            createInvalidValueLog(validable, reactivePowerSetpoint, "reactive power setpoint", "voltage regulator is off");
        }
    }

    private static boolean checkVoltageControlBool(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint) {
        if (voltageRegulatorOn == null) {
            LOG.error("{}voltage regulator status is not set", validable.getMessageHeader());
            return false;
        }
        boolean bVoltageRegulatorOn = voltageRegulatorOn; // make sonar happy java:S5411 Boxed "Boolean" should be avoided in boolean expressions
        if (bVoltageRegulatorOn) {
            if (Double.isNaN(voltageSetpoint) || voltageSetpoint <= 0) {
                createInvalidValueLog(validable, voltageSetpoint, "voltage setpoint", "voltage regulator is on");
            }
            return false;
        }
        return true;
    }

    @Override
    public void checkVoltageLimits(Validable validable, double lowVoltageLimit, double highVoltageLimit) {
        if (lowVoltageLimit < 0) {
            LOG.error("{}low voltage limit is < 0", validable.getMessageHeader());
        }
        if (highVoltageLimit < 0) {
            LOG.error("{}high voltage limit is < 0", validable.getMessageHeader());
        }
        if (lowVoltageLimit > highVoltageLimit) {
            LOG.error("{}Inconsistent voltage limit range [{}, {}]", validable.getMessageHeader(), lowVoltageLimit, highVoltageLimit);
        }
    }

    @Override
    public void checkX(Validable validable, double x) {
        if (Double.isNaN(x)) {
            LOG.error("{}x is invalid", validable.getMessageHeader());
        }
    }

    private static void createInvalidValueLog(Validable validable, double value, String valueName) {
        createInvalidValueLog(validable, value, valueName, null);
    }

    private static void createInvalidValueLog(Validable validable, double value, String valueName, String reason) {
        String r = reason == null ? "" : " (" + reason + ")";
        LOG.error("{}invalid value ({}) for {}{}]", validable.getMessageHeader(), value, valueName, r);
    }
}
