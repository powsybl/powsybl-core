/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.validation;

import com.powsybl.iidm.network.*;
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
public class DefaultValidation extends AbstractValidation implements Validation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultValidation.class);

    // Check attributes

    @Override
    public void checkActivePowerLimits(Validable validable, double minP, double maxP) {
        ValidationUtil.checkActivePowerLimits(validable, minP, maxP);
    }

    @Override
    public void checkActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        ValidationUtil.checkActivePowerSetpoint(validable, activePowerSetpoint);
    }

    @Override
    public void checkB(Validable validable, double b) {
        ValidationUtil.checkB(validable, b);
    }

    @Override
    public void checkB1(Validable validable, double b1) {
        ValidationUtil.checkB1(validable, b1);
    }

    @Override
    public void checkB2(Validable validable, double b2) {
        ValidationUtil.checkB2(validable, b2);
    }

    @Override
    public void checkBmax(Validable validable, double bMax) {
        ValidationUtil.checkBmax(validable, bMax);
    }

    @Override
    public void checkBmin(Validable validable, double bMin) {
        ValidationUtil.checkBmin(validable, bMin);
    }

    @Override
    public void checkBPerSection(Validable validable, double bPerSection) {
        ValidationUtil.checkBPerSection(validable, bPerSection);
    }

    @Override
    public void checkCaseDate(Validable validable, DateTime caseDate) {
        ValidationUtil.checkCaseDate(validable, caseDate);
    }

    @Override
    public void checkConvertersMode(Validable validable, HvdcLine.ConvertersMode convertersMode) {
        ValidationUtil.checkConvertersMode(validable, convertersMode);
    }

    @Override
    public void checkEnergySource(Validable validable, EnergySource energySource) {
        ValidationUtil.checkEnergySource(validable, energySource);
    }

    @Override
    public void checkForecastDistance(Validable validable, int forecastDistance) {
        ValidationUtil.checkForecastDistance(validable, forecastDistance);
    }

    @Override
    public void checkG(Validable validable, double g) {
        ValidationUtil.checkG(validable, g);
    }

    @Override
    public void checkG1(Validable validable, double g1) {
        ValidationUtil.checkG1(validable, g1);
    }

    @Override
    public void checkG2(Validable validable, double g2) {
        ValidationUtil.checkG2(validable, g2);
    }

    @Override
    public void checkHvdcActivePowerSetpoint(Validable validable, double activePowerSetpoint) {
        ValidationUtil.checkHvdcActivePowerSetpoint(validable, activePowerSetpoint);
    }

    @Override
    public void checkHvdcMaxP(Validable validable, double maxP) {
        ValidationUtil.checkHvdcMaxP(validable, maxP);
    }

    @Override
    public void checkLoadType(Validable validable, LoadType loadType) {
        ValidationUtil.checkLoadType(validable, loadType);
    }

    @Override
    public void checkLossFactor(Validable validable, float lossFactor) {
        ValidationUtil.checkLossFactor(validable, lossFactor);
    }

    @Override
    public void checkMaxP(Validable validable, double maxP) {
        ValidationUtil.checkMaxP(validable, maxP);
    }

    @Override
    public void checkMaximumSectionCount(Validable validable, int maximumSectionCount) {
        ValidationUtil.checkMaximumSectionCount(validable, maximumSectionCount);
    }

    @Override
    public void checkMinP(Validable validable, double minP) {
        ValidationUtil.checkMinP(validable, minP);
    }

    @Override
    public void checkNominalV(Validable validable, double nominalV) {
        ValidationUtil.checkNominalV(validable, nominalV);
    }

    @Override
    public void checkOnlyOneTapChangerRegulatingEnabled(Validable validable, Set<TapChanger> tapChangersNotIncludingTheModified, boolean regulating) {
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(validable, tapChangersNotIncludingTheModified, regulating);
    }

    @Override
    public void checkP0(Validable validable, double p0) {
        ValidationUtil.checkP0(validable, p0);
    }

    @Override
    public void checkPermanentLimit(Validable validable, double permanentLimit) {
        ValidationUtil.checkPermanentLimit(validable, permanentLimit);
    }

    @Override
    public void checkPhaseTapChangerRegulation(Validable validable, PhaseTapChanger.RegulationMode regulationMode,
                                               double regulationValue, boolean regulating, Terminal regulationTerminal, Network network) {
        ValidationUtil.checkPhaseTapChangerRegulation(validable, regulationMode, regulationValue, regulating, regulationTerminal, network);
    }

    @Override
    public void checkPowerFactor(Validable validable, double powerFactor) {
        ValidationUtil.checkPowerFactor(validable, powerFactor);
    }

    @Override
    public void checkQ0(Validable validable, double q0) {
        ValidationUtil.checkQ0(validable, q0);
    }

    @Override
    public void checkR(Validable validable, double r) {
        ValidationUtil.checkR(validable, r);
    }

    @Override
    public void checkRatedS(Validable validable, double ratedS) {
        ValidationUtil.checkRatedS(validable, ratedS);
    }

    @Override
    public void checkRatedU(Validable validable, double ratedU, String num) {
        ValidationUtil.checkRatedU(validable, ratedU, num);
    }

    @Override
    public void checkRatioTapChangerRegulation(Validable validable, boolean regulating, boolean loadTapChangingCapabilities,
                                               Terminal regulationTerminal, double targetV, Network network) {
        ValidationUtil.checkRatioTapChangerRegulation(validable, regulating, loadTapChangingCapabilities, regulationTerminal, targetV, network);
    }

    @Override
    public void checkRegulatingTerminal(Validable validable, Terminal terminal, Network network) {
        ValidationUtil.checkRegulatingTerminal(validable, terminal, network);
    }

    @Override
    public void checkSections(Validable validable, int sectionCount, int maxSectionCount) {
        ValidationUtil.checkSections(validable, sectionCount, maxSectionCount);
    }

    @Override
    public void checkStep(Validable validable, double rho, double r, double x, double g, double b) {
        if (Double.isNaN(rho)) {
            throw new ValidationException(validable, "step rho is not set");
        }
        if (Double.isNaN(r)) {
            throw new ValidationException(validable, "step r is not set");
        }
        if (Double.isNaN(x)) {
            throw new ValidationException(validable, "step x is not set");
        }
        if (Double.isNaN(g)) {
            throw new ValidationException(validable, "step g is not set");
        }
        if (Double.isNaN(b)) {
            throw new ValidationException(validable, "step b is not set");
        }
    }

    @Override
    public void checkStep(Validable validable, double alpha, double rho, double r, double x, double g, double b) {
        if (Double.isNaN(alpha)) {
            throw new ValidationException(validable, "step alpha is not set");
        }
        checkStep(validable, rho, r, x, g, b);
    }

    @Override
    public void checkSteps(Validable validable, Collection<? extends TapChangerStep<?>> steps) {
        if (steps.isEmpty()) {
            throw new ValidationException(validable, "a tap changer should have at least one step");
        }
    }

    @Override
    public void checkSvcRegulator(Validable validable, double voltageSetpoint, double reactivePowerSetpoint, StaticVarCompensator.RegulationMode regulationMode) {
        ValidationUtil.checkSvcRegulator(validable, voltageSetpoint, reactivePowerSetpoint, regulationMode);
    }

    @Override
    public void checkSwitchKind(Validable validable, SwitchKind kind) {
        if (kind == null) {
            throw new ValidationException(validable, "kind is not set");
        }
    }

    @Override
    public void checkTapPosition(Validable validable, int lowTapPosition, Integer tapPosition, int stepSize) {
        if (tapPosition == null) {
            throw new ValidationException(validable, "tap position is not set");
        }
        int highTapPosition = lowTapPosition + stepSize;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(validable, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
    }

    @Override
    public void checkTargetDeadband(Validable validable, String validableType, boolean regulating, double targetDeadband) {
        ValidationUtil.checkTargetDeadband(validable, validableType, regulating, targetDeadband);
    }

    @Override
    public void checkTemporaryLimit(Validable validable, double value, Integer acceptableDuration) {
        if (Double.isNaN(value)) {
            throw new ValidationException(validable, "temporary limit value is not set");
        }
        if (value <= 0) {
            throw new ValidationException(validable, "temporary limit value must be > 0");
        }
        if (acceptableDuration == null) {
            throw new ValidationException(validable, "acceptable duration is not set");
        }
        if (acceptableDuration < 0) {
            throw new ValidationException(validable, "acceptable duration must be >= 0");
        }
    }

    @Override
    public void checkTemporaryLimitName(Validable validable, String name) {
        if (name == null) {
            throw new ValidationException(validable, "name is not set");
        }
    }

    @Override
    public void checkTemporaryLimits(Validable validable, double permanentLimit, Map<Integer, LoadingLimits.TemporaryLimit> temporaryLimits) {
        // check temporary limits are consistents with permanent
        double previousLimit = Double.NaN;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits.values()) { // iterate in ascending order
            if (tl.getValue() <= permanentLimit) {
                LOGGER.debug("{}, temporary limit should be greater than permanent limit", validable.getMessageHeader());
            }
            if (Double.isNaN(previousLimit)) {
                previousLimit = tl.getValue();
            } else if (tl.getValue() <= previousLimit) {
                LOGGER.debug("{} : temporary limits should be in ascending value order", validable.getMessageHeader());
            }
        }
        // check name unicity
        temporaryLimits.values().stream()
                .collect(Collectors.groupingBy(LoadingLimits.TemporaryLimit::getName))
                .forEach((name, temporaryLimits1) -> {
                    if (temporaryLimits1.size() > 1) {
                        throw new ValidationException(validable, temporaryLimits1.size() + "temporary limits have the same name " + name);
                    }
                });
    }

    @Override
    public void checkTopologyKind(Validable validable, TopologyKind topologyKind) {
        ValidationUtil.checkTopologyKind(validable, topologyKind);
    }

    @Override
    public void checkVoltageControl(Validable validable, Boolean voltageRegulatorOn, double voltageSetpoint) {
        ValidationUtil.checkVoltageControl(validable, voltageRegulatorOn, voltageSetpoint);
    }

    @Override
    public void checkVoltageControl(Validable validable, boolean voltageRegulatorOn, double voltageSetpoint, double reactivePowerSetpoint) {
        ValidationUtil.checkVoltageControl(validable, voltageRegulatorOn, voltageSetpoint, reactivePowerSetpoint);
    }

    @Override
    public void checkVoltageLimits(Validable validable, double lowVoltageLimit, double highVoltageLimit) {
        ValidationUtil.checkVoltageLimits(validable, lowVoltageLimit, highVoltageLimit);
    }

    @Override
    public void checkX(Validable validable, double x) {
        ValidationUtil.checkX(validable, x);
    }
}
