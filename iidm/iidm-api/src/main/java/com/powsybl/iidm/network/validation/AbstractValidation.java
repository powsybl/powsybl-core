/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.validation;

import com.powsybl.iidm.network.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public abstract class AbstractValidation implements Validation {

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
}
