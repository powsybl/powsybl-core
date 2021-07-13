/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.validation;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface Validation {

    // Check equipments

    // Injections
    <B extends Battery & Validable> void checkBattery(B battery);

    <D extends DanglingLine & Validable> void checkDanglingLine(D danglingLine);

    <G extends Generator & Validable> void checkGenerator(G generator);

    <L extends Load & Validable> void checkLoad(L load);

    <S extends ShuntCompensator & Validable> void checkShuntCompensator(S shuntCompensator);

    <S extends StaticVarCompensator & Validable> void checkStaticVarCompensator(S staticVarCompensator);

    // Switches

    <S extends Switch & Validable> void checkSwitch(S sswitch);

    // Branches & Transformers

    <L extends Line & Validable> void checkLine(L line);

    <T extends ThreeWindingsTransformer & Validable> void checkThreeWindingsTransformer(T twt);

    <T extends TwoWindingsTransformer & Validable> void checkTwoWindingsTransformer(T twt);

    // Voltage Level
    <V extends VoltageLevel & Validable> void checkVoltageLevel(V voltageLevel);

    // DC components

    <H extends HvdcLine & Validable> void checkHvdcLine(H hvdcLine);

    <L extends LccConverterStation & Validable> void checkLccConverterStation(L lccConverterStation);

    <V extends VscConverterStation & Validable> void checkVscConverterStation(V vscConverterStation);

    // Check attributes

    void checkActivePowerLimits(Validable validable, double minP, double maxP);

    void checkActivePowerSetpoint(Validable validable, double activePowerSetpoint);

    void checkB(Validable validable, double b);

    void checkB1(Validable validable, double b1);

    void checkB2(Validable validable, double b2);

    void checkBmax(Validable validable, double bMax);

    void checkBmin(Validable validable, double bMin);

    void checkBPerSection(Validable validable, double bPerSection);

    void checkCaseDate(Validable validable, DateTime caseDate);

    void checkConvertersMode(Validable validable, HvdcLine.ConvertersMode convertersMode);

    void checkEnergySource(Validable validable, EnergySource energySource);

    void checkForecastDistance(Validable validable, int forecastDistance);

    void checkG(Validable validable, double g);

    void checkG1(Validable validable, double g1);

    void checkG2(Validable validable, double g2);

    void checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint);

    void checkHvdcMaxP(Validable validable, double maxP);

    void checkLoadType(Validable validable, LoadType loadType);

    void checkLossFactor(Validable validable, float lossFactor);

    void checkMaxP(Validable validable, double maxP);

    void checkMaximumSectionCount(Validable validable, int maximumSectionCount);

    void checkMinP(Validable validable, double minP);

    void checkNominalV(Validable validable, double nominalV);

    void checkOnlyOneTapChangerRegulatingEnabled(Validable validable,
                                                 Set<TapChanger> tapChangersNotIncludingTheModified, boolean regulating);

    void checkP0(Validable validable, double p0);

    void checkPermanentLimit(Validable validable, double permanentLimit);

    void checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                        double regulationValue, boolean regulating, Terminal regulationTerminal,
                                        Network network);

    void checkPowerFactor(Validable validable, double powerFactor);

    void checkQ0(Validable validable, double q0);

    void checkR(Validable validable, double r);

    void checkRatedS(Validable validable, double ratedS);

    void checkRatedU(Validable validable, double ratedU, String num);

    void checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
                                        Terminal regulationTerminal, double targetV, Network network);

    void checkRegulatingTerminal(Validable validable, Terminal terminal, Network network);

    void checkSections(Validable validable, int sectionCount, int maxSectionCount);

    void checkStep(Validable validable, double rho, double r, double x, double g, double b);

    void checkStep(Validable validable, double alpha, double rho, double r, double x, double g, double b);

    void checkSteps(Validable validable, Collection<? extends TapChangerStep<?>> steps);

    void checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint, StaticVarCompensator.RegulationMode regulationMode);

    void checkSwitchKind(Validable validable, SwitchKind kind);

    void checkTapPosition(Validable validable, int lowTapPosition, Integer tapPosition, int stepsSize);

    void checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband);

    void checkTemporaryLimit(Validable validable, double value, Integer acceptableDuration);

    void checkTemporaryLimitName(Validable validable, String name);

    void checkTemporaryLimits(Validable validable, double permanentLimit, Map<Integer, LoadingLimits.TemporaryLimit> temporaryLimits);

    void checkTopologyKind(Validable validable, TopologyKind topologyKind);

    void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint);

    void checkVoltageControl(Validable validable, boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint);

    void checkVoltageLimits(Validable validable, double lowVoltageLimit, double highVoltageLimit);

    void checkX(Validable validable, double x);

    static Validation getDefault() {
        return get(null);
    }

    static Validation get(String name) {
        return PlatformConfigNamedProvider.Finder.find(name, "network-validation", ValidationProvider.class, PlatformConfig.defaultConfig()).getValidation();
    }
}
